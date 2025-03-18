package net.ixdarklord.ultimine_addition.common.network;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.ixdarklord.coolcatlib.api.hooks.ServerLifecycleHooks;
import net.ixdarklord.ultimine_addition.common.network.packet.*;
import net.ixdarklord.ultimine_addition.common.network.packet.config.SyncPlaystyleModeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import java.util.Objects;

public class PacketHandler {
    public static void init() {
        NetworkHelper.registerC2S(SkillsRecordPacket.Open.TYPE, SkillsRecordPacket.Open.STREAM_CODEC, SkillsRecordPacket.Open::handle);
        NetworkHelper.registerC2S(SkillsRecordPacket.SyncData.C2S_TYPE, SkillsRecordPacket.SyncData.STREAM_CODEC, SkillsRecordPacket.SyncData::handle);
        NetworkHelper.registerS2C(SkillsRecordPacket.SyncData.S2C_TYPE, SkillsRecordPacket.SyncData.STREAM_CODEC, SkillsRecordPacket.SyncData::handle);
        NetworkHelper.registerS2C(MinerCertificatePacket.TYPE, MinerCertificatePacket.STREAM_CODEC, MinerCertificatePacket::handle);
        NetworkHelper.registerS2C(MiningSkillCardPacket.TYPE, MiningSkillCardPacket.STREAM_CODEC, MiningSkillCardPacket::handle);
        NetworkHelper.registerS2C(MiningSkillCardPacket.SyncBrewing.TYPE, MiningSkillCardPacket.SyncBrewing.STREAM_CODEC, MiningSkillCardPacket.SyncBrewing::handle);
        NetworkHelper.registerS2C(SyncChallengesPacket.TYPE, SyncChallengesPacket.STREAM_CODEC, SyncChallengesPacket::handle);
        NetworkHelper.registerS2C(PlayerAbilityPacket.TYPE, PlayerAbilityPacket.STREAM_CODEC, PlayerAbilityPacket::handle);

        // Config
        NetworkHelper.registerS2C(SyncPlaystyleModeConfig.TYPE, SyncPlaystyleModeConfig.STREAM_CODEC, SyncPlaystyleModeConfig::handle);
    }

    public static <T extends CustomPacketPayload> void sendToServer(T payload) {
        NetworkManager.sendToServer(payload);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T payload, ServerPlayer player) {
        NetworkManager.sendToPlayer(player, payload);
    }

    public static <T extends CustomPacketPayload> void sendToPlayers(T payload) {
        sendToPlayers(payload, Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(), "server is null!")
                .getPlayerList().getPlayers());
    }

    public static <T extends CustomPacketPayload> void sendToPlayers(T payload, Iterable<ServerPlayer> players) {
        NetworkManager.sendToPlayers(players, payload);
    }

    public static <T extends CustomPacketPayload> void sendToTarget(T payload, ServerLevel level, BlockPos pos, int range) {
        sendToPlayers(payload, level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(range > 0 ? range : 64)));
    }

    public static <T extends CustomPacketPayload> void sendToLevel(T payload, ServerLevel level) {
        sendToPlayers(payload, level.players());
    }
}
