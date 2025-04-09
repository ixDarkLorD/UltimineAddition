package net.ixdarklord.ultimine_addition.network.payloads;

import dev.architectury.networking.NetworkManager;
import net.ixdarklord.ultimine_addition.config.ClientSideConfigPreserver;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.ConfigValueWrapper;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ConfigSyncPayload(boolean forceChanges,
                                boolean shouldPreserve,
                                Map<List<String>, ConfigValueWrapper<?>> changes) implements CustomPacketPayload {
    public static final Type<ConfigSyncPayload> TYPE = new Type<>(FTBUltimineAddition.rl("config_sync"));

    public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ConfigSyncPayload::forceChanges,
            ByteBufCodecs.BOOL,
            ConfigSyncPayload::shouldPreserve,
            ByteBufCodecs.map(
                    HashMap::new,
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
                    ConfigValueWrapper.CODEC
            ),
            ConfigSyncPayload::changes,
            ConfigSyncPayload::new
    );

    public ConfigSyncPayload(boolean shouldPreserve, boolean forceChanges, ModConfigSpec.ConfigValue<?>... configValues) {
        this(forceChanges, shouldPreserve, ConfigHandler.toMap(configValues));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConfigSyncPayload message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            Map<List<String>, ConfigValueWrapper<?>> originalValues = new HashMap<>();

            message.changes().forEach((path, wrapper) -> {
                ConfigHandler.SyncResult syncResult = ConfigHandler.applySyncedValues(path, wrapper, message.forceChanges(), true);
                if (message.shouldPreserve() && syncResult.updated()) {
                    @SuppressWarnings("unchecked")
                    ConfigValueWrapper<Object> oldWrapper = new ConfigValueWrapper<>((Class<Object>) syncResult.oldValue().getClass(), syncResult.oldValue());
                    originalValues.put(path, oldWrapper);
                }
            });

            if (!originalValues.isEmpty()) {
                ClientSideConfigPreserver.preserveOriginalValues(originalValues);
            }
        });
    }
}