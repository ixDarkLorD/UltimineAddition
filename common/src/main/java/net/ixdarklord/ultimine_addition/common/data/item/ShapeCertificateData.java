package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// The Mining Skill Card type (tool) a Shape Certificate unlocks shapes for. Kept as the type's id, so a
// certificate for a custom card type that was removed stays readable.
public record ShapeCertificateData(String tool) {
    public static final Codec<ShapeCertificateData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("Tool").forGetter(ShapeCertificateData::tool)
    ).apply(instance, ShapeCertificateData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapeCertificateData> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(ShapeCertificateData::new, ShapeCertificateData::tool).cast();
    public static final DataComponentType<ShapeCertificateData> DATA_COMPONENT =
            DataComponentType.<ShapeCertificateData>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).build();
    // The one shape a certificate teaches. A separate component, so item models can still select on the tool alone.
    public static final DataComponentType<ResourceLocation> SHAPE_COMPONENT =
            DataComponentType.<ResourceLocation>builder().persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).build();

    public static @Nullable ResourceLocation getShape(ItemStack stack) {
        return stack.get(SHAPE_COMPONENT);
    }

    public static @Nullable String getTool(ItemStack stack) {
        ShapeCertificateData data = stack.get(DATA_COMPONENT);
        return data == null ? null : data.tool;
    }

    public static @Nullable MiningSkillCardItem.Type getType(ItemStack stack) {
        String tool = getTool(stack);
        if (tool == null) return null;
        try {
            return MiningSkillCardItem.Type.fromString(tool);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
