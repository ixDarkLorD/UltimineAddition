package net.ixdarklord.ultimine_addition.client.gui.components.skills_record;

import net.ixdarklord.coolcatlib.api.client.gui.components.ColorableImageButton;
import net.ixdarklord.coolcatlib.api.util.RenderUtils;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class EditChallengeScreen extends Screen {
    private static final ResourceLocation BACKGROUND_SPRITE = UltimineAddition.getLocation("container/skills_record/edit_challenge/background");
    private static final ResourceLocation ARROW_SPRITE = UltimineAddition.getLocation("container/skills_record/edit_challenge/arrow");
    protected int imageWidth = 155;
    protected int imageHeight = 91;
    protected int leftPos;
    protected int topPos;
    private final SkillsRecordScreen parent;
    private final MiningSkillCardData.ChallengeHolder challengeHolder;
    private EditBox newValueBox;
    private ColorableImageButton doneButton;

    public EditChallengeScreen(SkillsRecordScreen parent, MiningSkillCardData.ChallengeHolder challengeHolder) {
        super(Component.translatable("selectWorld.edit")
                .append(" ")
                .append(Component.translatable("challenge.ultimine_addition.title", Component.literal("[%s]".formatted(challengeHolder.getOrder())))));
        this.parent = parent.lock(true);
        this.challengeHolder = challengeHolder;
    }

    @Override
    protected void init() {
        this.parent.width = this.width;
        this.parent.height = this.height;
        this.parent.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(new StringWidget(this.leftPos + 16, this.topPos + 34, 123, 9, Component.literal(this.challengeHolder.getId().toString()), this.font) {
            private int ticks = 0;
            private boolean showSuccess = false;

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                Component component = Component.translatable("gui.ultimine_addition.skills_record.edit.copy_id").withStyle(ChatFormatting.GRAY);
                Component successComponent = Component.translatable("gui.ultimine_addition.skills_record.edit.copy_success").withStyle(ChatFormatting.GREEN);
                this.setTooltip(Tooltip.create(showSuccess ? successComponent : component));
                RenderUtils.renderScrollingString(guiGraphics, (int) (Util.getMillis() / 4), EditChallengeScreen.this.font, this.getMessage(), true, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 1, Color.WHITE.getRGB());

                if (showSuccess) {
                    ticks--;
                    if (ticks <= 0) {
                        showSuccess = false;
                    }
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (this.isHovered() && button == GLFW.GLFW_MOUSE_BUTTON_1) {
                    assert EditChallengeScreen.this.minecraft != null;
                    EditChallengeScreen.this.minecraft.keyboardHandler.setClipboard(this.getMessage().getString());
                    this.playDownSound(EditChallengeScreen.this.minecraft.getSoundManager());

                    showSuccess = true;
                    ticks = 80;
                    return true;
                }
                return false;
            }

        });

        this.newValueBox = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 91, this.topPos + 50, 34, 10, Component.translatable("gui.skills_record.edit.new_value")));
        this.newValueBox.setBordered(false);
        this.newValueBox.setTooltip(Tooltip.create(Component.translatable("gui.ultimine_addition.skills_record.edit.new_value", this.challengeHolder.getRequiredPoints())));
        this.newValueBox.insertText(String.valueOf(this.challengeHolder.getCurrentPoints()));
        this.newValueBox.setMaxLength(String.valueOf(this.challengeHolder.getRequiredPoints()).length());
        this.newValueBox.setFilter(s -> {
            try {
                if (s.isEmpty()) return true;
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });

        FrameLayout layout = new FrameLayout(this.leftPos + 10, this.topPos + 64, 135, 17);
        LinearLayout linearLayout = layout.addChild(LinearLayout.horizontal().spacing(5));

        linearLayout.addChild(this.addRenderableWidget(new ColorableImageButton(0, 0, 45, 13, SkillsRecordScreen.BUTTON_SPRITES, button ->
                EditChallengeScreen.this.onClose(), CommonComponents.GUI_CANCEL) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                int i = this.getX() + 2;
                int j = this.getX() + this.getWidth() - 2;
                renderScrollingString(guiGraphics, font, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight() - 1, Color.WHITE.getRGB());
            }
        }));

        this.doneButton = linearLayout.addChild(this.addRenderableWidget(new ColorableImageButton(0, 0, 45, 13, SkillsRecordScreen.BUTTON_SPRITES, button -> {
            SkillsRecordData data = this.parent.getMenu().getData();
            MiningSkillCardData cardData = MiningSkillCardData.loadData(data.getCardSlots().get(this.parent.selectedSlot));
            cardData.setAmount(this.challengeHolder.getId(), this.getNewValue());
            data.sendToServer(this.parent.getMenu().interactionHand).saveData(data.get());

            assert this.minecraft != null;
            assert this.minecraft.player != null;
            this.minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 1F, 1.5F);
            EditChallengeScreen.this.onClose();
        }, CommonComponents.GUI_DONE) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                int i = this.getX() + 2;
                int j = this.getX() + this.getWidth() - 2;
                renderScrollingString(guiGraphics, font, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight() - 1, Color.WHITE.getRGB());
            }
        }));

        layout.arrangeElements();
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        this.parent.clearWidgets();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.parent.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.doneButton.active = this.isValidInput();
        try {
            int value = Integer.parseInt(this.newValueBox.getValue());
            this.newValueBox.setValue(String.valueOf(Math.min(value, this.challengeHolder.getRequiredPoints())));
            this.newValueBox.setTextColor(value > this.challengeHolder.getCurrentPoints() ? new Color(0x3BFF4B).getRGB() : value < this.challengeHolder.getCurrentPoints() ? new Color(0xFF5E5E).getRGB() : Color.WHITE.getRGB());
        } catch (NumberFormatException ignored) {
        }

        int ticks = (int) Util.getMillis() / 4;
        RenderUtils.renderScrollingString(guiGraphics, ticks, this.font,
                this.title, true,
                this.leftPos + 10, this.topPos + 10,
                135, 18, 4, Color.WHITE.getRGB());

        RenderUtils.renderScrollingString(guiGraphics, ticks, this.font, Component.literal("" + this.challengeHolder.getCurrentPoints()), false, this.leftPos + 29, this.topPos + 49, 36, 9, 1, Color.GRAY.getRGB());
        this.renderArrow(guiGraphics);
    }

    private void renderArrow(GuiGraphics guiGraphics) {
        int value = this.getNewValue();
        boolean still = value == this.challengeHolder.getCurrentPoints();
        int minX = this.leftPos + (still ? 68 : 66);
        int minY = this.topPos + 48;
        int maxX = this.leftPos + 88;
        int maxY = this.topPos + 60;
        int arrowWidth = 19;
        int arrowHeight = 12;
        int spacing = 4;
        int speed = 50;

        int conveyorWidth = maxX - minX;
        int arrowTotalWidth = arrowWidth + spacing;
        int numArrows = (conveyorWidth / arrowTotalWidth) + 2;
        long currentTime = Util.getMillis();
        double offset = still ? 0 : (currentTime / 1000.0) * speed;

        guiGraphics.enableScissor(minX, minY, maxX, maxY);
        for (int i = 0; i < numArrows; i++) {
            int arrowX;
            if (still) {
                arrowX = minX + (i * arrowTotalWidth);
            } else {
                arrowX = minX + (i * arrowTotalWidth) + (int) (offset % arrowTotalWidth);

                if (arrowX > maxX) {
                    arrowX -= numArrows * arrowTotalWidth;
                }
            }

            if (arrowX + arrowWidth > minX) {
                guiGraphics.blitSprite(ARROW_SPRITE, arrowX, minY, arrowWidth, arrowHeight);
            }
        }
        guiGraphics.disableScissor();
    }

    private boolean isValidInput() {
        return this.getNewValue() != this.challengeHolder.getCurrentPoints();
    }

    private int getNewValue() {
        try {
            return Integer.parseInt(this.newValueBox.getValue());
        } catch (NumberFormatException e) {
            return this.challengeHolder.getCurrentPoints();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;

        if (this.isValidInput() && (keyCode == 257 || keyCode == 335)) {
            this.doneButton.onPress();
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (this.newValueBox.isHovered() && button == GLFW.GLFW_MOUSE_BUTTON_2) {
            this.newValueBox.setValue("");
            return true;
        }

        if (this.newValueBox.isFocused() && !this.newValueBox.isHovered()) {
            this.newValueBox.setFocused(false);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;

        if (this.newValueBox.isHoveredOrFocused()) {
            try {
                int value = (int) Mth.clamp(this.getNewValue() + Math.round(scrollY), 0, this.challengeHolder.getRequiredPoints());
                this.newValueBox.setValue(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return false;
            }
            return true;
        }

        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.setColor(this.parent.configuration.getBackgroundColor().getRed(), this.parent.configuration.getBackgroundColor().getGreen(), this.parent.configuration.getBackgroundColor().getBlue(), this.parent.configuration.getBackgroundColor().getAlpha());
        guiGraphics.blitSprite(BACKGROUND_SPRITE, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent.lock(false));
    }
}
