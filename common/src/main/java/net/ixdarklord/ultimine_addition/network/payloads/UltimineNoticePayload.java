package net.ixdarklord.ultimine_addition.network.payloads;

import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.ultimine_addition.client.gui.hud.UltimineNoticeHud;
import net.ixdarklord.ultimine_addition.common.progression.UltimineNotice;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.ixdarklord.coolcatcore.api.network.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record UltimineNoticePayload(UltimineNotice notice) implements CustomPacketPayload {
    public static final Type<UltimineNoticePayload> TYPE = new Type<>(FTBUltimineAddition.id("ultimine_notice"));
    public static final StreamCodec<FriendlyByteBuf, UltimineNoticePayload> STREAM_CODEC =
            UltimineNotice.STREAM_CODEC.map(UltimineNoticePayload::new, UltimineNoticePayload::notice);

    public static void handle(UltimineNoticePayload message, PacketContext context) {
        context.queue(() -> UltimineNoticeHud.INSTANCE.show(message.notice));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
