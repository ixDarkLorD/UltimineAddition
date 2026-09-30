package net.ixdarklord.ultimine_addition.core.fabric;

import net.ixdarklord.ultimine_addition.core.ServicePlatform;

public final class ServicePlatformImpl implements ServicePlatform {
    public static ServicePlatform get() {
        return new ServicePlatformImpl();
    }

    @Override
    public SlotAPI slotAPI() {
        return new ServicePlatformSlotAPIImpl();
    }

    @Override
    public Players players() {
        return new ServicePlatformPlayersImpl();
    }
}
