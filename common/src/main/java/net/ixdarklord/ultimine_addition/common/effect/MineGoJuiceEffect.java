package net.ixdarklord.ultimine_addition.common.effect;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.Holder;
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
        return type;
    }

    public static ResourceLocation getId(MiningSkillCardItem.Type type) {
        return FTBUltimineAddition.rl("mine_go_juice_%s".formatted(type.getId()));
    }

    public static void giveEffect(ServerPlayer player, MiningSkillCardItem.Type type) {
        Holder<MobEffect> effect = Registration.MOB_EFFECTS.getRegistrar().getHolder(MineGoJuiceEffect.getId(type));
        if (effect == null) return;

        MobEffectInstance instance = new MobEffectInstance(effect, 20, 2, false, false, false);
        if (player.getActiveEffectsMap().keySet()
                .stream()
                .filter(mobEffect -> mobEffect instanceof MineGoJuiceEffect juiceEffect && juiceEffect.getType() == type)
                .toList()
                .isEmpty()) {
            player.addEffect(instance);
        }
    }
}
