package net.ixdarklord.ultimine_addition.util;

import net.ixdarklord.coolcat_lib.util.InventoryHelper;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public static ItemStack findItemInHand(Player player, Item item) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() != item) stack = player.getOffhandItem();
        if (ServicePlatform.SlotAPI.isModLoaded() && stack.getItem() != item)
            stack = ServicePlatform.SlotAPI.getSkillsRecordItem(player);

        if (stack.getItem() == item) return stack;
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> listMatchingItem(Player player, Item item) {
        List<ItemStack> result = new ArrayList<>();
        if (ServicePlatform.SlotAPI.isModLoaded()) {
            ItemStack stack = ServicePlatform.SlotAPI.getSkillsRecordItem(player);
            if (!stack.isEmpty() && stack.is(item)) result.add(stack);
        }
        result.addAll(InventoryHelper.listMatchingItem(player.getInventory(), item));
        return result;
    }

    public static boolean checkTargetedBlock(Player player) {
        HitResult hit = player.pick(ServicePlatform.Players.getReachAttribute(player), 1.0F, false);
        if (!(hit instanceof BlockHitResult hitResult)) return false;

        BlockState blockState = player.level().getBlockState(hitResult.getBlockPos());
        ItemStack tool = player.getItemInHand(player.getUsedItemHand());
        return ServicePlatform.Players.isCorrectToolForBlock(tool, blockState);
    }

    public static boolean isItemInHandNotTools(Player player) {
        return !(getItemInHand(player, true).getItem() instanceof DiggerItem);
    }
    public static boolean isItemInHandCustomCardValid(Player player) {
        return FTBUltimineIntegration.getCustomCardTypes(player).stream()
                .map(MiningSkillCardItem.Type::utilizeRequiredTools)
                .flatMap(Collection::stream)
                .toList()
                .contains(getItemInHand(player, true).getItem());
    }
    public static boolean isItemInHandPickaxe(Player player) {
        ItemStack stack = getItemInHand(player, true);
        return stack.is(ItemTags.PICKAXES) || stack.getItem() instanceof PickaxeItem;
    }
    public static boolean isItemInHandAxe(Player player) {
        ItemStack stack = getItemInHand(player, true);
        return stack.is(ItemTags.AXES) || stack.getItem() instanceof AxeItem;
    }
    public static boolean isItemInHandShovel(Player player) {
        ItemStack stack = getItemInHand(player, true);
        return stack.is(ItemTags.SHOVELS) || stack.getItem() instanceof ShovelItem;
    }
    public static boolean isItemInHandHoe(Player player) {
        ItemStack stack = getItemInHand(player, true);
        return stack.is(ItemTags.HOES) || stack.getItem() instanceof HoeItem;
    }
}
