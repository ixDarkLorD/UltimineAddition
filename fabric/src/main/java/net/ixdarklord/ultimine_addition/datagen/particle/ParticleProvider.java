package net.ixdarklord.ultimine_addition.datagen.particle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public abstract class ParticleProvider implements DataProvider {
    private final DataGenerator.PathProvider pathProvider;

    private final Map<String, List<ResourceLocation>> data = new TreeMap<>();
    protected abstract void addParticles();

    public ParticleProvider(DataGenerator generator) {
        this.pathProvider = generator.createPathProvider(DataGenerator.Target.RESOURCE_PACK, "particles");
    }

    public void run(@NotNull CachedOutput cache) throws IOException {
        clear();
        addParticles();

        if (data.isEmpty()) throw new IllegalStateException("The structure data is empty!");
        generateParticle(cache);
    }

    private void generateParticle(CachedOutput cache) throws IOException {
        for (var particle : data.entrySet()) {
            JsonObject JSONProperties = new JsonObject();
            JsonArray array = new JsonArray();
            for (var entry : particle.getValue()) {
                array.add(entry.toString());
            }

            JSONProperties.add("textures", array);
            DataProvider.saveStable(cache, JSONProperties, pathProvider.file(UltimineAddition.getLocation(particle.getKey()), "json"));
        }
    }

    public void add(SimpleParticleType particle, ResourceLocation... texture) {
        var particleName = Objects.requireNonNull(Registration.PARTICLE_TYPES.getRegistrar().getId(particle)).getPath();
        if (data.containsKey(particleName)) {
            throw new IllegalStateException("Duplicate particle " + particleName);
        } else data.put(particleName, Arrays.stream(texture).toList());
    }

    private void clear() {
        data.clear();
    }

    public @NotNull String getName() {
        return "Particles";
    }
}
