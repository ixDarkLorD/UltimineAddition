package net.ixdarklord.ultimine_addition.config;

import net.ixdarklord.coolcatcore.api.config.type.EnumType;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum PlaystyleMode implements EnumType.Displayable {
    MODERN,
    LEGACY;

    @Override
    public Component displayName() {
        return Component.translatable("ultimine_addition.playstyle_mode." + this.name().toLowerCase(Locale.ROOT));
    }

    // Shown when choosing the mode from the config screen's list.
    @Override
    public Component description() {
        return Component.translatable("ultimine_addition.playstyle_mode." + this.name().toLowerCase(Locale.ROOT) + ".desc");
    }
}
