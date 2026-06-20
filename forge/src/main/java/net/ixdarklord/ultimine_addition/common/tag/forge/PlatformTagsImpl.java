package net.ixdarklord.ultimine_addition.common.tag.forge;

import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.common.Tags.Items;
import org.jetbrains.annotations.NotNull;

public final class PlatformTagsImpl implements PlatformTags {
    public static @NotNull PlatformTags get() {
        return new PlatformTagsImpl();
    }

    public @NotNull TagKey<Item> SLIME() {
        return Items.SLIMEBALLS;
    }

    public @NotNull TagKey<Block> STONES() {
        return Blocks.STONE;
    }

    public @NotNull TagKey<Block> COBBLESTONES() {
        return Blocks.COBBLESTONE;
    }

    public @NotNull TagKey<Block> OBSIDIAN() {
        return Blocks.OBSIDIAN;
    }

    public @NotNull TagKey<Block> ORES() {
        return Blocks.ORES;
    }

    public @NotNull TagKey<Block> COAL_ORES() {
        return Blocks.ORES_COAL;
    }

    public @NotNull TagKey<Block> IRON_ORES() {
        return Blocks.ORES_IRON;
    }

    public @NotNull TagKey<Block> COPPER_ORES() {
        return Blocks.ORES_COPPER;
    }

    public @NotNull TagKey<Block> GOLD_ORES() {
        return Blocks.ORES_GOLD;
    }

    public @NotNull TagKey<Block> LAPIS_ORES() {
        return Blocks.ORES_LAPIS;
    }

    public @NotNull TagKey<Block> REDSTONE_ORES() {
        return Blocks.ORES_REDSTONE;
    }

    public @NotNull TagKey<Block> DIAMOND_ORES() {
        return Blocks.ORES_DIAMOND;
    }

    public @NotNull TagKey<Block> EMERALD_ORES() {
        return Blocks.ORES_EMERALD;
    }

    public @NotNull TagKey<Block> QUARTZ_ORES() {
        return Blocks.ORES_QUARTZ;
    }
}
