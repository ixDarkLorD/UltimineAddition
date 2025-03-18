package net.ixdarklord.ultimine_addition.common.network.packet;

import dev.architectury.networking.NetworkManager;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record PlayerAbilityPacket(boolean state) implements CustomPacketPayload {
    public static final Type<PlayerAbilityPacket> TYPE = new Type<>(UltimineAddition.getLocation("sync_player_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerAbilityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PlayerAbilityPacket::state,
            PlayerAbilityPacket::new
    );

    public static void handle(PlayerAbilityPacket message, NetworkManager.PacketContext context) {
        context.queue(() -> ServicePlatform.get().players().setPlayerUltimineCapability(context.getPlayer(), message.state));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
