package net.ixdarklord.ultimine_addition.common.tag;

import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> MINING_SKILL_CARD = create("mining_skill_card");
    public static final TagKey<Item> MORE_VALUABLE_PIGMENT = create("more_valuable_pigment");
    public static final TagKey<Item> LESS_VALUABLE_PIGMENT = create("less_valuable_pigment");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, FTBUltimineAddition.rl(name));
    }
}
