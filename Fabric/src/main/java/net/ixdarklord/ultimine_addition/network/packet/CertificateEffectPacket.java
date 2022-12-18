package net.ixdarklord.ultimine_addition.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class CertificateEffectPacket {
    public static void receive(Minecraft client, ClientPacketListener clientPacketListener,
                               FriendlyByteBuf buf, PacketSender packetSender) {

        ItemStack stack = buf.readItem();
        assert client.level != null;
        Entity entity = client.level.getEntity(buf.readInt());
        client.execute(() -> {
            MinerCertificate.playAnimation(stack, entity);
        });
    }
}
