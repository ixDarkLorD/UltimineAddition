package net.ixdarklord.ultimine_addition.common.tag;

import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
    public static final TagKey<Block> DENY_IS_PLACED_BY_ENTITY = create("deny_is_placed_by_entity");

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, FTBUltimineAddition.rl(name));
    }
}
