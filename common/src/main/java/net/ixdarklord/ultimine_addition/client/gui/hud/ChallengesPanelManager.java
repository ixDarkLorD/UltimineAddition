package net.ixdarklord.ultimine_addition.client.gui.hud;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import net.ixdarklord.coolcatlib.api.client.gui.components.animations.AnimatedComponent;
import net.ixdarklord.ultimine_addition.client.gui.components.Panel;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.config.ConfigHandler.CLIENT;
import net.ixdarklord.ultimine_addition.config.ConfigHandler.COMMON;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class ChallengesPanelManager {
    public static ChallengesPanelManager INSTANCE = new ChallengesPanelManager();
    private final Minecraft mc = Minecraft.getInstance();
    private final Map<Panel.Key, Panel> panelMap = new TreeMap<>();
    private Panel.Align panelAlign;
    private final int panelPadding = 4;

    public void render(GuiGraphics guiGraphics, DeltaTracker ignored) {
        if (COMMON.PLAYSTYLE_MODE.get() != PlaystyleMode.LEGACY) {
            if (this.panelAlign == null) {
                this.panelAlign = CLIENT.CHALLENGES_PANEL_ALIGNMENT.get();
            }

            Window window = this.mc.getWindow();
            Player player = this.mc.player;
            if (player != null) {
                ItemStack stack = ItemUtils.findItemInHand(player, Registration.SKILLS_RECORD.get());
                if (this.shouldProcess(stack)) {
                    SkillsRecordData data = SkillsRecordData.load(stack);
                    this.createPanels(data);
                    this.updatePanels(data);
                }

                this.validatePanels(stack);
                this.resizePanels();
                this.adjustPanelsToFitScreen(window);
                this.alignPanels(window);

                for(Panel panel : this.panelMap.values()) {
                    panel.render(guiGraphics, stack);
                }

            }
        }
    }

    private void resizePanels() {
        int maxTextLength = 0;

        for(Panel panel : this.panelMap.values()) {
            if (!panel.isInactive() && !panel.isNotifyPanel()) {
                int titleWidth = this.mc.font.width(panel.getTitle());
                maxTextLength = Math.max(maxTextLength, titleWidth);
            }
        }

        int length = maxTextLength - 94;
        boolean b = length > 0;

        for(Panel panel : this.panelMap.values()) {
            if (panel.isActive()) {
                panel.setWidth(112 + (b ? length : 0));
            }
        }

    }

    private boolean shouldProcess(ItemStack stack) {
        if (!SkillsRecordData.hasData(stack)) {
            Collection<Panel> panels = this.panelMap.values();
            panels.forEach(Panel::markRemoved);
            this.slideOutPanels(panels);
            return false;
        } else {
            return !this.mc.isPaused();
        }
    }

    private void slideOutPanels(Collection<Panel> panels) {
        panels.stream().filter(Panel::isAssignedToRemove).map(Panel::getAnimatedComponent).filter(AnimatedComponent::isForward).forEach((component) -> component.play(true));
    }

    private void createPanels(SkillsRecordData recordData) {
        boolean hasInactivePanel = this.panelMap.values().stream().anyMatch(Panel::isInactive);
        if (hasInactivePanel && this.notContainPanel(recordData.getUUID(), 999)) {
            this.createNotifyPanel(recordData.getUUID());
        }

        for(int slotIndex = 0; slotIndex < recordData.getCardSlots().size(); ++slotIndex) {
            if (this.notContainPanel(recordData.getUUID(), slotIndex)) {
                Optional<MiningSkillCardData> cardOpt = recordData.getCardData(slotIndex);
                if (cardOpt.isPresent()) {
                    MiningSkillCardData cardData = cardOpt.get();

                    for(MiningSkillCardData.Challenge challenge : cardData.getChallenges()) {
                        if (challenge.isPinned()) {
                            Panel panel = this.getOrCreatePanel(recordData.getUUID(), slotIndex);
                            panel.setTitle(cardData.getStack().getHoverName());
                        }
                    }
                }
            }
        }

    }

    private void validatePanels(ItemStack stack) {
        if (SkillsRecordData.hasData(stack)) {
            SkillsRecordData recordData = SkillsRecordData.load(stack);
            this.panelMap.forEach((key, panel) -> {
                AnimatedComponent anim = panel.getAnimatedComponent();
                if (!panel.isAssignedToRemove() && !anim.isForward()) {
                    anim.play(false);
                }

                if (!panel.isNotifyPanel()) {
                    Optional<MiningSkillCardData> cardData = recordData.getCardData(key.slotIndex());
                    cardData.ifPresentOrElse((data) -> {
                        Set<Panel.Info> flaggedInfos = new HashSet<>();

                        for(Panel.Info info : panel.getInfos()) {
                            Optional<MiningSkillCardData.Challenge> challengeOpt = data.getChallenge(info.getChallengeId());
                            if (challengeOpt.isEmpty()) {
                                flaggedInfos.add(info);
                            } else {
                                MiningSkillCardData.Challenge challenge = challengeOpt.get();
                                if (challenge.isPinned() && panel.isAssignedToRemove()) {
                                    panel.cancelRemoval();
                                } else if (!challenge.isPinned()) {
                                    flaggedInfos.add(info);
                                }
                            }
                        }

                        flaggedInfos.forEach(panel::removeInfo);
                        if (panel.getInfos().isEmpty()) {
                            panel.markRemoved();
                        }

                    }, panel::markRemoved);
                }
            });
        }

        List<Panel> panelsToSlideOff = this.panelMap.values().stream().filter((p) -> p.isActive() && p.isAssignedToRemove() && p.getAnimatedComponent().isForward()).toList();
        if (!panelsToSlideOff.isEmpty()) {
            this.slideOutPanels(panelsToSlideOff);
        }

        this.panelMap.entrySet().removeIf((entry) -> {
            Panel panel = entry.getValue();
            AnimatedComponent anim = panel.getAnimatedComponent();
            return panel.isAssignedToRemove() && (panel.isInactive() || anim.isFinished());
        });
    }

    private void adjustPanelsToFitScreen(Window window) {
        this.panelMap.values().forEach((p) -> p.setActive(true));
        List<Map.Entry<Panel.Key, Panel>> entries = Lists.newArrayList(this.panelMap.entrySet());
        int screenHeight = window.getGuiScaledHeight();

        while(!this.doesLayoutFitScreen(entries, screenHeight)) {
            for(int i = entries.size() - 1; i >= 0; --i) {
                Panel panel = entries.get(i).getValue();
                if (panel.isActive() && !panel.isNotifyPanel()) {
                    panel.setActive(false);
                    if (this.doesLayoutFitScreen(entries, screenHeight)) {
                        break;
                    }
                }
            }
        }

        if (this.panelMap.values().stream().allMatch(Panel::isActive)) {
            this.panelMap.values().stream().filter(Panel::isNotifyPanel).forEach(Panel::markRemoved);
        }

    }

    private boolean doesLayoutFitScreen(List<Map.Entry<Panel.Key, Panel>> entries, int screenHeight) {
        LinearLayout tempLayout = LinearLayout.vertical().spacing(panelPadding);
        entries.stream().map(Map.Entry::getValue).filter(Panel::isActive).forEach((p) -> tempLayout.addChild(p.copy()));
        tempLayout.arrangeElements();
        return tempLayout.getY() >= 0 && tempLayout.getY() + tempLayout.getHeight() <= screenHeight;
    }

    private void alignPanels(Window window) {
        LinearLayout layout = LinearLayout.vertical().spacing(panelPadding);
        this.panelMap.values().stream().filter(Panel::isActive).forEach(layout::addChild);
        layout.arrangeElements();
        int baseX = this.panelAlign.toScreenPos().getX(window.getGuiScaledWidth(), layout.getWidth(), 4);
        int baseY = this.panelAlign.toScreenPos().getY(window.getGuiScaledHeight(), layout.getHeight(), 4);
        layout.setPosition(baseX, baseY);

        for(Panel panel : this.panelMap.values()) {
            AnimatedComponent anim = panel.getAnimatedComponent();
            AnimatedComponent.Position positions = anim.getRelativePosition(this.panelAlign.toScreenPos(), panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight(), 4);
            panel.setPosition(positions.x(), positions.y());
        }

    }

    private void updatePanels(SkillsRecordData data) {
        this.panelMap.forEach((key, panel) -> {
            if (panel.isNotifyPanel()) {
                panel.setTitle(Component.translatable("gui.ultimine_addition.skills_record.pin.panel", this.getInactivePanelsSize()));
            } else {
                data.getCardData(key.slotIndex()).ifPresent((cardData) -> {
                    panel.setTitle(cardData.getStack().getHoverName());
                    cardData.getChallenges().forEach((challenge) -> {
                        if (challenge.isPinned()) {
                            panel.addInfo(challenge);
                        }
                    });
                    panel.getInfos().forEach((info) -> cardData.getChallenge(info.getChallengeId()).ifPresent((challenge) -> {
                        if (challenge.isPinned()) {
                            info.setCurrentValue(challenge.getCurrentPoints());
                            info.setRequiredValue(challenge.getRequiredPoints());
                        }

                    }));
                });
            }
        });
    }

    private int getInactivePanelsSize() {
        return (int)this.panelMap.values().stream().filter(Panel::isInactive).count();
    }

    public void cycleAlignment(boolean next) {
        this.panelAlign = next ? this.panelAlign.next() : this.panelAlign.previous();
    }

    public Panel.Align getPanelAlignment() {
        return this.panelAlign;
    }

    private boolean notContainPanel(UUID uuid, int slotIndex) {
        return !this.panelMap.containsKey(new Panel.Key(uuid, slotIndex));
    }

    private Panel getOrCreatePanel(UUID uuid, int slotIndex) {
        Panel.Key key = new Panel.Key(uuid, slotIndex);
        return this.panelMap.computeIfAbsent(key, (k) -> Panel.create(slotIndex, false));
    }

    private void createNotifyPanel(UUID uuid) {
        Panel.Key key = new Panel.Key(uuid, 999);
        this.panelMap.computeIfAbsent(key, (k) -> Panel.create(999, true));
    }
}