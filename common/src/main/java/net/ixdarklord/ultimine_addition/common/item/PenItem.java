package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcat_lib.util.ScreenUtils;
import net.ixdarklord.ultimine_addition.client.gui.screen.SkillsRecordScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PenItem extends StorageDataAbstractItem {
    public PenItem(Properties properties) {
        super(properties, "ink_chamber", 2000, ComponentType.TOOLS);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) return;
        if (!stack.hasTag() && entity instanceof ServerPlayer player) getData(stack).sendToClient(player).saveData(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (!(Minecraft.getInstance().screen instanceof SkillsRecordScreen) && !stack.hasTag() && isShiftButtonNotPressed(tooltipComponents)) return;
        if (!stack.hasTag()) {
            Component component = Component.translatable("tooltip.ultimine_addition.pen.info").withStyle(ChatFormatting.GRAY);
            List<Component> components = ScreenUtils.splitComponent(component, getSplitterLength());
            tooltipComponents.addAll(components);
            return;
        }
        tooltipComponents.add(Component.literal("§8• ").append(Component.translatable("tooltip.ultimine_addition.pen.ink_chamber", getData(stack).getCapacity()).withStyle(ChatFormatting.GRAY)));
    }
}
