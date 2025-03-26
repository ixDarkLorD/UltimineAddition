package net.ixdarklord.ultimine_addition.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import net.ixdarklord.ultimine_addition.common.data.item.SelectedShapeData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(FTBUltimineClient.class)
abstract class FTBUltimineClientMixin {
    @ModifyReturnValue(method = "sneak", at = @At("RETURN"), remap = false)
    private boolean UA$ModifyReturn$sneak(boolean original) {
        if (FTBUltimineIntegration.hasToolWithShape(FTBUltimineClient.getClientPlayer())) {
            return false;
        }
        return original;
    }

    @Inject(method = "addPressedInfo", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 2), remap=false)
    private void UA$Inject$addPressedInfo(List<MutableComponent> list, CallbackInfo ci) {
        if (FTBUltimineIntegration.hasToolWithShape(FTBUltimineClient.getClientPlayer())) {
            ItemStack stack = FTBUltimineClient.getClientPlayer().getMainHandItem();
            list.add(Component.translatable("info.ultimine_addition.using_tool_shape", stack.getDisplayName()).withStyle(ChatFormatting.GRAY));
        }
    }

    @Redirect(method = "addPressedInfo", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/shape/ShapeRegistry;getShape(I)Ldev/ftb/mods/ftbultimine/shape/Shape;", ordinal = 1), remap=false)
    private Shape UA$Redirect$addPressedInfo(int idx) {
        if (FTBUltimineIntegration.hasToolWithShape(FTBUltimineClient.getClientPlayer())) {
            ItemStack stack = FTBUltimineClient.getClientPlayer().getMainHandItem();
            SelectedShapeData data = stack.get(Registration.SELECTED_SHAPE_COMPONENT.get());
            if (data != null) return data.shape();
        }
        return ShapeRegistry.getShape(idx);
    }
}
