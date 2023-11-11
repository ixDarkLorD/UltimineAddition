package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.datafixers.util.Pair;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.container.SkillsRecordContainer;
import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.chunk.ChunkManager;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.PenItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SkillsRecordData extends DataHandler<SkillsRecordData, ItemStack> {
    private ItemStack stack;
    private UUID uuid;
    private Container container;
    private byte viewingCard;
    private boolean consumeMode;
    private final Map<Integer, List<ResourceLocation>> pinnedChallenges = new TreeMap<>();

    public Pair<Boolean, Boolean> initTaskValidator(BlockState state, BlockPos pos, Player player, ChallengesData.Type challengeType) {
        boolean b1 = false;
        boolean b2 = false;
        for (int i = 0; i < getCardSlots().size(); i++) {
            ItemStack stack = getCardSlots().get(i);
            if (stack == ItemStack.EMPTY) continue;
            var pair = this.validateTask(stack, state, pos, player, challengeType);
            if (pair.getFirst()) {
                b1 = true;
            }
            if (pair.getSecond()) {
                b2 = true;
            }
        }
        return Pair.of(b1, b2);
    }

    private Pair<Boolean, Boolean> validateTask(ItemStack stack, BlockState state, BlockPos pos, Player player, ChallengesData.Type challengeType) {
        AtomicReference<Pair<Boolean, Boolean>> isConsumed = new AtomicReference<>(Pair.of(false, false));
        try {
            AtomicInteger i = new AtomicInteger();
            MiningSkillCardData cardData = new MiningSkillCardData().loadData(stack).sendToClient((ServerPlayer) player);
            cardData.getChallenges().forEach((identifier, values) -> {
                if (i.get() == 0) {
                    var challengeData = ChallengesManager.INSTANCE.getAllChallenges().get(identifier.id());
                    List<Block> blocks = ChallengesManager.INSTANCE.utilizeTargetedBlocks(challengeData);
                    ChunkAccess chunk = player.getLevel().getChunk(pos);
                    int inkChamber = getPenSlot().getItem() instanceof PenItem
                            ? ((PenItem)getAllSlots().get(4).getItem()).getData(getAllSlots().get(4)).getCapacity()
                            : 0;

                    boolean hasCorrectGamemode = !player.isCreative() && !player.isSpectator();
                    boolean isMissingRequiredItems = hasCorrectGamemode && (getAllSlots().get(4).isEmpty() || getAllSlots().get(5).isEmpty());
                    boolean notEnoughInk = hasCorrectGamemode && inkChamber == 0;
                    boolean isChallengeAccomplished = cardData.isChallengeAccomplished(identifier.id());
                    boolean isCorrectAction = challengeData.getChallengeType().equals(challengeType) || challengeData.getChallengeType().equals(challengeType.getConsumeVersion());
                    boolean isValidBlock = blocks.contains(state.getBlock());
                    boolean isCorrectTool = !hasCorrectGamemode || ChallengesManager.INSTANCE.isCorrectTool(player, challengeData);
                    boolean isBlockPlacedByEntity = ConfigHandler.COMMON.IS_PLACED_BY_ENTITY_CONDITION.get() && hasCorrectGamemode && !state.is(ModBlockTags.DENY_IS_PLACED_BY_ENTITY) && ChunkManager.INSTANCE.getChunkData(chunk).isBlockPlacedByEntity(pos);

                    if (ConfigHandler.COMMON.CHALLENGE_ACTIONS_LOGGER.get()) {
                        ChallengesManager.LOGGER.debug("/===========================================/");
                        ChallengesManager.LOGGER.debug("Challenge: {}", identifier.id());
                        ChallengesManager.LOGGER.debug("hasCorrectGamemode: {}", hasCorrectGamemode);
                        ChallengesManager.LOGGER.debug("isMissingRequiredItems: {}", isMissingRequiredItems);
                        ChallengesManager.LOGGER.debug("notEnoughInk: {}", notEnoughInk);
                        ChallengesManager.LOGGER.debug("isChallengeAccomplished: {}", isChallengeAccomplished);
                        ChallengesManager.LOGGER.debug("isCorrectAction: {}", isCorrectAction);
                        ChallengesManager.LOGGER.debug("isValidBlock: {}", isValidBlock);
                        ChallengesManager.LOGGER.debug("isCorrectTool: {}", isCorrectTool);
                        ChallengesManager.LOGGER.debug("isBlockPlacedByEntity: {}", isBlockPlacedByEntity);
                        ChallengesManager.LOGGER.debug("/===========================================/");
                    }

                    if (isBlockPlacedByEntity) {
                        ChunkManager.INSTANCE.getChunkData(chunk).getPlacedBlocks().forEach((entityIdentifier, placedBlocks) -> {
                            var list = placedBlocks.stream().filter(blockInfo -> blockInfo.pos().equals(pos)).toList();
                            if (!list.isEmpty()) {
                                MutableComponent component = new TranslatableComponent("info.ultimine_addition.placed_by_entity", new TranslatableComponent("entity.%s.%s".formatted(entityIdentifier.id().getNamespace(), entityIdentifier.id().getPath())));
                                player.displayClientMessage(component.withStyle(ChatFormatting.RED), true);
                            }
                        });
                    }
                    if (!isMissingRequiredItems && !notEnoughInk && !isChallengeAccomplished && isCorrectAction && isValidBlock && isCorrectTool && !isBlockPlacedByEntity) {
                        if (challengeData.getChallengeType().isConsuming()) {
                            if (consumeMode) {
                                cardData.addAmount(identifier.id(), 1).saveData(stack);
                                if (hasCorrectGamemode) consumeContents((ServerPlayer) player);
                                isConsumed.set(Pair.of(true, true));
                                player.getLevel().destroyBlock(pos, false, player);
                            }
                        } else {
                            cardData.addAmount(identifier.id(), 1).saveData(stack);
                            if (hasCorrectGamemode) consumeContents((ServerPlayer) player);
                            isConsumed.set(Pair.of(true, false));
                        }
                        i.getAndIncrement();
                    }
                }
            });
        } catch (ConcurrentModificationException ignored) {}
        return isConsumed.get();
    }

    public SkillsRecordData togglePinned(int slot, ResourceLocation challengeId) {
        if (this.pinnedChallenges.containsKey(slot)) {
            if (!this.pinnedChallenges.get(slot).contains(challengeId)) {
                this.pinnedChallenges.get(slot).add(challengeId);
            }
        } else this.pinnedChallenges.put(slot, new ArrayList<>(List.of(challengeId)));

        ItemStack itemStack = getCardSlots().get(slot);
        MiningSkillCardData data = new MiningSkillCardData().loadData(itemStack);
        MiningSkillCardData.InfoData infoData = data.getChallenge(challengeId);
        if (infoData != null) {
            infoData.togglePinned();
            data.saveData(itemStack);
        }
        return this;
    }

    private void consumeContents(ServerPlayer player) {
        ItemStack pen = getPenSlot();
        ItemStack paper = getPaperSlot();

        if (pen.getItem() instanceof PenItem item) {
            item.getData(pen).sendToClient(player).removeAmount(1).saveData(pen);
        }
        if (paper.getItem() == Items.PAPER) {
            boolean chance = ThreadLocalRandom.current().nextDouble() < ConfigHandler.COMMON.PAPER_CONSUMPTION_RATE.get();
            paper.shrink(chance ? 1 : 0);
        }
    }

    public NonNullList<ItemStack> getCardSlots() {
        NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            if (container.getItem(i).getItem() instanceof MiningSkillCardItem) {
                items.set(i, container.getItem(i));
            }
        }
        return items;
    }

    public NonNullList<ItemStack> getAllSlots() {
        NonNullList<ItemStack> items = NonNullList.withSize(this.container.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            if (container.getItem(i) != ItemStack.EMPTY) {
                items.set(i, container.getItem(i));
            }
        }
        return items;
    }

    public ItemStack getPenSlot() {
        return getAllSlots().get(4);
    }

    private ItemStack getPaperSlot() {
        return getAllSlots().get(5);
    }

    public boolean isConsumeMode() {
        return consumeMode;
    }

    public byte getViewingCard() {
        return viewingCard;
    }

    public Container getContainer() {
        return container;
    }

    public ItemStack get() {
        return this.stack;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public SkillsRecordData insertContainer(Container container) {
        this.container = container;
        return this;
    }

    public SkillsRecordData setViewingCard(int selectedSlot) {
        this.viewingCard = (byte) selectedSlot;
        return this;
    }

    public SkillsRecordData toggleConsumeMode() {
        this.consumeMode ^= true;
        return this;
    }

    @Override
    public void saveData(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(this.NBTBase);
        if (NBT == null) NBT = new CompoundTag();

        NBT.putUUID("UUID", this.uuid == null ? UUID.randomUUID() : this.uuid);
        NBT.merge(getNBTFromContainer(this.container));
        NBT.putByte("ViewingCard", viewingCard);
        NBT.putBoolean("ConsumeMode", this.consumeMode);
        stack.getOrCreateTag().put(this.NBTBase, NBT);
        super.saveData(stack);
    }

    public SkillsRecordData syncData(ServerPlayer player) {
        if (player.containerMenu instanceof SkillsRecordContainer skillsRecordContainer) {
            this.pinnedChallenges.forEach((slot, challengeList) -> {
                ItemStack cardStack = skillsRecordContainer.getCardSlots().get(slot).getItem();
                MiningSkillCardData cardData = new MiningSkillCardData().loadData(cardStack);
                challengeList.forEach(location -> {
                    MiningSkillCardData.InfoData infoData = cardData.getChallenge(location);
                    if (infoData != null) {
                        infoData.togglePinned();
                        cardData.saveData(cardStack);
                    }
                });
            });
        }
        return this;
    }

    @Override
    public SkillsRecordData loadData(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(this.NBTBase);
        if (NBT == null) NBT = new CompoundTag();

        this.uuid = NBT.contains("UUID") ? NBT.getUUID("UUID") : null;
        this.container = getContainerFromNBT(NBT);
        this.viewingCard = NBT.contains("ViewingCard") ? NBT.getByte("ViewingCard") : -1;
        this.consumeMode = NBT.getBoolean("ConsumeMode");
        this.stack = stack;
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeItem(this.stack);
        buf.writeByte(this.viewingCard);
        buf.writeBoolean(this.consumeMode);
        this.encodePinnedChallenges(buf);
    }

    public static SkillsRecordData fromNetwork(FriendlyByteBuf buf) {
        return new SkillsRecordData().loadData(buf.readItem())
                .setViewingCard(buf.readByte())
                .setConsumeMode(buf.readBoolean())
                .decodePinnedChallenges(buf);
    }

    private void encodePinnedChallenges(FriendlyByteBuf buf) {
        buf.writeMap(this.pinnedChallenges,
                FriendlyByteBuf::writeInt,
                (buffer, locationList) -> buffer.writeCollection(locationList, FriendlyByteBuf::writeResourceLocation));
    }

    private SkillsRecordData decodePinnedChallenges(FriendlyByteBuf buf) {
        this.pinnedChallenges.putAll(buf.readMap(FriendlyByteBuf::readInt, buffer -> buffer.readList(FriendlyByteBuf::readResourceLocation)));
        return this;
    }

    private SkillsRecordData setConsumeMode(boolean consumeMode) {
        this.consumeMode = consumeMode;
        return this;
    }

    private static CompoundTag getNBTFromContainer(Container container) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.isEmpty()) break;
            var itemStack = container.getItem(i);
            if (itemStack.isEmpty()) continue;

            var tag = new CompoundTag();
            tag.putByte("Slot", (byte) i);
            itemStack.save(tag);
            listTag.add(tag);
        }
        var tag = new CompoundTag();
        tag.put("Contents", listTag);
        return tag;
    }

    private static Container getContainerFromNBT(CompoundTag NBT) {
        Container container = new SimpleContainer(SkillsRecordItem.CONTAINER_SIZE);
        ListTag listTag = new ListTag();
        if (NBT != null) listTag = NBT.getList("Contents", 10);

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundTag = listTag.getCompound(i);
            int j = compoundTag.getByte("Slot") & 255;
            if (j < container.getContainerSize()) {
                container.setItem(j, ItemStack.of(compoundTag));
            }
        }
        return container;
    }
}