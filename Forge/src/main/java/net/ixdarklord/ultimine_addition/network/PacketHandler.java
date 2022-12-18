package net.ixdarklord.ultimine_addition.network;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.network.packet.CertificateEffectPacket;
import net.ixdarklord.ultimine_addition.network.packet.MinerCertificatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_NAME + "/PacketHandler");
    private static final String PROTOCOL_VERSION = "1.0";
    private static int PACKET_ID = 0;
    private static int id() {
        return PACKET_ID++;
    }
    public static final SimpleChannel MOD_CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Constants.MOD_ID, "packet_handler"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();

    public static void register() {
        MOD_CHANNEL.messageBuilder(CertificateEffectPacket.class,
                IDHolder(id(), "certificate_effect", "S2C"),
                NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CertificateEffectPacket::new)
                .encoder(CertificateEffectPacket::encode)
                .consumerMainThread(CertificateEffectPacket::handle)
                .add();

        MOD_CHANNEL.messageBuilder(MinerCertificatePacket.class,
                IDHolder(id(), "miner_certificate", "C2S"),
                NetworkDirection.PLAY_TO_SERVER)
                .decoder(MinerCertificatePacket::new)
                .encoder(MinerCertificatePacket::encode)
                .consumerMainThread(MinerCertificatePacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        MOD_CHANNEL.sendToServer(message);
    }
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        MOD_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
    public static <MSG> void sendToAllTracking(MSG message, LivingEntity entity) {
        MOD_CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }

    private static int IDHolder(int packetID, String packetName, String packetType) {
        switch (packetType) {
            case "C2S" -> LOGGER.info("Registering C2S receiver with id: {}", Constants.MOD_ID+":"+packetName);
            case "S2C" -> LOGGER.info("Registering S2C receiver with id: {}", Constants.MOD_ID+":"+packetName);
        }
        return packetID;
    }
}
