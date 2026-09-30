package net.ixdarklord.ultimine_addition.client.gui.screens;

import net.ixdarklord.coolcatcanvas.api.utils.ColorGradient;
import net.ixdarklord.coolcatcanvas.api.client.utils.Outline;
import net.ixdarklord.ultimine_addition.client.renderer.ItemAlpha;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.coolcatcore.api.config.client.ConfigScreens;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.ixdarklord.ultimine_addition.config.UAConfigs;
import net.ixdarklord.coolcatcanvas.api.client.gui.components.ColorableImageButton;
import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.AbstractDraggableWidget;
import net.ixdarklord.coolcatcore.api.client.utils.MouseHelper;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.ixdarklord.coolcatcore.api.utils.ColorUtils;
import net.ixdarklord.coolcatcore.api.utils.MathUtils;
import net.ixdarklord.ultimine_addition.client.gui.components.SlotSelectionOutline;
import net.ixdarklord.ultimine_addition.client.gui.theme.RecordTheme;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardViewerWidget;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.data.record.CardHistory;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordClientCache;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.common.menu.slot.CustomSlot;
import net.ixdarklord.ultimine_addition.common.menu.slot.MiningSkillCardSlot;
import net.ixdarklord.ultimine_addition.common.menu.slot.PaperSlot;
import net.ixdarklord.ultimine_addition.common.menu.slot.PenSlot;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.SkillsRecordPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SkillsRecordScreen extends AbstractContainerScreen<SkillsRecordMenu> implements CardViewerWidget.Actions {
    public static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(FTBUltimineAddition.id("container/skills_record/button"), FTBUltimineAddition.id("container/skills_record/button_disabled"), FTBUltimineAddition.id("container/skills_record/button_focused"));
    private static final Identifier BACKGROUND_TEXTURE = FTBUltimineAddition.getGuiTexture("container/skills_record", "png");
    private static final Identifier SLOT_SELECT_SPRITE = FTBUltimineAddition.id("container/skills_record/slot_select");
    public static final Identifier PROGRESS_BAR_SPRITE = FTBUltimineAddition.id("container/skills_record/progress_bar");
    public static final WidgetSprites CONFIGURATION_BUTTON_SPRITES = new WidgetSprites(FTBUltimineAddition.id("container/skills_record/configuration_button_enabled"), FTBUltimineAddition.id("container/skills_record/configuration_button_disabled"), FTBUltimineAddition.id("container/skills_record/configuration_button_focused"));
    private static final WidgetSprites CONSUME_BUTTON_SPRITES = new WidgetSprites(FTBUltimineAddition.id("container/skills_record/consume_on"), FTBUltimineAddition.id("container/skills_record/consume_off"), FTBUltimineAddition.id("container/skills_record/consume_on_focused"), FTBUltimineAddition.id("container/skills_record/consume_off_focused"));

    private static final int VIEWER_X = 9, VIEWER_Y = 20, VIEWER_WIDTH = 170, VIEWER_HEIGHT = 87;
    private static final int SIDE_BUTTON_X = 170;
    private static final int SLOT_MARKER_Y = 107;
    private static final int LABEL_FRAME = 0xFF656565;

    private ColorableImageButton configurationButton;
    private ConsumeButton consumeButton;
    private CardViewerWidget viewer;

    private SkillsRecordScreen.OverlayColor backgroundColor;
    private RecordTheme theme = RecordTheme.WHITE;
    private boolean isAnimationsEnabled;

    public int selectedSlot;
    private boolean isChallengesExists;
    private boolean isMissingItems;
    private boolean notEnoughInk;
    private boolean lock;

    public SkillsRecordScreen(SkillsRecordMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 188, 232);
    }

    public SkillsRecordScreen lock(boolean lock) {
        this.lock = lock;
        return this;
    }

    @Override
    public void init() {
        super.init();

        this.titleLabelX = 8;
        this.titleLabelY = 7;
        this.inventoryLabelX = (this.imageWidth / 2) - (this.font.width(this.playerInventoryTitle) / 2);
        this.inventoryLabelY = this.imageHeight - 96;

        this.createButtons();
        this.selectedSlot = this.menu.getData().getSelectedCard();

        this.syncConfig();

        // Kept across re-inits so an open details panel, zoom and pan survive the edit dialog.
        if (this.viewer == null) {
            this.viewer = new CardViewerWidget(this.leftPos + VIEWER_X, this.topPos + VIEWER_Y, VIEWER_WIDTH, VIEWER_HEIGHT, this);
        } else {
            this.viewer.setCompactBounds(this.leftPos + VIEWER_X, this.topPos + VIEWER_Y, VIEWER_WIDTH, VIEWER_HEIGHT);
        }
        // Fit and expand go on the title bar, left of the configuration button.
        this.viewer.setToolbarAnchor(this.configurationButton.getX() - 2, this.configurationButton.getY());
        this.addWidget(this.viewer);
    }

    @Override
    public void clearWidgets() {
        super.clearWidgets();
    }

    private void createButtons() {
        this.configurationButton = this.addRenderableWidget(new ColorableImageButton(this.leftPos + SIDE_BUTTON_X, this.topPos + 6, 10, 10, CONFIGURATION_BUTTON_SPRITES,
                // CoolCatLib's category popup, floating over the book; it comes back here without closing the menu.
                button -> this.minecraft.setScreen(ConfigScreens.categoryPopup(this, UAClientConfig.CONFIG, UAClientConfig.SKILLS_RECORD_CATEGORY, UAConfigs.SKILLS_RECORD_POPUP_THEME))));

        this.consumeButton = this.addRenderableWidget(new ConsumeButton(this.leftPos + SIDE_BUTTON_X, this.topPos + 114, 10, 18, this.menu.getData().isConsumeModeActive()) {
            @Override
            protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                if (!SkillsRecordItem.isConsumeChallengeExists(SkillsRecordScreen.this.menu.getData()) && this.isStateTriggered) {
                    this.isStateTriggered = false;
                }

                OverlayColor color = SkillsRecordScreen.this.backgroundColor;
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SkillsRecordScreen.CONSUME_BUTTON_SPRITES.get(this.isActive() && this.isStateTriggered, this.isActive() && this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height, color.argb());
                if (!this.isActive()) {
                    guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, ARGB.multiply(ColorUtils.rgbToRgba(Color.BLACK, 0.15F), color.argb()));
                }

                if (this.isActive()) {
                    double value = SkillsRecordScreen.this.isAnimationsEnabled ? (double) MathUtils.cycledBetweenValues(0.5F, 1.0F, 0.75F, (float) SkillsRecordScreen.this.menu.getPlayer().tickCount / 20.0F, false) : 1.0;
                    Color cTo = this.isStateTriggered ? new Color(2337827) : new Color(11018786);
                    Color cFrom = this.isStateTriggered ? ColorUtils.blend(new Color(3669815), cTo, value) : ColorUtils.blend(new Color(16725815), cTo, value);
                    Color color1 = this.isStateTriggered ? cFrom : cTo;
                    Color color2 = this.isStateTriggered ? cTo : cFrom;
                    guiGraphics.fillGradient(this.getX() + 1, this.getY() + (this.isStateTriggered ? 1 : 9), this.getX() + 9, this.getY() + (this.isStateTriggered ? 9 : 17), color1.getRGB(), color2.getRGB());
                }

                if (this.isActive() && this.isHovered()) {
                    Component state = SkillsRecordScreen.this.menu.getData().isConsumeModeActive() ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN) : Component.translatable("options.off").withStyle(ChatFormatting.RED);
                    Component info = Component.literal("➤ ").withStyle(ChatFormatting.GRAY).append(Component.translatable("gui.ultimine_addition.skills_record.consume", state).withStyle(ChatFormatting.WHITE));
                    guiGraphics.setTooltipForNextFrame(SkillsRecordScreen.this.font, info, mouseX, mouseY);
                }
            }

            @Override
            public void onPress(InputWithModifiers input) {
                this.isStateTriggered ^= true;
                SkillsRecordScreen.this.menu.getData().setConsumeMode(this.isStateTriggered);
                PayloadHandler.sendToServer(new SkillsRecordPayload.ToggleConsumeMode(this.isStateTriggered));
            }
        });
    }

    @Nullable MiningSkillCardData getSelectedCard() {
        if (this.selectedSlot < 0) return null;
        ItemStack stack = this.menu.getCardSlots().get(this.selectedSlot).getItem();
        return MiningSkillCardData.hasData(stack) ? MiningSkillCardData.load(stack) : null;
    }

    // Read every frame, so the book follows its settings while they're edited: in the settings popup drawn over it,
    // or in the config file (reloaded while the game runs).
    private void syncConfig() {
        // The look comes from the record's dye.
        this.theme = RecordTheme.of(this.menu.getRecordColor());
        this.backgroundColor = OverlayColor.of(this.theme.tint());
        this.isAnimationsEnabled = UAClientConfig.ANIMATIONS_MODE.get();
    }

    public void update() {
        this.syncConfig();
        if (this.selectedSlot > -1 && this.menu.getCardSlots().get(this.selectedSlot).getItem().isEmpty()) {
            this.selectCard(-1);
        }

        MiningSkillCardData card = this.getSelectedCard();
        this.isChallengesExists = card != null && !card.getChallenges().isEmpty();
        boolean hasCorrectGamemode = !this.menu.getPlayer().isCreative() && !this.menu.getPlayer().isSpectator();
        this.isMissingItems = hasCorrectGamemode && (!this.menu.getAllSlots().get(SkillsRecordData.PEN_SLOT).hasItem() || !this.menu.getAllSlots().get(SkillsRecordData.PAPER_SLOT).hasItem());
        this.notEnoughInk = hasCorrectGamemode && card != null && this.menu.getInkAmount() == 0;

        boolean blocked = this.lock || this.viewer.isExpandedWindow();
        this.menu.getAllSlots().forEach((slot) -> ((CustomSlot) slot).setEnabled(!blocked));

        List<ItemStack> missingItems = new ArrayList<>();
        if (this.isMissingItems) {
            if (!this.menu.getAllSlots().get(SkillsRecordData.PEN_SLOT).hasItem()) missingItems.add(new ItemStack(Registration.PEN.get()));
            if (!this.menu.getAllSlots().get(SkillsRecordData.PAPER_SLOT).hasItem()) missingItems.add(Items.PAPER.getDefaultInstance());
        }
        ItemStack cardStack = card == null ? ItemStack.EMPTY : this.menu.getCardSlots().get(this.selectedSlot).getItem();
        CardHistory history = card == null ? null : SkillsRecordClientCache.getHistory(card.getUUID()).orElse(null);
        this.viewer.update(new CardViewerWidget.State(!this.menu.isCardSlotsEmpty(), cardStack, card, history,
                this.menu.getData().isConsumeModeActive(), missingItems, this.notEnoughInk, false,
                this.theme, this.isAnimationsEnabled));

        for (GuiEventListener child : this.children()) {
            if (child instanceof AbstractWidget widget) {
                widget.active = !this.lock;
            }
        }

        if (this.configurationButton != null) {
            this.configurationButton.setColor(this.backgroundColor.color);
        }

        if (this.consumeButton != null) {
            if (this.consumeButton.active) {
                this.consumeButton.active = !blocked && SkillsRecordItem.isConsumeChallengeExists(this.menu.getData());
            }
            if (this.consumeButton.isFocused()) {
                this.consumeButton.setFocused(false);
            }
        }
    }

    private void selectCard(int slot) {
        this.selectedSlot = slot;
        this.menu.getData().setSelectedCard(slot);
        PayloadHandler.sendToServer(new SkillsRecordPayload.SelectCard(slot));
    }


    @Override
    public void togglePin(Identifier challengeId) {
        if (this.selectedSlot < 0) return;
        this.menu.getData().togglePinned(this.selectedSlot, challengeId);
        PayloadHandler.sendToServer(new SkillsRecordPayload.PinChallenge(this.selectedSlot, challengeId));
    }

    @Override
    public void editChallenge(Identifier challengeId) {
        MiningSkillCardData card = this.getSelectedCard();
        if (card == null) return;
        card.getChallenge(challengeId).ifPresent(challenge -> Objects.requireNonNull(this.minecraft).setScreen(new EditChallengeScreen(this, challenge)));
    }

    @Override
    public void rerollChallenge(Identifier challengeId) {
        if (this.selectedSlot < 0) return;
        PayloadHandler.sendToServer(new SkillsRecordPayload.RerollChallenge(this.selectedSlot, challengeId));
    }

    @Override
    public void claimCertificate(MiningSkillCardItem.Tier tier, Identifier shape) {
        if (this.selectedSlot < 0) return;
        PayloadHandler.sendToServer(new SkillsRecordPayload.ClaimCertificate(this.selectedSlot, tier.getValue(), shape));
    }

    @Override
    public int getInkAmount() {
        return this.menu.getData().getInkAmount();
    }

    @Override
    public void onExpandedChanged(boolean expanded) {
        this.setFocused(this.viewer);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.update();
        super.extractContents(guiGraphics, mouseX, mouseY, partialTick);

        // New stratum so the markers draw over slot items and highlights.
        guiGraphics.nextStratum();
        this.renderSlotDecorations(guiGraphics, this.leftPos, this.topPos);
        if (!this.viewer.isExpandedWindow()) {
            this.viewer.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (this.configurationButton.isHovered()) {
            guiGraphics.setTooltipForNextFrame(this.font, Component.literal("➤ ").withStyle(ChatFormatting.GRAY).append(Component.translatable("gui.ultimine_addition.skills_record.configuration").withStyle(ChatFormatting.WHITE)), mouseX, mouseY);
        }
        if (this.viewer.isExpandedWindow()) {
            this.viewer.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        }
        // Only while the tree itself is being navigated: not under a details, shape choice or message panel, nor under
        // the settings popup (which draws this screen behind it).
        if (UAClientConfig.SR_EDIT_MODE.get() && this.viewer.isTreeShown() && this.minecraft.screen == this) {
            this.renderZoomDebug(guiGraphics);
        }
    }

    // Left of the book, or of the expanded window.
    private void renderZoomDebug(GuiGraphicsExtractor guiGraphics) {
        guiGraphics.nextStratum();
        String text = String.format(Locale.ROOT, "Zoom %.2fx (%.0f%%)", this.viewer.getZoom(), this.viewer.getZoomProgress() * 100);
        boolean expanded = this.viewer.isExpandedWindow();
        int right = (expanded ? this.viewer.getX() : this.leftPos) - 4;
        int x = Math.max(2, right - this.font.width(text)), y = (expanded ? this.viewer.getY() : this.topPos) + 4;
        guiGraphics.fill(x - 2, y - 2, x + this.font.width(text) + 2, y + 9, 0xA0000000);
        guiGraphics.text(this.font, text, x, y, 0xFFFFFF55, false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 512, this.backgroundColor.argb());
        // With the background, so slot items draw above it.
        this.renderGhostItem(guiGraphics, this.leftPos, this.topPos);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        int color = this.theme.labelColor();
        guiGraphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, color, false);
        // The inventory label's patch is the opposite of the label (light behind dark text, dark behind light), so
        // the two never blend.
        boolean darkLabel = ARGB.red(color) * 0.299 + ARGB.green(color) * 0.587 + ARGB.blue(color) * 0.114 < 128;
        int x0 = this.inventoryLabelX - 1, y0 = this.inventoryLabelY - 1;
        int x1 = this.inventoryLabelX + this.font.width(this.playerInventoryTitle), y1 = this.inventoryLabelY + this.font.lineHeight;
        guiGraphics.fill(x0, y0, x1, y1, darkLabel ? 0x40FFFFFF : 0x50000000);
        // Framed on its left, top and right in the gray of the inventory's border; open at the bottom, where it sits
        // on that border. The top corners are left out, rounding them by a pixel.
        int frame = ARGB.multiply(LABEL_FRAME, this.theme.tint());
        guiGraphics.fill(x0, y0 - 1, x1, y0, frame);
        guiGraphics.fill(x0 - 1, y0, x0, y1, frame);
        guiGraphics.fill(x1, y0, x1 + 1, y1, frame);
        guiGraphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, color, false);
    }

    // A pen, paper or the ink the challenges need is missing: a red wash over the slot and a red outline running
    // around its inside.
    private static final int REQUIRED_OVERLAY = 0x55FF2020;
    private static final Outline REQUIRED_OUTLINE = Outline.gradient(ColorGradient.of(0xFF2A2A, 0xFF9A6A, 0xA01010)
            .withSpeed(0.8F).withSpread(1.0F));

    // The placeholder in an empty card, pen or paper slot: faded and greyed, so it reads as a hint, not an item.
    private static final float GHOST_ALPHA = 0.6F;
    private static final int GHOST_TINT = 0xB4B4B4;

    private void renderGhostItem(GuiGraphicsExtractor guiGraphics, int x, int y) {
        Matrix3x2fStack poseStack = guiGraphics.pose();
        List<ItemStack> listOfCards = List.of(
                new ItemStack(Registration.MINING_SKILL_CARD_PICKAXE.get()),
                new ItemStack(Registration.MINING_SKILL_CARD_AXE.get()),
                new ItemStack(Registration.MINING_SKILL_CARD_SHOVEL.get()),
                new ItemStack(Registration.MINING_SKILL_CARD_HOE.get())
        );
        boolean blocked = this.lock;
        ItemStack displayItem = ItemStack.EMPTY;
        for (Slot slot : this.menu.getAllSlots()) {
            if (slot instanceof MiningSkillCardSlot)
                displayItem = listOfCards.get(Mth.floor(this.menu.getPlayer().tickCount / 20.0F) % listOfCards.size());
            if (slot instanceof PenSlot) displayItem = new ItemStack(Registration.PEN.get());
            if (slot instanceof PaperSlot) displayItem = new ItemStack(Items.PAPER);
            if (displayItem.isEmpty()) return;

            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || blocked) {
                float size = MathUtils.cycledBetweenValues(0.75F, 0.90F, 1.0F, this.menu.getPlayer().tickCount / 20.0F, true);
                size = this.isAnimationsEnabled ? size : 0.90F;

                poseStack.pushMatrix();
                poseStack.translate(x + slot.x + 8.0F, y + slot.y + 8.0F);
                poseStack.scale(size, size);
                ItemStack ghost = displayItem;
                ItemAlpha.draw(GHOST_ALPHA, GHOST_TINT, () -> guiGraphics.fakeItem(ghost, -8, -8));
                poseStack.popMatrix();
            }

            boolean isMissingItems = this.isChallengesExists && this.isMissingItems && (slot instanceof PenSlot || slot instanceof PaperSlot);
            boolean notEnoughInk = this.isChallengesExists && this.notEnoughInk && slot instanceof PenSlot;
            // Only the pen and paper slots turn red; empty card slots stay grey.
            boolean warn = isMissingItems || notEnoughInk;
            int color = warn ? 0xff0000 : 0x8b8b8b;
            float alpha = warn ? 0.15F : 0.55F;

            if ((isMissingItems && stack.isEmpty()) || notEnoughInk) {
                // Over the ghost item, in a layer of its own.
                guiGraphics.nextStratum();
                guiGraphics.fill(x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, REQUIRED_OVERLAY);
                REQUIRED_OUTLINE.draw(guiGraphics, x + slot.x, y + slot.y, 16, 16);
            } else if (stack.isEmpty() || blocked) {
                guiGraphics.fill(x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, ARGB.multiply(ColorUtils.rgbToRgba(color, alpha), this.backgroundColor.argb()));
            }
        }
    }

    private void renderSlotDecorations(GuiGraphicsExtractor guiGraphics, int x, int y) {
        if (this.lock) return;
        int tint = this.backgroundColor.argb();

        if (!this.menu.getSlot(SkillsRecordData.PEN_SLOT).getItem().isEmpty())
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SELECT_SPRITE, x + 131, y + SLOT_MARKER_Y, 4, 8, tint);

        if (!this.menu.getSlot(SkillsRecordData.PAPER_SLOT).getItem().isEmpty())
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SELECT_SPRITE, x + 153, y + SLOT_MARKER_Y, 4, 8, tint);

        if (this.selectedSlot == -1) return;
        int X = 14 + (22 * this.selectedSlot);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SELECT_SPRITE, x + X, y + SLOT_MARKER_Y, 4, 8, tint);
        Slot slot = this.menu.getCardSlots().get(this.selectedSlot);
        SlotSelectionOutline.draw(guiGraphics, x + slot.x, y + slot.y, x + X, 4, y + SLOT_MARKER_Y, this.backgroundColor.argb(), 1.0F);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x(), mouseY = event.y();
        int button = event.button();

        if (this.viewer.isExpandedWindow()) {
            if (this.viewer.isMouseOver(mouseX, mouseY) && this.viewer.mouseClicked(event, doubleClick)) {
                this.setFocused(this.viewer);
                this.setDragging(true);
                return true;
            }
            if (!this.viewer.isMouseOver(mouseX, mouseY)) this.viewer.setExpanded(false);
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
            if (this.hoveredSlot != null && !this.hoveredSlot.getItem().isEmpty() && this.menu.getCardSlots().contains(this.hoveredSlot)) {
                int slot = this.hoveredSlot.getContainerSlot();
                this.selectCard(this.selectedSlot == slot ? -1 : slot);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.getFocused() != null) {
            if (this.getFocused().mouseReleased(event)) {
                if (this.getFocused() != this.viewer) {
                    this.clearFocus();
                    this.setFocused(null);
                }
                this.setDragging(false);
                return true;
            }
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.getFocused() != null && this.isDragging()) {
            if (this.getFocused().mouseDragged(event, dragX, dragY))
                return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // AbstractContainerScreen doesn't pass scrolling to widgets.
        if (this.viewer.isExpandedWindow() || (!this.lock && this.viewer.isMouseOver(mouseX, mouseY))) {
            return this.viewer.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || this.viewer.isExpandedWindow();
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        if (this.lock) return false;
        if (this.viewer != null && this.viewer.isExpandedWindow()) return false;

        if (this.getFocused() instanceof AbstractDraggableWidget component) {
            if (component != this.viewer && component.isMouseOver(mouseX, mouseY))
                return false;
        }
        return super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        boolean overViewer = this.viewer.isMouseOver(MouseHelper.getMouseX(), MouseHelper.getMouseY());
        if ((this.viewer.isExpandedWindow() || overViewer || event.key() == GLFW.GLFW_KEY_ESCAPE) && this.viewer.keyPressed(event)) return true;
        if (this.viewer.isExpandedWindow()) return true;
        return super.keyPressed(event);
    }

    public Collection<Rect2i> getComponentsRectangle() {
        Collection<Rect2i> collection = new HashSet<>();
        if (this.viewer != null && this.viewer.isExpandedWindow()) collection.add(RenderUtils.createRect2i(this.viewer));
        return collection;
    }

    private abstract static class ConsumeButton extends AbstractButton {
        protected boolean isStateTriggered;

        ConsumeButton(int x, int y, int width, int height, boolean initialState) {
            super(x, y, width, height, CommonComponents.EMPTY);
            this.isStateTriggered = initialState;
        }

        public boolean isStateTriggered() {
            return this.isStateTriggered;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    // The Skills Record's tint (a color from the client config).
    public static final class OverlayColor {
        public static final OverlayColor DEFAULT = new OverlayColor(new Color(255, 255, 255));

        private final Color color;

        private OverlayColor(Color color) {
            this.color = color;
        }

        public static OverlayColor of(int rgb) {
            return new OverlayColor(new Color(rgb & 0xFFFFFF));
        }

        public float alpha() {
            return new ColorUtils(color.getRGB()).alpha();
        }

        public float red() {
            return new ColorUtils(color.getRGB()).red();
        }

        public float green() {
            return new ColorUtils(color.getRGB()).green();
        }

        public float blue() {
            return new ColorUtils(color.getRGB()).blue();
        }

        public int argb() {
            return ARGB.colorFromFloat(this.alpha(), this.red(), this.green(), this.blue());
        }

        public Color convert() {
            return color;
        }
    }
}
