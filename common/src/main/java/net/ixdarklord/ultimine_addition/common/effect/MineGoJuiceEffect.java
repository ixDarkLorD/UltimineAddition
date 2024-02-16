package net.ixdarklord.ultimine_addition.common.effect;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class MineGoJuiceEffect extends MobEffect {
    private final MiningSkillCardItem.Type type;
    public MineGoJuiceEffect(MiningSkillCardItem.Type type, MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        this.type = type;
    }

    public MiningSkillCardItem.Type getType() {
        return type;
    }

    public static ResourceLocation getId(MiningSkillCardItem.Type type) {
        return UltimineAddition.getLocation("mine_go_juice_%s".formatted(type.getId()));
    }
}
