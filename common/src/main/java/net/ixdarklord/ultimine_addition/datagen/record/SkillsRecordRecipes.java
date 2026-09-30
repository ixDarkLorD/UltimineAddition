package net.ixdarklord.ultimine_addition.datagen.record;

import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The Skills Record's recipes, a clipboard: an iron clip on top, paper around an empty Mining Skill Card, and a board
 * of planks. With plain planks it's the plain (white) record; a dye in the middle of the board makes that color's
 * record. Shared by both loaders' recipe providers. Records can be recolored any number of times with a dye
 * ({@code skills_record_dyeing}).
 * <p>
 * 1.20.1's crafting results can't carry NBT everywhere, so the dyed ones are {@code ultimine_addition:dyed_skills_record}
 * recipes (a shaped recipe with a {@code "color"}) instead of a result with 26.1.2's {@code base_color} component.
 */
public final class SkillsRecordRecipes {
    private SkillsRecordRecipes() {}

    /**
     * @param output the output to save to, with the loader's conditions already applied
     * @param dyeTag the loader's tag of each color's dyes, so other mods' dyes work too
     */
    public static void save(Consumer<FinishedRecipe> output, Function<DyeColor, TagKey<Item>> dyeTag, CriterionTriggerInstance unlock) {
        for (DyeColor dye : DyeColor.values()) {
            boolean plain = dye == DyeColor.WHITE;
            ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.SKILLS_RECORD.get())
                    .define('N', Items.IRON_NUGGET)
                    .define('I', Items.IRON_INGOT)
                    .define('P', Items.PAPER)
                    .define('M', Registration.MINING_SKILL_CARD_EMPTY.get())
                    .define('W', ItemTags.PLANKS)
                    .pattern("NIN")
                    .pattern("PMP")
                    .pattern(plain ? "WWW" : "WDW")
                    .group(FTBUltimineAddition.MOD_ID)
                    .unlockedBy("has_mining_skill_card", unlock);
            if (plain) {
                builder.save(output, FTBUltimineAddition.id("skills_record"));
            } else {
                builder.define('D', dyeTag.apply(dye));
                builder.save(recipe -> output.accept(new Dyed(recipe, dye)), FTBUltimineAddition.id("skills_record_" + dye.getSerializedName()));
            }
        }
    }

    // A vanilla shaped recipe written as a dyed_skills_record one: the same fields, plus the color.
    private record Dyed(FinishedRecipe recipe, DyeColor color) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            this.recipe.serializeRecipeData(json);
            json.addProperty("color", this.color.getSerializedName());
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return this.recipe.getId();
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return Registration.DYED_SKILLS_RECORD_RECIPE_SERIALIZER.get();
        }

        @Override
        public @Nullable JsonObject serializeAdvancement() {
            return this.recipe.serializeAdvancement();
        }

        @Override
        public @Nullable ResourceLocation getAdvancementId() {
            return this.recipe.getAdvancementId();
        }
    }
}
