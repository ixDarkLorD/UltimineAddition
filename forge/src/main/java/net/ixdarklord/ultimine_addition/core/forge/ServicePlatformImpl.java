package net.ixdarklord.ultimine_addition.core.forge;

import net.ixdarklord.ultimine_addition.core.ServicePlatform;

public final class ServicePlatformImpl implements ServicePlatform {
    public static ServicePlatform get() {
        return new ServicePlatformImpl();
    }

    @Override
    public ServicePlatform.SlotAPI slotAPI() {
        return new ServicePlatformSlotAPIImpl();
    }

    @Override
    public ServicePlatform.Players players() {
        return new ServicePlatformPlayersImpl();
    }
}
