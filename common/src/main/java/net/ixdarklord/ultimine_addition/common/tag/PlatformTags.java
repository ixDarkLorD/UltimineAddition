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

public interface PlatformTags {
    @ExpectPlatform
    static @NotNull PlatformTags get() {
        throw new UnsupportedOperationException("This loader is not supported!");
    }

    // ITEM
    @NotNull TagKey<Item> SLIME();

    // BLOCKS
    @NotNull TagKey<Block> STONES();
    @NotNull TagKey<Block> COBBLESTONES();
    @NotNull TagKey<Block> OBSIDIAN();

    // ORES
    @NotNull TagKey<Block> ORES();
    @NotNull TagKey<Block> COAL_ORES();
    @NotNull TagKey<Block> IRON_ORES();
    @NotNull TagKey<Block> COPPER_ORES();
    @NotNull TagKey<Block> GOLD_ORES();
    @NotNull TagKey<Block> LAPIS_ORES();
    @NotNull TagKey<Block> REDSTONE_ORES();
    @NotNull TagKey<Block> DIAMOND_ORES();
    @NotNull TagKey<Block> EMERALD_ORES();
    @NotNull TagKey<Block> QUARTZ_ORES();

    // TOOLS
    @NotNull
    default TagKey<Item> PAXELS() {
        return createCommonTag(Registries.ITEM, "paxels");
    }

    @NotNull
    default TagKey<Item> TOOLS_PAXELS() {
        return createCommonTag(Registries.ITEM, "tools/paxel");
    }

    default <T> TagKey<T> createCommonTag(ResourceKey<Registry<T>> registry, String name) {
        return TagKey.create(registry, ResourceLocation.fromNamespaceAndPath("c", name));
    }
}
