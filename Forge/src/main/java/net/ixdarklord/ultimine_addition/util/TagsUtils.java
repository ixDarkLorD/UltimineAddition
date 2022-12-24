package net.ixdarklord.ultimine_addition.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class TagsUtils {
    public static class Blocks {
        public static final TagKey<Block> FORGE_ORES = forgeTag("ores");

        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }
    }
}
