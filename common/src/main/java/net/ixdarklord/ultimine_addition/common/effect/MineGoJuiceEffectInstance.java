package net.ixdarklord.ultimine_addition.common.effect;

import net.ixdarklord.coolcatcore.api.registry.RegistryEntry;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import static net.ixdarklord.ultimine_addition.config.UAServerConfig.CARD_POTION_DURATIONS;

public class MineGoJuiceEffectInstance extends MobEffectInstance {
    public MineGoJuiceEffectInstance(RegistryEntry<MobEffect> effect, int amplifier) {
        super(effect.get(),
                CARD_POTION_DURATIONS.getDefaultValue(MiningSkillCardItem.Tier.fromInt(amplifier+1)) * 20,
                amplifier
        );
    }

    // 1.20.1 has no onEffectStarted; applyEffect is its closest hook (as in the earlier 1.20.1 releases).
    @Override
    public void applyEffect(LivingEntity entity) {
        this.duration = CARD_POTION_DURATIONS.getValue(MiningSkillCardItem.Tier.fromInt(this.getAmplifier()+1)) * 20;
        super.applyEffect(entity);
    }

    @Override
    public int getDuration() {
        return CARD_POTION_DURATIONS.getValue(MiningSkillCardItem.Tier.fromInt(this.getAmplifier()+1)) * 20;
    }
}
