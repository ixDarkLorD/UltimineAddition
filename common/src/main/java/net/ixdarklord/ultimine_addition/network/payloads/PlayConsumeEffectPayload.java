package net.ixdarklord.ultimine_addition.network.payloads;

import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.ultimine_addition.client.particle.ConsumeEffect;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

// targetId: the entity (player) completing the challenge; the particles are pulled into it.
public record PlayConsumeEffectPayload(BlockPos pos, BlockState state, int targetId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayConsumeEffectPayload> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("play_consume_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayConsumeEffectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistries(BlockPos.CODEC), PlayConsumeEffectPayload::pos,
            ByteBufCodecs.fromCodecWithRegistries(BlockState.CODEC), PlayConsumeEffectPayload::state,
            ByteBufCodecs.VAR_INT, PlayConsumeEffectPayload::targetId,
            PlayConsumeEffectPayload::new);

    public static void handle(PlayConsumeEffectPayload message, PacketContext context) {
        context.queue(() -> ConsumeEffect.play(message.pos, message.state, message.targetId));
    }

    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
