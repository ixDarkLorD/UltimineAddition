package net.ixdarklord.ultimine_addition.network.packets;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public final class SyncChallengesPacket extends BaseS2CMessage {
    private final Map<ResourceLocation, ChallengeData> dataMap;

    public SyncChallengesPacket(FriendlyByteBuf buffer) {
        this(buffer.readMap(FriendlyByteBuf::readResourceLocation, ChallengeData::readBuffer));
    }

    public SyncChallengesPacket(Map<ResourceLocation, ChallengeData> dataMap) {
        this.dataMap = dataMap;
    }

    public MessageType getType() {
        return PacketHandler.SYNC_CHALLENGES;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeMap(this.dataMap, FriendlyByteBuf::writeResourceLocation, ChallengeData::writeBuffer);
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> ChallengesManager.INSTANCE.setChallenges(this.dataMap));
    }
}
