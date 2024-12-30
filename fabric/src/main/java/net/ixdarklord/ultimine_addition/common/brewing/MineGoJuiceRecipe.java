package net.ixdarklord.ultimine_addition.common.brewing;

import net.ixdarklord.coolcatlib.api.brewing.fabric.BrewingRecipeRegistry;
import net.ixdarklord.coolcatlib.api.brewing.fabric.IBrewingRecipe;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.potion.MineGoPotion;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.NotNull;

public class MineGoJuiceRecipe implements IBrewingRecipe {
    private final Holder<Potion> input;
    private final ItemStack ingredient;
    private final Holder<Potion> output;

    public MineGoJuiceRecipe(Holder<Potion> input, Item ingredient, Holder<Potion> output) {
        this(input, ingredient.getDefaultInstance(), output);
    }

    public MineGoJuiceRecipe(Holder<Potion> input, ItemStack ingredient, Holder<Potion> output) {
        this.input = input;
        this.ingredient = ingredient;
        this.output = output;
    }

    public static void register() {
        if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) return;
        BrewingRecipeRegistry.addRecipe(new MineGoJuiceRecipe(Potions.WATER, Items.ENCHANTED_BOOK, Registration.KNOWLEDGE_POTION));
        addTiers(Registration.MINING_SKILL_CARD_PICKAXE.get(), Registration.MINE_GO_JUICE_PICKAXE_POTION.getId());
        addTiers(Registration.MINING_SKILL_CARD_AXE.get(), Registration.MINE_GO_JUICE_AXE_POTION.getId());
        addTiers(Registration.MINING_SKILL_CARD_SHOVEL.get(), Registration.MINE_GO_JUICE_SHOVEL_POTION.getId());
        addTiers(Registration.MINING_SKILL_CARD_HOE.get(), Registration.MINE_GO_JUICE_HOE_POTION.getId());

        for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
            Item card = BuiltInRegistries.ITEM.get(type.getRegistryId());
            Potion potion = BuiltInRegistries.POTION.get(MineGoJuiceEffect.getId(type));

            if (card == Items.AIR || potion == null) continue;
            addTiers(card, MineGoJuiceEffect.getId(type));
        }
    }

    private static void addTiers(@NotNull Item item, ResourceLocation output) {
        ItemStack stack = new ItemStack(item);
        var data = MiningSkillCardData.loadData(stack);

        ItemStack tier1 = stack.copy();
        Holder<Potion> potion1 = Registration.POTIONS.getRegistrar().getHolder(output);
        data.setTier(MiningSkillCardItem.Tier.Novice).saveData(tier1);
        BrewingRecipeRegistry.addRecipe(new MineGoJuiceRecipe(Registration.KNOWLEDGE_POTION, tier1, potion1));

        ItemStack tier2 = stack.copy();
        Holder<Potion> potion2 = Registration.POTIONS.getRegistrar().getHolder(ResourceLocation.parse(output + "_2"));
        data.setTier(MiningSkillCardItem.Tier.Apprentice).saveData(tier2);
        BrewingRecipeRegistry.addRecipe(new MineGoJuiceRecipe(Registration.KNOWLEDGE_POTION, tier2, potion2));

        ItemStack tier3 = stack.copy();
        Holder<Potion> potion3 = Registration.POTIONS.getRegistrar().getHolder(ResourceLocation.parse(output + "_3"));
        data.setTier(MiningSkillCardItem.Tier.Adept).saveData(tier3);
        BrewingRecipeRegistry.addRecipe(new MineGoJuiceRecipe(Registration.KNOWLEDGE_POTION, tier3, potion3));
    }

    @Override
    public boolean isInput(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof PotionItem) {
            var holder = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
            if (holder.isPresent()) {
                return holder.get() == this.input;
            }
        }
        return false;
    }

    @Override
    public boolean isIngredient(@NotNull ItemStack ingredient) {
        if (ingredient.getItem() instanceof MiningSkillCardItem item) {
            int tier = item.getData(ingredient).getTier().getValue();
            return this.ingredient.is(ingredient.getItem()) && (tier > 0 && tier < 4) && item.getData(ingredient).getPotionPoints() > 0;
        }
        return false;
    }

    @Override
    public @NotNull ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack ingredient) {
        if (!this.isInput(input) || !this.isIngredient(ingredient))
            return ItemStack.EMPTY;

        if (ingredient.getItem() instanceof MiningSkillCardItem item && this.output instanceof MineGoPotion potion) {
            if (!item.getData(ingredient).getTier().equals(potion.getTier()))
                return ItemStack.EMPTY;
        }

        return PotionContents.createItemStack(Items.POTION, this.output);
    }
}
