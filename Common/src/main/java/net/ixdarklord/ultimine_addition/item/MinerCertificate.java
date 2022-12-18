package net.ixdarklord.ultimine_addition.item;

import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.helper.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MinerCertificate extends Item {
    public MinerCertificate(Properties properties) {
        super(properties);
    }

    private static Block LAST_BLOCK;

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (isAccomplished(stack) && !level.isClientSide) {
            if (Services.PLATFORM.isPlayerCapable(player)) {
                player.displayClientMessage(Component.translatable("info.ultimine_addition.obtained_already", "\u2714").withStyle(ChatFormatting.YELLOW), true);
                return InteractionResultHolder.fail(stack);
            } else {
                Services.PLATFORM.setPlayerCapability(player, true);
                Services.PLATFORM.sendCertificateEffect(stack, player);
                if (!player.isCreative()) stack.shrink(1);
                player.displayClientMessage(Component.translatable("info.ultimine_addition.obtain").withStyle(ChatFormatting.GREEN), true);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (ConfigHandler.COMMON.QUEST_MODE.get()) {
            Services.PLATFORM.setRequiredAmount(stack, ConfigHandler.COMMON.REQUIRED_AMOUNT.get(), (Player) entity);
        } else {
            Services.PLATFORM.setMinedBlocks(stack, 1, (Player) entity);
            Services.PLATFORM.setAccomplished(stack, true, (Player) entity);
        }
        if (isAccomplished(stack) && !Services.PLATFORM.isAccomplished(stack)) {
            Player player = (Player) entity;
            player.playSound(SoundEvents.NOTE_BLOCK_BELL, 1.0F, 1.0F);
            player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
            Services.PLATFORM.setAccomplished(stack, true, player);
        }

        super.inventoryTick(stack, level, entity, slotId, isSelected);
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
        boolean RESULT = MINE_COUNT >= (REQUIRED_AMOUNT != 0 ? REQUIRED_AMOUNT : 1);
        return RESULT;
    }

    public static void onBreakBlock(Player player) {
        if (LAST_BLOCK == Blocks.COBBLESTONE) {
            Constants.LOGGER.warn("It was a Cobblestone");
        }
        if (Services.PLATFORM.getPlatformName().equals("Forge")) {
            Services.PLATFORM.addMinedBlocks(new ItemStack(ItemsList.MINER_CERTIFICATE), 1, player);
            return;
        }
        NonNullList<ItemStack> items = player.getInventory().items;
        for (var item : items) {
            if (item.sameItem(new ItemStack(ItemsList.MINER_CERTIFICATE))) {
                Services.PLATFORM.addMinedBlocks(item, 1, player);
            }
        }
    }
    public static void checkingBlockInFront() {
        Minecraft MC = Minecraft.getInstance();
        if (MC.player != null) {
            HitResult block = MC.player.pick(20.0D, 0.0F, false);
            if (block.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult)block).getBlockPos();
                BlockState blockState = MC.player.level.getBlockState(blockPos);
                boolean isBlockChanged = false;
                if (LAST_BLOCK == null) {
                    LAST_BLOCK = blockState.getBlock();
                    isBlockChanged = true;
                }
                if (LAST_BLOCK != blockState.getBlock()) {
                    LAST_BLOCK = blockState.getBlock();
                    isBlockChanged = true;
                }
                if (isBlockChanged && Services.PLATFORM.isDevelopmentEnvironment())
                    Constants.LOGGER.warn("The block you looked at: {}", blockState.getBlock().getName().getString());
            }
        }
    }
    public static void playAnimation(ItemStack stack, Entity entity) {
        var MC = Minecraft.getInstance();
        MC.particleEngine.createTrackingEmitter(entity, Services.PLATFORM.getCelebrateParticle());
        MC.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING);
        var level = MC.level;
        if (level != null) {
            level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 0.25F, 2.5F, false);
            level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.NOTE_BLOCK_CHIME, entity.getSoundSource(), 1.0F, 1.0F, false);
        }

        if (entity == MC.player) {
            MC.gameRenderer.displayItemActivation(stack);
        }
    }
}
