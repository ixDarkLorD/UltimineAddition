package net.ixdarklord.ultimine_addition.data.gen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.ixdarklord.ultimine_addition.item.ItemsList;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class RecipeProvider extends FabricRecipeProvider {
    public RecipeProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<FinishedRecipe> exporter) {
        ShapedRecipeBuilder.shaped(ItemsList.MINER_CERTIFICATE)
                .define('P', Items.PAPER)
                .define('1', Items.DIAMOND_PICKAXE).define('2', Items.IRON_AXE)
                .define('3', Items.GOLDEN_HOE).define('4', Items.STONE_SHOVEL)
                .pattern(" 1 ")
                .pattern("2P3")
                .pattern(" 4 ")
                .unlockedBy("has_miner_certificate", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ItemsList.MINER_CERTIFICATE).build()))
                .save(exporter);
    }
}
