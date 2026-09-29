package net.ixdarklord.ultimine_addition.common.data.item;

import net.ixdarklord.coolcatcore.api.platform.Platform;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.coolcatcore.api.data.ItemDataComponent;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.record.CardProgress;
import net.ixdarklord.ultimine_addition.common.data.record.CardStore;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.LOGGER;

public final class MiningSkillCardData extends ItemDataComponent<MiningSkillCardData> {
    public static final UUID CREATIVE_UUID = Util.NIL_UUID;

    public static final Codec<MiningSkillCardData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            // A randomUUID() default would be shared by every card without a UUID.
            UUIDUtil.CODEC.optionalFieldOf("UUID").xmap(id -> id.orElseGet(UUID::randomUUID), Optional::of).forGetter(MiningSkillCardData::getUUID),
            MiningSkillCardItem.Tier.CODEC.fieldOf("Tier").forGetter(MiningSkillCardData::getTier),
            // (Older cards also carry a "DisplayItem" from the removed card renderer; it's ignored.)
            // Pre-SavedData fields, kept until migrated.
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("PotionPoints", 0).forGetter(data -> data.legacy == null ? 0 : data.legacy.getPotionPoints()),
            Challenge.CODEC.listOf().optionalFieldOf("Challenges", List.of()).forGetter(data -> data.legacy == null ? List.of() : data.legacy.getChallenges())
    ).apply(instance, (uuid, tier, points, challenges) -> new MiningSkillCardData(uuid, tier,
            challenges.isEmpty() && points == 0 ? null : new CardProgress(challenges, points, List.of()))));

    public static final StreamCodec<RegistryFriendlyByteBuf, MiningSkillCardData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MiningSkillCardData::getUUID,
            MiningSkillCardItem.Tier.STREAM_CODEC, MiningSkillCardData::getTier,
            ByteBufCodecs.optional(CardProgress.STREAM_CODEC), data -> Optional.ofNullable(data.legacy),
            (uuid, tier, legacy) -> new MiningSkillCardData(uuid, tier, legacy.orElse(null))
    );

    public static final DataComponentType<MiningSkillCardData> DATA_COMPONENT =
            DataComponentType.<MiningSkillCardData>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).build();

    @NotNull
    private final UUID uuid;
    private MiningSkillCardItem.Tier tier;
    private @Nullable CardProgress legacy;
    private @Nullable CardProgress local;
    // The stack's value this was loaded from, which keeps the temporary progress (see copy).
    private @Nullable MiningSkillCardData source;

    private MiningSkillCardData(@NotNull UUID uuid, MiningSkillCardItem.Tier tier, @Nullable CardProgress legacy) {
        super(DATA_COMPONENT);
        this.uuid = uuid;
        this.tier = tier;
        this.legacy = legacy;
    }

    public static MiningSkillCardData create(MiningSkillCardItem.Type type) {
        return new MiningSkillCardData(UUID.randomUUID(), MiningSkillCardItem.Tier.Unlearned, null);
    }

    @ApiStatus.Internal
    public static ItemStack createForCreativeTab(MiningSkillCardItem cardItem, MiningSkillCardItem.Tier tier) {
        ItemStack stack = cardItem.getDefaultInstance();
        MiningSkillCardData data = new MiningSkillCardData(CREATIVE_UUID, tier, null);
        data.stack = stack;
        stack.set(DATA_COMPONENT, data);
        return stack;
    }

    // A copy of the stack's value: a component value must never change in place. Vanilla tells a changed slot from the
    // copy it last sent, and that copy shares the value, so a tier changed in place would never reach the client.
    // Changes land when saved (save / writeComponent), as a new value.
    public static MiningSkillCardData load(ItemStack stack) {
        MiningSkillCardItem.Type type = stack.getItem() instanceof MiningSkillCardItem ? ((MiningSkillCardItem) stack.getItem()).getType() : MiningSkillCardItem.Type.EMPTY;
        MiningSkillCardData stored = stack.get(DATA_COMPONENT);
        return (stored != null ? stored.copy() : create(type)).setStack(stack);
    }

    private MiningSkillCardData copy() {
        MiningSkillCardData copy = new MiningSkillCardData(this.uuid, this.tier, this.legacy);
        // A card that isn't stored yet keeps its temporary progress between loads.
        copy.local = this.local;
        copy.source = this;
        return copy;
    }

    public static boolean hasData(@NotNull ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof MiningSkillCardItem && stack.has(DATA_COMPONENT);
    }

    private CardProgress progress() {
        if (!this.isCreativeItem()) {
            CardProgress stored = CardStore.progress(this);
            if (stored != null) return stored;
        }
        if (this.local == null) {
            this.local = this.legacy != null ? this.legacy : new CardProgress();
            if (this.source != null && this.source.local == null) this.source.local = this.local;
        }
        return this.local;
    }

    public boolean hasProgress() {
        return this.isCreativeItem() || CardStore.progress(this) != null;
    }

    public @Nullable CardProgress getLegacy() {
        return this.legacy;
    }

    @ApiStatus.Internal
    public @Nullable CardProgress takeLocal() {
        CardProgress local = this.local;
        this.local = null;
        return local == this.legacy ? null : local;
    }

    public void writeComponent() {
        super.save();
    }

    @ApiStatus.Internal
    public void clearLegacy() {
        this.legacy = null;
        if (this.stack != null) this.stack.set(DATA_COMPONENT, this);
    }

    public MiningSkillCardItem.Type getType() {
        return this.stack != null && this.stack.getItem() instanceof MiningSkillCardItem item ? item.getType() : MiningSkillCardItem.Type.EMPTY;
    }

    @Override
    public void save() {
        super.save();
        CardStore.changed(this);
    }

    public MiningSkillCardData initChallenges() {
        return this.rollChallenges(this.progress());
    }

    @ApiStatus.Internal
    public MiningSkillCardData rollChallenges(CardProgress progress) {
        MiningSkillCardItem.Type type = this.getType();
        if (type == MiningSkillCardItem.Type.EMPTY) {
            LOGGER.error("You've tried to initiate challenges on item can't accept it: {}", this.stack == null ? "<none>" : this.stack.getItem().getDescriptionId());
            return this;
        }

        List<Challenge> challenges = progress.getChallenges();
        challenges.clear();
        progress.setRerolls(0);
        if (this.tier == MiningSkillCardItem.Tier.Mastered) return this;

        if (this.tier != MiningSkillCardItem.Tier.Unlearned) progress.setPotionPoints(this.getMaxPotionPoints());

        int quantity = UAServerConfig.CARD_CHALLENGES_AMOUNT.getValue(this.tier);
        int[] order = {1};
        ChallengesManager.INSTANCE.getRandomChallenges(quantity, type, this.tier).forEach((location, data) ->
                challenges.add(new Challenge(location, order[0]++, data.getRequiredAmount())));
        return this;
    }

    public boolean validateChallenges() {
        if (this.tier == MiningSkillCardItem.Tier.Mastered) return false;
        List<Challenge> challenges = this.progress().getChallenges();
        if (challenges.isEmpty()) {
            this.initChallenges();
            return true;
        }

        MiningSkillCardItem.Type type = this.getType();
        List<Challenge> removed = challenges.stream()
                .filter(challenge -> !ChallengesManager.INSTANCE.getAllChallenges().containsKey(challenge.id))
                .toList();
        for (Challenge invalid : removed) {
            challenges.remove(invalid);
            boolean replaced = false;
            while (!replaced) {
                for (var entry : ChallengesManager.INSTANCE.getRandomChallenges(1, type, this.tier).entrySet()) {
                    if (challenges.stream().noneMatch(c -> c.id.equals(entry.getKey()))) {
                        challenges.add(new Challenge(entry.getKey(), invalid.order, entry.getValue().getRequiredAmount()));
                        if (UAServerConfig.CHALLENGE_MANAGER_LOGGER.get() || Platform.isDevelopmentEnvironment()) {
                            LOGGER.debug("Changing the invalid challenge! id:\"{}\" to: id:\"{}\"", invalid.id, entry.getKey());
                        }
                        replaced = true;
                    }
                }
            }
        }
        return !removed.isEmpty();
    }

    // The Shape Certificate for a tier this card has reached, not yet claimed from the Skills Record.
    public boolean canClaimCertificate(MiningSkillCardItem.Tier tier) {
        return FTBUltimineIntegration.isShapeCertificatesActive() && this.getType() != MiningSkillCardItem.Type.EMPTY && !this.isCreativeItem()
                && ShapeCertificateItem.forTier(tier) != null && this.tier.getValue() >= tier.getValue()
                && !this.progress().isCertificateClaimed(tier.getValue());
    }

    public void claimCertificate(MiningSkillCardItem.Tier tier) {
        this.progress().claimCertificate(tier.getValue());
    }

    public int getRerollsLeft() {
        return Math.max(0, UAServerConfig.REROLLS_PER_TIER.get() - this.progress().getRerolls());
    }

    // Only challenges without progress can be swapped, so a reroll never throws work away.
    public boolean canReroll(Challenge challenge) {
        return this.tier != MiningSkillCardItem.Tier.Mastered && !this.isCreativeItem()
                && challenge.currentPoints == 0 && this.getRerollsLeft() > 0;
    }

    public boolean rerollChallenge(Identifier challengeId) {
        Optional<Challenge> found = this.getChallenge(challengeId);
        if (found.isEmpty() || !this.canReroll(found.get())) return false;

        MiningSkillCardItem.Type type = this.getType();
        CardProgress progress = this.progress();
        List<Challenge> challenges = progress.getChallenges();
        List<Identifier> candidates = ChallengesManager.INSTANCE.getAllChallenges().entrySet().stream()
                .filter(entry -> entry.getValue().forCardType().equals(type) && entry.getValue().forCardTier().isEligible(this.tier))
                .map(Map.Entry::getKey)
                .filter(id -> challenges.stream().noneMatch(c -> c.id.equals(id)))
                .toList();
        if (candidates.isEmpty()) return false;

        Identifier next = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        Challenge old = found.get();
        challenges.set(challenges.indexOf(old), new Challenge(next, old.order, ChallengesManager.INSTANCE.getAllChallenges().get(next).getRequiredAmount()));
        progress.setRerolls(progress.getRerolls() + 1);
        return true;
    }

    public MiningSkillCardData addAmount(Identifier challengeId, int value) {
        Optional<Challenge> challengeData = this.getChallenge(challengeId);
        if (challengeData.isEmpty()) return this;
        Challenge challenge = challengeData.get();
        if (challenge.currentPoints >= challenge.requiredPoints) return this;
        return this.setAmount(challengeId, challenge.currentPoints + value);
    }

    public void accomplishChallenge(Identifier challengeId) {
        this.getChallenge(challengeId).ifPresent(challenge -> this.setAmount(challengeId, challenge.requiredPoints));
    }

    public MiningSkillCardData setAmount(Identifier challengeId, int value) {
        Optional<Challenge> challengeData = this.getChallenge(challengeId);
        if (challengeData.isEmpty()) return this;

        Challenge challenge = challengeData.get();
        challenge.currentPoints = Math.min(value, challenge.requiredPoints);
        this.checkChallengeAccomplishment(challenge);
        return this;
    }

    public MiningSkillCardData togglePinned(Identifier challengeId) {
        this.getChallenge(challengeId).ifPresent((challenge) -> challenge.isPinned ^= true);
        return this;
    }

    public void setPotionPoints(int value) {
        this.progress().setPotionPoints(value);
    }

    public MiningSkillCardData consumePotionPoint(int value) {
        CardProgress progress = this.progress();
        progress.setPotionPoints(progress.getPotionPoints() - value);
        return this;
    }

    public void resetPotionPoints() {
        this.setPotionPoints(this.getMaxPotionPoints());
    }

    public MiningSkillCardData setTier(MiningSkillCardItem.Tier tier) {
        this.tier = tier;
        return this;
    }

    public Optional<Challenge> getChallenge(Identifier challengeId) {
        for (Challenge challenge : this.getChallenges()) {
            if (challenge.id.equals(challengeId))
                return Optional.of(challenge);
        }
        return Optional.empty();
    }

    private void checkChallengeAccomplishment(Challenge challenge) {
        List<Challenge> finished = this.progress().getFinished();
        if (this.isAllChallengesCompleted()) {
            this.tier = this.tier.next();
            this.initChallenges();
            finished.add(new Challenge());
        } else if (this.isChallengeAccomplished(challenge) && !finished.contains(challenge)) {
            finished.add(challenge);
        }
    }

    public boolean isChallengeAccomplished(Challenge challenge) {
        return this.isChallengeAccomplished(challenge.id);
    }

    public boolean isChallengeAccomplished(Identifier challengeId) {
        return this.getChallenge(challengeId).filter((data) -> data.currentPoints >= data.requiredPoints).isPresent();
    }

    public boolean isAllChallengesCompleted() {
        return this.getChallenges().stream().allMatch(this::isChallengeAccomplished);
    }

    public @NotNull UUID getUUID() {
        return this.uuid;
    }

    public List<Challenge> getChallenges() {
        return this.progress().getChallenges();
    }

    public MiningSkillCardItem.Tier getTier() {
        return this.tier;
    }

    public boolean isPotionPointsFull() {
        return this.getPotionPoints() >= this.getMaxPotionPoints();
    }

    public int getPotionPoints() {
        return this.progress().getPotionPoints();
    }

    public int getMaxPotionPoints() {
        return UAServerConfig.CARD_POTION_POINTS.getValue(tier);
    }

    public boolean isCreativeItem() {
        return this.uuid.equals(CREATIVE_UUID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MiningSkillCardData that)) return false;
        return uuid.equals(that.uuid)
                && tier == that.tier
                && Objects.equals(this.legacy == null ? null : this.legacy.getChallenges(), that.legacy == null ? null : that.legacy.getChallenges());
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, tier);
    }

    @Override
    public String toString() {
        return "MiningSkillCardData{uuid=" + uuid + ", tier=" + tier + ", legacy=" + (legacy != null) + '}';
    }

    public static class Challenge implements Comparable<Challenge> {
        public static final Codec<Challenge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("Id").forGetter(Challenge::getId),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("Order", 0).forGetter(Challenge::getOrder),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("CurrentPoints", 0).forGetter(Challenge::getCurrentPoints),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("RequiredPoints").forGetter(Challenge::getRequiredPoints),
                Codec.BOOL.optionalFieldOf("IsPinned", false).forGetter(Challenge::isPinned)
        ).apply(instance, Challenge::new));

        public static final StreamCodec<FriendlyByteBuf, Challenge> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, Challenge::getId,
                ByteBufCodecs.INT, Challenge::getOrder,
                ByteBufCodecs.INT, Challenge::getCurrentPoints,
                ByteBufCodecs.INT, Challenge::getRequiredPoints,
                ByteBufCodecs.BOOL, Challenge::isPinned,
                Challenge::new
        );

        private final Identifier id;
        private final int order;
        private int currentPoints;
        private final int requiredPoints;
        private boolean isPinned;

        private Challenge() {
            this(Identifier.parse("completed"), 0, 1);
        }

        private Challenge(Identifier id, int order, int requiredPoints) {
            this(id, order, 0, requiredPoints);
        }

        private Challenge(Identifier id, int order, int currentPoints, int requiredPoints) {
            this(id, order, currentPoints, requiredPoints, false);
        }

        private Challenge(Identifier id, int order, int currentPoints, int requiredPoints, boolean isPinned) {
            this.id = id;
            this.order = order;
            this.currentPoints = currentPoints;
            this.requiredPoints = requiredPoints;
            this.isPinned = isPinned;
        }

        public Identifier getId() {
            return id;
        }

        public Challenge copy() {
            return new Challenge(this.id, this.order, this.currentPoints, this.requiredPoints, this.isPinned);
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
