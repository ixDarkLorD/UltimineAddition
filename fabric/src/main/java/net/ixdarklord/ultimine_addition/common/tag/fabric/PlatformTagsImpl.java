package net.ixdarklord.ultimine_addition.common.tag.fabric;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * @see PlatformTags
 */
public final class PlatformTagsImpl implements PlatformTags {

    public static @NotNull PlatformTags get() {
        return new PlatformTagsImpl();
    }

    public @NotNull TagKey<Item> SLIME() {
        return createCommonTag(Registries.ITEM, "slime");
    }

    public @NotNull TagKey<Block> STONES() {
        return ConventionalBlockTags.STONES;
    }

    public @NotNull TagKey<Block> COBBLESTONES() {
        return ConventionalBlockTags.COBBLESTONES;
    }

    public @NotNull TagKey<Block> OBSIDIAN() {
        return createCommonTag(Registries.BLOCK, "obsidian");
    }

    public @NotNull TagKey<Block> ORES() {
        return ConventionalBlockTags.ORES;
    }

    public @NotNull TagKey<Block> COAL_ORES() {
        return createCommonTag(Registries.BLOCK, "ores/coal");
    }

    public @NotNull TagKey<Block> IRON_ORES() {
        return createCommonTag(Registries.BLOCK, "ores/iron");
    }

    public @NotNull TagKey<Block> COPPER_ORES() {
        return createCommonTag(Registries.BLOCK, "ores/copper");
    }

    public @NotNull TagKey<Block> GOLD_ORES() {
        return createCommonTag(Registries.BLOCK, "ores/gold");
    }

    public @NotNull TagKey<Block> LAPIS_ORES() {
        return createCommonTag(Registries.BLOCK, "ores/lapis");
    }

    public @NotNull TagKey<Block> REDSTONE_ORES() {
        return createCommonTag(Registries.BLOCK, "ores/redstone");
    }

    public @NotNull TagKey<Block> DIAMOND_ORES() {
        return createCommonTag(Registries.BLOCK, "ores/diamond");
    }

    public @NotNull TagKey<Block> EMERALD_ORES() {
        return createCommonTag(Registries.BLOCK, "ores/emerald");
    }

    public @NotNull TagKey<Block> QUARTZ_ORES() {
        return ConventionalBlockTags.QUARTZ_ORES;
    }
}
