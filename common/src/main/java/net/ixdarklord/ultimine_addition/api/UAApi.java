package net.ixdarklord.ultimine_addition.api;

import com.google.common.base.Stopwatch;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UAApi {
    private final static Logger LOGGER = LogManager.getLogger(UltimineAddition.MOD_NAME + "/API");
    private final static Collection<IUAPlugin> plugins = new HashSet<>();

    public static void init() {
//        UAApi.plugins.add(new CustomTypesPlugin());
        call("Registering plugin", plugins, plugin -> plugin.register(new IUAPlugin.Registration(plugin)));
        if (!plugins.isEmpty()) LOGGER.info("Loaded {} plugins", plugins.size());
    }

    @SuppressWarnings("SameParameterValue")
    private static void call(String title, Collection<IUAPlugin> plugins, Consumer<IUAPlugin> func) {
        List<IUAPlugin> invalidPlugins = new ArrayList<>();
        for (IUAPlugin plugin : plugins) {
            try {
                ResourceLocation pluginUid = plugin.getUid();
                LOGGER.info("{}: {}...", title, pluginUid);
                Stopwatch stopwatch = Stopwatch.createStarted();
                func.accept(plugin);
                LOGGER.info("{}: {} took {}", title, pluginUid, stopwatch);
            } catch (RuntimeException | LinkageError e) {
                LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), plugin.getUid(), e);
                invalidPlugins.add(plugin);
            }
        }
        plugins.removeAll(invalidPlugins);
    }

    public static List<IUAPlugin.Registration> getRegistrations() {
        return plugins.stream().map(plugin -> {
                    IUAPlugin.Registration registration = new IUAPlugin.Registration(plugin);
                    Consumer<IUAPlugin> consumer = p -> p.register(registration);
                    consumer.accept(plugin);
                    return registration;
                }).toList();
    }

    public static Collection<MiningSkillCardItem.Type> getTypes() {
        return getRegistrations().stream()
                .map(IUAPlugin.Registration::getTypes)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
