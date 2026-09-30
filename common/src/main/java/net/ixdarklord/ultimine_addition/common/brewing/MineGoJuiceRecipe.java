package net.ixdarklord.ultimine_addition.common.brewing;

import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.coolcatcore.api.brewing.IBrewingRecipe;
import net.ixdarklord.coolcatcore.api.brewing.BrewingBuilder;
import net.ixdarklord.coolcatcore.api.event.v1.server.RegisterBrewingRecipesEvent;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.potion.MineGoPotion;
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

public class MineGoJuiceRecipe implements IBrewingRecipe {
    private final Potion input;
    private final Ingredient ingredient;
    private final Potion output;
    private final ItemStack outputStack;

    public MineGoJuiceRecipe(Potion input, Item ingredient, Potion output) {
        this(input, ingredient.getDefaultInstance(), output);
    }

    public MineGoJuiceRecipe(Potion input, ItemStack itemStack, Potion output) {
        this.input = input;
        this.ingredient = Ingredient.of(itemStack.getItem());
        this.output = output;
        this.outputStack = PotionUtils.setPotion(new ItemStack(Items.POTION), output);
    }

    // Always registered: brewing is built once per game on 1.20.1, and the playstyle mode can change while it runs,
    // so the recipes stop matching in the legacy mode instead (isInput).
    public static void register() {
        RegisterBrewingRecipesEvent.EVENT.register(event -> {
            BrewingBuilder builder = event.getBuilder();
            builder.addRecipe(new MineGoJuiceRecipe(Potions.WATER, Items.ENCHANTED_BOOK, Registration.KNOWLEDGE_POTION.get()));

            addTiers(builder, Registration.MINING_SKILL_CARD_PICKAXE.get(), Registration.MINE_GO_JUICE_PICKAXE_POTION.getId());
            addTiers(builder, Registration.MINING_SKILL_CARD_AXE.get(), Registration.MINE_GO_JUICE_AXE_POTION.getId());
            addTiers(builder, Registration.MINING_SKILL_CARD_SHOVEL.get(), Registration.MINE_GO_JUICE_SHOVEL_POTION.getId());
            addTiers(builder, Registration.MINING_SKILL_CARD_HOE.get(), Registration.MINE_GO_JUICE_HOE_POTION.getId());

            for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
                Item item = BuiltInRegistries.ITEM.get(type.getRegistryId());
                MiningSkillCardItem card = item instanceof MiningSkillCardItem ? (MiningSkillCardItem) item : null;
                Potion potion = BuiltInRegistries.POTION.get(MineGoJuiceEffect.getId(type));

                if (card == null || potion == null) continue;
                addTiers(builder, card, MineGoJuiceEffect.getId(type));
            }
        });
    }

    private static void addTiers(BrewingBuilder builder, @NotNull MiningSkillCardItem card, ResourceLocation output) {
        MiningSkillCardItem.Tier[] TIERS = {MiningSkillCardItem.Tier.Novice, MiningSkillCardItem.Tier.Apprentice, MiningSkillCardItem.Tier.Adept};

        for (int i = 0; i < TIERS.length; i++) {
            MiningSkillCardItem.Tier tier = TIERS[i];
            ItemStack itemStack = MiningSkillCardData.createForCreativeTab(card, tier);
            Potion potion = BuiltInRegistries.POTION.get(i > 0 ? new ResourceLocation(output + "_" + (i+1)) : output);
            builder.addRecipe(new MineGoJuiceRecipe(Registration.KNOWLEDGE_POTION.get(), itemStack, potion));
        }
    }

    // Vanilla ingredients only match items now; the potion and card tier checks live in isInput/getOutput.
    @Override
    public @NotNull Ingredient input() {
        return Ingredient.of(Items.POTION);
    }

    @Override
    public @NotNull Ingredient ingredient() {
        return this.ingredient;
    }

    @Override
    public @NotNull ItemStack output() {
        return this.outputStack;
    }

    @Override
    public boolean isInput(@NotNull ItemStack stack) {
        if (PlaystyleModes.isLegacy()) return false;
        if (stack.getItem() instanceof PotionItem) {
            return PotionUtils.getPotion(stack) == this.input;
        }
        return false;
    }

    @Override
    public boolean isIngredient(@NotNull ItemStack ingredient) {
        if (ingredient.getItem() instanceof MiningSkillCardItem item) {
            int tier = item.getData(ingredient).getTier().getValue();
            return this.ingredient.test(ingredient) && (tier > 0 && tier < 4) && item.getData(ingredient).getPotionPoints() > 0;
        }
        return this.ingredient.test(ingredient);
    }

    @Override
    public @NotNull ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack ingredient) {
        if (ingredient.getItem() instanceof MiningSkillCardItem item && this.output instanceof MineGoPotion potion) {
            if (!item.getData(ingredient).getTier().equals(potion.getTier()))
                return ItemStack.EMPTY;
        }

        return this.isInput(input) && this.isIngredient(ingredient)
                ? PotionUtils.setPotion(new ItemStack(Items.POTION), this.output)
                : ItemStack.EMPTY;
    }
}
