package net.ixdarklord.ultimine_addition.mixin.client;

import net.ixdarklord.ultimine_addition.client.renderer.ItemAlpha;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiItemRenderState.class)
public abstract class GuiItemRenderStateMixin implements ItemAlpha.Holder {
    @Unique
    private float ua$alpha = 1.0F;

    @Override
    public float ua$getAlpha() {
        return this.ua$alpha;
    }

    @Override
    public void ua$setAlpha(float alpha) {
        this.ua$alpha = alpha;
    }
}
