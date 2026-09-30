package net.ixdarklord.ultimine_addition.config;

import net.ixdarklord.coolcatcore.api.config.ConfigBuilder;
import net.ixdarklord.coolcatcore.api.config.ConfigValue;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem.Tier;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

// One int per card tier, as a config group ("novice", "apprentice", ...).
public final class TierValues {
    private final Map<Tier, ConfigValue<Integer>> values = new EnumMap<>(Tier.class);
    private final Map<Tier, Integer> defaults;

    public TierValues(ConfigBuilder builder, String key, Map<Tier, Integer> defaults, int min, int max, String... comment) {
        this.defaults = Collections.unmodifiableMap(new EnumMap<>(defaults));
        builder.push(key, comment);
        for (Tier tier : Tier.values()) {
            Integer value = defaults.get(tier);
            if (value != null) this.values.put(tier, builder.intValue(tier.name().toLowerCase(), value).range(min, max).build());
        }
        builder.pop();
    }

    public int getValue(Tier tier) {
        ConfigValue<Integer> value = this.values.get(tier);
        if (value == null) throw new IllegalArgumentException("No value for tier " + tier);
        return value.get();
    }

    public int getDefaultValue(Tier tier) {
        Integer value = this.defaults.get(tier);
        if (value == null) throw new IllegalArgumentException("No value for tier " + tier);
        return value;
    }

    public Map<Tier, Integer> getDefaultMapValue() {
        return this.defaults;
    }
}
