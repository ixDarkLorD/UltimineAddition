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
