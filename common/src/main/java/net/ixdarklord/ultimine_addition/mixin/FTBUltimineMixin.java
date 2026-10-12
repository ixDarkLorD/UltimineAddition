package net.ixdarklord.ultimine_addition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.utils.ItemCollector;
import net.ixdarklord.ultimine_addition.common.undo.UltimineUndo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.ftb.mods.ftblibrary.util.Lazy;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import net.ixdarklord.ultimine_addition.common.progression.CardPerk;
import net.ixdarklord.ultimine_addition.common.progression.UltimineStats;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    @Shadow @Final private Lazy<ItemCollector> tempBlockDropsList;
    // The operation being carried out: whose it is, and how many blocks it has broken.
    @Unique private ServerPlayer ua$player;
    @Unique private int ua$broken;
    @Unique private ItemStack ua$tool = ItemStack.EMPTY;
    @Unique private int ua$toolDamage;

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

    @Inject(method = "handleBlockBreak", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/utils/ItemCollector;clear()V"))
    private void UA$undoBegin(LevelAccessor level, BlockPos origPos, BlockState state, ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        this.ua$player = player;
        this.ua$broken = 0;
        this.ua$tool = player.getMainHandItem();
        this.ua$toolDamage = this.ua$tool.getDamageValue();
        UltimineUndo.begin(player, origPos);
    }

    // The Sated and Tireless perks: less or none of the hunger FTB Ultimine adds for each block.
    @WrapOperation(method = "handleBlockBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"))
    private void UA$perkHunger(ServerPlayer player, float exhaustion, Operation<Void> original) {
        float factor = CardPerk.hungerFactor(player);
        if (factor > 0.0F) original.call(player, exhaustion * factor);
    }

    @WrapOperation(method = "handleBlockBreak", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/FTBUltimine;tryBreakBlock(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Ldev/ftb/mods/ftbultimine/api/shape/Shape;Lnet/minecraft/world/phys/BlockHitResult;)Z"))
    private boolean UA$undoRecordBlock(ServerPlayer player, BlockPos pos, BlockState state, Shape shape, BlockHitResult hit, Operation<Boolean> original) {
        BlockState before = player.level().getBlockState(pos);
        // A container's contents, and the drops this one break adds to FTB's collector, so undo can leave them out.
        List<ItemStack> contents = new ArrayList<>();
        if (player.level().getBlockEntity(pos) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty()) contents.add(container.getItem(i).copy());
            }
        }
        List<ItemStack> collected = ((ItemCollectorAccessor) this.tempBlockDropsList.get()).ua$getItems();
        int collectedBefore = collected.size();
        BlockState[] neighbours = UltimineUndo.neighbours(player.level(), pos);
        boolean result = original.call(player, pos, state, shape, hit);
        if (result) this.ua$broken++;
        List<ItemStack> blockDrops = collected.size() > collectedBefore ? List.copyOf(collected.subList(collectedBefore, collected.size())) : List.of();
        UltimineUndo.recordBlock(player, pos, before, neighbours, contents, blockDrops);
        return result;
    }

    // The merged drops, and the item entities FTB Ultimine spawns for them at the origin.
    @WrapOperation(method = "handleBlockBreak", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/utils/ItemCollector;drop(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void UA$undoRecordDrops(ItemCollector collector, Level level, BlockPos pos, Operation<Void> original) {
        List<ItemStack> drops = ((ItemCollectorAccessor) collector).ua$getItems().stream().map(ItemStack::copy).toList();
        AABB area = new AABB(pos).inflate(1.5);
        Set<ItemEntity> before = new HashSet<>(level.getEntitiesOfClass(ItemEntity.class, area));
        original.call(collector, level, pos);
        List<ItemEntity> spawned = level.getEntitiesOfClass(ItemEntity.class, area, entity -> !before.contains(entity));
        UltimineUndo.recordDrops(drops, spawned);
        // Straight to the player, where the server has that on: picked up as if walked over, so what doesn't fit stays
        // on the ground (and undo finds the rest in the inventory).
        ServerPlayer player = this.ua$player;
        if (player != null && !player.isSpectator() && (UAServerConfig.DROPS_TO_INVENTORY.get() || CardPerk.MAGNET.has(player))) {
            for (ItemEntity entity : spawned) {
                if (!entity.isAlive()) continue;
                entity.setNoPickUpDelay();
                entity.playerTouch(player);
            }
        }
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
        if (cir.getReturnValueZ() && this.ua$player == player) {
            UltimineStats.onUltimine(player, this.ua$broken);
            net.ixdarklord.ultimine_addition.common.progression.JuicePause.onUltimine(player);
            net.ixdarklord.ultimine_addition.common.progression.ToolAffinity.add(player.getMainHandItem(), this.ua$broken);
            // A chime for a big one, higher the bigger it was.
            if (this.ua$broken >= 8 && UAServerConfig.ULTIMINE_SOUND.get()) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 0.8F + Math.min(this.ua$broken, 64) / 64.0F * 0.8F);
            }
            // The Vigor and Frenzy perks: a moment of Haste after the work (Frenzy's is stronger).
            boolean frenzy = CardPerk.FRENZY.has(player);
            if (this.ua$broken > 1 && (frenzy || CardPerk.VIGOR.has(player))) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HASTE, CardPerk.VIGOR_TICKS, frenzy ? 1 : 0, true, false, true));
            }
            // The Insight perk: experience for the work.
            if (this.ua$broken >= CardPerk.INSIGHT_BLOCKS && CardPerk.INSIGHT.has(player)) player.giveExperiencePoints(this.ua$broken / CardPerk.INSIGHT_BLOCKS);
            // The Thrifty perk: part of the wear this Ultimine put on the tool is given back (a tool that broke is gone).
            ItemStack tool = this.ua$tool;
            if (!tool.isEmpty() && tool == player.getMainHandItem() && tool.getDamageValue() > this.ua$toolDamage && CardPerk.THRIFTY.has(player)) {
                tool.setDamageValue(tool.getDamageValue() - (int) ((tool.getDamageValue() - this.ua$toolDamage) * CardPerk.THRIFTY_FACTOR));
            }
            this.ua$tool = ItemStack.EMPTY;
            this.ua$player = null;
        }
    }
}
