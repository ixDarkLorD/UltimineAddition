package net.ixdarklord.ultimine_addition.common.progression;

import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Rewards for a Mining Skill Card tiering up through its challenges: a Shape Certificate and a short taste of Ultimine.
public final class ProgressionRewards {
    private ProgressionRewards() {}

    public static void checkTierUp(ServerPlayer player, MiningSkillCardData card, MiningSkillCardItem.Tier before) {
        if (card.getTier().getValue() > before.getValue()) onTierUp(player, card, card.getTier());
    }

    private static void onTierUp(ServerPlayer player, MiningSkillCardData card, MiningSkillCardItem.Tier tier) {
        List<Component> lines = new ArrayList<>();
        ItemStack icon = card.getStack() == null ? ItemStack.EMPTY : card.getStack().copyWithCount(1);

        // The certificate is claimed from the tier's box in the Skills Record.
        ShapeCertificateItem certificate = ShapeCertificateItem.forTier(tier);
        if (certificate != null && card.canClaimCertificate(tier) && !ShapeCertificateItem.claimPool(player, card.getType().getId(), tier).isEmpty()) {
            ItemStack reward = certificate.create(card.getType());
            icon = reward.copy();
            lines.add(Component.translatable("info.ultimine_addition.notice.reward.certificate", reward.getHoverName().copy().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY));
        }

        int seconds = UAServerConfig.TIER_UP_TASTE_DURATION.get();
        if (seconds > 0 && giveTaste(player, card.getType(), tier, seconds)) {
            lines.add(Component.translatable("info.ultimine_addition.notice.reward.taste", seconds).withStyle(ChatFormatting.GRAY));
        }

        Component title = Component.translatable("info.ultimine_addition.notice.reward", tier.getDisplayName());
        new UltimineNotice(UltimineNotice.Kind.REWARD, title, lines, icon).send(player);
        player.level().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.6, 0.8, 0.6, 0.15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, player.getSoundSource(), 0.8F, 1.2F);
    }

    private static boolean giveTaste(ServerPlayer player, MiningSkillCardItem.Type type, MiningSkillCardItem.Tier tier, int seconds) {
        if (type == MiningSkillCardItem.Type.EMPTY) return false;
        Optional<Holder.Reference<MobEffect>> effect = BuiltInRegistries.MOB_EFFECT.get(MineGoJuiceEffect.getId(type));
        if (effect.isEmpty()) return false;
        int amplifier = Math.clamp(tier.getValue(), 1, MiningSkillCardItem.Tier.Adept.getValue()) - 1;
        return player.addEffect(new MobEffectInstance(effect.get(), seconds * 20, amplifier));
    }
}
