package net.ixdarklord.ultimine_addition.common.undo;

import net.ixdarklord.coolcatcore.api.event.v2.common.EntityEvents;
import net.ixdarklord.coolcatcore.api.event.v2.common.PlayerEvents;
import net.ixdarklord.coolcatcore.api.event.v2.common.ServerLifecycleEvents;
import net.ixdarklord.coolcatcore.api.event.v2.common.ServerTickEvents;
import net.ixdarklord.coolcatcore.api.event.v2.core.EventResult;
import net.ixdarklord.ultimine_addition.common.data.challenge.IneligibleBlocksSavedData;
import net.ixdarklord.ultimine_addition.common.progression.UltimineNotice;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.UndoPayload;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// Undoing an Ultimine operation: blocks go back, paid for with the items (and experience) they dropped, taken from the
// drops still on the ground first, then from the player's inventory. Nothing changes when something is missing.
// The FTB Ultimine mixin records each operation; players preview it first, then confirm.
public final class UltimineUndo {
    public static final String DISPLAY_TAG = "ultimine_addition.undo";
    private static final int GROW_TICKS = 10;
    // Players this close to an undo see its blocks grow back.
    private static final int GROW_RANGE = 96;
    private static final double MAX_DISTANCE = 64.0;

    private static final Map<UUID, Deque<Operation>> HISTORY = new HashMap<>();
    private static final List<Placement> PLACEMENTS = new ArrayList<>();
    private static final List<Job> JOBS = new ArrayList<>();
    // Undos confirmed (and paid for) while another was still growing back; each starts when the one before ends.
    private static final Map<UUID, Deque<Operation>> QUEUED = new HashMap<>();
    private static @Nullable Operation recording;

    private UltimineUndo() {}

    public static void init() {
        ServerTickEvents.END.register(UltimineUndo::tick);
        PlayerEvents.LEAVE.register(player -> {
            finishPlacements(p -> p.player == player);
            // Already paid for: put them in now rather than lose them.
            Deque<Operation> queued = QUEUED.remove(player.getUUID());
            if (queued != null) queued.forEach(op -> placeNow(player, op));
            HISTORY.remove(player.getUUID());
        });
        ServerLifecycleEvents.STOPPING.register(server -> {
            finishPlacements(p -> true);
            for (Map.Entry<UUID, Deque<Operation>> entry : QUEUED.entrySet()) {
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player != null) entry.getValue().forEach(op -> placeNow(player, op));
            }
            QUEUED.clear();
            HISTORY.clear();
        });
        // Growth displays older versions could leave in a saved chunk mid-animation.
        EntityEvents.LOAD.register((entity, level) -> {
            if (entity instanceof Display.BlockDisplay && entity.entityTags().contains(DISPLAY_TAG)) {
                entity.discard();
            }
        });
    }

    // --- Recording (called from FTBUltimineMixin while FTB Ultimine breaks the blocks) ---

    public static void begin(ServerPlayer player, BlockPos origin) {
        recording = UAServerConfig.UNDO_ENABLED.get() ? new Operation(player, origin) : null;
    }

    // contents: what a container block held just before it broke; blockDrops: what that break dropped. Blocks with
    // block entities come back empty, so the contents that spilled out are left out of the cost (they stay the
    // player's). Only this block's own drops are matched, so the same items mined elsewhere still count.
    // neighboursBefore: the six neighbours just before the break (see neighbours), to find linked halves it took along.
    public static void recordBlock(ServerPlayer player, BlockPos pos, BlockState before, BlockState[] neighboursBefore,
                                   List<ItemStack> contents, List<ItemStack> blockDrops) {
        Operation op = recording;
        ServerLevel level = player.level();
        if (op == null || !op.player.equals(player.getUUID()) || level.getBlockState(pos).equals(before)) return;
        // Blocks the break took along come back with it: the other half of a door, bed or tall plant (which drops
        // nothing of its own), and what hung on the block and popped off (a torch, a rail, a button...), whose dropped
        // items join the cost so undoing takes them back instead of duplicating them.
        List<BlockRecord> linked = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            BlockState neighbourBefore = neighboursBefore[direction.ordinal()];
            BlockState neighbourNow = level.getBlockState(neighbour);
            if (neighbourBefore.isAir() || neighbourNow.is(neighbourBefore.getBlock()) || !neighbourNow.canBeReplaced()
                    || op.recorded.containsKey(neighbour)) continue;
            linked.add(new BlockRecord(neighbour.immutable(), op.withRecordedNeighbours(level, neighbour, neighbourBefore), List.of()));
            if (!neighbourBefore.is(before.getBlock())) {
                for (ItemEntity dropped : level.getEntitiesOfClass(ItemEntity.class, new AABB(neighbour).inflate(0.25),
                        entity -> entity.tickCount == 0 && !op.attachedEntities.contains(entity))) {
                    op.attachedEntities.add(dropped);
                    op.attachedDrops.add(dropped.getItem().copy());
                }
            }
        }
        BlockRecord record = new BlockRecord(pos.immutable(), op.withRecordedNeighbours(level, pos, before), List.copyOf(linked));
        op.blocks.add(record);
        op.recorded.put(record.pos(), record.state());
        for (BlockRecord half : linked) op.recorded.put(half.pos(), half.state());
        if (contents.isEmpty() || blockDrops.isEmpty()) return;
        List<ItemStack> spilled = merge(blockDrops);
        for (ItemStack content : merge(contents)) {
            for (ItemStack drop : spilled) {
                if (!ItemStack.isSameItemSameComponents(drop, content)) continue;
                int count = Math.min(drop.getCount(), content.getCount());
                if (count > 0) op.contents.add(content.copyWithCount(count));
            }
        }
    }

    // A block's six neighbours, by Direction ordinal, for recordBlock.
    public static BlockState[] neighbours(Level level, BlockPos pos) {
        BlockState[] states = new BlockState[Direction.values().length];
        for (Direction direction : Direction.values()) states[direction.ordinal()] = level.getBlockState(pos.relative(direction));
        return states;
    }

    public static void recordDrops(List<ItemStack> drops, List<ItemEntity> entities) {
        Operation op = recording;
        if (op == null) return;
        List<ItemStack> all = new ArrayList<>(subtract(merge(drops), op.contents));
        all.addAll(op.attachedDrops);
        op.drops = merge(all);
        List<ItemEntity> allEntities = new ArrayList<>(entities);
        allEntities.addAll(op.attachedEntities);
        op.dropEntities = allEntities;
        op.dropsRecorded = true;
    }

    public static void finish(ServerPlayer player, boolean broke) {
        // FTB Ultimine breaks each block through the block-break event, which calls handleBlockBreak again and returns
        // false straight away; only the outer call that did the work (true) ends the recording.
        if (!broke) return;
        Operation op = recording;
        recording = null;
        if (op == null || op.blocks.isEmpty() || !op.player.equals(player.getUUID())) return;
        // FTB Ultimine had nothing to drop: what popped off is still paid for.
        if (!op.dropsRecorded) {
            op.drops = merge(op.attachedDrops);
            op.dropEntities = List.copyOf(op.attachedEntities);
        }
        // FTB Ultimine drops the operation's experience as one orb at the origin, spawned last.
        player.level().getEntitiesOfClass(ExperienceOrb.class, new AABB(op.origin).inflate(1.5), orb -> orb.tickCount == 0)
                .stream().findFirst().ifPresent(orb -> {
                    op.orb = orb;
                    op.xp = orb.getValue();
                });
        Deque<Operation> history = HISTORY.computeIfAbsent(player.getUUID(), uuid -> new ArrayDeque<>());
        history.addFirst(op);
        while (history.size() > UAServerConfig.UNDO_HISTORY.get()) history.removeLast();
    }

    // --- Preview and undo ---

    public static void preview(ServerPlayer player) {
        Operation op = latest(player);
        if (op == null) return;
        Cost cost = cost(player, op);
        List<BlockPos> positions = op.all().map(BlockRecord::pos).toList();
        List<BlockState> states = op.all().map(BlockRecord::state).toList();
        int available = HISTORY.getOrDefault(player.getUUID(), new ArrayDeque<>()).size();
        long expiresIn = Math.max(0L, op.time + UAServerConfig.UNDO_WINDOW.get() * 1000L - System.currentTimeMillis());
        PayloadHandler.sendToPlayer(new UndoPayload.Preview(positions, states, cost.items, cost.fromGround, op.xp, op.orb != null && op.orb.isAlive(), cost.free,
                available, UAServerConfig.UNDO_HISTORY.get(), expiresIn), player);
    }

    public static void confirm(ServerPlayer player) {
        Operation op = latest(player);
        if (op == null) return;
        Cost cost = cost(player, op);
        List<Component> missing = cost.missing(player, op);
        if (!missing.isEmpty()) {
            fail(player, missing);
            return;
        }
        if (!cost.free) cost.pay(player, op);
        Objects.requireNonNull(HISTORY.get(player.getUUID())).removeFirst();
        if (isBusy(player)) {
            // Waits for the undo growing back now; the progress HUD shows how many are waiting.
            QUEUED.computeIfAbsent(player.getUUID(), uuid -> new ArrayDeque<>()).addLast(op);
            JOBS.stream().filter(job -> job.player == player).forEach(Job::sendIfChanged);
        } else {
            schedule(player, op);
        }
    }

    private static boolean isBusy(ServerPlayer player) {
        return JOBS.stream().anyMatch(job -> job.player == player);
    }

    private static int queuedCount(ServerPlayer player) {
        Deque<Operation> queued = QUEUED.get(player.getUUID());
        return queued == null ? 0 : queued.size();
    }

    // Puts an operation's blocks back at once, without the animation.
    private static void placeNow(ServerPlayer player, Operation op) {
        Job job = new Job(player, op.blocks.size());
        for (int i = 0; i < op.blocks.size(); i++) {
            BlockRecord block = op.blocks.get(i);
            new Placement(player, player.level(), block, 0, job).place(i == 0);
        }
    }

    // The newest operation that can still be undone, or null after telling the player why not.
    private static @Nullable Operation latest(ServerPlayer player) {
        if (!UAServerConfig.UNDO_ENABLED.get()) {
            fail(player, List.of(Component.translatable("info.ultimine_addition.undo.disabled")));
            return null;
        }
        Deque<Operation> history = HISTORY.get(player.getUUID());
        long oldest = System.currentTimeMillis() - UAServerConfig.UNDO_WINDOW.get() * 1000L;
        if (history != null) history.removeIf(op -> op.time < oldest);
        Operation op = history == null ? null : history.peekFirst();
        if (op == null) {
            fail(player, List.of(Component.translatable("info.ultimine_addition.undo.nothing")));
            return null;
        }
        if (!player.level().dimension().equals(op.dimension) || player.position().distanceTo(op.origin.getCenter()) > MAX_DISTANCE) {
            fail(player, List.of(Component.translatable("info.ultimine_addition.undo.too_far")));
            return null;
        }
        ServerLevel level = player.level();
        if (op.all().anyMatch(b -> !level.getBlockState(b.pos()).canBeReplaced())) {
            fail(player, List.of(Component.translatable("info.ultimine_addition.undo.blocked")));
            return null;
        }
        return op;
    }

    private static void fail(ServerPlayer player, List<Component> lines) {
        List<Component> body = new ArrayList<>();
        body.add(Component.translatable("info.ultimine_addition.undo.failed").withStyle(ChatFormatting.RED));
        body.addAll(lines);
        new UltimineNotice(UltimineNotice.Kind.ACTION, UltimineNotice.actionsTitle(), body, ItemStack.EMPTY).send(player);
        PayloadHandler.sendToPlayer(new UndoPayload.Close(), player);
    }

    private static Cost cost(ServerPlayer player, Operation op) {
        boolean free = player.isCreative() || op.creative;
        List<Integer> fromGround = new ArrayList<>();
        for (ItemStack needed : op.drops) {
            int count = 0;
            for (ItemEntity entity : op.dropEntities) {
                if (entity.isAlive() && matches(entity.getItem(), needed)) count += entity.getItem().getCount();
            }
            fromGround.add(Math.min(count, needed.getCount()));
        }
        return new Cost(op.drops, fromGround, free);
    }

    public static int countInInventory(net.minecraft.world.entity.player.Player player, ItemStack stack) {
        int count = 0;
        for (ItemStack slot : player.getInventory().getNonEquipmentItems()) {
            if (matches(slot, stack)) count += slot.getCount();
        }
        ItemStack offhand = player.getOffhandItem();
        if (matches(offhand, stack)) count += offhand.getCount();
        return count;
    }

    private static List<ItemStack> subtract(List<ItemStack> stacks, List<ItemStack> removed) {
        List<ItemStack> left = new ArrayList<>();
        for (ItemStack stack : stacks) left.add(stack.copy());
        for (ItemStack remove : removed) {
            int count = remove.getCount();
            for (ItemStack stack : left) {
                if (count <= 0) break;
                if (!ItemStack.isSameItemSameComponents(stack, remove)) continue;
                int take = Math.min(count, stack.getCount());
                stack.shrink(take);
                count -= take;
            }
        }
        left.removeIf(ItemStack::isEmpty);
        return left;
    }

    // Items that keep their block's contents (shulker boxes) match whatever they hold: the block comes back empty, so
    // any such box pays, and what's inside goes back to the player (see Cost.pay).
    private static boolean matches(ItemStack stack, ItemStack needed) {
        return ItemStack.isSameItemSameComponents(withoutContents(stack), withoutContents(needed));
    }

    private static ItemStack withoutContents(ItemStack stack) {
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents == null || contents.equals(ItemContainerContents.EMPTY)) return stack;
        ItemStack copy = stack.copy();
        copy.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        return copy;
    }

    // Takes an item's stored contents out, for handing back after the item itself is paid.
    private static void takeContents(ItemStack stack, List<ItemStack> refund) {
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents != null) contents.nonEmptyItemCopyStream().forEach(refund::add);
    }

    private static List<ItemStack> merge(List<ItemStack> stacks) {
        List<ItemStack> merged = new ArrayList<>();
        outer:
        for (ItemStack stack : stacks) {
            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    existing.grow(stack.getCount());
                    continue outer;
                }
            }
            merged.add(stack.copy());
        }
        return merged;
    }

    private record Cost(List<ItemStack> items, List<Integer> fromGround, boolean free) {
        List<Component> missing(ServerPlayer player, Operation op) {
            List<Component> lines = new ArrayList<>();
            if (this.free) return lines;
            for (int i = 0; i < this.items.size(); i++) {
                ItemStack needed = this.items.get(i);
                int have = this.fromGround.get(i) + countInInventory(player, needed);
                if (have < needed.getCount()) {
                    lines.add(Component.translatable("info.ultimine_addition.undo.missing_item", needed.getCount() - have, needed.getHoverName()));
                }
            }
            boolean orbLeft = op.orb != null && op.orb.isAlive();
            if (op.xp > 0 && !orbLeft && player.totalExperience < op.xp) {
                lines.add(Component.translatable("info.ultimine_addition.undo.missing_xp", op.xp - player.totalExperience));
            }
            if (!lines.isEmpty()) lines.addFirst(Component.translatable("info.ultimine_addition.undo.missing"));
            return lines.size() > 6 ? List.of(lines.get(0), lines.get(1), lines.get(2), lines.get(3), lines.get(4),
                    Component.translatable("info.ultimine_addition.undo.missing_more", lines.size() - 5)) : lines;
        }

        void pay(ServerPlayer player, Operation op) {
            List<ItemStack> refund = new ArrayList<>();
            for (ItemStack needed : this.items) {
                int left = needed.getCount();
                for (ItemEntity entity : op.dropEntities) {
                    if (left <= 0) break;
                    ItemStack ground = entity.getItem();
                    if (!entity.isAlive() || !matches(ground, needed)) continue;
                    int take = Math.min(left, ground.getCount());
                    takeContents(ground, refund);
                    left -= take;
                    if (take >= ground.getCount()) entity.discard();
                    else entity.setItem(ground.copyWithCount(ground.getCount() - take));
                }
                List<ItemStack> slots = new ArrayList<>(player.getInventory().getNonEquipmentItems());
                slots.add(player.getOffhandItem());
                for (ItemStack slot : slots) {
                    if (left <= 0) break;
                    if (!matches(slot, needed)) continue;
                    int take = Math.min(left, slot.getCount());
                    takeContents(slot, refund);
                    slot.shrink(take);
                    left -= take;
                }
            }
            if (op.xp > 0) {
                if (op.orb != null && op.orb.isAlive()) op.orb.discard();
                else player.giveExperiencePoints(-op.xp);
            }
            // What paid boxes held goes back to the player (dropped at their feet when there's no room).
            for (ItemStack stack : refund) player.getInventory().placeItemBackInInventory(stack);
            player.getInventory().setChanged();
        }
    }

    // --- Putting the blocks back ---

    private static void schedule(ServerPlayer player, Operation op) {
        ServerLevel level = player.level();
        MinecraftServer server = level.getServer();
        // In the order FTB Ultimine broke them: the first broken block grows back first, the last one last.
        List<BlockRecord> blocks = new ArrayList<>(op.blocks);
        boolean animated = UAServerConfig.UNDO_ANIMATION.get();
        int perTick = UAServerConfig.UNDO_BLOCKS_PER_TICK.get();
        int now = server.getTickCount();
        // The client's undo HUD follows the placing through Progress packets.
        Job job = new Job(player, blocks.size());
        for (int i = 0; i < blocks.size(); i++) {
            BlockRecord block = blocks.get(i);
            // Each block's growth starts a fraction of a tick after the previous one (a steady wave, not per-tick
            // batches); its real block goes in on the first whole tick after that growth has finished.
            Placement placement = new Placement(player, level, block, animated ? now + Mth.ceil((float) i / perTick) : now, job);
            if (animated) PLACEMENTS.add(placement);
            else placement.place(i == 0);
        }
        job.sendIfChanged();
        if (animated && !blocks.isEmpty()) {
            JOBS.add(job);
            List<BlockPos> positions = new ArrayList<>();
            List<BlockState> states = new ArrayList<>();
            List<Float> starts = new ArrayList<>();
            for (int i = 0; i < blocks.size(); i++) {
                float start = (float) i / perTick;
                for (BlockRecord part : blocks.get(i).withLinked()) {
                    positions.add(part.pos());
                    states.add(part.state());
                    starts.add(start);
                }
            }
            PayloadHandler.sendToTarget(new UndoPayload.Grow(player.getUUID(), positions, states, starts, GROW_TICKS), level, op.origin, GROW_RANGE);
        }
    }

    // One confirmed undo being placed, for the progress sent to its player.
    private static final class Job {
        private final ServerPlayer player;
        private final int total;
        private int placed;
        private int sent = -1;
        private int sentQueued = -1;

        private Job(ServerPlayer player, int total) {
            this.player = player;
            this.total = total;
        }

        private boolean sendIfChanged() {
            int queued = queuedCount(this.player);
            if (this.placed != this.sent || queued != this.sentQueued) {
                this.sent = this.placed;
                this.sentQueued = queued;
                if (!this.player.hasDisconnected()) PayloadHandler.sendToPlayer(new UndoPayload.Progress(this.placed, this.total, queued), this.player);
            }
            return this.placed >= this.total;
        }
    }

    private static void tick(MinecraftServer server) {
        tickPlacements(server);
        List<ServerPlayer> finished = new ArrayList<>();
        JOBS.removeIf(job -> {
            boolean done = job.sendIfChanged();
            if (done) finished.add(job.player);
            return done;
        });
        // Each finished undo hands over to the next one its player queued.
        for (ServerPlayer player : finished) {
            Deque<Operation> queued = QUEUED.get(player.getUUID());
            Operation next = queued == null ? null : queued.pollFirst();
            if (queued != null && queued.isEmpty()) QUEUED.remove(player.getUUID());
            if (next != null && !player.hasDisconnected()) schedule(player, next);
        }
    }

    private static void tickPlacements(MinecraftServer server) {
        if (PLACEMENTS.isEmpty()) return;
        int now = server.getTickCount();
        Set<Level> soundPlayed = new HashSet<>();
        Iterator<Placement> it = PLACEMENTS.iterator();
        while (it.hasNext()) {
            Placement p = it.next();
            int age = now - p.startTick;
            if (age < 0) continue;
            // Clients draw the block growing (UndoGrowthClient); the real one goes in once that has finished, with a
            // tick to spare for the packet having arrived late.
            if (age >= GROW_TICKS + 1) {
                p.placeBlock(soundPlayed.add(p.level));
                it.remove();
            }
        }
    }

    private static void finishPlacements(java.util.function.Predicate<Placement> filter) {
        Iterator<Placement> it = PLACEMENTS.iterator();
        while (it.hasNext()) {
            Placement p = it.next();
            if (!filter.test(p)) continue;
            p.place(false);
            it.remove();
        }
        JOBS.removeIf(job -> job.placed >= job.total || job.player.hasDisconnected());
    }

    private static final class Placement {
        private final ServerPlayer player;
        private final ServerLevel level;
        private final BlockPos pos;
        private final BlockState state;
        private final List<BlockRecord> linked;
        private final int startTick;
        private final Job job;

        private Placement(ServerPlayer player, ServerLevel level, BlockRecord block, int startTick, Job job) {
            this.player = player;
            this.level = level;
            this.pos = block.pos();
            this.state = block.state();
            this.linked = block.linked();
            this.startTick = startTick;
            this.job = job;
        }

        // Finishing right away (logout, server stop, or no animation).
        void place(boolean sound) {
            this.placeBlock(sound);
        }

        private boolean placed;

        void placeBlock(boolean sound) {
            if (this.placed) return;
            this.placed = true;
            this.job.placed++;
            if (this.level.getBlockState(this.pos).canBeReplaced()) {
                // The block, then its other half in the same tick, so neither is ever left alone to be updated away.
                this.level.setBlock(this.pos, this.state, Block.UPDATE_ALL);
                // Blocks put back by undo don't count for challenges again.
                IneligibleBlocksSavedData.getOrCreate(this.level).add(this.player, new IneligibleBlocksSavedData.BlockInfo(this.pos, this.state));
                for (BlockRecord half : this.linked) {
                    if (!this.level.getBlockState(half.pos()).canBeReplaced()) continue;
                    this.level.setBlock(half.pos(), half.state(), Block.UPDATE_ALL);
                    IneligibleBlocksSavedData.getOrCreate(this.level).add(this.player, new IneligibleBlocksSavedData.BlockInfo(half.pos(), half.state()));
                }
                if (sound) {
                    var soundType = this.state.getSoundType();
                    this.level.playSound(null, this.pos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
                }
            } else {
                // Taken since the undo started: drop it instead, so nothing paid for is lost.
                Block.dropResources(this.state, this.level, this.pos);
            }
        }
    }

    // linked: what the same break removed with it (the other half of a door, bed or tall plant, a torch hung on it...),
    // placed right after it.
    private record BlockRecord(BlockPos pos, BlockState state, List<BlockRecord> linked) {
        private List<BlockRecord> withLinked() {
            if (this.linked.isEmpty()) return List.of(this);
            List<BlockRecord> all = new ArrayList<>(this.linked.size() + 1);
            all.add(this);
            all.addAll(this.linked);
            return all;
        }
    }

    private static final class Operation {
        private final UUID player;
        private final ResourceKey<Level> dimension;
        private final BlockPos origin;
        private final boolean creative;
        private final long time = System.currentTimeMillis();
        private final List<BlockRecord> blocks = new ArrayList<>();
        // Every recorded state by position, linked halves included.
        private final Map<BlockPos, BlockState> recorded = new HashMap<>();
        // Items that spilled out of broken containers: not part of the cost.
        private final List<ItemStack> contents = new ArrayList<>();
        private List<ItemStack> drops = List.of();
        private List<ItemEntity> dropEntities = List.of();
        // What popped off with the broken blocks (torches, rails...) dropped by itself, beside FTB Ultimine's drops.
        private final List<ItemStack> attachedDrops = new ArrayList<>();
        private final List<ItemEntity> attachedEntities = new ArrayList<>();
        private boolean dropsRecorded;
        private @Nullable ExperienceOrb orb;
        private int xp;

        private java.util.stream.Stream<BlockRecord> all() {
            return this.blocks.stream().flatMap(block -> block.withLinked().stream());
        }

        // FTB Ultimine breaks blocks one by one, and each break updates the neighbours' shapes: a glass pane next to one
        // broken earlier has already dropped that side. The state is recorded as it was with those neighbours still
        // there, so panes, fences, walls and the like come back connected.
        private BlockState withRecordedNeighbours(ServerLevel level, BlockPos pos, BlockState state) {
            BlockState shaped = state;
            for (Direction direction : Direction.values()) {
                BlockPos neighbour = pos.relative(direction);
                BlockState neighbourState = this.recorded.get(neighbour);
                if (neighbourState == null) continue;
                BlockState updated = shaped.updateShape(level, level, pos, direction, neighbour, neighbourState, level.getRandom());
                if (!updated.isAir()) shaped = updated;
            }
            return shaped;
        }

        private Operation(ServerPlayer player, BlockPos origin) {
            this.player = player.getUUID();
            this.dimension = player.level().dimension();
            this.origin = origin.immutable();
            this.creative = player.isCreative();
        }
    }
}
