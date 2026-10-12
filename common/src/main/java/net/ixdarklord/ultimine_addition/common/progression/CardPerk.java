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
public enum CardPerk {
    // A quarter more blocks in an Ultimine.
    REACH(Items.SPYGLASS),
    // An Ultimine's drops go straight into the inventory.
    MAGNET(Items.HOPPER),
    // Lucky finds (challenge blocks counting double) twice as often for this tool's challenges.
    FORTUNE(Items.RABBIT_FOOT),
    // A few seconds of Haste after an Ultimine.
    VIGOR(Items.SUGAR);

    public static final float REACH_FACTOR = 1.25F;
    public static final int VIGOR_TICKS = 120;

    private final Item icon;

    CardPerk(Item icon) {
        this.icon = icon;
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
        for (CardPerk perk : values()) if (!known.contains(perk.id())) open.add(perk);
        Collections.shuffle(open, new Random(card.getUUID().getMostSignificantBits() ^ card.getUUID().getLeastSignificantBits() ^ tier.getValue() * 0x9E3779B97F4A7C15L));
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
