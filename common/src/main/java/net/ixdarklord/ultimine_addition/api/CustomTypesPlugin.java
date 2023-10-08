package net.ixdarklord.ultimine_addition.api;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.resources.ResourceLocation;

public class CustomTypesPlugin implements IUAPlugin {
    @Override
    public ResourceLocation getUid() {
        return Constants.getLocation("custom_types");
    }

    @Override
    public void register(Registration registration) {}
}
