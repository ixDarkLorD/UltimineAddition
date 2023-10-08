package net.ixdarklord.ultimine_addition.datagen.challenge.builder;

import com.mojang.datafixers.util.Pair;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ChallengesBuilder {
    private final ResourceLocation id;
    private final MiningSkillCardItem.Type forCardType;
    private MiningSkillCardItem.Tier forCardTier;
    private ChallengesData.Type challengeType;
    private Pair<Integer, Integer> requiredAmount;
    private ItemStack requiredSpecificTool;
    private final List<String> targetedBlocks = new ArrayList<>();
    private ChallengesBuilder(ResourceLocation id, MiningSkillCardItem.Type forCardType) {
        this.id = id;
        this.forCardType = forCardType;
    }

    public static ChallengesBuilder create(ResourceLocation id, MiningSkillCardItem.Type forCardType) {
        return new ChallengesBuilder(id, forCardType);
    }

    public ChallengesBuilder forTier(MiningSkillCardItem.Tier forCardTier) {
        this.forCardTier = forCardTier;
        return this;
    }

    public ChallengesBuilder forType(ChallengesData.Type challengeType) {
        this.challengeType = challengeType;
        return this;
    }

    public ChallengesBuilder specificTool(@NotNull ItemStack item) {
        requiredSpecificTool = item;
        return this;
    }

    public ChallengesBuilder targetedBlocks(@NotNull Block... blocks) {
        for (var block : blocks) this.targetedBlocks.add(Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(block.asItem())).toString());
        return this;
    }

    @SafeVarargs
    public final ChallengesBuilder targetedBlocks(@NotNull TagKey<Block>... tagKeys) {
        for (var tag : tagKeys) this.targetedBlocks.add("#" + tag.location());
        return this;
    }

    public ChallengesBuilder requiredAmount(int value) {
        requiredAmount = Pair.of(value, value);
        return this;
    }

    public ChallengesBuilder requiredAmount(int min, int max) {
        if (min > max) throw new IllegalArgumentException(String.format("Min value is bigger than the max value. Min: %s | Max: %s", min, max));
        requiredAmount = Pair.of(min, max);
        return this;
    }

    public void save(Consumer<Result> consumer) {
        this.ensureValid();
        ChallengesData data = new ChallengesData(this.forCardType, this.forCardTier, this.challengeType, this.requiredAmount, this.requiredSpecificTool, this.targetedBlocks);
        consumer.accept(new Result(this.id, data));
    }

    private void ensureValid() {
        if (this.challengeType == null) throw new IllegalStateException("The challenge type it's not assigned " + this.id);
        if (this.targetedBlocks.isEmpty()) throw new IllegalStateException("There is no targeted blocks to accomplish this challenge " + this.id);
        if (this.requiredSpecificTool == null) this.requiredSpecificTool = ItemStack.EMPTY;
        if (this.forCardTier == null) this.forCardTier = MiningSkillCardItem.Tier.Unlearned;
        if (this.requiredAmount.getFirst() <= 0 && this.requiredAmount.getFirst() <= 0) this.requiredAmount = Pair.of(1, 1);
    }

    public record Result(ResourceLocation id, ChallengesData data) {}
}
