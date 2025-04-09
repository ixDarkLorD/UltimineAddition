package net.ixdarklord.ultimine_addition.client.gui.screens;

import net.ixdarklord.coolcatlib.api.util.ColorUtils;
import net.ixdarklord.ultimine_addition.common.data.item.SelectedShapeData;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.potion.MineGoPotion;
import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class ItemTooltipEvents {
    public static void init(ItemStack stack, List<Component> components, Item.TooltipContext ignored, TooltipFlag ignored1) {
        if (stack.has(Registration.SELECTED_SHAPE_COMPONENT.get())) {
            insertSelectedShapeInfo(stack, components);
        }

        if (stack.is(ModItemTags.LEGACY_DISABLED_ITEMS)) {
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

    private static void insertSelectedShapeInfo(ItemStack stack, List<Component> components) {
        SelectedShapeData shapeData = stack.get(Registration.SELECTED_SHAPE_COMPONENT.get());
        double ratio = Mth.clamp((Mth.sin(Util.getMillis() / 160F) + 1.0) / 2.0, 0.0, 1.0);
        Color color = ColorUtils.blendColors(new Color(0xA0DA3E), new Color(0xA0DA3E).brighter(), ratio);
        Component shapeName = Component.translatable("ftbultimine.shape." + Objects.requireNonNull(shapeData).shape().getName())
                .withColor(color.getRGB());

        List<MutableComponent> componentList = List.of(
                Component.literal(""),
                Component.translatable("tooltip.ultimine_addition.shape_selector.selected").withStyle(ChatFormatting.GRAY),
                Component.literal("- ").withStyle(ChatFormatting.DARK_GRAY).append(shapeName)
        );

        for (int i = 0; i < components.size(); i++) {
            Component component = components.get(i);
            if (component.getString().isEmpty()) {
                components.set(i, Component.literal(" "));
                components.addAll(i, componentList);
                return;
            }
        }

        components.addAll(componentList);
    }

    @Nullable
    private static Potion getPotion(ItemStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion()
                .map(Holder::value)
                .orElse(null);
    }
}
