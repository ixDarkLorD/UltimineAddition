package net.ixdarklord.ultimine_addition.common.data.challenge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Where a challenge counts (a challenge file's "conditions"): in these dimensions, in these biomes (ids, or tags
// starting with #), between these heights. Anything left out doesn't matter.
//
//   "conditions": { "dimensions": ["minecraft:the_nether"], "biomes": ["#minecraft:is_forest"], "min_y": -64, "max_y": 16 }
public record ChallengeConditions(List<Identifier> dimensions, List<String> biomes, Optional<Integer> minY, Optional<Integer> maxY) {
    public static final ChallengeConditions NONE = new ChallengeConditions(List.of(), List.of(), Optional.empty(), Optional.empty());
    public static final Codec<ChallengeConditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.listOf().optionalFieldOf("dimensions", List.of()).forGetter(ChallengeConditions::dimensions),
            Codec.STRING.listOf().optionalFieldOf("biomes", List.of()).forGetter(ChallengeConditions::biomes),
            Codec.INT.optionalFieldOf("min_y").forGetter(ChallengeConditions::minY),
            Codec.INT.optionalFieldOf("max_y").forGetter(ChallengeConditions::maxY)
    ).apply(instance, ChallengeConditions::new));

    public boolean isEmpty() {
        return this.dimensions.isEmpty() && this.biomes.isEmpty() && this.minY.isEmpty() && this.maxY.isEmpty();
    }

    public boolean test(Level level, BlockPos pos) {
        if (!this.dimensions.isEmpty() && !this.dimensions.contains(dimensionId(level))) return false;
        if (this.minY.isPresent() && pos.getY() < this.minY.get()) return false;
        if (this.maxY.isPresent() && pos.getY() > this.maxY.get()) return false;
        if (this.biomes.isEmpty()) return true;
        Holder<Biome> biome = level.getBiome(pos);
        for (String entry : this.biomes) {
            Identifier id = Identifier.tryParse(entry.startsWith("#") ? entry.substring(1) : entry);
            if (id == null) continue;
            if (entry.startsWith("#") ? biome.is(TagKey.create(Registries.BIOME, id)) : biome.is(ResourceKey.create(Registries.BIOME, id))) return true;
        }
        return false;
    }

    private static Identifier dimensionId(Level level) {
        return level.dimension().identifier();
    }

    /** The conditions in words, one per line, for the challenge's description. */
    public List<Component> describe() {
        List<Component> lines = new ArrayList<>();
        if (!this.dimensions.isEmpty()) {
            lines.add(Component.translatable("challenge.ultimine_addition.condition.dimension", joined(this.dimensions.stream()
                    .map(id -> (Component) Component.translatableWithFallback("dimension." + id.getNamespace() + "." + id.getPath(), pretty(id.getPath()))).toList())));
        }
        if (!this.biomes.isEmpty()) {
            lines.add(Component.translatable("challenge.ultimine_addition.condition.biome", joined(this.biomes.stream().map(entry -> {
                if (entry.startsWith("#")) return (Component) Component.literal(pretty(entry.substring(entry.indexOf(':') + 1)));
                Identifier id = Identifier.tryParse(entry);
                return id == null ? Component.literal(entry) : Component.translatableWithFallback("biome." + id.getNamespace() + "." + id.getPath(), pretty(id.getPath()));
            }).toList())));
        }
        if (this.minY.isPresent() && this.maxY.isPresent()) {
            lines.add(Component.translatable("challenge.ultimine_addition.condition.height", this.minY.get(), this.maxY.get()));
        } else if (this.minY.isPresent()) {
            lines.add(Component.translatable("challenge.ultimine_addition.condition.above", this.minY.get()));
        } else if (this.maxY.isPresent()) {
            lines.add(Component.translatable("challenge.ultimine_addition.condition.below", this.maxY.get()));
        }
        return lines.stream().map(line -> (Component) line.copy().withStyle(ChatFormatting.AQUA)).toList();
    }

    private static Component joined(List<Component> parts) {
        var out = Component.empty();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) out.append(", ");
            out.append(parts.get(i));
        }
        return out;
    }

    // "is_forest" -> "Is Forest", "the_nether" -> "The Nether"
    private static String pretty(String path) {
        StringBuilder out = new StringBuilder();
        for (String word : path.substring(path.lastIndexOf('/') + 1).split("_")) {
            if (word.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(this.dimensions, (b, id) -> b.writeUtf(id.toString()));
        buf.writeCollection(this.biomes, FriendlyByteBuf::writeUtf);
        buf.writeBoolean(this.minY.isPresent());
        this.minY.ifPresent(buf::writeInt);
        buf.writeBoolean(this.maxY.isPresent());
        this.maxY.ifPresent(buf::writeInt);
    }

    public static ChallengeConditions read(FriendlyByteBuf buf) {
        List<Identifier> dimensions = buf.readList(b -> Identifier.parse(b.readUtf()));
        List<String> biomes = buf.readList(FriendlyByteBuf::readUtf);
        Optional<Integer> minY = buf.readBoolean() ? Optional.of(buf.readInt()) : Optional.empty();
        Optional<Integer> maxY = buf.readBoolean() ? Optional.of(buf.readInt()) : Optional.empty();
        return new ChallengeConditions(dimensions, biomes, minY, maxY);
    }
}
