package net.ixdarklord.ultimine_addition.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import net.ixdarklord.ultimine_addition.common.data.item.SelectedShapeData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// FTB Ultimine 2001: the shape menu's sneak check is sneak(), and addPressedInfo fills a list of lines.
@Mixin(value = FTBUltimineClient.class, remap = false)
abstract class FTBUltimineClientMixin {
    @ModifyReturnValue(method = "sneak", at = @At("RETURN"))
    private boolean UA$ModifyReturn$isMenuSneaking(boolean original) {
        if (FTBUltimineIntegration.hasToolWithShape(Minecraft.getInstance().player)) {
            return false;
        }
        return original;
    }

    // The shape comes from the tool: says so in place of the "change shape" hint.
    @Inject(method = "addPressedInfo", at = @At("TAIL"))
    private void UA$Inject$addPressedInfo(List<MutableComponent> list, CallbackInfo ci) {
        if (FTBUltimineIntegration.hasToolWithShape(Minecraft.getInstance().player) && !list.isEmpty()) {
            ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
            MutableComponent text = Component.translatable("info.ultimine_addition.using_tool_shape", stack.getDisplayName())
                    .withStyle(ChatFormatting.GRAY);
            if (list.size() > 1 && list.get(1).getContents() instanceof TranslatableContents contents
                    && contents.getKey().equals("ftbultimine.change_shape")) {
                list.remove(1);
            }
            list.add(1, text);
        }
    }

    @Redirect(method = "addPressedInfo", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/shape/ShapeRegistry;getShape(I)Ldev/ftb/mods/ftbultimine/shape/Shape;", ordinal = 1))
    private Shape UA$Redirect$addPressedInfo(int idx) {
        if (FTBUltimineIntegration.hasToolWithShape(Minecraft.getInstance().player)) {
            ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
            SelectedShapeData data = Registration.SELECTED_SHAPE_COMPONENT.get(stack);
            if (data != null) return data.shape();
        }
        return ShapeRegistry.getShape(idx);
    }
}
