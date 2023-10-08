package net.ixdarklord.ultimine_addition.common.potion;

import net.ixdarklord.coolcat_lib.common.item.ComponentItem;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.resources.ResourceLocation;
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

    @Override
    public @NotNull String getName(@NotNull String prefix) {
        ResourceLocation id = Registration.POTIONS.getRegistrar().getId(this);
        assert id != null;
        String modifiedString = id.getPath().replaceAll("_\\d+$", "");
        return prefix + modifiedString;
    }
}
