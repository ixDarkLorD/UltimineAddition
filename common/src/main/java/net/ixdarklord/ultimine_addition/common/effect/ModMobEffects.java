package net.ixdarklord.ultimine_addition.common.effect;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.awt.*;

public class ModMobEffects {
    public static final MobEffect MINE_GO_JUICE_PICKAXE = new MineGoJuiceEffect(MiningSkillCardItem.Type.PICKAXE, MobEffectCategory.BENEFICIAL, new Color(0xF1E7CA).getRGB());
    public static final MobEffect MINE_GO_JUICE_AXE = new MineGoJuiceEffect(MiningSkillCardItem.Type.AXE, MobEffectCategory.BENEFICIAL, new Color(0x966F33).getRGB());
    public static final MobEffect MINE_GO_JUICE_SHOVEL = new MineGoJuiceEffect(MiningSkillCardItem.Type.SHOVEL, MobEffectCategory.BENEFICIAL, new Color(0x7CFC00).getRGB());
    public static final MobEffect MINE_GO_JUICE_HOE = new MineGoJuiceEffect(MiningSkillCardItem.Type.HOE, MobEffectCategory.BENEFICIAL, new Color(0x3E3117).getRGB());
}
