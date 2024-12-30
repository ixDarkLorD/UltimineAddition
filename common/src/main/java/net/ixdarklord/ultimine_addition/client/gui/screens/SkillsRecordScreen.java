package net.ixdarklord.ultimine_addition.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.client.gui.components.ColorableImageButton;
import net.ixdarklord.coolcatlib.api.client.gui.components.TextScreen;
import net.ixdarklord.coolcatlib.api.util.*;
import net.ixdarklord.ultimine_addition.client.gui.components.AbstractDraggableWidget;
import net.ixdarklord.ultimine_addition.client.gui.components.skills_record.ConfigurationPanel;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.common.menu.slot.CustomSlot;
import net.ixdarklord.ultimine_addition.common.menu.slot.MiningSkillCardSlot;
import net.ixdarklord.ultimine_addition.common.menu.slot.PaperSlot;
import net.ixdarklord.ultimine_addition.common.menu.slot.PenSlot;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.hooks.KeyBindingHooks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.commands.Commands;
import net.minecraft.core.NonNullList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class SkillsRecordScreen extends AbstractContainerScreen<SkillsRecordMenu> {
    private final ResourceLocation MENU_LOCATION = UltimineAddition.getGuiTexture("container/skills_record", "png");
    private final ResourceLocation SLOT_SELECT_SPRITE = UltimineAddition.getLocation("container/skills_record/slot_select");
    private final ResourceLocation PROGRESS_BAR_SPRITE = UltimineAddition.getLocation("container/skills_record/progress_bar");
    private final ResourceLocation ITEM_DISPLAY_SPRITE = UltimineAddition.getLocation("container/skills_record/item_display");
    private final WidgetSprites CONFIGURATION_BUTTON_SPRITES = new WidgetSprites(UltimineAddition.getLocation("container/skills_record/configuration_button_enabled"), UltimineAddition.getLocation("container/skills_record/configuration_button_disabled"), UltimineAddition.getLocation("container/skills_record/configuration_button_focused"));
    private final WidgetSprites CONSUME_BUTTON_SPRITES = new WidgetSprites(UltimineAddition.getLocation("container/skills_record/consume_on"), UltimineAddition.getLocation("container/skills_record/consume_off"), UltimineAddition.getLocation("container/skills_record/consume_on_focused"), UltimineAddition.getLocation("container/skills_record/consume_off_focused"));
    private final WidgetSprites SCROLLBAR_SPRITE = new WidgetSprites(UltimineAddition.getLocation("container/skills_record/scroller_enabled"), UltimineAddition.getLocation("container/skills_record/scroller_disabled"), UltimineAddition.getLocation("container/skills_record/scroller_focused"));

    private final int maxProgress = 100;
    private ColorableImageButton configurationButton;
    private StateSwitchingButton consumeButton;
    private ConfigurationPanel configuration;
    private TextScreen textScreen;

    private static float itemCycle;
    private int selectedSlot;
    private boolean isChallengesExists;
    private boolean isMissingItems;
    private boolean notEnoughInk;
    private int currentProgress;
    private boolean isScrolling;
    private boolean isProgressionBarShown;

    public SkillsRecordScreen(SkillsRecordMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 192;
        this.imageHeight = 224;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 5;
        this.inventoryLabelX = (this.imageWidth / 2) - (this.font.width(this.playerInventoryTitle) / 2);
        this.inventoryLabelY = this.imageHeight - 96;

        this.createButtons();
        this.selectedSlot = menu.getData().getSelectedCard();
        this.textScreen = TextScreen.build(this.leftPos + 10, this.topPos + 17, 157, 82, ConfigHandler.CLIENT.TEXT_SCREEN_SHADOW.get(), 2);

        boolean visibility = this.configuration != null && this.configuration.isVisible();
        this.configuration = this.addWidget(new ConfigurationPanel(this.leftPos + 194, this.topPos));
        this.configuration.setVisible(visibility);

        /*this.addRenderableWidget(new AbstractMultiPanelWidget(this.leftPos - 130, this.topPos, 128, 80, 2, false) {
            @Override
            protected void init() {
                Button button = this.addRenderableWidget(Button.builder(Component.empty(), button1 -> {}).build());
                Button button2 = this.addRenderableWidget(Button.builder(Component.empty(), button1 -> {}).size(40, 100).build());
                Button button3 = this.addRenderableWidget(Button.builder(Component.empty(), button1 -> {}).build());

                this.addToHeader(button);
                this.addToContents(button2);
                this.addToFooter(button3);

                this.selectPanel(1);
                Button button4 = this.addRenderableWidget(Button.builder(Component.empty(), button1 -> {}).size(60, 90).build());
                this.addToContents(button4);
                this.shouldRender(true);
            }

            @Override
            public @NotNull ScreenRectangle getDraggingRectangle() {
                return ScreenRectangle.empty();
            }
        });*/
    }

    private void createButtons() {
        this.configurationButton = this.addRenderableWidget(new ColorableImageButton(this.leftPos + 174, this.topPos + 4, 10, 10, CONFIGURATION_BUTTON_SPRITES,
                button -> {
                    this.configuration.toggleVisibility(true);
                    this.configuration.saveValuesToConfig();
                }));

        this.consumeButton = this.addRenderableWidget(new StateSwitchingButton(this.leftPos + 174, this.topPos + 106, 10, 18, this.menu.getData().isConsumeMode()) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                this.sprites = CONSUME_BUTTON_SPRITES;
                guiGraphics.setColor(SkillsRecordScreen.this.configuration.getBackgroundColor().getRed(), SkillsRecordScreen.this.configuration.getBackgroundColor().getGreen(), SkillsRecordScreen.this.configuration.getBackgroundColor().getBlue(), SkillsRecordScreen.this.configuration.getBackgroundColor().getAlpha());
                RenderSystem.disableDepthTest();
                guiGraphics.blitSprite(this.sprites.get(this.isStateTriggered, this.active && this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height);
                RenderSystem.enableDepthTest();
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

                double value = SkillsRecordScreen.this.configuration.isAnimationsEnabled() ? MathUtils.cycledBetweenValues(0.5F, 1.0F, 0.75F, SkillsRecordScreen.this.menu.getPlayer().tickCount / 20.0F, false) : 1.0F;
                Color cTo = this.isStateTriggered ? new Color(0x23AC23) : new Color(0xA82222);
                Color cFrom = this.isStateTriggered ? ColorUtils.blendColors(new Color(0x37FF37), cTo, value) : ColorUtils.blendColors(new Color(0xFF3737), cTo, value);
                Color color1 = this.isStateTriggered ? cFrom : cTo;
                Color color2 = this.isStateTriggered ? cTo : cFrom;
                guiGraphics.fillGradient(this.getX() + 1, this.getY() + (this.isStateTriggered ? 1 : 9), this.getX() + 9, this.getY() + (this.isStateTriggered ? 9 : 17), color1.getRGB(), color2.getRGB());
            }

            @Override
            public void onClick(double mouseX, double mouseY) {
                if (!SkillsRecordScreen.this.configuration.isVisible() && SkillsRecordScreen.this.isConsumeChallengeExists()) {
                    SkillsRecordData data = SkillsRecordScreen.this.menu.getData();
                    data.toggleConsumeMode().sendToServer().saveData(data.get());
                }
            }
        });
    }

    public void update() {
        this.currentProgress = 0;

        if (this.selectedSlot > -1 && this.menu.getCardSlots().get(this.selectedSlot).getItem() == ItemStack.EMPTY) {
            this.selectedSlot = -1;
            SkillsRecordData data = this.menu.getData();
            data.setSelectedCard(this.selectedSlot).sendToServer().saveData(data.get());
        }

        MiningSkillCardData data = MiningSkillCardData.loadData(this.selectedSlot > -1 ? this.menu.getCardSlots().get(selectedSlot).getItem() : ItemStack.EMPTY);
        boolean isCardTierMastered = data.getTier() == MiningSkillCardItem.Tier.Mastered;
        this.isChallengesExists = !data.getChallenges().isEmpty();
        boolean hasCorrectGamemode = !this.menu.getPlayer().isCreative() && !this.menu.getPlayer().isSpectator();
        this.isMissingItems = hasCorrectGamemode && (!this.menu.getAllSlots().get(4).hasItem() || !this.menu.getAllSlots().get(5).hasItem());
        this.notEnoughInk = hasCorrectGamemode && this.selectedSlot > -1 && this.menu.getInkAmount() == 0;

        if (this.configuration.isVisible()) {
            this.isProgressionBarShown = false;
            this.menu.getAllSlots().forEach(slot -> ((CustomSlot) slot).setEnabled(false));
        } else {
            if (this.isChallengesExists && !this.isMissingItems && !this.notEnoughInk && this.configuration.getProgressMode() == 0) {
                this.isProgressionBarShown = true;
            } else if (this.selectedSlot == -1 || isCardTierMastered)
                this.isProgressionBarShown = false;
            this.menu.getAllSlots().forEach(slot -> ((CustomSlot) slot).setEnabled(true));
        }

        this.textScreen.clear();
        this.textScreen.setHeight(82 - (this.isProgressionBarShown ? 9 : 0), true);

        if (this.configurationButton != null) {
            this.configurationButton.setColor(this.configuration.getBackgroundColor().color);
        }

        if (this.consumeButton != null) {
            Component state = (this.menu.getData().isConsumeMode()) ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN) : Component.translatable("options.off").withStyle(ChatFormatting.RED);
            Component info = Component.literal("➤ ").withStyle(ChatFormatting.GRAY).append(Component.translatable("gui.ultimine_addition.skills_record.consume", state).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC));
            this.consumeButton.active = !this.configuration.isVisible();
            this.consumeButton.setTooltip(this.consumeButton.isActive() ? Tooltip.create(info) : null);
            this.consumeButton.setStateTriggered(this.menu.getData().isConsumeMode());
            if (this.consumeButton.isFocused())
                this.consumeButton.setFocused(false);
        }
    }

    @Override
    public void onClose() {
        this.configuration.saveValuesToConfig();
        super.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.update();
        this.renderGhostItem(guiGraphics, this.leftPos, this.topPos);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        this.renderSlotDecorations(guiGraphics, this.leftPos, this.topPos);
        this.renderTextBox(guiGraphics);
        this.renderScroll(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
        this.renderProgressBar(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
        this.renderTextBoxComponent(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.configuration.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.setColor(this.configuration.getBackgroundColor().getRed(), this.configuration.getBackgroundColor().getGreen(), this.configuration.getBackgroundColor().getBlue(), this.configuration.getBackgroundColor().getAlpha());
        guiGraphics.blit(this.MENU_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Color color = ColorUtils.blendColors(new Color(0, 0, 0), this.configuration.getBackgroundColor().color, 0.75);
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, color.getRGB(), false);

        guiGraphics.fill(this.inventoryLabelX - 1, this.inventoryLabelY - 1, this.inventoryLabelX + this.font.width(this.playerInventoryTitle), this.inventoryLabelY + this.font.lineHeight, ColorUtils.RGBToRGBA(color.getRGB(), 0.5F));
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, color.getRGB(), false);
    }

    private void renderGhostItem(GuiGraphics guiGraphics, int x, int y) {
        PoseStack poseStack = guiGraphics.pose();
        List<ItemStack> listOfCards = List.of(
                new ItemStack(Registration.MINING_SKILL_CARD_PICKAXE),
                new ItemStack(Registration.MINING_SKILL_CARD_AXE),
                new ItemStack(Registration.MINING_SKILL_CARD_SHOVEL),
                new ItemStack(Registration.MINING_SKILL_CARD_HOE)
        );
        ItemStack displayItem = ItemStack.EMPTY;
        for (Slot slot : this.menu.getAllSlots()) {
            if (slot instanceof MiningSkillCardSlot)
                displayItem = listOfCards.get(Mth.floor(this.menu.getPlayer().tickCount / 20.0F) % listOfCards.size());
            if (slot instanceof PenSlot) displayItem = new ItemStack(Registration.PEN);
            if (slot instanceof PaperSlot) displayItem = new ItemStack(Items.PAPER);
            if (displayItem == ItemStack.EMPTY) return;

            ItemStack stack = slot.getItem();
            if (stack == ItemStack.EMPTY || this.configuration.isVisible()) {
                poseStack.pushPose();
                poseStack.translate(x + slot.x, y + slot.y, 100.0F);
                poseStack.translate(8.0, 8.0, 0.0);
                poseStack.scale(1.0F, -1.0F, 1.0F);
                poseStack.scale(16.0F, 16.0F, 16.0F);
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                assert this.minecraft != null;
                BakedModel model = this.minecraft.getItemRenderer().getModel(displayItem, null, null, 0);
                RenderSystem.applyModelViewMatrix();
                boolean bl = !model.usesBlockLight();
                if (bl) {
                    Lighting.setupForFlatItems();
                }

                poseStack.pushPose();
                RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

                float size = MathUtils.cycledBetweenValues(0.75F, 0.90F, 1.0F, this.menu.getPlayer().tickCount / 20.0F, true);
                size = this.configuration.isAnimationsEnabled() ? size : 0.90F;
                poseStack.scale(size, size, size);

                this.minecraft.getItemRenderer().render(displayItem, ItemDisplayContext.GUI, false, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, model);
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
            int color = (isMissingItems || notEnoughInk || (this.menu.isCardSlotsEmpty() && slot instanceof MiningSkillCardSlot)) ? 0xff0000 : 0x8b8b8b;
            float alpha = (isMissingItems || notEnoughInk || (this.menu.isCardSlotsEmpty() && slot instanceof MiningSkillCardSlot)) ? 0.15F : 0.55F;

            if (stack == ItemStack.EMPTY || notEnoughInk || this.configuration.isVisible()) {
                poseStack.pushPose();
                poseStack.translate(x + slot.x, y + slot.y, 100.0F);
                poseStack.translate(8.0, 8.0, 0.0);
                guiGraphics.setColor(this.configuration.getBackgroundColor().getRed(), this.configuration.getBackgroundColor().getGreen(), this.configuration.getBackgroundColor().getBlue(), this.configuration.getBackgroundColor().getAlpha());
                guiGraphics.fill(RenderType.guiOverlay(), -8, -8, 8, 8, ColorUtils.RGBToRGBA(color, alpha));
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            }
        }
    }

    private void renderSlotDecorations(GuiGraphics guiGraphics, int x, int y) {
        if (this.configuration.isVisible()) return;

        guiGraphics.setColor(this.configuration.getBackgroundColor().getRed(), this.configuration.getBackgroundColor().getGreen(), this.configuration.getBackgroundColor().getBlue(), this.configuration.getBackgroundColor().getAlpha());
        if (this.menu.getSlot(4).getItem() != ItemStack.EMPTY)
            guiGraphics.blitSprite(SLOT_SELECT_SPRITE, x + 135, y + 99, 4, 8);

        if (this.menu.getSlot(5).getItem() != ItemStack.EMPTY)
            guiGraphics.blitSprite(SLOT_SELECT_SPRITE, x + 157, y + 99, 4, 8);

        if (this.selectedSlot == -1) return;
        int X = 14 + (22 * this.selectedSlot);
        guiGraphics.blitSprite(SLOT_SELECT_SPRITE, x + X, y + 99, 4, 8);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderTextBox(GuiGraphics guiGraphics) {
        ItemStack stack = selectedSlot > -1 ? this.menu.getCardSlots().get(this.selectedSlot).getItem() : ItemStack.EMPTY;
        MiningSkillCardData data = MiningSkillCardData.loadData(stack);

        if (this.menu.isCardSlotsEmpty()) {
            textScreen.selectScreen(0)
                    .alignToCenter(true)
                    .create(Component.translatable("gui.ultimine_addition.skills_record.no_cards"), ChatFormatting.RED);
        } else if (this.selectedSlot == -1) {
            textScreen.selectScreen(0)
                    .alignToCenter(true)
                    .create(Component.translatable("gui.ultimine_addition.skills_record.select_card"), ChatFormatting.GRAY);
        } else if (this.selectedSlot > -1 && !data.getChallenges().isEmpty()) {
            textScreen.selectScreen(0).alignToCenter(false);
            createChallengesText(this.menu.getCardSlots());
        } else if (this.selectedSlot > -1 && data.getTier() == MiningSkillCardItem.Tier.Mastered) {
            textScreen.selectScreen(0)
                    .alignToCenter(true)
                    .create(Component.translatable("gui.ultimine_addition.skills_record.completed_card"), ChatFormatting.GOLD, ChatFormatting.ITALIC);
        } else {
            textScreen.selectScreen(0)
                    .alignToCenter(true)
                    .create(Component.translatable("gui.ultimine_addition.skills_record.no_challenges"), ChatFormatting.GRAY);
        }

        if (this.configuration.isVisible()) {
            textScreen.selectScreen(1)
                    .shouldRender(true).alignToCenter(List.of(true, false).get(Mth.floor(this.menu.getPlayer().tickCount / 20.0F) % 2))
                    .create(Component.translatable("gui.ultimine_addition.skills_record.example"), Component.literal("A: 001"), ChatFormatting.WHITE)
                    .create(Component.translatable("gui.ultimine_addition.skills_record.example"), Component.literal("B: 002"), ChatFormatting.GRAY)
                    .create(Component.translatable("gui.ultimine_addition.skills_record.example"), Component.literal("C: 003"), ChatFormatting.DARK_AQUA)
                    .create(Component.translatable("gui.ultimine_addition.skills_record.example"), Component.literal("C: 004"), ChatFormatting.GOLD);
        } else if (this.isChallengesExists && this.isMissingItems) {
            textScreen.selectScreen(1)
                    .shouldRender(true)
                    .alignToCenter(true)
                    .backgroundColor(new Color(75, 24, 24, 218))
                    .create(Component.translatable("gui.ultimine_addition.skills_record.missing_items").withStyle(ChatFormatting.RED));

            List<ItemStack> missingItems = new ArrayList<>();
            if (!this.menu.getAllSlots().get(4).hasItem()) missingItems.add(new ItemStack(Registration.PEN));
            if (!this.menu.getAllSlots().get(5).hasItem()) missingItems.add(Items.PAPER.getDefaultInstance());
            for (ItemStack item : missingItems) {
                textScreen.add(Component.literal("§c• ")
                        .append(Component.empty().append(item.getHoverName())
                                .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(item)))))
                        .getVisualOrderText());
            }

        } else if (this.isChallengesExists && this.notEnoughInk) {
            textScreen.selectScreen(1)
                    .shouldRender(true)
                    .alignToCenter(true)
                    .backgroundColor(new Color(75, 24, 24, 218))
                    .create(Component.translatable("gui.ultimine_addition.skills_record.not_enough_ink").withStyle(ChatFormatting.RED));
        } else textScreen.selectScreen(1)
                .shouldRender(false);

        this.textScreen.selectLastScreen(true)
                .renderAllBoxes(guiGraphics, Color.DARK_GRAY.getRGB(), this.configuration.getBackgroundColor().color);
    }

    private void renderScroll(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        int Y = (int) Mth.lerp(this.getScrollOffsetDelta(), y + 16, y + 86);
        boolean isOverButton = MouseHelper.isMouseOver(mouseX, mouseY, x + 174, Y, 0, 0, 9, 10);

        guiGraphics.setColor(this.configuration.getBackgroundColor().getRed(), this.configuration.getBackgroundColor().getGreen(), this.configuration.getBackgroundColor().getBlue(), this.configuration.getBackgroundColor().getAlpha());
        guiGraphics.blitSprite(this.SCROLLBAR_SPRITE.get(this.textScreen.canScroll(), isOverButton), x + 174, Y, 10, 13);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderProgressBar(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        if (!this.isProgressionBarShown) return;

        PoseStack poseStack = guiGraphics.pose();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blitSprite(PROGRESS_BAR_SPRITE, x + 10, y + 91, 156, 7);
        float value = (float) this.currentProgress / this.maxProgress;
        int bar = (int) Mth.lerp(value, 0, 154);

        poseStack.pushPose();
        poseStack.translate(x + 11, y + 97, 0);
        poseStack.mulPose(Axis.ZN.rotationDegrees(90));
        guiGraphics.fillGradient(RenderType.guiOverlay(), 0, 0, 5, bar, ColorUtils.RGBToRGBA(getProgressionBarColor(), 0.55F), ColorUtils.RGBToRGBA(getProgressionBarColor(), 0.75F), 0);
        poseStack.popPose();

        if (this.configuration.isVisible()) return;
        if (MouseHelper.isMouseOver(mouseX, mouseY, x, y, 10, 91, 156, 7)) {
            Component component = Component.translatable("gui.ultimine_addition.skills_record.progress", Component.literal("%" + this.currentProgress).withStyle(Style.EMPTY.withColor(getProgressionBarColor()))).withStyle(ChatFormatting.GRAY);
            guiGraphics.renderTooltip(this.font, component, mouseX - 50, mouseY - 5);
        }
    }

    private void renderTextBoxComponent(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.configuration.isVisible()) return;

        Style style = this.textScreen.getComponentStyleAt(mouseX, mouseY);
        if (style != null && style.getHoverEvent() != null) {
            var styleHoverEvent = style.getHoverEvent();
            if (styleHoverEvent.getValue(HoverEvent.Action.SHOW_TEXT) != null) {
                Component component = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);
                if (component != null) {
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    guiGraphics.renderTooltip(this.font, this.font.split(component, Math.max(this.width / 2, 200)), mouseX, mouseY);
                }
            }
            if (styleHoverEvent.getValue(HoverEvent.Action.SHOW_ITEM) != null) {
                var stackInfo = styleHoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
                if (stackInfo == null) return;
                int X = -10;
                int Y = -30;

                guiGraphics.pose().pushPose();
                this.renderItemTooltip(guiGraphics, stackInfo.getItemStack(), style, mouseX + X, mouseY + Y);
                guiGraphics.pose().popPose();
            }
        }
    }

    private void renderItemTooltip(GuiGraphics guiGraphics, ItemStack stack, Style style, int mouseX, int mouseY) {
        assert this.minecraft != null;
        AtomicInteger length = new AtomicInteger();
        List<Component> tooltip = Screen.getTooltipFromItem(this.minecraft, stack);
        List<ClientTooltipComponent> tooltipComponents = tooltip.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        tooltipComponents.forEach(component -> length.set(Math.max(length.get(), component.getWidth(this.font))));

        if (style.getClickEvent() != null) {
            Component component = Component.translatable("tooltip.ultimine_addition.skills_record.press.left_click").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            tooltipComponents.addAll(Language.getInstance().getVisualOrder(this.font.getSplitter().splitLines(component, Math.max(length.get(), 150), Style.EMPTY)).stream().map(ClientTooltipComponent::create).toList());
        }

        boolean isExceedingWindow = (mouseX + 12 + 18) + length.get() > this.width;
        int alignPosX = isExceedingWindow ? 6 : 0;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        guiGraphics.setColor(this.configuration.getBackgroundColor().getRed(), this.configuration.getBackgroundColor().getGreen(), this.configuration.getBackgroundColor().getBlue(), this.configuration.getBackgroundColor().getAlpha());
        guiGraphics.blitSprite(ITEM_DISPLAY_SPRITE, mouseX + alignPosX, mouseY, 26, 26);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.pose().popPose();
        guiGraphics.renderFakeItem(stack, mouseX + alignPosX + 5, mouseY + 5);
        int spacing = 6 - tooltipComponents.size() * 6;
        guiGraphics.renderTooltipInternal(this.font, tooltipComponents, mouseX + 18, mouseY + 21 + spacing, DefaultTooltipPositioner.INSTANCE);
    }

    private boolean isConsumeChallengeExists() {
        AtomicBoolean result = new AtomicBoolean();
        this.menu.getCardSlots().forEach(slot -> {
            ItemStack stack = slot.getItem();
            MiningSkillCardData cardData = MiningSkillCardData.loadData(stack);
            if (!cardData.getChallenges().stream().filter(identifier -> {
                if (ChallengesManager.INSTANCE.getAllChallenges().get(identifier.getId()) == null) return false;
                return ChallengesManager.INSTANCE.getAllChallenges().get(identifier.getId()).getChallengeType().isConsuming();
            }).toList().isEmpty())
                result.set(true);
        });
        return result.get();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            if (this.isScrollHovered(mouseX, mouseY)) {
                return this.isScrolling = this.textScreen.canScroll();
            }
            Style style = this.textScreen.getComponentStyleAt(mouseX, mouseY);
            if (style != null && style.getClickEvent() != null) {
                if (style.getClickEvent().getValue().contains("cycle")) {
                    itemCycle++;
                    return true;
                }
                if (this.selectedSlot > -1 && style.getClickEvent().getValue().contains("pin")) {
                    String value = style.getClickEvent().getValue().split(",")[1];
                    ResourceLocation challengeId = ResourceLocation.parse(value);

                    var data = this.menu.getData();
                    data.togglePinned(this.selectedSlot, challengeId).saveData(data.get());
                    return true;
                } else if (this.selectedSlot > -1 && style.getClickEvent().getValue().contains("edit")) {
                    String challengeId = style.getClickEvent().getValue().split(",")[1];
                    HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to edit this challenge!"));
                    String cmd = "/ua mining_skill_card challenges @s %s in_skills_record %s %s ".formatted(challengeId, this.menu.interactionHand.isEmpty(), this.selectedSlot);
                    ClickEvent click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd);
                    Component component = Component.literal(challengeId).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withUnderlined(true).withHoverEvent(hover).withClickEvent(click));
                    this.menu.getPlayer().sendSystemMessage(Component.literal("Challenge ID: ").append(component).withStyle(ChatFormatting.GRAY));
                    return true;
                }
            }
        }
        
        if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
            if (this.hoveredSlot != null && this.hoveredSlot.getItem() != ItemStack.EMPTY && this.menu.getCardSlots().contains(this.hoveredSlot)) {
                if (selectedSlot == -1 || selectedSlot != this.hoveredSlot.getContainerSlot()) {
                    this.selectedSlot = this.hoveredSlot.getContainerSlot();
                } else {
                    this.selectedSlot = -1;
                }

                var data = this.menu.getData();
                data.setSelectedCard(this.selectedSlot).sendToServer().saveData(data.get());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.getFocused() != null) {
            if (this.getFocused().mouseReleased(mouseX, mouseY, button)) {
                this.clearFocus();
                this.setFocused(null);
                return true;
            }
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            this.isScrolling = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.getFocused() != null) {
            if (this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY))
                return true;
        }
        
        if (this.isScrolling) {
            int i = this.topPos + 16;
            int j = i + 86;
            float delta = Mth.clamp((float) ((mouseY - i) / (j - i)), 0.0F, 1.0F);
            this.textScreen.scrollTo((int) Mth.lerp(delta, 0, this.textScreen.getRemainingLines()), true);
            return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;
        
        if (this.textScreen.canScroll()) {
            if (scrollY < 0) this.textScreen.scrollTo(+1, false);
            if (scrollY > 0) this.textScreen.scrollTo(-1, false);
            return true;
        }
        return false;
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        if (this.getFocused() instanceof AbstractDraggableWidget component) {
            if (component.isMouseOver(mouseX, mouseY))
                return false;
        }
        return super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) 
            return true;
        
        if (this.hoveredSlot != null) 
            return false;

        if (KeyBindingHooks.isMatches(KeyHandler.KEY_SHOW_PROGRESSION_BAR, keyCode, scanCode)) {
            if (this.isChallengesExists && !this.isMissingItems && !this.notEnoughInk && !this.configuration.isVisible() && this.configuration.getProgressMode() == 1)
                this.isProgressionBarShown = true;
        }
        
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (super.keyReleased(keyCode, scanCode, modifiers))
            return true;
        
        if (!KeyBindingHooks.isMatches(KeyHandler.KEY_SHOW_PROGRESSION_BAR, keyCode, scanCode)) {
            if (this.configuration.getProgressMode() == 1) this.isProgressionBarShown = false;
            return true;
        }
        
        return false;
    }

    private float getScrollOffsetDelta() {
        float offset = this.textScreen.getScrollOffset() / Math.max(1.0F, this.textScreen.getRemainingLines());
        return Mth.clamp(offset, 0.0F, 1.0F);
    }

    private boolean isScrollHovered(double mouseX, double mouseY) {
        int y = (int) Mth.lerp(this.getScrollOffsetDelta(), 16, 86);
        return MouseHelper.isMouseOver(mouseX, mouseY, this.leftPos, this.topPos, 174, y, 10, 13);
    }

    private void createChallengesText(NonNullList<Slot> slots) {
        ItemStack stack = slots.get(this.selectedSlot).getItem();
        MiningSkillCardData cardData = MiningSkillCardData.loadData(stack);
        ChallengesManager manager = ChallengesManager.INSTANCE;

        int currentValues = 0;
        int requiredValues = 0;
        for (MiningSkillCardData.ChallengeHolder challengeHolder : cardData.getChallenges()) {
            List<ItemStack> itemList = new ArrayList<>(List.of(ItemStack.EMPTY));
            ChallengesData challengesData = manager.getAllChallenges().get(challengeHolder.getId());
            String type = "null";

            if (challengesData != null) {
                itemList.clear();
                type = challengesData.getChallengeType().getTypeName();
                ChallengesManager.INSTANCE.utilizeTargetedBlocks(challengesData).forEach(block -> itemList.add(new ItemStack(block)));
                currentValues += challengeHolder.getCurrentPoints();
                requiredValues += challengeHolder.getRequiredPoints();
            }

            ItemStack displayItem = itemList.get(Mth.floor(itemCycle) % itemList.size());
            var hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(displayItem));
            Style questStyle = Style.EMPTY.withHoverEvent(hoverEvent).withItalic(true).withColor(new Color(0xBC9F7D).getRGB());
            if (itemList.size() > 1) {
                questStyle = questStyle.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, "cycle"));
            }

            Component title = ComponentHelper.limitComponent(Component.literal("》").append(Component.translatable("challenge.ultimine_addition.title", challengeHolder.getOrder()).setStyle(Style.EMPTY.withColor(new Color(0xFAF1C1).getRGB()))).append("《").withColor(new Color(0x7A7A7A).getRGB()), this.textScreen.getWidth());
            Component description = createChallengeDescription(type, questStyle, itemList, itemCycle);
            Style descStyle = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY);
            Style counterStyle = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GOLD);

            boolean isConsumeNeeded = challengesData != null && challengesData.getChallengeType().isConsuming() && !this.menu.getData().isConsumeMode()  && !cardData.isChallengeAccomplished(challengeHolder.getId());
            if (isConsumeNeeded) {
                HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("challenge.ultimine_addition.consume.info").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                counterStyle = Style.EMPTY.withHoverEvent(hover).withColor(ChatFormatting.RED);

            } else if (challengesData != null && challengesData.getChallengeType().isConsuming() && this.menu.getData().isConsumeMode() && !cardData.isChallengeAccomplished(challengeHolder.getId())) {
                HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("✖ ").append(Component.translatable("challenge.ultimine_addition.consume")).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                counterStyle = Style.EMPTY.withItalic(true).withHoverEvent(hover).withColor(ChatFormatting.LIGHT_PURPLE);

            } else if (cardData.isChallengeAccomplished(challengeHolder.getId())) {
                counterStyle = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GREEN);
            }

            List<FormattedCharSequence> result = new ArrayList<>(Language.getInstance().getVisualOrder(this.font.getSplitter().splitLines(title, this.textScreen.getWidth(), title.getStyle())));
            result.addAll(Language.getInstance().getVisualOrder(this.font.getSplitter().splitLines(description, this.textScreen.getWidth(), descStyle)));

            FormattedCharSequence counter = FormattedCharSequence.composite(FormattedCharSequence.forward("➤ ", counterStyle), FormattedCharSequence.forward(("%s/%s".formatted(challengeHolder.getCurrentPoints(), challengeHolder.getRequiredPoints())), counterStyle.withStrikethrough(isConsumeNeeded)));

            FormattedCharSequence buttons;
            HoverEvent pinHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("gui.ultimine_addition.skills_record.pin").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            FormattedCharSequence pinButton = FormattedCharSequence.forward("◎", Style.EMPTY
                    .withColor(challengeHolder.isPinned() ? ChatFormatting.YELLOW : ChatFormatting.GRAY)
                    .withStrikethrough(!challengeHolder.isPinned())
                    .withHoverEvent(pinHover)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, "pin,%s".formatted(challengeHolder.getId().toString())))
            );

            FormattedCharSequence editButton = FormattedCharSequence.forward("✎", Style.EMPTY
                    .withColor(ChatFormatting.GRAY)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, "edit,%s".formatted(challengeHolder.getId().toString())))
            );

            buttons = FormattedCharSequence.composite(
                    FormattedCharSequence.forward(" | ", Style.EMPTY.withColor(ChatFormatting.GRAY)),
                    pinButton
            );

            if (ConfigHandler.CLIENT.SR_EDIT_MODE.get() && this.menu.getPlayer().hasPermissions(Commands.LEVEL_GAMEMASTERS))
                buttons = FormattedCharSequence.composite(
                        buttons,
                        FormattedCharSequence.forward(" ", Style.EMPTY.withColor(ChatFormatting.GRAY)),
                        editButton
                );

            result.add(FormattedCharSequence.composite(counter, buttons));
            if (!this.textScreen.isEmpty()) this.textScreen.add(FormattedCharSequence.EMPTY).addAll(result);
            else this.textScreen.addAll(result);
        }
        this.calculateProgression(currentValues, requiredValues);
    }

    public static Component createChallengeDescription(String challengeType, Style style, List<ItemStack> items, float cycle) {
        ItemStack displayItem = items.get(Mth.floor(cycle) % items.size());
        Component info;
        if (items.size() > 1) {
            info = Component.translatable("challenge.ultimine_addition.various_blocks", Component.literal(displayItem.getHoverName().getString()).withStyle(style));
        } else info = Component.literal(items.getFirst().getHoverName().getString()).withStyle(style);
        return Component.translatable("challenge.ultimine_addition.%s".formatted(challengeType), info);
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

    public Collection<Rect2i> getComponentsRectangle() {
        Collection<Rect2i> collection = new HashSet<>();
        collection.add(RenderUtils.createRect2i(this.configuration));
        return collection;
    }

    public static float getItemCycle() {
        return itemCycle;
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

        public Color convert() {
            return color;
        }
    }
}