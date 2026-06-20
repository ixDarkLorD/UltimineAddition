package net.ixdarklord.ultimine_addition.network.packets;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.ixdarklord.ultimine_addition.client.handler.ClientHandler;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

public final class PlayConsumeEffectPacket extends BaseS2CMessage {
    private final BlockPos pos;
    private final BlockState state;

    public PlayConsumeEffectPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readJsonWithCodec(BlockState.CODEC));
    }

    public PlayConsumeEffectPacket(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    public MessageType getType() {
        return PacketHandler.PLAY_CONSUME_EFFECT;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeJsonWithCodec(BlockState.CODEC, this.state);
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> ClientHandler.playConsumeModeEffect(this.pos, this.state));
    }
}
