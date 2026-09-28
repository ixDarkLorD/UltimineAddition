package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcatcore.api.menu.ExtendedMenus;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.ixdarklord.coolcatcore.api.item.ComponentItem;
import net.ixdarklord.coolcatcore.api.utils.ComponentHelper;
import net.ixdarklord.ultimine_addition.common.menu.ShapeSelectorMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShapeSelectorItem extends ComponentItem {
    public static final Component TITLE = Component.translatable("item.ultimine_addition.shape_selector");

    public ShapeSelectorItem(Properties properties) {
        super(properties, ComponentType.TOOLS);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        if (isShiftButtonNotPressed(tooltipComponents)) return;

        Component component = Component.translatable("tooltip.ultimine_addition.shape_selector.info").withStyle(ChatFormatting.GRAY);
        List<Component> components = ComponentHelper.splitComponent(component, getSplitterLength());
        components.forEach(tooltipComponents);
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        if (!(player instanceof ServerPlayer serverPlayer)) return super.use(level, player, usedHand);
        ExtendedMenus.open(serverPlayer, new SimpleMenuProvider(ShapeSelectorMenu::new, TITLE));
        return super.use(level, player, usedHand);
    }

    @Override
    public boolean appendToName() {
        return true;
    }
}
