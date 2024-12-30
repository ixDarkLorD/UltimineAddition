package net.ixdarklord.ultimine_addition.datagen.tag;

import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends ItemTagsProvider {
    public ItemTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags) {
        super(output, lookupProvider, blockTags);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(ModItemTags.MINING_SKILL_CARD).add(ModItems.MINING_SKILL_CARD_EMPTY, ModItems.MINING_SKILL_CARD_PICKAXE, ModItems.MINING_SKILL_CARD_AXE, ModItems.MINING_SKILL_CARD_SHOVEL, ModItems.MINING_SKILL_CARD_HOE);
        this.tag(ModItemTags.MORE_VALUABLE_PIGMENT)
                .add(Items.BLACK_DYE, Items.RED_DYE ,Items.GREEN_DYE ,Items.BLUE_DYE)
                .addOptionalTags(Tags.Items.DYES_BLACK, Tags.Items.DYES_RED, Tags.Items.DYES_GREEN, Tags.Items.DYES_BLUE);
        this.tag(ModItemTags.LESS_VALUABLE_PIGMENT)
                .add(Items.WHITE_DYE, Items.BROWN_DYE, Items.CYAN_DYE, Items.GRAY_DYE, Items.LIGHT_BLUE_DYE, Items.LIGHT_GRAY_DYE, Items.LIME_DYE, Items.MAGENTA_DYE, Items.ORANGE_DYE, Items.PINK_DYE, Items.PURPLE_DYE, Items.YELLOW_DYE)
                .addOptionalTags(Tags.Items.DYES_WHITE, Tags.Items.DYES_BROWN, Tags.Items.DYES_CYAN, Tags.Items.DYES_GRAY, Tags.Items.DYES_LIGHT_BLUE, Tags.Items.DYES_LIGHT_GRAY, Tags.Items.DYES_LIME, Tags.Items.DYES_MAGENTA, Tags.Items.DYES_ORANGE, Tags.Items.DYES_PINK, Tags.Items.DYES_PURPLE, Tags.Items.DYES_YELLOW);
    }
}
