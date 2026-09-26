package net.ixdarklord.ultimine_addition.core;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.gui.ClientTooltipComponentRegistry;
import dev.architectury.registry.client.gui.MenuScreenRegistry;
import net.ixdarklord.ultimine_addition.client.event.ClientEventHandler;
import net.ixdarklord.ultimine_addition.client.gui.screens.ShapeSelectorScreen;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.ClientSkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;

public final class ClientSetup {
    public static void init() {
        KeyHandler.register();
        ClientEventHandler.register();
        ClientLifecycleEvent.CLIENT_SETUP.register((instance) -> setup());
        ClientTooltipComponentRegistry.register(SkillsRecordTooltip.class, ClientSkillsRecordTooltip::new);
        ClientTooltipComponentRegistry.register(SkillsRecordTooltip.Option.class, ClientSkillsRecordTooltip.Option::new);
    }

    public static void setup() {
        if (Platform.isFabric()) {
            MenuScreenRegistry.registerScreenFactory(Registration.SKILLS_RECORD_CONTAINER.get(), SkillsRecordScreen::new);
            MenuScreenRegistry.registerScreenFactory(Registration.SHAPE_SELECTOR_CONTAINER.get(), ShapeSelectorScreen::new);
        }

    }
}
