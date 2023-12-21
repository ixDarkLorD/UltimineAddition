package net.ixdarklord.ultimine_addition.common.data.challenge;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Random;

public class ChallengesData {
    private final MiningSkillCardItem.Type forCardType;
    private final MiningSkillCardItem.Tier forCardTier;
    private final Type challengeType;
    private final ItemStack requiredSpecificTool;
    private final List<String> targetedBlocks;
    private final Pair<Integer, Integer> requiredAmount;

    public static final Codec<ChallengesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MiningSkillCardItem.Type.CHALLENGE_CODEC.fieldOf("for_card_type").forGetter(ChallengesData::getForCardType),
            MiningSkillCardItem.Tier.CODEC.fieldOf("for_card_tier").forGetter(ChallengesData::getForCardTier),
            Type.CODEC.fieldOf("challenge_type").forGetter(ChallengesData::getChallengeType),
            Codec.pair(Codec.INT.fieldOf("min").codec(), Codec.INT.fieldOf("max").codec()).optionalFieldOf("required_amount", new Pair<>(1, 1)).forGetter(ChallengesData::getRequiredAmountPair),
            ItemStack.CODEC.optionalFieldOf("required_specific_tool", ItemStack.EMPTY, Lifecycle.experimental()).forGetter(ChallengesData::getRequiredSpecificTool),
            Codec.STRING.listOf().fieldOf("targeted_blocks").forGetter(ChallengesData::getTargetedBlocks)
    ).apply(instance, ChallengesData::new));

    public ChallengesData(MiningSkillCardItem.Type forCardType, MiningSkillCardItem.Tier forCardTier, Type challengeType, Pair<Integer, Integer> requiredAmount, ItemStack requiredSpecificTool, List<String> targetedBlocks) {
        this.forCardType = forCardType;
        this.forCardTier = forCardTier;
        this.challengeType = challengeType;
        this.requiredAmount = requiredAmount;
        this.requiredSpecificTool = requiredSpecificTool;
        this.targetedBlocks = targetedBlocks;
    }

    public MiningSkillCardItem.Type getForCardType() {
        return forCardType;
    }

    public MiningSkillCardItem.Tier getForCardTier() {
        return forCardTier;
    }

    public Type getChallengeType() {
        return challengeType;
    }

    public int getRequiredAmount() {
        Random random = new Random();
        return random.nextInt(requiredAmount.getSecond() - requiredAmount.getFirst() + 1) + requiredAmount.getFirst();
    }

    private Pair<Integer, Integer> getRequiredAmountPair() {
        return requiredAmount;
    }

    public ItemStack getRequiredSpecificTool() {
        return requiredSpecificTool;
    }

    public List<String> getTargetedBlocks() {
        return targetedBlocks;
    }

    public static void writeBuffer(FriendlyByteBuf buf, ChallengesData data) {
        buf.writeUtf(data.getForCardType().getId());
        buf.writeInt(data.getForCardTier().getValue());

        buf.writeUtf(data.getChallengeType().getTypeName());
        buf.writeBoolean(data.getChallengeType().isConsuming());

        buf.writeInt(data.getRequiredAmountPair().getFirst());
        buf.writeInt(data.getRequiredAmountPair().getSecond());

        buf.writeItem(data.getRequiredSpecificTool());
        buf.writeCollection(data.getTargetedBlocks(), FriendlyByteBuf::writeUtf);
    }

    public static ChallengesData readBuffer(FriendlyByteBuf buf) {
        MiningSkillCardItem.Type cardType = MiningSkillCardItem.Type.fromString(buf.readUtf());
        MiningSkillCardItem.Tier cardTier = MiningSkillCardItem.Tier.fromInt(buf.readInt());
        Type type = Type.fromValues(buf.readUtf(), buf.readBoolean());
        Pair<Integer, Integer> requiredAmounts = Pair.of(buf.readInt(), buf.readInt());
        ItemStack requiredSpecificTool = buf.readItem();
        List<String> targetedBlocks = buf.readList(FriendlyByteBuf::readUtf);
        return new ChallengesData(cardType, cardTier, type, requiredAmounts, requiredSpecificTool, targetedBlocks);
    }

    public enum Type {
        BREAK_BLOCK("break_block", false),
        STRIP_BLOCK("strip_block", false),
        FLATTEN_BLOCK("flatten_block", false),
        TILLING_BLOCK("tilling_block", false),
        @ApiStatus.Experimental
        INTERACT_WITH_BLOCK("interact_with_block", false),
        BREAK_BLOCK_CONSUME("break_block", true),
        STRIP_BLOCK_CONSUME("strip_block", true),
        FLATTEN_BLOCK_CONSUME("flatten_block", true),
        TILLING_BLOCK_CONSUME("tilling_block", true),
        @ApiStatus.Experimental
        INTERACT_WITH_BLOCK_CONSUME("interact_with_block", true);

        private final String type;
        private final boolean consume;
        Type(String type, boolean consume) {
            this.type = type;
            this.consume = consume;
        }

        public static final Codec<Type> CODEC = Codec.pair(Codec.STRING.fieldOf("id").codec(), Codec.BOOL.fieldOf("consume_block").codec()).comapFlatMap(pair -> {
            try {
                return DataResult.success(Type.fromValues(pair.getFirst(), pair.getSecond()));
            } catch (EnumConstantNotPresentException e) {
                return DataResult.error(() -> pair + " is not present.");
            }
        }, Type::getPair);

        public String getTypeName() {
            return type.toLowerCase();
        }

        public Type getConsumeVersion() {
            return fromValues(type, true);
        }

        public boolean isConsuming() {
            return consume;
        }

        private Pair<String, Boolean> getPair() {
            return Pair.of(type, consume);
        }

        public static Type fromValues(String input, boolean state) {
            for (Type enumValue : Type.values()) {
                if (enumValue.getTypeName().equalsIgnoreCase(input) && enumValue.isConsuming() == state) {
                    return enumValue; // Return the matching enum value
                }
            }
            throw new IllegalArgumentException(String.format("No enum constant with the specified values: %s, %s", input, state));
        }
    }
}
