package net.ixdarklord.ultimine_addition.client.gui.screens;

import net.minecraft.util.ARGB;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.math.Axis;
import net.ixdarklord.coolcatlib.api.client.gui.components.ColorableImageButton;
import net.ixdarklord.coolcatlib.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.SkillsRecordPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Optional;

public class EditChallengeScreen extends Screen {
    private static final Identifier BACKGROUND_SPRITE = FTBUltimineAddition.id("container/skills_record/edit_challenge/background");
    private static final Identifier EQUAL_SPRITE = FTBUltimineAddition.id("container/skills_record/edit_challenge/equal");
    private static final Identifier ARROW_SPRITE = FTBUltimineAddition.id("container/skills_record/edit_challenge/arrow");
    protected int imageWidth = 155;
    protected int imageHeight = 91;
    protected int leftPos;
    protected int topPos;
    private final SkillsRecordScreen parent;
    private final MiningSkillCardData.Challenge challenge;
    private EditBox newValueBox;
    private ColorableImageButton doneButton;

    public EditChallengeScreen(@NotNull SkillsRecordScreen parent, MiningSkillCardData.Challenge challenge) {
        super(Component.translatable("selectWorld.edit").append(" ").append(Component.translatable("challenge.ultimine_addition.title", Component.literal("[%s]".formatted(challenge.getOrder())))));
        this.parent = parent.lock(true);
        this.challenge = challenge;
    }

    @Override
    protected void init() {
        this.parent.width = this.width;
        this.parent.height = this.height;
        this.parent.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(new StringWidget(this.leftPos + 16, this.topPos + 34, 123, 9, Component.literal(this.challenge.getId().toString()), this.font) {
            private int ticks = 0;
            private boolean showSuccess = false;

            @Override
            public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                Component component = Component.translatable("gui.ultimine_addition.skills_record.edit.copy_id").withStyle(ChatFormatting.GRAY);
                Component successComponent = Component.translatable("gui.ultimine_addition.skills_record.edit.copy_success").withStyle(ChatFormatting.GREEN);
                this.setTooltip(Tooltip.create(showSuccess ? successComponent : component));
                RenderUtils.drawScrollingString(guiGraphics, (int)(Util.getMillis() / 4L), EditChallengeScreen.this.font, this.getMessage(), true, new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight()), 1, Color.WHITE.getRGB(), true);

                if (showSuccess) {
                    ticks--;
                    if (ticks <= 0) {
                        showSuccess = false;
                    }
                }
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                if (this.isHovered() && event.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
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

        this.newValueBox = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 91, this.topPos + 50, 34, 10, Component.translatable("gui.ultimine_addition.skills_record.edit.new_value")));
        this.newValueBox.setBordered(false);
        this.newValueBox.setTooltip(Tooltip.create(Component.translatable("gui.ultimine_addition.skills_record.edit.new_value", this.challenge.getRequiredPoints())));
        this.newValueBox.insertText(String.valueOf(this.challenge.getCurrentPoints()));
        this.newValueBox.setMaxLength(String.valueOf(this.challenge.getRequiredPoints()).length());
        // EditBox has no input filter anymore; strip anything that isn't a digit.
        this.newValueBox.setResponder(s -> {
            String digits = s.replaceAll("[^0-9]", "");
            if (!digits.equals(s)) this.newValueBox.setValue(digits);
        });

        FrameLayout layout = new FrameLayout(this.leftPos + 10, this.topPos + 64, 135, 17);
        LinearLayout linearLayout = layout.addChild(LinearLayout.horizontal().spacing(5));

        this.doneButton = linearLayout.addChild(this.addRenderableWidget(new ColorableImageButton(0, 0, 45, 13, SkillsRecordScreen.BUTTON_SPRITES, button -> {
            SkillsRecordData data = this.parent.getMenu().getData();
            Optional<MiningSkillCardData> dataOpt = data.getCardData(this.parent.selectedSlot);
            if (dataOpt.isPresent()) {
                dataOpt.get().setAmount(this.challenge.getId(), this.getNewValue()).save();
                data.onClientUpdate().save();
                PayloadHandler.sendToServer(new SkillsRecordPayload.EditChallenge(this.parent.selectedSlot, this.challenge.getId(), this.getNewValue()));

                assert this.minecraft != null;
                assert this.minecraft.player != null;
                this.minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.5F);
                this.onClose();
            }
        }, CommonComponents.GUI_DONE) {
            @Override
            public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
                this.extractDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
            }
        }));

        linearLayout.addChild(this.addRenderableWidget(new ColorableImageButton(0, 0, 45, 13, SkillsRecordScreen.BUTTON_SPRITES, button ->
                EditChallengeScreen.this.onClose(), CommonComponents.GUI_CANCEL) {

            @Override
            public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
                this.extractDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
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
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        this.doneButton.active = this.isValidInput();
        try {
            int value = Integer.parseInt(this.newValueBox.getValue());
            this.newValueBox.setValue(String.valueOf(Math.min(value, this.challenge.getRequiredPoints())));
            this.newValueBox.setTextColor(value > this.challenge.getCurrentPoints() ? new Color(0x3BFF4B).getRGB() : value < this.challenge.getCurrentPoints() ? new Color(0xFF5E5E).getRGB() : Color.WHITE.getRGB());
        } catch (NumberFormatException ignored) {
        }

        int ticks = (int) Util.getMillis() / 4;
        RenderUtils.drawScrollingString(guiGraphics, ticks, this.font, this.title, true, new ScreenRectangle(this.leftPos + 10, this.topPos + 10, 135, 18), 4, Color.WHITE.getRGB(), true);
        RenderUtils.drawScrollingString(guiGraphics, ticks, this.font, Component.literal("" + this.challenge.getCurrentPoints()), false, new ScreenRectangle(this.leftPos + 29, this.topPos + 49, 36, 9), 1, Color.GRAY.getRGB(), true);
        this.renderArrow(guiGraphics);
    }

    private void renderArrow(GuiGraphicsExtractor guiGraphics) {
        int value = this.getNewValue();
        boolean still = value == this.challenge.getCurrentPoints();
        boolean flip = value < this.challenge.getCurrentPoints();
        Identifier texture = still ? EQUAL_SPRITE : ARROW_SPRITE;
        int minX = this.leftPos + (still ? 68 : 66);
        int minY = this.topPos + 48;
        int maxX = this.leftPos + 88;
        int maxY = this.topPos + 60;
        int arrowWidth = 19;
        int arrowHeight = 12;
        int spacing = 4;
        int speed = 50;
        int conveyorWidth = maxX - minX;
        int conveyorHeight = maxY - minY;
        int arrowTotalWidth = arrowWidth + spacing;
        int numArrows = conveyorWidth / arrowTotalWidth + 2;
        long currentTime = Util.getMillis();
        double offset = still ? (double)0.0F : (double)currentTime / (double)1000.0F * (double)speed;
        guiGraphics.enableScissor(minX, minY, maxX, maxY);
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate((float)minX, (float)maxY);

        for(int i = 0; i < numArrows; ++i) {
            int localArrowX;
            if (still) {
                localArrowX = i * arrowTotalWidth;
            } else {
                localArrowX = i * arrowTotalWidth + (int)((flip ? -offset : offset) % (double)arrowTotalWidth);
                if (localArrowX > conveyorWidth) {
                    localArrowX -= numArrows * arrowTotalWidth;
                }

                if (localArrowX + arrowWidth < 0) {
                    localArrowX += numArrows * arrowTotalWidth;
                }
            }

            if (localArrowX + arrowWidth > 0 && localArrowX < conveyorWidth) {
                pose.pushMatrix();
                if (flip) {
                    pose.translate((float)localArrowX + (float)arrowWidth / 2.0F, (float)(-conveyorHeight) / 2.0F);
                    pose.rotate((float) Math.toRadians(-180.0F));
                    pose.translate(-((float)localArrowX + (float)arrowWidth / 2.0F), (float)conveyorHeight / 2.0F);
                }

                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, localArrowX, -conveyorHeight, arrowWidth, arrowHeight);
                pose.popMatrix();
            }
        }

        pose.popMatrix();
        guiGraphics.disableScissor();
    }

    private boolean isValidInput() {
        return this.getNewValue() != this.challenge.getCurrentPoints();
    }

    private int getNewValue() {
        try {
            return Integer.parseInt(this.newValueBox.getValue());
        } catch (NumberFormatException e) {
            return this.challenge.getCurrentPoints();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event))
            return true;

        int keyCode = event.key();
        if (this.isValidInput() && (keyCode == 257 || keyCode == 335)) {
            this.doneButton.onPress(event);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }

        int button = event.button();
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
                int value = (int) Mth.clamp(this.getNewValue() + Math.round(scrollY), 0, this.challenge.getRequiredPoints());
                this.newValueBox.setValue(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return false;
            }
            return true;
        }

        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // The Skills Record stays visible behind the dialog (with its own background and blur; blurring again here
        // isn't allowed in the same frame), dimmed, and the dialog starts a new layer so none of it shows through.
        this.parent.extractRenderStateWithTooltipAndSubtitles(guiGraphics, -1, -1, partialTick);
        guiGraphics.nextStratum();
        guiGraphics.fill(0, 0, this.width, this.height, 0x90000000);
        SkillsRecordScreen.OverlayColor color = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, this.leftPos, this.topPos, this.imageWidth, this.imageHeight,
                ARGB.colorFromFloat(color.alpha(), color.red(), color.green(), color.blue()));
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
