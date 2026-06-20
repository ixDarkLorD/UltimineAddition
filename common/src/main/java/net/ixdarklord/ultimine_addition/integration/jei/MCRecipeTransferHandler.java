package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.recipe.MCRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MCRecipeTransferHandler implements IRecipeTransferHandler<CraftingMenu, CraftingRecipe> {
   private final IRecipeTransferHandlerHelper handlerHelper;
   private final IRecipeTransferHandler<CraftingMenu, CraftingRecipe> handler;

   public MCRecipeTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
      this.handlerHelper = handlerHelper;
      IRecipeTransferInfo<CraftingMenu, CraftingRecipe> basicRecipeTransferInfo = handlerHelper.createBasicRecipeTransferInfo(CraftingMenu.class, MenuType.CRAFTING, RecipeTypes.CRAFTING, 1, 9, 10, 36);
      this.handler = handlerHelper.createUnregisteredRecipeTransferHandler(basicRecipeTransferInfo);
   }

   public @NotNull Class<? extends CraftingMenu> getContainerClass() {
      return this.handler.getContainerClass();
   }

   public @NotNull Optional<MenuType<CraftingMenu>> getMenuType() {
      return this.handler.getMenuType();
   }

   public @NotNull RecipeType<CraftingRecipe> getRecipeType() {
      return RecipeTypes.CRAFTING;
   }

   public @Nullable IRecipeTransferError transferRecipe(@NotNull CraftingMenu container, @NotNull CraftingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull Player player, boolean maxTransfer, boolean doTransfer) {
      if (!this.handlerHelper.recipeTransferHasServerSupport()) {
         Component tooltipMessage = Component.translatable("jei.tooltip.error.recipe.transfer.no.server");
         return this.handlerHelper.createUserErrorWithTooltip(tooltipMessage);
      } else {
         List<IRecipeSlotView> missingItems = this.getMissingItems(container, recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT));
         if (recipe instanceof MCRecipe && !missingItems.isEmpty()) {
            MutableComponent tooltipMessage = Component.translatable("jei.tooltip.error.recipe.transfer.missing");
            if (!this.missingTieredUpMiningCard(container, missingItems).isEmpty()) {
               tooltipMessage.append(" ").append(Component.translatable("jei.ultimine_addition.tooltip.missing_card"));
            }

            return this.handlerHelper.createUserErrorForMissingSlots(tooltipMessage, missingItems);
         } else {
            return this.handler.transferRecipe(container, recipe, recipeSlotsView, player, maxTransfer, doTransfer);
         }
      }
   }

   private List<IRecipeSlotView> getMissingItems(@NotNull CraftingMenu container, List<IRecipeSlotView> slotViews) {
      return slotViews.stream().filter((slotView) -> !slotView.isEmpty()).filter((slotView) -> slotView.getItemStacks().filter((stack) -> {
         Item item2 = stack.getItem();
         if (item2 instanceof MiningSkillCardItem item) {
            MiningSkillCardData data = item.getData(stack);
            List<ItemStack> inv = container.getItems().stream().filter((stack1) -> stack1.is(stack.getItem()) && item.getData(stack1).getTier().equals(data.getTier())).toList();
            return !inv.isEmpty();
         } else {
            List<ItemStack> inv = container.getItems().stream().filter((stack1) -> stack1.is(stack.getItem())).toList();
            return !inv.isEmpty();
         }
      }).toList().isEmpty()).collect(Collectors.toList());
   }

   private List<IRecipeSlotView> missingTieredUpMiningCard(@NotNull CraftingMenu container, List<IRecipeSlotView> slotViews) {
      return slotViews.stream().filter((slotView) -> !slotView.isEmpty()).filter((slotView) -> {
         List<ItemStack> list = new ArrayList<>(slotView.getItemStacks().toList());
         list.removeIf((stack) -> {
            MiningSkillCardData data = MiningSkillCardData.load(stack);
            List<ItemStack> inv = container.getItems().stream().filter((stack1) -> stack1.is(stack.getItem()) && !MiningSkillCardData.load(stack1).getTier().equals(data.getTier())).toList();
            return inv.isEmpty();
         });
         return !list.isEmpty();
      }).collect(Collectors.toList());
   }
}
