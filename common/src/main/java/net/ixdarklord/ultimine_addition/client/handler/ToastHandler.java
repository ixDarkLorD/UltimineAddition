package net.ixdarklord.ultimine_addition.client.handler;

import net.ixdarklord.ultimine_addition.client.gui.toast.ChallengesToast;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class ToastHandler {
    public static void playChallengeToast(MiningSkillCardData.Identifier identifier, ItemStack stack) {
        Minecraft.getInstance().getToasts().addToast(new ChallengesToast(identifier, stack));
    }
}
