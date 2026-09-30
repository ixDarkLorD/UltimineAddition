package net.ixdarklord.ultimine_addition.client.event;

import net.ixdarklord.coolcatcore.api.event.v2.client.ClientCommandEvents;
import net.ixdarklord.coolcatcore.api.event.v2.client.ClientGuiEvents;
import net.ixdarklord.coolcatcore.api.event.v2.client.ClientPlayerEvents;
import net.ixdarklord.coolcatcore.api.event.v2.client.ClientTickEvents;
import net.ixdarklord.ultimine_addition.client.undo.UndoGrowthClient;
import net.ixdarklord.ultimine_addition.client.undo.UndoPreviewClient;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordClientCache;
import net.ixdarklord.ultimine_addition.client.commands.SkillsRecordDebugCommand;
import net.ixdarklord.ultimine_addition.client.gui.hud.MinerCertificateStatus;
import net.ixdarklord.ultimine_addition.client.gui.hud.UltimineNoticeHud;
import net.ixdarklord.ultimine_addition.client.gui.hud.ChallengesPanelManager;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.SkillsRecordPayload;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.client.Minecraft;

public final class ClientEventHandler {
    public static void register() {
        net.ixdarklord.coolcatcore.api.event.v2.client.ItemTooltipEvents.MODIFY.register(ItemTooltipEvents::init);
        ClientGuiEvents.RENDER_HUD.register(ChallengesPanelManager.INSTANCE::render);
        ClientPlayerEvents.LEAVE.register(ChallengesPanelManager.INSTANCE::cleanup);
        ClientPlayerEvents.LEAVE.register(player -> SkillsRecordClientCache.clear());
        ClientGuiEvents.RENDER_HUD.register(MinerCertificateStatus.INSTANCE::render);
        ClientGuiEvents.RENDER_HUD.register(UltimineNoticeHud.INSTANCE::render);
        ClientGuiEvents.RENDER_HUD.register(UndoPreviewClient.INSTANCE::render);
        ClientTickEvents.END.register(UndoPreviewClient.INSTANCE::tick);
        ClientTickEvents.END.register(UndoGrowthClient.INSTANCE::tick);
        ClientPlayerEvents.LEAVE.register(player -> {
            UndoPreviewClient.INSTANCE.close();
            UndoGrowthClient.INSTANCE.clear();
        });
        ClientPlayerEvents.LEAVE.register(player -> UltimineNoticeHud.INSTANCE.clear());
        ClientTickEvents.END.register((instance) -> {
            // Also ticks on the title screen, before there's a player (Trinkets throws on a null one).
            if (instance.player == null) return;
            FTBUltimineIntegration.keyEvent(instance.player);
            if (ServicePlatform.get().slotAPI().isModLoaded()
                    && !ServicePlatform.get().slotAPI().getSkillsRecordItem(instance.player).isEmpty()
                    && Minecraft.getInstance().screen == null
                    && KeyHandler.KEY_OPEN_SKILLS_RECORD.consumeClick()) {
                PayloadHandler.sendToServer(new SkillsRecordPayload.Open());
            }
        });
        ClientCommandEvents.REGISTER.register(SkillsRecordDebugCommand::register);
    }
}
