package net.ixdarklord.ultimine_addition.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class TagsUtils {
    public static class Blocks {
        public static final TagKey<Block> COMMON_ORES = commonTag("ores");

        private static TagKey<Block> commonTag(String name) {
            return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("c", name));
        }
    }
}
