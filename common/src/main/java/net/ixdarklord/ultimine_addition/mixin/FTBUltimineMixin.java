package net.ixdarklord.ultimine_addition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.utils.ItemCollector;
import net.ixdarklord.ultimine_addition.common.undo.UltimineInProgress;
import net.ixdarklord.ultimine_addition.common.undo.UltimineUndo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.Container;
import net.ixdarklord.ultimine_addition.common.progression.CardPerk;
import net.ixdarklord.ultimine_addition.common.progression.UltimineStats;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import java.util.ArrayList;
import java.util.List;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.IntSupplier;

@Mixin(value = FTBUltimine.class)
public abstract class FTBUltimineMixin {
    // The operation being carried out (whose it is, how many blocks it broke, its drops) is in UltimineInProgress: since
    // FTB Ultimine 26.1.2.6 the blocks are broken in a static method, and the drops are gathered in DropsAccumulator.

    @Redirect(method = "handleBlockBreak", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/config/FTBUltimineServerConfig;getMaxBlocks(Lnet/minecraft/server/level/ServerPlayer;)I"))
    private int UA$Redirect$blockBroken(ServerPlayer player) {
        return FTBUltimineIntegration.getMaxBlocks(player);
    }

    @Redirect(method = "blockRightClick", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/config/FTBUltimineServerConfig;getMaxBlocks(Lnet/minecraft/server/level/ServerPlayer;)I"))
    private int UA$Redirect$blockRightClick(ServerPlayer player) {
        return FTBUltimineIntegration.getMaxBlocks(player);
    }

    @Redirect(method = "playerTick", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/FTBUltiminePlayerData;checkBlocks(Lnet/minecraft/server/level/ServerPlayer;ZLjava/util/function/IntSupplier;)V"))
    private void UA$Redirect$playerTick(FTBUltiminePlayerData data, ServerPlayer player, boolean sendUpdate, IntSupplier maxBlocks) {
        data.checkBlocks(player, sendUpdate, () -> FTBUltimineIntegration.getMaxBlocks(player));
    }

    // --- Undo recording (see UltimineUndo) ---

    // Just before the blocks are broken (the drops accumulator has started).
    @Inject(method = "handleBlockBreak", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/FTBUltimine;breakBlocksInShape(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/phys/BlockHitResult;Ldev/ftb/mods/ftbultimine/FTBUltiminePlayerData;)V"))
    private void UA$undoBegin(LevelAccessor level, BlockPos origPos, BlockState state, ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        UltimineInProgress.begin(player);
        UltimineUndo.begin(player, origPos);
    }

    // The Sated and Tireless perks: less or none of the hunger FTB Ultimine adds for each block.
    @WrapOperation(method = "breakBlocksInShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"))
    private static void UA$perkHunger(ServerPlayer player, float exhaustion, Operation<Void> original) {
        float factor = CardPerk.hungerFactor(player);
        if (factor > 0.0F) original.call(player, exhaustion * factor);
    }

    @WrapOperation(method = "breakBlocksInShape", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/FTBUltimine;tryBreakOneBlock(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Ldev/ftb/mods/ftbultimine/api/shape/Shape;Lnet/minecraft/world/phys/BlockHitResult;)Z"))
    private static boolean UA$undoRecordBlock(ServerPlayer player, BlockPos pos, BlockState state, Shape shape, BlockHitResult hit, Operation<Boolean> original) {
        BlockState before = player.level().getBlockState(pos);
        // A container's contents, and the drops this one break adds to FTB's collector, so undo can leave them out.
        List<ItemStack> contents = new ArrayList<>();
        if (player.level().getBlockEntity(pos) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty()) contents.add(container.getItem(i).copy());
            }
        }
        ItemCollector collector = UltimineInProgress.collector;
        List<ItemStack> collected = collector != null ? ((ItemCollectorAccessor) collector).ua$getItems() : List.of();
        int collectedBefore = collected.size();
        BlockState[] neighbours = UltimineUndo.neighbours(player.level(), pos);
        boolean result = original.call(player, pos, state, shape, hit);
        if (result) UltimineInProgress.broken++;
        List<ItemStack> blockDrops = collected.size() > collectedBefore ? List.copyOf(collected.subList(collectedBefore, collected.size())) : List.of();
        UltimineUndo.recordBlock(player, pos, before, neighbours, contents, blockDrops);
        return result;
    }

    // An Ultimine with a tool's right-click: what the blocks were and what they became, for undo.
    @WrapOperation(method = "blockRightClick", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/rightclick/RightClickDispatcher;dispatchRightClick(Ldev/ftb/mods/ftbultimine/api/shape/ShapeContext;Lnet/minecraft/world/InteractionHand;Ldev/ftb/mods/ftbultimine/FTBUltiminePlayerData;)I"))
    private int UA$undoRecordRightClick(dev.ftb.mods.ftbultimine.rightclick.RightClickDispatcher dispatcher, dev.ftb.mods.ftbultimine.api.shape.ShapeContext context,
                                        net.minecraft.world.InteractionHand hand, FTBUltiminePlayerData data, Operation<Integer> original) {
        ServerPlayer player = context.player();
        List<BlockPos> positions = List.copyOf(data.cachedPositions());
        List<BlockState> before = positions.stream().map(pos -> player.level().getBlockState(pos)).toList();
        int result = original.call(dispatcher, context, hand, data);
        if (result > 0) {
            net.ixdarklord.ultimine_addition.common.progression.JuicePause.onUltimine(player);
            List<BlockState> after = positions.stream().map(pos -> player.level().getBlockState(pos)).toList();
            UltimineUndo.recordRevert(player, context.origPos(), positions, before, after);
        }
        return result;
    }

    @Inject(method = "handleBlockBreak", at = @At("RETURN"))
    private void UA$undoFinish(LevelAccessor level, BlockPos origPos, BlockState state, ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        UltimineUndo.finish(player, cir.getReturnValueZ());
        // Only the outer call that did the work (see UltimineUndo.finish).
        if (cir.getReturnValueZ() && UltimineInProgress.player == player) {
            int broken = UltimineInProgress.broken;
            UltimineStats.onUltimine(player, broken);
            net.ixdarklord.ultimine_addition.common.progression.JuicePause.onUltimine(player);
            net.ixdarklord.ultimine_addition.common.progression.ToolAffinity.add(player.getMainHandItem(), broken);
            // A chime for a big one, higher the bigger it was.
            if (broken >= 8 && UAServerConfig.ULTIMINE_SOUND.get()) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 0.8F + Math.min(broken, 64) / 64.0F * 0.8F);
            }
            // The Vigor and Frenzy perks: a moment of Haste after the work (Frenzy's is stronger).
            boolean frenzy = CardPerk.FRENZY.has(player);
            if (broken > 1 && (frenzy || CardPerk.VIGOR.has(player))) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HASTE, CardPerk.VIGOR_TICKS, frenzy ? 1 : 0, true, false, true));
            }
            // The Insight perk: experience for the work.
            if (broken >= CardPerk.INSIGHT_BLOCKS && CardPerk.INSIGHT.has(player)) player.giveExperiencePoints(broken / CardPerk.INSIGHT_BLOCKS);
            // The Thrifty perk: part of the wear this Ultimine put on the tool is given back (a tool that broke is gone).
            ItemStack tool = UltimineInProgress.tool;
            int toolDamage = UltimineInProgress.toolDamage;
            if (!tool.isEmpty() && tool == player.getMainHandItem() && tool.getDamageValue() > toolDamage && CardPerk.THRIFTY.has(player)) {
                tool.setDamageValue(tool.getDamageValue() - (int) ((tool.getDamageValue() - toolDamage) * CardPerk.THRIFTY_FACTOR));
            }
            UltimineInProgress.clear();
        }
    }
}
