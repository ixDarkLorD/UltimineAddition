package net.ixdarklord.ultimine_addition.common.data.record;

import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record CardSync(UUID id, ItemStack card, CardProgress progress, CardHistory history) {
    public static final StreamCodec<FriendlyByteBuf, CardSync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID, CardSync::id,
            ByteBufCodecs.OPTIONAL_ITEM_STACK, CardSync::card,
            CardProgress.STREAM_CODEC, CardSync::progress,
            CardHistory.STREAM_CODEC, CardSync::history,
            CardSync::new);
}
