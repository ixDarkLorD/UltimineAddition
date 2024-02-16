package net.ixdarklord.ultimine_addition.datagen.tag;

import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends ItemTagsProvider {

    public ItemTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagLookup<Block>> completableFuture2, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, completableFuture, completableFuture2, UltimineAddition.MOD_ID, existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(ModItemTags.MINING_SKILL_CARD).add(ModItems.MINING_SKILL_CARD_EMPTY, ModItems.MINING_SKILL_CARD_PICKAXE, ModItems.MINING_SKILL_CARD_AXE, ModItems.MINING_SKILL_CARD_SHOVEL, ModItems.MINING_SKILL_CARD_HOE);
        this.tag(ModItemTags.MORE_VALUABLE_PIGMENT).addTags(Tags.Items.DYES_BLACK, Tags.Items.DYES_RED, Tags.Items.DYES_GREEN, Tags.Items.DYES_BLUE);
        this.tag(ModItemTags.LESS_VALUABLE_PIGMENT).addTags(Tags.Items.DYES_WHITE, Tags.Items.DYES_BROWN, Tags.Items.DYES_CYAN, Tags.Items.DYES_GRAY, Tags.Items.DYES_LIGHT_BLUE, Tags.Items.DYES_LIGHT_GRAY, Tags.Items.DYES_LIME, Tags.Items.DYES_MAGENTA, Tags.Items.DYES_ORANGE, Tags.Items.DYES_PINK, Tags.Items.DYES_PURPLE, Tags.Items.DYES_YELLOW);
    }
}
