package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.data.ItemDataComponent;
import net.ixdarklord.ultimine_addition.client.handler.ClientHandler;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.challenge.IneligibleBlocksSavedData;
import net.ixdarklord.ultimine_addition.common.item.PenItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.SkillsRecordPayload;
import net.ixdarklord.ultimine_addition.util.ContainerUtils;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.LOGGER;

public final class SkillsRecordData extends ItemDataComponent<SkillsRecordData> {
    public static final Codec<SkillsRecordData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("UUID", UUID.randomUUID()).forGetter(SkillsRecordData::getUUID),
            ItemStack.OPTIONAL_CODEC.listOf().xmap(itemStacks -> new SimpleContainer(itemStacks.toArray(ItemStack[]::new)), SimpleContainer::getItems).fieldOf("Contents").forGetter(SkillsRecordData::getContainer),
            Codec.INT.optionalFieldOf("SelectedCard", -1).forGetter(SkillsRecordData::getSelectedCard),
            Codec.BOOL.optionalFieldOf("ConsumeMode", false).forGetter(SkillsRecordData::isConsumeMode)
    ).apply(instance, SkillsRecordData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Map<Integer, MiningSkillCardData>> CARD_DATA_STREAM_CODEC =
            ByteBufCodecs.map((i) -> new TreeMap<>(), ByteBufCodecs.INT, MiningSkillCardData.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillsRecordData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull SkillsRecordData decode(RegistryFriendlyByteBuf buf) {
            return new SkillsRecordData(
                    buf.readUUID(),
                    new SimpleContainer(ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(buf).toArray(ItemStack[]::new)),
                    buf.readInt(),
                    buf.readBoolean(),
                    SkillsRecordData.CARD_DATA_STREAM_CODEC.decode(buf));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SkillsRecordData data) {
            buf.writeUUID(data.uuid);
            ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(buf, data.getContainer().getItems());
            buf.writeInt(data.selectedCard);
            buf.writeBoolean(data.consumeMode);
            SkillsRecordData.CARD_DATA_STREAM_CODEC.encode(buf, data.cachedCardData);
        }
    };

    public static final DataComponentType<SkillsRecordData> DATA_COMPONENT =
            DataComponentType.<SkillsRecordData>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).build();

    private final UUID uuid;
    private SimpleContainer container;
    private int selectedCard;
    private boolean consumeMode;
    private final Map<Integer, MiningSkillCardData> cachedCardData;

    private SkillsRecordData(UUID uuid, SimpleContainer container, int selectedCard, boolean consumeMode) {
        this(uuid, container, selectedCard, consumeMode, new TreeMap<>());
    }

    private SkillsRecordData(UUID uuid, SimpleContainer container, int selectedCard, boolean consumeMode, Map<Integer, MiningSkillCardData> cachedCardData) {
        super(DATA_COMPONENT);
        this.uuid = uuid;
        this.container = container;
        this.selectedCard = selectedCard;
        this.consumeMode = consumeMode;
        this.cachedCardData = cachedCardData;
    }

    public static SkillsRecordData create() {
        return new SkillsRecordData(UUID.randomUUID(), new SimpleContainer(6), -1, false);
    }

    public static SkillsRecordData load(ItemStack stack) {
        return stack.getOrDefault(DATA_COMPONENT, create()).setStack(stack);
    }

    public static boolean hasData(@NotNull ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof SkillsRecordItem && stack.has(DATA_COMPONENT);
    }

    public Pair<Boolean, Boolean> initTaskValidator(BlockState state, BlockPos pos, ServerPlayer player, ChallengeData.Type challengeType) {
        boolean b1 = false;
        boolean b2 = false;

        for(int i = 0; i < this.getCardSlots().size(); ++i) {
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
                                IneligibleBlocksSavedData.BlockEntry blockEntry = list.getFirst();
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
        ItemStack pen = getPenSlot();
        ItemStack paper = getPaperSlot();

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
            if (stack.isEmpty()) {
                this.invalidateCard(slot);
                return Optional.empty();
            } else {
                MiningSkillCardData cached = this.cachedCardData.get(slot);
                if (cached != null && cached.getUUID().equals(MiningSkillCardData.load(stack).getUUID())) {
                    return Optional.of(cached.setStack(stack));
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

        for (int i = 0; i < items.size(); i++) {
            items.set(i, container.getItem(i));
        }

        return items;
    }

    public NonNullList<ItemStack> getAllSlots() {
        return this.container.getItems();
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

    public int getSelectedCard() {
        return selectedCard;
    }

    public SimpleContainer getContainer() {
        return container;
    }

    @Nullable
    public UUID getUUID() {
        return this.uuid;
    }

    public SkillsRecordData insertContainer(Container container) {
        if (container.getContainerSize() != SkillsRecordMenu.CONTAINER_SIZE)
            throw new IllegalArgumentException("You have inserted a container of size other than %s! (Inserted Container Size: %s)"
                    .formatted(SkillsRecordMenu.CONTAINER_SIZE, container.getContainerSize()));

        NonNullList<ItemStack> stacks = NonNullList.withSize(SkillsRecordMenu.CONTAINER_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < container.getContainerSize(); i++) {
            stacks.set(i, container.getItem(i));
        }
        this.container = new SimpleContainer(stacks.toArray(ItemStack[]::new));
        return this;
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
        PayloadHandler.sendToPlayer(new SkillsRecordPayload.SyncData(slotIndex, this), player);

        for(int i = 0; i < this.getCardSlots().size(); ++i) {
            Optional<MiningSkillCardData> cardData = this.getCardData(i);
            cardData.ifPresent(MiningSkillCardData::onServerUpdate);
        }

        return this;
    }

    public SkillsRecordData onClientUpdate() {
        for(int i = 0; i < this.getCardSlots().size(); ++i) {
            Optional<MiningSkillCardData> dataOpt = this.getCardData(i);
            dataOpt.ifPresent(MiningSkillCardData::onClientUpdate);
        }

        this.updateClientOffhand();
        return this;
    }

    @Environment(EnvType.CLIENT)
    private void updateClientOffhand() {
        if (ClientHandler.getPlayer().containerMenu instanceof SkillsRecordMenu menu) {
            if (menu.interactionHand == InteractionHand.OFF_HAND) {
                ClientHandler.getPlayer().setItemSlot(EquipmentSlot.OFFHAND, this.stack);
            }

        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SkillsRecordData that)) return false;
        return Objects.equals(uuid, that.uuid)
                && ContainerUtils.equals(container, that.container)
                && selectedCard == that.selectedCard
                && consumeMode == that.consumeMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, selectedCard, consumeMode) + ContainerUtils.hashCode(container);
    }
}