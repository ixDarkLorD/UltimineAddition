package net.ixdarklord.ultimine_addition.client.event;

import dev.architectury.event.events.client.ClientTooltipEvent;
import net.ixdarklord.ultimine_addition.client.event.impl.ClientHudEvent;
import net.ixdarklord.ultimine_addition.client.gui.screen.ChallengesInfoPanel;
import net.ixdarklord.ultimine_addition.client.gui.screen.ItemTooltipEvents;

public class ScreenEvents {
    public static void init() {
        ClientTooltipEvent.ITEM.register(ItemTooltipEvents::init);
        ClientHudEvent.RENDER_PRE.register(ChallengesInfoPanel::render);
    }
}
