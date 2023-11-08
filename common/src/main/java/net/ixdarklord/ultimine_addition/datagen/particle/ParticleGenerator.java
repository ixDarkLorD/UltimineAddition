package net.ixdarklord.ultimine_addition.datagen.particle;

import net.ixdarklord.ultimine_addition.core.Constants;
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
                Constants.getLocation("celebrate"),
                Constants.getLocation("diamond_pickaxe"),
                Constants.getLocation("diamond_axe"),
                Constants.getLocation("celebrate"),
                Constants.getLocation("diamond_shovel"),
                Constants.getLocation("diamond_hoe"),
                Constants.getLocation("celebrate"));
    }

    @Override
    public @NotNull String getName() {
        return String.format("%s %s", Constants.MOD_NAME, super.getName());
    }
}
