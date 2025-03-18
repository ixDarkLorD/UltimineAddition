package net.ixdarklord.ultimine_addition.config;

import com.google.common.collect.ImmutableList;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.ixdarklord.ultimine_addition.client.gui.screens.ChallengesInfoPanel;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.mixin.ShapeRegistryAccessor;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConfigHandler {
    public static void register() {
        ServicePlatform.get().registerConfig();
    }

    public static class CLIENT {
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
        public static final ModConfigSpec.EnumValue<SkillsRecordScreen.BGColor> BACKGROUND_COLOR;
        public static final ModConfigSpec.ConfigValue<Boolean> ANIMATIONS_MODE;
        public static final ModConfigSpec.IntValue PROGRESS_BAR;
        public static final ModConfigSpec.EnumValue<ChallengesInfoPanel.Panel.Position> CHALLENGES_PANEL_POSITION;
        public static final ModConfigSpec.BooleanValue SR_EDIT_MODE;
        public static final ModConfigSpec.BooleanValue MSC_RENDERER;
        public static final ModConfigSpec.BooleanValue TEXT_SCREEN_SHADOW;

        static {
            BUILDER.push("Settings");
            SR_EDIT_MODE = BUILDER
                    .comment("Enables or disables Edit Mode in the Skills Record screen.",
                            "Edit Mode allows customization of the Skills Record interface.")
                    .define("sr_edit_mode", false);

            BUILDER.push("Visuals");
            TEXT_SCREEN_SHADOW = BUILDER
                    .comment("Toggles the drop shadow effect on text in the Skills Record screen.",
                            "When enabled, text will have a subtle shadow for better readability.")
                    .define("text_screen_shadow", true);

            BACKGROUND_COLOR = BUILDER
                    .comment("Sets the background color of the Skills Record GUI.",
                            "Choose from predefined color options to customize the interface.")
                    .defineEnum("background_color", SkillsRecordScreen.BGColor.DEFAULT);

            ANIMATIONS_MODE = BUILDER
                    .comment("Enables or disables animations in the Skills Record GUI.",
                            "Animations provide visual feedback for interactions.")
                    .define("animations_mode", true);

            PROGRESS_BAR = BUILDER
                    .comment("Controls the visibility mode of the progress bar:",
                            "0: Always visible.",
                            "1: Visible only when holding its keybind.",
                            "2: Disabled entirely.")
                    .defineInRange("progress_bar_mode", 0, 0, 2);

            CHALLENGES_PANEL_POSITION = BUILDER
                    .comment("Determines the position of the Challenges panel on the screen.",
                            "Choose from predefined positions (e.g., LEFT, RIGHT).")
                    .defineEnum("challenges_panel_pos", ChallengesInfoPanel.Panel.Position.LEFT);

            MSC_RENDERER = BUILDER
                    .comment("Enables or disables the Mining Skill Card Renderer.",
                            "Note: This feature is a work in progress (WIP) and not recommended for regular use.")
                    .define("msc_renderer", false);
            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }

    public static class COMMON {
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
        public static final ModConfigSpec.EnumValue<PlaystyleMode> PLAYSTYLE_MODE;

        static {
            BUILDER.push("Playstyle");
            PLAYSTYLE_MODE = BUILDER.worldRestart()
                    .comment("Defines the playstyle mode for the mod:",
                            "%s: Modern playstyle with new features and challenges.".formatted(PlaystyleMode.MODERN.name()),
                            "%s [WIP]: Single-tier Mining Skill Card that upgrades to Mastered upon challenge completion.".formatted(PlaystyleMode.ONE_TIER_ONLY.name()),
                            "%s: Restores mechanics from v0.1.0 (only one miner certificate and one challenge).".formatted(PlaystyleMode.LEGACY.name()))
                    .defineEnum("playstyle_mode", PlaystyleMode.MODERN);
            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }

    public static class SERVER {
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

        // General Settings
        public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_SHAPES;
        public static final ModConfigSpec.BooleanValue IS_PLACED_BY_ENTITY_CONDITION;
        public static final ModConfigSpec.IntValue CARD_VALIDATOR;

        // Gameplay Settings
        public static final ModConfigSpec.DoubleValue PAPER_CONSUMPTION_RATE;
        public static final ListConfigValue.Integer LEGACY_REQUIRED_AMOUNT;
        public static final ListConfigValue.EnumValue<MiningSkillCardItem.Tier> CARD_CHALLENGES_AMOUNT;
        public static final ListConfigValue.EnumValue<MiningSkillCardItem.Tier> CARD_POTION_POINTS;
        public static final ListConfigValue.EnumValue<MiningSkillCardItem.Tier> CARD_POTION_DURATIONS;
        public static final ModConfigSpec.BooleanValue CARD_MASTERED_EFFECT;
        public static final ModConfigSpec.BooleanValue CARD_TIER_BASED_MAX_BLOCKS;
        public static final ListConfigValue.EnumValue<MiningSkillCardItem.Tier> CARD_TIER_MAX_BLOCKS;

        // Trades Settings
        public static final ModConfigSpec.IntValue VILLAGER_CARD_TRADE_LEVEL;
        public static final ListConfigValue.Integer CARD_TRADE_PRICE;

        // Debugging Settings
        public static final ModConfigSpec.BooleanValue INELIGIBLE_BLOCKS_LOGGER;
        public static final ModConfigSpec.BooleanValue CHALLENGE_MANAGER_LOGGER;
        public static final ModConfigSpec.BooleanValue CHALLENGE_ACTIONS_LOGGER;

        static {
            BUILDER.push("General");
            BLACKLISTED_SHAPES = BUILDER
                    .comment("Defines a list of forbidden shape types for mining.",
                            "Valid shape IDs: %s".formatted(ShapeRegistryAccessor.getShapesList().stream().map(Shape::getName).toList()))
                    .defineList("blacklisted_shapes", Collections.emptyList(), String::new,
                            o -> o instanceof String s && ShapeRegistryAccessor.getShapesList().stream().map(Shape::getName).anyMatch(s1 -> s1.equals(s)));

            IS_PLACED_BY_ENTITY_CONDITION = BUILDER
                    .comment("If enabled, blocks placed by entities will not count towards challenges.",
                            "This prevents exploiting challenges with entity-placed blocks.")
                    .define("is_placed_by_entity_condition", true);

            CARD_VALIDATOR = BUILDER
                    .comment("Sets the validation time (in seconds) for fixing corrupted Mining Skill Cards.",
                            "Range: 1 ~ 600 seconds.")
                    .defineInRange("challenge_validator", 2, 1, 600);
            BUILDER.pop();

            BUILDER.push("Gameplay");
            PAPER_CONSUMPTION_RATE = BUILDER
                    .comment("Adjusts the paper consumption rate in the Skills Record.",
                            "Range: 0 (no consumption) to 1 (full consumption).")
                    .defineInRange("paper_consummation_rate", 0.35, 0, 1);

            LEGACY_REQUIRED_AMOUNT = new ListConfigValue.Integer(2, 1, Integer.MAX_VALUE);
            LEGACY_REQUIRED_AMOUNT.define(
                    BUILDER,
                    "legacy_required_amount",
                    ImmutableList.of(1, 100),
                    "Defines the required amount range for the miner certificate challenge.",
                    "Only applicable if the playstyle mode is set to LEGACY."
            );

            CARD_CHALLENGES_AMOUNT = new ListConfigValue.EnumValue<>(4, MiningSkillCardItem.Tier.class, 1, 30);
            CARD_CHALLENGES_AMOUNT.defineMap(
                    BUILDER,
                    "card_challenges_amount",
                    Map.of(
                            MiningSkillCardItem.Tier.Unlearned, 1,
                            MiningSkillCardItem.Tier.Novice,2,
                            MiningSkillCardItem.Tier.Apprentice,3,
                            MiningSkillCardItem.Tier.Adept,4
                    ),
                    "Defines the number of challenges per tier.",
                    "Each tier can have a different number of challenges.",
                    "Range: 1 ~ 30"
            );


            CARD_POTION_POINTS = new ListConfigValue.EnumValue<>(3, MiningSkillCardItem.Tier.class, 1, 20);
            CARD_POTION_POINTS.defineMap(
                    BUILDER,
                    "card_potion_points",
                    Map.of(
                            MiningSkillCardItem.Tier.Novice,3,
                            MiningSkillCardItem.Tier.Apprentice,2,
                            MiningSkillCardItem.Tier.Adept,1
                    ),
                    "Defines the potion points awarded per tier.",
                    "Higher tiers may award fewer points.",
                    "Range: 1 ~ 20"
            );

            CARD_POTION_DURATIONS = new ListConfigValue.EnumValue<>(3, MiningSkillCardItem.Tier.class, 60, 3600);
            CARD_POTION_DURATIONS.defineMap(
                    BUILDER.worldRestart(),
                    "card_potion_durations",
                    Map.of(
                            MiningSkillCardItem.Tier.Novice,300,
                            MiningSkillCardItem.Tier.Apprentice,600,
                            MiningSkillCardItem.Tier.Adept,1200
                    ),
                    "Defines the duration (in seconds) of the Ultimine ability per tier.",
                    "Range: 60 ~ 3600 seconds."
            );

            CARD_MASTERED_EFFECT = BUILDER
                    .comment("If enabled, a Mastered Mining Skill Card grants the Ultimine ability for its corresponding tool.",
                            "This provides a significant gameplay advantage.")
                    .define("card_mastered_effect", true);

            CARD_TIER_BASED_MAX_BLOCKS = BUILDER
                    .comment("If enabled, the Ultimine max blocks value will vary based on the card tier.",
                            "This adds a tier-based progression system.")
                    .define("tier_based_max_blocks", true);

            CARD_TIER_MAX_BLOCKS = new ListConfigValue.EnumValue<>(3, MiningSkillCardItem.Tier.class, 1, 64);
            CARD_TIER_MAX_BLOCKS.defineMap(
                    BUILDER,
                    "card_max_blocks",
                    Map.of(
                            MiningSkillCardItem.Tier.Novice,8,
                            MiningSkillCardItem.Tier.Apprentice,16,
                            MiningSkillCardItem.Tier.Adept,32
                    ),
                    "Defines the Ultimine max blocks value for each tier.",
                    "Range: 1 ~ 64 blocks."
            );
            BUILDER.pop();

            BUILDER.push("Trades");
            VILLAGER_CARD_TRADE_LEVEL = BUILDER.worldRestart()
                    .comment("Defines the required trade level for card trading with villagers.")
                    .defineInRange("card_trade_level", 1, 1, 5);

            CARD_TRADE_PRICE = new ListConfigValue.Integer(2, 1, 256);
            CARD_TRADE_PRICE.define(
                    BUILDER,
                    "card_trade_price",
                    ImmutableList.of(10, 100),
                    "Defines the trade price range for Mining Skill Cards.",
                    "Range: 1 ~ 256."
            );
            BUILDER.pop();

            BUILDER.push("Debugging");
            INELIGIBLE_BLOCKS_LOGGER = BUILDER
                    .comment("Enables or disables logging of ineligible blocks for challenges.",
                            "Useful for debugging challenge eligibility issues.")
                    .define("ineligible_blocks_logger", true);
            CHALLENGE_MANAGER_LOGGER = BUILDER
                    .comment("Enables or disables logging for the Challenge Manager.",
                            "Useful for tracking challenge progress and errors.")
                    .define("challenge_manager_logger", true);
            CHALLENGE_ACTIONS_LOGGER = BUILDER
                    .comment("Enables or disables logging for challenge actions.",
                            "Useful for debugging challenge-related events.")
                    .define("challenge_actions_logger", true);
            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }
}