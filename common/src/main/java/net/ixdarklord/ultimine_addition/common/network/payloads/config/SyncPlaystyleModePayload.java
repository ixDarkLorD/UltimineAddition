package net.ixdarklord.ultimine_addition.common.network.payloads.config;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record SyncPlaystyleModePayload(PlaystyleMode mode) implements CustomPacketPayload {
    public static final Type<SyncPlaystyleModePayload> TYPE = new Type<>(FTBUltimineAddition.getLocation("sync_playstyle_mode_config"));
    public static final StreamCodec<FriendlyByteBuf, SyncPlaystyleModePayload> STREAM_CODEC = StreamCodec.composite(
            NetworkHelper.enumStreamCodec(PlaystyleMode.class), SyncPlaystyleModePayload::mode,
            SyncPlaystyleModePayload::new
    );

    public static void handle(SyncPlaystyleModePayload message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ConfigHandler.COMMON.PLAYSTYLE_MODE.set(message.mode);
            FTBUltimineAddition.LOGGER.debug("[Config] Sync Playstyle Mode to {}!", message.mode.name().toLowerCase());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
