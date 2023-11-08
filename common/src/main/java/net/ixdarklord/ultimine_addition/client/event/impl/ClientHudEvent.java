package net.ixdarklord.ultimine_addition.client.event.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface ClientHudEvent {
    Event<RenderHud> RENDER_PRE = EventFactory.createLoop();

    @Environment(EnvType.CLIENT)
    interface RenderHud {
        void renderHud(PoseStack matrices, float tickDelta);
    }
}
