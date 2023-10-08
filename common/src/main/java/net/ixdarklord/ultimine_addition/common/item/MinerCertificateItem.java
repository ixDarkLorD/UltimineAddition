package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcat_lib.util.ScreenUtils;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MinerCertificateItem extends DataAbstractItem<MinerCertificateData> {
    public MinerCertificateItem(Properties properties) {
        super(properties, ComponentType.ABILITY);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide() && getData(stack).isAccomplished()) {
            if (!ServicePlatform.Players.isPlayerUltimineCapable(player)) {
                this.playParticleAndSound(player);
                Registration.OBTAIN_ULTIMINE_TRIGGER.trigger((ServerPlayer) player);
                getData(stack).sendToClient((ServerPlayer) player).createCelebration(true).saveData(stack);
                ServicePlatform.Players.setPlayerUltimineCapability(player, true);
                if (!player.isCreative()) stack.shrink(1);
                return InteractionResultHolder.success(stack);
            } else {
                getData(stack).sendToClient((ServerPlayer) player).createCelebration(true).saveData(stack);
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotID, boolean isSelected) {
        if (!stack.hasTag() && entity instanceof ServerPlayer player) {
            getData(stack).sendToClient(player).pickUpSound(true).setAccomplished(true).saveData(stack);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (this.isShiftButtonNotPressed(tooltipComponents)) return;
        Component component = Component.translatable("tooltip.ultimine_addition.certificate.info").withStyle(ChatFormatting.GRAY);
        List<Component> components = ScreenUtils.splitComponent(component, getSplitterLength());
        tooltipComponents.addAll(components);
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
            serverPlayer.getLevel().sendParticles(Registration.CELEBRATE_PARTICLE.get(),
                    serverPlayer.getX(), serverPlayer.getY()+0.15d, serverPlayer.getZ(),
                    PARTICLE_COUNT, 1.0d, 1.0d, 1.0d, 0.02d
            );
            serverPlayer.getLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    serverPlayer.getX(), serverPlayer.getY()+0.15d, serverPlayer.getZ(),
                    PARTICLE_COUNT, 1.0d, 1.0d, 1.0d, 0.02d
            );
            serverPlayer.getLevel().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.TOTEM_USE, serverPlayer.getSoundSource(), 0.25F, 2.5F);
            serverPlayer.getLevel().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.NOTE_BLOCK_CHIME, serverPlayer.getSoundSource(), 1.0F, 1.0F);
        }
    }

    @Override
    public MinerCertificateData getData(ItemStack stack) {
        return new MinerCertificateData().loadData(stack);
    }
}
