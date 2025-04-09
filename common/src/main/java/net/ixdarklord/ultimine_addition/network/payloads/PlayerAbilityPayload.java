package net.ixdarklord.ultimine_addition.network.payloads;

import dev.architectury.networking.NetworkManager;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record PlayerAbilityPayload(boolean state) implements CustomPacketPayload {
    public static final Type<PlayerAbilityPayload> TYPE = new Type<>(FTBUltimineAddition.rl("sync_player_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerAbilityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PlayerAbilityPayload::state,
            PlayerAbilityPayload::new
    );

    public static void handle(PlayerAbilityPayload message, NetworkManager.PacketContext context) {
        context.queue(() -> ServicePlatform.get().players().setPlayerUltimineCapability(context.getPlayer(), message.state));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
