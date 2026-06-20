package net.ixdarklord.ultimine_addition.network.packets;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.ixdarklord.ultimine_addition.client.handler.ClientHandler;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;

public final class PlayerAbilityPacket extends BaseS2CMessage {
    private final boolean value;

    public PlayerAbilityPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    public PlayerAbilityPacket(boolean value) {
        this.value = value;
    }

    public MessageType getType() {
        return PacketHandler.SYNC_PLAYER_ABILITY;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.value);
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> ServicePlatform.get().players().setPlayerUltimineCapability(ClientHandler.getPlayer(), this.value));
    }
}
