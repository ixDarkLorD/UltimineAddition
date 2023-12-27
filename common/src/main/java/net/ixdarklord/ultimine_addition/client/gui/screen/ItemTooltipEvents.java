package net.ixdarklord.ultimine_addition.client.gui.screen;

import net.ixdarklord.coolcat_lib.common.item.ComponentItem;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.potion.MineGoPotion;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.List;

public class ItemTooltipEvents {
    public static void init(ItemStack stack, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (stack.getItem() instanceof ComponentItem
                && stack.getItem() != ModItems.MINER_CERTIFICATE
                && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equalsIgnoreCase(Constants.MOD_ID)) {
            if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) {
                tooltipComponents.add(1, Component.translatable("tooltip.ultimine_addition.legacy_mode.disabled_item").withStyle(ChatFormatting.RED));
            }
        }

        if (stack.getItem() instanceof PotionItem && PotionUtils.getPotion(stack) instanceof MineGoPotion potion) {
            MutableComponent name = tooltipComponents.get(0).copy().append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY).append(potion.getComponentType().get()));
            tooltipComponents.set(0, name);

            tooltipComponents.add(1, Component.literal("§8• ").append(Component.translatable("tooltip.ultimine_addition.skill_card.tier", potion.getTier().getDisplayName())).withStyle(ChatFormatting.ITALIC));
            if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) {
                tooltipComponents.add(1, Component.translatable("tooltip.ultimine_addition.legacy_mode.disabled_item").withStyle(ChatFormatting.RED));
            }
        }
    }
}
