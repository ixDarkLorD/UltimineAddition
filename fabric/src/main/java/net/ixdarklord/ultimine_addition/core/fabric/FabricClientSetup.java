package net.ixdarklord.ultimine_addition.core.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.ClientSkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.particle.CelebrateParticle;
import net.ixdarklord.ultimine_addition.core.ClientSetup;
import net.ixdarklord.ultimine_addition.core.Registration;

public class FabricClientSetup implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSetup.init();
        ClientSetup.setup();
        this.initEvents();
    }

    private void initEvents() {
        ParticleFactoryRegistry.getInstance().register(Registration.CELEBRATE_PARTICLE.get(), CelebrateParticle.Provider::new);
        TooltipComponentCallback.EVENT.register(tooltipComponent -> {
            if (tooltipComponent instanceof SkillsRecordTooltip skillsRecordTooltip) {
                return new ClientSkillsRecordTooltip(skillsRecordTooltip);
            }
            if (tooltipComponent instanceof SkillsRecordTooltip.Option option) {
                return new ClientSkillsRecordTooltip.Option(option);
            }
            return null;
        });
    }
}
