package net.ixdarklord.ultimine_addition.common.effect;

import dev.architectury.registry.registries.RegistrySupplier;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import static net.ixdarklord.ultimine_addition.config.ConfigHandler.SERVER.CARD_POTION_DURATIONS;

public class MineGoJuiceEffectInstance extends MobEffectInstance {
    public MineGoJuiceEffectInstance(RegistrySupplier<MobEffect> effect, int amplifier) {
        super(BuiltInRegistries.MOB_EFFECT.getHolder(effect.getId()).orElseThrow(),
                CARD_POTION_DURATIONS.getDefaultValue(MiningSkillCardItem.Tier.fromInt(amplifier+1)) * 20,
                amplifier
        );
    }

    @Override
    public void onEffectStarted(LivingEntity entity) {
        this.duration = CARD_POTION_DURATIONS.getValue(MiningSkillCardItem.Tier.fromInt(this.getAmplifier()+1)) * 20;
        super.onEffectStarted(entity);
    }

    @Override
    public int getDuration() {
        return CARD_POTION_DURATIONS.getValue(MiningSkillCardItem.Tier.fromInt(this.getAmplifier()+1)) * 20;
    }
}
