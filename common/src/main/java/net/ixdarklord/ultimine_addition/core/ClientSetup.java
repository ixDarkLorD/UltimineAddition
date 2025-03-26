package net.ixdarklord.ultimine_addition.core;

import dev.architectury.registry.menu.MenuRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.ultimine_addition.client.event.ClientEventHandler;
import net.ixdarklord.ultimine_addition.client.gui.screens.ShapeSelectorScreen;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.handler.ItemPropertiesHandler;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;

@Environment(EnvType.CLIENT)
public class ClientSetup {
    public static void init() {
        KeyHandler.register();
        ClientEventHandler.register();
    }

    public static void setup() {
        ItemPropertiesHandler.register();
        MenuRegistry.registerScreenFactory(Registration.SKILLS_RECORD_CONTAINER.get(), SkillsRecordScreen::new);
        MenuRegistry.registerScreenFactory(Registration.SHAPE_SELECTOR_CONTAINER.get(), ShapeSelectorScreen::new);
    }
}
