package net.ixdarklord.ultimine_addition.common.data.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData.Challenge;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class CardProgress {
    public static final Codec<CardProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Challenge.CODEC.listOf().optionalFieldOf("Challenges", List.of()).forGetter(CardProgress::getChallenges),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("PotionPoints", 0).forGetter(CardProgress::getPotionPoints),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("Rerolls", 0).forGetter(CardProgress::getRerolls),
            Codec.INT.listOf().optionalFieldOf("ClaimedCertificates", List.of()).forGetter(p -> List.copyOf(p.claimedCertificates))
    ).apply(instance, (challenges, points, rerolls, claimed) -> new CardProgress(challenges, points, List.of(), rerolls, claimed)));

    public static final StreamCodec<FriendlyByteBuf, CardProgress> STREAM_CODEC = StreamCodec.composite(
            Challenge.STREAM_CODEC.apply(ByteBufCodecs.list()), CardProgress::getChallenges,
            ByteBufCodecs.VAR_INT, CardProgress::getPotionPoints,
            Challenge.STREAM_CODEC.apply(ByteBufCodecs.list()), CardProgress::getFinished,
            ByteBufCodecs.VAR_INT, CardProgress::getRerolls,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), p -> List.copyOf(p.claimedCertificates),
            CardProgress::new);

    private final List<Challenge> challenges;
    private int potionPoints;
    private final List<Challenge> finished;
    // Rerolls used in the current tier.
    private int rerolls;
    // Tiers (values) whose Shape Certificate was claimed; kept across tier-ups so each is claimed once per card.
    private final Set<Integer> claimedCertificates = new TreeSet<>();

    public CardProgress() {
        this(List.of(), 0, List.of(), 0, List.of());
    }

    public CardProgress(List<Challenge> challenges, int potionPoints, List<Challenge> finished) {
        this(challenges, potionPoints, finished, 0, List.of());
    }

    public CardProgress(List<Challenge> challenges, int potionPoints, List<Challenge> finished, int rerolls, List<Integer> claimedCertificates) {
        this.challenges = new ArrayList<>(challenges);
        this.potionPoints = potionPoints;
        this.finished = new ArrayList<>(finished);
        this.rerolls = rerolls;
        this.claimedCertificates.addAll(claimedCertificates);
    }

    public boolean isCertificateClaimed(int tier) {
        return this.claimedCertificates.contains(tier);
    }

    public void claimCertificate(int tier) {
        this.claimedCertificates.add(tier);
    }

    public int getRerolls() {
        return this.rerolls;
    }

    public void setRerolls(int rerolls) {
        this.rerolls = Math.max(0, rerolls);
    }

    public List<Challenge> getChallenges() {
        return this.challenges;
    }

    public int getPotionPoints() {
        return this.potionPoints;
    }

    public void setPotionPoints(int potionPoints) {
        this.potionPoints = Math.max(0, potionPoints);
    }

    public List<Challenge> getFinished() {
        return this.finished;
    }

    public void clearFinished() {
        this.finished.clear();
    }

    public void copyFrom(CardProgress other) {
        this.challenges.clear();
        this.challenges.addAll(other.challenges);
        this.potionPoints = other.potionPoints;
        this.finished.clear();
        this.rerolls = other.rerolls;
        this.claimedCertificates.clear();
        this.claimedCertificates.addAll(other.claimedCertificates);
    }

    public CardProgress copy() {
        return new CardProgress(this.challenges.stream().map(Challenge::copy).toList(), this.potionPoints,
                this.finished.stream().map(Challenge::copy).toList(), this.rerolls, List.copyOf(this.claimedCertificates));
    }

    public boolean isEmpty() {
        return this.challenges.isEmpty() && this.potionPoints == 0 && this.claimedCertificates.isEmpty();
    }
}
