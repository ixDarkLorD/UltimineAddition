package net.ixdarklord.ultimine_addition.common.data.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.progression.UltimineNotice;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.ShapePayload;
import net.ixdarklord.ultimine_addition.network.payloads.SyncShapesPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Shapes players draw themselves (the Shape Designer, opened from the Shape Selector): 5 x 5 layers, the first around
// the block being broken and up to four more behind it, kept with the world and added to FTB Ultimine's shape list next to the data packs' shapes. Its maker
// learns it for every tool they can already Ultimine with; others only get it the way they get any shape (the Miner
// Certificate or a juice). A player may keep a few (progression.custom_shapes_per_player; 0 turns the designer off).
public class CustomShapes extends SavedData {
    public static final int SIZE = 5;
    // How many layers deep a shape can go: the face being broken, and the ones behind it.
    public static final int MAX_DEPTH = 5;
    private static final int MAX_NAME = 24;

    public record Entry(ResourceLocation id, UUID owner, String ownerName, String name, List<String> pattern) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(Entry::id),
                UUIDUtil.CODEC.fieldOf("owner").forGetter(Entry::owner),
                Codec.STRING.optionalFieldOf("owner_name", "").forGetter(Entry::ownerName),
                Codec.STRING.fieldOf("name").forGetter(Entry::name),
                Codec.STRING.listOf().fieldOf("pattern").forGetter(Entry::pattern)
        ).apply(instance, Entry::new));

        // The first five rows are the face being broken (a data pack shape's "pattern"); each five after them a layer
        // further in, given as [right, up, depth] blocks.
        DataShape toShape() {
            List<List<Integer>> blocks = new ArrayList<>();
            for (int index = SIZE; index < this.pattern.size(); index++) {
                String line = this.pattern.get(index);
                int layer = index / SIZE, row = index % SIZE;
                for (int column = 0; column < line.length(); column++) {
                    if (line.charAt(column) == '#') blocks.add(List.of(column - SIZE / 2, SIZE / 2 - row, layer));
                }
            }
            return DataShape.of(this.id, new DataShape.Definition(Optional.of(this.name), this.pattern.subList(0, Math.min(SIZE, this.pattern.size())), blocks, "none"));
        }
    }

    public static final Codec<CustomShapes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().optionalFieldOf("shapes", List.of()).forGetter(data -> data.entries),
            Codec.INT.optionalFieldOf("next", 1).forGetter(data -> data.next)
    ).apply(instance, CustomShapes::new));
    // 1.20.1 SavedData is plain NBT: load decodes it with the codec and save() encodes it back.
    public static final String DATA_KEY = FTBUltimineAddition.MOD_ID + ".custom_shapes";

    private final List<Entry> entries;
    private int next;

    public CustomShapes() {
        this(List.of(), 1);
    }

    private CustomShapes(List<Entry> entries, int next) {
        this.entries = new ArrayList<>(entries);
        this.next = next;
    }

    public static CustomShapes get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CustomShapes::load, CustomShapes::new, DATA_KEY);
    }

    private static CustomShapes load(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(FTBUltimineAddition.LOGGER::error)
                .orElseGet(CustomShapes::new);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CODEC.encodeStart(NbtOps.INSTANCE, this)
                .resultOrPartial(FTBUltimineAddition.LOGGER::error)
                .ifPresent(encoded -> { if (encoded instanceof CompoundTag compound) tag.merge(compound); });
        return tag;
    }

    public static boolean enabled() {
        return UAServerConfig.CUSTOM_SHAPES_PER_PLAYER.get() > 0 && !PlaystyleModes.isLegacy();
    }

    public List<Entry> ownedBy(UUID player) {
        return this.entries.stream().filter(entry -> entry.owner().equals(player)).toList();
    }

    public @Nullable Entry find(ResourceLocation id) {
        return this.entries.stream().filter(entry -> entry.id().equals(id)).findFirst().orElse(null);
    }

    /** Puts the world's player-made shapes into the shape list and tells every player (on start, and after a change). */
    public static void refresh(MinecraftServer server) {
        List<DataShape> shapes = new ArrayList<>();
        for (Entry entry : get(server).entries) {
            try {
                shapes.add(entry.toShape());
            } catch (RuntimeException e) {
                FTBUltimineAddition.LOGGER.error("Skipping the player-made shape {}: {}", entry.id(), e.toString());
            }
        }
        DataShapesManager.INSTANCE.setCustom(shapes);
        DataShapesManager.INSTANCE.install();
        PayloadHandler.sendToPlayers(SyncShapesPayload.of(DataShapesManager.INSTANCE.getShapes()), server.getPlayerList().getPlayers());
    }

    private static void tell(ServerPlayer player, boolean ok, Component line) {
        new UltimineNotice(UltimineNotice.Kind.ACTION, Component.translatable("gui.ultimine_addition.shape_designer.title"),
                List.of(line.copy().withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED)), new ItemStack(Items.FILLED_MAP)).send(player);
    }

    /** The player's own shapes, for the designer's list: sent when it opens and after each change. */
    public static void sendMine(ServerPlayer player) {
        List<ShapePayload.Saved> mine = get(player.level().getServer()).ownedBy(player.getUUID()).stream()
                .map(entry -> new ShapePayload.Saved(entry.id(), entry.name(), entry.pattern())).toList();
        PayloadHandler.sendToPlayer(new ShapePayload.Mine(mine, UAServerConfig.CUSTOM_SHAPES_PER_PLAYER.get()), player);
    }

    /**
     * A player's drawing, from the designer: checked here, since the client can send anything. With {@code replace} it
     * takes the place of one of the player's shapes (which keeps its id, so it stays learned and a favourite).
     */
    public static void create(ServerPlayer player, String rawName, List<String> rows, @Nullable ResourceLocation replace) {
        MinecraftServer server = player.level().getServer();
        if (!enabled()) {
            tell(player, false, Component.translatable("info.ultimine_addition.shape_designer.disabled"));
            return;
        }
        // The pattern: layers of 5 rows of 5. The first has the broken block in its middle; everywhere else, in every
        // layer, a block or nothing.
        int blocks = 0;
        boolean valid = !rows.isEmpty() && rows.size() % SIZE == 0 && rows.size() <= SIZE * MAX_DEPTH;
        for (int index = 0; valid && index < rows.size(); index++) {
            String line = rows.get(index);
            int row = index % SIZE;
            if (line.length() != SIZE) valid = false;
            for (int column = 0; valid && column < SIZE; column++) {
                char c = line.charAt(column);
                boolean origin = index < SIZE && row == SIZE / 2 && column == SIZE / 2;
                if (origin ? c != 'o' : c != '#' && c != '.') valid = false;
                if (c == '#') blocks++;
            }
        }
        if (!valid || blocks == 0) {
            tell(player, false, Component.translatable("info.ultimine_addition.shape_designer.invalid"));
            return;
        }
        int maxBlocks = UAServerConfig.CUSTOM_SHAPES_MAX_BLOCKS.get();
        if (blocks > maxBlocks) {
            tell(player, false, Component.translatable("info.ultimine_addition.shape_designer.too_many", maxBlocks));
            return;
        }
        CustomShapes data = get(server);
        Entry replaced = replace == null ? null : data.find(replace);
        if (replace != null && (replaced == null || !replaced.owner().equals(player.getUUID()))) {
            tell(player, false, Component.translatable("info.ultimine_addition.shape_designer.invalid"));
            return;
        }
        int limit = UAServerConfig.CUSTOM_SHAPES_PER_PLAYER.get();
        if (replaced == null && data.ownedBy(player.getUUID()).size() >= limit) {
            tell(player, false, Component.translatable("info.ultimine_addition.shape_designer.limit", limit));
            return;
        }
        // A shape of one's own is no way around the challenges: it is only learned for tools the player can already
        // Ultimine with (a Shape Certificate used for it, or the Miner Certificate for all of them).
        var ability = ServicePlatform.get().players().getAbilityData(player);
        List<String> tools = new ArrayList<>();
        for (MiningSkillCardItem.Type type : MiningSkillCardItem.Type.TYPES) {
            if (type == MiningSkillCardItem.Type.EMPTY) continue;
            if (ability.getAbility() || !ability.getUnlockedShapes(type.getId()).isEmpty()) tools.add(type.getId());
        }
        if (tools.isEmpty()) {
            tell(player, false, Component.translatable("info.ultimine_addition.shape_designer.locked"));
            return;
        }

        String name = rawName.strip().replaceAll("\\p{Cntrl}", "");
        if (name.length() > MAX_NAME) name = name.substring(0, MAX_NAME).strip();
        if (name.isEmpty()) name = Component.translatable("gui.ultimine_addition.shape_designer.default_name").getString();
        ResourceLocation id = replaced != null ? replaced.id() : FTBUltimineAddition.id("custom/" + player.getUUID().toString().substring(0, 8) + "_" + data.next++);
        Entry entry = new Entry(id, player.getUUID(), player.getScoreboardName(), name, List.copyOf(rows));
        if (replaced != null) data.entries.set(data.entries.indexOf(replaced), entry);
        else data.entries.add(entry);
        data.setDirty();
        refresh(server);
        for (String tool : tools) ServicePlatform.get().players().unlockShapes(player, tool, List.of(id), 0);
        sendMine(player);
        tell(player, true, Component.translatable(replaced != null ? "info.ultimine_addition.shape_designer.replaced" : "info.ultimine_addition.shape_designer.saved", name));
    }

    /** Removes a player-made shape: its maker may, and operators. Returns whether it was there and theirs to remove. */
    public static boolean delete(MinecraftServer server, UUID asker, boolean operator, ResourceLocation id) {
        CustomShapes data = get(server);
        Entry entry = data.find(id);
        if (entry == null || !operator && !entry.owner().equals(asker)) return false;
        data.entries.remove(entry);
        data.setDirty();
        refresh(server);
        return true;
    }
}
