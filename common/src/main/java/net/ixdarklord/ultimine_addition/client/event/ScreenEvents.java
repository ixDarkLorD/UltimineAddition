package net.ixdarklord.ultimine_addition.client.event;

import dev.architectury.event.events.client.ClientTooltipEvent;
import net.ixdarklord.ultimine_addition.client.gui.screen.ItemTooltipAddition;

public class ScreenEvents {
    public static void init() {
        ClientTooltipEvent.ITEM.register(ItemTooltipAddition::init);
    }
}
