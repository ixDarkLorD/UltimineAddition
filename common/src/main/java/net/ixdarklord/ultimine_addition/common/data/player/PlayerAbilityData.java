package net.ixdarklord.ultimine_addition.common.data.player;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.coolcatcore.api.data.DataComponent;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PlayerAbilityData extends DataComponent<PlayerAbilityData> {
    // Also the key the earlier 1.20.1 releases saved it under before it became an attachment (Fabric: in the player's
    // data; Forge: in the "ultimine_addition:properties" capability), read by each loader's MixinLegacyPlayerData.
    public static final ResourceLocation DATA_ID = FTBUltimineAddition.id("ftb_ultimine_ability");
    public static final MapCodec<PlayerAbilityData> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.BOOL.fieldOf("is_unlocked").forGetter(PlayerAbilityData::getAbility),
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC.listOf()).optionalFieldOf("tool_shapes", Map.of()).forGetter(PlayerAbilityData::shapesAsLists),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("tool_certificate_tiers", Map.of()).forGetter(data -> data.certificateTiers)
    ).apply(inst, PlayerAbilityData::new));
    public static final Codec<PlayerAbilityData> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<FriendlyByteBuf, PlayerAbilityData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PlayerAbilityData::getAbility,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.RESOURCE_LOCATION.apply(ByteBufCodecs.list())), PlayerAbilityData::shapesAsLists,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT), data -> data.certificateTiers,
            PlayerAbilityData::new);

    private boolean isUnlocked;
    // Keyed by Mining Skill Card type id ("pickaxe", "axe", custom ones...): certificates unlock shapes per tool.
    private final Map<String, Set<ResourceLocation>> shapes = new HashMap<>();
    private final Map<String, Integer> certificateTiers = new HashMap<>();

    private PlayerAbilityData(boolean isUnlocked, Map<String, List<ResourceLocation>> shapes, Map<String, Integer> certificateTiers) {
        super(DATA_ID, CODEC);
        this.isUnlocked = isUnlocked;
        shapes.forEach((tool, list) -> this.shapes.put(tool, new LinkedHashSet<>(list)));
        this.certificateTiers.putAll(certificateTiers);
    }

    public static PlayerAbilityData create() {
        return new PlayerAbilityData(false, Map.of(), Map.of());
    }

    private Map<String, List<ResourceLocation>> shapesAsLists() {
        Map<String, List<ResourceLocation>> map = new HashMap<>();
        this.shapes.forEach((tool, set) -> map.put(tool, List.copyOf(set)));
        return map;
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
