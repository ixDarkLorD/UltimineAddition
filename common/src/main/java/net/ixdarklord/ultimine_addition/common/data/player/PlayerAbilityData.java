package net.ixdarklord.ultimine_addition.common.data.player;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.coolcatcore.api.data.DataComponent;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PlayerAbilityData extends DataComponent<PlayerAbilityData> {
    // Also the key Fabric saved it under before it became an attachment (read by MixinLegacyPlayerData).
    public static final Identifier DATA_ID = FTBUltimineAddition.id("ultimine_ability");
    public static final MapCodec<PlayerAbilityData> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.BOOL.fieldOf("is_unlocked").forGetter(PlayerAbilityData::getAbility),
            Codec.unboundedMap(Codec.STRING, Identifier.CODEC.listOf()).optionalFieldOf("tool_shapes", Map.of()).forGetter(PlayerAbilityData::shapesAsLists),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("tool_certificate_tiers", Map.of()).forGetter(data -> data.certificateTiers),
            Codec.STRING.optionalFieldOf("generic_juice", "").forGetter(PlayerAbilityData::getGenericJuice)
    ).apply(inst, PlayerAbilityData::new));
    public static final Codec<PlayerAbilityData> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerAbilityData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PlayerAbilityData::getAbility,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, Identifier.STREAM_CODEC.apply(ByteBufCodecs.list())), PlayerAbilityData::shapesAsLists,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT), data -> data.certificateTiers,
            ByteBufCodecs.STRING_UTF8, PlayerAbilityData::getGenericJuice,
            PlayerAbilityData::new);

    private boolean isUnlocked;
    // Keyed by Mining Skill Card type id ("pickaxe", "axe", custom ones...): certificates unlock shapes per tool.
    private final Map<String, Set<Identifier>> shapes = new HashMap<>();
    private final Map<String, Integer> certificateTiers = new HashMap<>();
    // The data pack card type the player's generic Mine-Go Juice is for (they share one effect), or "".
    private String genericJuice;

    private PlayerAbilityData(boolean isUnlocked, Map<String, List<Identifier>> shapes, Map<String, Integer> certificateTiers, String genericJuice) {
        super(DATA_ID, CODEC);
        this.isUnlocked = isUnlocked;
        shapes.forEach((tool, list) -> this.shapes.put(tool, new LinkedHashSet<>(list)));
        this.certificateTiers.putAll(certificateTiers);
        this.genericJuice = genericJuice;
    }

    public static PlayerAbilityData create() {
        return new PlayerAbilityData(false, Map.of(), Map.of(), "");
    }

    private Map<String, List<Identifier>> shapesAsLists() {
        Map<String, List<Identifier>> map = new HashMap<>();
        this.shapes.forEach((tool, set) -> map.put(tool, List.copyOf(set)));
        return map;
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

    public Set<Identifier> getUnlockedShapes(String tool) {
        return this.shapes.getOrDefault(tool, Set.of());
    }

    // The highest Shape Certificate tier used for this tool; sets its Ultimine block limit without the Miner Certificate.
    public int getCertificateTier(String tool) {
        return this.certificateTiers.getOrDefault(tool, 0);
    }

    public PlayerAbilityData unlockShapes(String tool, Collection<Identifier> shapes, int tier) {
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
