package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.*;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@JeiPlugin
public class JEIIntegration implements IModPlugin {
   public @NotNull ResourceLocation getPluginUid() {
      return FTBUltimineAddition.id("jei_integration");
   }

   public void registerItemSubtypes(@NotNull ISubtypeRegistration registration) {
      MiningSkillsCardInterpreter.init(registration);
      registration.registerSubtypeInterpreter(Registration.PEN.get(), new PenInterpreter());
   }

   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addGlobalGuiHandler(new SkillsRecordScreenHandler());
   }

   public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
      registration.addRecipeCategories(new ItemStorageDataRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
   }

   public void registerRecipes(@NotNull IRecipeRegistration registration) {
      registration.addRecipes(ItemStorageDataRecipeCategory.RECIPE_TYPE, ItemStorageDataRecipeCategory.getItemStorageDataRecipes());
      List<MiningSkillCardItem> skillCardItems = Stream.of(Registration.MINING_SKILL_CARD_PICKAXE, Registration.MINING_SKILL_CARD_AXE, Registration.MINING_SKILL_CARD_SHOVEL, Registration.MINING_SKILL_CARD_HOE).map(Supplier::get).toList();
      List<ItemStack> allCards = skillCardItems.stream().flatMap((item) -> Arrays.stream(MiningSkillCardItem.Tier.values()).filter((tier) -> tier != MiningSkillCardItem.Tier.Mastered).map((tier) -> MiningSkillCardData.createForCreativeTab(item, tier))).toList();
      List<ItemStack> masteredCards = skillCardItems.stream().map((item) -> MiningSkillCardData.createForCreativeTab(item, MiningSkillCardItem.Tier.Mastered)).toList();
      registration.addItemStackInfo(allCards, Component.translatable("jei.ultimine_addition.info.cards.grade_up"));
      registration.addItemStackInfo(masteredCards, Component.translatable("jei.ultimine_addition.info.cards.mastered"));
      ConfigIngredientInfoRecipe.addConfigInfo(registration, Registration.MINING_SKILL_CARD_EMPTY.get().getDefaultInstance(), "jei.ultimine_addition.info.cards.obtain", ConfigHandler.COMMON.VILLAGER_CARD_TRADE_LEVEL);
   }

   public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
      ItemStorageDataRecipeCategory.getCatalysts().forEach((stack) -> registration.addRecipeCatalyst(stack, ItemStorageDataRecipeCategory.RECIPE_TYPE));
   }

   public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
      registration.addRecipeTransferHandler(new MCRecipeTransferHandler(registration.getTransferHelper()), RecipeTypes.CRAFTING);
   }
}
