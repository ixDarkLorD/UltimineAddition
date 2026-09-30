package net.ixdarklord.ultimine_addition.network.payloads;

import net.ixdarklord.coolcatcore.api.network.PacketContext;
import dev.ftb.mods.ftbultimine.shape.Shape;
import io.netty.buffer.ByteBuf;
import net.ixdarklord.ultimine_addition.common.data.item.SelectedShapeData;
import net.ixdarklord.ultimine_addition.common.menu.ShapeSelectorMenu;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.ixdarklord.coolcatcore.api.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record UpdateItemShapePayload(String shapeId) implements CustomPacketPayload {
    public static final Type<UpdateItemShapePayload> TYPE = new Type<>(FTBUltimineAddition.id("update_item_shape"));

    public static final StreamCodec<ByteBuf, UpdateItemShapePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, UpdateItemShapePayload::shapeId,
            UpdateItemShapePayload::new
    );

    public static void handle(UpdateItemShapePayload message, PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer().containerMenu instanceof ShapeSelectorMenu menu)) return;
            ItemStack stack = menu.getSlot(0).getItem();
            if (stack.isEmpty()) return;

            if (message.shapeId.isEmpty()) {
                Registration.SELECTED_SHAPE_COMPONENT.remove(stack);
                return;
            }

            Shape shape = FTBUltimineIntegration.getShape(new ResourceLocation(message.shapeId));
            if (shape == null) return;
            Registration.SELECTED_SHAPE_COMPONENT.set(stack, new SelectedShapeData(shape));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
