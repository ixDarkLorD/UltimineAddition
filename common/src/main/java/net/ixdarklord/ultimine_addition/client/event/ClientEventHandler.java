package net.ixdarklord.ultimine_addition.client.event;

import dev.architectury.event.events.client.*;
import net.ixdarklord.ultimine_addition.client.commands.SkillsRecordDebugCommand;
import net.ixdarklord.ultimine_addition.client.gui.hud.MinerCertificateStatus;
import net.ixdarklord.ultimine_addition.client.gui.hud.ChallengesPanelManager;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.SkillsRecordPayload;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.client.Minecraft;

public final class ClientEventHandler {
    public static void register() {
        ClientTooltipEvent.ITEM.register(ItemTooltipEvents::init);
        ClientGuiEvent.RENDER_HUD.register(ChallengesPanelManager.INSTANCE::render);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(ChallengesPanelManager.INSTANCE::cleanup);
        ClientGuiEvent.RENDER_HUD.register(MinerCertificateStatus.INSTANCE::render);
        ClientTickEvent.CLIENT_POST.register((instance) -> {
            FTBUltimineIntegration.keyEvent(instance.player);
            if (ServicePlatform.get().slotAPI().isModLoaded()
                    && !ServicePlatform.get().slotAPI().getSkillsRecordItem(instance.player).isEmpty()
                    && Minecraft.getInstance().screen == null
                    && KeyHandler.KEY_OPEN_SKILLS_RECORD.consumeClick()) {
                PayloadHandler.sendToServer(new SkillsRecordPayload.Open());
            }
        });
        ClientCommandRegistrationEvent.EVENT.register(SkillsRecordDebugCommand::register);
    }
}
