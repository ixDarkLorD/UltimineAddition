package net.ixdarklord.ultimine_addition.datagen.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends FabricTagProvider.BlockTagProvider {

    public BlockTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        this.getOrCreateTagBuilder(ModBlockTags.DENY_IS_PLACED_BY_ENTITY)
                .forceAddTag(ConventionalBlockTags.CHESTS)
                .forceAddTag(BlockTags.BEDS)
                .add(Blocks.SPONGE, Blocks.WET_SPONGE)
                .add(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET)
                .add(Blocks.HAY_BLOCK)
                .add(Blocks.CRAFTING_TABLE)
                .add(Blocks.BARREL)
                .add(Blocks.LADDER)
                .add(Blocks.COMPOSTER)
                .add(Blocks.BOOKSHELF);

        this.getOrCreateTagBuilder(PlatformTags.get().OBSIDIAN()).add(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN);
        this.getOrCreateTagBuilder(PlatformTags.get().COAL_ORES()).add(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
        this.getOrCreateTagBuilder(PlatformTags.get().IRON_ORES()).add(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
        this.getOrCreateTagBuilder(PlatformTags.get().COPPER_ORES()).add(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
        this.getOrCreateTagBuilder(PlatformTags.get().GOLD_ORES()).add(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE);
        this.getOrCreateTagBuilder(PlatformTags.get().LAPIS_ORES()).add(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
        this.getOrCreateTagBuilder(PlatformTags.get().REDSTONE_ORES()).add(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
        this.getOrCreateTagBuilder(PlatformTags.get().DIAMOND_ORES()).add(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
        this.getOrCreateTagBuilder(PlatformTags.get().EMERALD_ORES()).add(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE);
    }
}
