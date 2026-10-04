package net.ixdarklord.ultimine_addition.network.payloads;

import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.ultimine_addition.common.data.shape.DataShape;
import net.ixdarklord.ultimine_addition.common.data.shape.DataShapesManager;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// The data pack shapes of the server, for the client's shape list: ids and names, in the server's order. Sent on join
// and after a data pack reload (an empty list clears them). Its codec comes from the record (CoolCatLib's PayloadCodecs).
public record SyncShapesPayload(List<Entry> shapes) implements CustomPacketPayload {
    public static final Type<SyncShapesPayload> TYPE = new Type<>(FTBUltimineAddition.id("sync_shapes"));

    public record Entry(ResourceLocation id, String name) {
    }

    public static SyncShapesPayload of(List<DataShape> shapes) {
        return new SyncShapesPayload(shapes.stream().map(shape -> new Entry(shape.getName(), shape.name())).toList());
    }

    public static void handle(SyncShapesPayload message, PacketContext context) {
        context.queue(() -> DataShapesManager.installClient(
                message.shapes.stream().map(entry -> DataShape.named(entry.id(), entry.name())).toList()));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
