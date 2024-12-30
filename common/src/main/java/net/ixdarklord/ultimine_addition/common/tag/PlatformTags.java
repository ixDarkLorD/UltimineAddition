package net.ixdarklord.ultimine_addition.common.tag;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public abstract class PlatformTags {

    @ExpectPlatform
    public static @NotNull PlatformTags get() {
        throw new AssertionError();
    }

    // ITEM
    public abstract @NotNull TagKey<Item> SLIME();

    // BLOCKS
    public abstract @NotNull TagKey<Block> STONES();
    public abstract @NotNull TagKey<Block> COBBLESTONES();
    public abstract @NotNull TagKey<Block> OBSIDIAN();

    // ORES
    public abstract @NotNull TagKey<Block> ORES();
    public abstract @NotNull TagKey<Block> COAL_ORES();
    public abstract @NotNull TagKey<Block> IRON_ORES();
    public abstract @NotNull TagKey<Block> COPPER_ORES();
    public abstract @NotNull TagKey<Block> GOLD_ORES();
    public abstract @NotNull TagKey<Block> LAPIS_ORES();
    public abstract @NotNull TagKey<Block> REDSTONE_ORES();
    public abstract @NotNull TagKey<Block> DIAMOND_ORES();
    public abstract @NotNull TagKey<Block> EMERALD_ORES();
    public abstract @NotNull TagKey<Block> QUARTZ_ORES();

    // TOOLS
    public @NotNull TagKey<Item> PAXELS() {
        return createCommonTag(Registries.ITEM, "paxels");
    }

    public @NotNull TagKey<Item> TOOLS_PAXELS() {
        return createCommonTag(Registries.ITEM, "tools/paxel");
    }

    public <T> TagKey<T> createCommonTag(ResourceKey<Registry<T>> registry, String name) {
        return TagKey.create(registry, ResourceLocation.fromNamespaceAndPath("c", name));
    }
}
