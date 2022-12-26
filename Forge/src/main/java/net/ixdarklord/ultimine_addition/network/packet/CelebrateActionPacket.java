package net.ixdarklord.ultimine_addition.network.packet;

import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CelebrateActionPacket {
    private final String actionName;
    private final ItemStack stack;
    private final Entity entity;

    public CelebrateActionPacket(String actionName, ItemStack stack, Entity entity) {
        this.actionName = actionName;
        this.stack = stack;
        this.entity = entity;
    }

    public CelebrateActionPacket(FriendlyByteBuf buf) {
        this.actionName = buf.readUtf();
        this.stack = buf.readItem();
        Minecraft MC = Minecraft.getInstance();
        assert MC.level != null;
        this.entity = MC.level.getEntity(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(actionName);
        buf.writeItem(stack);
        buf.writeInt(entity.getId());
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (actionName.equals("obtained")) {
                MinerCertificate.playParticleAndSound(context.getSender());
            }
            PacketHandler.sendToPlayer(new Play2Client(actionName, stack, context.getSender()), context.getSender());
        });
        context.setPacketHandled(true);
    }

    public static class Play2Client {
        private final String actionName;
        private final ItemStack stack;
        private Entity entity;

        public Play2Client(String actionName, ItemStack stack, Entity entity) {
            this.actionName = actionName;
            this.stack = stack;
            this.entity = entity;
        }

        public Play2Client(FriendlyByteBuf buf) {
            this.actionName = buf.readUtf();
            Minecraft MC = Minecraft.getInstance();
            this.stack = buf.readItem();
            if (MC.level != null)
                this.entity = MC.level.getEntity(buf.readInt());
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(actionName);
            buf.writeItem(stack);
            buf.writeInt(entity.getId());
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                switch (actionName) {
                    case "obtained" -> MinerCertificate.playAnimation(stack, entity);
                    case "accomplished" -> MinerCertificate.playClientSound(entity);
                }
            }));
            context.setPacketHandled(true);
        }
    }
}
