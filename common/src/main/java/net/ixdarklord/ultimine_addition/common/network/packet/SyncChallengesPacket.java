package net.ixdarklord.ultimine_addition.common.network.packet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class SyncChallengesPacket extends BaseS2CMessage {
    private final Map<ResourceLocation, ChallengesData> dataMap;

    public SyncChallengesPacket(FriendlyByteBuf buffer) {
        this(buffer.readMap(FriendlyByteBuf::readResourceLocation, ChallengesData::readBuffer));
    }
    public SyncChallengesPacket(Map<ResourceLocation, ChallengesData> dataMap) {
        this.dataMap = dataMap;
    }

    @Override
    public MessageType getType() {
        return PacketHandler.SYNC_CHALLENGES;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeMap(dataMap, FriendlyByteBuf::writeResourceLocation, ChallengesData::writeBuffer);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> ChallengesManager.INSTANCE.setChallenges(dataMap));
    }
}
