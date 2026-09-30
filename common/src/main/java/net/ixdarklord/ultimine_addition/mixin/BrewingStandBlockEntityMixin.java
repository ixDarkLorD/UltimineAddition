package net.ixdarklord.ultimine_addition.mixin;

import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.MiningSkillCardPayload;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordSavedData;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordSync;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
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
public abstract class BrewingStandBlockEntityMixin {

    @Shadow @Final private static int INGREDIENT_SLOT;

    // At the end of the brew, once the ingredient slot is set back: 1.20.1 Forge brews through BrewingRecipeRegistry (the
    // vanilla mixing loop and its NonNullList.set calls aren't in doBrew there) and may return early when the brew is
    // cancelled, so the last return is the one place both loaders share.
    @Inject(method = "doBrew", at = @At("TAIL"))
    private static void UA$Inject$updateBrewInv(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            ItemStack stack = items.get(INGREDIENT_SLOT).copy();
            if (stack.getItem() instanceof MiningSkillCardItem item) {
                // Consume potion points
                MiningSkillCardData data = item.getData(stack);
                int oldPoints = data.getPotionPoints();
                data.consumePotionPoint(1).save();
                items.set(INGREDIENT_SLOT, stack);

                // Sync data to client
                BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
                if (!(blockEntity instanceof BrewingStandBlockEntity brewingStandBlock)) return;
                List<ServerPlayer> players = serverLevel.getPlayers(brewingStandBlock::stillValid);

                // The card isn't carried by anyone, so push its new potion points along with the slot display.
                SkillsRecordSavedData storage = SkillsRecordSavedData.get(serverLevel.getServer());
                for (ServerPlayer player : players) {
                    FTBUltimineAddition.LOGGER.debug("{} synced potion points! [B:{} / A:{}]", player.getDisplayName().getString(), oldPoints, data.getPotionPoints());
                    PayloadHandler.sendToPlayer(new MiningSkillCardPayload.SyncBrewing(stack.copy()), player);
                    SkillsRecordSync.sendCards(player, List.of(storage.createSync(data, stack)));
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
