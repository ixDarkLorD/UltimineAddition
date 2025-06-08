package net.ixdarklord.ultimine_addition.network.payloads;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import io.netty.buffer.ByteBuf;
import net.ixdarklord.ultimine_addition.common.data.item.SelectedShapeData;
import net.ixdarklord.ultimine_addition.common.menu.ShapeSelectorMenu;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record UpdateItemShapePayload(String shapeId) implements CustomPacketPayload {
    public static final Type<UpdateItemShapePayload> TYPE = new Type<>(FTBUltimineAddition.rl("update_item_shape"));

    public static final StreamCodec<ByteBuf, UpdateItemShapePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, UpdateItemShapePayload::shapeId,
            UpdateItemShapePayload::new
    );

    public static void handle(UpdateItemShapePayload message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer().containerMenu instanceof ShapeSelectorMenu menu)) return;
            ItemStack stack = menu.getSlot(0).getItem();
            if (stack.isEmpty()) return;

            if (message.shapeId.isEmpty()) {
                stack.remove(Registration.SELECTED_SHAPE_COMPONENT.get());
                return;
            }

            Shape shape = FTBUltimineIntegration.getShape(ResourceLocation.parse(message.shapeId));
            if (shape == null) return;
            stack.set(Registration.SELECTED_SHAPE_COMPONENT.get(), new SelectedShapeData(shape));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
