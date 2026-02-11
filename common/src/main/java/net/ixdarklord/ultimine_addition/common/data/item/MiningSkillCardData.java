package net.ixdarklord.ultimine_addition.common.data.item;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;
import net.ixdarklord.coolcatlib.api.data.ItemDataComponent;
import net.ixdarklord.ultimine_addition.client.gui.toasts.ChallengesToast;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.MiningSkillCardPayload;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.LOGGER;

public final class MiningSkillCardData extends ItemDataComponent<MiningSkillCardData> {
    public static final Codec<MiningSkillCardData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("UUID", UUID.randomUUID()).forGetter(MiningSkillCardData::getUUID),
            MiningSkillCardItem.Tier.CODEC.fieldOf("Tier").forGetter(MiningSkillCardData::getTier),
            ItemStack.SIMPLE_ITEM_CODEC.fieldOf("DisplayItem").forGetter(MiningSkillCardData::getDisplayItem),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("PotionPoints", 0).forGetter(MiningSkillCardData::getPotionPoints),
            Challenge.CODEC.listOf().optionalFieldOf("Challenges", Lists.newArrayList()).forGetter(MiningSkillCardData::getChallenges)
    ).apply(instance, MiningSkillCardData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MiningSkillCardData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MiningSkillCardData::getUUID,
            MiningSkillCardItem.Tier.STREAM_CODEC, MiningSkillCardData::getTier,
            ItemStack.STREAM_CODEC, MiningSkillCardData::getDisplayItem,
            ByteBufCodecs.INT, MiningSkillCardData::getPotionPoints,
            Challenge.STREAM_CODEC.apply(ByteBufCodecs.list()), MiningSkillCardData::getChallenges,
            Challenge.STREAM_CODEC.apply(ByteBufCodecs.list()), data -> data.finishedChallenges,
            MiningSkillCardData::new
    );

    public static final DataComponentType<MiningSkillCardData> DATA_COMPONENT =
            DataComponentType.<MiningSkillCardData>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).build();

    @NotNull
    private final UUID uuid;
    private MiningSkillCardItem.Tier tier;
    private ItemStack displayItem;
    private int potionPoints;
    private final List<Challenge> challenges;
    private final List<Challenge> finishedChallenges;

    private MiningSkillCardData(@NotNull UUID uuid, MiningSkillCardItem.Tier tier, ItemStack displayItem, int potionPoints, List<Challenge> challenges) {
        this(uuid, tier, displayItem, potionPoints, challenges, Lists.newArrayList());
    }

    private MiningSkillCardData(@NotNull UUID uuid, MiningSkillCardItem.Tier tier, ItemStack displayItem, int potionPoints, List<Challenge> challenges, List<Challenge> finishedChallenges) {
        super(DATA_COMPONENT);
        this.uuid = uuid;
        this.tier = tier;
        this.displayItem = displayItem;
        this.potionPoints = potionPoints;
        this.challenges = Lists.newArrayList(challenges);
        this.finishedChallenges = Lists.newArrayList(finishedChallenges);
    }

    public static MiningSkillCardData create(MiningSkillCardItem.Type type) {
        return new MiningSkillCardData(UUID.randomUUID(), MiningSkillCardItem.Tier.Unlearned, type.getDefaultDisplayItem().getDefaultInstance(), 0, Lists.newArrayList());
    }

    @ApiStatus.Internal
    public static ItemStack createForCreativeTab(MiningSkillCardItem cardItem, MiningSkillCardItem.Tier tier) {
        ItemStack stack = cardItem.getDefaultInstance();
        MiningSkillCardData data = new MiningSkillCardData(UUID.fromString("00000000-0000-0000-0000-000000000000"), tier, cardItem.getType().getDefaultDisplayItem().getDefaultInstance(), 0, Lists.newArrayList());
        data.stack = stack;
        data.save();
        return stack;
    }

    public static MiningSkillCardData load(ItemStack stack) {
        MiningSkillCardItem.Type type = stack.getItem() instanceof MiningSkillCardItem ? ((MiningSkillCardItem) stack.getItem()).getType() : MiningSkillCardItem.Type.EMPTY;
        return stack.getOrDefault(DATA_COMPONENT, create(type)).setStack(stack);
    }

    public static boolean hasData(@NotNull ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof MiningSkillCardItem && stack.has(DATA_COMPONENT);
    }

    public MiningSkillCardData onClientUpdate() {
        for(Challenge finishedChallenge : this.finishedChallenges) {
            ChallengesToast.run(finishedChallenge, this.stack);
        }

        this.finishedChallenges.clear();
        return this;
    }

    public MiningSkillCardData onServerUpdate() {
        this.finishedChallenges.clear();
        return this;
    }

    public MiningSkillCardData initChallenges() {
        if (!(this.stack.getItem() instanceof MiningSkillCardItem)) {
            LOGGER.error("You've tried to initiate challenges on item can't accept it: {}", this.stack.getDescriptionId());
            return this;
        }

        if (tier == MiningSkillCardItem.Tier.Mastered) {
            challenges.clear();
            return this;
        }

        AtomicInteger slotId = new AtomicInteger(1);
        AtomicInteger quantity = new AtomicInteger();
        MiningSkillCardItem.Type type = ((MiningSkillCardItem) stack.getItem()).getType();
        quantity.set(ConfigHandler.SERVER.CARD_CHALLENGES_AMOUNT.getValue(tier));

        if (tier != MiningSkillCardItem.Tier.Unlearned && tier != MiningSkillCardItem.Tier.Mastered)
            this.resetPotionPoints();

        challenges.clear();
        ChallengesManager.INSTANCE.getRandomChallenges(quantity.get(), type, this.tier).forEach((location, data) -> {
            challenges.add(new Challenge(location, slotId.get(), data.getRequiredAmount()));
            slotId.getAndIncrement();
        });
        return this;
    }

    public boolean validateChallenges() {
        if (this.tier == MiningSkillCardItem.Tier.Mastered) return false;

        MiningSkillCardItem.Type type = ((MiningSkillCardItem) stack.getItem()).getType();
        if (!this.challenges.isEmpty()) {
            Collection<Challenge> removedChallenges = new TreeSet<>();
            this.challenges.forEach((challengeData) -> {
                if (!ChallengesManager.INSTANCE.getAllChallenges().containsKey(challengeData.id)) {
                    removedChallenges.add(challengeData);
                }
            });
            removedChallenges.forEach(challengeData -> {
                AtomicBoolean isDone = new AtomicBoolean(true);
                this.challenges.remove(challengeData);
                do {
                    ChallengesManager.INSTANCE.getRandomChallenges(1, type, this.tier).forEach((location, data) -> {
                        if (this.challenges.stream().filter(challengeData1 -> challengeData1.id.equals(location)).toList().isEmpty()) {
                            this.challenges.add(new Challenge(location, challengeData.order, data.getRequiredAmount()));
                            if (ConfigHandler.SERVER.CHALLENGE_MANAGER_LOGGER.get() || Platform.isDevelopmentEnvironment()) {
                                LOGGER.debug("Changing the invalid challenge! id:\"{}\" to: id:\"{}\"", challengeData.id, location);
                            }
                            isDone.set(false);
                        }
                    });
                } while (isDone.get());
            });
            return !removedChallenges.isEmpty();

        } else {
            initChallenges();
            return true;
        }
    }

    public void setDisplayItem(ItemStack stack) {
        this.displayItem = stack;
    }

    public MiningSkillCardData addAmount(ResourceLocation challengeId, int value) {
        Optional<Challenge> challengeData = this.getChallenge(challengeId);
        if (challengeData.isEmpty()) return this;

        int currentAmount = challengeData.get().currentPoints;
        int requiredAmount = challengeData.get().requiredPoints;
        if (currentAmount >= requiredAmount) return this;

        return setAmount(challengeId, currentAmount + value);
    }

    public void accomplishChallenge(ResourceLocation challengeId) {
        Optional<Challenge> challengeData = this.getChallenge(challengeId);
        if (challengeData.isEmpty()) return;
        setAmount(challengeId, challengeData.get().requiredPoints);
    }

    public MiningSkillCardData setAmount(ResourceLocation challengeId, int value) {
        Optional<Challenge> challengeData = this.getChallenge(challengeId);
        if (challengeData.isEmpty()) return this;

        Challenge challenge = challengeData.get();
        int requiredAmount = challenge.requiredPoints;
        challenge.currentPoints = Math.min(value, requiredAmount);
        this.checkChallengeAccomplishment(challenge);
        return this;
    }

    public MiningSkillCardData togglePinned(ResourceLocation challengeId) {
        this.getChallenge(challengeId).ifPresent((challenge) -> challenge.isPinned ^= true);
        return this;
    }

    public void setPotionPoints(int value) {
        this.potionPoints = value;
    }

    public MiningSkillCardData consumePotionPoint(int value) {
        this.potionPoints = Math.max(0, this.potionPoints - value);
        return this;
    }

    public void resetPotionPoints() {
        this.setPotionPoints(this.getMaxPotionPoints());
    }

    public MiningSkillCardData setTier(MiningSkillCardItem.Tier tier) {
        this.tier = tier;
        return this;
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public Optional<Challenge> getChallenge(ResourceLocation challengeId) {
        for (Challenge challenge : this.challenges) {
            if (challenge.id.equals(challengeId))
                return Optional.of(challenge);
        }
        return Optional.empty();
    }

    private void checkChallengeAccomplishment(Challenge challenge) {
        if (this.isAllChallengesCompleted()) {
            this.tier = this.tier.next();
            this.initChallenges();
            this.finishedChallenges.add(new Challenge());
        } else {
            if (this.isChallengeAccomplished(challenge) && !this.finishedChallenges.contains(challenge)) {
                this.finishedChallenges.add(challenge);
            }

        }
    }

    public boolean isChallengeAccomplished(Challenge challenge) {
        return this.isChallengeAccomplished(challenge.id);
    }

    public boolean isChallengeAccomplished(ResourceLocation challengeId) {
        Optional<Challenge> challengeData = this.getChallenge(challengeId);
        return challengeData.filter((data) -> data.currentPoints >= data.requiredPoints).isPresent();
    }

    public boolean isAllChallengesCompleted() {
        return this.getChallenges().stream().allMatch(this::isChallengeAccomplished);
    }

    public @NotNull UUID getUUID() {
        return this.uuid;
    }

    public List<Challenge> getChallenges() {
        return this.challenges;
    }

    public MiningSkillCardItem.Tier getTier() {
        return this.tier;
    }

    public boolean isPotionPointsFull() {
        return this.potionPoints >= this.getMaxPotionPoints();
    }

    public int getPotionPoints() {
        return this.potionPoints;
    }

    public int getMaxPotionPoints() {
        return ConfigHandler.SERVER.CARD_POTION_POINTS.getMapValue().get(tier);
    }

    public boolean isCreativeItem() {
        return this.uuid.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }

    public MiningSkillCardData sendToClient(ServerPlayer player, int slotIndex) {
        PayloadHandler.sendToPlayer(new MiningSkillCardPayload(slotIndex, this), player);
        return this.onServerUpdate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MiningSkillCardData that)) return false;
        return uuid.equals(that.uuid)
                && tier == that.tier
                && ItemStack.isSameItemSameComponents(this.displayItem, that.displayItem)
                && potionPoints == that.potionPoints
                && challenges.equals(that.challenges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, tier, potionPoints, challenges) + ItemStack.hashItemAndComponents(displayItem);
    }

    @Override
    public String toString() {
        return "MiningSkillCardData{" +
                "uuid=" + uuid +
                ", tier=" + tier +
                ", displayItem=" + displayItem +
                ", potionPoints=" + potionPoints +
                ", challenges=" + challenges +
                '}';
    }

    public static class Challenge implements Comparable<Challenge> {
        public static final Codec<Challenge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("Id").forGetter(Challenge::getId),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("Order", 0).forGetter(Challenge::getOrder),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("CurrentPoints", 0).forGetter(Challenge::getCurrentPoints),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("RequiredPoints").forGetter(Challenge::getRequiredPoints),
                Codec.BOOL.optionalFieldOf("IsPinned", false).forGetter(Challenge::isPinned)
        ).apply(instance, Challenge::new));

        public static final StreamCodec<FriendlyByteBuf, Challenge> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Challenge::getId,
                ByteBufCodecs.INT, Challenge::getOrder,
                ByteBufCodecs.INT, Challenge::getCurrentPoints,
                ByteBufCodecs.INT, Challenge::getRequiredPoints,
                ByteBufCodecs.BOOL, Challenge::isPinned,
                Challenge::new
        );

        private final ResourceLocation id;
        private final int order;
        private int currentPoints;
        private final int requiredPoints;
        private boolean isPinned;

        private Challenge() {
            this(ResourceLocation.parse("completed"), 0, 1);
        }

        private Challenge(ResourceLocation id, int order, int requiredPoints) {
            this(id, order, 0, requiredPoints);
        }

        private Challenge(ResourceLocation id, int order, int currentPoints, int requiredPoints) {
            this(id, order, currentPoints, requiredPoints, false);
        }

        private Challenge(ResourceLocation id, int order, int currentPoints, int requiredPoints, boolean isPinned) {
            this.id = id;
            this.order = order;
            this.currentPoints = currentPoints;
            this.requiredPoints = requiredPoints;
            this.isPinned = isPinned;
        }

        public ResourceLocation getId() {
            return id;
        }

        public int getOrder() {
            return order;
        }

        public boolean isPinned() {
            return isPinned;
        }

        public int getCurrentPoints() {
            return currentPoints;
        }

        public int getRequiredPoints() {
            return requiredPoints;
        }

        @Override
        public int compareTo(@NotNull MiningSkillCardData.Challenge other) {
            return Integer.compare(this.order, other.order);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Challenge that)) return false;
            return id.equals(that.id)
                    && order == that.order
                    && currentPoints == that.currentPoints
                    && requiredPoints == that.requiredPoints
                    && isPinned == that.isPinned;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, order, currentPoints, requiredPoints, isPinned);
        }

        @Override
        public String toString() {
            return "ChallengeHolder{" +
                    "id=" + id +
                    ", order=" + order +
                    ", currentPoints=" + currentPoints +
                    ", requiredPoints=" + requiredPoints +
                    ", isPinned=" + isPinned +
                    '}';
        }
    }
}
