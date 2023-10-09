package net.ixdarklord.ultimine_addition.client.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.ixdarklord.coolcat_lib.client.button.CustomImageButton;
import net.ixdarklord.coolcat_lib.client.components.TextScreen;
import net.ixdarklord.coolcat_lib.util.ColorUtils;
import net.ixdarklord.coolcat_lib.util.MathUtils;
import net.ixdarklord.coolcat_lib.util.MouseHelper;
import net.ixdarklord.coolcat_lib.util.ScreenUtils;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.container.SkillsRecordContainer;
import net.ixdarklord.ultimine_addition.common.container.slot.CustomSlot;
import net.ixdarklord.ultimine_addition.common.container.slot.MiningSkillCardSlot;
import net.ixdarklord.ultimine_addition.common.container.slot.PaperSlot;
import net.ixdarklord.ultimine_addition.common.container.slot.PenSlot;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SkillsRecordScreen extends AbstractContainerScreen<SkillsRecordContainer> {
    private final ResourceLocation OPTIONS_TEXTURE = Constants.getGuiTexture("skills_record_options", "png");
    private final ResourceLocation TEXTURE = Constants.getGuiTexture("skills_record", "png");
    protected final SkillsRecordContainer container;
    private TextScreen textScreen;
    private final int maxProgress = 100;
    private final List<Button> optionsButtonList = new ArrayList<>();
    private final List<Double> optionsX = new ArrayList<>(List.of(0.0, 0.0));
    private final List<Double> optionsY = new ArrayList<>(List.of(0.0, 0.0));
    private BGColor backgroundColor;
    private float time;
    private float click;
    private int progressMode;
    private int selectedSlot;
    //    private int selectedZone;
    private boolean isChallengesExists;
    private boolean isMissingItems;
    private boolean notEnoughInk;
    private int currentProgress;
    private CustomImageButton configurationButton;
    private Button backgroundColorButton;
    private Button animationsButton;
    private Button progressionBarButton;
    private boolean isScrolling;
    private boolean isOptionsShown;
    private boolean isDraggingWindow;
    private boolean isAnimationsEnabled;
    private boolean isProgressionBarShown;

    public SkillsRecordScreen(SkillsRecordContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        this.imageHeight = 218;
        this.titleLabelX = 8;
        this.titleLabelY = 5;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 92;

        this.time = 0;
        this.click = 0;
        this.selectedSlot = -1;
        this.currentProgress = 0;
        this.backgroundColor = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
        this.isAnimationsEnabled = ConfigHandler.CLIENT.ANIMATIONS_MODE.get();
        this.progressMode = ConfigHandler.CLIENT.PROGRESS_BAR.get();
    }

    @Override
    protected void init() {
        super.init();
        this.textScreen = new TextScreen(leftPos+10, topPos+17, 157, 82, ConfigHandler.CLIENT.TEXT_SCREEN_SHADOW.get()).build(2);
        this.resetOptionsWindow();
        createButtons(this.leftPos, this.topPos);
    }

    private void createButtons(int x, int y) {
        this.configurationButton = this.addRenderableWidget(new CustomImageButton(x+160, y+4, 9, 9, 177, 0, 9, TEXTURE, 256, 256, button -> {
            this.isOptionsShown ^= true;
            this.resetOptionsWindow();
        }, (button, poseStack, i, j) -> {
            Component component = Component.translatable("gui.ultimine_addition.skills_record.configuration");
            if (button.isActive() && !this.isOptionsShown)
                this.renderTooltip(poseStack, Component.literal("➤ ").withStyle(ChatFormatting.GRAY).append(component).withStyle(ChatFormatting.ITALIC), i, j);
        }, CommonComponents.EMPTY));

        double X = this.optionsX.get(0);
        double Y = this.optionsY.get(0);
        this.backgroundColorButton = this.addRenderableWidget(new ImageButton((int) (X+9), (int) (Y+29), 100, 10, 0, 0, 0, TEXTURE, 0, 0,
        button -> {
            if (!Screen.hasShiftDown()) this.backgroundColor = this.backgroundColor.next();
            else this.backgroundColor = this.backgroundColor.previous();
            this.configurationButton.setColor(this.backgroundColor.color);
            this.saveValuesToConfig();
        }, Component.translatable("gui.ultimine_addition.skills_record.option.bg_color")));

        this.animationsButton = this.addRenderableWidget(new ImageButton((int) (X+9), (int) (Y+42), 100, 10, 0, 0, 0, TEXTURE, 0, 0,
        button -> {
            this.isAnimationsEnabled ^= true;
            this.saveValuesToConfig();
        }, Component.translatable("gui.ultimine_addition.skills_record.option.animations")));

        this.progressionBarButton = this.addRenderableWidget(new ImageButton((int) (X+9), (int) (Y+55), 100, 10, 0, 0, 0, TEXTURE, 0, 0,
        button -> {
            this.progressMode = (this.progressMode >= 2) ? 0 : this.progressMode + 1;
            if (this.progressMode == 1) this.isProgressionBarShown = false;
            this.saveValuesToConfig();
        }, Component.translatable("gui.ultimine_addition.skills_record.option.progression_bar")));

        this.optionsButtonList.clear();
        this.optionsButtonList.addAll(List.of(
                this.backgroundColorButton,
                this.animationsButton,
                this.progressionBarButton
        ));
        this.updateButton(0, 0);
    }
    private void updateButton(int mouseX, int mouseY) {
        AtomicInteger length = new AtomicInteger(this.getButtonsTextLength());
        length.set(Math.max(this.font.width(Component.translatable("gui.ultimine_addition.skills_record.configuration")), length.get()));
        int spacing = Math.max(length.get() - 100 + 2, 0);
        if (spacing > 0) this.optionsButtonList.forEach(button -> button.setWidth(length.get()));

        this.configurationButton.setColor(this.backgroundColor.color);
        this.configurationButton.active = !MouseHelper.isMouseOver(mouseX, mouseY, this.leftPos, this.topPos, 160, 4, 9, 9) || !MouseHelper.isMouseOver(mouseX, mouseY, this.optionsX.get(0), this.optionsY.get(0), 0.0, 0.0, this.isOptionsShown ? 118 + spacing : 0, this.isOptionsShown ? 108 : 0);

        for (Button button : this.optionsButtonList) {
            button.visible = this.isOptionsShown;
        }
    }
    private void update(float partialTick) {
        if (!Screen.hasControlDown()) {
            this.time += partialTick;
        }
        this.currentProgress = 0;

        if (this.selectedSlot > -1 && this.container.getCardSlots().get(this.selectedSlot).getItem() == ItemStack.EMPTY) {
            this.selectedSlot = -1;
        }

        MiningSkillCardData data = new MiningSkillCardData().loadData(this.selectedSlot > -1 ? this.container.getCardSlots().get(selectedSlot).getItem() : ItemStack.EMPTY);
        boolean isCardTierMastered = data.getTier() == MiningSkillCardItem.Tier.Mastered;
        this.isChallengesExists = !data.getChallenges().isEmpty();
        boolean hasCorrectGamemode = !this.container.getPlayer().isCreative() && !this.container.getPlayer().isSpectator();
        this.isMissingItems = hasCorrectGamemode && (!this.container.getAllSlots().get(4).hasItem() || !this.container.getAllSlots().get(5).hasItem());
        this.notEnoughInk = hasCorrectGamemode && this.selectedSlot > -1 && this.container.getInkAmount() == 0;

        if (this.isOptionsShown) {
            this.isProgressionBarShown = false;
            this.container.getAllSlots().forEach(slot -> ((CustomSlot)slot).setEnabled(false));
        } else {
            if (this.isChallengesExists && !this.isMissingItems && !this.notEnoughInk && this.progressMode == 0) {
                this.isProgressionBarShown = true;
            } else if (this.selectedSlot == -1 || isCardTierMastered)
                this.isProgressionBarShown = false;
            this.container.getAllSlots().forEach(slot -> ((CustomSlot) slot).setEnabled(true));
        }

        this.textScreen.clear();
        this.textScreen.setHeight(82 - (this.isProgressionBarShown ? 9 : 0), true);
    }
    private void saveValuesToConfig() {
        ConfigHandler.CLIENT.BACKGROUND_COLOR.set(this.backgroundColor);
        ConfigHandler.CLIENT.ANIMATIONS_MODE.set(this.isAnimationsEnabled);
        ConfigHandler.CLIENT.PROGRESS_BAR.set(this.progressMode);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.update(partialTick);
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        this.renderScroller(poseStack, this.leftPos, this.topPos, mouseX, mouseY);
        this.renderProgressBar(poseStack, this.leftPos, this.topPos, mouseX, mouseY);
        this.renderConsumeButton(poseStack, this.leftPos, this.topPos, mouseX, mouseY);
        this.renderTextBoxComponent(poseStack, mouseX, mouseY);
        this.renderOptions(poseStack, mouseX, mouseY);
        this.renderTooltip(poseStack, mouseX, mouseY);
        this.updateButton(mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), this.backgroundColor.getAlpha());
        RenderSystem.setShaderTexture(0, this.TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        this.renderGhostItem(poseStack, this.leftPos, this.topPos);
        this.renderSlotDecorations(poseStack, this.leftPos, this.topPos);
        this.renderTextBox(poseStack);
    }

    @Override
    protected void renderLabels(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        Color color = ColorUtils.blendColors(new Color(0, 0, 0), this.backgroundColor.color, 0.75);
        this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, color.getRGB());
        this.font.draw(poseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, color.getRGB());
    }

    private void renderGhostItem(PoseStack poseStack, int x, int y) {
        List<ItemStack> listOfCards = List.of(
        new ItemStack(ModItems.MINING_SKILL_CARD_PICKAXE),
        new ItemStack(ModItems.MINING_SKILL_CARD_AXE),
        new ItemStack(ModItems.MINING_SKILL_CARD_SHOVEL),
        new ItemStack(ModItems.MINING_SKILL_CARD_HOE)
        );
        ItemStack displayItem = ItemStack.EMPTY;
        for (Slot slot : this.container.getAllSlots()) {
            if (slot instanceof MiningSkillCardSlot) displayItem = listOfCards.get(Mth.floor(this.time / 20.0F) % listOfCards.size());
            if (slot instanceof PenSlot) displayItem = new ItemStack(ModItems.PEN);
            if (slot instanceof PaperSlot) displayItem = new ItemStack(Items.PAPER);
            if (displayItem == ItemStack.EMPTY) return;

            ItemStack stack = slot.getItem();
            if (stack == ItemStack.EMPTY || isOptionsShown) {
                poseStack.pushPose();
                poseStack.translate(x + slot.x, y + slot.y, 100.0F + this.itemRenderer.blitOffset);
                poseStack.translate(8.0, 8.0, 0.0);
                poseStack.scale(1.0F, -1.0F, 1.0F);
                poseStack.scale(16.0F, 16.0F, 16.0F);
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                BakedModel model = this.itemRenderer.getModel(displayItem, null, null, 0);
                RenderSystem.applyModelViewMatrix();
                boolean bl = !model.usesBlockLight();
                if (bl) {
                    Lighting.setupForFlatItems();
                }

                poseStack.pushPose();
                RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

                float size = MathUtils.cycledBetweenValues(0.75F, 0.90F, 1.0F, time/20.0F, true);
                size = this.isAnimationsEnabled ? size : 0.90F;
                poseStack.scale(size, size, size);

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                this.itemRenderer.render(displayItem, ItemTransforms.TransformType.GUI, false, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, model);
                RenderSystem.enableDepthTest();
                bufferSource.endBatch();
                poseStack.popPose();
                if (bl) {
                    Lighting.setupFor3DItems();
                }

                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
                poseStack.popPose();
            }

            boolean isMissingItems = this.isChallengesExists && this.isMissingItems && (slot instanceof PenSlot || slot instanceof PaperSlot);
            boolean notEnoughInk = this.isChallengesExists && this.notEnoughInk && slot instanceof PenSlot;
            int color = (isMissingItems || notEnoughInk || (this.container.isCardSlotsEmpty() && slot instanceof MiningSkillCardSlot)) ? 0xff0000 : 0x8b8b8b;
            float alpha = (isMissingItems || notEnoughInk || (this.container.isCardSlotsEmpty() && slot instanceof MiningSkillCardSlot)) ? 0.15F : 0.55F;

            if (stack == ItemStack.EMPTY || notEnoughInk || isOptionsShown) {
                poseStack.pushPose();
                poseStack.translate(x + slot.x, y + slot.y, this.itemRenderer.blitOffset + 100.0F);
                poseStack.translate(8.0, 8.0, 0.0);
                RenderSystem.setShaderColor(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), this.backgroundColor.getAlpha());
                GuiComponent.fill(poseStack, -8, -8, 8, 8, ColorUtils.RGBToRGBA(color, alpha));
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            }
        }
    }

    private void renderSlotDecorations(PoseStack poseStack, int x, int y) {
        if (this.isOptionsShown) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F ,1.0F);
        if (container.getSlot(4).getItem() != ItemStack.EMPTY)
            this.blit(poseStack, x+133, y+99, 157, 219, 5, 8);

        if (container.getSlot(5).getItem() != ItemStack.EMPTY)
            this.blit(poseStack, x+153, y+99, 157, 219, 5, 8);

        if (this.selectedSlot == -1) return;
        int X = 18 + (20 * this.selectedSlot);

        this.blit(poseStack, x+X, y+99, 157, 219, 5, 8);
    }

    private void renderTextBox(PoseStack poseStack) {
        ItemStack stack = selectedSlot > -1 ? this.container.getCardSlots().get(this.selectedSlot).getItem() : ItemStack.EMPTY;
        MiningSkillCardData data = new MiningSkillCardData().loadData(stack);

        if (this.container.isCardSlotsEmpty()) {
            textScreen.selectBox(0)
                    .alignPosToCenter(true)
                    .create(Component.translatable("gui.ultimine_addition.skills_record.no_cards"), ChatFormatting.RED);
        } else if (this.selectedSlot == -1) {
            textScreen.selectBox(0)
                    .alignPosToCenter(true)
                    .create(Component.translatable("gui.ultimine_addition.skills_record.select_card"), ChatFormatting.GRAY);
        } else if (this.selectedSlot > -1 && !data.getChallenges().isEmpty()) {
            textScreen.selectBox(0).alignPosToCenter(false);
            createChallengesText(this.container.getCardSlots());
        } else if (this.selectedSlot > -1 && data.getTier() == MiningSkillCardItem.Tier.Mastered) {
            textScreen.selectBox(0)
                .alignPosToCenter(true)
                .create(Component.translatable("gui.ultimine_addition.skills_record.completed_card"), ChatFormatting.GOLD, ChatFormatting.ITALIC);
        } else {
            textScreen.selectBox(0)
                .alignPosToCenter(true)
                .create(Component.translatable("gui.ultimine_addition.skills_record.no_challenges"), ChatFormatting.GRAY);
        }

        if (this.isOptionsShown) {
            textScreen.selectBox(1)
                    .shouldRender(true).alignPosToCenter(List.of(true, false).get(Mth.floor(this.time / 20.0F) % 2))
                    .create(Component.translatable("gui.ultimine_addition.skills_record.example"), Component.literal("A: 001"), ChatFormatting.WHITE)
                    .create(Component.translatable("gui.ultimine_addition.skills_record.example"), Component.literal("B: 002"), ChatFormatting.GRAY)
                    .create(Component.translatable("gui.ultimine_addition.skills_record.example"), Component.literal("C: 003"), ChatFormatting.DARK_AQUA)
                    .create(Component.translatable("gui.ultimine_addition.skills_record.example"), Component.literal("C: 004"), ChatFormatting.GOLD);
            if (textScreen.canScroll() && this.optionsX.get(0) == this.leftPos + 178 && this.optionsY.get(0) == this.topPos) {
                this.moveOptionsWindowTo(this.leftPos + 191, this.topPos);
            }
        } else if (this.isChallengesExists && this.isMissingItems) {
            textScreen.selectBox(1)
                    .shouldRender(true)
                    .alignPosToCenter(true)
                    .backgroundColor(new Color(75, 24, 24, 218))
                    .create(Component.translatable("gui.ultimine_addition.skills_record.missing_items").withStyle(ChatFormatting.RED));

            List<ItemStack> missingItems = new ArrayList<>();
            if (!this.container.getAllSlots().get(4).hasItem()) missingItems.add(ModItems.PEN.getDefaultInstance());
            if (!this.container.getAllSlots().get(5).hasItem()) missingItems.add(Items.PAPER.getDefaultInstance());
            for (ItemStack item : missingItems) {
                textScreen.add(Component.literal("§c• ")
                        .append(Component.empty().append(item.getHoverName())
                                .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(item)))))
                        .getVisualOrderText());
            }

        } else if (this.isChallengesExists && this.notEnoughInk) {
            textScreen.selectBox(1)
                    .shouldRender(true)
                    .alignPosToCenter(true)
                    .backgroundColor(new Color(75, 24, 24, 218))
                    .create(Component.translatable("gui.ultimine_addition.skills_record.not_enough_ink").withStyle(ChatFormatting.RED));
        } else textScreen.selectBox(1)
                .shouldRender(false);

        this.textScreen.renderAllBoxes(poseStack, Color.DARK_GRAY.getRGB(), this.backgroundColor.color);
    }

    private void moveOptionsWindowTo(double newX, double newY) {
        this.optionsX.set(0, newX);
        this.optionsY.set(0, newY);
    }

    private void renderScroller(PoseStack poseStack, int x, int y, int mouseX, int mouseY) {
        if (!this.textScreen.canScroll()) return;

        float offset = this.textScreen.getScrollOffset() / Math.max(1.0F, this.textScreen.getRemainingLines());
        float delta = Mth.clamp(offset, 0.0F, 1.0F);
        int Y = (int) Mth.lerp(delta, this.topPos + 16, this.topPos + 88);
        boolean isOverButton = MouseHelper.isMouseOver(mouseX, mouseY, x+174, Y, 0, 0, 9, 10);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), this.backgroundColor.getAlpha());
        this.blit(poseStack, x+173, y+10, 177, 28, 16, 95);
        this.blit(poseStack, x+174, Y, 177, (!isOverButton ? 124 : 135), 9, 11);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderProgressBar(PoseStack poseStack, int x, int y, int mouseX, int mouseY) {
        if (!this.isProgressionBarShown) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.blit(poseStack, x+10, y + 91, 0, 219, 156, 7);
        float value = (float) this.currentProgress / this.maxProgress;
        int bar = (int) Mth.lerp(value, 0, 154);

        poseStack.pushPose();
        poseStack.translate(x+11, y+97, 0);
        poseStack.mulPose(Vector3f.ZN.rotationDegrees(90));
        fillGradient(poseStack, 0, 0, 5, bar, ColorUtils.RGBToRGBA(getProgressionBarColor(), 0.55F), ColorUtils.RGBToRGBA(getProgressionBarColor(), 0.75F));
        poseStack.popPose();

        if (this.isOptionsShown) return;
        if (MouseHelper.isMouseOver(mouseX, mouseY, x, y, 10, 91, 156, 7)) {
            Component component = Component.translatable("gui.ultimine_addition.skills_record.progress", Component.literal("%" + this.currentProgress).withStyle(Style.EMPTY.withColor(getProgressionBarColor()))).withStyle(ChatFormatting.GRAY);
            this.renderTooltip(poseStack, component, mouseX - 50, mouseY - 5);
        }
    }

    private void renderConsumeButton(PoseStack poseStack, int x, int y, int mouseX, int mouseY) {
        if (!this.isOptionsShown && !this.isConsumeChallengeExists()) return;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.TEXTURE);
        RenderSystem.setShaderColor(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), this.backgroundColor.getAlpha());
        this.blit(poseStack, x+86, y+106, 177, 147, 9, 18);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int color = (this.container.getData().isConsumeMode()) ? new Color(0x00FF4F).getRGB() : new Color(0xD92121).getRGB();
        fillGradient(poseStack, x+87, y+107, x+94, y+123, ColorUtils.RGBToRGBA(color, 1.0F), ColorUtils.RGBToRGBA(color, 0.75F));

        int yPos = (this.container.getData().isConsumeMode()) ? 115 : 107;
        RenderSystem.setShaderColor(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), this.backgroundColor.getAlpha());
        this.blit(poseStack, x+87, y+yPos, 177, 166, 7, 8);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (!this.isOptionsShown && MouseHelper.isMouseOver(mouseX, mouseY, x, y, 86, 106, 9, 18)) {
            Component state = (this.container.getData().isConsumeMode()) ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN) : Component.translatable("options.off").withStyle(ChatFormatting.RED);
            Component info = Component.literal("➤ ").withStyle(ChatFormatting.GRAY).append(Component.translatable("gui.ultimine_addition.skills_record.consume", state).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC));
            this.renderTooltip(poseStack, info, mouseX, mouseY);
        }
    }

    private void renderTextBoxComponent(PoseStack poseStack, int mouseX, int mouseY) {
        if (isOptionsShown) return;
        Style style = this.textScreen.getComponentStyleAt(mouseX, mouseY);
        if (style != null && style.getHoverEvent() != null) {
            var styleHoverEvent = style.getHoverEvent();
            if (styleHoverEvent.getValue(HoverEvent.Action.SHOW_TEXT) != null) {
                Component component = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);
                if (component != null) {
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    this.renderTooltip(poseStack, this.font.split(component, Math.max(this.width / 2, 200)), mouseX, mouseY);
                }
            }
            if (styleHoverEvent.getValue(HoverEvent.Action.SHOW_ITEM) != null) {
                var stackInfo = styleHoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
                if (stackInfo == null) return;
                int X = -10;
                int Y = -30;

                poseStack.pushPose();
                this.renderItemTooltip(poseStack, stackInfo.getItemStack(), style, mouseX+X , mouseY+Y);
                poseStack.popPose();
            }
        }
    }

    private void renderOptions(PoseStack poseStack, int mouseX, int mouseY) {
        if (!this.isOptionsShown) return;
        if (this.isDraggingWindow) {
            if (this.optionsX.get(1) == 0.0) {
                this.optionsX.set(1, Math.abs(this.optionsX.get(0) - mouseX));
                this.optionsY.set(1, Math.abs(this.optionsY.get(0) - mouseY));
            }
            this.optionsX.set(0, mouseX - this.optionsX.get(1));
            this.optionsY.set(0, mouseY - this.optionsY.get(1));
        } else {
            this.optionsX.set(1, 0.0);
            this.optionsY.set(1, 0.0);
        }

        AtomicInteger length = new AtomicInteger(this.getButtonsTextLength());
        length.set(Math.max(this.font.width(Component.translatable("gui.ultimine_addition.skills_record.configuration")), length.get()));
        int spacing = Math.max(length.get() - 100 + 2, 0);

        poseStack.pushPose();
        poseStack.translate(this.optionsX.get(0), this.optionsY.get(0), this.itemRenderer.blitOffset + 350.0F);
        int filling = 5;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.OPTIONS_TEXTURE);
        RenderSystem.setShaderColor(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), this.backgroundColor.getAlpha());
        this.blit(poseStack, 0, 0, 0, 0, 59, 108);
        for (int i = 0; i <= spacing / filling; i++)
            this.blit(poseStack, 59 + (filling * i), 0, 9, 0, filling, 108);
        this.blit(poseStack, 59+spacing, 0, 60, 0, 58, 108);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.drawCenteredString(poseStack, this.font, Component.translatable("gui.ultimine_addition.skills_record.configuration"), 59+(spacing/2), 17, Color.WHITE.getRGB());

        for (int i = 0; i < this.optionsButtonList.size(); i++) {
            int l = 13 * i;
            this.optionsButtonList.get(i).x = (int) (this.optionsX.get(0)+9);
            this.optionsButtonList.get(i).y = (int) (this.optionsY.get(0)+29+l);
            GuiComponent.fill(poseStack, 9, 29+l, 108+spacing, 39+l, ColorUtils.RGBToRGBA(Color.WHITE.getRGB(), this.optionsButtonList.get(i).isHoveredOrFocused() ? 0.45F : 0.25F));
            GuiComponent.drawCenteredString(poseStack, this.font, this.optionsButtonList.get(i).getMessage(), 59+(spacing/2), 30+l, Color.WHITE.getRGB());
            this.renderOptionsTooltip(poseStack, this.optionsButtonList.get(i), (int) (mouseX-this.optionsX.get(0)), (int) (mouseY-this.optionsY.get(0)));
        }

        if (this.isDraggingWindow)
            GuiComponent.fill(poseStack, 7, 4, 110+spacing, 13, ColorUtils.RGBToRGBA(Color.WHITE.getRGB(), 0.25F));
        poseStack.popPose();
    }

    private void renderOptionsTooltip(PoseStack poseStack, @NotNull Button button, int mouseX, int mouseY) {
        if (!button.isHoveredOrFocused()) return;
        Component component = Component.empty();

        if (button == this.backgroundColorButton) {
            int color = ColorUtils.RGBToRGBA(this.backgroundColor.color.getRGB(), this.backgroundColor.getAlpha());
            component = Component.translatable(String.format("gui.ultimine_addition.color.%s", this.backgroundColor.name().toLowerCase())).withStyle(Style.EMPTY.withColor(color));
        } else if (button == this.animationsButton) {
            component = this.isAnimationsEnabled ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN) : Component.translatable("options.off").withStyle(ChatFormatting.RED);
        } else if (button == this.progressionBarButton) {
            switch (this.progressMode) {
                case 0 -> component = Component.translatable("options.on").withStyle(ChatFormatting.GREEN);
                case 1 -> {
                    String[] parts = KeyHandler.KEY_SHOW_PROGRESSION_BAR.saveString().split("\\.");
                    component = Component.translatable("gui.ultimine_addition.skills_record.option.hold_keybind", parts[parts.length - 1].toUpperCase()).withStyle(ChatFormatting.AQUA);
                    if (I18n.exists(KeyHandler.KEY_SHOW_PROGRESSION_BAR.saveString()))
                        component = Component.translatable("gui.ultimine_addition.skills_record.option.hold_keybind", I18n.get(KeyHandler.KEY_SHOW_PROGRESSION_BAR.saveString())).withStyle(ChatFormatting.AQUA);
                }
                case 2 -> component = Component.translatable("options.off").withStyle(ChatFormatting.RED);
            }
        }
        poseStack.pushPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderTooltip(poseStack, Component.literal("➤ ").withStyle(ChatFormatting.DARK_GRAY).append(component).withStyle(ChatFormatting.ITALIC), mouseX, mouseY);
        poseStack.popPose();
    }

    private void renderItemTooltip(@NotNull PoseStack poseStack, ItemStack stack, Style style, int mouseX, int mouseY) {
        AtomicInteger length = new AtomicInteger();
        List<Component> tooltip = this.getTooltipFromItem(stack);
        List<ClientTooltipComponent> tooltipComponents = tooltip.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        tooltipComponents.forEach(component -> length.set(Math.max(length.get(), component.getWidth(this.font))));

        if (style.getClickEvent() != null) {
            Component component = Component.translatable("tooltip.ultimine_addition.skills_record.press.left_click").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            tooltipComponents.addAll(Language.getInstance().getVisualOrder(this.font.getSplitter().splitLines(component, Math.max(length.get(), 150), Style.EMPTY)).stream().map(ClientTooltipComponent::create).toList());
        }

        boolean isExceedingWindow = (mouseX+12+18) + length.get() > this.width;
        int alignPosX = isExceedingWindow ? 6 : 0;

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, this.itemRenderer.blitOffset + 100.0F);
        RenderSystem.setShaderColor(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), this.backgroundColor.getAlpha());
        this.blit(poseStack, mouseX+alignPosX, mouseY, 0, 227, 26, 26);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        this.itemRenderer.renderAndDecorateFakeItem(stack, mouseX+alignPosX+5, mouseY+5);
        int spacing = 6 - tooltipComponents.size() * 6;
        this.renderTooltipInternal(poseStack, tooltipComponents, mouseX+18, mouseY+21+spacing);
    }

    private boolean isConsumeChallengeExists() {
        AtomicBoolean result = new AtomicBoolean();
        this.container.getCardSlots().forEach(slot -> {
            ItemStack stack = slot.getItem();
            MiningSkillCardData cardData = new MiningSkillCardData().loadData(stack);
            if (!cardData.getChallenges().keySet().stream().filter(identifier -> {
                if (ChallengesManager.INSTANCE.getAllChallenges().get(identifier.id()) == null) return false;
                return ChallengesManager.INSTANCE.getAllChallenges().get(identifier.id()).getChallengeType().isConsuming();
            }).toList().isEmpty())
                result.set(true);
        });
        return result.get();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isOptionsShown && this.isInsideOptionsDragBox(mouseX, mouseY)) {
                this.isDraggingWindow = true;
                return true;
            }
            if (this.isInsideScrollbar(mouseX, mouseY)) {
                this.isScrolling = this.textScreen.canScroll();
                return true;
            }
            if (this.textScreen.getComponentStyleAt(mouseX, mouseY) != null && this.textScreen.getComponentStyleAt(mouseX, mouseY).getClickEvent() != null) {
                this.click++;
                return true;
            }
            if (!isOptionsShown && this.isConsumeChallengeExists() && MouseHelper.isMouseOver(mouseX, mouseY, this.leftPos, this.topPos, 86, 106, 9, 18)) {
                var data = this.container.getData();
                data.toggleConsumeMode().sendToServer().saveData(data.get());
            }
        }
        if (button == 1) {
            if (this.hoveredSlot != null && this.hoveredSlot.getItem() != ItemStack.EMPTY && this.container.getCardSlots().contains(this.hoveredSlot)) {
                if (selectedSlot == -1 || selectedSlot != this.hoveredSlot.getContainerSlot()) {
                    this.selectedSlot = this.hoveredSlot.getContainerSlot();
                } else this.selectedSlot = -1;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isScrolling = this.isDraggingWindow = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isScrolling) {
            int i = this.topPos + 15;
            int j = i + 83;
            float delta = Mth.clamp((float) ((mouseY - i) / (j - i)), 0.0F, 1.0F);
            this.textScreen.scrollTo((int) Mth.lerp(delta, 0, this.textScreen.getRemainingLines()), true);
            return true;
        } else return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.textScreen.canScroll()) {
            if (delta < 0) this.textScreen.scrollTo(+1, false);
            if (delta > 0) this.textScreen.scrollTo(-1, false);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.hoveredSlot != null) return super.keyPressed(keyCode, scanCode, modifiers);

        if (KeyHandler.KEY_SHOW_PROGRESSION_BAR.matches(keyCode, scanCode)) {
            if (this.isChallengesExists && !this.isMissingItems && !this.notEnoughInk && !this.isOptionsShown && this.progressMode == 1)
                this.isProgressionBarShown = true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (KeyHandler.KEY_SHOW_PROGRESSION_BAR.matches(keyCode, scanCode)) {
            if (this.progressMode == 1) this.isProgressionBarShown = false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        int spacing = Math.max(this.getButtonsTextLength() - 100, 0);
        if (MouseHelper.isMouseOver(mouseX, mouseY, this.optionsX.get(0), this.optionsY.get(0), 0.0, 0.0, 118+spacing, 108)) return false;
        return super.isHovering(x, y, width, height, mouseX, mouseY);
    }
    private boolean isInsideScrollbar(double mouseX, double mouseY) {
        return MouseHelper.isMouseOver(mouseX, mouseY, this.leftPos, this.topPos, 174, 16, 6, 82);
    }
    private boolean isInsideOptionsDragBox(double mouseX, double mouseY) {
        return MouseHelper.isMouseOver((int) mouseX, (int) mouseY, this.optionsX.get(0), this.optionsY.get(0), 7, 4, 104, 8);
    }

    private void resetOptionsWindow() {
        this.optionsX.set(0, (double) (this.leftPos + 178));
        this.optionsY.set(0, (double) this.topPos);
    }

    private void createChallengesText(NonNullList<Slot> slots) {
        ItemStack stack = slots.get(this.selectedSlot).getItem();
        MiningSkillCardData cardData = new MiningSkillCardData().loadData(stack);
        ChallengesManager manager = ChallengesManager.INSTANCE;

        AtomicInteger currentValues = new AtomicInteger();
        AtomicInteger requiredValues = new AtomicInteger();
        cardData.getChallenges().forEach((identifier, values) -> {
            List<ItemStack> itemList = new ArrayList<>(List.of(ItemStack.EMPTY));
            ChallengesData challengesData = manager.getAllChallenges().get(identifier.id());
            String type = "null";

            if (challengesData != null) {
                itemList.clear();
                type = challengesData.getChallengeType().getTypeName();
                ChallengesManager.INSTANCE.utilizeTargetedBlocks(challengesData).forEach(block -> itemList.add(new ItemStack(block)));
                currentValues.addAndGet(values.getCurrent());
                requiredValues.addAndGet(values.getRequired());
            }

            ItemStack displayItem = itemList.get(Mth.floor(this.click) % itemList.size());
            var hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(displayItem));
            Style questStyle = Style.EMPTY.withHoverEvent(hoverEvent).withItalic(true).withColor(ChatFormatting.DARK_AQUA);
            Component info;
            if (itemList.size() > 1) {
                questStyle = questStyle.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, ""));
                info = Component.translatable("challenge.ultimine_addition.various_blocks", Component.literal(displayItem.getHoverName().getString()).withStyle(questStyle));
            } else info = Component.literal(itemList.get(0).getHoverName().getString()).withStyle(questStyle);

            @SuppressWarnings("UnnecessaryUnicodeEscape")
            Component title = ScreenUtils.limitComponent(Component.literal("\u300E").append(Component.translatable("challenge.ultimine_addition.title", identifier.order())).append("\u300F"), this.textScreen.getWidth());
            Component consume = Component.literal("✖ ").append(Component.translatable("challenge.ultimine_addition.consume")).withStyle(ChatFormatting.RED);
            Component description = Component.translatable(String.format("challenge.ultimine_addition.%s", type), info);
            Style descStyle = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY);
            Style counterStyle = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GOLD);

            if (challengesData != null && challengesData.getChallengeType().isConsuming() && !this.container.getData().isConsumeMode() && !cardData.isChallengeAccomplished(identifier.id())) {
                HoverEvent consumeRequired = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("challenge.ultimine_addition.consume.info").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                counterStyle = Style.EMPTY.withHoverEvent(consumeRequired).withStrikethrough(true).withColor(ChatFormatting.RED);

            } else if (challengesData != null && challengesData.getChallengeType().isConsuming() && this.container.getData().isConsumeMode() && !cardData.isChallengeAccomplished(identifier.id())) {
                counterStyle = Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_PURPLE);

            } else if (cardData.isChallengeAccomplished(identifier.id())) {
                counterStyle = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GREEN);
            }

            List<FormattedCharSequence> result = new ArrayList<>(Language.getInstance().getVisualOrder(this.font.getSplitter().splitLines(title, this.textScreen.getWidth(), title.getStyle())));
            if (challengesData != null && challengesData.getChallengeType().isConsuming())
                result.addAll(Language.getInstance().getVisualOrder(this.font.getSplitter().splitLines(consume, this.textScreen.getWidth(), consume.getStyle())));
            result.addAll(Language.getInstance().getVisualOrder(this.font.getSplitter().splitLines(description, this.textScreen.getWidth(), descStyle)));

            result.add(FormattedCharSequence.forward(String.format("➤ %s/%s", values.getCurrent(), values.getRequired()), counterStyle));
            if (!this.textScreen.isEmpty()) this.textScreen.add(FormattedCharSequence.EMPTY).addAll(result); else this.textScreen.addAll(result);
        });
        this.calculateProgression(currentValues.get(), requiredValues.get());
    }

    private void calculateProgression(int currentValues, int requiredValues) {
        if (requiredValues == 0) return;
        float value = (float) currentValues / requiredValues * this.maxProgress;
        this.currentProgress = (int) value;
    }

    public int getProgressionBarColor() {
        float f = Math.min((float) currentProgress / maxProgress, 1.0F);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public Collection<Rect2i> getOptionsRect() {
        Collection<Rect2i> collection = Collections.singleton(new Rect2i(0, 0, 0, 0));
        if (this.optionsButtonList.isEmpty()) return collection;

        int spacing = Math.max(this.getButtonsTextLength() - 100, 0);
        if (this.isOptionsShown) {
            collection = Collections.singleton(new Rect2i(this.optionsX.get(0).intValue(), this.optionsY.get(0).intValue(), 118+spacing, 108));
        }
        return collection;
    }

    private int getButtonsTextLength() {
        AtomicInteger length = new AtomicInteger(0);
        this.optionsButtonList.forEach(button ->
                length.set(Math.max(this.font.width(button.getMessage()), length.get()))
        );
        return length.get();
    }

    public enum BGColor {
        DEFAULT(new Color(255, 255, 255)),
        RED(new Color(255, 116, 116)),
        ORANGE(new Color(255, 162, 94)),
        YELLOW(new Color(253, 241, 113)),
        GREEN(new Color(144, 238, 144)),
        BLUE(new Color(112, 153, 255)),
        INDIGO(new Color(126, 80, 176)),
        VIOLET(new Color(238, 130, 238));

        private final Color color;
        BGColor(Color color) {
            this.color = color;
        }

        public float getAlpha() {
            return new ColorUtils(color.getRGB()).getAlpha();
        }
        public float getRed() {
            return new ColorUtils(color.getRGB()).getRed();
        }
        public float getGreen() {
            return new ColorUtils(color.getRGB()).getGreen();
        }
        public float getBlue() {
            return new ColorUtils(color.getRGB()).getBlue();
        }

        public BGColor next() {
            int nextIndex = (this.ordinal() + 1) % BGColor.values().length;
            return BGColor.values()[nextIndex];
        }

        public BGColor previous() {
            int prevIndex = (this.ordinal() - 1 + BGColor.values().length) % BGColor.values().length;
            return BGColor.values()[prevIndex];
        }
    }
}
