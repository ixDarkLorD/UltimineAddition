package net.ixdarklord.ultimine_addition.datagen.challenge;

import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.datagen.challenge.builder.ChallengesBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.ixdarklord.ultimine_addition.core.UltimineAddition.getLocation;

public class ChallengeGenerator extends ChallengeProvider {
    public ChallengeGenerator(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildChallenges(Consumer<ChallengesBuilder.Result> consumer) {
        buildPickaxeChallenges(consumer);
        buildAxeChallenges(consumer);
        buildShovelChallenges(consumer);
        buildHoeChallenges(consumer);
    }

    private void buildPickaxeChallenges(Consumer<ChallengesBuilder.Result> consumer) {
        ChallengesBuilder.create(getLocation("gathering_stones"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Unlearned)
                .requiredAmount(128)
                .targetedBlocks(ModBlockTags.FORGE_STONE, ModBlockTags.FABRIC_STONE)
                .save(consumer);
        ChallengesBuilder.create(getLocation("it_looks_dripping"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Unlearned)
                .requiredAmount(25, 64)
                .targetedBlocks(Blocks.DRIPSTONE_BLOCK, Blocks.POINTED_DRIPSTONE)
                .save(consumer);
        ChallengesBuilder.create(getLocation("digging_for_coal"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Unlearned)
                .requiredAmount(64, 128)
                .targetedBlocks(ModBlockTags.FORGE_COAL_ORES, ModBlockTags.FABRIC_COAL_ORES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("digging_for_iron"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64, 128)
                .targetedBlocks(ModBlockTags.FORGE_IRON_ORES, ModBlockTags.FABRIC_IRON_ORES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("digging_for_copper"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64, 128)
                .targetedBlocks(ModBlockTags.FORGE_COPPER_ORES, ModBlockTags.FABRIC_COPPER_ORES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("digging_for_gold"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(32, 64)
                .targetedBlocks(ModBlockTags.FORGE_GOLD_ORES, ModBlockTags.FABRIC_GOLD_ORES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("digging_for_lapis"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64, 128)
                .targetedBlocks(ModBlockTags.FORGE_LAPIS_ORES, ModBlockTags.FABRIC_LAPIS_ORES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("ugly_quartz"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.AMETHYST_CLUSTER)
                .save(consumer);
        ChallengesBuilder.create(getLocation("is_it_useful"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.CALCITE, Blocks.BASALT)
                .save(consumer);
        ChallengesBuilder.create(getLocation("digging_for_redstone"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(32, 64)
                .targetedBlocks(ModBlockTags.FORGE_REDSTONE_ORES, ModBlockTags.FABRIC_REDSTONE_ORES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("digging_for_diamond"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(32, 64)
                .targetedBlocks(ModBlockTags.FORGE_DIAMOND_ORES, ModBlockTags.FABRIC_DIAMOND_ORES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("digging_for_emerald"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(16, 32)
                .targetedBlocks(ModBlockTags.FORGE_EMERALD_ORES, ModBlockTags.FABRIC_EMERALD_ORES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("under_da_sea"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(16, 32)
                .targetedBlocks(Blocks.PRISMARINE, Blocks.DARK_PRISMARINE, Blocks.SEA_LANTERN)
                .save(consumer);
        ChallengesBuilder.create(getLocation("nether_time"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(10)
                .targetedBlocks(Blocks.OBSIDIAN)
                .save(consumer);
        ChallengesBuilder.create(getLocation("netherrack"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(128, 350)
                .targetedBlocks(Blocks.NETHERRACK)
                .save(consumer);
        ChallengesBuilder.create(getLocation("do_you_wanna_build_a_laser"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(64, 128)
                .targetedBlocks(ModBlockTags.FORGE_QUARTZ_ORES, ModBlockTags.FABRIC_QUARTZ_ORES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("glowstone"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.GLOWSTONE)
                .save(consumer);
        ChallengesBuilder.create(getLocation("bone_block"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(20, 40)
                .targetedBlocks(Blocks.BONE_BLOCK)
                .save(consumer);
        ChallengesBuilder.create(getLocation("badlands_or_mesa"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(128, 256)
                .targetedBlocks(BlockTags.TERRACOTTA)
                .save(consumer);
        ChallengesBuilder.create(getLocation("blackstone_variants"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.BLACKSTONE, Blocks.GILDED_BLACKSTONE)
                .save(consumer);
        ChallengesBuilder.create(getLocation("this_looks_old_but_good"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(8, 16)
                .targetedBlocks(Blocks.ANCIENT_DEBRIS)
                .save(consumer);
        ChallengesBuilder.create(getLocation("the_end"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(128, 256)
                .targetedBlocks(Blocks.END_STONE)
                .save(consumer);
        ChallengesBuilder.create(getLocation("purrr_blocks"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_cobblestone_hardmode"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(256, 512)
                .targetedBlocks(ModBlockTags.FORGE_STONE, ModBlockTags.FABRIC_STONE)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_obsidian_hardmode"), MiningSkillCardItem.Type.PICKAXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(128, 180)
                .targetedBlocks(ModBlockTags.FORGE_OBSIDIAN, ModBlockTags.FABRIC_OBSIDIAN)
                .save(consumer);
    }

    private void buildAxeChallenges(Consumer<ChallengesBuilder.Result> consumer) {
        ChallengesBuilder.create(getLocation("gathering_logs"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Unlearned)
                .requiredAmount(128)
                .targetedBlocks(BlockTags.LOGS)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_birch"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Unlearned)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.BIRCH_LOG)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_spruce"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Unlearned)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.SPRUCE_LOG)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_bases"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(4, 8)
                .targetedBlocks(Blocks.CRAFTING_TABLE, Blocks.CHEST, Blocks.BARREL)
                .targetedBlocks(BlockTags.BEDS)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_dark_oak"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.DARK_OAK_LOG)
                .save(consumer);
        ChallengesBuilder.create(getLocation("strip_oak"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.STRIP_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(32, 64)
                .targetedBlocks(Blocks.OAK_LOG)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_stripped_oak"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64)
                .targetedBlocks(Blocks.STRIPPED_OAK_LOG)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_ladder"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(16, 32)
                .targetedBlocks(Blocks.LADDER)
                .save(consumer);
        ChallengesBuilder.create(getLocation("strip_dark_oak"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.STRIP_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.DARK_OAK_LOG)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_stripped_dark_oak"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(40, 80)
                .targetedBlocks(Blocks.STRIPPED_DARK_OAK_LOG)
                .save(consumer);
        ChallengesBuilder.create(getLocation("obtain_bookshelf"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(8, 16)
                .targetedBlocks(Blocks.BOOKSHELF)
                .save(consumer);
        ChallengesBuilder.create(getLocation("obtain_composter"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(2, 4)
                .targetedBlocks(Blocks.COMPOSTER)
                .save(consumer);
        ChallengesBuilder.create(getLocation("obtain_melon"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(16, 32)
                .targetedBlocks(Blocks.MELON)
                .save(consumer);
        ChallengesBuilder.create(getLocation("obtain_pumpkin"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(16, 32)
                .targetedBlocks(Blocks.PUMPKIN)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_bamboo"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(16, 32)
                .targetedBlocks(Blocks.BAMBOO)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_oak_hardcore"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(200, 350)
                .targetedBlocks(Blocks.OAK_LOG)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_spruce_hardcore"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(200, 350)
                .targetedBlocks(Blocks.SPRUCE_LOG)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_birch_hardcore"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(200, 350)
                .targetedBlocks(Blocks.BIRCH_LOG)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_jungle_hardcore"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(200, 350)
                .targetedBlocks(Blocks.BIRCH_LOG)
                .save(consumer);
        ChallengesBuilder.create(getLocation("strip_logs_hardcore"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(200, 350)
                .targetedBlocks(BlockTags.LOGS)
                .save(consumer);
        ChallengesBuilder.create(getLocation("obtain_melon_hardcore"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.MELON)
                .save(consumer);
        ChallengesBuilder.create(getLocation("obtain_pumpkin_hardcore"), MiningSkillCardItem.Type.AXE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.PUMPKIN)
                .save(consumer);
    }

    private void buildShovelChallenges(Consumer<ChallengesBuilder.Result> consumer) {
        ChallengesBuilder.create(getLocation("gathering_dirt"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Unlearned)
                .requiredAmount(64, 128)
                .targetedBlocks(BlockTags.DIRT)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_sand"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Unlearned)
                .requiredAmount(64, 128)
                .targetedBlocks(BlockTags.SAND)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_gravel"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Unlearned)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.GRAVEL)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_clay"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.CLAY)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_podzol"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.PODZOL)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_coarse_dirt"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.COARSE_DIRT)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_snow"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(128, 256)
                .targetedBlocks(Blocks.SNOW, Blocks.SNOW_BLOCK)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_soul_sand"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(128, 256)
                .targetedBlocks(Blocks.SOUL_SAND, Blocks.SOUL_SOIL)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_mycelium"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.MYCELIUM)
                .save(consumer);
        ChallengesBuilder.create(getLocation("flatting_dirt"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.FLATTEN_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.DIRT)
                .save(consumer);
        ChallengesBuilder.create(getLocation("flatting_grass"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.FLATTEN_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.GRASS_BLOCK)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_dirt_hardcore"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(128, 256)
                .targetedBlocks(BlockTags.DIRT)
                .save(consumer);
        ChallengesBuilder.create(getLocation("flatting_dirt_hardcore"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.FLATTEN_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.GRASS_BLOCK, Blocks.DIRT)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_red_sand_hardcore"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(128, 256)
                .targetedBlocks(Blocks.RED_SAND)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_sand_hardcore"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(256, 512)
                .targetedBlocks(Blocks.SAND)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_coarse_dirt_hardcore"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(128, 256)
                .targetedBlocks(Blocks.COARSE_DIRT)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_podzol_hardcore"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(128, 256)
                .targetedBlocks(Blocks.PODZOL)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_snow_hardcore"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(256, 512)
                .targetedBlocks(Blocks.SNOW, Blocks.SNOW_BLOCK)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_gravel_hardcore"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(128, 256)
                .targetedBlocks(Blocks.GRAVEL)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_clay_hardcore"), MiningSkillCardItem.Type.SHOVEL)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(128, 256)
                .targetedBlocks(Blocks.CLAY)
                .save(consumer);
    }

    private void buildHoeChallenges(Consumer<ChallengesBuilder.Result> consumer) {
        ChallengesBuilder.create(getLocation("gathering_leaves"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Unlearned)
                .requiredAmount(64, 128)
                .targetedBlocks(BlockTags.LEAVES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("tilling_for_farmland"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.TILLING_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Unlearned)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.DIRT_PATH)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_flowers"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Unlearned)
                .requiredAmount(64, 128)
                .targetedBlocks(BlockTags.FLOWERS)
                .save(consumer);
        ChallengesBuilder.create(getLocation("break_grass"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.GRASS, Blocks.TALL_GRASS)
                .save(consumer);
        ChallengesBuilder.create(getLocation("break_birch_leaves"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.BIRCH_LEAVES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("break_poppy_and_dandelion"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(16, 128)
                .targetedBlocks(Blocks.POPPY, Blocks.DANDELION)
                .save(consumer);
        ChallengesBuilder.create(getLocation("break_jungle_leaves"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Novice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.JUNGLE_LEAVES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_hay_bale"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.HAY_BLOCK)
                .save(consumer);
        ChallengesBuilder.create(getLocation("break_oxeye"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(16, 64)
                .targetedBlocks(Blocks.OXEYE_DAISY)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_kelp"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(16, 128)
                .targetedBlocks(Blocks.KELP)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_dark_oak_leaves_hardcore"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(128, 256)
                .targetedBlocks(Blocks.DARK_OAK_LEAVES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_sculk_sensor"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(4, 16)
                .targetedBlocks(Blocks.SCULK_SENSOR)
                .save(consumer);
        ChallengesBuilder.create(getLocation("break_seagrass"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Apprentice)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.SEAGRASS)
                .save(consumer);
        ChallengesBuilder.create(getLocation("break_shroomlight"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.SHROOMLIGHT)
                .save(consumer);
        ChallengesBuilder.create(getLocation("break_nether_wart_block"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.NETHER_WART_BLOCK)
                .save(consumer);
        ChallengesBuilder.create(getLocation("break_warped_wart_block"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.NETHER_WART_BLOCK)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_nether_wart"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(16, 32)
                .targetedBlocks(Blocks.NETHER_WART)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_sponge"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(16, 32)
                .targetedBlocks(Blocks.SPONGE, Blocks.WET_SPONGE)
                .save(consumer);
        ChallengesBuilder.create(getLocation("tilling_for_farmland_hardcore"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.TILLING_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.DIRT_PATH)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_moss"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(64, 128)
                .targetedBlocks(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET)
                .save(consumer);
        ChallengesBuilder.create(getLocation("gathering_leaves_hardcore"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(256, 512)
                .targetedBlocks(BlockTags.LEAVES)
                .save(consumer);
        ChallengesBuilder.create(getLocation("break_flowers"), MiningSkillCardItem.Type.HOE)
                .forType(ChallengesData.Type.BREAK_BLOCK_CONSUME)
                .forTier(MiningSkillCardItem.Tier.Adept)
                .requiredAmount(256, 512)
                .targetedBlocks(BlockTags.FLOWERS)
                .save(consumer);
    }

    @Override
    public @NotNull String getName() {
        return String.format("%s %s", UltimineAddition.MOD_NAME, super.getName());
    }
}
