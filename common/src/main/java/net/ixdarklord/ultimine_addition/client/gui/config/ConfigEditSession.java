package net.ixdarklord.ultimine_addition.client.gui.config;

import net.ixdarklord.coolcatcore.api.config.Config;
import net.ixdarklord.coolcatcore.api.config.ConfigValue;
import net.ixdarklord.coolcatcore.api.config.type.ValidationResult;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// The changes made in a config popup (from Glazed Menu, trimmed to a local client config): pending values, applied
// on save, and invalid input.
public final class ConfigEditSession {
    private final Config config;
    private final Map<ConfigValue<?>, Object> pending = new LinkedHashMap<>();
    private final Map<ConfigValue<?>, Component> errors = new HashMap<>();

    public ConfigEditSession(Config config) {
        this.config = config;
    }

    public Config config() {
        return this.config;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ConfigValue<T> value) {
        return (T) this.pending.getOrDefault(value, value.getStored());
    }

    public boolean isModified(ConfigValue<?> value) {
        return this.pending.containsKey(value) || this.errors.containsKey(value);
    }

    public <T> boolean isDefault(ConfigValue<T> value) {
        return value.type().equals(this.get(value), value.getDefault());
    }

    public Optional<Component> error(ConfigValue<?> value) {
        return Optional.ofNullable(this.errors.get(value));
    }

    public int modifiedCount() {
        return this.pending.size();
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public Collection<Component> errors() {
        return this.errors.values();
    }

    /** Whether the value's dependency is met by the pending values. */
    public boolean isActive(ConfigValue<?> value) {
        return value.dependency().map(dependency -> {
            ConfigValue<?> source = dependency.source();
            Object sourceValue = source.config() == this.config ? this.pending.getOrDefault(source, source.getStored()) : source.get();
            return dependency.isMetBy(sourceValue);
        }).orElse(true);
    }

    /** Sets a pending value, checked by the value's validators; invalid values are reported instead. */
    public <T> void set(ConfigValue<T> value, T newValue) {
        ValidationResult<T> result = value.validate(newValue);
        if (result.isError()) {
            this.errors.put(value, result.message().orElse(Component.empty()));
            return;
        }
        this.errors.remove(value);
        if (value.type().equals(newValue, value.getStored())) this.pending.remove(value);
        else this.pending.put(value, newValue);
    }

    public void resetToDefaults(Collection<ConfigValue<?>> values) {
        values.forEach(this::reset);
    }

    private <T> void reset(ConfigValue<T> value) {
        this.set(value, value.getDefault());
    }

    /** Applies the pending values and saves the file. */
    @SuppressWarnings("unchecked")
    public void save() {
        if (this.pending.isEmpty() || this.hasErrors()) return;
        this.pending.forEach((value, newValue) -> ((ConfigValue<Object>) value).set(newValue));
        this.pending.clear();
        this.config.save();
    }
}
