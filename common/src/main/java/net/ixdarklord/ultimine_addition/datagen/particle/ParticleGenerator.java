package net.ixdarklord.ultimine_addition.datagen.particle;

import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.data.PackOutput;

public class ParticleGenerator extends ParticleProvider {
    public ParticleGenerator(PackOutput output) {
        super(output);
    }

    @Override
    protected void addParticles() {
        add(Registration.CELEBRATE_PARTICLE.get(),
                FTBUltimineAddition.getLocation("celebrate"),
                FTBUltimineAddition.getLocation("diamond_pickaxe"),
                FTBUltimineAddition.getLocation("diamond_axe"),
                FTBUltimineAddition.getLocation("celebrate"),
                FTBUltimineAddition.getLocation("diamond_shovel"),
                FTBUltimineAddition.getLocation("diamond_hoe"),
                FTBUltimineAddition.getLocation("celebrate"));
    }
}
