package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.ixdarklord.coolcatlib.api.data.ItemDataComponent;
import net.ixdarklord.coolcatlib.api.utils.CodecUtils;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class SelectedShapeData extends ItemDataComponent<SelectedShapeData> {
    public static final ResourceLocation DATA_ID = FTBUltimineAddition.id("selected_shape_data");
    public static final Codec<Shape> SHAPE_CODEC;
    public static final Codec<SelectedShapeData> CODEC;
    private @Nullable Shape shape;

    private SelectedShapeData(@Nullable Shape shape) {
        super(DATA_ID, CODEC);
        this.shape = shape;
    }

    private static SelectedShapeData create() {
        return new SelectedShapeData(null);
    }

    public static SelectedShapeData load(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag().getCompound(DATA_ID.toString());
        SelectedShapeData data = tag.isEmpty() ? create() : CodecUtils.decode(CODEC, tag);
        return data.setStack(stack);
    }

    public static boolean hasData(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            return stack.getTagElement(DATA_ID.toString()) != null;
        } else {
            return false;
        }
    }

    public @Nullable Shape getShape() {
        return this.shape;
    }

    public SelectedShapeData setShape(@Nullable Shape shape) {
        this.shape = shape;
        return this;
    }

    static {
        SHAPE_CODEC = Codec.STRING.comapFlatMap((id) -> {
            for (Shape shape : FTBUltimineIntegration.getShapesList()) {
                if (shape.getName().equals(id)) {
                    return DataResult.success(shape);
                }
            }

            return DataResult.error(() -> "Invalid shape ID: '" + id + "'");
        }, Shape::getName);
        CODEC = RecordCodecBuilder.create((instance) -> instance.group(SHAPE_CODEC.optionalFieldOf("ShapeId").forGetter((data) -> Optional.ofNullable(data.getShape()))).apply(instance, (shapeOpt) -> new SelectedShapeData(shapeOpt.orElse(null))));
    }
}
