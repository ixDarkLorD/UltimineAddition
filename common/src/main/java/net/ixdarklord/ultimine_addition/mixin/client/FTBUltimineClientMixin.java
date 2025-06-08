package net.ixdarklord.ultimine_addition.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
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
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(FTBUltimineClient.class)
abstract class FTBUltimineClientMixin {
    @ModifyReturnValue(method = "isMenuSneaking", at = @At("RETURN"), remap = false)
    private boolean UA$ModifyReturn$isMenuSneaking(boolean original) {
        if (FTBUltimineIntegration.hasToolWithShape(FTBUltimineClient.getClientPlayer())) {
            return false;
        }
        return original;
    }

    @ModifyReturnValue(method = "addPressedInfo", at = @At(value = "RETURN"), remap = false)
    private <T> List<T> UA$ModifyReturn$addPressedInfo(List<T> original) {
        if (FTBUltimineIntegration.hasToolWithShape(FTBUltimineClient.getClientPlayer())) {
            try {
                @SuppressWarnings("unchecked")
                Class<T> indentedLineClass = (Class<T>) Class.forName("dev.ftb.mods.ftbultimine.client.FTBUltimineClient$IndentedLine");

                Constructor<T> constructor = indentedLineClass.getDeclaredConstructor(int.class, Component.class);
                constructor.setAccessible(true);

                ItemStack stack = FTBUltimineClient.getClientPlayer().getMainHandItem();
                Component text = Component.translatable("info.ultimine_addition.using_tool_shape", stack.getDisplayName())
                        .withStyle(ChatFormatting.GRAY);
                T indentedLine = constructor.newInstance(0, text);

                Method textMethod = indentedLineClass.getDeclaredMethod("text");
                textMethod.setAccessible(true);

                List<T> modified = new ArrayList<>(original);

                MutableComponent component = Component.translatable("ftbultimine.change_shape.short").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                T secondLine = modified.get(1);

                Component invoke = (Component) textMethod.invoke(secondLine);
                if (invoke.getString().equals(component.getString())) {
                    modified.remove(1);
                }

                modified.add(1, indentedLine);
                return Collections.unmodifiableList(modified);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return original;
    }

    @Redirect(method = "addPressedInfo", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/shape/ShapeRegistry;getShape(I)Ldev/ftb/mods/ftbultimine/api/shape/Shape;", ordinal = 1), remap = false)
    private Shape UA$Redirect$addPressedInfo(ShapeRegistry instance, int idx) {
        if (FTBUltimineIntegration.hasToolWithShape(FTBUltimineClient.getClientPlayer())) {
            ItemStack stack = FTBUltimineClient.getClientPlayer().getMainHandItem();
            SelectedShapeData data = stack.get(Registration.SELECTED_SHAPE_COMPONENT.get());
            if (data != null) return data.shape();
        }
        return instance.getShape(idx);
    }
}
