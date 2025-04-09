package net.ixdarklord.ultimine_addition.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum PlaystyleMode {
    MODERN,
    ONE_TIER_ONLY,
    LEGACY;

    public static final StreamCodec<ByteBuf, PlaystyleMode> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(PlaystyleMode::valueOf, PlaystyleMode::name);
}
