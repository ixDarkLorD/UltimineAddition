package net.ixdarklord.ultimine_addition.network.packets;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.ixdarklord.ultimine_addition.config.ClientSideConfigPreserver;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.ConfigValueWrapper;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SyncConfigPacket extends BaseS2CMessage {
    private final boolean forceChanges;
    private final boolean shouldPreserve;
    private final Map<List<String>, ConfigValueWrapper<?>> changes;

    public SyncConfigPacket(boolean shouldPreserve, boolean forceChanges, ForgeConfigSpec.ConfigValue<?>... configValues) {
        this(forceChanges, shouldPreserve, ConfigHandler.toMap(configValues));
    }

    public SyncConfigPacket(boolean forceChanges, boolean shouldPreserve, Map<List<String>, ConfigValueWrapper<?>> changes) {
        this.forceChanges = forceChanges;
        this.shouldPreserve = shouldPreserve;
        this.changes = changes;
    }

    public SyncConfigPacket(FriendlyByteBuf buf) {
        this.forceChanges = buf.readBoolean();
        this.shouldPreserve = buf.readBoolean();
        this.changes = buf.readMap((b) -> b.readList(FriendlyByteBuf::readUtf), ConfigValueWrapper::decode);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.forceChanges);
        buf.writeBoolean(this.shouldPreserve);
        buf.writeMap(this.changes, (b, path) -> b.writeCollection(path, FriendlyByteBuf::writeUtf), (b, wrapper) -> wrapper.encode(b));
    }

    public MessageType getType() {
        return PacketHandler.SYNC_CONFIG;
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            Map<List<String>, ConfigValueWrapper<?>> originalValues = new HashMap<>();
            this.changes.forEach((path, wrapper) -> {
                ConfigHandler.SyncResult syncResult = ConfigHandler.applySyncedValues(path, wrapper, this.forceChanges, true);
                if (this.shouldPreserve && syncResult.updated()) {
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
