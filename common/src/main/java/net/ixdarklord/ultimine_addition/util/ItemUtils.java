package net.ixdarklord.ultimine_addition.util;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

import java.util.Collection;

public class ItemUtils {
    public record ItemSorter(ItemStack item, int slotId, int order){}

    public static ItemStack getItemInHand(Player player, boolean checkBoth) {
        ItemStack stack = ItemStack.EMPTY;
        if (player.getMainHandItem() != ItemStack.EMPTY)
            stack = player.getMainHandItem();
        else if (checkBoth)
            stack = player.getOffhandItem();

        if (stack.getItem() != Items.AIR) return stack;
        return ItemStack.EMPTY;
    }

    @SuppressWarnings("ConstantValue")
    public static ItemStack findItemInHand(Player player, Item item) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() != item) stack = player.getOffhandItem();
        if (stack.getItem() != item && ServicePlatform.SlotAPI.isModLoaded())
            stack = ServicePlatform.SlotAPI.getSkillsRecordItem(player);

        if (stack.getItem() == item) return stack;
        return ItemStack.EMPTY;
    }

    public static boolean isItemInHandNotTools(Player player) {
        return !(getItemInHand(player, true).getItem() instanceof DiggerItem);
    }
    public static boolean isItemInHandCustomCardValid(Player player) {
        return MiningSkillCardItem.Type.TYPES.stream()
                .filter(MiningSkillCardItem.Type::isCustomType)
                .map(MiningSkillCardItem.Type::utilizeRequiredTools)
                .flatMap(Collection::stream)
                .toList()
                .contains(getItemInHand(player, true).getItem());
    }
    public static boolean isItemInHandPickaxe(Player player) {
        ItemStack stack;
        if (player.getMainHandItem() != ItemStack.EMPTY) {
            stack = player.getMainHandItem();
        } else stack = player.getOffhandItem();

        return stack.is(ServicePlatform.Tags.getPickaxes()) || stack.getItem() instanceof PickaxeItem;
    }
    public static boolean isItemInHandAxe(Player player) {
        ItemStack stack;
        if (player.getMainHandItem() != ItemStack.EMPTY) {
            stack = player.getMainHandItem();
        } else stack = player.getOffhandItem();

        return stack.is(ServicePlatform.Tags.getAxes()) || stack.getItem() instanceof AxeItem;
    }
    public static boolean isItemInHandShovel(Player player) {
        ItemStack stack;
        if (player.getMainHandItem() != ItemStack.EMPTY) {
            stack = player.getMainHandItem();
        } else stack = player.getOffhandItem();

        return stack.is(ServicePlatform.Tags.getShovels()) || stack.getItem() instanceof ShovelItem;
    }
    public static boolean isItemInHandHoe(Player player) {
        ItemStack stack;
        if (player.getMainHandItem() != ItemStack.EMPTY) {
            stack = player.getMainHandItem();
        } else stack = player.getOffhandItem();

        return stack.is(ServicePlatform.Tags.getHoes()) || stack.getItem() instanceof HoeItem;
    }
}
