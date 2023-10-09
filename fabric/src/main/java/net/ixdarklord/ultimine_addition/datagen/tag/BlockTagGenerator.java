package net.ixdarklord.ultimine_addition.datagen.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
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

        this.getOrCreateTagBuilder(ModBlockTags.FABRIC_STONE).add(Blocks.STONE, Blocks.INFESTED_STONE, Blocks.DEEPSLATE, Blocks.INFESTED_DEEPSLATE, Blocks.ANDESITE, Blocks.GRANITE, Blocks.DIORITE);
        this.getOrCreateTagBuilder(ModBlockTags.FABRIC_OBSIDIAN).add(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN);
        this.getOrCreateTagBuilder(ModBlockTags.FABRIC_COAL_ORES).add(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
        this.getOrCreateTagBuilder(ModBlockTags.FABRIC_IRON_ORES).add(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
        this.getOrCreateTagBuilder(ModBlockTags.FABRIC_COPPER_ORES).add(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
        this.getOrCreateTagBuilder(ModBlockTags.FABRIC_GOLD_ORES).add(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE);
        this.getOrCreateTagBuilder(ModBlockTags.FABRIC_LAPIS_ORES).add(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
        this.getOrCreateTagBuilder(ModBlockTags.FABRIC_REDSTONE_ORES).add(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
        this.getOrCreateTagBuilder(ModBlockTags.FABRIC_DIAMOND_ORES).add(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
        this.getOrCreateTagBuilder(ModBlockTags.FABRIC_EMERALD_ORES).add(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE);
        this.getOrCreateTagBuilder(ModBlockTags.FABRIC_QUARTZ_ORES).add(Blocks.NETHER_QUARTZ_ORE);
    }
}
