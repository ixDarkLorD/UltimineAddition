package net.ixdarklord.ultimine_addition.integration.jei;

import net.minecraft.core.registries.BuiltInRegistries;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.Items;
import mezz.jei.common.Internal;
import org.joml.Matrix3x2fStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.ixdarklord.coolcatcore.api.client.utils.MouseHelper;
import net.ixdarklord.coolcatcore.api.utils.ComponentHelper;
import net.ixdarklord.ultimine_addition.common.data.item.StorageItemData;
import net.ixdarklord.ultimine_addition.common.item.StorageItem;
import net.ixdarklord.ultimine_addition.common.recipe.ItemStorageDataRecipe;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.DataIngredient;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ItemStorageDataRecipeCategory implements IRecipeCategory<ItemStorageDataRecipe> {
    public static final Identifier TEXTURES = FTBUltimineAddition.getGuiTexture("jei/item_storage_data_recipe", "png");
    public static final RecipeType<ItemStorageDataRecipe> RECIPE_TYPE =
            RecipeType.create(FTBUltimineAddition.MOD_ID, "item_storage_data", ItemStorageDataRecipe.class);
    private final IDrawable background;
    private final ITickTimer timer;
    private Component title;

    public ItemStorageDataRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURES, 0, 0, 128, 41);
        this.timer = helper.createTickTimer(60, 380, false);
        this.title = Component.literal("Not Assigned!");
    }

    @NotNull
    public static List<ItemStack> getCatalysts() {
        // The client no longer has a RecipeManager; JEI keeps the recipes synced from the server.
        List<ItemStorageDataRecipe> recipes = new ArrayList<>(Internal.getClientSyncedRecipes().byType(net.minecraft.world.item.crafting.RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe.value() instanceof ItemStorageDataRecipe)
                .map(recipe -> (ItemStorageDataRecipe) recipe.value())
                .toList());
        return recipes.stream().map(ItemStorageDataRecipe::getResultItem).toList();
    }

    public static List<ItemStorageDataRecipe> getItemStorageDataRecipes() {
        List<ItemStorageDataRecipe> recipes = Internal.getClientSyncedRecipes().byType(net.minecraft.world.item.crafting.RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe.value() instanceof ItemStorageDataRecipe)
                .map(recipe -> (ItemStorageDataRecipe) recipe.value())
                .toList();

        List<ItemStorageDataRecipe> result = new ArrayList<>();
        recipes.forEach(recipe -> recipe.getDataIngredients().forEach(i -> {
            NonNullList<DataIngredient> items = NonNullList.create();
            items.add(DataIngredient.of(i.getAmount(), i.getItems()));
            result.add(new ItemStorageDataRecipe(recipe.getGroup(), recipe.getCategory(), recipe.getResultItem(), recipe.getStorageName(), items));
            if (recipe.getResultItem().getItem() instanceof StorageItem item) {
                ItemStack penStack = recipe.getResultItem().copy();
                StorageItemData data = item.getData(penStack);
                data.save();
                result.add(new ItemStorageDataRecipe(recipe.getGroup(), recipe.getCategory(), penStack, recipe.getStorageName(), items));
            }
        }));
        return result;
    }

    @Override
    public @NotNull IRecipeType<ItemStorageDataRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return this.background.getWidth();
    }

    @Override
    public int getHeight() {
        return this.background.getHeight();
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ItemStorageDataRecipe recipe, @NotNull IFocusGroup focuses) {
        if (this.title.equals(Component.literal("Not Assigned!"))) {
            this.title = Component.translatable(String.format("jei.ultimine_addition.category.item_storage.%s", Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(recipe.getResultItem().getItem())).getPath()));
        }
        List<ItemStack> items = DataIngredient.toDisplayStacks(recipe.getDataIngredients());
        builder.addSlot(RecipeIngredientRole.INPUT, 9, 5).addItemStack(recipe.getResultItem());
        builder.addSlot(RecipeIngredientRole.INPUT, 103, 5).addIngredients(VanillaTypes.ITEM_STACK, items);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ItemStorageDataRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        int value = recipeSlotsView.getSlotViews().get(1).getDisplayedItemStack().orElse(ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("amount", 0);
        Component component = Component.translatable(String.format("jei.ultimine_addition.recipe.item_storage.%s", recipe.getStorageName()), value);
        if (component.getString().length() >= 27 && MouseHelper.isMouseOver(mouseX, mouseY, 3, 28, 121, 12)) {
            tooltip.add(component);
        }
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return new AnimatedCrafting(null, 5, 5, 5, 5);
    }

    @Override
    public void draw(@NotNull ItemStorageDataRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
        new AnimatedCrafting(this.timer).draw(guiGraphics, 51, 0);

        guiGraphics.pose().pushMatrix();
        Font font = Minecraft.getInstance().font;
        int value = recipeSlotsView.getSlotViews().get(1).getDisplayedItemStack().orElse(ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("amount", 0);
        Component component = ComponentHelper.limitComponent(Component.translatable(String.format("jei.ultimine_addition.recipe.item_storage.%s", recipe.getStorageName()), value), 27);
        guiGraphics.text(font, component, 5, 30, Color.WHITE.getRGB());
        guiGraphics.pose().popMatrix();
    }

    private record AnimatedCrafting(@Nullable ITickTimer timer, int maskTop, int maskBottom, int maskLeft,
                                    int maskRight) implements IDrawableStatic {
        public AnimatedCrafting(@Nullable ITickTimer timer) {
            this(timer, 0, 0, 0, 0);
        }

        @Override
        public void draw(GuiGraphicsExtractor guiGraphics, int x, int y) {
            this.draw(guiGraphics, x, y, this.maskTop, this.maskBottom, this.maskLeft, this.maskRight);
        }

        @Override
        public void draw(GuiGraphicsExtractor guiGraphics, int x, int y, int maskTop, int maskBottom, int maskLeft, int maskRight) {
            Matrix3x2fStack poseStack = guiGraphics.pose();

            if (maskLeft == 0 && maskRight == 0 && maskTop == 0 && maskBottom == 0) {
                maskLeft = this.maskLeft;
                maskRight = this.maskRight;
                maskTop = this.maskTop;
                maskBottom = this.maskBottom;
            }

            int maskedWidth = this.getWidth() - maskLeft - maskRight;
            int maskedHeight = this.getHeight() - maskTop - maskBottom;
            if (maskedWidth <= 0 || maskedHeight <= 0) {
                return;
            }

            float baseSize = 16;
            float scaleX = ((float) maskedWidth * baseSize) / this.getWidth();
            float scaleY = ((float) maskedHeight * baseSize) / this.getHeight();
            float scale = Math.min(scaleX, scaleY);

            int adjustedX = x + maskLeft;
            int adjustedY = y + maskTop;

            // Crafting table (block rendering in GUIs is gone; draw its item icon, bobbing when animated)
            poseStack.pushMatrix();
            float itemScale = scale / 16.0F;
            float bob = this.timer != null ? (float) Math.cos(this.timer.getValue() / 20.0F) : 0.0F;
            poseStack.translate(adjustedX + (maskedWidth - 16 * itemScale) / 2.0F, adjustedY + (maskedHeight - 16 * itemScale) / 2.0F + bob);
            poseStack.scale(itemScale, itemScale);
            guiGraphics.fakeItem(new ItemStack(Items.CRAFTING_TABLE), 0, 0);
            poseStack.popMatrix();

            // Plus Symbol
            poseStack.pushMatrix();
            // Keep the plus sign above the crafting table.
            guiGraphics.nextStratum();
            if (this.timer != null) {
                poseStack.translate(adjustedX, adjustedY + (float) Math.sin(this.timer.getValue() / 20.0F));
            } else {
                poseStack.translate(adjustedX, adjustedY);
            }

            float plusScaleX = ((float) maskedWidth / this.getWidth()) - 1.0F;
            float plusScaleY = ((float) maskedHeight / this.getHeight()) - 1.0F;
            float plusScale = Math.min(plusScaleX, plusScaleY);
            poseStack.scale(1.0F + plusScale, 1.0F + plusScale);

            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, 14, 14, 0.0F, 42.0F, 12, 12, 256, 256);
            poseStack.popMatrix();
        }

        @Override
        public int getWidth() {
            return 26;
        }

        @Override
        public int getHeight() {
            return 26;
        }
    }
}
