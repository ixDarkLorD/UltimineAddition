package net.ixdarklord.ultimine_addition.common.network.packet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;

public class MinerCertificatePacket extends BaseS2CMessage {
    private final MinerCertificateData data;

    public MinerCertificatePacket(FriendlyByteBuf buf) {
        this(MinerCertificateData.fromNetwork(buf));
    }
    public MinerCertificatePacket(MinerCertificateData data) {
        this.data = data;
    }

    @Override
    public MessageType getType() {
        return PacketHandler.SYNC_MINER_CERTIFICATE;
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
