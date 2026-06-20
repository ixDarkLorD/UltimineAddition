package net.ixdarklord.ultimine_addition.common.data.challenge;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus.Experimental;

import java.util.List;
import java.util.Random;

public record ChallengeData(MiningSkillCardItem.Type forCardType, MiningSkillCardItem.Tier forCardTier,
                            Type challengeType, Pair<Integer, Integer> requiredAmount, ItemStack requiredSpecificTool,
                            List<String> targetedBlocks) {
    public static final Codec<ChallengeData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(MiningSkillCardItem.Type.CODEC.fieldOf("for_card_type").forGetter(ChallengeData::forCardType), MiningSkillCardItem.Tier.CODEC.fieldOf("for_card_tier").forGetter(ChallengeData::forCardTier), ChallengeData.Type.CODEC.fieldOf("challenge_type").forGetter(ChallengeData::challengeType), Codec.pair(Codec.INT.fieldOf("min").codec(), Codec.INT.fieldOf("max").codec()).optionalFieldOf("required_amount", new Pair<>(1, 1)).forGetter(ChallengeData::getRequiredAmountPair), ItemStack.CODEC.optionalFieldOf("required_specific_tool", ItemStack.EMPTY, Lifecycle.experimental()).forGetter(ChallengeData::requiredSpecificTool), Codec.STRING.listOf().fieldOf("targeted_blocks").forGetter(ChallengeData::targetedBlocks)).apply(instance, ChallengeData::new));

    public int getRequiredAmount() {
        Random random = new Random();
        return random.nextInt(this.requiredAmount.getSecond() - this.requiredAmount.getFirst() + 1) + this.requiredAmount.getFirst();
    }

    private Pair<Integer, Integer> getRequiredAmountPair() {
        return this.requiredAmount;
    }

    public static void writeBuffer(FriendlyByteBuf buf, ChallengeData data) {
        buf.writeUtf(data.forCardType().id());
        buf.writeInt(data.forCardTier().getValue());
        buf.writeUtf(data.challengeType().getTypeId());
        buf.writeBoolean(data.challengeType().isConsuming());
        buf.writeInt(data.getRequiredAmountPair().getFirst());
        buf.writeInt(data.getRequiredAmountPair().getSecond());
        buf.writeItem(data.requiredSpecificTool());
        buf.writeCollection(data.targetedBlocks(), FriendlyByteBuf::writeUtf);
    }

    public static ChallengeData readBuffer(FriendlyByteBuf buf) {
        MiningSkillCardItem.Type cardType = MiningSkillCardItem.Type.fromString(buf.readUtf());
        MiningSkillCardItem.Tier cardTier = MiningSkillCardItem.Tier.fromInt(buf.readInt());
        Type type = ChallengeData.Type.fromValues(buf.readUtf(), buf.readBoolean());
        Pair<Integer, Integer> requiredAmounts = Pair.of(buf.readInt(), buf.readInt());
        ItemStack requiredSpecificTool = buf.readItem();
        List<String> targetedBlocks = buf.readList(FriendlyByteBuf::readUtf);
        return new ChallengeData(cardType, cardTier, type, requiredAmounts, requiredSpecificTool, targetedBlocks);
    }

    public enum Type {
        BREAK_BLOCK("break_block", false),
        STRIP_BLOCK("strip_block", false),
        FLATTEN_BLOCK("flatten_block", false),
        TILLING_BLOCK("tilling_block", false),
        @Experimental
        INTERACT_WITH_BLOCK("interact_with_block", false),
        BREAK_BLOCK_CONSUME("break_block", true),
        STRIP_BLOCK_CONSUME("strip_block", true),
        FLATTEN_BLOCK_CONSUME("flatten_block", true),
        TILLING_BLOCK_CONSUME("tilling_block", true),
        @Experimental
        INTERACT_WITH_BLOCK_CONSUME("interact_with_block", true);

        private final String id;
        private final boolean consume;
        public static final Codec<Type> CODEC = Codec.pair(Codec.STRING.fieldOf("id").codec(), Codec.BOOL.fieldOf("consume_block").codec()).comapFlatMap((pair) -> {
            try {
                return DataResult.success(fromValues(pair.getFirst(), pair.getSecond()));
            } catch (EnumConstantNotPresentException e) {
                return DataResult.error(() -> pair + " is not present.");
            }
        }, Type::getPair);

        Type(String id, boolean consume) {
            this.id = id;
            this.consume = consume;
        }

        public String getTypeId() {
            return this.id.toLowerCase();
        }

        public Type getConsumeVersion() {
            return fromValues(this.id, true);
        }

        public boolean isConsuming() {
            return this.consume;
        }

        private Pair<String, Boolean> getPair() {
            return Pair.of(this.id, this.consume);
        }

        public static Type fromValues(String input, boolean state) {
            for (Type enumValue : values()) {
                if (enumValue.getTypeId().equalsIgnoreCase(input) && enumValue.isConsuming() == state) {
                    return enumValue;
                }
            }

            throw new IllegalArgumentException(String.format("No enum constant with the specified values: %s, %s", input, state));
        }
    }
}
