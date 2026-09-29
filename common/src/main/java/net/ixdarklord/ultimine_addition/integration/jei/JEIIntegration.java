package net.ixdarklord.ultimine_addition.integration.jei;

import java.util.ArrayList;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.*;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@JeiPlugin
public class JEIIntegration implements IModPlugin {

    @Override
    public @NotNull Identifier getPluginUid() {
        return FTBUltimineAddition.id("jei_integration");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        MiningSkillsCardInterpreter.init(registration);
        registration.registerSubtypeInterpreter(ModItems.PEN, new PenInterpreter());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGlobalGuiHandler(new SkillsRecordScreenHandler());
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ItemStorageDataRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        registration.addRecipes(ItemStorageDataRecipeCategory.RECIPE_TYPE, ItemStorageDataRecipeCategory.getItemStorageDataRecipes());

        final List<MiningSkillCardItem> skillCardItems = List.of(
                ModItems.MINING_SKILL_CARD_PICKAXE,
                ModItems.MINING_SKILL_CARD_AXE,
                ModItems.MINING_SKILL_CARD_SHOVEL,
                ModItems.MINING_SKILL_CARD_HOE
        );

        final List<ItemStack> allCards = skillCardItems.stream()
                .flatMap(item -> Arrays.stream(MiningSkillCardItem.Tier.values())
                        .filter(tier -> tier != MiningSkillCardItem.Tier.Mastered)
                        .map(tier -> MiningSkillCardData.createForCreativeTab(item, tier))
                ).toList();

        final List<ItemStack> masteredCards = skillCardItems.stream()
                .map(item -> MiningSkillCardData.createForCreativeTab(item, MiningSkillCardItem.Tier.Mastered))
                .toList();

        registration.addItemStackInfo(allCards, Component.translatable("jei.ultimine_addition.info.cards.grade_up"));
        registration.addItemStackInfo(masteredCards, Component.translatable("jei.ultimine_addition.info.cards.mastered"));
        for (ShapeCertificateItem certificate : ShapeCertificateItem.all()) {
            List<ItemStack> stacks = new ArrayList<>();
            for (var shape : ShapeCertificateItem.tierList(certificate.getTier())) {
                for (MiningSkillCardItem.Type type : MiningSkillCardItem.Type.TYPES) {
                    if (type != MiningSkillCardItem.Type.EMPTY) stacks.add(certificate.create(type, shape));
                }
            }
            registration.addItemStackInfo(stacks, Component.translatable("jei.ultimine_addition.info.shape_certificate", certificate.getTier().getDisplayName()));
        }
        registration.addItemStackInfo(ModItems.MINING_SKILL_CARD_EMPTY.getDefaultInstance(), Component.translatable("jei.ultimine_addition.info.cards.obtain"));
        registration.addRecipes(RecipeTypes.CRAFTING, recoloringRecipes());
    }

    // The Skills Record's recoloring is a special recipe JEI can't show, so it gets one display recipe per color:
    // a Skills Record (of any color) and a dye make that color's record, keeping what's inside.
    private static List<RecipeHolder<CraftingRecipe>> recoloringRecipes() {
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
        for (DyeColor color : DyeColor.values()) {
            Item dye = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(color.getSerializedName() + "_dye"));
            ItemStackTemplate result = new ItemStackTemplate(ModItems.SKILLS_RECORD,
                    DataComponentPatch.builder().set(DataComponents.BASE_COLOR, color).build());
            CraftingRecipe recipe = new ShapelessRecipe(new Recipe.CommonInfo(false),
                    new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, FTBUltimineAddition.MOD_ID + ":skills_record_dyeing"),
                    result, List.of(Ingredient.of(ModItems.SKILLS_RECORD), Ingredient.of(dye)));
            ResourceKey<Recipe<?>> id = ResourceKey.create(Registries.RECIPE, FTBUltimineAddition.id("jei/skills_record_dyeing/" + color.getSerializedName()));
            recipes.add(new RecipeHolder<>(id, recipe));
        }
        return recipes;
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        ItemStorageDataRecipeCategory.getCatalysts().forEach(stack ->
                registration.addRecipeCatalyst(stack, ItemStorageDataRecipeCategory.RECIPE_TYPE));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new MCRecipeTransferHandler(registration.getTransferHelper()), RecipeTypes.CRAFTING);
    }
}
