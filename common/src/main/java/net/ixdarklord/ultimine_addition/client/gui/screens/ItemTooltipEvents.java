package net.ixdarklord.ultimine_addition.client.gui.screens;

import net.ixdarklord.coolcatlib.api.item.ComponentItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.potion.MineGoPotion;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemTooltipEvents {
    public static void init(ItemStack stack, List<Component> components, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag) {
        if (stack.getItem() instanceof ComponentItem
                && stack.getItem() != ModItems.MINER_CERTIFICATE
                && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equalsIgnoreCase(FTBUltimineAddition.MOD_ID)) {
            if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) {
                components.add(1, Component.translatable("tooltip.ultimine_addition.legacy_mode.disabled_item").withStyle(ChatFormatting.RED));
            }
        }

        if (stack.getItem() instanceof PotionItem && getPotion(stack) instanceof MineGoPotion potion) {
            MutableComponent name = components.getFirst().copy().append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY).append(potion.getComponentType().get()));
            components.set(0, name);

            components.add(1, Component.literal("§8• ").append(Component.translatable("tooltip.ultimine_addition.skill_card.tier", potion.getTier().getDisplayName())).withStyle(ChatFormatting.ITALIC));
            if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) {
                components.add(1, Component.translatable("tooltip.ultimine_addition.legacy_mode.disabled_item").withStyle(ChatFormatting.RED));
            }
        }

        if (stack.getItem() instanceof SkillsRecordItem) {
            for (int i = 0; i < components.size(); i++) {
                Component component = components.get(i);
                String slot = "trinkets.slot.hand.skills_record";
                if (component.getString().contains(slot)) {
                    components.set(i, Component.literal(component.getString().replace(slot, Component.translatable("item.ultimine_addition.skills_record").getString())).withStyle(ChatFormatting.BLUE));
                }
            }
        }
    }

    @Nullable
    private static Potion getPotion(ItemStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion()
                .map(Holder::value)
                .orElse(null);
    }
}
