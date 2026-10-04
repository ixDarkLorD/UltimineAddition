package net.ixdarklord.ultimine_addition.common.data.item;

import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.common.progression.ChallengeBoosts;
import net.ixdarklord.ultimine_addition.common.progression.ProgressionRewards;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.challenge.IneligibleBlocksSavedData;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordClientCache;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordLink;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordSavedData;
import net.ixdarklord.ultimine_addition.common.item.PenItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.LOGGER;

public final class SkillsRecordData {
    public static final int CARD_SLOTS = 4;
    public static final int PEN_SLOT = 4;
    public static final int PAPER_SLOT = 5;

    public static final Codec<SkillsRecordData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("UUID").forGetter(SkillsRecordData::getUUID),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("Contents").forGetter(data -> data.container.getItems()),
            Codec.INT.optionalFieldOf("SelectedCard", -1).forGetter(SkillsRecordData::getSelectedCard),
            Codec.BOOL.optionalFieldOf("ConsumeMode", false).forGetter(SkillsRecordData::isConsumeModeActive)
    ).apply(instance, SkillsRecordData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillsRecordData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SkillsRecordData::getUUID,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, data -> data.container.getItems(),
            ByteBufCodecs.VAR_INT, data -> data.selectedCard + 1,
            ByteBufCodecs.BOOL, SkillsRecordData::isConsumeModeActive,
            (uuid, items, selected, consume) -> new SkillsRecordData(uuid, items, selected - 1, consume));

    private final UUID uuid;
    private final SimpleContainer container;
    private int selectedCard;
    private boolean consumeMode;
    private @Nullable SkillsRecordSavedData owner;
    private int version;

    private SkillsRecordData(UUID uuid, List<ItemStack> contents, int selectedCard, boolean consumeMode) {
        this.uuid = uuid;
        this.container = new RecordContainer();
        for (int i = 0; i < Math.min(contents.size(), this.container.getContainerSize()); i++) {
            this.container.setItem(i, contents.get(i));
        }
        this.selectedCard = selectedCard;
        this.consumeMode = consumeMode;
    }

    private final class RecordContainer extends SimpleContainer {
        private RecordContainer() {
            super(SkillsRecordMenu.CONTAINER_SIZE);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            SkillsRecordData.this.save();
        }
    }

    public static SkillsRecordData create(UUID uuid) {
        return new SkillsRecordData(uuid, List.of(), -1, false);
    }

    public static SkillsRecordData fromLegacy(UUID uuid, SkillsRecordLink.Legacy legacy) {
        return new SkillsRecordData(uuid, legacy.contents(), legacy.selectedCard(), legacy.consumeMode());
    }

    public static SkillsRecordData get(ItemStack stack, Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return SkillsRecordSavedData.get(serverLevel.getServer()).resolve(stack);
        }
        return getClient(stack).orElseGet(() -> create(Objects.requireNonNullElse(SkillsRecordLink.getId(stack), new UUID(0L, 0L))));
    }

    public static Optional<SkillsRecordData> getClient(ItemStack stack) {
        return SkillsRecordClientCache.get(stack);
    }

    @ApiStatus.Internal
    public void attach(SkillsRecordSavedData owner) {
        this.owner = owner;
    }

    public int getVersion() {
        return this.version;
    }

    @ApiStatus.Internal
    public void bumpVersion() {
        this.version++;
    }

    public void save() {
        if (this.owner != null) this.owner.onRecordChanged(this);
    }

    public SkillsRecordData snapshot() {
        return new SkillsRecordData(this.uuid, this.container.getItems().stream().map(ItemStack::copy).toList(), this.selectedCard, this.consumeMode);
    }

    @ApiStatus.Internal
    public void copyFrom(SkillsRecordData other) {
        for (int i = 0; i < this.container.getContainerSize(); i++) {
            ItemStack incoming = other.container.getItem(i);
            if (!ItemStack.matches(this.container.getItem(i), incoming)) {
                this.container.setItem(i, incoming);
            }
        }
        this.selectedCard = other.selectedCard;
        this.consumeMode = other.consumeMode;
    }

    public Pair<Boolean, Boolean> initTaskValidator(BlockState state, BlockPos pos, ServerPlayer player, ChallengeData.Type challengeType) {
        boolean progressed = false;
        boolean consumed = false;

        for (int i = 0; i < CARD_SLOTS; ++i) {
            Optional<MiningSkillCardData> dataOpt = this.getCardData(i);
            if (dataOpt.isPresent()) {
                Pair<Boolean, Boolean> pair = this.validateTask(dataOpt.get(), state, pos, player, challengeType);
                progressed |= pair.getFirst();
                consumed |= pair.getSecond();
            }
        }

        return Pair.of(progressed, consumed);
    }

    private Pair<Boolean, Boolean> validateTask(MiningSkillCardData cardData, BlockState state, BlockPos pos, ServerPlayer player, ChallengeData.Type challengeType) {
        IneligibleBlocksSavedData savedData = IneligibleBlocksSavedData.getOrCreate((ServerLevel) player.level());
        boolean hasCorrectGamemode = !player.isCreative() && !player.isSpectator();
        boolean isMissingRequiredItems = hasCorrectGamemode && (this.getPenSlot().isEmpty() || this.getPaperSlot().isEmpty());
        boolean notEnoughInk = hasCorrectGamemode && this.getInkAmount() == 0;

        // Copy: finishing the tier rerolls the list.
        for (MiningSkillCardData.Challenge challenge : List.copyOf(cardData.getChallenges())) {
            Identifier challengeId = challenge.getId();
            ChallengeData challengeData = ChallengesManager.INSTANCE.getAllChallenges().get(challengeId);
            if (challengeData == null) continue;

            List<Block> blocks = ChallengesManager.INSTANCE.utilizeTargetedBlocks(challengeData);
            boolean isChallengeAccomplished = cardData.isChallengeAccomplished(challengeId);
            boolean isCorrectAction = challengeData.challengeType().equals(challengeType) || challengeData.challengeType().equals(challengeType.getConsumeVersion());
            boolean isValidBlock = blocks.contains(state.getBlock());
            boolean isCorrectTool = !hasCorrectGamemode || ChallengesManager.INSTANCE.isCorrectTool(player, challengeData);
            boolean isBlockPlacedByEntity = UAServerConfig.IS_PLACED_BY_ENTITY_CONDITION.get() && hasCorrectGamemode && !state.is(ModBlockTags.DENY_IS_PLACED_BY_ENTITY) && savedData.isBlockPlacedByEntity(pos);

            if (UAServerConfig.CHALLENGE_ACTIONS_LOGGER.get()) {
                LOGGER.debug("/----------[Challenge Tracker]----------/");
                LOGGER.debug("Challenge Id: {}", challengeId);
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

            boolean eligible = !isMissingRequiredItems && !notEnoughInk && !isChallengeAccomplished && isCorrectAction && isValidBlock && isCorrectTool;
            if (!eligible) continue;

            if (isBlockPlacedByEntity) {
                this.notifyPlacedByEntity(savedData, pos, player);
                continue;
            }

            boolean consuming = challengeData.challengeType().isConsuming();
            if (consuming && !this.consumeMode) continue;

            MiningSkillCardItem.Tier before = cardData.getTier();
            int points = 1;
            if (hasCorrectGamemode) points += ChallengeBoosts.bonusPoints(player, challengeId, pos, state.getBlock().asItem().getDefaultInstance());
            cardData.addAmount(challengeId, points).save();
            // Done when the card still lists it as accomplished, or moved on to its next tier with it.
            if (cardData.getTier() != before || cardData.isChallengeAccomplished(challengeId)) {
                net.ixdarklord.ultimine_addition.common.data.reward.DataRewards.onChallengeCompleted(player, cardData, before, challengeId);
            }
            ProgressionRewards.checkTierUp(player, cardData, before);
            if (hasCorrectGamemode) this.consumeContents();
            return Pair.of(true, consuming);
        }
        return Pair.of(false, false);
    }

    private void notifyPlacedByEntity(IneligibleBlocksSavedData savedData, BlockPos pos, ServerPlayer player) {
        savedData.getChunkEntries().forEach((chunkPos, chunkEntries) -> chunkEntries.stream()
                .filter(blockEntry -> blockEntry.placedBlocks().stream().anyMatch(blockInfo -> blockInfo.pos().equals(pos)))
                .findFirst()
                .ifPresent(blockEntry -> {
                    MutableComponent component = Component.literal("[").append(SkillsRecordItem.TITLE.copy().withStyle(ChatFormatting.YELLOW)).append("] ").withStyle(ChatFormatting.GRAY);
                    Identifier entityId = blockEntry.placerData().entityId();
                    MutableComponent info = Component.translatable("info.ultimine_addition.placed_by_entity", Component.translatable("entity.%s.%s".formatted(entityId.getNamespace(), entityId.getPath()))).withStyle(ChatFormatting.RED);
                    player.sendOverlayMessage(component.append(info));
                }));
    }

    public SkillsRecordData togglePinned(int cardSlot, Identifier challengeId) {
        this.getCardData(cardSlot).ifPresent((cardData) -> cardData.togglePinned(challengeId).save());
        return this;
    }

    public void consumeInk(int amount) {
        ItemStack pen = this.getPenSlot();
        if (amount > 0 && pen.getItem() instanceof PenItem item) {
            item.getData(pen).removeAmount(amount).save();
        }
    }

    private void consumeContents() {
        ItemStack pen = this.getPenSlot();
        ItemStack paper = this.getPaperSlot();

        if (pen.getItem() instanceof PenItem item) {
            item.getData(pen).removeAmount(1).save();
        }
        if (paper.is(Items.PAPER) && ThreadLocalRandom.current().nextDouble() < UAServerConfig.PAPER_CONSUMPTION_RATE.get()) {
            paper.shrink(1);
        }
    }

    public Optional<MiningSkillCardData> getCardData(int slot) {
        if (slot < 0 || slot >= CARD_SLOTS) return Optional.empty();
        ItemStack stack = this.container.getItem(slot);
        return MiningSkillCardData.hasData(stack) ? Optional.of(MiningSkillCardData.load(stack)) : Optional.empty();
    }

    public NonNullList<ItemStack> getCardSlots() {
        NonNullList<ItemStack> items = NonNullList.withSize(CARD_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < CARD_SLOTS; i++) {
            items.set(i, this.container.getItem(i));
        }
        return items;
    }

    public boolean hasCards() {
        for (int i = 0; i < CARD_SLOTS; i++) {
            if (!this.container.getItem(i).isEmpty()) return true;
        }
        return false;
    }

    public NonNullList<ItemStack> getAllSlots() {
        return this.container.getItems();
    }

    public ItemStack getPenSlot() {
        return this.container.getItem(PEN_SLOT);
    }

    public ItemStack getPaperSlot() {
        return this.container.getItem(PAPER_SLOT);
    }

    public int getInkAmount() {
        ItemStack pen = this.getPenSlot();
        return pen.getItem() instanceof PenItem item ? item.getData(pen).getCapacity() : 0;
    }

    public SimpleContainer getContainer() {
        return this.container;
    }

    public boolean isConsumeModeActive() {
        return this.consumeMode;
    }

    public int getSelectedCard() {
        return this.selectedCard;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public SkillsRecordData setSelectedCard(int selectedSlot) {
        this.selectedCard = selectedSlot;
        return this;
    }

    public SkillsRecordData setConsumeMode(boolean trigger) {
        this.consumeMode = trigger;
        return this;
    }
}
