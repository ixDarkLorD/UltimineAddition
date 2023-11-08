package net.ixdarklord.ultimine_addition.common.config;

import dev.architectury.platform.Platform;
import net.ixdarklord.coolcat_lib.util.TomlConfigReader;
import net.ixdarklord.ultimine_addition.client.gui.screen.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Supplier;

public class ConfigHandler {
    public static void register() {
        ServicePlatform.registerConfig();
        MSCCustomType.registerConfig();
    }

    public static class CLIENT {
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        public static final ForgeConfigSpec.EnumValue<SkillsRecordScreen.BGColor> BACKGROUND_COLOR;
        public static final ForgeConfigSpec.ConfigValue<Boolean> ANIMATIONS_MODE;
        public static final ForgeConfigSpec.IntValue PROGRESS_BAR;
        public static final ForgeConfigSpec.BooleanValue MSC_RENDERER;
        public static final ForgeConfigSpec.BooleanValue TEXT_SCREEN_SHADOW;

        static {
            BUILDER.push("Visuals");
            TEXT_SCREEN_SHADOW = BUILDER
                    .comment("This will Enable or Disable the drop shadow effect in the text screen of the Skills Record.")
                    .define("text_screen_shadow", true);
            BACKGROUND_COLOR = BUILDER
                    .comment(" This is the background color for the skills record GUI.")
                    .defineEnum("background_color", SkillsRecordScreen.BGColor.DEFAULT);
            ANIMATIONS_MODE = BUILDER
                    .comment(" This will enable or disable the animations on the skills record GUI.")
                    .define("animations_mode", true);
            PROGRESS_BAR = BUILDER
                    .comment(" Here you can choose whatever mode you prefer for the bar visibility",
                            " In the skills record GUI.",
                            " 0: Always on.",
                            " 1: On holding its keybind. \"Default Keybind: Shift\"",
                            " 2: Disabled.")
                    .defineInRange("progress_bar_mode", 0, 0, 2);
            MSC_RENDERER = BUILDER
                    .comment(" Here you can enable or disable the Mining Skill Card Renderer",
                            " It's not recommended for now! [WIP]")
                    .define("msc_renderer", false);
            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }

    public static class COMMON {
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final TomlConfigReader CONFIG_READER = new TomlConfigReader(Constants.MOD_NAME, String.format("%s/%s/common-config.toml", Platform.getConfigFolder(), Constants.MOD_ID));

        public static final ForgeConfigSpec.IntValue CHALLENGE_VALIDATOR;
        public static final ForgeConfigSpec.BooleanValue TIER_BASED_MAX_BLOCKS;
        public static final ForgeConfigSpec.IntValue TIER_0_CHALLENGES_AMOUNT;
        public static final ForgeConfigSpec.IntValue TIER_1_CHALLENGES_AMOUNT;
        public static final ForgeConfigSpec.IntValue TIER_2_CHALLENGES_AMOUNT;
        public static final ForgeConfigSpec.IntValue TIER_3_CHALLENGES_AMOUNT;
        public static final ForgeConfigSpec.IntValue TIER_1_MAX_BLOCKS;
        public static final ForgeConfigSpec.IntValue TIER_2_MAX_BLOCKS;
        public static final ForgeConfigSpec.IntValue TIER_3_MAX_BLOCKS;

        /**
         * This is a safe version of {@link COMMON#TIER_1_TIME} to use even if the config wasn't loaded.
         */
        public static final Supplier<Integer> TIER_1_TIME_SAFE;

        /**
         * This is a safe version of {@link COMMON#TIER_2_TIME} to use even if the config wasn't loaded.
         */
        public static final Supplier<Integer> TIER_2_TIME_SAFE;

        /**
         * This is a safe version of {@link COMMON#TIER_3_TIME} to use even if the config wasn't loaded.
         */
        public static final Supplier<Integer> TIER_3_TIME_SAFE;

        public static final ForgeConfigSpec.IntValue TIER_1_TIME;
        public static final ForgeConfigSpec.IntValue TIER_2_TIME;
        public static final ForgeConfigSpec.IntValue TIER_3_TIME;
        public static final ForgeConfigSpec.BooleanValue CHUNK_DATA_LOGGER;
        public static final ForgeConfigSpec.BooleanValue CHALLENGE_MANAGER_LOGGER;
        public static final ForgeConfigSpec.BooleanValue CHALLENGE_ACTIONS_LOGGER;
        public static final ForgeConfigSpec.DoubleValue PAPER_CONSUMPTION_RATE;

        static {
            BUILDER.push("General");
            PAPER_CONSUMPTION_RATE = BUILDER
                    .comment(" Here, you can change the rate of paper consumption in the Skills Record.")
                    .defineInRange("paper_consummation_rate", 0.35, 0, 1);
            CHALLENGE_VALIDATOR = BUILDER
                    .comment(" Here, You can change the time to validate the challenges in the mining skills card for fixing the corrupted data if present.",
                            " It's formatted in seconds.")
                    .defineInRange("challenge_validator", 2, 1, 600);
            BUILDER.pop();

            BUILDER.push("Challenges");
            TIER_0_CHALLENGES_AMOUNT = BUILDER
                    .comment("You can change the values on how many challenges should be given in each tier.",
                            "But remember that you must have the exact number of challenges in the Datapack.",
                            "Otherwise, it will make the game crash!")
                    .defineInRange("tier_0_challenges_amount", 1, 1, 20);
            TIER_1_CHALLENGES_AMOUNT = BUILDER.defineInRange("tier_1_challenges_amount", 2, 1, 20);
            TIER_2_CHALLENGES_AMOUNT = BUILDER.defineInRange("tier_2_challenges_amount", 4, 1, 20);
            TIER_3_CHALLENGES_AMOUNT = BUILDER.defineInRange("tier_3_challenges_amount", 5, 1, 20);
            BUILDER.pop();

            BUILDER.push("Ability");
            TIER_BASED_MAX_BLOCKS = BUILDER
                    .comment(" This makes the ultimine max blocks value different for every tier.")
                    .define("tier_based_max_blocks", true);

            TIER_1_MAX_BLOCKS = BUILDER
                    .comment(" You can change the ultimine max blocks value for each tier.")
                    .defineInRange("tier_1_max_blocks", 8, 1, 64);
            TIER_2_MAX_BLOCKS = BUILDER.defineInRange("tier_2_max_blocks", 16, 1, 64);
            TIER_3_MAX_BLOCKS = BUILDER.defineInRange("tier_3_max_blocks", 32, 1, 64);

            TIER_1_TIME = BUILDER
                    .comment(" You can change the ultimine ability time per tier.",
                            " It's formatted in seconds.")
                    .defineInRange("tier_1_time", 300, 60, 3600);
            TIER_2_TIME = BUILDER.defineInRange("tier_2_time", 600, 60, 3600);
            TIER_3_TIME = BUILDER.defineInRange("tier_3_time", 1200, 60, 3600);
            BUILDER.pop();

            BUILDER.push("Debug");
            CHUNK_DATA_LOGGER = BUILDER
                    .comment(" Enable or disable the ChunkData logger.")
                    .define("chunk_data_logger", false);
            CHALLENGE_MANAGER_LOGGER = BUILDER
                    .comment(" Enable or disable the ChallengeManager logger.")
                    .define("challenge_manager_logger", false);
            CHALLENGE_ACTIONS_LOGGER = BUILDER
                    .define("challenge_actions_logger", false);
            BUILDER.pop();
            SPEC = BUILDER.build();

            TIER_1_TIME_SAFE = () -> CONFIG_READER.getIntValue("Ability.tier_1_time", 300);
            TIER_2_TIME_SAFE = () -> CONFIG_READER.getIntValue("Ability.tier_2_time", 600);
            TIER_3_TIME_SAFE = () -> CONFIG_READER.getIntValue("Ability.tier_3_time", 1200);
        }
    }
}
