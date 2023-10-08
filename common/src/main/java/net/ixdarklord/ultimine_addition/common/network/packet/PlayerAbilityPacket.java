package net.ixdarklord.ultimine_addition.common.network.packet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.ixdarklord.ultimine_addition.client.handler.ClientPlayerUltimineHandler;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;

public class PlayerAbilityPacket extends BaseS2CMessage {
    private final boolean state;

    public PlayerAbilityPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }
    public PlayerAbilityPacket(boolean state) {
        this.state = state;
    }

    @Override
    public MessageType getType() {
        return PacketHandler.SYNC_PLAYER_ABILITY;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.state);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> ClientPlayerUltimineHandler.setCapability(state));
    }
}
