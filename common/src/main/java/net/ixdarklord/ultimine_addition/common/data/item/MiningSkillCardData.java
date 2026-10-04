package net.ixdarklord.ultimine_addition.common.data.item;

import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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

    public static final StreamCodec<FriendlyByteBuf, MiningSkillCardData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID, MiningSkillCardData::getUUID,
            MiningSkillCardItem.Tier.STREAM_CODEC, MiningSkillCardData::getTier,
            ByteBufCodecs.optional(CardProgress.STREAM_CODEC), data -> Optional.ofNullable(data.legacy),
            (uuid, tier, legacy) -> new MiningSkillCardData(uuid, tier, legacy.orElse(null))
    );

    public static final ItemComponentType<MiningSkillCardData> DATA_COMPONENT = new ItemComponentType<>(FTBUltimineAddition.id("mining_skill_card_data"), CODEC);

    // 1.20.1 has no data components: every load decodes the stack's NBT into a new object, so the temporary progress
    // of a card that isn't stored yet (see progress) is kept here by card UUID, one map per side, instead of in the
    // stack's component value. Entries go once the card is stored (takeLocal) or after a while unused.
    private static final Cache<UUID, CardProgress> SERVER_LOCAL = CacheBuilder.newBuilder().maximumSize(1024).expireAfterAccess(10, TimeUnit.MINUTES).build();
    private static final Cache<UUID, CardProgress> CLIENT_LOCAL = CacheBuilder.newBuilder().maximumSize(1024).expireAfterAccess(10, TimeUnit.MINUTES).build();

    @NotNull
    private final UUID uuid;
    private MiningSkillCardItem.Tier tier;
    private @Nullable CardProgress legacy;
    private @Nullable CardProgress local;

    private MiningSkillCardData(@NotNull UUID uuid, MiningSkillCardItem.Tier tier, @Nullable CardProgress legacy) {
        super(DATA_COMPONENT.id(), CODEC);
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
        DATA_COMPONENT.set(stack, data);
        return stack;
    }

    // A new object read from the stack's NBT (1.20.1 keeps item data in NBT); changes land when saved (save /
    // writeComponent).
    public static MiningSkillCardData load(ItemStack stack) {
        MiningSkillCardItem.Type type = stack.getItem() instanceof MiningSkillCardItem item ? item.getType(stack) : MiningSkillCardItem.Type.EMPTY;
        MiningSkillCardData stored = DATA_COMPONENT.get(stack);
        return (stored != null ? stored : create(type)).setStack(stack);
    }

    private static Cache<UUID, CardProgress> localProgress() {
        return CardStore.isServerThread() ? SERVER_LOCAL : CLIENT_LOCAL;
    }

    public static boolean hasData(@NotNull ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof MiningSkillCardItem && DATA_COMPONENT.has(stack);
    }

    private CardProgress progress() {
        if (!this.isCreativeItem()) {
            CardProgress stored = CardStore.progress(this);
            if (stored != null) return stored;
        }
        if (this.local == null) {
            // A card that isn't stored yet keeps its temporary progress between loads (display-only creative cards,
            // which all share one UUID, don't).
            boolean shared = !this.isCreativeItem();
            this.local = shared ? localProgress().getIfPresent(this.uuid) : null;
            if (this.local == null) {
                this.local = this.legacy != null ? this.legacy : new CardProgress();
                if (shared) localProgress().put(this.uuid, this.local);
            }
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
        CardProgress local = this.local != null ? this.local : localProgress().getIfPresent(this.uuid);
        this.local = null;
        localProgress().invalidate(this.uuid);
        return local == this.legacy ? null : local;
    }

    public void writeComponent() {
        super.save();
    }

    @ApiStatus.Internal
    public void clearLegacy() {
        this.legacy = null;
        if (this.stack != null) DATA_COMPONENT.set(this.stack, this);
    }

    public MiningSkillCardItem.Type getType() {
        return this.stack != null && this.stack.getItem() instanceof MiningSkillCardItem item ? item.getType(this.stack) : MiningSkillCardItem.Type.EMPTY;
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

    public boolean rerollChallenge(ResourceLocation challengeId) {
        Optional<Challenge> found = this.getChallenge(challengeId);
        if (found.isEmpty() || !this.canReroll(found.get())) return false;

        MiningSkillCardItem.Type type = this.getType();
        CardProgress progress = this.progress();
        List<Challenge> challenges = progress.getChallenges();
        List<ResourceLocation> candidates = ChallengesManager.INSTANCE.getAllChallenges().entrySet().stream()
                .filter(entry -> entry.getValue().forCardType().equals(type) && entry.getValue().forCardTier().isEligible(this.tier))
                .map(Map.Entry::getKey)
                .filter(id -> challenges.stream().noneMatch(c -> c.id.equals(id)))
                .toList();
        if (candidates.isEmpty()) return false;

        ResourceLocation next = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        Challenge old = found.get();
        challenges.set(challenges.indexOf(old), new Challenge(next, old.order, ChallengesManager.INSTANCE.getAllChallenges().get(next).getRequiredAmount()));
        progress.setRerolls(progress.getRerolls() + 1);
        return true;
    }

    public MiningSkillCardData addAmount(ResourceLocation challengeId, int value) {
        Optional<Challenge> challengeData = this.getChallenge(challengeId);
        if (challengeData.isEmpty()) return this;
        Challenge challenge = challengeData.get();
        if (challenge.currentPoints >= challenge.requiredPoints) return this;
        return this.setAmount(challengeId, challenge.currentPoints + value);
    }

    public void accomplishChallenge(ResourceLocation challengeId) {
        this.getChallenge(challengeId).ifPresent(challenge -> this.setAmount(challengeId, challenge.requiredPoints));
    }

    public MiningSkillCardData setAmount(ResourceLocation challengeId, int value) {
        Optional<Challenge> challengeData = this.getChallenge(challengeId);
        if (challengeData.isEmpty()) return this;

        Challenge challenge = challengeData.get();
        challenge.currentPoints = Math.min(value, challenge.requiredPoints);
        this.checkChallengeAccomplishment(challenge);
        return this;
    }

    public MiningSkillCardData togglePinned(ResourceLocation challengeId) {
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

    public Optional<Challenge> getChallenge(ResourceLocation challengeId) {
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

    public boolean isChallengeAccomplished(ResourceLocation challengeId) {
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
                ResourceLocation.CODEC.fieldOf("Id").forGetter(Challenge::getId),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("Order", 0).forGetter(Challenge::getOrder),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("CurrentPoints", 0).forGetter(Challenge::getCurrentPoints),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("RequiredPoints").forGetter(Challenge::getRequiredPoints),
                Codec.BOOL.optionalFieldOf("IsPinned", false).forGetter(Challenge::isPinned)
        ).apply(instance, Challenge::new));

        public static final StreamCodec<FriendlyByteBuf, Challenge> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.RESOURCE_LOCATION, Challenge::getId,
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
            this(new ResourceLocation("completed"), 0, 1);
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
