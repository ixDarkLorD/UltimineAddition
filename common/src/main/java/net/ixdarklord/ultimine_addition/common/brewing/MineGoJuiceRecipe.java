package net.ixdarklord.ultimine_addition.common.brewing;

import net.ixdarklord.coolcatlib.api.brewing.BrewingBuilder;
import net.ixdarklord.coolcatlib.api.brewing.BrewingRecipe;
import net.ixdarklord.coolcatlib.api.event.v1.server.RegisterBrewingRecipesEvent;
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
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class MineGoJuiceRecipe extends BrewingRecipe {
    private final Holder<Potion> input;
    private final Ingredient ingredient;
    private final Holder<Potion> output;

    public MineGoJuiceRecipe(Holder<Potion> input, Item ingredient, Holder<Potion> output) {
        this(input, ingredient.getDefaultInstance(), output);
    }

    public MineGoJuiceRecipe(Holder<Potion> input, ItemStack itemStack, Holder<Potion> output) {
        super(Ingredient.of(PotionContents.createItemStack(Items.POTION, input)), Ingredient.of(itemStack), PotionContents.createItemStack(Items.POTION, output));
        this.input = input;
        this.ingredient = Ingredient.of(itemStack);
        this.output = output;
    }

    public static void register() {
        RegisterBrewingRecipesEvent.EVENT.register(event -> {
            if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) return;
            BrewingBuilder builder = event.getBuilder();
            builder.addRecipe(new MineGoJuiceRecipe(Potions.WATER, Items.ENCHANTED_BOOK, getHolder(Registration.KNOWLEDGE_POTION.get())));

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

    private static Holder<Potion> getHolder(Potion potion) {
        ResourceLocation location = BuiltInRegistries.POTION.getKey(potion);
        IllegalArgumentException exception = new IllegalArgumentException("unregistered potion: " + potion.getClass().getSimpleName());
        if (location == null) throw exception;
        return BuiltInRegistries.POTION.getHolder(location).orElseThrow(() -> exception);
    }

    private static void addTiers(BrewingBuilder builder, @NotNull MiningSkillCardItem card, ResourceLocation output) {
        MiningSkillCardItem.Tier[] TIERS = {MiningSkillCardItem.Tier.Novice, MiningSkillCardItem.Tier.Apprentice, MiningSkillCardItem.Tier.Adept};

        for (int i = 0; i < TIERS.length; i++) {
            MiningSkillCardItem.Tier tier = TIERS[i];
            ItemStack itemStack = MiningSkillCardData.createForCreativeTab(card, tier);
            Holder<Potion> potion = Registration.POTIONS.getRegistrar().getHolder(i > 0 ? ResourceLocation.parse(output + "_" + (i+1)) : output);
            builder.addRecipe(new MineGoJuiceRecipe(getHolder(Registration.KNOWLEDGE_POTION.get()), itemStack, potion));
        }
    }

    @Override
    public boolean isInput(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof PotionItem) {
            return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().orElse(null) == this.input;
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
        if (ingredient.getItem() instanceof MiningSkillCardItem item && this.output.value() instanceof MineGoPotion potion) {
            if (!item.getData(ingredient).getTier().equals(potion.getTier()))
                return ItemStack.EMPTY;
        }

        return this.isInput(input) && this.isIngredient(ingredient)
                ? PotionContents.createItemStack(Items.POTION, this.output)
                : ItemStack.EMPTY;
    }
}
