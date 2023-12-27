package net.ixdarklord.ultimine_addition.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.handler.codec.CodecException;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.awt.*;
import java.util.stream.IntStream;

public class CodecHelper {
    public static final Codec<Item> ITEM_CODEC = ResourceLocation.CODEC.comapFlatMap(location -> {
        try {
            return DataResult.success(Registry.ITEM.get(location));
        } catch (CodecException e) {
            return DataResult.error(location + " is not registered item.");
        }
    }, Registry.ITEM::getKey).stable();

    public static final Codec<Color> COLOR_CODEC = Codec.INT_STREAM.comapFlatMap(intStream -> {
        try {
            return Util.fixedSize(intStream, 3).map(intArray ->
                    new Color(intArray[0], intArray[1], intArray[2]));
        } catch (CodecException e) {
            int[] values = intStream.toArray();
            return DataResult.error("Invalid Color Format. [r: %s, g: %s, b: %s]".formatted(values[0], values[1], values[2]));
        }
    }, color -> IntStream.of(color.getRed(), color.getGreen(), color.getBlue())).stable();
}
