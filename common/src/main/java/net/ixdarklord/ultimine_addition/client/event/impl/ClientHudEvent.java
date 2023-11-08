package net.ixdarklord.ultimine_addition.client.event.impl;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;

public interface ClientHudEvent {
    Event<RenderHud> RENDER_PRE = EventFactory.createLoop();

    @Environment(EnvType.CLIENT)
    interface RenderHud {
        void renderHud(GuiGraphics guiGraphics, float tickDelta);
    }
}
