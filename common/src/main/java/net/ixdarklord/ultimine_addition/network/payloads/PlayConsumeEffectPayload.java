package net.ixdarklord.ultimine_addition.network.payloads;

import dev.architectury.networking.NetworkManager;
import net.ixdarklord.ultimine_addition.client.handler.ClientHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public record PlayConsumeEffectPayload(BlockPos pos, BlockState state) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayConsumeEffectPayload> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("play_consume_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayConsumeEffectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistries(BlockPos.CODEC), PlayConsumeEffectPayload::pos,
            ByteBufCodecs.fromCodecWithRegistries(BlockState.CODEC), PlayConsumeEffectPayload::state,
            PlayConsumeEffectPayload::new);

    public static void handle(PlayConsumeEffectPayload message, NetworkManager.PacketContext context) {
        context.queue(() -> ClientHandler.playConsumeModeEffect(message.pos, message.state));
    }

    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
