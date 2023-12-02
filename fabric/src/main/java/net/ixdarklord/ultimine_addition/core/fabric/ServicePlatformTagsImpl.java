package net.ixdarklord.ultimine_addition.core.fabric;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

@SuppressWarnings("unused")
public class ServicePlatformTagsImpl {
    public static TagKey<Item> getPickaxes() {
        return ConventionalItemTags.PICKAXES;
    }
    public static TagKey<Item> getAxes() {
        return ConventionalItemTags.AXES;
    }
    public static TagKey<Item> getShovels() {
        return ConventionalItemTags.SHOVELS;
    }
    public static TagKey<Item> getHoes() {
        return ConventionalItemTags.HOES;
    }
}
