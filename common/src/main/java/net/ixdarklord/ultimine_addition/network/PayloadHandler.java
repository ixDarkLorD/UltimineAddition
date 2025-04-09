package net.ixdarklord.ultimine_addition.network;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.ixdarklord.ultimine_addition.network.payloads.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public class PayloadHandler {
    public static void init() {
        NetworkHelper.registerC2S(SkillsRecordPayload.Open.TYPE, SkillsRecordPayload.Open.STREAM_CODEC, SkillsRecordPayload.Open::handle);
        NetworkHelper.registerC2S(SkillsRecordPayload.SyncData.C2S_TYPE, SkillsRecordPayload.SyncData.STREAM_CODEC, SkillsRecordPayload.SyncData::handle);
        NetworkHelper.registerC2S(UpdateItemShapePayload.TYPE, UpdateItemShapePayload.STREAM_CODEC, UpdateItemShapePayload::handle);
        NetworkHelper.registerS2C(SkillsRecordPayload.SyncData.S2C_TYPE, SkillsRecordPayload.SyncData.STREAM_CODEC, SkillsRecordPayload.SyncData::handle);
        NetworkHelper.registerS2C(MinerCertificatePayload.TYPE, MinerCertificatePayload.STREAM_CODEC, MinerCertificatePayload::handle);
        NetworkHelper.registerS2C(MiningSkillCardPayload.TYPE, MiningSkillCardPayload.STREAM_CODEC, MiningSkillCardPayload::handle);
        NetworkHelper.registerS2C(MiningSkillCardPayload.SyncBrewing.TYPE, MiningSkillCardPayload.SyncBrewing.STREAM_CODEC, MiningSkillCardPayload.SyncBrewing::handle);
        NetworkHelper.registerS2C(SyncChallengesPayload.TYPE, SyncChallengesPayload.STREAM_CODEC, SyncChallengesPayload::handle);
        NetworkHelper.registerS2C(PlayerAbilityPayload.TYPE, PlayerAbilityPayload.STREAM_CODEC, PlayerAbilityPayload::handle);
        NetworkHelper.registerS2C(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC, ConfigSyncPayload::handle);
    }

    public static <T extends CustomPacketPayload> void sendToServer(T payload) {
        NetworkManager.sendToServer(payload);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T payload, ServerPlayer player) {
        NetworkManager.sendToPlayer(player, payload);
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
