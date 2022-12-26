package net.ixdarklord.ultimine_addition.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class CelebrateActionPacket {
    public static void receive(MinecraftServer server, ServerPlayer player,
                               ServerGamePacketListenerImpl ignored1,
                               FriendlyByteBuf buf, PacketSender ignored2) {

        String actionName = buf.readUtf();
        ItemStack stack = buf.readItem();
        Minecraft MC = Minecraft.getInstance();
        assert MC.level != null;
        Entity entity = MC.level.getEntity(buf.readInt());
        assert entity != null;

        server.execute(() -> {
            if (actionName.equals("obtained")) {
                MinerCertificate.playParticleAndSound(player);
            }
            var buffer = PacketByteBufs.create();
            buffer.writeUtf(actionName);
            buffer.writeItem(stack);
            buffer.writeInt(entity.getId());
            ServerPlayNetworking.send(player, PacketHandler.CELEBRATE_ACTION_SYNC_ID, buffer);
        });
    }

    public static class Play2Client {
        public static void receive(Minecraft client, ClientPacketListener ignored1,
                                   FriendlyByteBuf buf, PacketSender ignored2) {

            String actionName = buf.readUtf();
            ItemStack stack = buf.readItem();
            assert client.level != null;
            Entity entity = client.level.getEntity(buf.readInt());
            assert entity != null;

            client.execute(() -> {
                switch (actionName) {
                    case "obtained" -> MinerCertificate.playAnimation(stack, entity);
                    case "accomplished" -> MinerCertificate.playClientSound(entity);
                }
            });
        }
    }
}
