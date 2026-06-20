package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.data.ItemDataComponent;
import net.ixdarklord.coolcatlib.api.utils.CodecUtils;
import net.ixdarklord.coolcatlib.api.utils.ContainerHelper;
import net.ixdarklord.ultimine_addition.client.handler.ClientHandler;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.challenge.IneligibleBlocksSavedData;
import net.ixdarklord.ultimine_addition.common.item.PenItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packets.SkillsRecordPacket;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.LOGGER;

public final class SkillsRecordData extends ItemDataComponent<SkillsRecordData> {
    public static final ResourceLocation DATA_ID = FTBUltimineAddition.id("skills_record_data");
    public static final Codec<SkillsRecordData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(UUIDUtil.CODEC.optionalFieldOf("UUID", UUID.randomUUID()).forGetter(SkillsRecordData::getUUID), ItemStack.CODEC.listOf().xmap((itemStacks) -> new SimpleContainer(itemStacks.toArray(ItemStack[]::new)), ContainerHelper::getItems).fieldOf("Contents").forGetter(SkillsRecordData::getContainer), Codec.INT.optionalFieldOf("SelectedCard", -1).forGetter(SkillsRecordData::getSelectedCard), Codec.BOOL.optionalFieldOf("ConsumeMode", false).forGetter(SkillsRecordData::isConsumeModeActive)).apply(instance, SkillsRecordData::new));
    private final UUID uuid;
    private SimpleContainer container;
    private int selectedCard;
    private boolean consumeMode;
    private final Map<Integer, MiningSkillCardData> cachedCardData;

    private SkillsRecordData(UUID uuid, SimpleContainer container, int selectedCard, boolean consumeMode) {
        this(uuid, container, selectedCard, consumeMode, new TreeMap<>());
    }

    private SkillsRecordData(UUID uuid, SimpleContainer container, int selectedCard, boolean consumeMode, Map<Integer, MiningSkillCardData> cachedCardData) {
        super(DATA_ID, CODEC);
        this.uuid = uuid;
        this.container = container;
        this.selectedCard = selectedCard;
        this.consumeMode = consumeMode;
        this.cachedCardData = cachedCardData;
    }

    private static SkillsRecordData create() {
        return new SkillsRecordData(UUID.randomUUID(), new SimpleContainer(SkillsRecordMenu.CONTAINER_SIZE), -1, false);
    }

    public static SkillsRecordData load(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag().getCompound(DATA_ID.toString());
        SkillsRecordData data = tag.isEmpty() ? create() : CodecUtils.decode(CODEC, tag);
        return data.setStack(stack);
    }

    public static boolean hasData(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof SkillsRecordItem) {
            CompoundTag tag = stack.getOrCreateTag().getCompound(DATA_ID.toString());
            return !tag.isEmpty();
        }
        return false;
    }

    public Pair<Boolean, Boolean> initTaskValidator(BlockState state, BlockPos pos, ServerPlayer player, ChallengeData.Type challengeType) {
        boolean b1 = false;
        boolean b2 = false;

        for (int i = 0; i < this.getCardSlots().size(); ++i) {
            Optional<MiningSkillCardData> dataOpt = this.getCardData(i);
            if (dataOpt.isPresent()) {
                Pair<Boolean, Boolean> pair = this.validateTask(dataOpt.get(), state, pos, player, challengeType);
                if (pair.getFirst()) {
                    b1 = true;
                }

                if (pair.getSecond()) {
                    b2 = true;
                }
            }
        }

        return Pair.of(b1, b2);
    }

    private Pair<Boolean, Boolean> validateTask(MiningSkillCardData cardData, BlockState state, BlockPos pos, ServerPlayer player, ChallengeData.Type challengeType) {
        AtomicReference<Pair<Boolean, Boolean>> isConsumed = new AtomicReference<>(Pair.of(false, false));
        try {
            AtomicInteger i = new AtomicInteger();
            cardData.getChallenges().forEach((identifier) -> {
                if (i.get() == 0) {
                    var savedData = IneligibleBlocksSavedData.getOrCreate((ServerLevel) player.level());
                    var challengeData = ChallengesManager.INSTANCE.getAllChallenges().get(identifier.getId());
                    List<Block> blocks = ChallengesManager.INSTANCE.utilizeTargetedBlocks(challengeData);
                    int inkChamber = getPenSlot().getItem() instanceof PenItem
                            ? ((PenItem) getAllSlots().get(4).getItem()).getData(getAllSlots().get(4)).getCapacity()
                            : 0;

                    boolean hasCorrectGamemode = !player.isCreative() && !player.isSpectator();
                    boolean isMissingRequiredItems = hasCorrectGamemode && (getAllSlots().get(4).isEmpty() || getAllSlots().get(5).isEmpty());
                    boolean notEnoughInk = hasCorrectGamemode && inkChamber == 0;
                    boolean isChallengeAccomplished = cardData.isChallengeAccomplished(identifier.getId());
                    boolean isCorrectAction = challengeData.challengeType().equals(challengeType) || challengeData.challengeType().equals(challengeType.getConsumeVersion());
                    boolean isValidBlock = blocks.contains(state.getBlock());
                    boolean isCorrectTool = !hasCorrectGamemode || ChallengesManager.INSTANCE.isCorrectTool(player, challengeData);
                    boolean isBlockPlacedByEntity = ConfigHandler.SERVER.IS_PLACED_BY_ENTITY_CONDITION.get() && hasCorrectGamemode && !state.is(ModBlockTags.DENY_IS_PLACED_BY_ENTITY) && savedData.isBlockPlacedByEntity(pos);

                    if (ConfigHandler.SERVER.CHALLENGE_ACTIONS_LOGGER.get()) {
                        LOGGER.debug("/----------[Challenge Tracker]----------/");
                        LOGGER.debug("Challenge Id: {}", identifier.getId());
                        LOGGER.debug("hasCorrectGamemode: {}", hasCorrectGamemode);
                        LOGGER.debug("isMissingRequiredItems: {}", isMissingRequiredItems);
                        LOGGER.debug("notEnoughInk: {}", notEnoughInk);
                        LOGGER.debug("isChallengeAccomplished: {}", isChallengeAccomplished);
                        LOGGER.debug("isCorrectAction: {}", isCorrectAction);
                        LOGGER.debug("isValidBlock: {}", isValidBlock);
                        LOGGER.debug("isCorrectTool: {}", isCorrectTool);
                        LOGGER.debug("isBlockPlacedByEntity: {}", isBlockPlacedByEntity);
                        LOGGER.debug("/----------------------------------------/");
                    }

                    if (!isMissingRequiredItems && !notEnoughInk && !isChallengeAccomplished && isCorrectAction && isValidBlock && isCorrectTool && isBlockPlacedByEntity) {
                        savedData.getChunkEntries().forEach((chunkPos, chunkEntries) -> {
                            var list = chunkEntries.stream()
                                    .filter(blockEntry -> !blockEntry.placedBlocks().stream().filter(blockInfo -> blockInfo.pos().equals(pos)).toList().isEmpty())
                                    .toList();

                            if (!list.isEmpty()) {
                                IneligibleBlocksSavedData.BlockEntry blockEntry = list.get(0);
                                MutableComponent component = Component.literal("[").append(SkillsRecordItem.TITLE.copy().withStyle(ChatFormatting.YELLOW)).append("] ").withStyle(ChatFormatting.GRAY);
                                MutableComponent info = Component.translatable("info.ultimine_addition.placed_by_entity", Component.translatable("entity.%s.%s".formatted(blockEntry.placerData().entityId().getNamespace(), blockEntry.placerData().entityId().getPath()))).withStyle(ChatFormatting.RED);
                                player.displayClientMessage(component.append(info), true);
                            }
                        });
                    }
                    if (!isMissingRequiredItems && !notEnoughInk && !isChallengeAccomplished && isCorrectAction && isValidBlock && isCorrectTool && !isBlockPlacedByEntity) {
                        if (challengeData.challengeType().isConsuming()) {
                            if (consumeMode) {
                                cardData.addAmount(identifier.getId(), 1).save();
                                if (hasCorrectGamemode) consumeContents();
                                isConsumed.set(Pair.of(true, true));
                            }
                        } else {
                            cardData.addAmount(identifier.getId(), 1).save();
                            if (hasCorrectGamemode) consumeContents();
                            isConsumed.set(Pair.of(true, false));
                        }
                        i.getAndIncrement();
                    }
                }
            });
        } catch (ConcurrentModificationException ignored) {
        }
        return isConsumed.get();
    }

    public SkillsRecordData togglePinned(int cardSlot, ResourceLocation challengeId) {
        this.getCardData(cardSlot).ifPresent((cardData) -> cardData.togglePinned(challengeId).save());
        return this;
    }

    private void consumeContents() {
        ItemStack pen = this.getPenSlot();
        ItemStack paper = this.getPaperSlot();
        if (pen.getItem() instanceof PenItem item) {
            item.getData(pen).removeAmount(1).save();
        }

        if (paper.getItem() == Items.PAPER) {
            boolean chance = ThreadLocalRandom.current().nextDouble() < ConfigHandler.SERVER.PAPER_CONSUMPTION_RATE.get();
            paper.shrink(chance ? 1 : 0);
        }

    }

    public void invalidateCard(int slot) {
        this.cachedCardData.remove(slot);
    }

    public Optional<MiningSkillCardData> getCardData(int slot) {
        if (slot >= 0 && slot < this.getCardSlots().size()) {
            ItemStack stack = this.getCardSlots().get(slot);
            if (!MiningSkillCardData.hasData(stack)) {
                this.invalidateCard(slot);
                return Optional.empty();
            } else {
                MiningSkillCardData cached = this.cachedCardData.get(slot);
                if (cached != null && cached.getStack() != null && cached.equals(MiningSkillCardData.load(stack))) {
                    return Optional.of(cached);
                } else {
                    MiningSkillCardData newData = MiningSkillCardData.load(stack);
                    this.cachedCardData.put(slot, newData);
                    return Optional.of(newData);
                }
            }
        } else {
            return Optional.empty();
        }
    }

    public NonNullList<ItemStack> getCardSlots() {
        NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);

        for (int i = 0; i < items.size(); ++i) {
            items.set(i, this.container.getItem(i));
        }

        return items;
    }

    public NonNullList<ItemStack> getAllSlots() {
        return ContainerHelper.getItems(this.container);
    }

    public ItemStack getPenSlot() {
        return this.getAllSlots().get(4);
    }

    private ItemStack getPaperSlot() {
        return this.getAllSlots().get(5);
    }

    public boolean isConsumeModeActive() {
        return this.consumeMode;
    }

    public int getSelectedCard() {
        return this.selectedCard;
    }

    public SimpleContainer getContainer() {
        return this.container;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public SkillsRecordData insertContainer(Container container) {
        if (container.getContainerSize() != SkillsRecordMenu.CONTAINER_SIZE) {
            throw new IllegalArgumentException("You have inserted a container of size other than %s! (Inserted Container Size: %s)"
                    .formatted(SkillsRecordMenu.CONTAINER_SIZE, container.getContainerSize()));
        } else {
            NonNullList<ItemStack> stacks = NonNullList.withSize(SkillsRecordMenu.CONTAINER_SIZE, ItemStack.EMPTY);

            for (int i = 0; i < container.getContainerSize(); ++i) {
                stacks.set(i, container.getItem(i));
            }

            this.container = new SimpleContainer(stacks.toArray(ItemStack[]::new));
            return this;
        }
    }

    public SkillsRecordData setSelectedCard(int selectedSlot) {
        this.selectedCard = selectedSlot;
        return this;
    }

    public SkillsRecordData setConsumeMode(boolean trigger) {
        this.consumeMode = trigger;
        return this;
    }

    public SkillsRecordData sendToClient(ServerPlayer player, @Nullable InteractionHand hand) {
        return this.sendToClient(player, ItemUtils.getSlotIndex(hand));
    }

    public SkillsRecordData sendToClient(ServerPlayer player, int slotIndex) {
        PacketHandler.sendToPlayer(new SkillsRecordPacket.SyncData(slotIndex, this), player);

        for (int i = 0; i < this.getCardSlots().size(); ++i) {
            Optional<MiningSkillCardData> cardData = this.getCardData(i);
            cardData.ifPresent(MiningSkillCardData::onServerUpdate);
        }

        return this;
    }

    public void writeBuffer(FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(CODEC, this);
        NonNullList<ItemStack> cardSlots = this.getCardSlots();
        int nonEmptyCount = (int) cardSlots.stream().filter((stack) -> !stack.isEmpty()).count();
        buf.writeInt(nonEmptyCount);

        for (int i = 0; i < cardSlots.size(); ++i) {
            Optional<MiningSkillCardData> cardData = this.getCardData(i);
            if (cardData.isPresent()) {
                MiningSkillCardData data = cardData.get();
                buf.writeInt(i);
                data.writeFinishedChallenges(buf);
            }
        }

    }

    public static SkillsRecordData readBuffer(FriendlyByteBuf buf) {
        SkillsRecordData data = buf.readJsonWithCodec(CODEC);
        int count = buf.readInt();

        for (int j = 0; j < count; ++j) {
            int slotIndex = buf.readInt();
            data.invalidateCard(slotIndex);
            data.getCardData(slotIndex).ifPresent((cardData) -> cardData.readFinishedChallenges(buf));
        }

        return data;
    }

    public SkillsRecordData onClientUpdate() {
        for (int i = 0; i < this.getCardSlots().size(); ++i) {
            Optional<MiningSkillCardData> dataOptional = this.getCardData(i);
            dataOptional.ifPresent(MiningSkillCardData::onClientUpdate);
        }

        this.updateClientOffhand();
        return this;
    }

    @Environment(EnvType.CLIENT)
    private void updateClientOffhand() {
        AbstractContainerMenu abstractContainerMenu = ClientHandler.getPlayer().containerMenu;
        if (abstractContainerMenu instanceof SkillsRecordMenu menu) {
            if (menu.interactionHand == InteractionHand.OFF_HAND) {
                ClientHandler.getPlayer().setItemSlot(EquipmentSlot.OFFHAND, this.stack);
            }

        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof SkillsRecordData that)) {
            return false;
        } else {
            return Objects.equals(this.uuid, that.uuid) && ContainerHelper.equals(this.container, that.container) && this.selectedCard == that.selectedCard && this.consumeMode == that.consumeMode;
        }
    }

    public int hashCode() {
        return Objects.hash(this.uuid, this.selectedCard, this.consumeMode) + ContainerHelper.hashCode(this.container);
    }
}
