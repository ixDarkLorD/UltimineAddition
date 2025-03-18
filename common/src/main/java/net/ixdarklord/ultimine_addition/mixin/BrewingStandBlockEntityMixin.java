package net.ixdarklord.ultimine_addition.mixin;

import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.MiningSkillCardPacket;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BrewingStandBlockEntity.class)
abstract class BrewingStandBlockEntityMixin {

    @Shadow @Final private static int INGREDIENT_SLOT;

    @Inject(method = "doBrew", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;", ordinal = 1))
    private static void UA$Inject$updateBrewInv(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            ItemStack stack = items.get(INGREDIENT_SLOT).copy();
            if (stack.getItem() instanceof MiningSkillCardItem item) {
                // Consume potion points
                MiningSkillCardData data = item.getData(stack);
                int oldPoints = data.getPotionPoints();
                data.consumePotionPoint(1).saveData(stack);
                items.set(INGREDIENT_SLOT, stack);

                // Sync data to client
                BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
                if (!(blockEntity instanceof BrewingStandBlockEntity brewingStandBlock)) return;
                List<ServerPlayer> players = serverLevel.getPlayers(brewingStandBlock::stillValid);

                for (ServerPlayer player : players) {
                    UltimineAddition.LOGGER.debug("{} synced potion points! [B:{} / A:{}]", player.getDisplayName().getString(), oldPoints, data.getPotionPoints());
                    PacketHandler.sendToPlayer(new MiningSkillCardPacket.SyncBrewing(stack.copy()), player);
                }
            }
        }
    }

    @Redirect(method = "doBrew", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private static void UA$Redirect$consumePotionPoint(ItemStack stack, int i) {
        if (!(stack.getItem() instanceof MiningSkillCardItem)) {
            stack.shrink(i);
        }
    }
}
