package net.ixdarklord.ultimine_addition.network.payloads;

import dev.ftb.mods.ftbultimine.shape.Shape;
import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.ShapeDiagrams;
import net.ixdarklord.ultimine_addition.common.progression.ShapeDiagram;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.coolcatcore.api.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// The diagrams of Ultimine shapes, for the Skills Record's shape choice: the client asks for the shapes it is about to
// show, and the server (which can ask FTB Ultimine for a shape's blocks) answers with their pictures. Both codecs come
// from the records (CoolCatLib's PayloadCodecs).
public final class ShapeDiagramPayload {
    private static final int MAX_SHAPES = 64;

    private ShapeDiagramPayload() {
    }

    public record Request(List<ResourceLocation> shapes) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(FTBUltimineAddition.id("shape_diagram_request"));

        public static void handle(Request message, PacketContext context) {
            context.queue(() -> {
                if (!(context.getPlayer() instanceof ServerPlayer player)) return;
                List<ShapeDiagram> diagrams = new ArrayList<>();
                for (ResourceLocation id : message.shapes.subList(0, Math.min(MAX_SHAPES, message.shapes.size()))) {
                    Shape shape = FTBUltimineIntegration.getShape(id);
                    if (shape != null) diagrams.add(ShapeDiagram.compute(player, id, shape));
                }
                if (!diagrams.isEmpty()) PayloadHandler.sendToPlayer(new Diagrams(diagrams), player);
            });
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Diagrams(List<ShapeDiagram> diagrams) implements CustomPacketPayload {
        public static final Type<Diagrams> TYPE = new Type<>(FTBUltimineAddition.id("shape_diagrams"));

        public static void handle(Diagrams message, PacketContext context) {
            context.queue(() -> ShapeDiagrams.accept(message.diagrams));
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
