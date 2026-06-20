package net.ixdarklord.ultimine_addition.common.item;

import dev.architectury.registry.menu.MenuRegistry;
import net.ixdarklord.coolcatlib.api.item.ComponentItem;
import net.ixdarklord.coolcatlib.api.utils.ComponentHelper;
import net.ixdarklord.ultimine_addition.common.menu.ShapeSelectorMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShapeSelectorItem extends ComponentItem {
    public static final Component TITLE = Component.translatable("item.ultimine_addition.shape_selector");

    public ShapeSelectorItem(Item.Properties properties) {
        super(properties, ComponentType.TOOLS);
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        if (!this.isShiftButtonNotPressed(tooltipComponents)) {
            Component component = Component.translatable("tooltip.ultimine_addition.shape_selector.info").withStyle(ChatFormatting.GRAY);
            List<Component> components = ComponentHelper.splitComponent(component, this.getSplitterLength());
            tooltipComponents.addAll(components);
        }
    }

    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (player instanceof ServerPlayer serverPlayer) {
            MenuRegistry.openExtendedMenu(serverPlayer, new SimpleMenuProvider(ShapeSelectorMenu::new, TITLE), (buf) -> {
            });
            return super.use(level, player, usedHand);
        } else {
            return super.use(level, player, usedHand);
        }
    }

    public boolean appendToName() {
        return true;
    }
}
