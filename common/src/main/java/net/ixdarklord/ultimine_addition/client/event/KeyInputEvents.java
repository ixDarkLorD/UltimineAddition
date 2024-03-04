package net.ixdarklord.ultimine_addition.client.event;

import dev.architectury.event.events.client.ClientTickEvent;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.SkillsRecordPacket;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;

public class KeyInputEvents {
    public static void init() {
        ClientTickEvent.CLIENT_POST.register((instance) -> {
            FTBUltimineIntegration.keyEvent(instance.player);
            handleOpenSkillsRecordKey();
        });
    }

    private static void handleOpenSkillsRecordKey() {
        if (!ServicePlatform.SlotAPI.isModLoaded()) return;
        if (KeyHandler.KEY_OPEN_SKILLS_RECORD.consumeClick()) {
            PacketHandler.sendToServer(new SkillsRecordPacket.Open());
        }
    }
}
