package net.ixdarklord.ultimine_addition.client.gui.components;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.client.gui.components.widgets.AbstractDraggableWidget;
import net.ixdarklord.coolcatlib.api.client.utils.RenderUtils;
import net.ixdarklord.coolcatlib.api.utils.ColorUtils;
import net.ixdarklord.ultimine_addition.client.gui.layouts.LinearLayout;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ConfigurationPanel extends AbstractDraggableWidget {
    private static final ResourceLocation BACKGROUND_LOCATION = FTBUltimineAddition.getGuiSprite("container/skills_record/configuration/background");
    private static final int BUTTON_WIDTH = 45;
    private static final int BUTTON_HEIGHT = 14;
    private final List<ColoredButton> buttons = Lists.newArrayList();

    public ConfigurationPanel(int x, int y) {
        super(Component.translatable("gui.ultimine_addition.skills_record.configuration").withStyle(ChatFormatting.GRAY), x, y, 63, 102, true);
        this.visible = false;
        this.blitOffset = 600.0F;
    }

    public void init() {
        this.layout.defaultChildLayoutSetting().paddingVertical(2);
        LinearLayout linearLayout = this.layout.addChild(LinearLayout.vertical().spacing(2));

        for (ColoredButton button : this.buttons) {
            button.setWidth(45 + this.getWidthSpacing());
        }

        for (ColoredButton button : this.buttons) {
            linearLayout.addChild(button);
            this.addRenderableWidget(button);
        }

    }

    protected void updateChildren() {
        super.updateChildren();
        this.title.setPosition(this.x + 9, this.y + 17);
        this.title.setWidth(47 + this.getWidthSpacing());
        this.title.alignCenter();

        for (AbstractButton button : this.getButtons()) {
            if (button instanceof ColoredButton imageButton) {
                imageButton.setColor(this.getBGColor().convert());
            }
        }

    }

    protected void renderBackground(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.setColor(this.getBGColor().red(), this.getBGColor().green(), this.getBGColor().blue(), this.getBGColor().alpha());
        RenderUtils.blitNineSliced(guiGraphics, BACKGROUND_LOCATION, this.getRectangle().left(), this.getRectangle().top(), this.getRectangle().width(), this.getRectangle().height(), 16, 32, 118, 108);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
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
        return Math.max(length - 45 + 10, 0);
    }

    private int getHeightSpacing() {
        return Math.max(this.layout.getHeight() - 72, 0);
    }

    public void addButton(Button.OnPress onPress, Component component, Consumer<TooltipInfo> consumer) {
        ColoredButton button = new ColoredButton(this.x, this.y + 14 * this.getButtons().size(), 45, 14, 5, SkillsRecordScreen.BUTTON_SPRITES, 45, 12, onPress, component, consumer);
        this.buttons.add(button);
    }

    public SkillsRecordScreen.OverlayColor getBGColor() {
        return ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
    }

    public ColorUtils getDraggingAreaColor() {
        return new ColorUtils(this.getBGColor().convert().getRGB());
    }

    public @NotNull ScreenRectangle getDraggingRectangle() {
        return new ScreenRectangle(this.getRectangle().position().x() + 8, this.getRectangle().position().y() + 5, this.getRectangle().width() - 16, 7);
    }

    protected @NotNull ScreenRectangle layoutRectangle() {
        return new ScreenRectangle(this.getRectangle().position().x() + 9, this.getRectangle().position().y() + 27, this.getRectangle().width() - 18, this.getRectangle().height() - 36);
    }

    public @NotNull ScreenRectangle getRectangle() {
        return this.applyIfVisible(new ScreenRectangle(this.x, this.y, this.width + this.getWidthSpacing(), this.height + this.getHeightSpacing()));
    }
}
