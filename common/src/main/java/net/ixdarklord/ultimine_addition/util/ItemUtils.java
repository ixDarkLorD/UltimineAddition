package net.ixdarklord.ultimine_addition.util;

import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

public class ItemUtils {
    public record ItemSorter(ItemStack item, int slotId, int order){}

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
        ItemStack stack;
        if (player.getMainHandItem() != ItemStack.EMPTY) {
            stack = player.getMainHandItem();
        } else stack = player.getOffhandItem();
        return !(stack.getItem() instanceof DiggerItem);
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

    public static TagKey<Item> createTag(String name, String prefix) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(name + prefix));
    }
}
