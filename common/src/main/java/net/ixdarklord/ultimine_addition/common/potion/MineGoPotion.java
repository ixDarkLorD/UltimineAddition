package net.ixdarklord.ultimine_addition.common.potion;

import net.ixdarklord.coolcatlib.api.item.ComponentItem;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import org.jetbrains.annotations.NotNull;

public class MineGoPotion extends Potion {
    @NotNull private final MiningSkillCardItem.Tier tier;
    private final ComponentItem.ComponentType componentType = ComponentItem.ComponentType.ABILITY;
    public MineGoPotion(MiningSkillCardItem.@NotNull Tier tier, MobEffectInstance... mobEffectInstances) {
        super(mobEffectInstances);
        this.tier = tier;
    }

    public ComponentItem.ComponentType getComponentType() {
        return componentType;
    }

    public MiningSkillCardItem.@NotNull Tier getTier() {
        return tier;
    }
}
