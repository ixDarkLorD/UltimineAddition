package net.ixdarklord.ultimine_addition.item;

import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.helper.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MinerCertificate extends Item {
    public MinerCertificate(Properties properties) {
        super(properties);
    }
    private static final int PARTICLE_COUNT = 60;
    private static boolean PLAY_ANIMATION = false;
    private static boolean CONSUME_ITEM = false;
    private static boolean PLAY_SOUND = false;

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide) {
            if (isAccomplished(stack)) {
                if (Services.PLATFORM.isPlayerCapable(player)) {
                    player.displayClientMessage(Component.translatable("info.ultimine_addition.obtained_already", "\u2714").withStyle(ChatFormatting.YELLOW), true);
                    return InteractionResultHolder.fail(stack);
                } else {
                    Services.PLATFORM.setPlayerCapability(player, true);
                    PLAY_ANIMATION = true;
                    CONSUME_ITEM = true;
                    player.displayClientMessage(Component.translatable("info.ultimine_addition.obtain").withStyle(ChatFormatting.GREEN), true);
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            }
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotID, boolean isSelected) {
        if (!level.isClientSide()) {
            if (ConfigHandler.COMMON.QUEST_MODE.get()) {
                Services.PLATFORM.setRequiredAmount(stack, slotID, ConfigHandler.COMMON.REQUIRED_AMOUNT.get(), (Player) entity);
            } else {
                Services.PLATFORM.setMinedBlocks(stack, slotID, 1, (Player) entity);
                Services.PLATFORM.setAccomplished(stack, slotID, true, (Player) entity);
            }
            if (isAccomplished(stack) && !Services.PLATFORM.isAccomplished(stack)) {
                Services.PLATFORM.setAccomplished(stack, slotID, true, (Player) entity);
                PLAY_SOUND = true;
            }
            if (CONSUME_ITEM && !PLAY_ANIMATION) {
                if (!((Player) entity).isCreative()) stack.shrink(1);
                CONSUME_ITEM = false;
            }
        }
        if (level.isClientSide()) {
            if (PLAY_ANIMATION) {
                Services.PLATFORM.sendCertificateEffect(stack, (Player) entity);
                PLAY_ANIMATION = false;
            }
            if (PLAY_SOUND) {
                LocalPlayer player = (LocalPlayer) entity;
                player.playSound(SoundEvents.NOTE_BLOCK_BELL, 1.0F, 1.0F);
                player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
                PLAY_SOUND = false;
            }
        }

        super.inventoryTick(stack, level, entity, slotID, isSelected);
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        int MINE_COUNT = Services.PLATFORM.getMinedBlocks(stack);
        int REQUIRED_AMOUNT = Services.PLATFORM.getRequiredAmount(stack);
        String SEALED = Component.translatable("tooltip.ultimine_addition.certificate.sealed").getString();
        String OPENED = Component.translatable("tooltip.ultimine_addition.certificate.opened").getString();
        String QUEST1 = Component.translatable("tooltip.ultimine_addition.quest1", MINE_COUNT).getString();

        if (isAccomplished(stack)) {
            tooltipComponents.add(Component.literal("\u00A78\u300E\u00A76" + OPENED + "\u00A78\u300F"));
        } else {
            tooltipComponents.add(Component.literal("\u00A78\u300E\u00A75" + SEALED + "\u00A78\u300F"));
        }
        if (REQUIRED_AMOUNT == 0) {
            tooltipComponents.add(Component.translatable("tooltip.ultimine_addition.info"));
            return;
        }

        if (!Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("tooltip.ultimine_addition.press_shift").withStyle(ChatFormatting.YELLOW));
            return;
        }

        if (!isAccomplished(stack)) tooltipComponents.add(Component.translatable("tooltip.ultimine_addition.quest1.info", REQUIRED_AMOUNT).withStyle(ChatFormatting.DARK_AQUA));
        tooltipComponents.add(Component.literal("\u00A77\u27A4 \u00A7b" + QUEST1));

        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return isAccomplished(stack);
    }

    public static boolean isAccomplished(ItemStack stack) {
        int MINE_COUNT = Services.PLATFORM.getMinedBlocks(stack);
        int REQUIRED_AMOUNT = Services.PLATFORM.getRequiredAmount(stack);
        return MINE_COUNT >= (REQUIRED_AMOUNT != 0 ? REQUIRED_AMOUNT : 1);
    }

    public static void onBreakBlock(BlockPos pos, Player player) {
        BlockState blockState = player.getLevel().getBlockState(pos);
        if (blockState.is(Services.PLATFORM.oresTag())) {

            NonNullList<ItemStack> items = player.getInventory().items;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getItem() == ItemsList.MINER_CERTIFICATE) {
                    Services.PLATFORM.addMinedBlocks(items.get(i), i, 1, player);
                }
            }
        }
    }
    public static void playParticleAndSound(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            player.getLevel().sendParticles(player, Services.PLATFORM.getCelebrateParticle(), true,
                    player.getX(), player.getY()+0.15d, player.getZ(),
                    PARTICLE_COUNT, 0.5d, 1.0d, 0.5d, 0.02d
            );
            player.getLevel().sendParticles(player, ParticleTypes.TOTEM_OF_UNDYING, true,
                    player.getX(), player.getY()+0.15d, player.getZ(),
                    PARTICLE_COUNT, 0.85d, 1.0d, 0.85d, 0.02d
            );
            player.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, player.getSoundSource(), 0.25F, 2.5F);
            player.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_CHIME, player.getSoundSource(), 1.0F, 1.0F);
        }
    }
    public static void playAnimation(ItemStack stack, Entity entity) {
        var MC = Minecraft.getInstance();
        if (entity == MC.player) {
            MC.gameRenderer.displayItemActivation(stack);
        }
    }
}
