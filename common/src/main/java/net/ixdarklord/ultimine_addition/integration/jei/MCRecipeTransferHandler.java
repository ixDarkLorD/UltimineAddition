package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.transfer.BasicRecipeTransferHandler;
import mezz.jei.common.transfer.BasicRecipeTransferInfo;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.recipe.MCRecipe;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MCRecipeTransferHandler implements IRecipeTransferHandler<CraftingMenu, CraftingRecipe> {
    private final IConnectionToServer serverConnection;
    private final IRecipeTransferHandlerHelper handlerHelper;
    private final IRecipeTransferHandler<CraftingMenu, CraftingRecipe> handler;

    public MCRecipeTransferHandler(IConnectionToServer serverConnection, IStackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper) {
        this.serverConnection = serverConnection;
        this.handlerHelper = handlerHelper;
        var transferInfo = new BasicRecipeTransferInfo<>(CraftingMenu.class, RecipeTypes.CRAFTING, 1, 9, 10, 36);
        this.handler = new BasicRecipeTransferHandler<>(serverConnection, stackHelper, handlerHelper, transferInfo);
    }

    @Override
    public @NotNull Class<CraftingMenu> getContainerClass() {
        return CraftingMenu.class;
    }

    @Override
    public @NotNull Class<CraftingRecipe> getRecipeClass() {
        return CraftingRecipe.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@NotNull CraftingMenu container, @NotNull CraftingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull Player player, boolean maxTransfer, boolean doTransfer) {
        if (!serverConnection.isJeiOnServer()) {
            Component tooltipMessage = new TranslatableComponent("jei.tooltip.error.recipe.transfer.no.server");
            return this.handlerHelper.createUserErrorWithTooltip(tooltipMessage);
        }

        List<IRecipeSlotView> missingItems = this.getMissingItems(container, recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT));
        if (recipe instanceof MCRecipe) {
            if (!missingItems.isEmpty()) {
                MutableComponent tooltipMessage = new TranslatableComponent("jei.tooltip.error.recipe.transfer.missing");
                if (!missingTieredUpMiningCard(container, missingItems).isEmpty())
                    tooltipMessage.append(" ").append(new TranslatableComponent("jei.ultimine_addition.tooltip.missing_card"));
                return this.handlerHelper.createUserErrorForMissingSlots(tooltipMessage, missingItems);
            }
        }
        return this.handler.transferRecipe(container, recipe, recipeSlotsView, player, maxTransfer, doTransfer);
    }

    private List<IRecipeSlotView> getMissingItems(@NotNull CraftingMenu container, List<IRecipeSlotView> slotViews) {
        return slotViews.stream()
                .filter(slotView -> !slotView.isEmpty())
                .filter(slotView -> slotView.getItemStacks().filter(stack -> {
                    if (stack.getItem() instanceof MiningSkillCardItem item) {
                        var data = item.getData(stack);
                        var inv = container.getItems().stream().filter(stack1 -> stack1.is(stack.getItem()) && item.getData(stack1).getTier().equals(data.getTier())).toList();
                        return !inv.isEmpty();
                    } else {
                        var inv = container.getItems().stream().filter(stack1 -> stack1.is(stack.getItem())).toList();
                        return !inv.isEmpty();
                    }
                }).toList().isEmpty())
                .collect(Collectors.toList());
    }

    private List<IRecipeSlotView> missingTieredUpMiningCard(@NotNull CraftingMenu container, List<IRecipeSlotView> slotViews) {
        return slotViews.stream()
                .filter(slotView -> !slotView.isEmpty())
                .filter(slotView -> {
                    List<ItemStack> list = new ArrayList<>(slotView.getItemStacks().toList());
                    list.removeIf(stack -> {
                        MiningSkillCardData data = new MiningSkillCardData().loadData(stack);
                        var inv = container.getItems().stream().filter(stack1 -> stack1.is(stack.getItem()) && !new MiningSkillCardData().loadData(stack1).getTier().equals(data.getTier())).toList();
                        return inv.isEmpty();
                    });
                    return !list.isEmpty();
                })
                .collect(Collectors.toList());
    }
}
