package net.ixdarklord.ultimine_addition.common.progression;

import io.netty.buffer.ByteBuf;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.UltimineNoticePayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// A short message for the notice HUD above the hotbar.
public record UltimineNotice(Kind kind, Component title, List<Component> lines, ItemStack icon) {
    public static final StreamCodec<FriendlyByteBuf, UltimineNotice> STREAM_CODEC = StreamCodec.composite(
            Kind.STREAM_CODEC, UltimineNotice::kind,
            ByteBufCodecs.COMPONENT, UltimineNotice::title,
            ByteBufCodecs.COMPONENT.apply(ByteBufCodecs.list()), UltimineNotice::lines,
            ByteBufCodecs.OPTIONAL_ITEM_STACK, UltimineNotice::icon,
            UltimineNotice::new);

    public static Component actionsTitle() {
        return Component.translatable("info.ultimine_addition.notice.actions");
    }

    public void send(ServerPlayer player) {
        PayloadHandler.sendToPlayer(new UltimineNoticePayload(this), player);
    }

    public enum Kind {
        // Ultimine itself: locked, undone, undo refused. Titled "Ultimine Actions"; the status is the first line.
        ACTION(0xFF8FC8FF, 2600),
        REWARD(0xFFF2C14E, 4500),
        STREAK(0xFFFF9A3C, 1600),
        LUCKY(0xFF62D96B, 2200);

        public static final StreamCodec<ByteBuf, Kind> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(i -> Kind.values()[i], Kind::ordinal);

        private final int accent;
        private final long duration;

        Kind(int accent, long duration) {
            this.accent = accent;
            this.duration = duration;
        }

        public int accent() {
            return this.accent;
        }

        // Milliseconds on screen, not counting the fade.
        public long duration() {
            return this.duration;
        }
    }
}
