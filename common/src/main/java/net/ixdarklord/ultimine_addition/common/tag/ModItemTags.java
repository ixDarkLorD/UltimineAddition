package net.ixdarklord.ultimine_addition.common.tag;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> MINING_SKILL_CARD = create("mining_skill_card");
    public static final TagKey<Item> MORE_VALUABLE_PIGMENT = create("more_valuable_pigment");
    public static final TagKey<Item> LESS_VALUABLE_PIGMENT = create("less_valuable_pigment");
    public static final TagKey<Item> SLIMEBALLS_FABRIC = createFabric("slime_balls");

    public static final TagKey<Item> PICKAXES = createMC("pickaxes");
    public static final TagKey<Item> AXES = createMC("axes");
    public static final TagKey<Item> SHOVELS = createMC("shovels");
    public static final TagKey<Item> HOES = createMC("hoes");

    public static TagKey<Item> create(String name) {
        return TagKey.create(Registry.ITEM_REGISTRY, Constants.getLocation(name));
    }
    public static TagKey<Item> createMC(String name) {
        return TagKey.create(Registry.ITEM_REGISTRY, Constants.getMCLocation(name));
    }
    public static TagKey<Item> createForge(String name) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", name));
    }
    public static TagKey<Item> createFabric(String name) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("c", name));
    }
}
