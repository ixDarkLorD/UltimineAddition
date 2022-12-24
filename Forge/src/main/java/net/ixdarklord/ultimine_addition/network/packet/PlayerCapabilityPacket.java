package net.ixdarklord.ultimine_addition.network.packet;

import net.ixdarklord.ultimine_addition.data.player.PlayerUltimineCapabilityProvider;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerCapabilityPacket {
    private final Entity entity;
    private final boolean state;

    public PlayerCapabilityPacket(Entity entity, boolean state) {
        this.entity = entity;
        this.state = state;
    }

    public PlayerCapabilityPacket(FriendlyByteBuf buf) {
        Minecraft MC = Minecraft.getInstance();
        assert MC.level != null;
        this.entity = MC.level.getEntity(buf.readInt());
        this.state = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entity.getId());
        buf.writeBoolean(state);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player != null) {
                player.getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                    capability.setCapability(state);
                    PacketHandler.sendToPlayer(new PlayerCapabilityPacket.DataSyncS2C(capability.getCapability()), (ServerPlayer) entity);
                });
            }
        });
        context.setPacketHandled(true);
    }

    public static class DataSyncS2C {
        private final boolean state;

        public DataSyncS2C(boolean state) {
            this.state = state;
        }
        public DataSyncS2C(FriendlyByteBuf buf) {
            this.state = buf.readBoolean();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(state);
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    player.getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent(capability ->
                            capability.setCapability(state));
                }
            });
        }
    }
}
