package net.ixdarklord.ultimine_addition.client.gui.screens;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.client.gui.components.TextScreen;
import net.ixdarklord.coolcatlib.api.client.gui.components.widgets.AbstractDraggableWidget;
import net.ixdarklord.coolcatlib.api.client.gui.components.widgets.WidgetSprites;
import net.ixdarklord.coolcatlib.api.client.utils.MouseHelper;
import net.ixdarklord.coolcatlib.api.client.utils.RenderUtils;
import net.ixdarklord.coolcatlib.api.utils.ColorUtils;
import net.ixdarklord.coolcatlib.api.utils.ComponentHelper;
import net.ixdarklord.coolcatlib.api.utils.MathUtils;
import net.ixdarklord.ultimine_addition.client.gui.components.ColoredButton;
import net.ixdarklord.ultimine_addition.client.gui.components.ConfigurationPanel;
import net.ixdarklord.ultimine_addition.client.gui.hud.ChallengesPanelManager;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
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
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.hooks.KeyBindingHooks;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packets.SkillsRecordPacket;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public final class SkillsRecordScreen extends AbstractContainerScreen<SkillsRecordMenu> {
    public static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(FTBUltimineAddition.getGuiSprite("container/skills_record/button"), FTBUltimineAddition.getGuiSprite("container/skills_record/button_disabled"), FTBUltimineAddition.getGuiSprite("container/skills_record/button_focused"));
    private static final ResourceLocation BACKGROUND_TEXTURE = FTBUltimineAddition.getGuiTexture("container/skills_record");
    private static final ResourceLocation SLOT_SELECT_SPRITE = FTBUltimineAddition.getGuiSprite("container/skills_record/slot_select");
    public static final ResourceLocation PROGRESS_BAR_SPRITE = FTBUltimineAddition.getGuiSprite("container/skills_record/progress_bar");
    private static final ResourceLocation ITEM_DISPLAY_SPRITE = FTBUltimineAddition.getGuiSprite("container/skills_record/item_display");
    public static final WidgetSprites CONFIGURATION_BUTTON_SPRITES = new WidgetSprites(FTBUltimineAddition.getGuiSprite("container/skills_record/configuration_button_enabled"), FTBUltimineAddition.getGuiSprite("container/skills_record/configuration_button_disabled"), FTBUltimineAddition.getGuiSprite("container/skills_record/configuration_button_focused"));
    private static final WidgetSprites CONSUME_BUTTON_SPRITES = new WidgetSprites(FTBUltimineAddition.getGuiSprite("container/skills_record/consume_on"), FTBUltimineAddition.getGuiSprite("container/skills_record/consume_off"), FTBUltimineAddition.getGuiSprite("container/skills_record/consume_on_focused"), FTBUltimineAddition.getGuiSprite("container/skills_record/consume_off_focused"));
    private static final WidgetSprites SCROLLBAR_SPRITE = new WidgetSprites(FTBUltimineAddition.getGuiSprite("container/skills_record/scroller_enabled"), FTBUltimineAddition.getGuiSprite("container/skills_record/scroller_disabled"), FTBUltimineAddition.getGuiSprite("container/skills_record/scroller_focused"));
    private final int maxProgress = 100;
    private ColoredButton configurationButton;
    private StateSwitchingButton consumeButton;
    public ConfigurationPanel configuration;
    private TextScreen textScreen;
    private OverlayColor backgroundColor;
    private boolean isAnimationsEnabled;
    private int progressMode;
    private static float itemCycle;
    public int selectedSlot;
    private boolean isChallengesExists;
    private boolean isMissingItems;
    private boolean notEnoughInk;
    private int currentProgress;
    private boolean isScrolling;
    private boolean isProgressionBarShown;
    private boolean lock;

    public SkillsRecordScreen(SkillsRecordMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 192;
        this.imageHeight = 224;
    }

    public SkillsRecordScreen lock(boolean lock) {
        this.lock = lock;
        return this;
    }

    public void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 5;
        this.inventoryLabelX = this.imageWidth / 2 - this.font.width(this.playerInventoryTitle) / 2;
        this.inventoryLabelY = this.imageHeight - 96;
        this.createButtons();
        this.selectedSlot = this.menu.getData().getSelectedCard();
        this.textScreen = TextScreen.build(this.leftPos + 10, this.topPos + 17, 157, 82, ConfigHandler.CLIENT.TEXT_SCREEN_SHADOW.get(), 2);
        this.backgroundColor = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
        this.isAnimationsEnabled = ConfigHandler.CLIENT.ANIMATIONS_MODE.get();
        this.progressMode = ConfigHandler.CLIENT.PROGRESS_BAR.get();
        boolean visibility = this.configuration != null && this.configuration.isVisible();
        this.configuration = this.addWidget(new ConfigurationPanel(this.leftPos + 194, this.topPos));
        this.configuration.setVisible(visibility);
        this.configuration.addButton((button) -> {
            if (!Screen.hasShiftDown()) {
                this.backgroundColor = this.backgroundColor.next();
            } else {
                this.backgroundColor = this.backgroundColor.previous();
            }

            this.saveValuesToConfig();
        }, Component.translatable("gui.ultimine_addition.skills_record.option.bg_color"), (tooltipInfo) -> {
            int color = ColorUtils.rgbToRgba(this.backgroundColor.convert().getRGB(), this.backgroundColor.alpha());
            tooltipInfo.component = Component.literal("➤ ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable(String.format("gui.ultimine_addition.color.%s", this.backgroundColor.name().toLowerCase())).withStyle(Style.EMPTY.withColor(color)));
            tooltipInfo.tooltipComponent = new SkillsRecordTooltip.Option(0, tooltipInfo.component);
        });
        this.configuration.addButton((button) -> {
            this.isAnimationsEnabled ^= true;
            this.saveValuesToConfig();
        }, Component.translatable("gui.ultimine_addition.skills_record.option.animations"), (tooltipInfo) -> {
            MutableComponent component = this.isAnimationsEnabled ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN) : Component.translatable("options.off").withStyle(ChatFormatting.RED);
            tooltipInfo.component = Component.literal("➤ ").withStyle(ChatFormatting.DARK_GRAY).append(component);
        });
        this.configuration.addButton((button) -> {
            this.progressMode = this.progressMode >= 2 ? 0 : this.progressMode + 1;
            this.saveValuesToConfig();
        }, Component.translatable("gui.ultimine_addition.skills_record.option.progression_bar"), (tooltipInfo) -> {
            MutableComponent component = Component.empty();
            switch (this.progressMode) {
                case 0 -> component = Component.translatable("options.on").withStyle(ChatFormatting.GREEN);
                case 1 ->
                        component = Component.translatable("gui.ultimine_addition.skills_record.option.hold_keybind", KeyHandler.KEY_SHOW_PROGRESSION_BAR.getTranslatedKeyMessage()).withStyle(ChatFormatting.AQUA);
                case 2 -> component = Component.translatable("options.off").withStyle(ChatFormatting.RED);
            }

            tooltipInfo.component = Component.literal("➤ ").withStyle(ChatFormatting.DARK_GRAY).append(component);
        });
        this.configuration.addButton((button) -> {
            ChallengesPanelManager.INSTANCE.cycleAlignment(!Screen.hasShiftDown());
            this.saveValuesToConfig();
        }, Component.translatable("gui.ultimine_addition.skills_record.option.panel_alignment"), (tooltipInfo) -> {
            MutableComponent component = Component.translatable("gui.ultimine_addition.skills_record.option.panel_alignment.%s".formatted(ChallengesPanelManager.INSTANCE.getPanelAlignment().getSerializedName())).withStyle(ChatFormatting.WHITE);
            tooltipInfo.component = Component.literal("➤ ").withStyle(ChatFormatting.DARK_GRAY).append(component);
            tooltipInfo.tooltipComponent = new SkillsRecordTooltip.Option(1, tooltipInfo.component);
        });
    }

    public void saveValuesToConfig() {
        ConfigHandler.CLIENT.BACKGROUND_COLOR.set(this.backgroundColor);
        ConfigHandler.CLIENT.ANIMATIONS_MODE.set(this.isAnimationsEnabled);
        ConfigHandler.CLIENT.PROGRESS_BAR.set(this.progressMode);
        ConfigHandler.CLIENT.CHALLENGES_PANEL_ALIGNMENT.set(ChallengesPanelManager.INSTANCE.getPanelAlignment());
    }

    public void clearWidgets() {
        super.clearWidgets();
    }

    private void createButtons() {
        this.configurationButton = this.addRenderableWidget(new ColoredButton(this.leftPos + 174, this.topPos + 4, 10, 10, CONFIGURATION_BUTTON_SPRITES, 10, 10, (button) -> {
            this.configuration.toggleVisibility(true);
            this.saveValuesToConfig();
        }, Component.empty(), (tooltipInfo) -> tooltipInfo.component = Component.literal("➤ ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("gui.ultimine_addition.skills_record.configuration").withStyle(ChatFormatting.WHITE))));
        this.consumeButton = this.addRenderableWidget(new StateSwitchingButton(this.leftPos + 174, this.topPos + 106, 10, 18, this.menu.getData().isConsumeModeActive()) {
            public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                this.resourceLocation = SkillsRecordScreen.CONSUME_BUTTON_SPRITES.get(this.isActive() && this.isStateTriggered, this.isActive() && this.isHoveredOrFocused());
                if (!SkillsRecordScreen.this.isConsumeChallengeExists() && this.isStateTriggered) {
                    this.isStateTriggered = false;
                }

                OverlayColor color = SkillsRecordScreen.this.backgroundColor;
                guiGraphics.setColor(color.red(), color.green(), color.blue(), color.alpha());
                RenderSystem.disableDepthTest();
                guiGraphics.blit(this.resourceLocation, this.getX(), this.getY(), 0.0F, 0.0F, this.width, this.height, 10, 18);
                if (!this.isActive()) {
                    guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, ColorUtils.rgbToRgba(Color.BLACK, 0.15F));
                }

                RenderSystem.enableDepthTest();
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                if (this.isActive()) {
                    double value = SkillsRecordScreen.this.isAnimationsEnabled ? (double) MathUtils.cycledBetweenValues(0.5F, 1.0F, 0.75F, (float) SkillsRecordScreen.this.menu.getPlayer().tickCount / 20.0F, false) : (double) 1.0F;
                    Color cTo = this.isStateTriggered ? new Color(2337827) : new Color(11018786);
                    Color cFrom = this.isStateTriggered ? ColorUtils.blend(new Color(3669815), cTo, value) : ColorUtils.blend(new Color(16725815), cTo, value);
                    Color color1 = this.isStateTriggered ? cFrom : cTo;
                    Color color2 = this.isStateTriggered ? cTo : cFrom;
                    guiGraphics.fillGradient(this.getX() + 1, this.getY() + (this.isStateTriggered ? 1 : 9), this.getX() + 9, this.getY() + (this.isStateTriggered ? 9 : 17), color1.getRGB(), color2.getRGB());
                }

                if (this.isActive() && this.isHovered()) {
                    Component state = SkillsRecordScreen.this.menu.getData().isConsumeModeActive() ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN) : Component.translatable("options.off").withStyle(ChatFormatting.RED);
                    Component info = Component.literal("➤ ").withStyle(ChatFormatting.GRAY).append(Component.translatable("gui.ultimine_addition.skills_record.consume", state).withStyle(ChatFormatting.WHITE));
                    guiGraphics.renderTooltip(SkillsRecordScreen.this.font, info, mouseX, mouseY);
                }

            }

            public void onClick(double mouseX, double mouseY) {
                this.isStateTriggered ^= true;
                SkillsRecordMenu menu = SkillsRecordScreen.this.menu;
                SkillsRecordData data = menu.getData();
                data.setConsumeMode(this.isStateTriggered).save();
                PacketHandler.sendToServer(new SkillsRecordPacket.ToggleConsumeMode(this.isStateTriggered));
            }
        });
    }

    public void update() {
        this.currentProgress = 0;
        if (this.selectedSlot > -1 && this.menu.getCardSlots().get(this.selectedSlot).getItem() == ItemStack.EMPTY) {
            this.selectedSlot = -1;
            SkillsRecordData data = this.menu.getData();
            data.setSelectedCard(this.selectedSlot).save();
            PacketHandler.sendToServer(new SkillsRecordPacket.SelectCard(this.selectedSlot));
        }

        MiningSkillCardData data = MiningSkillCardData.load(this.selectedSlot > -1 ? this.menu.getCardSlots().get(this.selectedSlot).getItem() : ItemStack.EMPTY);
        boolean isCardTierMastered = data.getTier() == MiningSkillCardItem.Tier.Mastered;
        this.isChallengesExists = !data.getChallenges().isEmpty();
        boolean hasCorrectGamemode = !this.menu.getPlayer().isCreative() && !this.menu.getPlayer().isSpectator();
        this.isMissingItems = hasCorrectGamemode && (!this.menu.getAllSlots().get(4).hasItem() || !this.menu.getAllSlots().get(5).hasItem());
        this.notEnoughInk = hasCorrectGamemode && this.selectedSlot > -1 && this.menu.getInkAmount() == 0;
        if (!this.lock && !this.configuration.isVisible()) {
            if (this.isChallengesExists && !this.isMissingItems && !this.notEnoughInk && this.progressMode == 0) {
                this.isProgressionBarShown = true;
            } else if (this.selectedSlot == -1 || isCardTierMastered) {
                this.isProgressionBarShown = false;
            }

            this.menu.getAllSlots().forEach((slot) -> ((CustomSlot) slot).setEnabled(true));
        } else {
            this.isProgressionBarShown = false;
            this.menu.getAllSlots().forEach((slot) -> ((CustomSlot) slot).setEnabled(false));
        }

        this.textScreen.clear();
        this.textScreen.setHeight(82 - (this.isProgressionBarShown ? 9 : 0), true);

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
                this.consumeButton.active = !this.configuration.isVisible() && this.isConsumeChallengeExists();
            }

            if (this.consumeButton.isFocused()) {
                this.consumeButton.setFocused(false);
            }
        }

    }

    public void onClose() {
        this.saveValuesToConfig();
        super.onClose();
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
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

    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.setColor(this.backgroundColor.red(), this.backgroundColor.green(), this.backgroundColor.blue(), this.backgroundColor.alpha());
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Color color = ColorUtils.blend(new Color(0, 0, 0), this.backgroundColor.color, 0.25F);
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, color.getRGB(), false);
        int i = this.inventoryLabelX - 1;
        int i2 = this.inventoryLabelY - 1;
        int i3 = this.inventoryLabelX + this.font.width(this.playerInventoryTitle);
        int i4 = this.inventoryLabelY;
        Objects.requireNonNull(this.font);
        guiGraphics.fill(i, i2, i3, i4 + 9, ColorUtils.rgbToRgba(color, 0.5F));
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, color.getRGB(), false);
    }

    private void renderGhostItem(GuiGraphics guiGraphics, int x, int y) {
        PoseStack poseStack = guiGraphics.pose();
        List<ItemStack> listOfCards = List.of(new ItemStack(Registration.MINING_SKILL_CARD_PICKAXE.get()), new ItemStack(Registration.MINING_SKILL_CARD_AXE.get()), new ItemStack(Registration.MINING_SKILL_CARD_SHOVEL.get()), new ItemStack(Registration.MINING_SKILL_CARD_HOE.get()));
        ItemStack displayItem = ItemStack.EMPTY;

        for (Slot slot : this.menu.getAllSlots()) {
            if (slot instanceof MiningSkillCardSlot) {
                displayItem = listOfCards.get(Mth.floor((float) this.menu.getPlayer().tickCount / 20.0F) % listOfCards.size());
            }

            if (slot instanceof PenSlot) {
                displayItem = new ItemStack(Registration.PEN.get());
            }

            if (slot instanceof PaperSlot) {
                displayItem = new ItemStack(Items.PAPER);
            }

            if (displayItem == ItemStack.EMPTY) {
                return;
            }

            ItemStack stack = slot.getItem();
            if (stack == ItemStack.EMPTY || this.lock || this.configuration.isVisible()) {
                poseStack.pushPose();
                poseStack.translate((float) (x + slot.x), (float) (y + slot.y), 100.0F);
                poseStack.translate(8.0F, 8.0F, (double) 0.0F);
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
                RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                float size = MathUtils.cycledBetweenValues(0.75F, 0.9F, 1.0F, (float) this.menu.getPlayer().tickCount / 20.0F, true);
                size = this.isAnimationsEnabled ? size : 0.9F;
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
            int color = !isMissingItems && !notEnoughInk && (!this.menu.isCardSlotsEmpty() || !(slot instanceof MiningSkillCardSlot)) ? 9145227 : 16711680;
            float alpha = !isMissingItems && !notEnoughInk && (!this.menu.isCardSlotsEmpty() || !(slot instanceof MiningSkillCardSlot)) ? 0.55F : 0.15F;
            if (stack == ItemStack.EMPTY || notEnoughInk || this.lock || this.configuration.isVisible()) {
                poseStack.pushPose();
                poseStack.translate((float) (x + slot.x), (float) (y + slot.y), 100.0F);
                poseStack.translate(8.0F, 8.0F, (double) 0.0F);
                guiGraphics.setColor(this.backgroundColor.red(), this.backgroundColor.green(), this.backgroundColor.blue(), this.backgroundColor.alpha());
                guiGraphics.fill(RenderType.guiOverlay(), -8, -8, 8, 8, ColorUtils.rgbToRgba(color, alpha));
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            }
        }

    }

    private void renderSlotDecorations(GuiGraphics guiGraphics, int x, int y) {
        if (!this.lock) {
            guiGraphics.setColor(this.backgroundColor.red(), this.backgroundColor.green(), this.backgroundColor.blue(), this.backgroundColor.alpha());
            if (this.menu.getSlot(4).getItem() != ItemStack.EMPTY) {
                guiGraphics.blit(SLOT_SELECT_SPRITE, x + 135, y + 99, 0.0F, 0.0F, 4, 8, 4, 8);
            }

            if (this.menu.getSlot(5).getItem() != ItemStack.EMPTY) {
                guiGraphics.blit(SLOT_SELECT_SPRITE, x + 157, y + 99, 0.0F, 0.0F, 4, 8, 4, 8);
            }

            if (this.selectedSlot != -1) {
                int X = 14 + 22 * this.selectedSlot;
                guiGraphics.blit(SLOT_SELECT_SPRITE, x + X, y + 99, 0.0F, 0.0F, 4, 8, 4, 8);
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private void renderTextBox(GuiGraphics guiGraphics) {
        ItemStack stack = this.selectedSlot > -1 ? this.menu.getCardSlots().get(this.selectedSlot).getItem() : ItemStack.EMPTY;
        MiningSkillCardData data = MiningSkillCardData.load(stack);
        if (this.menu.isCardSlotsEmpty()) {
            this.textScreen.selectScreen(0).alignToCenter(true).create(Component.translatable("gui.ultimine_addition.skills_record.no_cards"), ChatFormatting.RED);
        } else if (this.selectedSlot == -1) {
            this.textScreen.selectScreen(0).alignToCenter(true).create(Component.translatable("gui.ultimine_addition.skills_record.select_card"), ChatFormatting.GRAY);
        } else if (this.selectedSlot > -1 && !data.getChallenges().isEmpty()) {
            this.textScreen.selectScreen(0).alignToCenter(false);
            this.createChallengesText(this.menu.getCardSlots());
        } else if (this.selectedSlot > -1 && data.getTier() == MiningSkillCardItem.Tier.Mastered) {
            this.textScreen.selectScreen(0).alignToCenter(true).create(Component.translatable("gui.ultimine_addition.skills_record.completed_card"), ChatFormatting.GOLD, ChatFormatting.ITALIC);
        } else {
            this.textScreen.selectScreen(0).alignToCenter(true).create(Component.translatable("gui.ultimine_addition.skills_record.no_challenges"), ChatFormatting.GRAY);
        }

        if (this.configuration.isVisible()) {
            this.textScreen.selectScreen(1).shouldRender(true).alignToCenter(List.of(true, false).get(Mth.floor((float) this.menu.getPlayer().tickCount / 20.0F) % 2)).create(Component.translatable("gui.ultimine_addition.skills_record.example"), Component.literal("A: 001"), new ChatFormatting[]{ChatFormatting.WHITE}).create(Component.translatable("gui.ultimine_addition.skills_record.example"), Component.literal("B: 002"), new ChatFormatting[]{ChatFormatting.GRAY}).create(Component.translatable("gui.ultimine_addition.skills_record.example"), Component.literal("C: 003"), new ChatFormatting[]{ChatFormatting.DARK_AQUA}).create(Component.translatable("gui.ultimine_addition.skills_record.example"), Component.literal("C: 004"), ChatFormatting.GOLD);
        } else if (this.isChallengesExists && this.isMissingItems) {
            this.textScreen.selectScreen(1).shouldRender(true).alignToCenter(true).backgroundColor(new Color(75, 24, 24, 218)).create(Component.translatable("gui.ultimine_addition.skills_record.missing_items").withStyle(ChatFormatting.RED));
            List<ItemStack> missingItems = new ArrayList<>();
            if (!this.menu.getAllSlots().get(4).hasItem()) {
                missingItems.add(new ItemStack(Registration.PEN.get()));
            }

            if (!this.menu.getAllSlots().get(5).hasItem()) {
                missingItems.add(Items.PAPER.getDefaultInstance());
            }

            for (ItemStack item : missingItems) {
                this.textScreen.add(Component.literal("§c• ").append(Component.empty().append(item.getHoverName()).withStyle((style) -> style.withColor(ChatFormatting.GRAY).withItalic(true).withHoverEvent(new HoverEvent(Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(item))))).getVisualOrderText());
            }
        } else if (this.isChallengesExists && this.notEnoughInk) {
            this.textScreen.selectScreen(1).shouldRender(true).alignToCenter(true).backgroundColor(new Color(75, 24, 24, 218)).create(Component.translatable("gui.ultimine_addition.skills_record.not_enough_ink").withStyle(ChatFormatting.RED));
        } else {
            this.textScreen.selectScreen(1).shouldRender(false);
        }

        this.textScreen.selectLastScreen(true).renderAllBoxes(guiGraphics, Color.DARK_GRAY.getRGB(), this.backgroundColor.color);
    }

    private void renderScroll(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        int Y = (int) Mth.lerp(this.getScrollOffsetDelta(), 16.0F, 86.0F);
        boolean isOverButton = this.isHovering(174, Y, 9, 12, mouseX, mouseY);
        guiGraphics.setColor(this.backgroundColor.red(), this.backgroundColor.green(), this.backgroundColor.blue(), this.backgroundColor.alpha());
        guiGraphics.blit(SCROLLBAR_SPRITE.get(this.textScreen.canScroll(), isOverButton), x + 174, y + Y, 0.0F, 0.0F, 10, 13, 10, 13);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderProgressBar(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        if (this.isProgressionBarShown) {
            PoseStack poseStack = guiGraphics.pose();
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.blit(PROGRESS_BAR_SPRITE, x + 10, y + 91, 0.0F, 0.0F, 156, 7, 156, 7);
            float f = (float) this.currentProgress;
            float value = f / maxProgress;
            int bar = (int) Mth.lerp(value, 0.0F, 154.0F);
            poseStack.pushPose();
            poseStack.translate((float) (x + 11), (float) (y + 97), 0.0F);
            poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
            guiGraphics.fillGradient(RenderType.guiOverlay(), 0, 0, 5, bar, ColorUtils.rgbToRgba(this.getProgressionBarColor(), 0.55F), ColorUtils.rgbToRgba(this.getProgressionBarColor(), 0.75F), 0);
            poseStack.popPose();
            if (!this.lock) {
                if (MouseHelper.isMouseOver(mouseX, mouseY, x, y, 10, 91, 156, 7)) {
                    Component component = Component.translatable("gui.ultimine_addition.skills_record.progress", Component.literal("%" + this.currentProgress).withStyle(Style.EMPTY.withColor(this.getProgressionBarColor()))).withStyle(ChatFormatting.GRAY);
                    guiGraphics.renderTooltip(this.font, component, mouseX - 50, mouseY - 5);
                }

            }
        }
    }

    private void renderTextBoxComponent(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!this.lock) {
            Style style = this.textScreen.getComponentStyleAt(mouseX, mouseY);
            if (style != null && style.getHoverEvent() != null) {
                HoverEvent styleHoverEvent = style.getHoverEvent();
                if (styleHoverEvent.getValue(Action.SHOW_TEXT) != null) {
                    Component component = style.getHoverEvent().getValue(Action.SHOW_TEXT);
                    if (component != null) {
                        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                        guiGraphics.renderTooltip(this.font, this.font.split(component, Math.max(this.width / 2, 200)), mouseX, mouseY);
                    }
                }

                if (styleHoverEvent.getValue(Action.SHOW_ITEM) != null) {
                    HoverEvent.ItemStackInfo stackInfo = styleHoverEvent.getValue(Action.SHOW_ITEM);
                    if (stackInfo == null) {
                        return;
                    }

                    int X = -10;
                    int Y = -30;
                    guiGraphics.pose().pushPose();
                    this.renderItemTooltip(guiGraphics, stackInfo.getItemStack(), style, mouseX + X, mouseY + Y);
                    guiGraphics.pose().popPose();
                }
            }

        }
    }

    private void renderItemTooltip(GuiGraphics guiGraphics, ItemStack stack, Style style, int mouseX, int mouseY) {
        assert this.minecraft != null;

        AtomicInteger length = new AtomicInteger();
        List<Component> tooltip = Screen.getTooltipFromItem(this.minecraft, stack);
        List<ClientTooltipComponent> tooltipComponents = tooltip.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        tooltipComponents.forEach((component) -> length.set(Math.max(length.get(), component.getWidth(this.font))));
        if (style.getClickEvent() != null) {
            Component component = Component.translatable("tooltip.ultimine_addition.skills_record.press.left_click").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            tooltipComponents.addAll(Language.getInstance().getVisualOrder(this.font.getSplitter().splitLines(component, Math.max(length.get(), 150), Style.EMPTY)).stream().map(ClientTooltipComponent::create).toList());
        }

        boolean isExceedingWindow = mouseX + 12 + 18 + length.get() > this.width;
        int alignPosX = isExceedingWindow ? 6 : 0;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        guiGraphics.setColor(this.backgroundColor.red(), this.backgroundColor.green(), this.backgroundColor.blue(), this.backgroundColor.alpha());
        guiGraphics.blit(ITEM_DISPLAY_SPRITE, mouseX + alignPosX, mouseY, 0.0F, 0.0F, 26, 26, 26, 26);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.pose().popPose();
        guiGraphics.renderFakeItem(stack, mouseX + alignPosX + 5, mouseY + 5);
        int spacing = 6 - tooltipComponents.size() * 6;
        guiGraphics.renderTooltipInternal(this.font, tooltipComponents, mouseX + 18, mouseY + 21 + spacing, DefaultTooltipPositioner.INSTANCE);
    }

    private boolean isConsumeChallengeExists() {
        AtomicBoolean result = new AtomicBoolean();
        this.menu.getCardSlots().forEach((slot) -> {
            ItemStack stack = slot.getItem();
            if (MiningSkillCardData.hasData(stack)) {
                MiningSkillCardData cardData = MiningSkillCardData.load(stack);
                if (!cardData.getChallenges().stream().filter((challengeHolder) -> ChallengesManager.INSTANCE.getAllChallenges().get(challengeHolder.getId()) != null && ChallengesManager.INSTANCE.getAllChallenges().get(challengeHolder.getId()).challengeType().isConsuming()).toList().isEmpty()) {
                    result.set(true);
                }

            }
        });
        return result.get();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.isScrollHovered(mouseX, mouseY)) {
                return this.isScrolling = this.textScreen.canScroll();
            }

            Style style = this.textScreen.getComponentStyleAt(mouseX, mouseY);
            if (style != null && style.getClickEvent() != null) {
                if (style.getClickEvent().getValue().contains("[cycle]")) {
                    ++itemCycle;
                    return true;
                }

                if (this.selectedSlot > -1 && style.getClickEvent().getValue().contains("[pin]")) {
                    String value = style.getClickEvent().getValue().split("<>")[1];
                    ResourceLocation challengeId = new ResourceLocation(value);
                    SkillsRecordData data = this.menu.getData();
                    data.setStack(ItemUtils.getSkillsRecord(this.menu.getPlayer(), this.menu.interactionHand));
                    data.togglePinned(this.selectedSlot, challengeId).save();
                    PacketHandler.sendToServer(new SkillsRecordPacket.PinChallenge(this.selectedSlot, challengeId));
                    return true;
                }

                if (this.selectedSlot > -1 && style.getClickEvent().getValue().contains("[edit]")) {
                    JsonElement element = JsonParser.parseString(style.getClickEvent().getValue().split("<>")[1]);
                    DataResult<Pair<MiningSkillCardData.Challenge, JsonElement>> decode = MiningSkillCardData.Challenge.CODEC.decode(JsonOps.INSTANCE, element);
                    if (this.minecraft == null) {
                        return false;
                    }

                    this.minecraft.setScreen(new EditChallengeScreen(this, (decode.getOrThrow(false, (s) -> {
                    })).getFirst()));
                    return true;
                }
            }
        }

        if (button == 1 && this.hoveredSlot != null && this.hoveredSlot.getItem() != ItemStack.EMPTY && this.menu.getCardSlots().contains(this.hoveredSlot)) {
            if (this.selectedSlot != -1 && this.selectedSlot == this.hoveredSlot.getContainerSlot()) {
                this.selectedSlot = -1;
            } else {
                this.selectedSlot = this.hoveredSlot.getContainerSlot();
            }

            SkillsRecordData data = this.menu.getData();
            data.setSelectedCard(this.selectedSlot).save();
            PacketHandler.sendToServer(new SkillsRecordPacket.SelectCard(this.selectedSlot));
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.getFocused() != null && this.getFocused().mouseReleased(mouseX, mouseY, button)) {
            this.clearFocus();
            this.setFocused(null);
            return true;
        } else {
            if (button == 0) {
                this.isScrolling = false;
            }

            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.getFocused() != null && this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        } else if (this.isScrolling) {
            int i = this.topPos + 16;
            int j = i + 86;
            float delta = Mth.clamp((float) ((mouseY - (double) i) / (double) (j - i)), 0.0F, 1.0F);
            this.textScreen.scrollTo((int) Mth.lerp(delta, 0.0F, (float) this.textScreen.getRemainingLines()), true);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (super.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        } else if (this.textScreen.canScroll()) {
            if (delta < (double) 0.0F) {
                this.textScreen.scrollTo(1, false);
            }

            if (delta > (double) 0.0F) {
                this.textScreen.scrollTo(-1, false);
            }

            return true;
        } else {
            return false;
        }
    }

    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        if (this.lock) {
            return false;
        } else {
            GuiEventListener guiEventListener = this.getFocused();
            if (guiEventListener instanceof AbstractDraggableWidget component) {
                if (component.isMouseOver(mouseX, mouseY)) {
                    return false;
                }
            }

            return super.isHovering(x, y, width, height, mouseX, mouseY);
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.hoveredSlot != null) {
            return false;
        } else {
            if (KeyBindingHooks.isMatches(KeyHandler.KEY_SHOW_PROGRESSION_BAR, keyCode, scanCode) && this.isChallengesExists && !this.isMissingItems && !this.notEnoughInk && !this.lock && this.progressMode == 1) {
                this.isProgressionBarShown = true;
            }

            return false;
        }
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (super.keyReleased(keyCode, scanCode, modifiers)) {
            return true;
        } else if (!KeyBindingHooks.isMatches(KeyHandler.KEY_SHOW_PROGRESSION_BAR, keyCode, scanCode)) {
            if (this.progressMode == 1) {
                this.isProgressionBarShown = false;
            }

            return true;
        } else {
            return false;
        }
    }

    private float getScrollOffsetDelta() {
        float offset = (float) this.textScreen.getScrollOffset() / Math.max(1.0F, (float) this.textScreen.getRemainingLines());
        return Mth.clamp(offset, 0.0F, 1.0F);
    }

    private boolean isScrollHovered(double mouseX, double mouseY) {
        int y = (int) Mth.lerp(this.getScrollOffsetDelta(), 16.0F, 86.0F);
        return MouseHelper.isMouseOver(mouseX, mouseY, this.leftPos, this.topPos, 174, y, 10, 13);
    }

    private void createChallengesText(NonNullList<Slot> slots) {
        ItemStack stack = slots.get(this.selectedSlot).getItem();
        MiningSkillCardData cardData = MiningSkillCardData.load(stack);
        ChallengesManager manager = ChallengesManager.INSTANCE;
        int currentValues = 0;
        int requiredValues = 0;

        for (MiningSkillCardData.Challenge challenge : cardData.getChallenges()) {
            List<ItemStack> itemList = new ArrayList<>(List.of(ItemStack.EMPTY));
            ChallengeData challengeData = manager.getAllChallenges().get(challenge.getId());
            if (challengeData != null) {
                itemList = ChallengesManager.INSTANCE.utilizeTargetedBlocks(challengeData).stream().map(Block::asItem).map(Item::getDefaultInstance).toList();
                currentValues += challenge.getCurrentPoints();
                requiredValues += challenge.getRequiredPoints();
            }

            ItemStack displayItem = itemList.get(Mth.floor(itemCycle) % itemList.size());
            HoverEvent hoverEvent = new HoverEvent(Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(displayItem));
            Style questStyle = Style.EMPTY.withHoverEvent(hoverEvent).withItalic(true).withColor((new Color(12361597)).getRGB());
            if (itemList.size() > 1) {
                questStyle = questStyle.withClickEvent(new ClickEvent(net.minecraft.network.chat.ClickEvent.Action.OPEN_FILE, "[cycle]"));
            }

            Component title = ComponentHelper.limitComponent(Component.literal("》").append(Component.translatable("challenge.ultimine_addition.title", challenge.getOrder()).setStyle(Style.EMPTY.withColor((new Color(16445889)).getRGB()))).append("《").withStyle(Style.EMPTY.withColor((new Color(8026746)).getRGB())), this.textScreen.getWidth());
            List<Component> descriptions = manager.createChallengeDescription(challenge.getId(), Style.EMPTY.withColor(ChatFormatting.GRAY), itemCycle, questStyle);
            Style descStyle = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY);
            Style counterStyle = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GOLD);
            boolean isConsumeNeeded = challengeData != null && challengeData.challengeType().isConsuming() && !this.menu.getData().isConsumeModeActive() && !cardData.isChallengeAccomplished(challenge.getId());
            if (isConsumeNeeded) {
                HoverEvent hover = new HoverEvent(Action.SHOW_TEXT, Component.translatable("challenge.ultimine_addition.consume.info").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                counterStyle = Style.EMPTY.withHoverEvent(hover).withColor(ChatFormatting.RED);
            } else if (challengeData != null && challengeData.challengeType().isConsuming() && this.menu.getData().isConsumeModeActive() && !cardData.isChallengeAccomplished(challenge.getId())) {
                HoverEvent hover = new HoverEvent(Action.SHOW_TEXT, Component.literal("✖ ").append(Component.translatable("challenge.ultimine_addition.consume")).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                counterStyle = Style.EMPTY.withItalic(true).withHoverEvent(hover).withColor(ChatFormatting.LIGHT_PURPLE);
            } else if (cardData.isChallengeAccomplished(challenge.getId())) {
                counterStyle = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GREEN);
            }

            List<FormattedCharSequence> result = new ArrayList<>(Language.getInstance().getVisualOrder(this.font.getSplitter().splitLines(title, this.textScreen.getWidth(), title.getStyle())));

            for (Component description : descriptions) {
                result.addAll(Language.getInstance().getVisualOrder(this.font.getSplitter().splitLines(description, this.textScreen.getWidth(), descStyle)));
            }

            FormattedCharSequence counter = FormattedCharSequence.composite(FormattedCharSequence.forward("➤ ", counterStyle), FormattedCharSequence.forward("%s/%s".formatted(challenge.getCurrentPoints(), challenge.getRequiredPoints()), counterStyle.withStrikethrough(isConsumeNeeded)));
            HoverEvent pinHover = new HoverEvent(Action.SHOW_TEXT, Component.translatable("gui.ultimine_addition.skills_record.pin.click").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            FormattedCharSequence pinButton = FormattedCharSequence.forward("◎", Style.EMPTY.withColor(challenge.isPinned() ? ChatFormatting.YELLOW : ChatFormatting.GRAY).withStrikethrough(!challenge.isPinned()).withHoverEvent(pinHover).withClickEvent(new ClickEvent(net.minecraft.network.chat.ClickEvent.Action.OPEN_FILE, "[pin]<>%s".formatted(challenge.getId().toString()))));
            DataResult<JsonElement> dataResult = MiningSkillCardData.Challenge.CODEC.encodeStart(JsonOps.INSTANCE, challenge);
            HoverEvent editHover = new HoverEvent(Action.SHOW_TEXT, Component.translatable("gui.ultimine_addition.skills_record.edit.click").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            FormattedCharSequence editButton = FormattedCharSequence.forward("✎", Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withHoverEvent(editHover).withClickEvent(new ClickEvent(net.minecraft.network.chat.ClickEvent.Action.OPEN_FILE, "[edit]<>%s".formatted(dataResult.getOrThrow(false, (s) -> {
            })))));
            FormattedCharSequence buttons = FormattedCharSequence.composite(FormattedCharSequence.forward(" [", Style.EMPTY.withColor(Color.GRAY.getRGB())), pinButton);
            if (ConfigHandler.CLIENT.SR_EDIT_MODE.get() && this.menu.getPlayer().hasPermissions(2)) {
                buttons = FormattedCharSequence.composite(buttons, FormattedCharSequence.forward("]-[", Style.EMPTY.withColor(Color.GRAY.getRGB())), editButton);
            }

            buttons = FormattedCharSequence.composite(buttons, FormattedCharSequence.forward("]", Style.EMPTY.withColor(Color.GRAY.getRGB())));
            result.add(FormattedCharSequence.composite(counter, buttons));
            if (!this.textScreen.isEmpty()) {
                this.textScreen.add(FormattedCharSequence.EMPTY).addAll(result);
            } else {
                this.textScreen.addAll(result);
            }
        }

        this.calculateProgression(currentValues, requiredValues);
    }

    private void calculateProgression(int currentValues, int requiredValues) {
        if (requiredValues != 0) {
            float f = (float) currentValues / (float) requiredValues;
            float value = f * maxProgress;
            this.currentProgress = (int) value;
        }
    }

    public int getProgressionBarColor() {
        float f = Math.min((float) this.currentProgress / maxProgress, 1.0F);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public Collection<Rect2i> getComponentsRectangle() {
        Collection<Rect2i> collection = new HashSet<>();
        collection.add(RenderUtils.createRect2i(this.configuration));
        return collection;
    }

    public enum OverlayColor {
        DEFAULT(new Color(255, 255, 255)),
        RED(new Color(255, 116, 116)),
        ORANGE(new Color(255, 162, 94)),
        YELLOW(new Color(253, 241, 113)),
        GREEN(new Color(144, 238, 144)),
        BLUE(new Color(112, 153, 255)),
        INDIGO(new Color(126, 80, 176)),
        VIOLET(new Color(238, 130, 238));

        private final Color color;

        OverlayColor(Color color) {
            this.color = color;
        }

        public float alpha() {
            return (new ColorUtils(this.color.getRGB())).alpha();
        }

        public float red() {
            return (new ColorUtils(this.color.getRGB())).red();
        }

        public float green() {
            return (new ColorUtils(this.color.getRGB())).green();
        }

        public float blue() {
            return (new ColorUtils(this.color.getRGB())).blue();
        }

        public OverlayColor next() {
            int nextIndex = (this.ordinal() + 1) % values().length;
            return values()[nextIndex];
        }

        public OverlayColor previous() {
            int prevIndex = (this.ordinal() - 1 + values().length) % values().length;
            return values()[prevIndex];
        }

        public Color convert() {
            return this.color;
        }

    }
}
