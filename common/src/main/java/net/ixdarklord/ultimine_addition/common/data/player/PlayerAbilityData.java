package net.ixdarklord.ultimine_addition.common.data.player;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.coolcatcore.api.data.DataComponent;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PlayerAbilityData extends DataComponent<PlayerAbilityData> {
    // Also the key Fabric saved it under before it became an attachment (read by MixinLegacyPlayerData).
    public static final ResourceLocation DATA_ID = FTBUltimineAddition.id("ultimine_ability");
    public static final MapCodec<PlayerAbilityData> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.BOOL.fieldOf("is_unlocked").forGetter(PlayerAbilityData::getAbility),
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC.listOf()).optionalFieldOf("tool_shapes", Map.of()).forGetter(PlayerAbilityData::shapesAsLists),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("tool_certificate_tiers", Map.of()).forGetter(data -> data.certificateTiers),
            Codec.STRING.optionalFieldOf("generic_juice", "").forGetter(PlayerAbilityData::getGenericJuice),
            Timed.CODEC.optionalFieldOf("timed_challenge", Timed.NONE).forGetter(PlayerAbilityData::getTimed),
            Stats.CODEC.optionalFieldOf("stats", Stats.NONE).forGetter(PlayerAbilityData::getStats),
            Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()).optionalFieldOf("tool_perks", Map.of()).forGetter(PlayerAbilityData::perksAsLists),
            ResourceLocation.CODEC.listOf().optionalFieldOf("favorite_shapes", List.of()).forGetter(PlayerAbilityData::getFavoriteShapes),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("tool_prestige", Map.of()).forGetter(data -> data.prestige)
    ).apply(inst, PlayerAbilityData::new));
    public static final Codec<PlayerAbilityData> CODEC = MAP_CODEC.codec();
    // Written by hand: 1.21.1's StreamCodec.composite takes six fields at most.
    private static final StreamCodec<io.netty.buffer.ByteBuf, Map<String, List<ResourceLocation>>> SHAPES_STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()));
    private static final StreamCodec<io.netty.buffer.ByteBuf, Map<String, Integer>> COUNTS_STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT);
    private static final StreamCodec<io.netty.buffer.ByteBuf, Map<String, List<String>>> PERKS_STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()));
    private static final StreamCodec<io.netty.buffer.ByteBuf, List<ResourceLocation>> FAVORITES_STREAM_CODEC = ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list());
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerAbilityData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ByteBufCodecs.BOOL.encode(buf, data.getAbility());
                SHAPES_STREAM_CODEC.encode(buf, data.shapesAsLists());
                COUNTS_STREAM_CODEC.encode(buf, data.certificateTiers);
                ByteBufCodecs.STRING_UTF8.encode(buf, data.getGenericJuice());
                Timed.STREAM_CODEC.encode(buf, data.getTimed());
                Stats.STREAM_CODEC.encode(buf, data.getStats());
                PERKS_STREAM_CODEC.encode(buf, data.perksAsLists());
                FAVORITES_STREAM_CODEC.encode(buf, data.getFavoriteShapes());
                COUNTS_STREAM_CODEC.encode(buf, data.prestige);
            },
            buf -> new PlayerAbilityData(ByteBufCodecs.BOOL.decode(buf), SHAPES_STREAM_CODEC.decode(buf), COUNTS_STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf), Timed.STREAM_CODEC.decode(buf), Stats.STREAM_CODEC.decode(buf),
                    PERKS_STREAM_CODEC.decode(buf), FAVORITES_STREAM_CODEC.decode(buf), COUNTS_STREAM_CODEC.decode(buf)));

    // What the player has done with Ultimine, for /ultimine_addition stats and top (UltimineStats).
    public record Stats(long blocks, int operations, int largest, int undos, int dailies, int challenges) {
        public static final Stats NONE = new Stats(0L, 0, 0, 0, 0, 0);
        public static final Codec<Stats> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.LONG.optionalFieldOf("blocks", 0L).forGetter(Stats::blocks),
                Codec.INT.optionalFieldOf("operations", 0).forGetter(Stats::operations),
                Codec.INT.optionalFieldOf("largest", 0).forGetter(Stats::largest),
                Codec.INT.optionalFieldOf("undos", 0).forGetter(Stats::undos),
                Codec.INT.optionalFieldOf("dailies", 0).forGetter(Stats::dailies),
                Codec.INT.optionalFieldOf("challenges", 0).forGetter(Stats::challenges)
        ).apply(inst, Stats::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Stats> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG, Stats::blocks,
                ByteBufCodecs.VAR_INT, Stats::operations,
                ByteBufCodecs.VAR_INT, Stats::largest,
                ByteBufCodecs.VAR_INT, Stats::undos,
                ByteBufCodecs.VAR_INT, Stats::dailies,
                ByteBufCodecs.VAR_INT, Stats::challenges,
                Stats::new);
    }

    // The player's progress in a daily or weekly challenge (TimedChallenge): which period and challenge it is for,
    // so a new day's challenge starts from nothing.
    public record Timed(long period, String challenge, int progress, boolean done) {
        public static final Timed NONE = new Timed(0L, "", 0, false);
        public static final Codec<Timed> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.LONG.fieldOf("period").forGetter(Timed::period),
                Codec.STRING.fieldOf("challenge").forGetter(Timed::challenge),
                Codec.INT.fieldOf("progress").forGetter(Timed::progress),
                Codec.BOOL.fieldOf("done").forGetter(Timed::done)
        ).apply(inst, Timed::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Timed> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG, Timed::period,
                ByteBufCodecs.STRING_UTF8, Timed::challenge,
                ByteBufCodecs.VAR_INT, Timed::progress,
                ByteBufCodecs.BOOL, Timed::done,
                Timed::new);
    }

    private boolean isUnlocked;
    // Keyed by Mining Skill Card type id ("pickaxe", "axe", custom ones...): certificates unlock shapes per tool.
    private final Map<String, Set<ResourceLocation>> shapes = new HashMap<>();
    private final Map<String, Integer> certificateTiers = new HashMap<>();
    // The data pack card type the player's generic Mine-Go Juice is for (they share one effect), or "".
    private String genericJuice;
    private Timed timed;
    private Stats stats;
    // Card perks (CardPerk ids) learned per card type.
    private final Map<String, Set<String>> perks = new HashMap<>();
    // The shapes the player wants to cycle through; with none marked, all they can use.
    private final Set<ResourceLocation> favoriteShapes = new LinkedHashSet<>();
    // How many times the player took a card of each type from Mastered back to Unlearned.
    private final Map<String, Integer> prestige = new HashMap<>();

    private PlayerAbilityData(boolean isUnlocked, Map<String, List<ResourceLocation>> shapes, Map<String, Integer> certificateTiers, String genericJuice, Timed timed, Stats stats,
                              Map<String, List<String>> perks, List<ResourceLocation> favoriteShapes, Map<String, Integer> prestige) {
        super(DATA_ID, CODEC);
        this.isUnlocked = isUnlocked;
        shapes.forEach((tool, list) -> this.shapes.put(tool, new LinkedHashSet<>(list)));
        this.certificateTiers.putAll(certificateTiers);
        this.genericJuice = genericJuice;
        this.timed = timed;
        this.stats = stats;
        perks.forEach((tool, list) -> this.perks.put(tool, new LinkedHashSet<>(list)));
        this.favoriteShapes.addAll(favoriteShapes);
        this.prestige.putAll(prestige);
    }

    public static PlayerAbilityData create() {
        return new PlayerAbilityData(false, Map.of(), Map.of(), "", Timed.NONE, Stats.NONE, Map.of(), List.of(), Map.of());
    }

    private Map<String, List<ResourceLocation>> shapesAsLists() {
        Map<String, List<ResourceLocation>> map = new HashMap<>();
        this.shapes.forEach((tool, set) -> map.put(tool, List.copyOf(set)));
        return map;
    }

    private Map<String, List<String>> perksAsLists() {
        Map<String, List<String>> map = new HashMap<>();
        this.perks.forEach((tool, set) -> map.put(tool, List.copyOf(set)));
        return map;
    }

    public int getPrestige(String tool) {
        return this.prestige.getOrDefault(tool, 0);
    }

    public PlayerAbilityData addPrestige(String tool) {
        this.prestige.merge(tool, 1, Integer::sum);
        return this;
    }

    public List<ResourceLocation> getFavoriteShapes() {
        return List.copyOf(this.favoriteShapes);
    }

    /** Marks or unmarks a shape; returns whether it is a favourite now. */
    public boolean toggleFavoriteShape(ResourceLocation shape) {
        if (this.favoriteShapes.remove(shape)) return false;
        this.favoriteShapes.add(shape);
        return true;
    }

    public PlayerAbilityData clearFavoriteShapes() {
        this.favoriteShapes.clear();
        return this;
    }

    public Set<String> getPerks(String tool) {
        return this.perks.getOrDefault(tool, Set.of());
    }

    public Map<String, Set<String>> getAllPerks() {
        return this.perks;
    }

    public PlayerAbilityData learnPerk(String tool, String perk) {
        this.perks.computeIfAbsent(tool, t -> new LinkedHashSet<>()).add(perk);
        return this;
    }

    public PlayerAbilityData resetPerks() {
        this.perks.clear();
        return this;
    }

    public Stats getStats() {
        return this.stats;
    }

    public PlayerAbilityData setStats(Stats stats) {
        this.stats = stats;
        return this;
    }

    public Timed getTimed() {
        return this.timed;
    }

    public PlayerAbilityData setTimed(Timed timed) {
        this.timed = timed;
        return this;
    }

    public String getGenericJuice() {
        return this.genericJuice;
    }

    public PlayerAbilityData setGenericJuice(String type) {
        this.genericJuice = type;
        return this;
    }

    public boolean getAbility() {
        return isUnlocked;
    }

    public PlayerAbilityData setAbility(boolean unlocked) {
        isUnlocked = unlocked;
        return this;
    }

    public Set<ResourceLocation> getUnlockedShapes(String tool) {
        return this.shapes.getOrDefault(tool, Set.of());
    }

    // The highest Shape Certificate tier used for this tool; sets its Ultimine block limit without the Miner Certificate.
    public int getCertificateTier(String tool) {
        return this.certificateTiers.getOrDefault(tool, 0);
    }

    public PlayerAbilityData unlockShapes(String tool, Collection<ResourceLocation> shapes, int tier) {
        this.shapes.computeIfAbsent(tool, t -> new LinkedHashSet<>()).addAll(shapes);
        this.certificateTiers.merge(tool, tier, Math::max);
        return this;
    }

    public PlayerAbilityData resetShapes() {
        this.shapes.clear();
        this.certificateTiers.clear();
        return this;
    }
}
