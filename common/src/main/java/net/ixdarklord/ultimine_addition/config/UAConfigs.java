package net.ixdarklord.ultimine_addition.config;

import net.ixdarklord.coolcatcore.api.config.ConfigColorScheme;
import net.ixdarklord.coolcatcore.api.config.ConfigTheme;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;

// CoolCatLib configs register themselves when their class loads; this loads them at the right time.
// Syncing the server config (on join and whenever it changes), the config screens and hot reloading all come from
// CoolCatLib.
public final class UAConfigs {
    // The accent of the mod's config screens: the warm orange of the logo, between its coral pickaxe and golden ore.
    private static final int ACCENT = 0xFFF0894A;

    // The Skills Record's settings popup: its panel is drawn like the book (frame, striped title band, dark screen) and
    // its controls in the same neutral greys, in both light and dark mode.
    private static final ConfigColorScheme SKILLS_RECORD_COLORS = ConfigColorScheme.DARK.toBuilder()
            .accent(ACCENT).backdrop(0xFF000000)
            .panel(0x30000000).panelBorder(0xFF2B2B2B).bar(0xC8303030).popup(0xFF404040)
            .rowHover(0x18FFFFFF).field(0xFF262626).fieldBorder(0xFF6E6E6E)
            .button(0xFF333333).buttonHover(0xFF444444).buttonDisabled(0xFF2A2A2A)
            .toggleOff(0xFF585858).knob(0xFFF0F0F0)
            .text(0xFFFFFFFF).textDim(0xFFBDBDBD).textMuted(0xFF8C8C8C)
            .build();
    public static final ConfigTheme SKILLS_RECORD_POPUP_THEME = ConfigTheme.builder()
            .colors(SKILLS_RECORD_COLORS)
            .lightColors(SKILLS_RECORD_COLORS)
            .popupSprite(FTBUltimineAddition.id("config/popup"))
            .build();

    private UAConfigs() {}

    // While the mod is constructed.
    public static void register() {
        ConfigTheme.setForMod(FTBUltimineAddition.MOD_ID, ConfigTheme.builder().colors(ConfigColorScheme.tinted(ACCENT)).build());
        UAServerConfig.CONFIG.id();
        PlaystyleModes.register();
    }

    // Client only.
    public static void registerClient() {
        UAClientConfig.CONFIG.id();
    }
}
