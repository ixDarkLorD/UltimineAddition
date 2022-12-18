package net.ixdarklord.ultimine_addition.config;

import net.ixdarklord.ultimine_addition.helper.Services;
import net.minecraftforge.common.ForgeConfigSpec;


public class ConfigHandler {
    public static void register() {
        Services.PLATFORM.registerConfig();
    }

    public static class COMMON {
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        public static final ForgeConfigSpec.ConfigValue<Boolean> QUEST_MODE;
        public static final ForgeConfigSpec.IntValue REQUIRED_AMOUNT;

        static {
            BUILDER.push("General");
            QUEST_MODE = BUILDER
                    .comment("If you don't like the quest mode to obtain the miner certificate",
                             "You can turn it off from here!")
                    .define("quest_mode", true);
            REQUIRED_AMOUNT = BUILDER
                    .comment("You can change the required amount of blocks you need to break to unseal the miner certificate.")
                    .defineInRange("required_amount", 400, 1, 20000);
            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }
}
