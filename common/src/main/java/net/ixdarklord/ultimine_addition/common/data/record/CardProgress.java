package net.ixdarklord.ultimine_addition.common.data.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData.Challenge;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
            Codec.INT.listOf().optionalFieldOf("ClaimedCertificates", List.of()).forGetter(p -> List.copyOf(p.claimedCertificates)),
            Codec.INT.listOf().optionalFieldOf("ClaimedPerks", List.of()).forGetter(p -> List.copyOf(p.claimedPerks)),
            Codec.STRING.optionalFieldOf("Owner", "").forGetter(CardProgress::getOwner),
            Codec.STRING.optionalFieldOf("OwnerName", "").forGetter(CardProgress::getOwnerName),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("Prestige", 0).forGetter(CardProgress::getPrestige)
    ).apply(instance, (challenges, points, rerolls, claimed, perks, owner, ownerName, prestige) -> {
        CardProgress progress = new CardProgress(challenges, points, List.of(), rerolls, claimed, perks);
        progress.setOwner(owner, ownerName);
        progress.prestige = prestige;
        return progress;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, CardProgress> STREAM_CODEC = StreamCodec.composite(
            Challenge.STREAM_CODEC.apply(ByteBufCodecs.list()), CardProgress::getChallenges,
            ByteBufCodecs.VAR_INT, CardProgress::getPotionPoints,
            Challenge.STREAM_CODEC.apply(ByteBufCodecs.list()), CardProgress::getFinished,
            ByteBufCodecs.VAR_INT, CardProgress::getRerolls,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), p -> List.copyOf(p.claimedCertificates),
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), p -> List.copyOf(p.claimedPerks),
            ByteBufCodecs.STRING_UTF8, CardProgress::getOwner,
            ByteBufCodecs.STRING_UTF8, CardProgress::getOwnerName,
            ByteBufCodecs.VAR_INT, CardProgress::getPrestige,
            (challenges, points, finished, rerolls, claimed, perks, owner, ownerName, prestige) -> {
                CardProgress progress = new CardProgress(challenges, points, finished, rerolls, claimed, perks);
                progress.setOwner(owner, ownerName);
                progress.prestige = prestige;
                return progress;
            });

    private final List<Challenge> challenges;
    private int potionPoints;
    private final List<Challenge> finished;
    // Rerolls used in the current tier.
    private int rerolls;
    // Tiers (values) whose Shape Certificate was claimed; kept across tier-ups so each is claimed once per card.
    private final Set<Integer> claimedCertificates = new TreeSet<>();

    // Tiers (values) whose perk was picked, as for the certificates.
    private final Set<Integer> claimedPerks = new TreeSet<>();

    public CardProgress() {
        this(List.of(), 0, List.of(), 0, List.of(), List.of());
    }

    public CardProgress(List<Challenge> challenges, int potionPoints, List<Challenge> finished) {
        this(challenges, potionPoints, finished, 0, List.of(), List.of());
    }

    public CardProgress(List<Challenge> challenges, int potionPoints, List<Challenge> finished, int rerolls, List<Integer> claimedCertificates, List<Integer> claimedPerks) {
        this.challenges = new ArrayList<>(challenges);
        this.potionPoints = potionPoints;
        this.finished = new ArrayList<>(finished);
        this.rerolls = rerolls;
        this.claimedCertificates.addAll(claimedCertificates);
        this.claimedPerks.addAll(claimedPerks);
    }

    // The player the card is bound to (their UUID and name), where the server binds cards; "" for nobody yet.
    private String owner = "", ownerName = "";
    // How many times the card was taken from Mastered back to Unlearned.
    private int prestige;

    public String getOwner() {
        return this.owner;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public void setOwner(String owner, String ownerName) {
        this.owner = owner;
        this.ownerName = ownerName;
    }

    public int getPrestige() {
        return this.prestige;
    }

    /** One more prestige: the tiers' certificates and perks can be claimed again on the way back up. */
    public void prestige() {
        this.prestige++;
        this.claimedCertificates.clear();
        this.claimedPerks.clear();
    }

    public boolean isPerkClaimed(int tier) {
        return this.claimedPerks.contains(tier);
    }

    public void claimPerk(int tier) {
        this.claimedPerks.add(tier);
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
        this.claimedPerks.clear();
        this.claimedPerks.addAll(other.claimedPerks);
        this.owner = other.owner;
        this.ownerName = other.ownerName;
        this.prestige = other.prestige;
    }

    public CardProgress copy() {
        CardProgress copy = this.copyBase();
        copy.setOwner(this.owner, this.ownerName);
        copy.prestige = this.prestige;
        return copy;
    }

    private CardProgress copyBase() {
        return new CardProgress(this.challenges.stream().map(Challenge::copy).toList(), this.potionPoints,
                this.finished.stream().map(Challenge::copy).toList(), this.rerolls, List.copyOf(this.claimedCertificates), List.copyOf(this.claimedPerks));
    }

    public boolean isEmpty() {
        return this.challenges.isEmpty() && this.potionPoints == 0 && this.claimedCertificates.isEmpty() && this.claimedPerks.isEmpty() && this.owner.isEmpty() && this.prestige == 0;
    }
}
