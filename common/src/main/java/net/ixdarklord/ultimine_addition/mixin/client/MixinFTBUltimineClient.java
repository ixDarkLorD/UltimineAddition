package net.ixdarklord.ultimine_addition.mixin.client;

import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import net.ixdarklord.ultimine_addition.common.data.item.SelectedShapeData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FTBUltimineClient.class)
public abstract class MixinFTBUltimineClient {
    @Inject(method = "sneak", at = @At("HEAD"), remap = false, cancellable = true)
    private void UA$Inject$sneak(CallbackInfoReturnable<Boolean> cir) {
        if (FTBUltimineIntegration.hasToolWithShape(FTBUltimineClient.getClientPlayer())) {
            cir.setReturnValue(false);
        }

    }

    @Inject(method = "addPressedInfo", at = @At("TAIL"), remap = false)
    private void UA$Inject$addPressedInfo1(List<MutableComponent> list, CallbackInfo ci) {
        Component blockReason = FTBUltimineIntegration.INSTANCE.ultimineBlockReason(FTBUltimineClient.getClientPlayer());
        if (blockReason != null) {
            list.add(1, Component.literal("| ").append(blockReason.copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.RED)).withStyle(ChatFormatting.GRAY));
        }

    }

    @Inject(method = "addPressedInfo", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 2))
    private void UA$Inject$addPressedInfo2(List<MutableComponent> list, CallbackInfo ci) {
        if (FTBUltimineIntegration.hasToolWithShape(FTBUltimineClient.getClientPlayer())) {
            ItemStack stack = FTBUltimineClient.getClientPlayer().getMainHandItem();
            list.add(Component.translatable("info.ultimine_addition.using_tool_shape", stack.getDisplayName()).withStyle(ChatFormatting.GRAY));
        }

    }

    @Redirect(method = "addPressedInfo", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/shape/ShapeRegistry;getShape(I)Ldev/ftb/mods/ftbultimine/shape/Shape;", ordinal = 1), remap = false)
    private Shape UA$Redirect$addPressedInfo(int idx) {
        if (FTBUltimineIntegration.hasToolWithShape(FTBUltimineClient.getClientPlayer())) {
            ItemStack stack = FTBUltimineClient.getClientPlayer().getMainHandItem();
            SelectedShapeData data = SelectedShapeData.load(stack);
            if (data != null && data.getShape() != null) {
                return data.getShape();
            }
        }

        return ShapeRegistry.getShape(idx);
    }
}
