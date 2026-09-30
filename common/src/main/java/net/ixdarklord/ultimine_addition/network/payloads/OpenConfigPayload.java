package net.ixdarklord.ultimine_addition.network.payloads;

import net.ixdarklord.coolcatcore.api.config.client.ConfigScreens;
import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

// Opens one of the mod's config screens on the client (from /ultimine_addition config). Its codec comes from the record
// (CoolCatLib's PayloadCodecs). Editing the server config is still checked by CoolCatLib when saving.
public record OpenConfigPayload(Target target) implements CustomPacketPayload {
    public static final Type<OpenConfigPayload> TYPE = new Type<>(FTBUltimineAddition.id("open_config"));

    public static void handle(OpenConfigPayload message, PacketContext context) {
        context.queue(() -> Minecraft.getInstance().setScreen(switch (message.target) {
            case ALL -> ConfigScreens.create(null, FTBUltimineAddition.MOD_ID);
            case CLIENT -> ConfigScreens.create(null, UAClientConfig.CONFIG);
            case SERVER -> ConfigScreens.create(null, UAServerConfig.CONFIG);
        }));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Target {
        // Every config of the mod to choose from (server and startup ones included).
        ALL,
        CLIENT,
        SERVER
    }
}
