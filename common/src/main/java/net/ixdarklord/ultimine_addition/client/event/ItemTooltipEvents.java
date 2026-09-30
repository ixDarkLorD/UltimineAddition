package net.ixdarklord.ultimine_addition.client.event;

import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.coolcatcore.api.utils.ColorUtils;
import net.ixdarklord.ultimine_addition.common.data.item.SelectedShapeData;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.potion.MineGoPotion;
import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public final class ItemTooltipEvents {
    public static void init(ItemStack stack, TooltipFlag ignored1, List<Component> components) {
        if (Registration.SELECTED_SHAPE_COMPONENT.has(stack)) {
            insertSelectedShapeInfo(stack, components);
        }

        if (stack.is(ModItemTags.LEGACY_DISABLED_ITEMS)) {
            if (PlaystyleModes.isLegacy()) {
                components.add(1, Component.translatable("tooltip.ultimine_addition.legacy_mode.disabled_item").withStyle(ChatFormatting.RED));
            }
        }

        if (stack.getItem() instanceof PotionItem && getPotion(stack) instanceof MineGoPotion potion) {
            MutableComponent name = components.get(0).copy().append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY).append(potion.getComponentType().get()));
            components.set(0, name);

            components.add(1, Component.literal("§8• ").append(Component.translatable("tooltip.ultimine_addition.skill_card.tier", potion.getTier().getDisplayName())).withStyle(ChatFormatting.ITALIC));
            potion.getEffects().stream()
                    .map(effect -> effect.getEffect())
                    .filter(effect -> effect instanceof MineGoJuiceEffect)
                    .findFirst()
                    .ifPresent(effect -> components.add(2, Component.translatable("tooltip.ultimine_addition.mine_go_juice.info",
                            ShapeCertificateItem.toolName(((MineGoJuiceEffect) effect).getType().getId()).copy().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY)));
            if (PlaystyleModes.isLegacy()) {
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
        SelectedShapeData shapeData = Registration.SELECTED_SHAPE_COMPONENT.get(stack);
        double ratio = Mth.clamp((Mth.sin(Util.getMillis() / 160F) + 1.0) / 2.0, 0.0, 1.0);
        Color color = ColorUtils.blend(new Color(0xA0DA3E), new Color(0xA0DA3E).brighter(), ratio);
        Component shapeName = FTBUltimineIntegration.shapeName(Objects.requireNonNull(shapeData).shape())
                .withStyle(style -> style.withColor(color.getRGB()));

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
        return PotionUtils.getPotion(stack);
    }
}
