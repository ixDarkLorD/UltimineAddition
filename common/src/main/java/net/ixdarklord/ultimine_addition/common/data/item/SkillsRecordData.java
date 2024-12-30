package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.challenge.IneligibleBlocksSavedData;
import net.ixdarklord.ultimine_addition.common.item.PenItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.SkillsRecordPacket;
import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
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

public class SkillsRecordData extends DataHandler<SkillsRecordData, ItemStack> {
    public static final Codec<SkillsRecordData> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillsRecordData> STREAM_CODEC;
    public static final DataComponentType<SkillsRecordData> DATA_COMPONENT;

    private final UUID uuid;
    private SimpleContainer container;
    private int selectedCard;
    private boolean consumeMode;
    private final Map<Integer, List<ResourceLocation>> pinnedChallenges = new TreeMap<>();

    private SkillsRecordData() {
        this(UUID.randomUUID(), new SimpleContainer(SkillsRecordItem.CONTAINER_SIZE), -1, false);
    }

    private SkillsRecordData(UUID uuid, SimpleContainer container, int selectedCard, boolean consumeMode) {
        this.uuid = uuid;
        this.container = container;
        this.selectedCard = selectedCard;
        this.consumeMode = consumeMode;
    }

    public static SkillsRecordData create() {
        return new SkillsRecordData();
    }

    public static SkillsRecordData loadData(ItemStack stack) {
        return stack.getOrDefault(DATA_COMPONENT, create()).setDataHolder(stack);
    }

    @Override
    public void saveData(ItemStack stack) {
        stack.set(DATA_COMPONENT, this);
        super.saveData(stack);
    }

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.optionalFieldOf("UUID", UUID.randomUUID()).forGetter(SkillsRecordData::getUUID),
                ItemStack.OPTIONAL_CODEC.listOf().xmap(itemStacks -> new SimpleContainer(itemStacks.toArray(ItemStack[]::new)), SimpleContainer::getItems).fieldOf("Contents").forGetter(SkillsRecordData::getContainer),
                Codec.INT.optionalFieldOf("SelectedCard", -1).forGetter(SkillsRecordData::getSelectedCard),
                Codec.BOOL.optionalFieldOf("ConsumeMode", false).forGetter(SkillsRecordData::isConsumeMode)
        ).apply(instance, SkillsRecordData::new));

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull SkillsRecordData decode(RegistryFriendlyByteBuf buf) {
                return new SkillsRecordData(
                        buf.readUUID(),
                        new SimpleContainer(ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(buf).toArray(ItemStack[]::new)),
                        buf.readInt(),
                        buf.readBoolean())
                        .decodePinnedChallenges(buf);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, SkillsRecordData data) {
                buf.writeUUID(data.uuid);
                ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(buf, data.getContainer().getItems());
                buf.writeInt(data.selectedCard);
                buf.writeBoolean(data.consumeMode);
                data.encodePinnedChallenges(buf);
            }
        };
        DATA_COMPONENT = DataComponentType.<SkillsRecordData>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).build();
    }

    public Pair<Boolean, Boolean> initTaskValidator(BlockState state, BlockPos pos, ServerPlayer player, ChallengesData.Type challengeType) {
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

    private Pair<Boolean, Boolean> validateTask(ItemStack stack, BlockState state, BlockPos pos, ServerPlayer player, ChallengesData.Type challengeType) {
        AtomicReference<Pair<Boolean, Boolean>> isConsumed = new AtomicReference<>(Pair.of(false, false));
        try {
            AtomicInteger i = new AtomicInteger();
            MiningSkillCardData cardData = MiningSkillCardData.loadData(stack);
            cardData.getChallenges().forEach((identifier) -> {
                if (i.get() == 0) {
                    var savedData = IneligibleBlocksSavedData.getOrCreate((ServerLevel) player.level());
                    var challengeData = ChallengesManager.INSTANCE.getAllChallenges().get(identifier.getId());
                    List<Block> blocks = ChallengesManager.INSTANCE.utilizeTargetedBlocks(challengeData);
                    int inkChamber = getPenSlot().getItem() instanceof PenItem
                            ? ((PenItem)getAllSlots().get(4).getItem()).getData(getAllSlots().get(4)).getCapacity()
                            : 0;

                    boolean hasCorrectGamemode = !player.isCreative() && !player.isSpectator();
                    boolean isMissingRequiredItems = hasCorrectGamemode && (getAllSlots().get(4).isEmpty() || getAllSlots().get(5).isEmpty());
                    boolean notEnoughInk = hasCorrectGamemode && inkChamber == 0;
                    boolean isChallengeAccomplished = cardData.isChallengeAccomplished(identifier.getId());
                    boolean isCorrectAction = challengeData.getChallengeType().equals(challengeType) || challengeData.getChallengeType().equals(challengeType.getConsumeVersion());
                    boolean isValidBlock = blocks.contains(state.getBlock());
                    boolean isCorrectTool = !hasCorrectGamemode || ChallengesManager.INSTANCE.isCorrectTool(player, challengeData);
                    boolean isBlockPlacedByEntity = ConfigHandler.COMMON.IS_PLACED_BY_ENTITY_CONDITION.get() && hasCorrectGamemode && !state.is(ModBlockTags.DENY_IS_PLACED_BY_ENTITY) && savedData.isBlockPlacedByEntity(pos);

                    if (ConfigHandler.COMMON.CHALLENGE_ACTIONS_LOGGER.get()) {
                        ChallengesManager.LOGGER.debug("/===========================================/");
                        ChallengesManager.LOGGER.debug("Challenge: {}", identifier.getId());
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

                    if (!isMissingRequiredItems && !notEnoughInk && !isChallengeAccomplished && isCorrectAction && isValidBlock && isCorrectTool && isBlockPlacedByEntity) {
                        savedData.getChunkEntries().forEach((chunkPos, chunkEntries) -> {
                            var list = chunkEntries.stream()
                                    .filter(blockEntry -> !blockEntry.placedBlocks().stream().filter(blockInfo -> blockInfo.pos.equals(pos)).toList().isEmpty())
                                    .toList();

                            if (!list.isEmpty()) {
                                IneligibleBlocksSavedData.BlockEntry blockEntry = list.getFirst();
                                MutableComponent component = Component.literal("[").append(SkillsRecordItem.TITLE.copy().withStyle(ChatFormatting.YELLOW)).append("] ").withStyle(ChatFormatting.GRAY);
                                MutableComponent info = Component.translatable("info.ultimine_addition.placed_by_entity", Component.translatable("entity.%s.%s".formatted(blockEntry.entityId().getNamespace(), blockEntry.entityId().getPath()))).withStyle(ChatFormatting.RED);
                                player.displayClientMessage(component.append(info), true);
                            }
                        });
                    }
                    if (!isMissingRequiredItems && !notEnoughInk && !isChallengeAccomplished && isCorrectAction && isValidBlock && isCorrectTool && !isBlockPlacedByEntity) {
                        if (challengeData.getChallengeType().isConsuming()) {
                            if (consumeMode) {
                                cardData.addAmount(identifier.getId(), 1).sendToClient(player).saveData(stack);
                                if (hasCorrectGamemode) consumeContents();
                                isConsumed.set(Pair.of(true, true));
                                player.level().removeBlock(pos, false);
                            }
                        } else {
                            cardData.addAmount(identifier.getId(), 1).sendToClient(player).saveData(stack);
                            if (hasCorrectGamemode) consumeContents();
                            isConsumed.set(Pair.of(true, false));
                        }
                        i.getAndIncrement();
                    }
                }
            });
        } catch (ConcurrentModificationException ignored) {}
        return isConsumed.get();
    }

    @Environment(EnvType.CLIENT)
    public SkillsRecordData togglePinned(int slot, ResourceLocation challengeId) {
        if (this.pinnedChallenges.containsKey(slot)) {
            if (!this.pinnedChallenges.get(slot).contains(challengeId)) {
                this.pinnedChallenges.get(slot).add(challengeId);
            }
        } else this.pinnedChallenges.put(slot, new ArrayList<>(List.of(challengeId)));

        ItemStack itemStack = getCardSlots().get(slot);
        MiningSkillCardData data = MiningSkillCardData.loadData(itemStack);
        Optional<MiningSkillCardData.ChallengeHolder> challengeData = data.getChallenge(challengeId);
        if (challengeData.isPresent()) {
            challengeData.get().togglePinned();
            data.saveData(itemStack);
        }
        return sendToServer();
    }

    private void consumeContents() {
        ItemStack pen = getPenSlot();
        ItemStack paper = getPaperSlot();

        if (pen.getItem() instanceof PenItem item) {
            item.getData(pen).removeAmount(1).saveData(pen);
        }
        if (paper.getItem() == Items.PAPER) {
            boolean chance = ThreadLocalRandom.current().nextDouble() < ConfigHandler.COMMON.PAPER_CONSUMPTION_RATE.get();
            paper.shrink(chance ? 1 : 0);
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
        if (container.getContainerSize() != SkillsRecordItem.CONTAINER_SIZE)
            throw new IllegalArgumentException("You have inserted a container of size other than %s! (Inserted Container Size: %s)"
                    .formatted(SkillsRecordItem.CONTAINER_SIZE, container.getContainerSize()));

        NonNullList<ItemStack> stacks = NonNullList.withSize(SkillsRecordItem.CONTAINER_SIZE, ItemStack.EMPTY);
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

    public SkillsRecordData toggleConsumeMode() {
        this.consumeMode ^= true;
        return this;
    }

    public SkillsRecordData sendToClient(ServerPlayer player) {
        PacketHandler.sendToPlayer(new SkillsRecordPacket.SyncData(Env.CLIENT, this), player);
        return this;
    }

    public SkillsRecordData sendToServer() {
        PacketHandler.sendToServer(new SkillsRecordPacket.SyncData(Env.SERVER, this));
        return this;
    }

    public SkillsRecordData syncData(ServerPlayer player) {
        if (player.containerMenu instanceof SkillsRecordMenu skillsRecordMenu) {
            this.pinnedChallenges.forEach((slot, challengeList) -> {
                ItemStack cardStack = skillsRecordMenu.getCardSlots().get(slot).getItem();
                MiningSkillCardData cardData = MiningSkillCardData.loadData(cardStack);
                challengeList.forEach(location -> {
                    Optional<MiningSkillCardData.ChallengeHolder> challengeData = cardData.getChallenge(location);
                    if (challengeData.isPresent()) {
                        challengeData.get().togglePinned();
                        cardData.saveData(cardStack);
                    }
                });
            });
        }
        return this;
    }

    private void encodePinnedChallenges(RegistryFriendlyByteBuf buf) {
        buf.writeMap(this.pinnedChallenges,
                FriendlyByteBuf::writeInt,
                (buffer, locationList) -> buf.writeCollection(locationList, FriendlyByteBuf::writeResourceLocation));
    }

    private SkillsRecordData decodePinnedChallenges(RegistryFriendlyByteBuf buf) {
        this.pinnedChallenges.putAll(buf.readMap(FriendlyByteBuf::readInt, buffer -> buf.readList(FriendlyByteBuf::readResourceLocation)));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkillsRecordData data)) return false;
        return selectedCard == data.selectedCard && consumeMode == data.consumeMode && Objects.equals(uuid, data.uuid) && Objects.equals(container, data.container) && Objects.equals(pinnedChallenges, data.pinnedChallenges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, container, selectedCard, consumeMode, pinnedChallenges);
    }
}