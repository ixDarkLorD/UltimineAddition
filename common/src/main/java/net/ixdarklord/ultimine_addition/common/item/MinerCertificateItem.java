package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcatlib.api.utils.ComponentHelper;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MinerCertificateItem extends DataAbstractItem<MinerCertificateData> {
    public MinerCertificateItem(Item.Properties properties) {
        super(properties, ComponentType.ABILITY);
    }

    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide()) {
            if (ServicePlatform.get().players().isPlayerUltimineCapable(player) && isAccomplished(stack)) {
                level.playLocalSound(player.getOnPos(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.0F, 0.5F, false);
                this.getData(stack).sendClientMessage(player);
            }

            return InteractionResultHolder.pass(stack);
        } else {
            if (player instanceof ServerPlayer serverPlayer) {
                if (!ServicePlatform.get().players().isPlayerUltimineCapable(serverPlayer) && isAccomplished(stack)) {
                    this.playParticleAndSound(player);
                    Registration.ULTIMINE_OBTAIN_TRIGGER.trigger(serverPlayer);
                    this.getData(stack).playCelebration(true).sendClientMessage(player).sendToClient(ItemUtils.getSlotIndex(usedHand), serverPlayer).save();
                    ServicePlatform.get().players().setPlayerUltimineCapability(player, true);
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }

                    return InteractionResultHolder.success(stack);
                }
            }

            return InteractionResultHolder.fail(stack);
        }
    }

    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotIndex, boolean isSelected) {
        if (entity instanceof ServerPlayer player) {
            this.getData(stack).tick(slotIndex, player);
        }

    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        Optional<MinerCertificateData.Legacy> legacy = this.getData(stack).getLegacy();
        legacy.ifPresent((value) -> value.createInfoComponent(tooltipComponents, false));
        if (!this.isShiftButtonNotPressed(tooltipComponents)) {
            if (legacy.isPresent()) {
                legacy.get().createInfoComponent(tooltipComponents, true);
            } else {
                Component component = Component.translatable("tooltip.ultimine_addition.certificate.info").withStyle(ChatFormatting.GRAY);
                List<Component> components = ComponentHelper.splitComponent(component, this.getSplitterLength());
                tooltipComponents.addAll(components);
            }
        }
    }

    public boolean isFoil(@NotNull ItemStack stack) {
        return isAccomplished(stack);
    }

    public static boolean isAccomplished(ItemStack stack) {
        if (stack.getItem() instanceof MinerCertificateItem item) {
            return item.getData(stack).isAccomplished();
        }
        return false;
    }

    public void playParticleAndSound(Entity entity) {
        int PARTICLE_COUNT = 100;
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.serverLevel().sendParticles(Registration.CELEBRATE_PARTICLE.get(), serverPlayer.getX(), serverPlayer.getY() + (double) 0.5F, serverPlayer.getZ(), PARTICLE_COUNT, 1.0F, 1.0F, 1.0F, 0.05);
            serverPlayer.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, serverPlayer.getX(), serverPlayer.getY() + (double) 0.5F, serverPlayer.getZ(), PARTICLE_COUNT, 1.0F, 1.0F, 1.0F, 0.05);
            serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.TOTEM_USE, serverPlayer.getSoundSource(), 0.25F, 2.5F);
            serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.NOTE_BLOCK_CHIME.value(), serverPlayer.getSoundSource(), 1.0F, 1.0F);
        }

    }

    public MinerCertificateData getData(ItemStack stack) {
        return MinerCertificateData.load(stack);
    }
}
