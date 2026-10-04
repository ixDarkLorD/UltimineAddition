package net.ixdarklord.ultimine_addition.common.item;

import org.jetbrains.annotations.Nullable;
import net.ixdarklord.coolcatcore.api.utils.ComponentHelper;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class MinerCertificateItem extends DataAbstractItem<MinerCertificateData> {
    public MinerCertificateItem(Properties properties) {
        super(properties, ComponentType.ABILITY);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide()) {
            if (ServicePlatform.get().players().isPlayerUltimineCapable(player) && isAccomplished(stack)) {
                level.playSound(player, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.0F, 0.5F);
                getData(stack).sendClientMessage(player);
            }

            return InteractionResultHolder.pass(stack);
        }

        if (!ServicePlatform.get().players().isPlayerUltimineCapable(player)) {
            if (isAccomplished(stack)) {
                this.playParticleAndSound(player);
                Registration.ULTIMINE_OBTAIN_TRIGGER.get().trigger((ServerPlayer) player);
                getData(stack).playCelebration(true).sendClientMessage(player).sendToClient(ItemUtils.getSlotIndex(usedHand), (ServerPlayer) player).save();
                ServicePlatform.get().players().setPlayerUltimineCapability(player, true);
                if (!player.isCreative()) stack.shrink(1);
                return InteractionResultHolder.success(stack);
            }
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotIndex, boolean isSelected) {
        if (entity instanceof ServerPlayer player)
            getData(stack).tick(ItemUtils.findSlotIndex(player, stack), player);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        Optional<MinerCertificateData.Legacy> legacy = getData(stack).getLegacy();
        legacy.ifPresent(value -> value.createInfoComponent(tooltipComponents, false));

        if (this.isShiftButtonNotPressed(tooltipComponents::add)) return;
        if (legacy.isPresent()) {
            legacy.get().createInfoComponent(tooltipComponents, true);
            return;
        }

        Component component = Component.translatable("tooltip.ultimine_addition.certificate.info").withStyle(ChatFormatting.GRAY);
        List<Component> components = ComponentHelper.splitComponent(component, getSplitterLength());
        tooltipComponents.addAll(components);
    }

    // Sealed, it's a Completion Envelope; the Miner Certificate is what's inside once it's earned.
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return isAccomplished(stack) ? super.getName(stack) : Component.translatable("item.ultimine_addition.completion_envelope");
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return isAccomplished(stack);
    }

    public static boolean isAccomplished(ItemStack stack) {
        return stack.getItem() instanceof MinerCertificateItem item && item.getData(stack).isAccomplished();
    }

    public void playParticleAndSound(Entity entity) {
        final int PARTICLE_COUNT = 80;
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.serverLevel().sendParticles(Registration.CELEBRATE_PARTICLE.get(),
                    serverPlayer.getX(), serverPlayer.getY() + 0.15d, serverPlayer.getZ(),
                    PARTICLE_COUNT, 1.0d, 1.0d, 1.0d, 0.02d
            );
            serverPlayer.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    serverPlayer.getX(), serverPlayer.getY() + 0.15d, serverPlayer.getZ(),
                    PARTICLE_COUNT, 1.0d, 1.0d, 1.0d, 0.02d
            );
            serverPlayer.serverLevel().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.TOTEM_USE, serverPlayer.getSoundSource(), 0.25F, 2.5F);
            serverPlayer.serverLevel().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.NOTE_BLOCK_CHIME.value(), serverPlayer.getSoundSource(), 1.0F, 1.0F);
        }
    }

    @Override
    public MinerCertificateData getData(ItemStack stack) {
        return MinerCertificateData.load(stack);
    }
}
