package net.ixdarklord.ultimine_addition.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.client.gui.components.ColoredButton;
import net.ixdarklord.ultimine_addition.client.gui.layouts.LinearLayout;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packets.SkillsRecordPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class EditChallengeScreen extends Screen {
    private static final ResourceLocation BACKGROUND_SPRITE = FTBUltimineAddition.getGuiSprite("container/skills_record/edit_challenge/background");
    private static final ResourceLocation EQUAL_SPRITE = FTBUltimineAddition.getGuiSprite("container/skills_record/edit_challenge/equal");
    private static final ResourceLocation ARROW_SPRITE = FTBUltimineAddition.getGuiSprite("container/skills_record/edit_challenge/arrow");
    private final int imageWidth = 155;
    private final int imageHeight = 91;
    private int leftPos;
    private int topPos;
    private final SkillsRecordScreen parent;
    private final MiningSkillCardData.Challenge challenge;
    private EditBox newValueBox;
    private ColoredButton doneButton;

    public EditChallengeScreen(@NotNull SkillsRecordScreen parent, MiningSkillCardData.Challenge challenge) {
        super(Component.translatable("selectWorld.edit").append(" ").append(Component.translatable("challenge.ultimine_addition.title", Component.literal("[%s]".formatted(challenge.getOrder())))));
        this.parent = parent.lock(true);
        this.challenge = challenge;
    }

    protected void init() {
        this.parent.width = this.width;
        this.parent.height = this.height;
        this.parent.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.newValueBox = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 91, this.topPos + 50, 34, 10, Component.translatable("gui.ultimine_addition.skills_record.edit.new_value")));
        this.newValueBox.setBordered(false);
        this.newValueBox.setTooltip(Tooltip.create(Component.translatable("gui.ultimine_addition.skills_record.edit.new_value", this.challenge.getRequiredPoints())));
        this.newValueBox.insertText(String.valueOf(this.challenge.getCurrentPoints()));
        this.newValueBox.setMaxLength(String.valueOf(this.challenge.getRequiredPoints()).length());
        this.newValueBox.setFilter((s) -> {
            try {
                if (!s.isEmpty()) {
                    Integer.parseInt(s);
                }
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        FrameLayout layout = new FrameLayout(this.leftPos + 10, this.topPos + 63, 135, 17);
        LinearLayout linearLayout = layout.addChild(LinearLayout.horizontal().spacing(5));
        this.doneButton = linearLayout.addChild(this.addRenderableWidget(new ColoredButton(0, 0, 45, 14, 5, SkillsRecordScreen.BUTTON_SPRITES, 45, 12, (button) -> {
            SkillsRecordData data = this.parent.getMenu().getData();
            Optional<MiningSkillCardData> dataOpt = data.getCardData(this.parent.selectedSlot);
            if (dataOpt.isPresent()) {
                dataOpt.get().setAmount(this.challenge.getId(), this.getNewValue()).save();
                data.onClientUpdate().save();
                PacketHandler.sendToServer(new SkillsRecordPacket.EditChallenge(this.parent.selectedSlot, this.challenge.getId(), this.getNewValue()));

                assert this.minecraft != null;

                assert this.minecraft.player != null;

                this.minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.5F);
                this.onClose();
            }
        }, CommonComponents.GUI_DONE)));
        linearLayout.addChild(this.addRenderableWidget(new ColoredButton(0, 0, 45, 14, 5, SkillsRecordScreen.BUTTON_SPRITES, 45, 12, (button) -> this.onClose(), CommonComponents.GUI_CANCEL)));
        layout.arrangeElements();
        this.addRenderableWidget(new StringWidget(this.leftPos + 16, this.topPos + 34, 123, 9, Component.literal(this.challenge.getId().toString()), this.font) {
            private int ticks = 0;
            private boolean showSuccess = false;

            public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                Component component = Component.translatable("gui.ultimine_addition.skills_record.edit.copy_id").withStyle(ChatFormatting.GRAY);
                Component successComponent = Component.translatable("gui.ultimine_addition.skills_record.edit.copy_success").withStyle(ChatFormatting.GREEN);
                this.setTooltip(Tooltip.create(this.showSuccess ? successComponent : component));
                RenderUtils.drawScrollingString(guiGraphics, (int) (Util.getMillis() / 4L), EditChallengeScreen.this.font, this.getMessage(), true, new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight()), 1, Color.WHITE.getRGB(), true);
                if (this.showSuccess) {
                    --this.ticks;
                    if (this.ticks <= 0) {
                        this.showSuccess = false;
                    }
                }

            }

            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (this.isHovered() && button == 0) {
                    assert EditChallengeScreen.this.minecraft != null;

                    EditChallengeScreen.this.minecraft.keyboardHandler.setClipboard(this.getMessage().getString());
                    this.playDownSound(EditChallengeScreen.this.minecraft.getSoundManager());
                    this.showSuccess = true;
                    this.ticks = 80;
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    protected void clearWidgets() {
        super.clearWidgets();
        this.parent.clearWidgets();
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.parent.render(guiGraphics, mouseX, mouseY, partialTick);
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, 310.0F);
        this.renderBackground(guiGraphics);
        this.renderBg(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.doneButton.active = this.isValidInput();

        try {
            int value = Integer.parseInt(this.newValueBox.getValue());
            this.newValueBox.setValue(String.valueOf(Math.min(value, this.challenge.getRequiredPoints())));
            this.newValueBox.setTextColor(value > this.challenge.getCurrentPoints() ? (new Color(3931979)).getRGB() : (value < this.challenge.getCurrentPoints() ? (new Color(16735838)).getRGB() : Color.WHITE.getRGB()));
        } catch (NumberFormatException e) {
        }

        int ticks = (int) Util.getMillis() / 4;
        RenderUtils.drawScrollingString(guiGraphics, ticks, this.font, this.title, true, new ScreenRectangle(this.leftPos + 10, this.topPos + 10, 135, 18), 4, Color.WHITE.getRGB(), true);
        RenderUtils.drawScrollingString(guiGraphics, ticks, this.font, Component.literal("" + this.challenge.getCurrentPoints()), false, new ScreenRectangle(this.leftPos + 29, this.topPos + 49, 36, 9), 1, Color.GRAY.getRGB(), true);
        this.renderArrow(guiGraphics);
        pose.popPose();
    }

    private void renderArrow(GuiGraphics guiGraphics) {
        int value = this.getNewValue();
        boolean still = value == this.challenge.getCurrentPoints();
        boolean flip = value < this.challenge.getCurrentPoints();
        ResourceLocation texture = still ? EQUAL_SPRITE : ARROW_SPRITE;
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
        double offset = still ? (double) 0.0F : (double) currentTime / (double) 1000.0F * (double) speed;
        guiGraphics.enableScissor(minX, minY, maxX, maxY);
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate((float) minX, (float) maxY, 0.0F);

        for (int i = 0; i < numArrows; ++i) {
            int localArrowX;
            if (still) {
                localArrowX = i * arrowTotalWidth;
            } else {
                localArrowX = i * arrowTotalWidth + (int) ((flip ? -offset : offset) % (double) arrowTotalWidth);
                if (localArrowX > conveyorWidth) {
                    localArrowX -= numArrows * arrowTotalWidth;
                }

                if (localArrowX + arrowWidth < 0) {
                    localArrowX += numArrows * arrowTotalWidth;
                }
            }

            if (localArrowX + arrowWidth > 0 && localArrowX < conveyorWidth) {
                pose.pushPose();
                if (flip) {
                    pose.translate((float) localArrowX + (float) arrowWidth / 2.0F, (float) (-conveyorHeight) / 2.0F, 0.0F);
                    pose.mulPose(Axis.ZN.rotationDegrees(180.0F));
                    pose.translate(-((float) localArrowX + (float) arrowWidth / 2.0F), (float) conveyorHeight / 2.0F, 0.0F);
                }

                guiGraphics.blit(texture, localArrowX, -conveyorHeight, 0.0F, 0.0F, arrowWidth, arrowHeight, arrowWidth, arrowHeight);
                pose.popPose();
            }
        }

        pose.popPose();
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

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (!this.isValidInput() || keyCode != 257 && keyCode != 335) {
            return false;
        } else {
            this.doneButton.onPress();
            return true;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        } else if (this.newValueBox.isHovered() && button == 1) {
            this.newValueBox.setValue("");
            return true;
        } else {
            if (this.newValueBox.isFocused() && !this.newValueBox.isHovered()) {
                this.newValueBox.setFocused(false);
            }

            return false;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (super.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        } else if (this.newValueBox.isHoveredOrFocused()) {
            try {
                int value = (int) Mth.clamp((float) ((long) this.getNewValue() + Math.round(delta)), 0.0F, (float) this.challenge.getRequiredPoints());
                this.newValueBox.setValue(String.valueOf(value));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public void renderBg(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        SkillsRecordScreen.OverlayColor color = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
        guiGraphics.setColor(color.red(), color.green(), color.blue(), color.alpha());
        guiGraphics.blit(BACKGROUND_SPRITE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        assert this.minecraft != null;

        this.minecraft.setScreen(this.parent.lock(false));
    }
}
