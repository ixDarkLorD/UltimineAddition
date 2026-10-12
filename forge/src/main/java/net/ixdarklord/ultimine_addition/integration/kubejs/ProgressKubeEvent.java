package net.ixdarklord.ultimine_addition.integration.kubejs;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
import net.ixdarklord.ultimine_addition.common.progression.ProgressionHooks;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

// One of the mod's progression moments, as scripts see it (see UAKubeJSPlugin).
public class ProgressKubeEvent extends PlayerEventJS {
    private final ProgressionHooks.Event event;

    public ProgressKubeEvent(ProgressionHooks.Event event) {
        this.event = event;
    }

    @Override
    public Player getEntity() {
        return this.event.player();
    }

    /** The card type's id: "pickaxe", "mypack:hammer"... */
    public String getCardType() {
        return this.event.cardType();
    }

    /** The card's tier (0 Unlearned to 4 Mastered); the prestige count for the prestige event. */
    public int getTier() {
        return this.event.tier();
    }

    /** The challenge's id, where there is one. */
    public @Nullable String getChallenge() {
        return this.event.challenge() == null ? null : this.event.challenge().toString();
    }

    /** The perk's id for perkLearned; "" otherwise. */
    public String getDetail() {
        return this.event.detail();
    }
}
