package net.ixdarklord.ultimine_addition.common.data.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class CardHistory {
    public record ChallengeRecord(ResourceLocation id, int order, int requiredPoints, long completedAt) {
        public static final Codec<ChallengeRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("Id").forGetter(ChallengeRecord::id),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("Order", 0).forGetter(ChallengeRecord::order),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("Required", 0).forGetter(ChallengeRecord::requiredPoints),
                Codec.LONG.optionalFieldOf("CompletedAt", 0L).forGetter(ChallengeRecord::completedAt)
        ).apply(instance, ChallengeRecord::new));

        public static final StreamCodec<FriendlyByteBuf, ChallengeRecord> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.RESOURCE_LOCATION, ChallengeRecord::id,
                ByteBufCodecs.VAR_INT, ChallengeRecord::order,
                ByteBufCodecs.VAR_INT, ChallengeRecord::requiredPoints,
                ByteBufCodecs.VAR_LONG, ChallengeRecord::completedAt,
                ChallengeRecord::new);
    }

    public record TierRecord(MiningSkillCardItem.Tier tier, long completedAt, boolean skipped, List<ChallengeRecord> challenges) {
        public static final Codec<TierRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MiningSkillCardItem.Tier.CODEC.fieldOf("Tier").forGetter(TierRecord::tier),
                Codec.LONG.optionalFieldOf("CompletedAt", 0L).forGetter(TierRecord::completedAt),
                Codec.BOOL.optionalFieldOf("Skipped", false).forGetter(TierRecord::skipped),
                ChallengeRecord.CODEC.listOf().optionalFieldOf("Challenges", List.of()).forGetter(TierRecord::challenges)
        ).apply(instance, TierRecord::new));

        public static final StreamCodec<FriendlyByteBuf, TierRecord> STREAM_CODEC = StreamCodec.composite(
                MiningSkillCardItem.Tier.STREAM_CODEC, TierRecord::tier,
                ByteBufCodecs.VAR_LONG, TierRecord::completedAt,
                ByteBufCodecs.BOOL, TierRecord::skipped,
                ChallengeRecord.STREAM_CODEC.apply(ByteBufCodecs.list()), TierRecord::challenges,
                TierRecord::new);
    }

    private record Observed(ResourceLocation id, int order, int requiredPoints) {
        static final Codec<Observed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("Id").forGetter(Observed::id),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("Order", 0).forGetter(Observed::order),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("Required", 0).forGetter(Observed::requiredPoints)
        ).apply(instance, Observed::new));
    }

    public static final MapCodec<CardHistory> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MiningSkillCardItem.Tier.CODEC.optionalFieldOf("ObservedTier").forGetter(h -> Optional.ofNullable(h.observedTier)),
            Observed.CODEC.listOf().optionalFieldOf("Observed", List.of()).forGetter(h -> h.observed),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.LONG).optionalFieldOf("CompletionTimes", Map.of()).forGetter(h -> h.completionTimes),
            TierRecord.CODEC.listOf().optionalFieldOf("Tiers", List.of()).forGetter(h -> List.copyOf(h.completedTiers.values()))
    ).apply(instance, (tier, observed, times, tiers) -> new CardHistory(tier.orElse(null), observed, times, tiers)));

    public static final Codec<CardHistory> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<FriendlyByteBuf, CardHistory> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.RESOURCE_LOCATION, ByteBufCodecs.VAR_LONG), h -> h.completionTimes,
            TierRecord.STREAM_CODEC.apply(ByteBufCodecs.list()), h -> List.copyOf(h.completedTiers.values()),
            (times, tiers) -> new CardHistory(null, List.of(), times, tiers));

    private @Nullable MiningSkillCardItem.Tier observedTier;
    private final List<Observed> observed;
    private final Map<ResourceLocation, Long> completionTimes;
    private final EnumMap<MiningSkillCardItem.Tier, TierRecord> completedTiers = new EnumMap<>(MiningSkillCardItem.Tier.class);

    public CardHistory() {
        this(null, List.of(), Map.of(), List.of());
    }

    private CardHistory(@Nullable MiningSkillCardItem.Tier observedTier, List<Observed> observed, Map<ResourceLocation, Long> completionTimes, List<TierRecord> tiers) {
        this.observedTier = observedTier;
        this.observed = new ArrayList<>(observed);
        this.completionTimes = new HashMap<>(completionTimes);
        tiers.forEach(record -> this.completedTiers.put(record.tier(), record));
    }

    public CardHistory copy() {
        return new CardHistory(this.observedTier, this.observed, this.completionTimes, List.copyOf(this.completedTiers.values()));
    }

    public Optional<TierRecord> getTier(MiningSkillCardItem.Tier tier) {
        return Optional.ofNullable(this.completedTiers.get(tier));
    }

    public Collection<TierRecord> getCompletedTiers() {
        return Collections.unmodifiableCollection(this.completedTiers.values());
    }

    public OptionalLong getCompletionTime(ResourceLocation challengeId) {
        Long time = this.completionTimes.get(challengeId);
        return time == null || time <= 0 ? OptionalLong.empty() : OptionalLong.of(time);
    }

    public boolean observe(MiningSkillCardData card, long now) {
        MiningSkillCardItem.Tier tier = card.getTier();
        boolean changed = false;

        if (this.observedTier == null) {
            for (MiningSkillCardData.Challenge challenge : card.getChallenges()) {
                if (card.isChallengeAccomplished(challenge)) this.completionTimes.put(challenge.getId(), 0L);
            }
            changed = true;
        } else if (tier.ordinal() > this.observedTier.ordinal()) {
            List<ChallengeRecord> done = this.observed.stream()
                    .map(o -> new ChallengeRecord(o.id(), o.order(), o.requiredPoints(), this.completionTimes.getOrDefault(o.id(), now)))
                    .sorted(Comparator.comparingInt(ChallengeRecord::order))
                    .toList();
            this.completedTiers.put(this.observedTier, new TierRecord(this.observedTier, now, false, done));
            for (int i = this.observedTier.ordinal() + 1; i < tier.ordinal(); i++) {
                MiningSkillCardItem.Tier skipped = MiningSkillCardItem.Tier.values()[i];
                this.completedTiers.put(skipped, new TierRecord(skipped, now, true, List.of()));
            }
            this.completionTimes.clear();
            changed = true;
        } else if (tier.ordinal() < this.observedTier.ordinal()) {
            this.completedTiers.keySet().removeIf(t -> t.ordinal() >= tier.ordinal());
            this.completionTimes.clear();
            changed = true;
        }

        Set<ResourceLocation> current = new HashSet<>();
        for (MiningSkillCardData.Challenge challenge : card.getChallenges()) {
            current.add(challenge.getId());
            if (card.isChallengeAccomplished(challenge)) {
                if (!this.completionTimes.containsKey(challenge.getId())) {
                    this.completionTimes.put(challenge.getId(), now);
                    changed = true;
                }
            } else if (this.completionTimes.remove(challenge.getId()) != null) {
                changed = true;
            }
        }
        if (this.completionTimes.keySet().retainAll(current)) changed = true;

        List<Observed> snapshot = card.getChallenges().stream()
                .map(c -> new Observed(c.getId(), c.getOrder(), c.getRequiredPoints()))
                .toList();
        if (!snapshot.equals(this.observed)) {
            this.observed.clear();
            this.observed.addAll(snapshot);
            changed = true;
        }
        this.observedTier = tier;
        return changed;
    }
}
