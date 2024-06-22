package net.ixdarklord.ultimine_addition.client.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.ixdarklord.coolcat_lib.client.gui.component.animation.AnimatedComponent;
import net.ixdarklord.coolcat_lib.client.gui.component.animation.SlideAnimation;
import net.ixdarklord.coolcat_lib.client.gui.screen.ScreenPosition;
import net.ixdarklord.coolcat_lib.util.ColorUtils;
import net.ixdarklord.coolcat_lib.util.MathUtils;
import net.ixdarklord.coolcat_lib.util.ScreenUtils;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

import static net.ixdarklord.ultimine_addition.util.ItemUtils.findItemInHand;

public class ChallengesInfoPanel {
    private static final ResourceLocation HUD = new ResourceLocation(UltimineAddition.MOD_ID, "textures/gui/challenge_panel.png");
    public static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    public static final ChallengesInfoPanel INSTANCE = new ChallengesInfoPanel();

    private final int textureWidth = 112;
    private final int textureHeight = 32;
    private final List<Panel> PANEL_LIST = new ArrayList<>();
    private Panel.Position panelPos = Panel.Position.LEFT;
    private float time;
    private float lastStamp;

    private void tick(float partialTicks) {
        if (partialTicks < this.lastStamp) {
            this.time += 1.0F - this.lastStamp;
            this.time += partialTicks;
        } else {
            this.time += partialTicks - this.lastStamp;
        }

        this.lastStamp = partialTicks;
        this.updatePos();
    }

    private void updatePos() {
        this.panelPos = ConfigHandler.CLIENT.CHALLENGES_PANEL_POSITION.get();
        for (Panel panel : PANEL_LIST) {
            panel.getAnimatedComponent().setScreenPosition(this.panelPos.toScreenPos());
        }
    }

    public void render(PoseStack poseStack, float partialTicks) {
        if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) return;
        Minecraft MC = Minecraft.getInstance();
        if (MC.options.renderDebug) return;

        Window window = MC.getWindow();
        Font font = MC.font;
        Player player = MC.player;
        if (player == null) return;

        ItemStack stack = findItemInHand(player, ModItems.SKILLS_RECORD);
        if (stack == ItemStack.EMPTY) slideOutPanels(PANEL_LIST.stream().filter(Panel::isNotTestPanel));
        if (!MC.isPaused() && !PANEL_LIST.isEmpty()) tick(partialTicks);

        if (!MC.isPaused()) {
            SkillsRecordData recordData = new SkillsRecordData().loadData(stack);
            createPanels(recordData);
            validatePanels(recordData, window);
            PANEL_LIST.forEach(panel -> panel.animatedComponent.updateAnimation());
        }

        poseStack.pushPose();
        int textLength = getTextLength(font);
        for (int slot = 0; slot < PANEL_LIST.size(); slot++) {
            Panel panel = PANEL_LIST.get(slot);
            if (panel.isNotActive()) continue;

            AnimatedComponent animatedComponent = panel.getAnimatedComponent();
            if (animatedComponent.getScreenPosition() == null) break;

            int[] offsets = animatedComponent.getAnimatedOffsets(window.getGuiScaledWidth(), window.getGuiScaledHeight(), panel.getWidth() + textLength, panel.getHeight(), 4);
            int X = offsets[0];
            int Y = getAlignedYPos(offsets[1], slot, 4);

            SkillsRecordScreen.BGColor bgColor = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
            if (panel.isNotMorePanel()) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, HUD);

                // Slots
                RenderSystem.setShaderColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), bgColor.getAlpha());
                GuiComponent.blit(poseStack, X + 5, Y, 5, 0, 51, 10, 256, 256);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                int spacing = 13 * Math.min(panel.getSlot(), 3);
                double value = MathUtils.cycledBetweenValues(0.0F, 1.0F, 0.8F, this.time/20.0F, false);
                Color color = ColorUtils.blendColors(new Color(0x6AB020), new Color(0x89E229), ConfigHandler.CLIENT.ANIMATIONS_MODE.get() ? value : 0.0F);
                GuiComponent.fill(poseStack, X+8 + spacing, Y+3, X+14 + spacing, Y+9, color.getRGB());

                // Title BG
                RenderSystem.setShaderColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), bgColor.getAlpha());
                ScreenUtils.blitNineSliced(poseStack, X, Y + 10, panel.getWidth() + textLength, 11, 20, 4, panel.getWidth(), 11, 0, 10);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                // Info BG
                TRANSLUCENT_TRANSPARENCY.setupRenderState();
                RenderSystem.setShaderColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), bgColor.getAlpha());
                ScreenUtils.blitNineSliced(poseStack, X, Y + 18, panel.getWidth() + textLength, 14 + Math.max(0, font.lineHeight * (panel.getInfos().size() - 1)), 20, 4, panel.getWidth(), 14, 0, 21);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                TRANSLUCENT_TRANSPARENCY.clearRenderState();

                // Title String
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                GuiComponent.drawCenteredString(poseStack, font, panel.getTitle(), X + ((panel.getWidth() + textLength) / 2), Y + 11, Color.WHITE.getRGB());
            } else {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, HUD);

                RenderSystem.setShaderColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), bgColor.getAlpha());
                ScreenUtils.blitNineSliced(poseStack, X, Y, panel.getWidth() + textLength, 11, 20, 4, panel.getWidth(), 11, 0, 10);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                ScreenUtils.drawScrollingString(poseStack, (int) time * 8, font, panel.getTitle(), true, X, Y, panel.getWidth() + textLength, 10, 4, Color.WHITE.getRGB());
            }

            for (int i = 0; i < panel.getInfos().size(); i++) {
                Panel.Info info = panel.getInfos().get(i);
                int currentProgression;
                float value = (float) info.getCurrentValue() / info.getRequiredValue() * 100;
                currentProgression = (int) value;

                Component values = new TextComponent("%" + currentProgression).withStyle(currentProgression >= 100 ? ChatFormatting.GREEN : ChatFormatting.GOLD);
                int valuesXPos = font.width(values) - font.width("%0");

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                ScreenUtils.drawScrollingString(poseStack, (int) time * 8, font, info.getMessage().copy().withStyle(ChatFormatting.GRAY), false, X + 2, Y + 22 + (font.lineHeight * i), 94 + textLength - valuesXPos, 8, 0, Color.WHITE.getRGB());
                GuiComponent.drawString(poseStack, font, values, X + textLength + 98 - valuesXPos, Y + 22 + (font.lineHeight * i), Color.WHITE.getRGB());
            }
        }
        poseStack.popPose();
    }

    private void createPanels(SkillsRecordData recordData) {
        var trashList = new ArrayList<>(PANEL_LIST.stream().filter(panel -> !panel.getAnimatedComponent().isPlaying()).filter(Panel::isNotMorePanel).filter(Panel::isNotTestPanel).toList());
        for (int i = 0; i < recordData.getCardSlots().size(); i++) {
            int cardSlot = i;
            ItemStack cardStack = recordData.getCardSlots().get(cardSlot);
            MiningSkillCardData cardData = new MiningSkillCardData().loadData(cardStack);
            cardData.getChallenges().forEach((identifier, infoData) -> {
                PANEL_LIST.removeIf(panel -> panel.getUUID() == null || !panel.getUUID().equals(recordData.getUUID()));
                var list = PANEL_LIST.stream().filter(panel -> panel.getSlot() == cardSlot).toList();
                ChallengesManager manager = ChallengesManager.INSTANCE;
                ChallengesData challengesData = manager.getAllChallenges().get(identifier.id());
                Component message = new TextComponent(identifier.id().toString());
                if (challengesData != null) {
                    List<ItemStack> items = manager.utilizeTargetedBlocks(challengesData).stream().map(ItemStack::new).toList();
                    message = SkillsRecordScreen.createChallengeDescription(challengesData.getChallengeType().getTypeName(), Style.EMPTY, items, SkillsRecordScreen.getItemCycle());
                }
                if (infoData.isPinned()) {
                    if (list.isEmpty()) {
                        PANEL_LIST.add(new Panel(
                                recordData.getUUID(),
                                cardSlot,
                                new Panel.TextureDimension(textureWidth, textureHeight),
                                new TextComponent(recordData.getCardSlots().get(cardSlot).getHoverName().getString()), List.of(new Panel.Info(identifier.order(), identifier.id(), message, infoData.getCurrentValue(), infoData.getRequiredValue())), new SlideAnimation(this.panelPos.toScreenPos(), 60))
                        );
                    } else {
                        Panel panel = list.get(0);
                        trashList.removeIf(panel1 -> panel1.equals(panel));
                        var infoList = panel.getInfos();
                        AnimatedComponent component = panel.getAnimatedComponent();

                        var filteredInfoList = infoList.stream().filter(info -> info.getChallengeId().equals(identifier.id())).toList();
                        if (filteredInfoList.isEmpty()) {
                            infoList.add(new Panel.Info(identifier.order(), identifier.id(), message, infoData.getCurrentValue(), infoData.getRequiredValue()));
                        } else {
                            Component finalMessage = message;
                            filteredInfoList.forEach(info -> info.setMessage(finalMessage));
                            filteredInfoList.forEach(info -> {
                                info.setCurrentValue(infoData.getCurrentValue());
                                info.setRequiredValue(infoData.getRequiredValue());
                            });
                        }
                        if (component.isPlaying() && !component.isEnteringScene()) {
                            panel.getAnimatedComponent().startAnimation(true);
                        }
                    }
                } else {
                    if (!list.isEmpty()) {
                        Panel panel = list.get(0);
                        var infoList = panel.getInfos();
                        infoList.removeIf(info -> info.getChallengeId().equals(identifier.id()));
                    }
                }
            });
        }
        slideOutPanels(trashList.stream());
    }

    private void validatePanels(SkillsRecordData recordData, Window window) {
        PANEL_LIST.forEach(panel -> panel.setActive(true));
        for (int slot = PANEL_LIST.size(); slot-- > 0;) {
            Panel panel = PANEL_LIST.get(slot);
            if (panel.isNotActive() || panel.isMorePanel) continue;

            AnimatedComponent animatedComponent = panel.getAnimatedComponent();
            if (animatedComponent.getScreenPosition() == null) break;

            int offsetY = animatedComponent.getAnimatedOffsets(0, window.getGuiScaledHeight(), 0, panel.getHeight(), 4)[1];
            int Y = getAlignedYPos(offsetY, slot, 4);
            if (this.panelPos != Panel.Position.BOTTOM_LEFT && this.panelPos != Panel.Position.BOTTOM_RIGHT) {
                Y += panel.getHeight();
            }

            if (Y < 0 || Y > window.getGuiScaledHeight()-4) {
                if (this.panelPos == Panel.Position.BOTTOM_LEFT || this.panelPos == Panel.Position.BOTTOM_RIGHT) {
                    PANEL_LIST.get((PANEL_LIST.size()-1) - (slot+1)).setActive(false);
                } else panel.setActive(false);
            }
        }

        int inactiveSlots = PANEL_LIST.stream().filter(Panel::isNotActive).toList().size();
        for (int slot = PANEL_LIST.size(); slot-- > 0;) {
            Panel panel = PANEL_LIST.get(slot);
            AnimatedComponent component = panel.getAnimatedComponent();
            if ((!component.isPlaying() || panel.isNotActive()) && !component.isEnteringScene()) {
                panel.setRemoved();
            }
        }

        var list = PANEL_LIST.stream().filter(panel -> panel.getSlot() == 999).toList();
        if (inactiveSlots > 0) {
            if (list.isEmpty()) {
                PANEL_LIST.add(new Panel(
                        recordData.getUUID(),
                        999,
                        new Panel.TextureDimension(textureWidth, 12),
                        new TranslatableComponent("gui.ultimine_addition.skills_record.pin.panel", inactiveSlots),
                        null,
                        new SlideAnimation(this.panelPos.toScreenPos(), 60),
                        true, false));
            } else {
                Panel panel = list.get(0);
                panel.setTitle(new TranslatableComponent("gui.ultimine_addition.skills_record.pin.panel", inactiveSlots));

                AnimatedComponent component = panel.getAnimatedComponent();
                if (recordData.get() != ItemStack.EMPTY && component.isPlaying() && !component.isEnteringScene()) {
                    panel.getAnimatedComponent().startAnimation(true);
                }
            }
        } else if (!list.isEmpty()) {
            slideOutPanels(list.stream());
        }

        PANEL_LIST.removeIf(Panel::isAssignedToRemove);
        PANEL_LIST.sort(Comparator.comparingInt(Panel::getSlot));
    }

    private int getTextLength(Font font) {
        int textLength = 0;
        for (Panel panel : PANEL_LIST) {
            if (panel.isNotActive()) continue;
            if (panel.isNotMorePanel()) {
                textLength = Math.max(font.width(panel.getTitle()), textLength);
                textLength = Math.max(0, textLength - panel.getWidth());
            }
        }
        return textLength > 0 ? textLength + 8 : 0;
    }

    private int getAlignedYPos(int yOffset, int slot, @SuppressWarnings("SameParameterValue") int padding) {
        int Y = yOffset;
        switch (this.panelPos) {
            case TOP_LEFT, TOP_RIGHT -> {
                for (int i = 0; i < slot; i++) {
                    Panel panel = PANEL_LIST.get(i);
                    if (panel.isNotActive()) continue;
                    Y += (panel.getHeight() + padding);
                }
            }
            case LEFT, RIGHT -> {
                Y += Math.max(0, (PANEL_LIST.get(slot).getHeight()/2));
                for (Panel panel : PANEL_LIST) {
                    if (panel.isNotActive()) continue;
                    Y -= ((panel.getHeight() / 2) + (padding / 2));
                }
                for (int i = 0; i < slot; i++) {
                    Panel panel = PANEL_LIST.get(i);
                    if (panel.isNotActive()) continue;
                    Y += (panel.getHeight() + padding);
                }
            }
            case BOTTOM_LEFT, BOTTOM_RIGHT -> {
                for (int i = slot+1; i < PANEL_LIST.size(); i++) {
                    Panel panel = PANEL_LIST.get(i);
                    if (panel.isNotActive()) continue;
                    Y -= (panel.getHeight() + padding);
                }
            }
        }
        return Y;
    }

    private void slideOutPanels(Stream<Panel> panelStream) {
        panelStream.map(Panel::getAnimatedComponent)
                .filter(AnimatedComponent::isEnteringScene)
                .forEach(component -> component.startAnimation(false));
    }

    public void setPanelPos(Panel.Position panelPos) {
        this.panelPos = panelPos;
    }

    public Panel.Position getPanelPos() {
        return panelPos;
    }

    public static class Panel {
        private boolean active;

        private final UUID uuid;

        private final TextureDimension textureDimension;

        private final int slot;

        private Component title;

        private final List<Info> infos;

        private final AnimatedComponent animatedComponent;

        private boolean assignedToRemove;

        private final boolean isMorePanel;

        private final boolean isTestPanel;

        Panel(UUID uuid, int slot, TextureDimension textureDimension, Component title, List<Info> infos, AnimatedComponent animatedComponent) {
            this(uuid, slot, textureDimension, title, infos, animatedComponent, false, false, false);
        }

        Panel(UUID uuid, int slot, TextureDimension textureDimension, Component title, List<Info> infos, AnimatedComponent animatedComponent, boolean isMorePanel, boolean isTestPanel) {
            this(uuid, slot, textureDimension, title, infos, animatedComponent, false, isMorePanel, isTestPanel);
        }

        Panel(UUID uuid, int slot, TextureDimension textureDimension, Component title, List<Info> infos, AnimatedComponent animatedComponent, boolean assignedToRemove, boolean isMorePanel, boolean isTestPanel) {
            this.active = true;
            this.uuid = uuid;
            this.slot = slot;
            this.textureDimension = textureDimension;
            this.title = title;
            this.infos = infos == null ? new ArrayList<>() : new ArrayList<>(infos);
            this.animatedComponent = animatedComponent;
            this.assignedToRemove = assignedToRemove;
            this.isMorePanel = isMorePanel;
            this.isTestPanel = isTestPanel;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public void setTitle(Component title) {
            this.title = title;
        }

        public void setRemoved() {
            this.assignedToRemove = true;
        }

        public UUID getUUID() {
            return uuid;
        }

        public boolean isNotActive() {
            return !active;
        }

        public int getSlot() {
            return this.slot;
        }

        public int getWidth() {
            return textureDimension.width();
        }

        public int getHeight() {
            return textureDimension.height() + Math.max(0, Minecraft.getInstance().font.lineHeight * (infos.size() - 1));
        }

        public TextureDimension getTextureDimension() {
            return textureDimension;
        }

        public Component getTitle() {
            return this.title;
        }

        public List<Info> getInfos() {
            if (this.infos == null) return new ArrayList<>();
            this.infos.sort(Comparator.comparingInt(o -> o.order));
            return this.infos;
        }

        public AnimatedComponent getAnimatedComponent() {
            return this.animatedComponent;
        }

        public boolean isAssignedToRemove() {
            return this.assignedToRemove;
        }

        public boolean isNotMorePanel() {
            return !this.isMorePanel;
        }

        public boolean isNotTestPanel() {
            return !this.isTestPanel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Panel panel = (Panel) o;
            return active == panel.active && slot == panel.slot && assignedToRemove == panel.assignedToRemove && isMorePanel == panel.isMorePanel && isTestPanel == panel.isTestPanel && Objects.equals(uuid, panel.uuid) && Objects.equals(textureDimension, panel.textureDimension) && Objects.equals(title, panel.title) && Objects.equals(infos, panel.infos) && Objects.equals(animatedComponent, panel.animatedComponent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(active, uuid, textureDimension, slot, title, infos, animatedComponent, assignedToRemove, isMorePanel, isTestPanel);
        }

        public static class Info {
            private final int order;
            private final ResourceLocation challengeId;
            private Component message;
            private int currentValue;
            private int requiredValue;

            private Info(int order, ResourceLocation challengeId, Component message, int currentValue, int requiredValue) {
                this.order = order;
                this.challengeId = challengeId;
                this.message = message;
                this.currentValue = currentValue;
                this.requiredValue = requiredValue;
            }

            public ResourceLocation getChallengeId() {
                return challengeId;
            }

            public Component getMessage() {
                return message;
            }

            public int getCurrentValue() {
                return currentValue;
            }

            public int getRequiredValue() {
                return requiredValue;
            }

            public void setMessage(Component message) {
                this.message = message;
            }

            public void setCurrentValue(int currentValue) {
                this.currentValue = currentValue;
            }

            public void setRequiredValue(int requiredValue) {
                this.requiredValue = requiredValue;
            }
        }

        public record TextureDimension(int width, int height) {}

        public enum Position implements StringRepresentable {
            DISABLED(-1, null),
            TOP_LEFT(0, ScreenPosition.TOP_LEFT),
            TOP_RIGHT(2, ScreenPosition.TOP_RIGHT),
            LEFT(3, ScreenPosition.LEFT),
            RIGHT(5, ScreenPosition.RIGHT),
            BOTTOM_LEFT(6, ScreenPosition.BOTTOM_LEFT),
            BOTTOM_RIGHT(8, ScreenPosition.BOTTOM_RIGHT);

            private final int posIndex;
            private final @Nullable ScreenPosition screenPos;

            Position(int posIndex, @Nullable ScreenPosition screenPos) {
                this.posIndex = posIndex;
                this.screenPos = screenPos;
            }

            @Nullable
            public ScreenPosition toScreenPos() {
                return screenPos;
            }

            public Position next() {
                int nextOrdinal = (this.ordinal() + 1) % values().length;
                return values()[nextOrdinal];
            }

            public Position previous() {
                int previousOrdinal = (this.ordinal() - 1 + values().length) % values().length;
                return values()[previousOrdinal];
            }

            @Override
            public @NotNull String getSerializedName() {
                return name().toLowerCase();
            }

            public int getPosIndex() {
                return posIndex;
            }
        }
    }
}
