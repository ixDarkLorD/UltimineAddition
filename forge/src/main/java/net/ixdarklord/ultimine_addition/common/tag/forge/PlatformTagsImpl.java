package net.ixdarklord.ultimine_addition.common.tag.forge;

import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

/**
 * @see PlatformTags
 */
public final class PlatformTagsImpl implements PlatformTags {
    public static @NotNull PlatformTags get() {
        return new PlatformTagsImpl();
    }

    public @NotNull TagKey<Item> SLIME() {
        return Tags.Items.SLIMEBALLS;
    }

    public @NotNull TagKey<Item> SEEDS() {
        return Tags.Items.SEEDS;
    }

    public @NotNull TagKey<Item> DYE(DyeColor color) {
        return color.getTag();
    }

    public @NotNull TagKey<Block> STONES() {
        return Tags.Blocks.STONE;
    }

    public @NotNull TagKey<Block> COBBLESTONES() {
        return Tags.Blocks.COBBLESTONE;
    }

    public @NotNull TagKey<Block> OBSIDIAN() {
        return Tags.Blocks.OBSIDIAN;
    }

    public @NotNull TagKey<Block> ORES() {
        return Tags.Blocks.ORES;
    }

    public @NotNull TagKey<Block> COAL_ORES() {
        return Tags.Blocks.ORES_COAL;
    }

    public @NotNull TagKey<Block> IRON_ORES() {
        return Tags.Blocks.ORES_IRON;
    }

    public @NotNull TagKey<Block> COPPER_ORES() {
        return Tags.Blocks.ORES_COPPER;
    }

    public @NotNull TagKey<Block> GOLD_ORES() {
        return Tags.Blocks.ORES_GOLD;
    }

    public @NotNull TagKey<Block> LAPIS_ORES() {
        return Tags.Blocks.ORES_LAPIS;
    }

    public @NotNull TagKey<Block> REDSTONE_ORES() {
        return Tags.Blocks.ORES_REDSTONE;
    }

    public @NotNull TagKey<Block> DIAMOND_ORES() {
        return Tags.Blocks.ORES_DIAMOND;
    }

    public @NotNull TagKey<Block> EMERALD_ORES() {
        return Tags.Blocks.ORES_EMERALD;
    }

    public @NotNull TagKey<Block> QUARTZ_ORES() {
        return Tags.Blocks.ORES_QUARTZ;
    }
}
