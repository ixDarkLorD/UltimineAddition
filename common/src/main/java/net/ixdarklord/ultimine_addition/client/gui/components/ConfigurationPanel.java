package net.ixdarklord.ultimine_addition.client.gui.components;

import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.RenderPipelines;
import com.google.common.collect.Lists;
import net.ixdarklord.coolcatlib.api.client.gui.components.ColorableImageButton;
import net.ixdarklord.coolcatlib.api.client.gui.components.widgets.AbstractDraggableWidget;
import net.ixdarklord.coolcatlib.api.utils.ColorUtils;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class ConfigurationPanel extends AbstractDraggableWidget {
    private static final Identifier BACKGROUND_LOCATION = FTBUltimineAddition.id("container/skills_record/configuration/background");
    private static final int BUTTON_WIDTH = 45;
    private static final int BUTTON_HEIGHT = 14;
    private final List<ColoredButton> buttons = Lists.newArrayList();

    public ConfigurationPanel(int x, int y) {
        super(Component.translatable("gui.ultimine_addition.skills_record.configuration").withStyle(ChatFormatting.GRAY),
                x, y,
                BUTTON_WIDTH + 18, 102,
                true
        );
        this.visible = false;
    }

    @Override
    public void init() {
        this.layout.defaultChildLayoutSetting().paddingVertical(2);
        LinearLayout linearLayout = this.layout.addChild(LinearLayout.vertical().spacing(2));

        for (ColoredButton button : this.buttons) {
            button.setWidth(BUTTON_WIDTH + this.getWidthSpacing());
        }

        for (ColoredButton button : this.buttons) {
            linearLayout.addChild(button);
            this.addRenderableWidget(button);
        }
    }

    @Override
    protected void updateChildren() {
        super.updateChildren();
        // StringWidget no longer aligns text itself; center it within the title area.
        int titleAreaWidth = BUTTON_WIDTH + 2 + this.getWidthSpacing();
        this.title.setPosition(this.x + 9 + (titleAreaWidth - this.font.width(this.title.getMessage())) / 2, this.y + 16);

        for (AbstractButton button : this.getButtons()) {
            if (button instanceof ColorableImageButton imageButton)
                imageButton.setColor(this.getBGColor().convert());
        }
    }

    @Override
    protected void renderBackground(GuiGraphicsExtractor guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Draw above the slot items (replaces the old z-offset).
        guiGraphics.nextStratum();
        SkillsRecordScreen.OverlayColor bg = this.getBGColor();
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_LOCATION, getRectangle().left(), getRectangle().top(), getRectangle().width(), getRectangle().height(), ARGB.colorFromFloat(bg.alpha(), bg.red(), bg.green(), bg.blue()));
    }

    private int getButtonsTextLength() {
        int length = 0;
        for (AbstractButton button : this.buttons) {
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

    public void addButton(Button.OnPress onPress, Component component, Consumer<TooltipInfo> consumer) {
        ColoredButton button = new ColoredButton(this.x, this.y + BUTTON_HEIGHT * getButtons().size(), BUTTON_WIDTH, BUTTON_HEIGHT, SkillsRecordScreen.BUTTON_SPRITES, onPress, component, consumer) {
            @Override
            public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
                this.extractDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
            }
        };

        this.buttons.add(button);
    }

    public SkillsRecordScreen.OverlayColor getBGColor() {
        return ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
    }

    @Override
    public ColorUtils getDraggingAreaColor() {
        return new ColorUtils(getBGColor().convert().getRGB());
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
