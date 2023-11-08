package net.ixdarklord.ultimine_addition.datagen.particle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.datagen.helper.DataGeneratorHelper;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class ParticleProvider implements DataProvider {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final Map<ResourceLocation, List<ResourceLocation>> data = new TreeMap<>();
    private final Path path;

    protected abstract void addParticles();

    public ParticleProvider(DataGenerator generator) {
        this.path = generator.getOutputFolder();
    }

    @Override
    public void run(@NotNull HashCache cache) throws IOException {
        clear();
        addParticles();

        if (data.isEmpty()) throw new IllegalStateException("The structure data is empty!");
        generateParticle(cache);
    }

    private void generateParticle(HashCache cache) throws IOException {
        for (Map.Entry<ResourceLocation, List<ResourceLocation>> particle : data.entrySet()) {
            JsonObject JSONProperties = new JsonObject();
            JsonArray textures = new JsonArray();
            for (ResourceLocation texture : particle.getValue()) {
                textures.add(texture.toString());
            }
            JSONProperties.add("textures", textures);

            Path path = DataGeneratorHelper.createAssetsPath(this.path, "particles", particle.getKey());
            DataProvider.save(GSON, cache, JSONProperties, path);
        }
    }

    public void add(SimpleParticleType particle, ResourceLocation... texture) {
        ResourceLocation particleId = Registration.PARTICLE_TYPES.getRegistrar().getId(particle);
        if (data.containsKey(particleId)) {
            throw new IllegalStateException("Duplicate particle " + particleId);
        } else data.put(particleId, Arrays.stream(texture).toList());
    }

    private void clear() {
        data.clear();
    }

    public @NotNull String getName() {
        return "Particles";
    }
}
