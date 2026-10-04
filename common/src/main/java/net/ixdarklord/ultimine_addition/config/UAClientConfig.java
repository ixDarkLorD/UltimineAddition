package net.ixdarklord.ultimine_addition.config;

import net.ixdarklord.coolcatcore.api.config.Config;
import net.ixdarklord.coolcatcore.api.config.ConfigBuilder;
import net.ixdarklord.coolcatcore.api.config.ConfigScope;
import net.ixdarklord.coolcatcore.api.config.ConfigValue;
import net.ixdarklord.ultimine_addition.client.gui.components.ChallengesPanel;
import net.ixdarklord.ultimine_addition.client.gui.screens.ShapeSelectorScreen;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;

// Client only: it names client classes, so nothing on the server may load it.
public final class UAClientConfig {
    private static final ConfigBuilder BUILDER = Config.builder(FTBUltimineAddition.MOD_ID, ConfigScope.CLIENT)
            .comment(FTBUltimineAddition.MOD_NAME + " client settings.");

    // Also opened on its own from the Skills Record's configuration button (a category popup).
    public static final String SKILLS_RECORD_CATEGORY = "skills_record";

    static { BUILDER.push(SKILLS_RECORD_CATEGORY); }
    public static final ConfigValue<Boolean> ANIMATIONS_MODE = BUILDER.bool("animations", true)
            .comment("Animations in the Skills Record.").build();
    public static final ConfigValue<Boolean> TEXT_SCREEN_SHADOW = BUILDER.bool("text_shadow", true)
            .comment("Drop shadow on the Skills Record's card viewer text.").build();
    public static final ConfigValue<ChallengesPanel.Align> CHALLENGES_PANEL_ALIGNMENT = BUILDER.enumValue("challenges_panel_alignment", ChallengesPanel.Align.LEFT)
            .comment("Where pinned challenges are shown on the HUD.").build();
    static { BUILDER.pop(); }

    static { BUILDER.push("visuals"); }
    public static final ConfigValue<ShapeSelectorScreen.Filter> SHAPE_SELECTOR_FILTER = BUILDER.enumValue("shape_selector_filter", ShapeSelectorScreen.Filter.ALL)
            .comment("Shapes listed by the Shape Selector: all, or only the ones that aren't blacklisted.").build();
    static { BUILDER.pop(); }

    static { BUILDER.push("undo"); }
    public static final ConfigValue<Boolean> CONFIRM_MISSING_ITEMS = BUILDER.bool("confirm_missing_items", true)
            .comment("Asks before an undo with missing items, which only puts back the blocks you can pay for.").build();
    static { BUILDER.pop(); }

    static { BUILDER.push("debug"); }
    public static final ConfigValue<Boolean> SR_EDIT_MODE = BUILDER.bool("skills_record_edit_mode", false)
            .comment("Lets operators edit challenge progress from the Skills Record, and shows debug info in it.").build();
    static { BUILDER.pop(); }

    public static final Config CONFIG = BUILDER.build();

    private UAClientConfig() {}
}
