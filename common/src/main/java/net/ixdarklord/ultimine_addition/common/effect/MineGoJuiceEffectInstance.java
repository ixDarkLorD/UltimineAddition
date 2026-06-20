package net.ixdarklord.ultimine_addition.common.effect;

import dev.architectury.registry.registries.RegistrySupplier;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MineGoJuiceEffectInstance extends MobEffectInstance {
    public MineGoJuiceEffectInstance(RegistrySupplier<MobEffect> effect, int amplifier) {
        super(Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.get(effect.getId())), ConfigHandler.SERVER.CARD_POTION_DURATIONS.getDefaultValue(MiningSkillCardItem.Tier.fromInt(amplifier + 1)) * 20, amplifier);
    }

    public void applyEffect(@NotNull LivingEntity entity) {
        this.duration = ConfigHandler.SERVER.CARD_POTION_DURATIONS.getValue(MiningSkillCardItem.Tier.fromInt(this.getAmplifier() + 1)) * 20;
        super.applyEffect(entity);
    }

    public int getDuration() {
        return ConfigHandler.SERVER.CARD_POTION_DURATIONS.getValue(MiningSkillCardItem.Tier.fromInt(this.getAmplifier() + 1)) * 20;
    }
}
