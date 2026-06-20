package net.ixdarklord.ultimine_addition.datagen.tag;

import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends ItemTagsProvider {
   public ItemTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagsProvider.TagLookup<Block>> completableFuture2, @Nullable ExistingFileHelper existingFileHelper) {
      super(output, completableFuture, completableFuture2, "ultimine_addition", existingFileHelper);
   }

   protected void addTags(HolderLookup.@NotNull Provider provider) {
      this.tag(ModItemTags.LEGACY_DISABLED_ITEMS).add(ModItems.SKILLS_RECORD, ModItems.PEN, ModItems.INK_CHAMBER).addTag(ModItemTags.MINING_SKILL_CARD);
      this.tag(ModItemTags.MINING_SKILL_CARD).add(ModItems.MINING_SKILL_CARD_EMPTY, ModItems.MINING_SKILL_CARD_PICKAXE, ModItems.MINING_SKILL_CARD_AXE, ModItems.MINING_SKILL_CARD_SHOVEL, ModItems.MINING_SKILL_CARD_HOE);
      this.tag(ModItemTags.MORE_VALUABLE_PIGMENT).add(new Item[]{Items.BLACK_DYE, Items.RED_DYE, Items.GREEN_DYE, Items.BLUE_DYE}).addOptionalTags(net.minecraftforge.common.Tags.Items.DYES_BLACK, net.minecraftforge.common.Tags.Items.DYES_RED, net.minecraftforge.common.Tags.Items.DYES_GREEN, net.minecraftforge.common.Tags.Items.DYES_BLUE);
      this.tag(ModItemTags.LESS_VALUABLE_PIGMENT).add(new Item[]{Items.WHITE_DYE, Items.BROWN_DYE, Items.CYAN_DYE, Items.GRAY_DYE, Items.LIGHT_BLUE_DYE, Items.LIGHT_GRAY_DYE, Items.LIME_DYE, Items.MAGENTA_DYE, Items.ORANGE_DYE, Items.PINK_DYE, Items.PURPLE_DYE, Items.YELLOW_DYE}).addOptionalTags(net.minecraftforge.common.Tags.Items.DYES_WHITE, net.minecraftforge.common.Tags.Items.DYES_BROWN, net.minecraftforge.common.Tags.Items.DYES_CYAN, net.minecraftforge.common.Tags.Items.DYES_GRAY, net.minecraftforge.common.Tags.Items.DYES_LIGHT_BLUE, net.minecraftforge.common.Tags.Items.DYES_LIGHT_GRAY, net.minecraftforge.common.Tags.Items.DYES_LIME, net.minecraftforge.common.Tags.Items.DYES_MAGENTA, net.minecraftforge.common.Tags.Items.DYES_ORANGE, net.minecraftforge.common.Tags.Items.DYES_PINK, net.minecraftforge.common.Tags.Items.DYES_PURPLE, net.minecraftforge.common.Tags.Items.DYES_YELLOW);
   }
}
