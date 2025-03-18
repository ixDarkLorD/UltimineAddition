package net.ixdarklord.ultimine_addition.config;

import net.minecraft.util.StringRepresentable;
import net.neoforged.fml.config.IConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record ConfigInfo(String modId, Type type, IConfigSpec spec, String fileName) {
    public ConfigInfo(String modId, String configType, IConfigSpec spec, String fileName) {
        this(modId, Type.typeFromString(configType), spec, fileName);
    }

    public enum Type implements StringRepresentable {
        COMMON,
        CLIENT,
        SERVER,
        STARTUP;

        Type() {}

        public static Type typeFromString(String name) {
            for (Type type : values()) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid type: " + name);
        }

        public String extension() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public @NotNull String getSerializedName() {
            return this.extension();
        }
    }

}
