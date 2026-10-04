package net.ixdarklord.ultimine_addition.common.effect;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// Mine-Go Juice: Ultimine with every shape for one tool while it lasts. The built-in tools have an effect each. Card
// types from data packs share the generic one (effects can't be registered from data packs): which type a player's
// generic juice is for is kept in their ability data, so one generic juice is active at a time.
public class MineGoJuiceEffect extends MobEffect {
    private final MiningSkillCardItem.Type type;

    public MineGoJuiceEffect(MiningSkillCardItem.Type type, MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        this.type = type;
    }

    /** The effect's own type: {@link MiningSkillCardItem.Type#GENERIC} for the shared one (see {@link #typeOf}). */
    public MiningSkillCardItem.Type getType() {
        return type;
    }

    public static ResourceLocation getId(MiningSkillCardItem.Type type) {
        return FTBUltimineAddition.id("mine_go_juice_%s".formatted(type.isData() ? MiningSkillCardItem.Type.GENERIC.getId() : type.getId()));
    }

    /** The effect of a type's juice: its own for a built-in tool, the generic one for a data pack type. */
    public static Optional<Holder.Reference<MobEffect>> holder(MiningSkillCardItem.Type type) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(getId(type));
    }

    /** The type an effect is the juice of, for this player (null when it isn't a juice, or its type is gone). */
    public static MiningSkillCardItem.@Nullable Type typeOf(MobEffect effect, Player player) {
        if (!(effect instanceof MineGoJuiceEffect juice)) return null;
        if (juice.type != MiningSkillCardItem.Type.GENERIC) return juice.type;
        MiningSkillCardItem.Type type = MiningSkillCardItem.Type.byId(ServicePlatform.get().players().getAbilityData(player).getGenericJuice());
        return type != null && type.isData() ? type : null;
    }

    /** Whether the player is under this type's juice. */
    public static boolean has(Player player, MiningSkillCardItem.Type type) {
        Optional<Holder.Reference<MobEffect>> effect = holder(type);
        if (effect.isEmpty() || !player.hasEffect(effect.get())) return false;
        return !type.isData() || type.equals(typeOf(effect.get().value(), player));
    }

    /** Gives the player this type's juice. A data pack type's replaces the generic juice they had. */
    public static boolean grant(ServerPlayer player, MiningSkillCardItem.Type type, int ticks, int amplifier) {
        return grant(player, type, new MobEffectInstance(holder(type).orElseThrow(), ticks, amplifier));
    }

    private static boolean grant(ServerPlayer player, MiningSkillCardItem.Type type, MobEffectInstance instance) {
        if (type.isData() && !type.getId().equals(ServicePlatform.get().players().getAbilityData(player).getGenericJuice())) {
            // Another type's generic juice ends here: the new one mustn't inherit its time.
            player.removeEffect(instance.getEffect());
            ServicePlatform.get().players().modifyAbilityData(player, data -> data.setGenericJuice(type.getId()));
        }
        return player.addEffect(instance);
    }

    /** The name of a type's juice, like "Mine-Go Juice: Rock Roulette". */
    public static MutableComponent juiceName(MiningSkillCardItem.Type type) {
        if (type.isData()) {
            // "juice_name" is a translation key or plain text, like the type's name. Without one, a language file can
            // still name the juice under the type's key with ".juice" (ultimine_addition.card_type.<namespace>.<path>.juice);
            // failing that, the juice is named after its tool.
            if (!type.getJuiceName().isEmpty()) return Component.translatable(type.getJuiceName());
            String key = type.translationKey() + ".juice";
            return Language.getInstance().has(key) ? Component.translatable(key)
                    : Component.translatable("effect.ultimine_addition.mine_go_juice_generic.of", type.displayName());
        }
        return holder(type).map(effect -> effect.value().getDisplayName().copy())
                .orElseGet(() -> Component.translatable("info.ultimine_addition.notice.locked.juice"));
    }

    // The Mastered card's bonus: a juice renewed every tick while the card is carried.
    public static void giveEffect(ServerPlayer player, MiningSkillCardItem.Type type) {
        Optional<Holder.Reference<MobEffect>> effect = holder(type);
        if (effect.isEmpty() || has(player, type)) return;
        // A data pack type doesn't take the generic juice over from another type the player drank.
        if (type.isData() && player.hasEffect(effect.get())) return;
        grant(player, type, new MobEffectInstance(effect.get(), 20, 2, false, false, false));
    }
}
