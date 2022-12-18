package net.ixdarklord.ultimine_addition.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.network.packet.CertificateEffectPacket;
import net.ixdarklord.ultimine_addition.network.packet.MinerCertificatePacket;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_NAME + "/PacketHandler");
    public static final ResourceLocation CERTIFICATE_EFFECT_ID = new ResourceLocation(Constants.MOD_ID, "certificate_effect");
    public static final ResourceLocation MINER_CERTIFICATE_ID = new ResourceLocation(Constants.MOD_ID, "miner_certificate");

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(IDHolder(CERTIFICATE_EFFECT_ID, "S2C"), CertificateEffectPacket::receive);
    }
    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(IDHolder(MINER_CERTIFICATE_ID, "C2S"), MinerCertificatePacket::receive);
    }

    private static ResourceLocation IDHolder(ResourceLocation packetID, String packetType) {
        switch (packetType) {
            case "C2S" -> LOGGER.info("Registering C2S receiver with id: {}", packetID.toString());
            case "S2C" -> LOGGER.info("Registering S2C receiver with id: {}", packetID.toString());
        }
        return packetID;
    }
}