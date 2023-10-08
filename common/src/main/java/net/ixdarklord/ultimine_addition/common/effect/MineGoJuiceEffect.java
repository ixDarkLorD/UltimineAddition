package net.ixdarklord.ultimine_addition.common.effect;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class MineGoJuiceEffect extends MobEffect {
    private final MiningSkillCardItem.Type type;
    protected MineGoJuiceEffect(MiningSkillCardItem.Type type, MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        this.type = type;
    }

    public MiningSkillCardItem.Type getType() {
        return type;
    }
}
