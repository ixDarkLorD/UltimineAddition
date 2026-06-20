package net.ixdarklord.ultimine_addition.client.event;

import net.ixdarklord.coolcatlib.api.utils.ColorUtils;
import net.ixdarklord.ultimine_addition.common.data.item.SelectedShapeData;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.potion.MineGoPotion;
import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.awt.*;
import java.util.List;

public class ItemTooltipEvents {
    public static void init(ItemStack stack, List<Component> components, TooltipFlag ignored) {
        CompoundTag tag = stack.getTag();
        if (tag != null && !tag.getCompound(SelectedShapeData.DATA_ID.toString()).isEmpty()) {
            insertSelectedShapeInfo(stack, components);
        }

        if (stack.is(ModItemTags.LEGACY_DISABLED_ITEMS) && ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) {
            components.add(1, Component.translatable("tooltip.ultimine_addition.legacy_mode.disabled_item").withStyle(ChatFormatting.RED));
        }

        if (stack.getItem() instanceof PotionItem) {
            Potion potion2 = getPotion(stack);
            if (potion2 instanceof MineGoPotion potion) {
                MutableComponent name = components.get(0).copy().append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY).append(potion.getComponentType().get()));
                components.set(0, name);
                components.add(1, Component.literal("§8• ").append(Component.translatable("tooltip.ultimine_addition.skill_card.tier", potion.getTier().getDisplayName())).withStyle(ChatFormatting.ITALIC));
                if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) {
                    components.add(1, Component.translatable("tooltip.ultimine_addition.legacy_mode.disabled_item").withStyle(ChatFormatting.RED));
                }
            }
        }

        if (stack.getItem() instanceof SkillsRecordItem) {
            for (int i = 0; i < components.size(); ++i) {
                Component component = components.get(i);
                String slot = "trinkets.slot.hand.skills_record";
                if (component.getString().contains(slot)) {
                    components.set(i, Component.literal(component.getString().replace(slot, Component.translatable("item.ultimine_addition.skills_record").getString())).withStyle(ChatFormatting.BLUE));
                }
            }
        }

    }

    private static void insertSelectedShapeInfo(ItemStack stack, List<Component> components) {
        SelectedShapeData shapeData = SelectedShapeData.load(stack);
        if (shapeData != null && shapeData.getShape() != null) {
            double ratio = Mth.clamp(((double) Mth.sin((float) Util.getMillis() / 160.0F) + (double) 1.0F) / (double) 2.0F, 0.0F, 1.0F);
            Color color = ColorUtils.blend((new Color(10541630)).brighter(), new Color(10541630), ratio);
            Component shapeName = FTBUltimineIntegration.getShapeDisplayName(shapeData.getShape()).copy().withStyle(Style.EMPTY.withColor(color.getRGB()));
            List<MutableComponent> componentList = List.of(Component.literal(""), Component.translatable("tooltip.ultimine_addition.shape_selector.selected").withStyle(ChatFormatting.GRAY), Component.literal("- ").withStyle(ChatFormatting.DARK_GRAY).append(shapeName));

            for (int i = 0; i < components.size(); ++i) {
                Component component = components.get(i);
                if (component.getString().isEmpty()) {
                    components.set(i, Component.literal(" "));
                    components.addAll(i, componentList);
                    return;
                }
            }

            components.addAll(componentList);
        }
    }

    private static Potion getPotion(ItemStack stack) {
        return PotionUtils.getPotion(stack);
    }
}
