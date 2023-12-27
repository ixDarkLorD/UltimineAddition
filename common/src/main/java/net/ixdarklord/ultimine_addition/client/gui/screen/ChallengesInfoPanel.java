package net.ixdarklord.ultimine_addition.client.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.ixdarklord.coolcat_lib.client.gui.component.animation.AnimatedComponent;
import net.ixdarklord.coolcat_lib.client.gui.component.animation.SlideAnimation;
import net.ixdarklord.coolcat_lib.client.gui.screen.ScreenPosition;
import net.ixdarklord.coolcat_lib.util.ColorUtils;
import net.ixdarklord.coolcat_lib.util.ScreenUtils;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static net.ixdarklord.ultimine_addition.util.ItemUtils.findItemInHand;

public class ChallengesInfoPanel {
    private static final ResourceLocation HUD = new ResourceLocation(Constants.MOD_ID, "textures/gui/challenge_panel.png");
    public static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    private static final int textureWidth = 112;
    private static final int textureHeight = 32;
    private static final int edgePadding = 4;
    private static final int panelPadding = 2;
    private static final ScreenPosition PANEL_POS = ScreenPosition.LEFT;
    private static final List<Panel> PANEL_LIST = new ArrayList<>();
    private static int tickCount;

    public static void render(GuiGraphics guiGraphics, float ignored) {
        if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) return;
        Minecraft MC = Minecraft.getInstance();
        Window window = MC.getWindow();
        Font font = MC.font;
        Player player = MC.player;
        if (player == null) return;

        ItemStack stack = findItemInHand(player, ModItems.SKILLS_RECORD);
        if (stack == ItemStack.EMPTY) slideOutPanels(PANEL_LIST.stream().filter(Panel::isNotTestPanel));
        if (!MC.isPaused() && !PANEL_LIST.isEmpty()) tickCount++;

        SkillsRecordData recordData = new SkillsRecordData().loadData(stack);
        createPanels(recordData);
        validatePanels(recordData, window, font);
        if (!MC.isPaused()) PANEL_LIST.forEach(panel -> panel.animatedComponent.updateAnimation());

        int[] yPosAligner = new int[]{0, 0};
        int textLength = 0;
        for (Panel panel : PANEL_LIST) {
            if (panel.isNotActive()) continue;
            if (panel.isNotMorePanel()) {
                textLength = Math.max(font.width(panel.getTitle()), textLength);
            }
            yPosAligner[0] += Math.max(0, font.lineHeight * (panel.getInfos().size() - 1));
        }
        textLength = Math.max(0, textLength - textureWidth);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0F, 0F, -200F);
        for (int slot = 0; slot < PANEL_LIST.size(); slot++) {
            Panel panel = PANEL_LIST.get(slot);
            AnimatedComponent animatedComponent = panel.getAnimatedComponent();
            if (panel.isNotActive()) continue;

            int length = textLength > 0 ? textLength + 8 : 0;
            int[] offsets = animatedComponent.getAnimatedOffsets(window.getGuiScaledWidth(), window.getGuiScaledHeight(), textureWidth + length, textureHeight, edgePadding);
            int X = offsets[0];
            int Y = getAlignedYPos(offsets, yPosAligner, slot);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (panel.isNotMorePanel()) {
                // Slots
                TRANSLUCENT_TRANSPARENCY.setupRenderState();
                guiGraphics.blit(HUD, X + 5, Y, 5, 0, 51, 10, 256, 256);
                int spacing = 13 * Math.min(panel.getSlot(), 3);
                guiGraphics.fill(X+8 + spacing, Y+3, X+14 + spacing, Y+9, ColorUtils.RGBToRGBA(Color.GREEN.getRGB(), 0.50F));
                TRANSLUCENT_TRANSPARENCY.clearRenderState();

                // Title BG
                guiGraphics.blitNineSliced(HUD, X, Y + 10, textureWidth + length, 11, 20, 4, textureWidth, 11, 0, 10);

                // Info BG
                TRANSLUCENT_TRANSPARENCY.setupRenderState();
                guiGraphics.blitNineSliced(HUD, X, Y + 18, textureWidth + length, 14 + Math.max(0, font.lineHeight * (panel.getInfos().size() - 1)), 20, 4, textureWidth, 14, 0, 21);
                TRANSLUCENT_TRANSPARENCY.clearRenderState();

                // Title String
                guiGraphics.drawCenteredString(font, panel.getTitle(), X + ((textureWidth + length) / 2), Y + 11, Color.WHITE.getRGB());
            } else {
                guiGraphics.blitNineSliced(HUD, X, Y, textureWidth + length, 11, 20, 4, textureWidth, 11, 0, 10);
                ScreenUtils.drawScrollingString(guiGraphics, tickCount*2, font, panel.getTitle(), true, X, Y, textureWidth + length, 10, 2, Color.WHITE.getRGB());
            }

            AtomicInteger i = new AtomicInteger();
            panel.getInfos().forEach(info -> {
                int currentProgression;
                float value = (float) info.getCurrentValue() / info.getRequiredValue() * 100;
                currentProgression = (int) value;

                Component values = Component.literal("%" + currentProgression).withStyle(currentProgression >= 100 ? ChatFormatting.GREEN : ChatFormatting.GOLD);
                int valuesXPos = font.width(values) - font.width("%0");

                ScreenUtils.drawScrollingString(guiGraphics, tickCount*2, font, info.getMessage().copy().withStyle(ChatFormatting.GRAY), false, X + 2, Y + 22 + (font.lineHeight * i.get()), 94 + length - valuesXPos, 8, 0, Color.WHITE.getRGB());
                guiGraphics.drawString(font, values, X + length + 98 - valuesXPos, Y + 22 + (font.lineHeight * i.get()), Color.WHITE.getRGB());
                i.getAndIncrement();
            });
            yPosAligner[1] += Math.max(0, font.lineHeight * (panel.getInfos().size()-1));
        }
        guiGraphics.pose().popPose();
    }

    private static void createPanels(SkillsRecordData recordData) {
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
                Component message = Component.literal(identifier.id().toString());
                if (challengesData != null) {
                    List<ItemStack> items = manager.utilizeTargetedBlocks(challengesData).stream().map(ItemStack::new).toList();
                    message = SkillsRecordScreen.createChallengeDescription(challengesData.getChallengeType().getTypeName(), Style.EMPTY, items, SkillsRecordScreen.getItemCycle());
                }
                if (infoData.isPinned()) {
                    if (list.isEmpty()) {
                        PANEL_LIST.add(new Panel(
                                recordData.getUUID(),
                                cardSlot,
                                Component.literal(recordData.getCardSlots().get(cardSlot).getHoverName().getString()),
                                List.of(new Panel.Info(identifier.order(), identifier.id(), message, infoData.getCurrentValue(), infoData.getRequiredValue())),
                                new SlideAnimation(PANEL_POS, 60))
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

    private static void validatePanels(SkillsRecordData recordData, Window window, Font font) {
        int count = 0;
        PANEL_LIST.forEach(panel -> panel.setActive(true));
        for (int slot = PANEL_LIST.size(); slot-- > 0;) {
            int currentPos = 0;
            for (int i = 0; i < PANEL_LIST.size()-count; i++) {
                Panel panel = PANEL_LIST.get(i);
                currentPos += Math.max(0, font.lineHeight * (panel.getInfos().size() - 1));
                if (i > 0) currentPos += textureHeight + panelPadding;
            }
            Panel panel = PANEL_LIST.get(slot);
            if (panel.isMorePanel) {
                panel = PANEL_LIST.get(Math.max(0, slot-1));
            }
            float yPos = PANEL_POS.getY(window.getGuiScaledHeight(), textureHeight, edgePadding);
            yPos -= (float) currentPos / 2;
            if (yPos < 0 || yPos > window.getGuiScaledWidth()) {
                count++;
                panel.setActive(false);
            }
        }
        int inactiveSlots = PANEL_LIST.stream().filter(Panel::isNotActive).toList().size();
        for (int slot = PANEL_LIST.size(); slot-- > 0;) {
            Panel panel = PANEL_LIST.get(slot);
            AnimatedComponent component = panel.getAnimatedComponent();
            if (!component.isPlaying() && !component.isEnteringScene()) {
                panel.setRemoved();
            }
        }

        var list = PANEL_LIST.stream().filter(panel -> panel.getSlot() == 999).toList();
        if (inactiveSlots > 0) {
            if (list.isEmpty()) {
                PANEL_LIST.add(new Panel(
                        recordData.getUUID(),
                        999,
                        Component.translatable("gui.ultimine_addition.skills_record.pin.panel", inactiveSlots),
                        null,
                        new SlideAnimation(PANEL_POS, 60),
                        true, false));
            } else {
                Panel panel = list.get(0);
                panel.setTitle(Component.translatable("gui.ultimine_addition.skills_record.pin.panel", inactiveSlots));

                AnimatedComponent component = panel.getAnimatedComponent();
                if (recordData.get() != ItemStack.EMPTY && component.isPlaying() && !component.isEnteringScene()) {
                    panel.getAnimatedComponent().startAnimation(true);
                }
            }
        } else if (!list.isEmpty()) {
            list.forEach(Panel::setRemoved);
        }

        PANEL_LIST.removeIf(Panel::isAssignedToRemove);
        PANEL_LIST.sort(Comparator.comparingInt(Panel::getSlot));
    }

    private static int getAlignedYPos(int[] offsets, int[] yPosAligner, int slot) {
        int Y = offsets[1];
        for (int i = 0; i < PANEL_LIST.size(); i++) {
            Panel panel1 = PANEL_LIST.get(i);
            if (i == 0 || panel1.isNotActive()) continue;
            Y += (textureHeight / 2) + (panelPadding / 2);
        }
        Y -= yPosAligner[0] / 2;
        Y += yPosAligner[1];
        for (int i = slot +1; i < PANEL_LIST.size(); i++) {
            Panel panel1 = PANEL_LIST.get(i);
            if (panel1.isNotActive()) continue;
            Y -= (textureHeight + panelPadding);
        }
        return Y;
    }

    private static void slideOutPanels(Stream<Panel> pinnedStream) {
        pinnedStream.map(Panel::getAnimatedComponent)
                .filter(AnimatedComponent::isEnteringScene)
                .forEach(component -> component.startAnimation(false));
    }

    private static class Panel {
        private final UUID uuid;

        private boolean active;

        private final int slot;

        private Component title;

        private final List<Info> infos;

        private final AnimatedComponent animatedComponent;

        private boolean assignedToRemove;

        private final boolean isMorePanel;

        private final boolean isTestPanel;

        Panel(UUID uuid, int slot, Component title, List<Info> infos, AnimatedComponent animatedComponent) {
            this(uuid, slot, title, infos, animatedComponent, false, false, false);
        }

        Panel(UUID uuid, int slot, Component title, List<Info> infos, AnimatedComponent animatedComponent, boolean isMorePanel, boolean isTestPanel) {
            this(uuid, slot, title, infos, animatedComponent, false, isMorePanel, isTestPanel);
        }

        Panel(UUID uuid, int slot, Component title, List<Info> infos, AnimatedComponent animatedComponent, boolean assignedToRemove, boolean isMorePanel, boolean isTestPanel) {
            this.uuid = uuid;
            this.active = true;
            this.slot = slot;
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
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Panel) obj;
            return Objects.equals(this.uuid, that.uuid) &&
                    this.active == that.active &&
                    this.slot == that.slot &&
                    Objects.equals(this.title, that.title) &&
                    Objects.equals(this.infos, that.infos) &&
                    Objects.equals(this.animatedComponent, that.animatedComponent) &&
                    this.assignedToRemove == that.assignedToRemove &&
                    this.isMorePanel == that.isMorePanel &&
                    this.isTestPanel == that.isTestPanel;
        }

        @Override
        public int hashCode() {
            return Objects.hash(active, slot, title, infos, animatedComponent, assignedToRemove, isMorePanel, isTestPanel);
        }

        @Override
        public String toString() {
            return "Pinned[" +
                    "active=" + active + ", " +
                    "cardSlot=" + slot + ", " +
                    "title=" + title + ", " +
                    "infos=" + infos + ", " +
                    "animatedComponent=" + animatedComponent + ", " +
                    "assignedToRemove=" + assignedToRemove + ", " +
                    "isMorePanel=" + isMorePanel + ", " +
                    "isTestPanel=" + isTestPanel + ']';
        }

        private static class Info {
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
    }
}
