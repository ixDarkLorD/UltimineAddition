package net.ixdarklord.ultimine_addition.util;

import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemUtils {
    public record ItemSorter(ItemStack item, int slotId, int order){}

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

        return stack.is(ServicePlatform.Tags.getPickaxes());
    }
    public static boolean isItemInHandAxe(Player player) {
        ItemStack stack;
        if (player.getMainHandItem() != ItemStack.EMPTY) {
            stack = player.getMainHandItem();
        } else stack = player.getOffhandItem();

        return stack.is(ServicePlatform.Tags.getAxes());
    }
    public static boolean isItemInHandShovel(Player player) {
        ItemStack stack;
        if (player.getMainHandItem() != ItemStack.EMPTY) {
            stack = player.getMainHandItem();
        } else stack = player.getOffhandItem();

        return stack.is(ServicePlatform.Tags.getShovels());
    }
    public static boolean isItemInHandHoe(Player player) {
        ItemStack stack;
        if (player.getMainHandItem() != ItemStack.EMPTY) {
            stack = player.getMainHandItem();
        } else stack = player.getOffhandItem();

        return stack.is(ServicePlatform.Tags.getHoes());
    }

    public static TagKey<Item> createTag(String name, String prefix) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(name + prefix));
    }
}
