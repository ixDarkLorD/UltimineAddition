package net.ixdarklord.ultimine_addition.common.tag;

import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
    public static final TagKey<Block> DENY_IS_PLACED_BY_ENTITY = create("deny_is_placed_by_entity");

    public static final TagKey<Block> FORGE_STONE = createForge("stone");
    public static final TagKey<Block> FORGE_OBSIDIAN = createForge("obsidian");
    public static final TagKey<Block> FABRIC_STONE = createFabric("stone");
    public static final TagKey<Block> FABRIC_OBSIDIAN = createFabric("obsidian");

    // Ores
    public static final TagKey<Block> FORGE_ORES = createForge("ores");
    public static final TagKey<Block> FABRIC_ORES = createFabric("ores");
    public static final TagKey<Block> FORGE_COAL_ORES = createForge("ores/coal");
    public static final TagKey<Block> FORGE_IRON_ORES = createForge("ores/iron");
    public static final TagKey<Block> FORGE_COPPER_ORES = createForge("ores/copper");
    public static final TagKey<Block> FORGE_GOLD_ORES = createForge("ores/gold");
    public static final TagKey<Block> FORGE_LAPIS_ORES = createForge("ores/lapis");
    public static final TagKey<Block> FORGE_REDSTONE_ORES = createForge("ores/redstone");
    public static final TagKey<Block> FORGE_DIAMOND_ORES = createForge("ores/diamond");
    public static final TagKey<Block> FORGE_EMERALD_ORES = createForge("ores/emerald");
    public static final TagKey<Block> FORGE_QUARTZ_ORES = createForge("ores/quartz");
    public static final TagKey<Block> FABRIC_COAL_ORES = createFabric("coal_ores");
    public static final TagKey<Block> FABRIC_IRON_ORES = createFabric("iron_ores");
    public static final TagKey<Block> FABRIC_COPPER_ORES = createFabric("copper_ores");
    public static final TagKey<Block> FABRIC_GOLD_ORES = createFabric("gold_ores");
    public static final TagKey<Block> FABRIC_LAPIS_ORES = createFabric("lapis_ores");
    public static final TagKey<Block> FABRIC_REDSTONE_ORES = createFabric("redstone_ores");
    public static final TagKey<Block> FABRIC_DIAMOND_ORES = createFabric("diamond_ores");
    public static final TagKey<Block> FABRIC_EMERALD_ORES = createFabric("emerald_ores");
    public static final TagKey<Block> FABRIC_QUARTZ_ORES = createFabric("quartz_ores");

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, UltimineAddition.getLocation(name));
    }

    private static TagKey<Block> createForge(String name) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation("forge", name));
    }

    private static TagKey<Block> createFabric(String name) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation("c", name));
    }
}
