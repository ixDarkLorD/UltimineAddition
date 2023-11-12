package net.ixdarklord.ultimine_addition.util;

import dev.architectury.platform.Platform;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

public class ItemUtils {
    public record ItemSorter(ItemStack item, int slotId, int order){}

    public static ItemStack findItemInHand(Player player, Item item, boolean includeCurios) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() != item) stack = player.getOffhandItem();
        if (includeCurios && stack.getItem() != item && Platform.isModLoaded("curios"))
            stack = ItemStack.EMPTY;

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

        return stack.is(ItemTags.PICKAXES) || stack.getItem() instanceof PickaxeItem;
    }
    public static boolean isItemInHandAxe(Player player) {
        ItemStack stack;
        if (player.getMainHandItem() != ItemStack.EMPTY) {
            stack = player.getMainHandItem();
        } else stack = player.getOffhandItem();

        return stack.is(ItemTags.AXES) || stack.getItem() instanceof AxeItem;
    }
    public static boolean isItemInHandShovel(Player player) {
        ItemStack stack;
        if (player.getMainHandItem() != ItemStack.EMPTY) {
            stack = player.getMainHandItem();
        } else stack = player.getOffhandItem();

        return stack.is(ItemTags.SHOVELS) || stack.getItem() instanceof ShovelItem;
    }
    public static boolean isItemInHandHoe(Player player) {
        ItemStack stack;
        if (player.getMainHandItem() != ItemStack.EMPTY) {
            stack = player.getMainHandItem();
        } else stack = player.getOffhandItem();

        return stack.is(ItemTags.HOES) || stack.getItem() instanceof HoeItem;
    }

    public static TagKey<Item> createTag(String name, String prefix) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(name + prefix));
    }
}
