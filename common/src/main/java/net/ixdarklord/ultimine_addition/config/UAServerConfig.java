package net.ixdarklord.ultimine_addition.config;

import net.ixdarklord.coolcatcore.api.config.Config;
import net.ixdarklord.coolcatcore.api.config.ConfigBuilder;
import net.ixdarklord.coolcatcore.api.config.ConfigScope;
import net.ixdarklord.coolcatcore.api.config.ConfigValue;
import net.ixdarklord.coolcatcore.api.config.type.EnumType;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem.Tier.*;

// Synced to clients whenever it changes (CoolCatLib); ops can edit it in-game with /ultimine_addition config.
public final class UAServerConfig {
    private static final ConfigBuilder BUILDER = Config.builder(FTBUltimineAddition.MOD_ID, ConfigScope.SERVER)
            .comment(FTBUltimineAddition.MOD_NAME + " server settings.");

    static { BUILDER.push("general"); }
    // Applies at once: see PlaystyleModes.
    public static final ConfigValue<PlaystyleMode> PLAYSTYLE_MODE = BUILDER.enumValue("playstyle_mode", PlaystyleMode.MODERN)
            .comment("Defines the playstyle mode for the mod:",
                    "modern: Modern playstyle with Mining Skill Cards, the Skills Record and Shape Certificates.",
                    "legacy: Restores mechanics from v0.1.0 (only one miner certificate and one challenge).",
                    "Changes apply at once, and players on a server play by the server's mode.")
            .build();
    public static final ConfigValue<List<String>> BLACKLISTED_SHAPES = BUILDER.stringList("blacklisted_shapes", List.of())
            .comment("Ultimine shapes nobody can use.",
                    "Press \"F3-H\" and then hold \"Left Shift\" in the Shape Selector to see a shape's ID.",
                    "Example: [\"ftbultimine:shapeless\", \"ftbultimine:small_tunnel\"]")
            .build();
    public static final ConfigValue<Boolean> IS_PLACED_BY_ENTITY_CONDITION = BUILDER.bool("is_placed_by_entity_condition", true)
            .comment("If enabled, blocks placed by entities don't count towards challenges.").build();
    public static final ConfigValue<Integer> CARD_VALIDATOR = BUILDER.intValue("challenge_validator", 2).range(1, 600)
            .comment("How often (in seconds) Mining Skill Cards are checked for invalid challenges.").build();
    static { BUILDER.pop(); }

    static { BUILDER.push("progression"); }
    public static final ConfigValue<Boolean> SHAPE_CERTIFICATES = BUILDER.bool("shape_certificates", true)
            .comment("A Mining Skill Card reaching Novice, Apprentice or Adept for the first time gives its owner a Shape Certificate for the card's tool.",
                    "Using one permanently unlocks its Ultimine shapes for that tool; Mine-Go Juice unlocks every shape while it lasts,",
                    "and the Miner Certificate unlocks everything for good.",
                    "Has no effect in the legacy playstyle mode.")
            .build();
    public static final ConfigValue<List<String>> NOVICE_CERTIFICATE_SHAPES = BUILDER.stringList("novice_certificate_shapes", List.of("ftbultimine:small_square"))
            .comment("Shapes unlocked by the Novice Shape Certificate, for its tool.").enabledWhen(SHAPE_CERTIFICATES).build();
    public static final ConfigValue<List<String>> APPRENTICE_CERTIFICATE_SHAPES = BUILDER.stringList("apprentice_certificate_shapes", List.of("ftbultimine:escape_tunnel", "ftbultimine:mining_tunnel"))
            .comment("Shapes unlocked by the Apprentice Shape Certificate, for its tool.").enabledWhen(SHAPE_CERTIFICATES).build();
    public static final ConfigValue<List<String>> ADEPT_CERTIFICATE_SHAPES = BUILDER.stringList("adept_certificate_shapes", List.of("ftbultimine:small_tunnel", "ftbultimine:large_tunnel"))
            .comment("Shapes unlocked by the Adept Shape Certificate, for its tool.").enabledWhen(SHAPE_CERTIFICATES).build();
    public static final ConfigValue<ExtraShapes> EXTRA_SHAPES_CERTIFICATE = BUILDER.enumValue("extra_shapes_certificate", ExtraShapes.ADEPT)
            .comment("Shapes added by other mods (FTB Ultimine plugins) that no certificate lists above join this certificate.",
                    "none leaves them to the Miner Certificate and Mine-Go Juice.")
            .enabledWhen(SHAPE_CERTIFICATES).build();
    public static final ConfigValue<Boolean> TIER_UP_TASTE = BUILDER.bool("tier_up_taste", false)
            .comment("Grants a short Mine-Go Juice for the card's tool when a Mining Skill Card tiers up.").build();
    public static final ConfigValue<Integer> TIER_UP_TASTE_DURATION = BUILDER.intValue("tier_up_taste_duration", 60).range(0, 600)
            .comment("Seconds of free Mine-Go Juice for the card's tool when a Mining Skill Card tiers up. 0 disables it.")
            .enabledWhen(TIER_UP_TASTE).build();
    public static final ConfigValue<Integer> STREAK_BONUS_INTERVAL = BUILDER.intValue("streak_bonus_interval", 5).range(0, 100)
            .comment("Every Nth challenge block broken in a streak counts one extra point. 0 disables streaks.").build();
    public static final ConfigValue<Integer> STREAK_WINDOW = BUILDER.intValue("streak_window", 3).range(1, 30)
            .comment("Seconds allowed between two challenge blocks before the streak resets.")
            .enabledWhen(STREAK_BONUS_INTERVAL, interval -> interval > 0).build();
    public static final ConfigValue<Double> LUCKY_FIND_CHANCE = BUILDER.doubleValue("lucky_find_chance", 0.05).range(0.0, 1.0).slider()
            .comment("Chance for a challenge block to count double.").build();
    public static final ConfigValue<Integer> REROLLS_PER_TIER = BUILDER.intValue("rerolls_per_tier", 1).range(0, 10)
            .comment("How many unstarted challenges a card can swap for new ones per tier. 0 disables rerolls.").build();
    public static final ConfigValue<Integer> REROLL_INK_COST = BUILDER.intValue("reroll_ink_cost", 16).range(0, 1000)
            .comment("Ink taken from the Skills Record's pen for each reroll.")
            .enabledWhen(REROLLS_PER_TIER, rerolls -> rerolls > 0).build();
    static { BUILDER.pop(); }

    public enum ExtraShapes implements EnumType.Displayable {
        NONE(null), NOVICE(Novice), APPRENTICE(Apprentice), ADEPT(Adept);

        private final MiningSkillCardItem.@Nullable Tier tier;

        ExtraShapes(MiningSkillCardItem.@Nullable Tier tier) {
            this.tier = tier;
        }

        public MiningSkillCardItem.@Nullable Tier tier() {
            return this.tier;
        }

        @Override
        public Component displayName() {
            return Component.translatable("ultimine_addition.extra_shapes." + this.name().toLowerCase(Locale.ROOT));
        }
    }

    static { BUILDER.push("undo"); }
    public static final ConfigValue<Boolean> UNDO_ENABLED = BUILDER.bool("enabled", true)
            .comment("Lets players undo their last Ultimine operations (Ctrl + Z by default).",
                    "Undoing puts the blocks back and takes the items (and experience) they dropped back, from the ground first,",
                    "then from the player's inventory; it's refused when something is missing.")
            .build();
    public static final ConfigValue<Integer> UNDO_WINDOW = BUILDER.intValue("window", 300).range(5, 3600)
            .comment("Seconds after an Ultimine operation during which it can be undone.").enabledWhen(UNDO_ENABLED).build();
    public static final ConfigValue<Integer> UNDO_HISTORY = BUILDER.intValue("history", 3).range(1, 10).slider()
            .comment("How many Ultimine operations per player can be undone, newest first.").enabledWhen(UNDO_ENABLED).build();
    public static final ConfigValue<Boolean> UNDO_ANIMATION = BUILDER.bool("animation", true)
            .comment("Blocks grow back into place one after another instead of appearing at once.").enabledWhen(UNDO_ENABLED).build();
    public static final ConfigValue<Integer> UNDO_BLOCKS_PER_TICK = BUILDER.intValue("animation_blocks_per_tick", 4).range(1, 64)
            .comment("How many blocks start growing back each tick.").enabledWhen(UNDO_ANIMATION).build();
    static { BUILDER.pop(); }

    static { BUILDER.push("mining_skill_cards"); }
    public static final TierValues CARD_CHALLENGES_AMOUNT = new TierValues(BUILDER, "challenges_amount",
            Map.of(Unlearned, 1, Novice, 2, Apprentice, 2, Adept, 3), 1, 30,
            "Number of challenges a card gets in each tier.");
    public static final TierValues CARD_POTION_POINTS = new TierValues(BUILDER, "potion_points",
            Map.of(Novice, 3, Apprentice, 2, Adept, 1), 1, 20,
            "Mine-Go Juice brews a card can make in each tier.");
    public static final TierValues CARD_POTION_DURATIONS = new TierValues(BUILDER, "potion_durations",
            Map.of(Novice, 300, Apprentice, 600, Adept, 1200), 60, 3600,
            "Duration (in seconds) of the Mine-Go Juice brewed with a card of each tier.");
    public static final ConfigValue<Boolean> CARD_MASTERED_EFFECT = BUILDER.bool("mastered_effect", true)
            .comment("If enabled, carrying a Mastered Mining Skill Card grants Ultimine for its tool.").build();
    public static final ConfigValue<Boolean> CARD_TIER_BASED_MAX_BLOCKS = BUILDER.bool("tier_based_max_blocks", true)
            .comment("If enabled, Mine-Go Juice and Shape Certificates limit Ultimine's max blocks by tier.").build();
    public static final TierValues CARD_TIER_MAX_BLOCKS = new TierValues(BUILDER, "max_blocks",
            Map.of(Novice, 8, Apprentice, 16, Adept, 32), 1, 64,
            "Ultimine max blocks for each tier.");
    static { BUILDER.pop(); }

    static { BUILDER.push("skills_record"); }
    public static final ConfigValue<Double> PAPER_CONSUMPTION_RATE = BUILDER.doubleValue("paper_consumption_rate", 0.35).range(0.0, 1.0).slider()
            .comment("Chance for each challenge point to use up a paper from the Skills Record.").build();
    static { BUILDER.pop(); }

    static { BUILDER.push("legacy"); }
    public static final ConfigValue<Integer> LEGACY_REQUIRED_MIN = BUILDER.intValue("required_amount_min", 64).range(1, Integer.MAX_VALUE)
            .comment("Lowest number of ores a sealed Miner Certificate can ask for (legacy playstyle mode only).").build();
    public static final ConfigValue<Integer> LEGACY_REQUIRED_MAX = BUILDER.intValue("required_amount_max", 128).range(1, Integer.MAX_VALUE)
            .comment("Highest number of ores a sealed Miner Certificate can ask for (legacy playstyle mode only).").build();
    static { BUILDER.pop(); }

    static { BUILDER.push("debugging"); }
    public static final ConfigValue<Boolean> INELIGIBLE_BLOCKS_LOGGER = BUILDER.bool("ineligible_blocks_logger", false)
            .comment("Log blocks that don't count for challenges.").build();
    public static final ConfigValue<Boolean> CHALLENGE_MANAGER_LOGGER = BUILDER.bool("challenge_manager_logger", false)
            .comment("Log challenge loading and assignment.").build();
    public static final ConfigValue<Boolean> CHALLENGE_ACTIONS_LOGGER = BUILDER.bool("challenge_actions_logger", false)
            .comment("Log every challenge action check.").build();
    static { BUILDER.pop(); }

    public static final Config CONFIG = BUILDER.build();

    private UAServerConfig() {}
}
