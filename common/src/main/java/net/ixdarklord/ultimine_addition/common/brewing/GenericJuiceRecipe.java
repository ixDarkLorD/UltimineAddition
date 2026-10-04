package net.ixdarklord.ultimine_addition.common.brewing;

import net.ixdarklord.coolcatcore.api.brewing.IBrewingRecipe;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.GenericMineGoJuiceItem;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

// Brewing the Mine-Go Juice of a data pack card type: a Knowledge Potion and that type's card (Novice to Adept, with
// potion points left) give the generic juice, carrying the card's type and tier. The built-in tools' juices are
// MineGoJuiceRecipe's, one potion each.
public class GenericJuiceRecipe implements IBrewingRecipe {
    @Override
    public @NotNull Ingredient input() {
        return Ingredient.of(Items.POTION);
    }

    @Override
    public @NotNull Ingredient ingredient() {
        return Ingredient.of(ModItems.MINING_SKILL_CARD_GENERIC);
    }

    @Override
    public @NotNull ItemStack output() {
        return new ItemStack(ModItems.MINE_GO_JUICE_GENERIC);
    }

    @Override
    public boolean isInput(@NotNull ItemStack stack) {
        if (PlaystyleModes.isLegacy() || !(stack.getItem() instanceof PotionItem)) return false;
        return PotionUtils.getPotion(stack) == Registration.KNOWLEDGE_POTION.get();
    }

    @Override
    public boolean isIngredient(@NotNull ItemStack ingredient) {
        if (!(ingredient.getItem() instanceof MiningSkillCardItem item) || !ingredient.is(ModItems.MINING_SKILL_CARD_GENERIC)) return false;
        if (item.getType(ingredient) == MiningSkillCardItem.Type.EMPTY) return false;
        MiningSkillCardData data = item.getData(ingredient);
        int tier = data.getTier().getValue();
        return tier > 0 && tier < 4 && data.getPotionPoints() > 0;
    }

    @Override
    public @NotNull ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack ingredient) {
        if (!this.isInput(input) || !this.isIngredient(ingredient)) return ItemStack.EMPTY;
        MiningSkillCardItem item = (MiningSkillCardItem) ingredient.getItem();
        return GenericMineGoJuiceItem.create(item.getType(ingredient), item.getData(ingredient).getTier());
    }
}
