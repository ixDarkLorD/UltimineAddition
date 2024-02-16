package net.ixdarklord.ultimine_addition.datagen.particle;

import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.data.PackOutput;

public class ParticleGenerator extends ParticleProvider {
    public ParticleGenerator(PackOutput output) {
        super(output);
    }

    @Override
    protected void addParticles() {
        add(Registration.CELEBRATE_PARTICLE.get(),
                UltimineAddition.getLocation("celebrate"),
                UltimineAddition.getLocation("diamond_pickaxe"),
                UltimineAddition.getLocation("diamond_axe"),
                UltimineAddition.getLocation("celebrate"),
                UltimineAddition.getLocation("diamond_shovel"),
                UltimineAddition.getLocation("diamond_hoe"),
                UltimineAddition.getLocation("celebrate"));
    }
}
