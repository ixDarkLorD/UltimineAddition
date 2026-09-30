package net.ixdarklord.ultimine_addition.common.item;

import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerLevel;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.ixdarklord.coolcatcore.api.utils.ComponentHelper;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.common.data.item.StorageItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PenItem extends StorageItem {
    public PenItem(Properties properties) {
        super(properties, StorageItemData.create("ink_chamber", 2000), ComponentType.TOOLS);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity entity, @Nullable EquipmentSlot slot) {
        if (this.isLegacyMode()) return;
        if (!stack.has(StorageItemData.DATA_COMPONENT) && entity instanceof ServerPlayer)
            getData(stack).save();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
        if (!(Minecraft.getInstance().screen instanceof SkillsRecordScreen) && !stack.has(StorageItemData.DATA_COMPONENT) && isShiftButtonNotPressed(tooltipComponents)) return;
        if (!stack.has(StorageItemData.DATA_COMPONENT)) {
            Component component = Component.translatable("tooltip.ultimine_addition.pen.info").withStyle(ChatFormatting.GRAY);
            List<Component> components = ComponentHelper.splitComponent(component, getSplitterLength());
            components.forEach(tooltipComponents);
            return;
        }
        tooltipComponents.accept(Component.literal("§8• ").append(Component.translatable("tooltip.ultimine_addition.pen.ink_chamber", getData(stack).getCapacity()).withStyle(ChatFormatting.GRAY)));
    }
}
