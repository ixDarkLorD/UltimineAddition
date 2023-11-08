package net.ixdarklord.ultimine_addition.core.fabric;

import dev.architectury.registry.menu.MenuRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.ixdarklord.ultimine_addition.client.gui.screen.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.handler.ItemPropertiesHandler;
import net.ixdarklord.ultimine_addition.core.ClientSetup;
import net.ixdarklord.ultimine_addition.core.Registration;

public class FabricClientSetup implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSetup.init();
        ItemPropertiesHandler.register();
        MenuRegistry.registerScreenFactory(Registration.SKILLS_RECORD_CONTAINER.get(), SkillsRecordScreen::new);
    }
}
