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
    private final Ingredient ingredient;
    private final Potion output;

    public MineGoJuiceRecipe(Potion input, Item ingredient, Potion output) {
        this(input, ingredient.getDefaultInstance(), output);
    }

    public MineGoJuiceRecipe(Potion input, ItemStack itemStack, Potion output) {
        super(Ingredient.of(createPotion(input)), Ingredient.of(itemStack), createPotion(output));
        this.input = input;
        this.ingredient = Ingredient.of(itemStack);
        this.output = output;
    }

    public static void register() {
        RegisterBrewingRecipesEvent.EVENT.register((event) -> {
            if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() != PlaystyleMode.LEGACY) {
                BrewingBuilder builder = event.getBuilder();
                builder.addRecipe(new MineGoJuiceRecipe(Potions.WATER, Items.ENCHANTED_BOOK, Registration.KNOWLEDGE_POTION.get()));
                addTiers(builder, Registration.MINING_SKILL_CARD_PICKAXE.get(), Registration.MINE_GO_JUICE_PICKAXE_POTION.getId());
                addTiers(builder, Registration.MINING_SKILL_CARD_AXE.get(), Registration.MINE_GO_JUICE_AXE_POTION.getId());
                addTiers(builder, Registration.MINING_SKILL_CARD_SHOVEL.get(), Registration.MINE_GO_JUICE_SHOVEL_POTION.getId());
                addTiers(builder, Registration.MINING_SKILL_CARD_HOE.get(), Registration.MINE_GO_JUICE_HOE_POTION.getId());

                for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
                    Item item = BuiltInRegistries.ITEM.get(type.getRegistryId());
                    MiningSkillCardItem card = item instanceof MiningSkillCardItem ? (MiningSkillCardItem) item : null;
                    Potion potion = Registration.POTIONS.getRegistrar().get(MineGoJuiceEffect.getId(type));
                    if (card != null && potion != null) {
                        addTiers(builder, card, MineGoJuiceEffect.getId(type));
                    }
                }

            }
        });
    }

    private static void addTiers(BrewingBuilder builder, @NotNull MiningSkillCardItem card, ResourceLocation output) {
        MiningSkillCardItem.Tier[] TIERS = new MiningSkillCardItem.Tier[]{MiningSkillCardItem.Tier.Novice, MiningSkillCardItem.Tier.Apprentice, MiningSkillCardItem.Tier.Adept};

        for (int i = 0; i < TIERS.length; ++i) {
            MiningSkillCardItem.Tier tier = TIERS[i];
            ItemStack itemStack = MiningSkillCardData.createForCreativeTab(card, tier);
            ResourceLocation id = i > 0 ? new ResourceLocation(output + "_" + (i + 1)) : output;
            Potion potion = Registration.POTIONS.getRegistrar().get(id);
            if (potion == null) {
                throw new IllegalArgumentException("There is not potion with id: \"%s\"".formatted(id));
            }

            builder.addRecipe(new MineGoJuiceRecipe(Registration.KNOWLEDGE_POTION.get(), itemStack, potion));
        }

    }

    public boolean isInput(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof PotionItem) {
            return PotionUtils.getPotion(stack) == this.input;
        } else {
            return false;
        }
    }

    public boolean isIngredient(@NotNull ItemStack ingredient) {
        Item item2 = ingredient.getItem();
        if (!(item2 instanceof MiningSkillCardItem item)) {
            return this.ingredient.test(ingredient);
        } else {
            int tier = item.getData(ingredient).getTier().getValue();
            return this.ingredient.test(ingredient) && tier > 0 && tier < 4 && item.getData(ingredient).getPotionPoints() > 0;
        }
    }

    public @NotNull ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack ingredient) {
        if (ingredient.getItem() instanceof MiningSkillCardItem item) {
            if (this.output instanceof MineGoPotion potion) {
                if (!item.getData(ingredient).getTier().equals(potion.getTier())) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return this.isInput(input) && this.isIngredient(ingredient) ? createPotion(this.output) : ItemStack.EMPTY;
    }

    private static @NotNull ItemStack createPotion(Potion potion) {
        return PotionUtils.setPotion(Items.POTION.getDefaultInstance(), potion);
    }
}
