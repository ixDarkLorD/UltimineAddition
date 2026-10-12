package net.ixdarklord.ultimine_addition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.ItemCollection;
import dev.ftb.mods.ftbultimine.shape.ShapeContext;
import net.ixdarklord.ultimine_addition.common.progression.CardPerk;
import net.ixdarklord.ultimine_addition.common.progression.JuicePause;
import net.ixdarklord.ultimine_addition.common.progression.ToolAffinity;
import net.ixdarklord.ultimine_addition.common.progression.UltimineStats;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.common.undo.UltimineUndo;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

// FTB Ultimine 2001: the break handler is blockBroken (an Architectury block-break listener). It breaks each block with
// the player's ServerPlayerGameMode.destroyBlock, and collects the drops of an operation in a fresh ItemCollection.
// (The injection targets name Minecraft classes, so they're remapped through the refmap; only the shadow isn't.)
@Mixin(value = FTBUltimine.class)
public abstract class FTBUltimineMixin {
    @Shadow(remap = false) private ItemCollection tempBlockDropsList;

    @Redirect(method = "blockBroken", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/config/FTBUltimineServerConfig;getMaxBlocks(Lnet/minecraft/server/level/ServerPlayer;)I"))
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

    // FTB's blockBroken takes and returns Architectury types (IntValue, EventResult); only the arguments used are
    // captured here, so this mod doesn't refer to Architectury itself.
    @Inject(method = "blockBroken", at = @At(value = "NEW", target = "dev/ftb/mods/ftbultimine/ItemCollection"))
    private void UA$undoBegin(CallbackInfoReturnable<?> cir, @Local(argsOnly = true) ServerPlayer player, @Local(argsOnly = true) BlockPos origPos,
                              @Share("undoRan") LocalBooleanRef ran, @Share("tool") LocalRef<ItemStack> tool, @Share("toolDamage") LocalIntRef toolDamage) {
        ran.set(true);
        // The tool the operation starts with, and its wear, for the Thrifty perk.
        tool.set(player.getMainHandItem());
        toolDamage.set(player.getMainHandItem().getDamageValue());
        UltimineUndo.begin(player, origPos);
    }

    // The Sated and Tireless perks: less or none of the hunger FTB Ultimine adds for each block.
    @WrapOperation(method = "blockBroken", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"))
    private void UA$perkHunger(ServerPlayer player, float exhaustion, Operation<Void> original) {
        float factor = CardPerk.hungerFactor(player);
        if (factor > 0.0F) original.call(player, exhaustion * factor);
    }

    @WrapOperation(method = "blockBroken", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean UA$undoRecordBlock(ServerPlayerGameMode gameMode, BlockPos pos, Operation<Boolean> original, @Local(argsOnly = true) ServerPlayer player,
                                       @Share("broken") LocalIntRef broken) {
        BlockState before = player.level().getBlockState(pos);
        // A container's contents, and the drops this one break adds to FTB's collection, so undo can leave them out.
        List<ItemStack> contents = new ArrayList<>();
        if (player.level().getBlockEntity(pos) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty()) contents.add(container.getItem(i).copy());
            }
        }
        List<ItemStack> collected = ((ItemCollectorAccessor) this.tempBlockDropsList).ua$getItems();
        int collectedBefore = collected.size();
        BlockState[] neighbours = UltimineUndo.neighbours(player.serverLevel(), pos);
        boolean result = original.call(gameMode, pos);
        if (result) broken.set(broken.get() + 1);
        List<ItemStack> blockDrops = collected.size() > collectedBefore ? List.copyOf(collected.subList(collectedBefore, collected.size())) : List.of();
        UltimineUndo.recordBlock(player, pos, before, neighbours, contents, blockDrops);
        return result;
    }

    // The merged drops, and the item entities FTB Ultimine spawns for them at the origin.
    @WrapOperation(method = "blockBroken", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/ItemCollection;drop(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void UA$undoRecordDrops(ItemCollection collector, Level level, BlockPos pos, Operation<Void> original, @Local(argsOnly = true) ServerPlayer player) {
        List<ItemStack> drops = ((ItemCollectorAccessor) collector).ua$getItems().stream().map(ItemStack::copy).toList();
        AABB area = new AABB(pos).inflate(1.5);
        Set<ItemEntity> before = new HashSet<>(level.getEntitiesOfClass(ItemEntity.class, area));
        original.call(collector, level, pos);
        List<ItemEntity> spawned = level.getEntitiesOfClass(ItemEntity.class, area, entity -> !before.contains(entity));
        UltimineUndo.recordDrops(drops, spawned);
        // Straight to the player, where the server has that on: picked up as if walked over, so what doesn't fit stays
        // on the ground (and undo finds the rest in the inventory).
        if (!player.isSpectator() && (UAServerConfig.DROPS_TO_INVENTORY.get() || CardPerk.MAGNET.has(player))) {
            for (ItemEntity entity : spawned) {
                if (!entity.isAlive()) continue;
                entity.setNoPickUpDelay();
                entity.playerTouch(player);
            }
        }
    }

    // An Ultimine with a tool's right-click: what the blocks were and what they became, for undo. (FTB Ultimine 2001
    // hands the right-click to its platform method.)
    @WrapOperation(method = "blockRightClick", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/utils/PlatformMethods;blockRightClick(Ldev/ftb/mods/ftbultimine/shape/ShapeContext;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Ldev/ftb/mods/ftbultimine/FTBUltiminePlayerData;)I"))
    private int UA$undoRecordRightClick(ShapeContext context, ServerPlayer player, InteractionHand hand, BlockPos clickPos, Direction face, FTBUltiminePlayerData data, Operation<Integer> original) {
        List<BlockPos> positions = List.copyOf(data.cachedPositions());
        List<BlockState> before = positions.stream().map(pos -> player.level().getBlockState(pos)).toList();
        int result = original.call(context, player, hand, clickPos, face, data);
        if (result > 0) {
            JuicePause.onUltimine(player);
            List<BlockState> after = positions.stream().map(pos -> player.level().getBlockState(pos)).toList();
            UltimineUndo.recordRevert(player, clickPos, positions, before, after);
        }
        return result;
    }

    // Whether an operation ran: FTB returns pass() on every check before building its drop collector, and
    // interruptFalse() once it has (where undoBegin marks this call). The nested block-break events an operation
    // causes are calls of their own, unmarked.
    @Inject(method = "blockBroken", at = @At("RETURN"))
    private void UA$undoFinish(CallbackInfoReturnable<?> cir, @Local(argsOnly = true) ServerPlayer player, @Share("undoRan") LocalBooleanRef ran,
                               @Share("broken") LocalIntRef brokenRef, @Share("tool") LocalRef<ItemStack> toolRef, @Share("toolDamage") LocalIntRef toolDamage) {
        UltimineUndo.finish(player, ran.get());
        // Only the call that did the work (see above).
        if (!ran.get()) return;
        int broken = brokenRef.get();
        UltimineStats.onUltimine(player, broken);
        JuicePause.onUltimine(player);
        ToolAffinity.add(player.getMainHandItem(), broken);
        // A chime for a big one, higher the bigger it was.
        if (broken >= 8 && UAServerConfig.ULTIMINE_SOUND.get()) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 0.5F, 0.8F + Math.min(broken, 64) / 64.0F * 0.8F);
        }
        // The Vigor and Frenzy perks: a moment of Haste after the work (Frenzy's is stronger).
        boolean frenzy = CardPerk.FRENZY.has(player);
        if (broken > 1 && (frenzy || CardPerk.VIGOR.has(player))) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, CardPerk.VIGOR_TICKS, frenzy ? 1 : 0, true, false, true));
        }
        // The Insight perk: experience for the work.
        if (broken >= CardPerk.INSIGHT_BLOCKS && CardPerk.INSIGHT.has(player)) player.giveExperiencePoints(broken / CardPerk.INSIGHT_BLOCKS);
        // The Thrifty perk: part of the wear this Ultimine put on the tool is given back (a tool that broke is gone).
        ItemStack tool = toolRef.get();
        if (tool != null && !tool.isEmpty() && tool == player.getMainHandItem() && tool.getDamageValue() > toolDamage.get() && CardPerk.THRIFTY.has(player)) {
            tool.setDamageValue(tool.getDamageValue() - (int) ((tool.getDamageValue() - toolDamage.get()) * CardPerk.THRIFTY_FACTOR));
        }
    }
}
