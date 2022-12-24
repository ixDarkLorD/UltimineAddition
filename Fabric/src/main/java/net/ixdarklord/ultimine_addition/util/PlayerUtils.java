package net.ixdarklord.ultimine_addition.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ixdarklord.ultimine_addition.data.IDataHandler;
import net.ixdarklord.ultimine_addition.data.player.PlayerUltimineData;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PlayerUtils {
    public static class CapabilityData {
        public static void set(IDataHandler handler, boolean state) {
            PlayerUltimineData data = handler.getPlayerUltimineData();
            data.setCapability(state);
            sendData2Client((ServerPlayer) handler, data.getCapability());
        }
        public static void sync(IDataHandler handler) {
            PlayerUltimineData data = handler.getPlayerUltimineData();
            sendData2Client((ServerPlayer) handler, data.getCapability());
        }
        private static void sendData2Client(ServerPlayer player, boolean state) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(state);
            ServerPlayNetworking.send(player, PacketHandler.PLAYER_CAPABILITY_SYNC_ID, buf);
        }
    }
}
