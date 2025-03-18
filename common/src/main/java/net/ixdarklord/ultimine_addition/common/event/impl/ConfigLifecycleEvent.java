package net.ixdarklord.ultimine_addition.common.event.impl;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.ixdarklord.ultimine_addition.config.ConfigInfo;

public class ConfigLifecycleEvent {
    public static final Event<ConfigUpdate> EVENT = EventFactory.createEventResult(ConfigUpdate.class);

    @FunctionalInterface
    public interface ConfigUpdate {
        void onConfigUpdate(ConfigInfo configInfo, ConfigUpdateType updateType);
    }

    public enum ConfigUpdateType {
        LOADING,
        RELOADING,
        UNLOADING
    }
}
