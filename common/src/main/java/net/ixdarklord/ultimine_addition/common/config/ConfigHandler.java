package net.ixdarklord.ultimine_addition.common.config;

import net.ixdarklord.ultimine_addition.client.gui.screen.ChallengesInfoPanel;
import net.ixdarklord.ultimine_addition.client.gui.screen.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHandler {
    public static void register() {
        ServicePlatform.registerConfig();
    }

    public static void validate() {
        if (COMMON.TRADE_LOW_PRICE.get() > COMMON.TRADE_HIGH_PRICE.get()) {
            throw new IllegalArgumentException("The low price has a higher value than high price.");
        }
        if (COMMON.LEGACY_REQUIRED_AMOUNT_MIN.get() > COMMON.LEGACY_REQUIRED_AMOUNT_MAX.get()) {
            throw new IllegalArgumentException("The low required amount has a higher value than max required amount.");
        }
    }

    public static class CLIENT {
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec.EnumValue<SkillsRecordScreen.BGColor> BACKGROUND_COLOR;
        public static final ForgeConfigSpec.ConfigValue<Boolean> ANIMATIONS_MODE;
        public static final ForgeConfigSpec.IntValue PROGRESS_BAR;
        public static final ForgeConfigSpec.EnumValue<ChallengesInfoPanel.Panel.Position> CHALLENGES_PANEL_POSITION;
        public static final ForgeConfigSpec.BooleanValue MSC_RENDERER;
        public static final ForgeConfigSpec.BooleanValue TEXT_SCREEN_SHADOW;

        static {
            BUILDER.push("Visuals");
            TEXT_SCREEN_SHADOW = BUILDER
                    .comment("This will Enable or Disable the drop shadow effect in the text screen of the Skills Record.")
                    .define("text_screen_shadow", true);
            BACKGROUND_COLOR = BUILDER
                    .comment("This is the background color for the skills record GUI.")
                    .defineEnum("background_color", SkillsRecordScreen.BGColor.DEFAULT);
            ANIMATIONS_MODE = BUILDER
                    .comment("This will enable or disable the animations on the skills record GUI.")
                    .define("animations_mode", true);
            PROGRESS_BAR = BUILDER
                    .comment("Here you can choose whatever mode you prefer for the bar visibility",
                            "In the skills record GUI.",
                            "0: Always on.",
                            "1: On holding its keybind. \"Default Keybind: Shift\"",
                            "2: Disabled.")
                    .defineInRange("progress_bar_mode", 0, 0, 2);
            CHALLENGES_PANEL_POSITION = BUILDER
                    .comment("You can choose when will the challenges panel appears on the screen.")
                    .defineEnum("challenges_panel_pos", ChallengesInfoPanel.Panel.Position.LEFT);
            MSC_RENDERER = BUILDER
                    .comment("Here you can enable or disable the Mining Skill Card Renderer",
                            "It's not recommended for now! [WIP]")
                    .define("msc_renderer", false);
            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }

    public static class COMMON {
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final SafeConfig.Builder SAFE_CONFIG_BUILDER = new SafeConfig.Builder(UltimineAddition.MOD_ID, "common-config.toml", "%s/SafeConfig".formatted(UltimineAddition.MOD_NAME));

        /**
         * This is a safe version of {@link COMMON#PLAYSTYLE_MODE} to use even if the config wasn't loaded.
         */
        public static final SafeConfig<PlaystyleMode> PLAYSTYLE_MODE_SAFE;
        public static final ForgeConfigSpec.EnumValue<PlaystyleMode> PLAYSTYLE_MODE;
        public static final ForgeConfigSpec.IntValue CARD_TRADE_LEVEL;
        public static final ForgeConfigSpec.IntValue TRADE_LOW_PRICE;
        public static final ForgeConfigSpec.IntValue TRADE_HIGH_PRICE;
        public static final ForgeConfigSpec.DoubleValue PAPER_CONSUMPTION_RATE;
        public static final ForgeConfigSpec.BooleanValue IS_PLACED_BY_ENTITY_CONDITION;
        public static final ForgeConfigSpec.IntValue CHALLENGE_VALIDATOR;
        public static final ForgeConfigSpec.BooleanValue TIER_BASED_MAX_BLOCKS;
        public static final ForgeConfigSpec.IntValue LEGACY_REQUIRED_AMOUNT_MIN;
        public static final ForgeConfigSpec.IntValue LEGACY_REQUIRED_AMOUNT_MAX;
        public static final ForgeConfigSpec.IntValue TIER_0_CHALLENGES_AMOUNT;
        public static final ForgeConfigSpec.IntValue TIER_1_CHALLENGES_AMOUNT;
        public static final ForgeConfigSpec.IntValue TIER_2_CHALLENGES_AMOUNT;
        public static final ForgeConfigSpec.IntValue TIER_3_CHALLENGES_AMOUNT;

        public static final ForgeConfigSpec.IntValue TIER_1_POTION_POINTS;
        public static final ForgeConfigSpec.IntValue TIER_2_POTION_POINTS;
        public static final ForgeConfigSpec.IntValue TIER_3_POTION_POINTS;

        public static final ForgeConfigSpec.BooleanValue MASTERED_CARD_EFFECT;
        public static final ForgeConfigSpec.IntValue TIER_1_MAX_BLOCKS;
        public static final ForgeConfigSpec.IntValue TIER_2_MAX_BLOCKS;
        public static final ForgeConfigSpec.IntValue TIER_3_MAX_BLOCKS;

        /**
         * This is a safe version of {@link COMMON#TIER_1_TIME} to use even if the config wasn't loaded.
         */
        public static final SafeConfig<Integer> TIER_1_TIME_SAFE;

        /**
         * This is a safe version of {@link COMMON#TIER_2_TIME} to use even if the config wasn't loaded.
         */
        public static final SafeConfig<Integer> TIER_2_TIME_SAFE;

        /**
         * This is a safe version of {@link COMMON#TIER_3_TIME} to use even if the config wasn't loaded.
         */
        public static final SafeConfig<Integer> TIER_3_TIME_SAFE;

        public static final ForgeConfigSpec.IntValue TIER_1_TIME;
        public static final ForgeConfigSpec.IntValue TIER_2_TIME;
        public static final ForgeConfigSpec.IntValue TIER_3_TIME;
        public static final ForgeConfigSpec.BooleanValue INELIGIBLE_BLOCKS_LOGGER;
        public static final ForgeConfigSpec.BooleanValue CHALLENGE_MANAGER_LOGGER;
        public static final ForgeConfigSpec.BooleanValue CHALLENGE_ACTIONS_LOGGER;

        static {
            BUILDER.push("General");
            PLAYSTYLE_MODE = BUILDER
                    .comment("%s: This is the current modern playstyle of the mod! With new features and exciting challenges.".formatted(PlaystyleMode.MODERN.name()),
                            "%s [WIP]: There will be one tier for the Mining Skill Card. If you complete all the challenges, it will turn the card to the Mastered tier immediately.".formatted(PlaystyleMode.ONE_TIER_ONLY.name()),
                            "%s: It will revert the mod mechanics as it was on the original release. (\"v0.1.0\") There will be only the miner certificate with one challenge to complete.".formatted(PlaystyleMode.LEGACY.name()))
                    .defineEnum("playstyle_mode", PlaystyleMode.MODERN);

            PAPER_CONSUMPTION_RATE = BUILDER
                    .comment("You can change the rate of paper consumption in the Skills Record.")
                    .defineInRange("paper_consummation_rate", 0.35, 0, 1);
            IS_PLACED_BY_ENTITY_CONDITION = BUILDER
                    .comment("This condition is when the block is placed by any entity... It will not count as a point toward the challenges.")
                    .define("is_placed_by_entity_condition", true);
            CHALLENGE_VALIDATOR = BUILDER
                    .comment("Here, You can change the time to validate the challenges in the mining skills card for fixing the corrupted data if present.",
                            "It's formatted in seconds.")
                    .defineInRange("challenge_validator", 2, 1, 600);
            BUILDER.pop();

            BUILDER.push("Trades");
            CARD_TRADE_LEVEL = BUILDER
                    .comment("Here, you can change which level the Mining Skill Card appears in villager trades.")
                    .defineInRange("villager_card_trade_level", 2, 1, 5);
            TRADE_LOW_PRICE = BUILDER
                    .comment("It will change the Mining Skill Card cost in villager trades.")
                    .defineInRange("trade_low_price", 8, 1, 64);
            TRADE_HIGH_PRICE = BUILDER.defineInRange("trade_high_price", 24, 1, 64);
            BUILDER.pop();

            BUILDER.push("Challenges");
            LEGACY_REQUIRED_AMOUNT_MIN = BUILDER
                    .comment("Here, you can change the value for the required amount of ores.",
                            "NOTE: This values only matters if the playstyle is set on Legacy.")
                    .defineInRange("legacy_required_amount_min", 400, 1, 999999);
            LEGACY_REQUIRED_AMOUNT_MAX = BUILDER.defineInRange("legacy_required_amount_max", 400, 1, 999999);
            TIER_0_CHALLENGES_AMOUNT = BUILDER
                    .comment("You can change the values on how many challenges should be given in each tier.",
                            "But remember that you must have the exact number of challenges available in the Datapack.",
                            "Otherwise, it will make the game crash!")
                    .defineInRange("tier_0_challenges_amount", 1, 1, 20);
            TIER_1_CHALLENGES_AMOUNT = BUILDER.defineInRange("tier_1_challenges_amount", 2, 1, 20);
            TIER_2_CHALLENGES_AMOUNT = BUILDER.defineInRange("tier_2_challenges_amount", 4, 1, 20);
            TIER_3_CHALLENGES_AMOUNT = BUILDER.defineInRange("tier_3_challenges_amount", 5, 1, 20);
            BUILDER.pop();

            BUILDER.push("Potions");
            TIER_1_POTION_POINTS = BUILDER
                    .comment("You can change the values on how many potion points should be given in each tier.")
                    .defineInRange("tier_1_potion_points", 3, 0, 20);
            TIER_2_POTION_POINTS = BUILDER.defineInRange("tier_2_potion_points", 2, 0, 20);
            TIER_3_POTION_POINTS = BUILDER.defineInRange("tier_3_potion_points", 1, 0, 20);
            BUILDER.pop();

            BUILDER.push("Ability");
            MASTERED_CARD_EFFECT = BUILDER
                    .comment("If a Mining Skill Card reaches the \"mastered\" tier, it will give the player the ultimine ability for the exact tool the card has on.")
                    .define("mastered_card_effect", true);
            TIER_BASED_MAX_BLOCKS = BUILDER
                    .comment("This makes the ultimine max blocks value different for every tier.")
                    .define("tier_based_max_blocks", true);

            TIER_1_MAX_BLOCKS = BUILDER
                    .comment("You can change the ultimine max blocks value for each tier.")
                    .defineInRange("tier_1_max_blocks", 8, 1, 64);
            TIER_2_MAX_BLOCKS = BUILDER.defineInRange("tier_2_max_blocks", 16, 1, 64);
            TIER_3_MAX_BLOCKS = BUILDER.defineInRange("tier_3_max_blocks", 32, 1, 64);

            TIER_1_TIME = BUILDER
                    .comment("You can change the ultimine ability time per tier.",
                            "It's formatted in seconds.")
                    .defineInRange("tier_1_time", 300, 60, 3600);
            TIER_2_TIME = BUILDER.defineInRange("tier_2_time", 600, 60, 3600);
            TIER_3_TIME = BUILDER.defineInRange("tier_3_time", 1200, 60, 3600);
            BUILDER.pop();

            BUILDER.push("Debug");
            INELIGIBLE_BLOCKS_LOGGER = BUILDER
                    .comment("Enable or disable the IneligibleBlocks logger.")
                    .define("ineligible_blocks_logger", false);
            CHALLENGE_MANAGER_LOGGER = BUILDER
                    .comment("Enable or disable the ChallengeManager logger.")
                    .define("challenge_manager_logger", false);
            CHALLENGE_ACTIONS_LOGGER = BUILDER
                    .define("challenge_actions_logger", false);
            BUILDER.pop();
            SPEC = BUILDER.build();

            PLAYSTYLE_MODE_SAFE = SAFE_CONFIG_BUILDER.readEnum("General.playstyle_mode", PlaystyleMode.MODERN);
            TIER_1_TIME_SAFE = SAFE_CONFIG_BUILDER.readInt("Ability.tier_1_time", 300);
            TIER_2_TIME_SAFE = SAFE_CONFIG_BUILDER.readInt("Ability.tier_2_time", 600);
            TIER_3_TIME_SAFE = SAFE_CONFIG_BUILDER.readInt("Ability.tier_3_time", 1200);
        }
    }
}