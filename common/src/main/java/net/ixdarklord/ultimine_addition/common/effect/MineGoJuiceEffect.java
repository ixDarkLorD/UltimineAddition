package net.ixdarklord.ultimine_addition.common.effect;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

public class MineGoJuiceEffect extends MobEffect {
    private final MiningSkillCardItem.Type type;

    public MineGoJuiceEffect(MiningSkillCardItem.Type type, MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        this.type = type;
    }

    public MiningSkillCardItem.Type getType() {
        return this.type;
    }

    public static ResourceLocation getId(MiningSkillCardItem.Type type) {
        return FTBUltimineAddition.id("mine_go_juice_%s".formatted(type.id()));
    }

    public static void giveEffect(ServerPlayer player, MiningSkillCardItem.Type type) {
        MobEffect effect = Registration.MOB_EFFECTS.getRegistrar().get(getId(type));
        if (effect != null) {
            MobEffectInstance instance = new MobEffectInstance(effect, 20, 2, false, false, false);
            if (player.getActiveEffectsMap().keySet().stream().filter((mobEffect) -> {
                if (mobEffect instanceof MineGoJuiceEffect juiceEffect) {
                    return juiceEffect.getType() == type;
                }

                return false;
            }).toList().isEmpty()) {
                player.addEffect(instance);
            }

        }
    }
}
