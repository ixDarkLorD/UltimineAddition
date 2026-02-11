package net.ixdarklord.ultimine_addition.common.data.challenge;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Random;

public record ChallengeData(MiningSkillCardItem.Type forCardType, MiningSkillCardItem.Tier forCardTier,
                            Type challengeType, Pair<Integer, Integer> requiredAmount, ItemStack requiredSpecificTool,
                            List<String> targetedBlocks) {
    public static final Codec<ChallengeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MiningSkillCardItem.Type.CODEC.fieldOf("for_card_type").forGetter(ChallengeData::forCardType),
            MiningSkillCardItem.Tier.CODEC.fieldOf("for_card_tier").forGetter(ChallengeData::forCardTier),
            Type.CODEC.fieldOf("challenge_type").forGetter(ChallengeData::challengeType),
            Codec.pair(Codec.INT.fieldOf("min").codec(), Codec.INT.fieldOf("max").codec()).optionalFieldOf("required_amount", new Pair<>(1, 1)).forGetter(ChallengeData::getRequiredAmountPair),
            ItemStack.CODEC.optionalFieldOf("required_specific_tool", ItemStack.EMPTY, Lifecycle.experimental()).forGetter(ChallengeData::requiredSpecificTool),
            Codec.STRING.listOf().fieldOf("targeted_blocks").forGetter(ChallengeData::targetedBlocks)
    ).apply(instance, ChallengeData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChallengeData> STREAM_CODEC = StreamCodec.of(ChallengeData::writeBuffer, ChallengeData::readBuffer);

    public int getRequiredAmount() {
        Random random = new Random();
        return random.nextInt(requiredAmount.getSecond() - requiredAmount.getFirst() + 1) + requiredAmount.getFirst();
    }

    private Pair<Integer, Integer> getRequiredAmountPair() {
        return requiredAmount;
    }

    public static void writeBuffer(RegistryFriendlyByteBuf buf, ChallengeData data) {
        buf.writeUtf(data.forCardType().getId());
        buf.writeInt(data.forCardTier().getValue());

        buf.writeUtf(data.challengeType().getTypeId());
        buf.writeBoolean(data.challengeType().isConsuming());

        buf.writeInt(data.getRequiredAmountPair().getFirst());
        buf.writeInt(data.getRequiredAmountPair().getSecond());

        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, data.requiredSpecificTool());
        buf.writeCollection(data.targetedBlocks(), FriendlyByteBuf::writeUtf);
    }

    public static ChallengeData readBuffer(RegistryFriendlyByteBuf buf) {
        MiningSkillCardItem.Type cardType = MiningSkillCardItem.Type.fromString(buf.readUtf());
        MiningSkillCardItem.Tier cardTier = MiningSkillCardItem.Tier.fromInt(buf.readInt());
        Type type = Type.fromValues(buf.readUtf(), buf.readBoolean());
        Pair<Integer, Integer> requiredAmounts = Pair.of(buf.readInt(), buf.readInt());
        ItemStack requiredSpecificTool = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        List<String> targetedBlocks = buf.readList(FriendlyByteBuf::readUtf);
        return new ChallengeData(cardType, cardTier, type, requiredAmounts, requiredSpecificTool, targetedBlocks);
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

        public String getTypeId() {
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
                if (enumValue.getTypeId().equalsIgnoreCase(input) && enumValue.isConsuming() == state) {
                    return enumValue; // Return the matching enum value
                }
            }
            throw new IllegalArgumentException(String.format("No enum constant with the specified values: %s, %s", input, state));
        }
    }
}
