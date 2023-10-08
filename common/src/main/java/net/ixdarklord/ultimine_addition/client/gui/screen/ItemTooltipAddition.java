package net.ixdarklord.ultimine_addition.client.gui.screen;

import net.ixdarklord.ultimine_addition.common.potion.MineGoPotion;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.List;

public class ItemTooltipAddition {
    public static void init(ItemStack stack, List<Component> tooltip, @SuppressWarnings("unused") TooltipFlag context) {
        if (stack.isEmpty()) return;
        if (stack.getItem() instanceof PotionItem && PotionUtils.getPotion(stack) instanceof MineGoPotion potion) {
            tooltip.add(1, Component.literal("§8• ").append(Component.translatable("tooltip.ultimine_addition.skill_card.tier", potion.getTier().getDisplayName())).withStyle(ChatFormatting.ITALIC));
            tooltip.add(1, Component.literal("§8| ").append(potion.getComponentType().get()));
        }
    }
}
