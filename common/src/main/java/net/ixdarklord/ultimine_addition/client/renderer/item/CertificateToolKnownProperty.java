package net.ixdarklord.ultimine_addition.client.renderer.item;

import com.mojang.serialization.MapCodec;
import net.ixdarklord.ultimine_addition.common.data.item.ShapeCertificateData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// Whether a Shape Certificate's tool is a card type the game knows. A certificate without one (its data pack is gone,
// or it never had a tool) shows a question mark where the tool goes.
public record CertificateToolKnownProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<CertificateToolKnownProperty> MAP_CODEC = MapCodec.unit(new CertificateToolKnownProperty());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        String tool = ShapeCertificateData.getTool(itemStack);
        return tool != null && MiningSkillCardItem.Type.byId(tool) != null;
    }

    @Override
    public MapCodec<CertificateToolKnownProperty> type() {
        return MAP_CODEC;
    }
}
