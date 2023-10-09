package net.ixdarklord.ultimine_addition.integration.jei;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.ixdarklord.coolcat_lib.util.MouseHelper;
import net.ixdarklord.coolcat_lib.util.ScreenUtils;
import net.ixdarklord.ultimine_addition.common.data.recipe.DataIngredient;
import net.ixdarklord.ultimine_addition.common.data.recipe.ItemStorageDataRecipe;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.*;

public class ItemStorageDataRecipeCategory implements IRecipeCategory<ItemStorageDataRecipe> {
    public static final ResourceLocation TEXTURES = Constants.getGuiTexture("item_storage_data_recipe_jei", "png");
    public static final RecipeType<ItemStorageDataRecipe> RECIPE_TYPE =
            RecipeType.create(Constants.MOD_ID, ItemStorageDataRecipe.Serializer.NAME.getPath(), ItemStorageDataRecipe.class);
    private final IGuiHelper helper;
    private final IDrawable background;
    private final ITickTimer timer;
    private IDrawable icon;
    private Component title;

    public ItemStorageDataRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.background = helper.createDrawable(TEXTURES, 0, 0, 128, 41);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.BEDROCK));
        this.title = Component.literal("Not Assigned!");
        this.timer = helper.createTickTimer(60, 380, false);
    }

    @NotNull
    public static List<ItemStack> getCatalysts() {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
        List<ItemStorageDataRecipe> recipes = new ArrayList<>(rm.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe instanceof ItemStorageDataRecipe)
                .map(recipe -> (ItemStorageDataRecipe) recipe)
                .toList());
        return recipes.stream().map(ItemStorageDataRecipe::getResultItem).toList();
    }

    public static List<ItemStorageDataRecipe> getItemStorageDataRecipes() {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
        List<ItemStorageDataRecipe> storageDataRecipes = rm.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe instanceof ItemStorageDataRecipe)
                .map(recipe -> (ItemStorageDataRecipe) recipe)
                .toList();

        List<ItemStorageDataRecipe> result = new ArrayList<>();
        storageDataRecipes.forEach(recipe -> recipe.getDataIngredients().forEach(i -> {
            NonNullList<DataIngredient> items = NonNullList.create();
            items.add(DataIngredient.of(i.getAmount(), i.getItems()));
            result.add(new ItemStorageDataRecipe(recipe.getId(), recipe.getGroup(), recipe.getCategory(), recipe.getResultItem(), recipe.getStorageName(), items));
        }));
        return result;
    }

    @Override
    public @NotNull RecipeType<ItemStorageDataRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return this.title;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ItemStorageDataRecipe recipe, @NotNull IFocusGroup focuses) {
        if (this.icon != this.helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.CRAFTING_TABLE.getDefaultInstance())) {
            this.icon = this.helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.CRAFTING_TABLE.getDefaultInstance());
            this.title = Component.translatable(String.format("jei.ultimine_addition.category.item_storage.%s", Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(recipe.getResultItem().getItem())).getPath()));
        }
        List<ItemStack> items = DataIngredient.toNormal(recipe.getDataIngredients()).stream().map(Ingredient::getItems).flatMap(Arrays::stream).toList();
        builder.addSlot(RecipeIngredientRole.INPUT, 9, 5).addItemStack(recipe.getResultItem());
        builder.addSlot(RecipeIngredientRole.INPUT, 103, 5).addIngredients(VanillaTypes.ITEM_STACK, items);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(ItemStorageDataRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        int value = recipeSlotsView.getSlotViews().get(1).getDisplayedItemStack().orElse(ItemStack.EMPTY).getOrCreateTag().getInt("amount");
        Component component = Component.translatable(String.format("jei.ultimine_addition.recipe.item_storage.%s", recipe.getStorageName()), value);
        if (component.getString().length() >= 27 && MouseHelper.isMouseOver(mouseX, mouseY, 3, 28, 121, 12)) {
            return List.of(component);
        }
        return Collections.emptyList();
    }

    @Override
    public void draw(@NotNull ItemStorageDataRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        PoseStack poseStack = guiGraphics.pose();

        // CRAFTING
        poseStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.translate(45.0F, 12.0F + Math.cos(this.timer.getValue() / 20.0F), 100.0F);
        poseStack.translate(8.0F, 8.0F, 0.0F);
        poseStack.scale(1.0F, -1.0F, 1.0F);
        poseStack.scale(16.0F, 16.0F, 16.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(30));
        poseStack.mulPose(Axis.YP.rotationDegrees(45));
        poseStack.mulPose(Axis.ZP.rotationDegrees(1));

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(Blocks.CRAFTING_TABLE.defaultBlockState(), poseStack, bufferSource,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        bufferSource.endBatch();
        poseStack.popPose();

        // PLUS
        poseStack.pushPose();
        poseStack.translate(0.0F, Math.sin(this.timer.getValue() / 20.0F), 120.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        guiGraphics.blit(TEXTURES, 69, 14, 0, 42, 12, 12, 256, 256);
        poseStack.popPose();

        // TEXT
        poseStack.pushPose();
        Font font = Minecraft.getInstance().font;
        int value = recipeSlotsView.getSlotViews().get(1).getDisplayedItemStack().orElse(ItemStack.EMPTY).getOrCreateTag().getInt("amount");
        Component component = ScreenUtils.limitComponent(Component.translatable(String.format("jei.ultimine_addition.recipe.item_storage.%s", recipe.getStorageName()), value), 27);
        guiGraphics.drawString(font, component, 5, 30, Color.WHITE.getRGB());
        poseStack.popPose();
    }
}
