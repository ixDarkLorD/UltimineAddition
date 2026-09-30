package net.ixdarklord.ultimine_addition.common.data.record;

import net.ixdarklord.ultimine_addition.common.data.item.ItemComponentType;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.minecraft.core.UUIDUtil;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record SkillsRecordLink(Optional<UUID> id, Optional<Legacy> legacy) {
    public record Legacy(List<ItemStack> contents, int selectedCard, boolean consumeMode) {}

    public static final Codec<SkillsRecordLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("UUID").forGetter(SkillsRecordLink::id),
            ItemStack.CODEC.listOf().optionalFieldOf("Contents").forGetter((SkillsRecordLink link) -> link.legacy.map(Legacy::contents)),
            Codec.INT.optionalFieldOf("SelectedCard", -1).forGetter((SkillsRecordLink link) -> link.legacy.map(Legacy::selectedCard).orElse(-1)),
            Codec.BOOL.optionalFieldOf("ConsumeMode", false).forGetter((SkillsRecordLink link) -> link.legacy.map(Legacy::consumeMode).orElse(false))
    ).apply(instance, (id, contents, selected, consume) ->
            new SkillsRecordLink(id, contents.map(list -> new Legacy(list, selected, consume)))));

    public static final StreamCodec<ByteBuf, SkillsRecordLink> STREAM_CODEC =
            ByteBufCodecs.UUID.map(SkillsRecordLink::of, link -> link.id().orElse(Util.NIL_UUID));

    public static final ItemComponentType<SkillsRecordLink> DATA_COMPONENT = new ItemComponentType<>(FTBUltimineAddition.id("skills_record_data"), CODEC);

    public static SkillsRecordLink of(UUID id) {
        return new SkillsRecordLink(Optional.of(id), Optional.empty());
    }

    public static @Nullable UUID getId(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof SkillsRecordItem)) return null;
        SkillsRecordLink link = DATA_COMPONENT.get(stack);
        return link == null ? null : link.id().filter(id -> !id.equals(Util.NIL_UUID)).orElse(null);
    }

    public static boolean isLinked(ItemStack stack) {
        return getId(stack) != null;
    }
}
