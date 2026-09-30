package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbultimine.shape.Shape;
import io.netty.handler.codec.CodecException;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;

public record SelectedShapeData(Shape shape) {
    public static final Codec<Shape> SHAPE_CODEC = Codec.STRING.comapFlatMap(id -> {
        for (Shape shape : FTBUltimineIntegration.getShapesList()) {
            // The earlier 1.20.1 releases saved FTB Ultimine 2001's plain shape name ("small_tunnel").
            if (FTBUltimineIntegration.shapeId(shape).toString().equals(id) || shape.getName().equals(id))
                return DataResult.success(shape);
        }
        return DataResult.error(() -> "Invalid shape ID: '" + id + "'.");
    }, shape1 -> FTBUltimineIntegration.shapeId(shape1).toString());

    public static final StreamCodec<FriendlyByteBuf, Shape> SHAPE_STREAM_CODEC = StreamCodec.of(
            (buf, shape) -> buf.writeResourceLocation(FTBUltimineIntegration.shapeId(shape)),
            buf -> {
                Shape shape = FTBUltimineIntegration.getShape(buf.readResourceLocation());
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
