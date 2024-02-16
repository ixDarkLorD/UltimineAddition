package net.ixdarklord.ultimine_addition.common.brewing;

import net.ixdarklord.coolcat_lib.common.brewing.fabric.BrewingRecipe;
import net.ixdarklord.coolcat_lib.common.brewing.fabric.BrewingRecipeRegistry;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.potion.MineGoPotion;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class MineGoJuiceRecipe extends BrewingRecipe {
    private final Potion input;
    private final Potion output;

    public MineGoJuiceRecipe(Potion input, Item ingredient, Potion output) {
        super(Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), input)), Ingredient.of(ingredient), PotionUtils.setPotion(new ItemStack(Items.POTION), output));
        this.input = input;
        this.output = output;
    }
    public MineGoJuiceRecipe(Potion input, ItemStack ingredient, Potion output) {
        super(Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), input)), Ingredient.of(ingredient), PotionUtils.setPotion(new ItemStack(Items.POTION), output));
        this.input = input;
        this.output = output;
    }

    public static void register() {
        if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) return;
        BrewingRecipeRegistry.addRecipe(new MineGoJuiceRecipe(Potions.WATER, Items.ENCHANTED_BOOK, Registration.KNOWLEDGE_POTION.get()));
        addTiers(Registration.MINING_SKILL_CARD_PICKAXE.get(), Registration.MINE_GO_JUICE_PICKAXE_POTION.getId());
        addTiers(Registration.MINING_SKILL_CARD_AXE.get(), Registration.MINE_GO_JUICE_AXE_POTION.getId());
        addTiers(Registration.MINING_SKILL_CARD_SHOVEL.get(), Registration.MINE_GO_JUICE_SHOVEL_POTION.getId());
        addTiers(Registration.MINING_SKILL_CARD_HOE.get(), Registration.MINE_GO_JUICE_HOE_POTION.getId());

        for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
            String cardName = "mining_skill_card_%s".formatted(type.getId());
            String potionName = "mine_go_juice_%s".formatted(type.getId());
            Item customCard = Registration.ITEMS.getRegistrar().get(UltimineAddition.getLocation(cardName));
            Potion customPotion = Registration.POTIONS.getRegistrar().get(UltimineAddition.getLocation(potionName));

            if (customCard == null || customPotion == null) continue;
            addTiers(customCard, UltimineAddition.getLocation(potionName));
        }
    }

    private static void addTiers(@NotNull Item item, ResourceLocation output) {
        ItemStack stack = new ItemStack(item);
        var data = new MiningSkillCardData().loadData(stack);

        ItemStack tier1 = stack.copy();
        Potion potion1 = BuiltInRegistries.POTION.get(output);
        data.setTier(MiningSkillCardItem.Tier.Novice).saveData(tier1);
        BrewingRecipeRegistry.addRecipe(new MineGoJuiceRecipe(Registration.KNOWLEDGE_POTION.get(), tier1, potion1));

        ItemStack tier2 = stack.copy();
        Potion potion2 = BuiltInRegistries.POTION.get(new ResourceLocation(output + "_2"));
        data.setTier(MiningSkillCardItem.Tier.Apprentice).saveData(tier2);
        BrewingRecipeRegistry.addRecipe(new MineGoJuiceRecipe(Registration.KNOWLEDGE_POTION.get(), tier2, potion2));

        ItemStack tier3 = stack.copy();
        Potion potion3 = BuiltInRegistries.POTION.get(new ResourceLocation(output + "_3"));
        data.setTier(MiningSkillCardItem.Tier.Adept).saveData(tier3);
        BrewingRecipeRegistry.addRecipe(new MineGoJuiceRecipe(Registration.KNOWLEDGE_POTION.get(), tier3, potion3));
    }

    @Override
    public boolean isInput(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof PotionItem) {
            return PotionUtils.getPotion(stack) == this.input;
        }
        return super.isInput(stack);
    }

    @Override
    public boolean isIngredient(@NotNull ItemStack ingredient) {
        if (ingredient.getItem() instanceof MiningSkillCardItem item) {
            int tier = item.getData(ingredient).getTier().getValue();
            return getIngredient().test(ingredient) && (tier > 0 && tier < 4) && item.getData(ingredient).getPotionPoints() > 0;
        }
        return super.isIngredient(ingredient);
    }

    @Override
    public @NotNull ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack ingredient) {
        if (!this.isInput(input) || !this.isIngredient(ingredient))
            return ItemStack.EMPTY;

        if (ingredient.getItem() instanceof MiningSkillCardItem item && this.output instanceof MineGoPotion potion) {
            if (!item.getData(ingredient).getTier().equals(potion.getTier()))
                return ItemStack.EMPTY;
        }

        return getOutput().copy();
    }
}
