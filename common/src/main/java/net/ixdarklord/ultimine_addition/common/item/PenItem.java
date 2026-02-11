package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcatlib.api.utils.ComponentHelper;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.common.data.item.StorageItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PenItem extends StorageItem {
    public PenItem(Properties properties) {
        super(properties, StorageItemData.create("ink_chamber", 2000), ComponentType.TOOLS);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (this.isLegacyMode() || level.isClientSide()) return;
        if (!stack.has(StorageItemData.DATA_COMPONENT) && entity instanceof ServerPlayer)
            getData(stack).save();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
        if (!(Minecraft.getInstance().screen instanceof SkillsRecordScreen) && !stack.has(StorageItemData.DATA_COMPONENT) && isShiftButtonNotPressed(tooltipComponents)) return;
        if (!stack.has(StorageItemData.DATA_COMPONENT)) {
            Component component = Component.translatable("tooltip.ultimine_addition.pen.info").withStyle(ChatFormatting.GRAY);
            List<Component> components = ComponentHelper.splitComponent(component, getSplitterLength());
            tooltipComponents.addAll(components);
            return;
        }
        tooltipComponents.add(Component.literal("§8• ").append(Component.translatable("tooltip.ultimine_addition.pen.ink_chamber", getData(stack).getCapacity()).withStyle(ChatFormatting.GRAY)));
    }
}
