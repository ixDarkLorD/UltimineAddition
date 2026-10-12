package net.ixdarklord.ultimine_addition.common.progression;

import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

// A perk for a tool, learned for good by the player who picks it. A Mining Skill Card offers two at each of its Novice,
// Apprentice and Adept tiers (the ones the player doesn't have for that tool yet); the pick is made in the card viewer
// and stored with the player's ability data, per card type.
//
// Every perk belongs to a tier and is only offered from that tier on, so the small comforts come first and the ones that
// make an Ultimine bigger or free come last: a tier's box offers its own tier's perks before any left over from earlier.
public enum CardPerk {
    // --- Novice: comforts ---
    // An Ultimine's drops go straight into the inventory.
    MAGNET(Items.HOPPER, MiningSkillCardItem.Tier.Novice),
    // A few seconds of Haste after an Ultimine.
    VIGOR(Items.SUGAR, MiningSkillCardItem.Tier.Novice),
    // An Ultimine's extra hunger is halved.
    SATED(Items.BREAD, MiningSkillCardItem.Tier.Novice),
    // An Ultimine wears the tool a quarter less.
    THRIFTY(Items.ANVIL, MiningSkillCardItem.Tier.Novice),

    // --- Apprentice: more out of the work ---
    // A quarter more blocks in an Ultimine.
    REACH(Items.SPYGLASS, MiningSkillCardItem.Tier.Apprentice),
    // Lucky finds (challenge blocks counting double) twice as often for this tool's challenges.
    FORTUNE(Items.RABBIT_FOOT, MiningSkillCardItem.Tier.Apprentice),
    // A point of experience for every eight blocks of an Ultimine.
    INSIGHT(Items.EXPERIENCE_BOTTLE, MiningSkillCardItem.Tier.Apprentice),

    // --- Adept: the strong ones ---
    // Another quarter more blocks (on top of Reach).
    EXPANSE(Items.ENDER_EYE, MiningSkillCardItem.Tier.Adept),
    // Haste II after an Ultimine.
    FRENZY(Items.BLAZE_POWDER, MiningSkillCardItem.Tier.Adept),
    // An Ultimine costs no extra hunger at all.
    TIRELESS(Items.GOLDEN_CARROT, MiningSkillCardItem.Tier.Adept);

    public static final float REACH_FACTOR = 1.25F;
    public static final int VIGOR_TICKS = 120;
    public static final float THRIFTY_FACTOR = 0.25F;
    public static final int INSIGHT_BLOCKS = 8;

    private final Item icon;
    private final MiningSkillCardItem.Tier tier;

    CardPerk(Item icon, MiningSkillCardItem.Tier tier) {
        this.icon = icon;
        this.tier = tier;
    }

    /** The tier this perk is offered from. */
    public MiningSkillCardItem.Tier tier() {
        return this.tier;
    }

    /** How much of FTB Ultimine's per-block hunger the player pays with the tool in hand. */
    public static float hungerFactor(Player player) {
        return TIRELESS.has(player) ? 0.0F : SATED.has(player) ? 0.5F : 1.0F;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public ItemStack icon() {
        return new ItemStack(this.icon);
    }

    public MutableComponent displayName() {
        return Component.translatable("perk.ultimine_addition." + this.id());
    }

    public MutableComponent description() {
        return Component.translatable("perk.ultimine_addition." + this.id() + ".description");
    }

    public static @Nullable CardPerk byId(String id) {
        for (CardPerk perk : values()) if (perk.id().equals(id)) return perk;
        return null;
    }

    public static boolean enabled() {
        return UAServerConfig.CARD_PERKS.get() && !PlaystyleModes.isLegacy();
    }

    /** The tiers whose box offers a perk. */
    public static boolean isPerkTier(MiningSkillCardItem.Tier tier) {
        return tier == MiningSkillCardItem.Tier.Novice || tier == MiningSkillCardItem.Tier.Apprentice || tier == MiningSkillCardItem.Tier.Adept;
    }

    /**
     * The two perks this card offers this player at this tier: picked from the ones the player doesn't have for the
     * card's tool, the same two every time it is asked (so the client and the server agree, and reopening the choice
     * doesn't reroll it). Empty when there is nothing to pick.
     */
    public static List<CardPerk> offer(Player player, MiningSkillCardData card, MiningSkillCardItem.Tier tier) {
        if (!enabled() || !card.canClaimPerk(tier)) return List.of();
        Set<String> known = ServicePlatform.get().players().getAbilityData(player).getPerks(card.getType().getId());
        List<CardPerk> open = new ArrayList<>();
        for (CardPerk perk : values()) if (!known.contains(perk.id()) && perk.tier.getValue() <= tier.getValue()) open.add(perk);
        Collections.shuffle(open, new Random(card.getUUID().getMostSignificantBits() ^ card.getUUID().getLeastSignificantBits() ^ tier.getValue() * 0x9E3779B97F4A7C15L));
        // The claimed tier's own perks first, then what is left of the earlier tiers' (the sort keeps the shuffle within a tier).
        open.sort(java.util.Comparator.comparingInt((CardPerk perk) -> -perk.tier.getValue()));
        return List.copyOf(open.subList(0, Math.min(2, open.size())));
    }

    /** Whether the player has this perk for the tool in their hand. */
    public boolean has(Player player) {
        if (!enabled()) return false;
        var data = ServicePlatform.get().players().getAbilityData(player);
        for (MiningSkillCardItem.Type type : ItemUtils.getToolTypes(ItemUtils.getItemInHand(player, true))) {
            if (data.getPerks(type.getId()).contains(this.id())) return true;
        }
        return false;
    }

    /** Whether the player has this perk for this card type. */
    public boolean has(Player player, MiningSkillCardItem.Type type) {
        return enabled() && ServicePlatform.get().players().getAbilityData(player).getPerks(type.getId()).contains(this.id());
    }
}
