package net.ixdarklord.ultimine_addition.common.data.record;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record CardSync(UUID id, ItemStack card, CardProgress progress, CardHistory history) {
    public static final StreamCodec<RegistryFriendlyByteBuf, CardSync> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, CardSync::id,
            ItemStack.OPTIONAL_STREAM_CODEC, CardSync::card,
            CardProgress.STREAM_CODEC, CardSync::progress,
            CardHistory.STREAM_CODEC, CardSync::history,
            CardSync::new);
}
