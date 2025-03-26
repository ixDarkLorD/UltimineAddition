package net.ixdarklord.ultimine_addition.integration.jei;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.ixdarklord.coolcatlib.api.util.ComponentHelper;
import net.ixdarklord.coolcatlib.api.util.MouseHelper;
import net.ixdarklord.ultimine_addition.common.data.item.ItemStorageData;
import net.ixdarklord.ultimine_addition.common.item.StorageDataAbstractItem;
import net.ixdarklord.ultimine_addition.common.recipe.ItemStorageDataRecipe;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.DataIngredient;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ItemStorageDataRecipeCategory implements IRecipeCategory<ItemStorageDataRecipe> {
    public static final ResourceLocation TEXTURES = FTBUltimineAddition.getGuiTexture("jei/item_storage_data_recipe", "png");
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
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
        List<ItemStorageDataRecipe> recipes = new ArrayList<>(rm.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe.value() instanceof ItemStorageDataRecipe)
                .map(recipe -> (ItemStorageDataRecipe) recipe.value())
                .toList());
        return recipes.stream().map(ItemStorageDataRecipe::getResultItem).toList();
    }

    public static List<ItemStorageDataRecipe> getItemStorageDataRecipes() {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
        List<ItemStorageDataRecipe> recipes = rm.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe.value() instanceof ItemStorageDataRecipe)
                .map(recipe -> (ItemStorageDataRecipe) recipe.value())
                .toList();

        List<ItemStorageDataRecipe> result = new ArrayList<>();
        recipes.forEach(recipe -> recipe.getDataIngredients().forEach(i -> {
            NonNullList<DataIngredient> items = NonNullList.create();
            items.add(DataIngredient.of(i.getAmount(), i.getItems()));
            result.add(new ItemStorageDataRecipe(recipe.getGroup(), recipe.getCategory(), recipe.getResultItem(), recipe.getStorageName(), items));
            if (recipe.getResultItem().getItem() instanceof StorageDataAbstractItem item) {
                ItemStack penStack = recipe.getResultItem().copy();
                ItemStorageData data = item.getData(penStack);
                data.saveData(penStack);
                result.add(new ItemStorageDataRecipe(recipe.getGroup(), recipe.getCategory(), penStack, recipe.getStorageName(), items));
            }
        }));
        return result;
    }

    public static List<RecipeHolder<CraftingRecipe>> getAdjustedCraftingRecipe() {
        ClientLevel level = Minecraft.getInstance().level;
        assert level != null;
        RecipeManager rm = level.getRecipeManager();
        return rm.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING).stream()
                .filter(holder -> holder.value().getResultItem(level.registryAccess()).getItem() instanceof StorageDataAbstractItem)
                .peek(holder -> {
                    ItemStack itemStack = holder.value().getResultItem(level.registryAccess());
                    if (itemStack.getItem() instanceof StorageDataAbstractItem item) {
                        item.getData(itemStack).saveData(itemStack);
                    }
                })
                .toList();
    }

    @Override
    public @NotNull RecipeType<ItemStorageDataRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return this.title;
    }


    @SuppressWarnings("removal")
    @Override
    public @NotNull IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ItemStorageDataRecipe recipe, @NotNull IFocusGroup focuses) {
        if (this.title.equals(Component.literal("Not Assigned!"))) {
            this.title = Component.translatable(String.format("jei.ultimine_addition.category.item_storage.%s", Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(recipe.getResultItem().getItem())).getPath()));
        }
        List<ItemStack> items = DataIngredient.toNormal(recipe.getDataIngredients()).stream().map(Ingredient::getItems).flatMap(Arrays::stream).toList();
        builder.addSlot(RecipeIngredientRole.INPUT, 9, 5).addItemStack(recipe.getResultItem());
        builder.addSlot(RecipeIngredientRole.INPUT, 103, 5).addIngredients(VanillaTypes.ITEM_STACK, items);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ItemStorageDataRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        int value = recipeSlotsView.getSlotViews().get(1).getDisplayedItemStack().orElse(ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("amount");
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
    public void draw(@NotNull ItemStorageDataRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        new AnimatedCrafting(this.timer).draw(guiGraphics, 51, 0);

        guiGraphics.pose().pushPose();
        Font font = Minecraft.getInstance().font;
        int value = recipeSlotsView.getSlotViews().get(1).getDisplayedItemStack().orElse(ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("amount");
        Component component = ComponentHelper.limitComponent(Component.translatable(String.format("jei.ultimine_addition.recipe.item_storage.%s", recipe.getStorageName()), value), 27);
        guiGraphics.drawString(font, component, 5, 30, Color.WHITE.getRGB());
        guiGraphics.pose().popPose();
    }

    private record AnimatedCrafting(@Nullable ITickTimer timer, int maskTop, int maskBottom, int maskLeft,
                                    int maskRight) implements IDrawableStatic {
        public AnimatedCrafting(@Nullable ITickTimer timer) {
            this(timer, 0, 0, 0, 0);
        }

        @Override
        public void draw(GuiGraphics guiGraphics, int x, int y) {
            this.draw(guiGraphics, x, y, this.maskTop, this.maskBottom, this.maskLeft, this.maskRight);
        }

        @Override
        public void draw(GuiGraphics guiGraphics, int x, int y, int maskTop, int maskBottom, int maskLeft, int maskRight) {
            PoseStack poseStack = guiGraphics.pose();

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

            // Crafting
            poseStack.pushPose();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            float dynamicYOffset = (maskedHeight * 20F) / this.getHeight();
            if (this.timer != null) {
                poseStack.translate(adjustedX, adjustedY + dynamicYOffset + Math.cos(this.timer.getValue() / 20.0F), 100);
            } else {
                poseStack.translate(adjustedX, adjustedY + dynamicYOffset, 100);
            }

            poseStack.scale(scale, -scale, scale);
            poseStack.mulPose(Axis.XP.rotationDegrees(30));
            poseStack.mulPose(Axis.YP.rotationDegrees(45));

            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            dispatcher.renderSingleBlock(Blocks.CRAFTING_TABLE.defaultBlockState(), poseStack, bufferSource,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            bufferSource.endBatch();
            poseStack.popPose();

            // Plus Symbol
            poseStack.pushPose();
            if (this.timer != null) {
                poseStack.translate(adjustedX, adjustedY + Math.sin(this.timer.getValue() / 20.0F), 120);
            } else {
                poseStack.translate(adjustedX, adjustedY, 120);
            }

            float plusScaleX = ((float) maskedWidth / this.getWidth()) - 1.0F;
            float plusScaleY = ((float) maskedHeight / this.getHeight()) - 1.0F;
            float plusScale = Math.min(plusScaleX, plusScaleY);
            poseStack.scale(1.0F + plusScale, 1.0F + plusScale, 1.0F);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            guiGraphics.blit(TEXTURES, 14, 14, 0, 42, 12, 12, 256, 256);
            poseStack.popPose();
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
