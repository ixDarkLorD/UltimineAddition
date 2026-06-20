package net.ixdarklord.ultimine_addition.network.packets;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.ixdarklord.ultimine_addition.common.data.item.SelectedShapeData;
import net.ixdarklord.ultimine_addition.common.menu.ShapeSelectorMenu;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class UpdateItemShapePacket extends BaseC2SMessage {
    private final String shapeId;

    public UpdateItemShapePacket(String shapeId) {
        this.shapeId = shapeId;
    }

    public UpdateItemShapePacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.shapeId);
    }

    public MessageType getType() {
        return PacketHandler.UPDATE_ITEM_SHAPE;
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            AbstractContainerMenu abstractContainerMenu = context.getPlayer().containerMenu;
            if (abstractContainerMenu instanceof ShapeSelectorMenu menu) {
                ItemStack stack = menu.getSlot(0).getItem();
                if (!stack.isEmpty()) {
                    if (this.shapeId.isEmpty()) {
                        stack.getOrCreateTag().remove(SelectedShapeData.DATA_ID.toString());
                    } else {
                        Shape shape = FTBUltimineIntegration.getShape(this.shapeId);
                        if (shape != null) {
                            SelectedShapeData.load(stack).setShape(shape).save();
                        }
                    }
                }
            }
        });
    }
}
