package net.ixdarklord.ultimine_addition.network;

import net.ixdarklord.coolcatcore.api.network.Network;
import net.ixdarklord.coolcatcore.api.network.PayloadReceiver;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
        registerC2S(SkillsRecordPayload.RerollChallenge.TYPE, SkillsRecordPayload.RerollChallenge.STREAM_CODEC, SkillsRecordPayload.RerollChallenge::handle);
        registerC2S(SkillsRecordPayload.ClaimCertificate.TYPE, SkillsRecordPayload.ClaimCertificate.STREAM_CODEC, SkillsRecordPayload.ClaimCertificate::handle);
        registerC2S(SkillsRecordPayload.RequestRecord.TYPE, SkillsRecordPayload.RequestRecord.STREAM_CODEC, SkillsRecordPayload.RequestRecord::handle);
        registerC2S(SkillsRecordPayload.RequestCard.TYPE, SkillsRecordPayload.RequestCard.STREAM_CODEC, SkillsRecordPayload.RequestCard::handle);
        registerC2S(UndoPayload.Request.TYPE, UndoPayload.Request.STREAM_CODEC, UndoPayload.Request::handle);
        registerC2S(UndoPayload.HistoryRequest.TYPE, UndoPayload.HistoryRequest.STREAM_CODEC, UndoPayload.HistoryRequest::handle);
        registerC2S(UpdateItemShapePayload.TYPE, UpdateItemShapePayload.STREAM_CODEC, UpdateItemShapePayload::handle);
        registerS2C(SkillsRecordPayload.SyncRecord.TYPE, SkillsRecordPayload.SyncRecord.STREAM_CODEC, SkillsRecordPayload.SyncRecord::handle);
        registerS2C(SkillsRecordPayload.SyncCards.TYPE, SkillsRecordPayload.SyncCards.STREAM_CODEC, SkillsRecordPayload.SyncCards::handle);
        registerS2C(MinerCertificatePayload.TYPE, MinerCertificatePayload.STREAM_CODEC, MinerCertificatePayload::handle);
        registerS2C(MiningSkillCardPayload.SyncBrewing.TYPE, MiningSkillCardPayload.SyncBrewing.STREAM_CODEC, MiningSkillCardPayload.SyncBrewing::handle);
        registerS2C(SyncChallengesPayload.TYPE, SyncChallengesPayload.STREAM_CODEC, SyncChallengesPayload::handle);
        registerS2C(UndoPayload.Preview.TYPE, UndoPayload.Preview.STREAM_CODEC, UndoPayload.Preview::handle);
        registerS2C(UndoPayload.Close.TYPE, UndoPayload.Close.STREAM_CODEC, UndoPayload.Close::handle);
        registerS2C(UndoPayload.History.TYPE, UndoPayload.History.STREAM_CODEC, UndoPayload.History::handle);
        registerS2C(UndoPayload.Progress.TYPE, UndoPayload.Progress.STREAM_CODEC, UndoPayload.Progress::handle);
        registerS2C(UndoPayload.Grow.TYPE, UndoPayload.Grow.STREAM_CODEC, UndoPayload.Grow::handle);
        registerS2C(UltimineNoticePayload.TYPE, UltimineNoticePayload.STREAM_CODEC, UltimineNoticePayload::handle);
        registerS2C(PlayConsumeEffectPayload.TYPE, PlayConsumeEffectPayload.STREAM_CODEC, PlayConsumeEffectPayload::handle);
        // Codec derived from the record by CoolCatLib.
        Network.registerClientbound(OpenConfigPayload.class, OpenConfigPayload::handle);
        Network.registerServerbound(ShapeDiagramPayload.Request.class, ShapeDiagramPayload.Request::handle);
        Network.registerClientbound(ShapeDiagramPayload.Diagrams.class, ShapeDiagramPayload.Diagrams::handle);
        Network.registerClientbound(SyncShapesPayload.class, SyncShapesPayload::handle);
        Network.registerClientbound(SyncCardTypesPayload.class, SyncCardTypesPayload::handle);
    }

    private static <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, PayloadReceiver<T> handler) {
        Network.registerServerbound(type, codec, handler);
    }

    private static <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, PayloadReceiver<T> handler) {
        Network.registerClientbound(type, codec, handler);
    }

    public static <T extends CustomPacketPayload> void sendToServer(T payload) {
        Network.sendToServer(payload);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T payload, ServerPlayer player) {
        Network.sendToPlayer(player, payload);
    }

    public static <T extends CustomPacketPayload> void sendToPlayers(T payload, Iterable<ServerPlayer> players) {
        Network.sendToPlayers(players, payload);
    }

    public static <T extends CustomPacketPayload> void sendToTarget(T payload, ServerLevel level, BlockPos pos, int range) {
        sendToPlayers(payload, level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(range > 0 ? range : 64)));
    }

    public static <T extends CustomPacketPayload> void sendToLevel(T payload, ServerLevel level) {
        sendToPlayers(payload, level.players());
    }
}
