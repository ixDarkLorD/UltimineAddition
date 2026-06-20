package net.ixdarklord.ultimine_addition.datagen.tag;

import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends BlockTagsProvider {
   public BlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
      super(output, lookupProvider, "ultimine_addition", existingFileHelper);
   }

   protected void addTags(HolderLookup.@NotNull Provider provider) {
      this.tag(ModBlockTags.DENY_IS_PLACED_BY_ENTITY).addTag(Blocks.CHESTS).addTag(Blocks.CHESTS_TRAPPED).addTag(BlockTags.BEDS).add(new Block[]{net.minecraft.world.level.block.Blocks.SPONGE, net.minecraft.world.level.block.Blocks.WET_SPONGE}).add(new Block[]{net.minecraft.world.level.block.Blocks.MOSS_BLOCK, net.minecraft.world.level.block.Blocks.MOSS_CARPET}).add(net.minecraft.world.level.block.Blocks.CRAFTING_TABLE).add(net.minecraft.world.level.block.Blocks.HAY_BLOCK).add(net.minecraft.world.level.block.Blocks.BARREL).add(net.minecraft.world.level.block.Blocks.LADDER).add(net.minecraft.world.level.block.Blocks.COMPOSTER).add(net.minecraft.world.level.block.Blocks.BOOKSHELF);
   }
}
