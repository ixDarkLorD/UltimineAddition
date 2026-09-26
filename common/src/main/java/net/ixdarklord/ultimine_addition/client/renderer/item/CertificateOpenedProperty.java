package net.ixdarklord.ultimine_addition.client.renderer.item;

import com.mojang.serialization.MapCodec;
import net.ixdarklord.ultimine_addition.common.item.MinerCertificateItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Item model condition: true once a Miner Certificate has been accomplished (replaces the "opened" item property). */
public record CertificateOpenedProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<CertificateOpenedProperty> MAP_CODEC = MapCodec.unit(new CertificateOpenedProperty());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        return MinerCertificateItem.isAccomplished(itemStack);
    }

    @Override
    public MapCodec<CertificateOpenedProperty> type() {
        return MAP_CODEC;
    }
}
