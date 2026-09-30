package net.ixdarklord.ultimine_addition.common.potion;

import net.minecraft.core.registries.BuiltInRegistries;
import net.ixdarklord.coolcatcore.api.item.ComponentItem;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import org.jetbrains.annotations.NotNull;

public class MineGoPotion extends Potion {
    @NotNull private final MiningSkillCardItem.Tier tier;
    private final ComponentItem.ComponentType componentType = ComponentItem.ComponentType.ABILITY;
    public MineGoPotion(MiningSkillCardItem.@NotNull Tier tier, MobEffectInstance mobEffectInstances) {
        super(Util.make(() -> {
            ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(mobEffectInstances.getEffect().value());
            Holder<MobEffect> effect = id == null ? null : BuiltInRegistries.MOB_EFFECT.getHolder(id).orElse(null);
            IllegalArgumentException exception = new IllegalArgumentException("Unknown MobEffect: " + id);
            if (effect == null) throw exception;
            ResourceKey<MobEffect> resourceKey = effect.unwrapKey()
                    .orElseThrow(() -> exception);
            return resourceKey.location().getPath();
        }), mobEffectInstances);
        this.tier = tier;
    }

    public ComponentItem.ComponentType getComponentType() {
        return componentType;
    }

    public MiningSkillCardItem.@NotNull Tier getTier() {
        return tier;
    }
}
