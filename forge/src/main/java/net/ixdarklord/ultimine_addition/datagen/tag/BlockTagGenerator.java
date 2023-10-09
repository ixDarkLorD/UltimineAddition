package net.ixdarklord.ultimine_addition.datagen.tag;

import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends BlockTagsProvider {

    public BlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModBlockTags.DENY_IS_PLACED_BY_ENTITY)
                .addTag(Tags.Blocks.CHESTS)
                .addTag(Tags.Blocks.CHESTS_TRAPPED)
                .addTag(BlockTags.BEDS)
                .add(Blocks.SPONGE, Blocks.WET_SPONGE)
                .add(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET)
                .add(Blocks.CRAFTING_TABLE)
                .add(Blocks.HAY_BLOCK)
                .add(Blocks.BARREL)
                .add(Blocks.LADDER)
                .add(Blocks.COMPOSTER)
                .add(Blocks.BOOKSHELF);
    }
}
