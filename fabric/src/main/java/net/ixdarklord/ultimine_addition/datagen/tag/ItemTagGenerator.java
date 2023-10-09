package net.ixdarklord.ultimine_addition.datagen.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends FabricTagProvider.ItemTagProvider {

    public ItemTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        this.getOrCreateTagBuilder(ModItemTags.SLIMEBALLS_FABRIC).add(Items.SLIME_BALL);
        this.getOrCreateTagBuilder(ModItemTags.MINING_SKILL_CARD).add(ModItems.MINING_SKILL_CARD_EMPTY, ModItems.MINING_SKILL_CARD_PICKAXE, ModItems.MINING_SKILL_CARD_AXE, ModItems.MINING_SKILL_CARD_SHOVEL, ModItems.MINING_SKILL_CARD_HOE);
        this.getOrCreateTagBuilder(ModItemTags.MORE_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.BLACK_DYES);
        this.getOrCreateTagBuilder(ModItemTags.MORE_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.RED_DYES);
        this.getOrCreateTagBuilder(ModItemTags.MORE_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.GREEN_DYES);
        this.getOrCreateTagBuilder(ModItemTags.MORE_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.BLUE_DYES);
        this.getOrCreateTagBuilder(ModItemTags.LESS_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.WHITE_DYES);
        this.getOrCreateTagBuilder(ModItemTags.LESS_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.BROWN_DYES);
        this.getOrCreateTagBuilder(ModItemTags.LESS_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.CYAN_DYES);
        this.getOrCreateTagBuilder(ModItemTags.LESS_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.GRAY_DYES);
        this.getOrCreateTagBuilder(ModItemTags.LESS_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.LIGHT_BLUE_DYES);
        this.getOrCreateTagBuilder(ModItemTags.LESS_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.LIGHT_GRAY_DYES);
        this.getOrCreateTagBuilder(ModItemTags.LESS_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.LIME_DYES);
        this.getOrCreateTagBuilder(ModItemTags.LESS_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.MAGENTA_DYES);
        this.getOrCreateTagBuilder(ModItemTags.LESS_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.ORANGE_DYES);
        this.getOrCreateTagBuilder(ModItemTags.LESS_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.PINK_DYES);
        this.getOrCreateTagBuilder(ModItemTags.LESS_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.PURPLE_DYES);
        this.getOrCreateTagBuilder(ModItemTags.LESS_VALUABLE_PIGMENT).forceAddTag(ConventionalItemTags.YELLOW_DYES);
    }
}
