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
                FTBUltimineAddition.rl("celebrate"),
                FTBUltimineAddition.rl("diamond_pickaxe"),
                FTBUltimineAddition.rl("diamond_axe"),
                FTBUltimineAddition.rl("celebrate"),
                FTBUltimineAddition.rl("diamond_shovel"),
                FTBUltimineAddition.rl("diamond_hoe"),
                FTBUltimineAddition.rl("celebrate"));
    }
}
