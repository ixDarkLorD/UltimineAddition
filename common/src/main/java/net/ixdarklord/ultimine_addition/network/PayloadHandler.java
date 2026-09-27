package net.ixdarklord.ultimine_addition.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import dev.architectury.utils.Env;
import dev.architectury.platform.Platform;
import dev.architectury.networking.NetworkManager;
import net.ixdarklord.ultimine_addition.network.payloads.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public class PayloadHandler {
    public static void init() {
        registerC2S(SkillsRecordPayload.Open.TYPE, SkillsRecordPayload.Open.STREAM_CODEC, SkillsRecordPayload.Open::handle);
        registerC2S(SkillsRecordPayload.SelectCard.TYPE, SkillsRecordPayload.SelectCard.STREAM_CODEC, SkillsRecordPayload.SelectCard::handle);
        registerC2S(SkillsRecordPayload.ToggleConsumeMode.TYPE, SkillsRecordPayload.ToggleConsumeMode.STREAM_CODEC, SkillsRecordPayload.ToggleConsumeMode::handle);
        registerC2S(SkillsRecordPayload.PinChallenge.TYPE, SkillsRecordPayload.PinChallenge.STREAM_CODEC, SkillsRecordPayload.PinChallenge::handle);
        registerC2S(SkillsRecordPayload.EditChallenge.TYPE, SkillsRecordPayload.EditChallenge.STREAM_CODEC, SkillsRecordPayload.EditChallenge::handle);
        registerC2S(SkillsRecordPayload.RequestRecord.TYPE, SkillsRecordPayload.RequestRecord.STREAM_CODEC, SkillsRecordPayload.RequestRecord::handle);
        registerC2S(SkillsRecordPayload.RequestCard.TYPE, SkillsRecordPayload.RequestCard.STREAM_CODEC, SkillsRecordPayload.RequestCard::handle);
        registerC2S(UpdateItemShapePayload.TYPE, UpdateItemShapePayload.STREAM_CODEC, UpdateItemShapePayload::handle);
        registerS2C(SkillsRecordPayload.SyncRecord.TYPE, SkillsRecordPayload.SyncRecord.STREAM_CODEC, SkillsRecordPayload.SyncRecord::handle);
        registerS2C(SkillsRecordPayload.SyncCards.TYPE, SkillsRecordPayload.SyncCards.STREAM_CODEC, SkillsRecordPayload.SyncCards::handle);
        registerS2C(MinerCertificatePayload.TYPE, MinerCertificatePayload.STREAM_CODEC, MinerCertificatePayload::handle);
        registerS2C(MiningSkillCardPayload.SyncBrewing.TYPE, MiningSkillCardPayload.SyncBrewing.STREAM_CODEC, MiningSkillCardPayload.SyncBrewing::handle);
        registerS2C(SyncChallengesPayload.TYPE, SyncChallengesPayload.STREAM_CODEC, SyncChallengesPayload::handle);
        registerS2C(PlayerAbilityPayload.TYPE, PlayerAbilityPayload.STREAM_CODEC, PlayerAbilityPayload::handle);
        registerS2C(SyncConfigPayload.TYPE, SyncConfigPayload.STREAM_CODEC, SyncConfigPayload::handle);
        registerS2C(PlayConsumeEffectPayload.TYPE, PlayConsumeEffectPayload.STREAM_CODEC, PlayConsumeEffectPayload::handle);
    }

    private static <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, NetworkManager.NetworkReceiver<T> handler) {
        NetworkManager.registerReceiver(NetworkManager.c2s(), type, codec, handler);
    }

    private static <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, NetworkManager.NetworkReceiver<T> handler) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            NetworkManager.registerReceiver(NetworkManager.s2c(), type, codec, handler);
        } else {
            NetworkManager.registerS2CPayloadType(type, codec);
        }
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
