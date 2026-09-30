package net.ixdarklord.ultimine_addition.core.fabric;

import net.ixdarklord.ultimine_addition.client.undo.UndoBlockEntities;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.ixdarklord.ultimine_addition.client.undo.UndoGhostRenderer;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.ixdarklord.ultimine_addition.client.renderer.item.UAItemModels;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.ixdarklord.ultimine_addition.client.particle.CelebrateParticle;
import net.ixdarklord.ultimine_addition.core.Registration;

// Fabric-only client setup. The mod's client side is constructed by CoolCatLib: Core from the "coolcatcore:client"
// entrypoint (UltimineAdditionClientConstructor).
public class FabricClientSetup implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        UAItemModels.registerProperties(FabricModelPredicateProviderRegistry::register);
        ParticleFactoryRegistry.getInstance().register(Registration.CELEBRATE_PARTICLE.get(), CelebrateParticle.Provider::new);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> UndoGhostRenderer.render(context.matrixStack()));
        // Just before the level's block entities, which the level renderer draws together with these.
        WorldRenderEvents.AFTER_ENTITIES.register(context -> UndoBlockEntities.render(context.matrixStack()));
    }
}
