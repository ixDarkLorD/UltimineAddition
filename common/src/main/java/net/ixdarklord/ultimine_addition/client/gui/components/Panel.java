package net.ixdarklord.ultimine_addition.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.ixdarklord.coolcatlib.api.client.gui.components.animations.AnimatedComponent;
import net.ixdarklord.coolcatlib.api.client.gui.components.animations.SlideAnimation;
import net.ixdarklord.coolcatlib.api.client.gui.components.animations.SlideAnimation.Direction;
import net.ixdarklord.coolcatlib.api.client.utils.RenderUtils;
import net.ixdarklord.coolcatlib.api.client.utils.ScreenAnchor;
import net.ixdarklord.coolcatlib.api.utils.ColorUtils;
import net.ixdarklord.coolcatlib.api.utils.MathUtils;
import net.ixdarklord.ultimine_addition.client.gui.layouts.LinearLayout;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class Panel implements LayoutElement {
    private static final ResourceLocation TITLE_TEXTURE = FTBUltimineAddition.getGuiSprite("challenge_panel/title");
    private static final ResourceLocation SLOT_INDICATOR_TEXTURE = FTBUltimineAddition.getGuiSprite("challenge_panel/slot_indicator");
    private static final ResourceLocation DESCRIPTION_TEXTURE = FTBUltimineAddition.getGuiSprite("challenge_panel/description");
    public static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private final Minecraft mc;
    private final int slot;
    private final boolean notifyPanel;
    private boolean active = true;
    private int x;
    private int y;
    private int width;
    private int height;
    private Component title = Component.empty();
    private final Set<Info> infos = new TreeSet<>();
    private boolean consumeMode = false;
    private final AnimatedComponent animatedComponent;
    private boolean assignedToRemove = false;
    private float time;
    private long lastStamp = System.nanoTime();
    private LinearLayout infosLayout;

    private Panel(int slot, boolean notifyPanel) {
        this.width = 112;
        this.height = !notifyPanel ? 30 : 15;
        this.mc = Minecraft.getInstance();
        this.slot = slot;
        this.notifyPanel = notifyPanel;
        this.animatedComponent = new SlideAnimation(0.5F, Direction.HORIZONTAL);
    }

    public static Panel create(int slot, boolean notifyPanel) {
        return new Panel(slot, notifyPanel);
    }

    private void update() {
        long currentTime = System.nanoTime();
        float delta = (float) (currentTime - this.lastStamp) / 1.0E9F * 20.0F;
        this.lastStamp = currentTime;
        if (!this.mc.isPaused()) {
            this.time += delta;
        }

        this.animatedComponent.update();
    }

    public void render(GuiGraphics guiGraphics) {
        this.update();
        if (this.active) {
            SkillsRecordScreen.OverlayColor overlayColor = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
            if (!this.notifyPanel) {
                RenderSystem.setShaderColor(overlayColor.red(), overlayColor.green(), overlayColor.blue(), overlayColor.alpha());
                guiGraphics.blit(SLOT_INDICATOR_TEXTURE, this.x + 5, this.y, 0.0F, 0.0F, 51, 10, 51, 10);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int spacing = 13 * this.slot;
                double value = MathUtils.cycledBetweenValues(0.0F, 1.0F, 0.8F, this.time / 20.0F, false);
                Color color = ColorUtils.blend(new Color(10223406), new Color(8311081), ConfigHandler.CLIENT.ANIMATIONS_MODE.get() ? value : (double) 0.0F);
                guiGraphics.fill(this.x + 8 + spacing, this.y + 3, this.x + 14 + spacing, this.y + 9, color.getRGB());
            }

            RenderSystem.setShaderColor(overlayColor.red(), overlayColor.green(), overlayColor.blue(), overlayColor.alpha());
            RenderUtils.blitNineSliced(guiGraphics, TITLE_TEXTURE, this.x, this.y + (!this.notifyPanel ? 10 : 0), this.getWidth(), 15, 18, 7, 112, 15);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (!this.notifyPanel) {
                TRANSLUCENT_TRANSPARENCY.setupRenderState();
                RenderSystem.setShaderColor(overlayColor.red(), overlayColor.green(), overlayColor.blue(), overlayColor.alpha());
                RenderUtils.blitNineSliced(guiGraphics, DESCRIPTION_TEXTURE, this.x, this.y + 22, this.getWidth(), this.infos.isEmpty() ? 0 : this.getInfoHeight() + 8, 4, 112, 14);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                TRANSLUCENT_TRANSPARENCY.clearRenderState();
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderUtils.drawScrollingString(guiGraphics, (int) (this.time * 8.0F), this.mc.font, this.title, true, new ScreenRectangle(this.x, this.y + (!this.notifyPanel ? 13 : 3), this.getWidth(), 9), 8, Color.WHITE.getRGB(), true);
            this.updateInfoLayout();

            for (Info info : this.getInfos()) {
                info.render(guiGraphics, this.time, overlayColor);
            }
        }
    }

    private void updateInfoLayout() {
        this.infosLayout = LinearLayout.vertical().spacing(4);
        this.getInfos().forEach(this.infosLayout::addChild);
        this.infosLayout.arrangeElements();
        this.infosLayout.setPosition(this.x + 2, this.y + 27);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height + this.getInfoHeight();
    }

    public int getInfoHeight() {
        return this.infosLayout == null ? 0 : Math.max(0, this.infosLayout.getHeight());
    }

    public void visitWidgets(Consumer<AbstractWidget> consumer) {
    }

    public void setActive(boolean active) {
        if (!this.notifyPanel) {
            this.active = active;
        }
    }

    public void setTitle(Component title) {
        this.title = title;
    }

    public void markRemoved() {
        this.assignedToRemove = true;
    }

    public void cancelRemoval() {
        this.assignedToRemove = false;
        this.animatedComponent.play(false);
    }

    public void addInfo(MiningSkillCardData.Challenge challenge) {
        if (!this.notifyPanel) {
            this.infos.add(new Info(this, challenge.getOrder(), challenge.getId(), challenge.getCurrentPoints(), challenge.getRequiredPoints()));
        }
    }

    public void removeInfo(Info info) {
        this.infos.removeIf(info::equals);
    }

    public void setConsumeMode(boolean state) {
        this.consumeMode = state;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isInactive() {
        return !this.active;
    }

    public boolean isAssignedToRemove() {
        return this.assignedToRemove;
    }

    public int getSlot() {
        return this.slot;
    }

    public Component getTitle() {
        return this.title;
    }

    public Set<Info> getInfos() {
        return this.infos;
    }

    public AnimatedComponent getAnimatedComponent() {
        return this.animatedComponent;
    }

    public boolean isNotifyPanel() {
        return this.notifyPanel;
    }

    public Panel copy() {
        Panel copy = new Panel(this.slot, this.notifyPanel);
        copy.active = this.active;
        copy.x = this.x;
        copy.y = this.y;
        copy.title = this.title.copy();
        copy.assignedToRemove = this.assignedToRemove;

        for (Info info : this.infos) {
            copy.infos.add(info.copy(copy));
        }

        copy.updateInfoLayout();
        return copy;
    }

    public enum Align implements StringRepresentable {
        TOP_LEFT(0, ScreenAnchor.TOP_LEFT),
        TOP_RIGHT(2, ScreenAnchor.TOP_RIGHT),
        LEFT(3, ScreenAnchor.LEFT),
        RIGHT(5, ScreenAnchor.RIGHT),
        BOTTOM_LEFT(6, ScreenAnchor.BOTTOM_LEFT),
        BOTTOM_RIGHT(8, ScreenAnchor.BOTTOM_RIGHT);

        private final int posIndex;
        private final ScreenAnchor screenPos;

        Align(int posIndex, ScreenAnchor screenPos) {
            this.posIndex = posIndex;
            this.screenPos = screenPos;
        }

        public ScreenAnchor toScreenPos() {
            return this.screenPos;
        }

        public Align next() {
            int nextOrdinal = (this.ordinal() + 1) % values().length;
            return values()[nextOrdinal];
        }

        public Align previous() {
            int previousOrdinal = (this.ordinal() - 1 + values().length) % values().length;
            return values()[previousOrdinal];
        }

        public @NotNull String getSerializedName() {
            return this.name().toLowerCase();
        }

        public int getPosIndex() {
            return this.posIndex;
        }
    }

    public record Key(UUID uuid, int slotIndex) implements Comparable<Key> {
        public int compareTo(@NotNull Key that) {
            return Integer.compare(this.slotIndex, that.slotIndex);
        }
    }

    public static class Info implements LayoutElement, Comparable<Info> {
        private static final int PADDING = 4;
        private final Panel parent;
        private final int order;
        private final ResourceLocation challengeId;
        private int currentValue;
        private int requiredValue;
        private int x;
        private int y;
        private int width;
        private int height;

        public Info(Panel parent, int order, ResourceLocation challengeId, int currentValue, int requiredValue) {
            this.parent = parent;
            this.order = order;
            this.challengeId = challengeId;
            this.currentValue = currentValue;
            this.requiredValue = requiredValue;
        }

        public void render(GuiGraphics guiGraphics, float time, SkillsRecordScreen.OverlayColor overlayColor) {
            this.width = this.parent.getWidth() - PADDING;
            Minecraft mc = Minecraft.getInstance();
            Font font = mc.font;
            PoseStack pose = guiGraphics.pose();
            float TITLE_SCALE = 0.9F;
            float ID_SCALE = 0.8F;
            float DESC_SCALE = 0.8F;
            float PROGRESS_SCALE = 0.9F;
            int BAR_TOP_PADDING = 2;
            int BAR_SIDE_PADDING = 2;
            int BAR_HEIGHT = 3;
            int TOTAL_PADDING = 8;
            int BAR_WIDTH = this.width - PADDING;

            int titleHeight = (int) (9.0F * TITLE_SCALE);
            int idHeight = (int) (9.0F * ID_SCALE);
            int progressTextHeight = (int) (9.0F * PROGRESS_SCALE);
            List<Component> descriptionLines = ChallengesManager.INSTANCE.createChallengeDescription(this.challengeId, Style.EMPTY, time / 20.0F, Style.EMPTY);
            int descHeight = 0;

            for (Component line : descriptionLines) {
                int lineCount = font.split(line, this.width + 25).size();
                descHeight += (int) (9.0F * DESC_SCALE * (float) lineCount);
            }

            this.height = titleHeight + idHeight + descHeight + progressTextHeight + BAR_TOP_PADDING + BAR_HEIGHT + TOTAL_PADDING + 1;
            float progress = this.requiredValue > 0 ? (float) this.currentValue / (float) this.requiredValue : 0.0F;
            int percent = (int) (progress * 100.0F);

            pose.pushPose();
            pose.translate((float) (this.x + BAR_SIDE_PADDING), (float) this.y, 0.0F);
            pose.scale(0.9F, 0.9F, 1.0F);
            Component title = Component.translatable("challenge.ultimine_addition.title", this.order).withStyle(ChatFormatting.BOLD);
            guiGraphics.drawString(font, title, 0, 0, 16777215, true);
            pose.popPose();

            pose.pushPose();
            pose.translate((float) (this.x + 2), (float) (this.y + titleHeight + 2), 0.0F);
            pose.scale(0.8F, 0.8F, 1.0F);
            Component idLine = Component.translatable("challenge.%s.%s.name".formatted(this.challengeId.getNamespace(), this.challengeId.getPath().replace("/", "."))).withStyle(ChatFormatting.AQUA);
            int cWidth = this.width - 12;
            guiGraphics.drawString(font, Component.literal("\ud83d\udcdd"), 1, -1, Color.WHITE.getRGB(), true);
            RenderUtils.drawScrollingString(guiGraphics, (int) (time * 16.0F), font, idLine, false, new ScreenRectangle(10, 0, cWidth, idHeight), new ScreenRectangle(this.x + 10, this.y + titleHeight, cWidth, titleHeight), 16777215, true);
            pose.popPose();

            int descStartY = this.y + 4 + titleHeight + idHeight;
            guiGraphics.fill(this.x + 4, descStartY, this.x + 5, descStartY + descHeight + 1, ColorUtils.rgbToRgba(Color.BLACK, 0.25F));
            guiGraphics.fill(this.x + 3, descStartY - 1, this.x + 4, descStartY + descHeight, Color.LIGHT_GRAY.getRGB());
            pose.pushPose();
            pose.translate((float) (this.x + 6), (float) descStartY, 0.0F);
            pose.scale(0.8F, 0.8F, 1.0F);
            int descY = 0;

            for (Component line : descriptionLines) {
                for (FormattedCharSequence s : font.split(line, this.width + 25)) {
                    guiGraphics.drawString(font, s, 0, descY, Color.LIGHT_GRAY.getRGB(), true);
                    Objects.requireNonNull(font);
                    descY += 9;
                }
            }

            pose.popPose();
            int progressY = 4 + descStartY + (int) ((float) descY * 0.8F);
            pose.pushPose();
            pose.translate((float) (this.x + 4), (float) progressY, 0.0F);
            pose.scale(0.9F, 0.9F, 1.0F);
            boolean isConsuming = ChallengesManager.INSTANCE.getChallengeData(this.getChallengeId()).map((data) -> data.challengeType().isConsuming()).orElse(false);
            String symbol = this.currentValue < this.requiredValue && isConsuming && !parent.consumeMode ? "✘" : "»";
            ChatFormatting baseFormat = this.currentValue >= this.requiredValue ? ChatFormatting.GREEN : ChatFormatting.GOLD;
            ChatFormatting finalFormating = this.currentValue < this.requiredValue && isConsuming ? (parent.consumeMode ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.RED) : baseFormat;
            Component progressText = Component.literal(String.format("%s %d%% (%d/%d)", symbol, percent, this.currentValue, this.requiredValue)).withStyle(finalFormating);
            guiGraphics.drawString(font, progressText, 0, 0, 16766720, false);
            pose.popPose();
            pose.pushPose();
            pose.translate((float) (this.x + 2), (float) (progressY + progressTextHeight + 2), 0.0F);
            RenderSystem.setShaderColor(overlayColor.red(), overlayColor.green(), overlayColor.blue(), overlayColor.alpha());
            RenderUtils.blitNineSliced(guiGraphics, SkillsRecordScreen.PROGRESS_BAR_SPRITE, -1, -1, BAR_WIDTH + 2, 5, 18, 3, 156, 7);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Color barColor = ColorUtils.multiBlend(progress, new Color(16721960), new Color(16750098), new Color(5832487));
            int filledWidth = (int) ((float) BAR_WIDTH * progress);
            guiGraphics.fillGradient(0, 0, filledWidth, 3, barColor.getRGB(), barColor.darker().getRGB());
            if (filledWidth < BAR_WIDTH) {
                guiGraphics.fill(filledWidth, 0, filledWidth + 1, 3, barColor.darker().darker().darker().getRGB());
            }
            pose.popPose();
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        public int getOrder() {
            return this.order;
        }

        public ResourceLocation getChallengeId() {
            return this.challengeId;
        }

        public int getCurrentValue() {
            return this.currentValue;
        }

        public int getRequiredValue() {
            return this.requiredValue;
        }

        public void setCurrentValue(int currentValue) {
            this.currentValue = currentValue;
        }

        public void setRequiredValue(int requiredValue) {
            this.requiredValue = requiredValue;
        }

        public Panel getParent() {
            return this.parent;
        }

        public void visitWidgets(Consumer<AbstractWidget> consumer) {
        }

        public int compareTo(@NotNull Info that) {
            return Integer.compare(this.order, that.order);
        }

        public boolean equals(Object o) {
            if (!(o instanceof Info info)) {
                return false;
            } else {
                return this.order == info.order && Objects.equals(this.challengeId, info.challengeId);
            }
        }

        public int hashCode() {
            return Objects.hash(this.order, this.challengeId);
        }

        public Info copy(Panel parent) {
            Info copy = new Info(parent, this.order, this.challengeId, this.currentValue, this.requiredValue);
            copy.x = this.x;
            copy.y = this.y;
            copy.width = this.width;
            copy.height = this.height;
            return copy;
        }
    }
}
