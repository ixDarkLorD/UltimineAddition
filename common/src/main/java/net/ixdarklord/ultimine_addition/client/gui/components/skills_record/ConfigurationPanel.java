package net.ixdarklord.ultimine_addition.client.gui.components.skills_record;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.client.gui.components.ColorableImageButton;
import net.ixdarklord.coolcatlib.api.client.gui.components.widgets.AbstractDraggableWidget;
import net.ixdarklord.coolcatlib.api.util.ColorUtils;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ConfigurationPanel extends AbstractDraggableWidget {
    private static final ResourceLocation BACKGROUND_LOCATION = FTBUltimineAddition.rl("container/skills_record/configuration/background");
    private static final int BUTTON_WIDTH = 45;
    private static final int BUTTON_HEIGHT = 14;
    private SkillsRecordScreen.OverlayColor backgroundColor;
    private boolean isAnimationsEnabled;
    private int progressMode;

    private ColorableImageButton backgroundColorButton;
    private ColorableImageButton animationsButton;
    private ColorableImageButton progressionBarButton;
    private ColorableImageButton challengesPanelPosButton;
    private ColorableImageButton challengesPanelSizeButton;

    public ConfigurationPanel(int x, int y) {
        super(Component.translatable("gui.ultimine_addition.skills_record.configuration").withStyle(ChatFormatting.GRAY),
                x, y,
                BUTTON_WIDTH + 18, 102,
                true
        );
        this.visible = false;
        this.blitOffset = ItemRenderer.ITEM_COUNT_BLIT_OFFSET + 400F;
        this.backgroundColor = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
        this.isAnimationsEnabled = ConfigHandler.CLIENT.ANIMATIONS_MODE.get();
        this.progressMode = ConfigHandler.CLIENT.PROGRESS_BAR.get();
    }

    @Override
    public void init() {
        this.layout.defaultChildLayoutSetting().paddingVertical(2);
        LinearLayout linearLayout = this.layout.addChild(LinearLayout.vertical().spacing(2));

        this.backgroundColorButton = this.addButton(linearLayout, (button) -> {
            if (!Screen.hasShiftDown()) this.backgroundColor = this.backgroundColor.next();
            else this.backgroundColor = this.backgroundColor.previous();
            this.saveValuesToConfig();
        }, Component.translatable("gui.ultimine_addition.skills_record.option.bg_color"));

        this.animationsButton = this.addButton(linearLayout, (button) -> {
            this.isAnimationsEnabled ^= true;
            this.saveValuesToConfig();
        }, Component.translatable("gui.ultimine_addition.skills_record.option.animations"));

        this.progressionBarButton = this.addButton(linearLayout, (button) -> {
            this.progressMode = (this.progressMode >= 2) ? 0 : this.progressMode + 1;
            this.saveValuesToConfig();
        }, Component.translatable("gui.ultimine_addition.skills_record.option.progression_bar"));

        this.challengesPanelPosButton = this.addButton(linearLayout, (button) -> {
            if (!Screen.hasShiftDown())
                ChallengesInfoPanel.INSTANCE.setPanelPos(ChallengesInfoPanel.INSTANCE.getPanelPos().next());
            else
                ChallengesInfoPanel.INSTANCE.setPanelPos(ChallengesInfoPanel.INSTANCE.getPanelPos().previous());
            this.saveValuesToConfig();
        }, Component.translatable("gui.ultimine_addition.skills_record.option.panel_pos"));

//        this.challengesPanelSizeButton = this.addButton(linearLayout, (button) -> {}, Component.translatable("gui.ultimine_addition.skills_record.option.panel_size"));

        for (AbstractButton button : this.getButtons()) {
            button.setWidth(BUTTON_WIDTH + this.getWidthSpacing());
        }
    }

    @Override
    protected void updateChildren() {
        super.updateChildren();
        this.title.setPosition(this.x + 9, this.y + 16);
        this.title.setSize(BUTTON_WIDTH + 2 + this.getWidthSpacing(), 11);
        this.title.alignCenter();

        for (AbstractButton button : this.getButtons()) {
            if (button instanceof ColorableImageButton imageButton)
                imageButton.setColor(this.backgroundColor.convert());
        }
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.setColor(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), this.backgroundColor.getAlpha());
        guiGraphics.blitSprite(BACKGROUND_LOCATION, getRectangle().left(), getRectangle().top(), getRectangle().width(), getRectangle().height());
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderTooltip(Button button, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!button.isHovered()) return;
        final MutableComponent component = Component.literal("➤ ").withStyle(ChatFormatting.DARK_GRAY);
        Optional<TooltipComponent> tooltipComponent = Optional.empty();

        if (button == this.backgroundColorButton) {
            int color = ColorUtils.RGBToRGBA(this.backgroundColor.convert().getRGB(), this.backgroundColor.getAlpha());
            component.append(Component.translatable(String.format("gui.ultimine_addition.color.%s", this.backgroundColor.name().toLowerCase())).withStyle(Style.EMPTY.withColor(color)));
            tooltipComponent = Optional.of(new SkillsRecordTooltip.Option(0, component));
        }
        if (button == this.animationsButton) {
            component.append(this.isAnimationsEnabled ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN) : Component.translatable("options.off").withStyle(ChatFormatting.RED));
        }
        if (button == this.progressionBarButton) {
            switch (this.progressMode) {
                case 0 -> component.append(Component.translatable("options.on").withStyle(ChatFormatting.GREEN));
                case 1 ->
                        component.append(Component.translatable("gui.ultimine_addition.skills_record.option.hold_keybind", KeyHandler.KEY_SHOW_PROGRESSION_BAR.getTranslatedKeyMessage()).withStyle(ChatFormatting.AQUA));
                case 2 -> component.append(Component.translatable("options.off").withStyle(ChatFormatting.RED));
            }
        }
        if (button == this.challengesPanelPosButton) {
            ChatFormatting color = ChallengesInfoPanel.INSTANCE.getPanelPos().getSerializedName().equals("disabled") ? ChatFormatting.RED : ChatFormatting.WHITE;
            component.append(Component.translatable("gui.ultimine_addition.skills_record.option.panel_pos.%s".formatted(ChallengesInfoPanel.INSTANCE.getPanelPos().getSerializedName())).withStyle(color));
            tooltipComponent = Optional.of(new SkillsRecordTooltip.Option(1, component));
        }
        if (button == this.challengesPanelSizeButton) {
            component.append(Component.translatable("gui.ultimine_addition.skills_record.option.soon").withStyle(ChatFormatting.WHITE));
        }

        guiGraphics.pose().pushPose();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int y = mouseY + 9 - tooltipComponent.map(c -> ClientTooltipComponent.create(c).getHeight() / 2).orElse(0);
        guiGraphics.renderTooltip(this.font, List.of(component.withStyle(ChatFormatting.ITALIC)), tooltipComponent, mouseX, y);
        guiGraphics.pose().popPose();
    }

    private int getButtonsTextLength() {
        int length = 0;
        for (AbstractButton button : this.getButtons()) {
            length = Math.max(this.font.width(button.getMessage()), length);
        }
        return Math.max(this.font.width(Component.translatable("gui.ultimine_addition.skills_record.configuration")), length);
    }

    private int getWidthSpacing() {
        int length = this.getButtonsTextLength();
        return Math.max(length - BUTTON_WIDTH + 10, 0);
    }

    private int getHeightSpacing() {
        return Math.max(this.layout.getHeight() - 72, 0);
    }

    @Override
    public ColorUtils getDraggingAreaColor() {
        return new ColorUtils(this.backgroundColor.convert().getRGB());
    }

    private ColorableImageButton addButton(LinearLayout layout, Button.OnPress onPress, Component message) {
        ColorableImageButton button = new ColorableImageButton(this.x, this.y + BUTTON_HEIGHT * getButtons().size(), BUTTON_WIDTH, BUTTON_HEIGHT, SkillsRecordScreen.BUTTON_SPRITES, onPress, message) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                TextColor color = this.getMessage().getStyle().getColor();
                color = color == null ? TextColor.fromRgb(Color.WHITE.getRGB()) : color;
                this.renderString(guiGraphics, font, color.getValue());
                ConfigurationPanel.this.renderTooltip(this, guiGraphics, mouseX, mouseY);
            }
        };

        layout.addChild(button);
        return this.addRenderableWidget(button);
    }

    public void saveValuesToConfig() {
        ConfigHandler.CLIENT.BACKGROUND_COLOR.set(this.backgroundColor);
        ConfigHandler.CLIENT.ANIMATIONS_MODE.set(this.isAnimationsEnabled);
        ConfigHandler.CLIENT.PROGRESS_BAR.set(this.progressMode);
        ConfigHandler.CLIENT.CHALLENGES_PANEL_POSITION.set(ChallengesInfoPanel.INSTANCE.getPanelPos());
    }

    public SkillsRecordScreen.OverlayColor getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isAnimationsEnabled() {
        return isAnimationsEnabled;
    }

    public int getProgressMode() {
        return progressMode;
    }

    @Override
    public @NotNull ScreenRectangle getDraggingRectangle() {
        return new ScreenRectangle(this.getRectangle().position().x() + 8, this.getRectangle().position().y() + 5, this.getRectangle().width() - 16, 7);
    }

    @Override
    protected @NotNull ScreenRectangle layoutRectangle() {
        return new ScreenRectangle(this.getRectangle().position().x() + 9, this.getRectangle().position().y() + 27, this.getRectangle().width() - 18, this.getRectangle().height() - 36);
    }

    @Override
    public @NotNull ScreenRectangle getRectangle() {
        return applyIfVisible(new ScreenRectangle(this.x, this.y, this.width + this.getWidthSpacing(), this.height + this.getHeightSpacing()));
    }
}
