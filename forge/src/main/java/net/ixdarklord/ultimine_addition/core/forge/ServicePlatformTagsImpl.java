package net.ixdarklord.ultimine_addition.core.forge;

import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

@SuppressWarnings("unused")
public class ServicePlatformTagsImpl {
    public static TagKey<Item> getPickaxes() {
        return ModItemTags.PICKAXES;
    }
    public static TagKey<Item> getAxes() {
        return ModItemTags.AXES;
    }
    public static TagKey<Item> getShovels() {
        return ModItemTags.SHOVELS;
    }
    public static TagKey<Item> getHoes() {
        return ModItemTags.HOES;
    }
}
