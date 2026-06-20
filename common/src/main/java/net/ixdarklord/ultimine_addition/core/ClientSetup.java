package net.ixdarklord.ultimine_addition.core;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.client.gui.ClientTooltipComponentRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.ultimine_addition.client.event.ClientEventHandler;
import net.ixdarklord.ultimine_addition.client.gui.screens.ShapeSelectorScreen;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.ClientSkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.handler.ItemPropertiesHandler;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;
import net.ixdarklord.ultimine_addition.client.particle.CelebrateParticle;

@Environment(EnvType.CLIENT)
public final class ClientSetup {
   public static void init() {
      KeyHandler.register();
      ClientEventHandler.register();
      ClientLifecycleEvent.CLIENT_SETUP.register((instance) -> setup());
      ParticleProviderRegistry.register(Registration.CELEBRATE_PARTICLE, CelebrateParticle.Provider::new);
      ClientTooltipComponentRegistry.register(SkillsRecordTooltip.class, ClientSkillsRecordTooltip::new);
      ClientTooltipComponentRegistry.register(SkillsRecordTooltip.Option.class, ClientSkillsRecordTooltip.Option::new);
   }

   public static void setup() {
      ItemPropertiesHandler.register();
      MenuRegistry.registerScreenFactory(Registration.SKILLS_RECORD_CONTAINER.get(), SkillsRecordScreen::new);
      MenuRegistry.registerScreenFactory(Registration.SHAPE_SELECTOR_CONTAINER.get(), ShapeSelectorScreen::new);
   }
}
