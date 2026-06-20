package net.ixdarklord.ultimine_addition.client.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.*;
import net.ixdarklord.ultimine_addition.client.commands.SkillsRecordDebugCommand;
import net.ixdarklord.ultimine_addition.client.gui.hud.ChallengesPanelManager;
import net.ixdarklord.ultimine_addition.client.gui.hud.MinerCertificateStatus;
import net.ixdarklord.ultimine_addition.client.handler.ClientHandler;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packets.SkillsRecordPacket;

public class ClientEventHandler {
    public static void register() {
        ClientTooltipEvent.ITEM.register(ItemTooltipEvents::init);
        ClientGuiEvent.RENDER_HUD.register(ChallengesPanelManager.INSTANCE::render);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(ChallengesPanelManager.INSTANCE::cleanup);
        ClientGuiEvent.RENDER_HUD.register(MinerCertificateStatus.INSTANCE::render);
        ClientRawInputEvent.KEY_PRESSED.register((instance, keyCode, scanCode, action, modifiers) -> {
            FTBUltimineIntegration.keyEvent(instance.player);
            if (ServicePlatform.get().slotAPI().isModLoaded() && !ServicePlatform.get().slotAPI().getSkillsRecordItem(ClientHandler.getPlayer()).isEmpty() && instance.screen == null && KeyHandler.KEY_OPEN_SKILLS_RECORD.isDown()) {
                PacketHandler.sendToServer(new SkillsRecordPacket.Open());
                return EventResult.interruptTrue();
            } else {
                return EventResult.pass();
            }
        });
        ClientCommandRegistrationEvent.EVENT.register(SkillsRecordDebugCommand::register);
    }
}
