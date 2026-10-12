package net.ixdarklord.ultimine_addition.network.payloads;

import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.ultimine_addition.common.data.shape.CustomShapes;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.coolcatcore.api.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// What a player does with shapes from their screens: marking a favourite (the Shape Selector's star), and saving,
// replacing and deleting shapes of their own (the Shape Designer, which also asks for the list of them). The codecs come
// from the records (CoolCatLib's PayloadCodecs).
public final class ShapePayload {
    private ShapePayload() {
    }

    public record Favorite(ResourceLocation shape) implements CustomPacketPayload {
        public static final Type<Favorite> TYPE = new Type<>(FTBUltimineAddition.id("favorite_shape"));

        public static void handle(Favorite message, PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    ServicePlatform.get().players().modifyAbilityData(player, data -> data.toggleFavoriteShape(message.shape));
                }
            });
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // "replace" is the id of the player's shape this drawing takes the place of, or empty for a new one.
    public record Design(String name, List<String> rows, String replace) implements CustomPacketPayload {
        public static final Type<Design> TYPE = new Type<>(FTBUltimineAddition.id("design_shape"));

        public static void handle(Design message, PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player && message.name.length() <= 64 && message.rows.size() <= CustomShapes.SIZE * CustomShapes.MAX_DEPTH) {
                    ResourceLocation replace = message.replace.isEmpty() ? null : ResourceLocation.tryParse(message.replace);
                    if (replace != null || message.replace.isEmpty()) CustomShapes.create(player, message.name, message.rows, replace);
                }
            });
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Delete(ResourceLocation shape) implements CustomPacketPayload {
        public static final Type<Delete> TYPE = new Type<>(FTBUltimineAddition.id("delete_shape"));

        public static void handle(Delete message, PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    CustomShapes.delete(player.level().getServer(), player.getUUID(), false, message.shape);
                    CustomShapes.sendMine(player);
                }
            });
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Asks for the player's own shapes (the designer, as it opens).
    public record ListRequest(boolean own) implements CustomPacketPayload {
        public static final Type<ListRequest> TYPE = new Type<>(FTBUltimineAddition.id("my_shapes_request"));

        public static void handle(ListRequest message, PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) CustomShapes.sendMine(player);
            });
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // One of the player's shapes as drawn: five rows a layer, the front one first.
    public record Saved(ResourceLocation id, String name, List<String> rows) {
    }

    // The player's own shapes and how many they may keep. The designer reads the last one received.
    public record Mine(List<Saved> shapes, int limit) implements CustomPacketPayload {
        public static final Type<Mine> TYPE = new Type<>(FTBUltimineAddition.id("my_shapes"));
        private static volatile Mine latest = new Mine(List.of(), 0);

        public static Mine latest() {
            return latest;
        }

        public static void handle(Mine message, PacketContext context) {
            context.queue(() -> latest = message);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
