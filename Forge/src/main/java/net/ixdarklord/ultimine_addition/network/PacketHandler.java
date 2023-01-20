package net.ixdarklord.ultimine_addition.network;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.network.packet.CelebrateActionPacket;
import net.ixdarklord.ultimine_addition.network.packet.MinerCertificatePacket;
import net.ixdarklord.ultimine_addition.network.packet.PlayerCapabilityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
        MOD_CHANNEL.messageBuilder(CelebrateActionPacket.class,
                        IDHolder(id(), "celebrate_action", "C2S"),
                        NetworkDirection.PLAY_TO_SERVER)
                .decoder(CelebrateActionPacket::new)
                .encoder(CelebrateActionPacket::encode)
                .consumer(CelebrateActionPacket::handle)
                .add();
        MOD_CHANNEL.messageBuilder(CelebrateActionPacket.Play2Client.class,
                        IDHolder(id(), "celebrate_action.play_to_client", "S2C"),
                        NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CelebrateActionPacket.Play2Client::new)
                .encoder(CelebrateActionPacket.Play2Client::encode)
                .consumer(CelebrateActionPacket.Play2Client::handle)
                .add();

        MOD_CHANNEL.messageBuilder(PlayerCapabilityPacket.class,
                        IDHolder(id(), "player_capability", "C2S"),
                        NetworkDirection.PLAY_TO_SERVER)
                .decoder(PlayerCapabilityPacket::new)
                .encoder(PlayerCapabilityPacket::encode)
                .consumer(PlayerCapabilityPacket::handle)
                .add();
        MOD_CHANNEL.messageBuilder(PlayerCapabilityPacket.DataSyncS2C.class,
                        IDHolder(id(), "player_capability.data_sync_s2c", "S2C"),
                        NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PlayerCapabilityPacket.DataSyncS2C::new)
                .encoder(PlayerCapabilityPacket.DataSyncS2C::encode)
                .consumer(PlayerCapabilityPacket.DataSyncS2C::handle)
                .add();

        MOD_CHANNEL.messageBuilder(MinerCertificatePacket.DataSyncS2C.class,
                        IDHolder(id(), "miner_certificate.data_sync_s2c", "S2C"),
                        NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MinerCertificatePacket.DataSyncS2C::new)
                .encoder(MinerCertificatePacket.DataSyncS2C::encode)
                .consumer(MinerCertificatePacket.DataSyncS2C::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        MOD_CHANNEL.sendToServer(message);
    }
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        MOD_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    private static int IDHolder(int packetID, String packetName, String packetType) {
        LOGGER.info("Registering {} receiver with id: {} ({})", packetType, packetID, Constants.MOD_ID+":"+packetName);
        return packetID;
    }
}
