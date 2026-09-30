package net.ixdarklord.ultimine_addition.core;

import net.ixdarklord.coolcatcore.api.client.registry.MenuScreenRegistry;
import net.ixdarklord.coolcatcore.api.client.registry.TooltipComponentRegistry;
import net.ixdarklord.coolcatcore.api.core.ClientModConstructor;
import net.ixdarklord.ultimine_addition.client.event.ClientEventHandler;
import net.ixdarklord.ultimine_addition.client.gui.screens.ShapeSelectorScreen;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.ClientSkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;
import net.ixdarklord.ultimine_addition.config.UAConfigs;
import net.minecraft.client.Minecraft;

// The mod's client entry point, constructed by CoolCatLib: Core only on a client: the "coolcatcore:client" entrypoint on
// Fabric, NeoForgeSetup on NeoForge. Loader-only client setup (item models, particles, render hooks) stays in
// FabricClientSetup / NeoForgeClientSetup.
public final class UltimineAdditionClientConstructor implements ClientModConstructor {
    @Override
    public void onConstructMod() {
        UAConfigs.registerClient();
        KeyHandler.register();
        FTBUltimineIntegration.clientPlayer = () -> Minecraft.getInstance().isSameThread() ? Minecraft.getInstance().player : null;
        ClientEventHandler.register();
        TooltipComponentRegistry.register(SkillsRecordTooltip.class, ClientSkillsRecordTooltip::new);
        MenuScreenRegistry.register(Registration.SKILLS_RECORD_CONTAINER, SkillsRecordScreen::new);
        MenuScreenRegistry.register(Registration.SHAPE_SELECTOR_CONTAINER, ShapeSelectorScreen::new);
    }
}
