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

public final class CardProgress {
    public static final Codec<CardProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Challenge.CODEC.listOf().optionalFieldOf("Challenges", List.of()).forGetter(CardProgress::getChallenges),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("PotionPoints", 0).forGetter(CardProgress::getPotionPoints)
    ).apply(instance, (challenges, points) -> new CardProgress(challenges, points, List.of())));

    public static final StreamCodec<RegistryFriendlyByteBuf, CardProgress> STREAM_CODEC = StreamCodec.composite(
            Challenge.STREAM_CODEC.apply(ByteBufCodecs.list()), CardProgress::getChallenges,
            ByteBufCodecs.VAR_INT, CardProgress::getPotionPoints,
            Challenge.STREAM_CODEC.apply(ByteBufCodecs.list()), CardProgress::getFinished,
            CardProgress::new);

    private final List<Challenge> challenges;
    private int potionPoints;
    private final List<Challenge> finished;

    public CardProgress() {
        this(List.of(), 0, List.of());
    }

    public CardProgress(List<Challenge> challenges, int potionPoints, List<Challenge> finished) {
        this.challenges = new ArrayList<>(challenges);
        this.potionPoints = potionPoints;
        this.finished = new ArrayList<>(finished);
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
    }

    public CardProgress copy() {
        return new CardProgress(this.challenges.stream().map(Challenge::copy).toList(), this.potionPoints,
                this.finished.stream().map(Challenge::copy).toList());
    }

    public boolean isEmpty() {
        return this.challenges.isEmpty() && this.potionPoints == 0;
    }
}
