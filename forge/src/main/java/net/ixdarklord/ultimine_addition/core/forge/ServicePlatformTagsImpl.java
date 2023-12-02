package net.ixdarklord.ultimine_addition.core.forge;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

@SuppressWarnings("unused")
public class ServicePlatformTagsImpl {
    public static TagKey<Item> getPickaxes() {
        return Tags.Items.TOOLS_PICKAXES;
    }
    public static TagKey<Item> getAxes() {
        return Tags.Items.TOOLS_AXES;
    }
    public static TagKey<Item> getShovels() {
        return Tags.Items.TOOLS_SHOVELS;
    }
    public static TagKey<Item> getHoes() {
        return Tags.Items.TOOLS_HOES;
    }
}
