package net.ixdarklord.ultimine_addition.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.ixdarklord.ultimine_addition.data.IDataHandler;
import net.ixdarklord.ultimine_addition.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class PlayerCapabilityPacket {
    public static void receive(MinecraftServer server, ServerPlayer serverPlayer,
                               ServerGamePacketListenerImpl ignored1,
                               FriendlyByteBuf buf, PacketSender ignored2) {
        final boolean state = buf.readBoolean();
        server.execute(() -> {
            PlayerUtils.CapabilityData.set((IDataHandler) serverPlayer, state);
        });
    }

    public static class DataSyncS2C {
        public static void receive(Minecraft client, ClientPacketListener ignored1,
                                   FriendlyByteBuf buf, PacketSender ignored2) {
            final boolean state = buf.readBoolean();
            client.execute(() -> {
                if (client.player != null) {
                    ((IDataHandler) client.player).getPlayerUltimineData().setCapability(state);
                }
            });
        }
    }
}
