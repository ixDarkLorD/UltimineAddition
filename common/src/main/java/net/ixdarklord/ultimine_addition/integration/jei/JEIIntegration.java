package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.*;
import mezz.jei.common.Internal;
import mezz.jei.common.network.IConnectionToServer;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

@JeiPlugin
public class JEIIntegration implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return Constants.getLocation("jei_integration");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.PEN, new PenInterpreter());
        MiningSkillsCardInterpreter.init(registration);
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
        List<ItemStack> cards = Stream.of(ModItems.MINING_SKILL_CARD_PICKAXE, ModItems.MINING_SKILL_CARD_AXE, ModItems.MINING_SKILL_CARD_SHOVEL, ModItems.MINING_SKILL_CARD_HOE).map(item -> (MiningSkillCardItem)item)
                .map(item -> {
                    ItemStack stack = item.getDefaultInstance();
                    item.getData(stack).setTier(MiningSkillCardItem.Tier.Mastered).saveData(stack);
                    return stack;
                }).toList();

        if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) return;
        registration.addItemStackInfo(cards, new TranslatableComponent("jei.ultimine_addition.info.cards.grade_up"));
        registration.addItemStackInfo(ModItems.MINING_SKILL_CARD_EMPTY.getDefaultInstance(), new TranslatableComponent("jei.ultimine_addition.info.cards.obtain", ConfigHandler.COMMON.CARD_TRADE_LEVEL.get()));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        ItemStorageDataRecipeCategory.getCatalysts().forEach(stack -> registration.addRecipeCatalyst(stack, ItemStorageDataRecipeCategory.RECIPE_TYPE));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        IConnectionToServer serverConnection = Internal.getServerConnection();
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        registration.addRecipeTransferHandler(new MCRecipeTransferHandler(serverConnection, jeiHelpers.getStackHelper(), registration.getTransferHelper()), RecipeTypes.CRAFTING);
    }
}
