package net.ixdarklord.ultimine_addition.common.network.packet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;

public class MiningSkillCardPacket extends BaseS2CMessage {
    private final MiningSkillCardData data;

    public MiningSkillCardPacket(FriendlyByteBuf buf) {
        this(MiningSkillCardData.fromNetwork(buf));
    }

    public MiningSkillCardPacket(MiningSkillCardData data) {
        this.data = data;
    }

    @Override
    public MessageType getType() {
        return PacketHandler.SYNC_MINING_SKILL_CARD;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        this.data.toNetwork(buf);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> data.saveData(data.get()));
    }
}
