package net.ixdarklord.ultimine_addition.common.network;

import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import net.ixdarklord.ultimine_addition.common.network.packet.*;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

public class PacketHandler {
    public static final SimpleNetworkManager MANAGER = SimpleNetworkManager.create(UltimineAddition.MOD_ID);
    public static final MessageType OPEN_SKILLS_RECORD = MANAGER.registerC2S("open_skills_record", SkillsRecordPacket.Open::new);
    public static final MessageType TOGGLE_SKILLS_RECORD = MANAGER.registerC2S("toggle_skills_record", SkillsRecordPacket.Toggle::new);
    public static final MessageType SYNC_SKILLS_RECORD = MANAGER.registerS2C("sync_skills_record", SkillsRecordPacket::new);
    public static final MessageType SYNC_CHALLENGES = MANAGER.registerS2C("sync_challenges", SyncChallengesPacket::new);
    public static final MessageType SYNC_PLAYER_ABILITY = MANAGER.registerS2C("sync_player_ability", PlayerAbilityPacket::new);
    public static final MessageType SYNC_ITEM_STORAGE_DATA = MANAGER.registerS2C("sync_item_storage_data", ItemStorageDataPacket::new);
    public static final MessageType SYNC_MINING_SKILL_CARD = MANAGER.registerS2C("sync_mining_skill_card", MiningSkillCardPacket::new);
    public static final MessageType SYNC_MINER_CERTIFICATE = MANAGER.registerS2C("sync_miner_certificate", MinerCertificatePacket::new);

    public static void register() {}

    public static <MSG extends BaseC2SMessage> void sendToServer(MSG message) {
        message.sendToServer();
    }

    public static <MSG extends BaseS2CMessage> void sendToPlayer(MSG message, ServerPlayer player) {
        message.sendTo(player);
    }

    public static <MSG extends BaseS2CMessage> void sendToTarget(MSG message, ServerLevel level, BlockPos pos, int range) {
        message.sendTo(level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(range > 0 ? range : 64)));
    }

    public static <MSG extends BaseS2CMessage> void sendToLevel(MSG message, ServerLevel level) {
        message.sendToLevel(level);
    }

    public static <MSG extends BaseS2CMessage> void sendToChunk(MSG message, LevelChunk chunk) {
        message.sendToChunkListeners(chunk);
    }
}
