package net.ixdarklord.ultimine_addition.datagen.tag;

import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class BlockTagGenerator extends BlockTagsProvider {
    public BlockTagGenerator(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
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
