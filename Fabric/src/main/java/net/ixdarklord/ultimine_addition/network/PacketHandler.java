package net.ixdarklord.ultimine_addition.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.network.packet.CertificateEffectPacket;
import net.ixdarklord.ultimine_addition.network.packet.MinerCertificatePacket;
import net.ixdarklord.ultimine_addition.network.packet.PlayerCapabilityPacket;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_NAME + "/PacketHandler");
    public static final ResourceLocation PLAYER_CAPABILITY_ID = new ResourceLocation(Constants.MOD_ID, "player_capability");
    public static final ResourceLocation MINER_CERTIFICATE_ID = new ResourceLocation(Constants.MOD_ID, "miner_certificate");
    public static final ResourceLocation CERTIFICATE_EFFECT_ID = new ResourceLocation(Constants.MOD_ID, "certificate_effect");
    public static final ResourceLocation PLAYER_CAPABILITY_SYNC_ID = new ResourceLocation(Constants.MOD_ID, "player_capability.data_sync_s2c");
    public static final ResourceLocation MINER_CERTIFICATE_SYNC_ID = new ResourceLocation(Constants.MOD_ID, "miner_certificate.data_sync_s2c");
    public static final ResourceLocation CERTIFICATE_EFFECT_SYNC_ID = new ResourceLocation(Constants.MOD_ID, "certificate_effect.play_to_client");


    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(IDHolder(PLAYER_CAPABILITY_ID, "C2S"), PlayerCapabilityPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(IDHolder(MINER_CERTIFICATE_ID, "C2S"), MinerCertificatePacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(IDHolder(CERTIFICATE_EFFECT_ID, "C2S"), CertificateEffectPacket::receive);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(IDHolder(PLAYER_CAPABILITY_SYNC_ID, "S2C"), PlayerCapabilityPacket.DataSyncS2C::receive);
        ClientPlayNetworking.registerGlobalReceiver(IDHolder(MINER_CERTIFICATE_SYNC_ID, "S2C"), MinerCertificatePacket.DataSyncS2C::receive);
        ClientPlayNetworking.registerGlobalReceiver(IDHolder(CERTIFICATE_EFFECT_SYNC_ID, "S2C"), CertificateEffectPacket.Play2Client::receive);
    }

    private static ResourceLocation IDHolder(ResourceLocation packetID, String packetType) {
        LOGGER.info("Registering {} receiver with id: {}", packetType, packetID.toString());
        return packetID;
    }
}