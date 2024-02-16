package net.ixdarklord.ultimine_addition.datagen.particle;

import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.data.DataGenerator;
import org.jetbrains.annotations.NotNull;

public class ParticleGenerator extends ParticleProvider {
    public ParticleGenerator(DataGenerator generator) {
        super(generator);
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

    @Override
    public @NotNull String getName() {
        return String.format("%s %s", UltimineAddition.MOD_NAME, super.getName());
    }
}
