package net.ixdarklord.ultimine_addition.common.data.record;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record SkillsRecordLink(Optional<UUID> id, Optional<Legacy> legacy) {
    public record Legacy(List<ItemStack> contents, int selectedCard, boolean consumeMode) {}

    public static final Codec<SkillsRecordLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("UUID").forGetter(SkillsRecordLink::id),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("Contents").forGetter(link -> link.legacy.map(Legacy::contents)),
            Codec.INT.optionalFieldOf("SelectedCard", -1).forGetter(link -> link.legacy.map(Legacy::selectedCard).orElse(-1)),
            Codec.BOOL.optionalFieldOf("ConsumeMode", false).forGetter(link -> link.legacy.map(Legacy::consumeMode).orElse(false))
    ).apply(instance, (id, contents, selected, consume) ->
            new SkillsRecordLink(id, contents.map(list -> new Legacy(list, selected, consume)))));

    public static final StreamCodec<ByteBuf, SkillsRecordLink> STREAM_CODEC =
            UUIDUtil.STREAM_CODEC.map(SkillsRecordLink::of, link -> link.id().orElse(Util.NIL_UUID));

    public static final DataComponentType<SkillsRecordLink> DATA_COMPONENT =
            DataComponentType.<SkillsRecordLink>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).build();

    public static SkillsRecordLink of(UUID id) {
        return new SkillsRecordLink(Optional.of(id), Optional.empty());
    }

    public static @Nullable UUID getId(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof SkillsRecordItem)) return null;
        SkillsRecordLink link = stack.get(DATA_COMPONENT);
        return link == null ? null : link.id().filter(id -> !id.equals(Util.NIL_UUID)).orElse(null);
    }

    public static boolean isLinked(ItemStack stack) {
        return getId(stack) != null;
    }
}
