package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbultimine.shape.Shape;
import io.netty.handler.codec.CodecException;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.mixin.ShapeRegistryAccessor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SelectedShapeData(Shape shape) {
    public static final Codec<Shape> SHAPE_CODEC = Codec.STRING.comapFlatMap(id -> {
        for (Shape shape : ShapeRegistryAccessor.getShapesList()) {
            if (shape.getName().equals(id))
                return DataResult.success(shape);
        }
        return DataResult.error(() -> "Invalid shape ID: '" + id + "'.");
    }, Shape::getName);

    public static final StreamCodec<FriendlyByteBuf, Shape> SHAPE_STREAM_CODEC = StreamCodec.of(
            (buf, shape) -> buf.writeUtf(shape.getName()),
            buf -> {
                Shape shape = FTBUltimineIntegration.getShape(buf.readUtf());
                if (shape == null) throw new CodecException("Shape is null!!");
                return shape;
            });

    public static final Codec<SelectedShapeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SHAPE_CODEC.fieldOf("ShapeId").forGetter(SelectedShapeData::shape)
    ).apply(instance, SelectedShapeData::new));

    public static final StreamCodec<FriendlyByteBuf, SelectedShapeData> STREAM_CODEC = StreamCodec.composite(
            SHAPE_STREAM_CODEC, SelectedShapeData::shape,
            SelectedShapeData::new
    );

}
