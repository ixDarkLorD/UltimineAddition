package net.ixdarklord.ultimine_addition.network.payloads;

import io.netty.buffer.ByteBuf;
import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.ultimine_addition.client.undo.UndoGrowthClient;
import net.ixdarklord.ultimine_addition.client.undo.UndoPreviewClient;
import net.ixdarklord.ultimine_addition.client.undo.UndoProgressHud;
import net.ixdarklord.ultimine_addition.common.undo.UltimineUndo;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class UndoPayload {
    private UndoPayload() {}

    // First press asks for a preview; pressing again while it's shown confirms.
    public record Request(boolean confirm) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(FTBUltimineAddition.id("undo_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> STREAM_CODEC =
                ByteBufCodecs.BOOL.map(Request::new, Request::confirm).cast();

        public static void handle(Request msg, PacketContext ctx) {
            ctx.queue(() -> {
                if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
                if (msg.confirm) UltimineUndo.confirm(player);
                else UltimineUndo.preview(player);
            });
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // What undoing the newest operation would put back and cost. fromGround: how much of each item is still lying
    // where it dropped (the client counts its own inventory).
    public record Preview(List<BlockPos> positions, List<BlockState> states, List<ItemStack> cost, List<Integer> fromGround,
                          int xp, boolean xpOnGround, boolean free,
                          // Undos stored for the player (this one included), the most the server keeps, and the
                          // milliseconds left before this one leaves the undo window.
                          int available, int maxHistory, long expiresIn) implements CustomPacketPayload {
        public static final Type<Preview> TYPE = new Type<>(FTBUltimineAddition.id("undo_preview"));
        // 1.21.1's StreamCodec.composite takes at most six fields, so this one is written out.
        private static final StreamCodec<ByteBuf, List<BlockPos>> POSITIONS_CODEC = BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list());
        private static final StreamCodec<ByteBuf, List<BlockState>> STATES_CODEC = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY).apply(ByteBufCodecs.list());
        private static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> COST_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list());
        private static final StreamCodec<ByteBuf, List<Integer>> FROM_GROUND_CODEC = ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list());
        public static final StreamCodec<RegistryFriendlyByteBuf, Preview> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
            POSITIONS_CODEC.encode(buf, msg.positions());
            STATES_CODEC.encode(buf, msg.states());
            COST_CODEC.encode(buf, msg.cost());
            FROM_GROUND_CODEC.encode(buf, msg.fromGround());
            buf.writeVarInt(msg.xp());
            buf.writeBoolean(msg.xpOnGround());
            buf.writeBoolean(msg.free());
            buf.writeVarInt(msg.available());
            buf.writeVarInt(msg.maxHistory());
            buf.writeVarLong(msg.expiresIn());
        }, buf -> new Preview(POSITIONS_CODEC.decode(buf), STATES_CODEC.decode(buf), COST_CODEC.decode(buf), FROM_GROUND_CODEC.decode(buf),
                buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarLong()));

        public static void handle(Preview msg, PacketContext ctx) {
            ctx.queue(() -> UndoPreviewClient.INSTANCE.open(msg));
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // How many of a confirmed undo's blocks are back in place, for the client's progress HUD.
    // queued: confirmed undos of the same player waiting to start after this one.
    public record Progress(int placed, int total, int queued) implements CustomPacketPayload {
        public static final Type<Progress> TYPE = new Type<>(FTBUltimineAddition.id("undo_progress"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Progress> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Progress::placed,
                ByteBufCodecs.VAR_INT, Progress::total,
                ByteBufCodecs.VAR_INT, Progress::queued,
                Progress::new);

        public static void handle(Progress msg, PacketContext ctx) {
            ctx.queue(() -> UndoProgressHud.INSTANCE.update(msg.placed(), msg.total(), msg.queued()));
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // A confirmed undo's blocks for nearby clients to draw growing back, in the order they were broken: each starts
    // after its (fractional) start in ticks and grows for growTicks, after which the server places the real block.
    // owner's client also drives its progress HUD from this timeline.
    public record Grow(UUID owner, List<BlockPos> positions, List<BlockState> states, List<Float> starts, int growTicks) implements CustomPacketPayload {
        public static final Type<Grow> TYPE = new Type<>(FTBUltimineAddition.id("undo_grow"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Grow> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, Grow::owner,
                BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), Grow::positions,
                ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY).apply(ByteBufCodecs.list()), Grow::states,
                ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), Grow::starts,
                ByteBufCodecs.VAR_INT, Grow::growTicks,
                Grow::new);

        public static void handle(Grow msg, PacketContext ctx) {
            ctx.queue(() -> UndoGrowthClient.INSTANCE.add(msg));
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Close() implements CustomPacketPayload {
        public static final Type<Close> TYPE = new Type<>(FTBUltimineAddition.id("undo_close"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Close> STREAM_CODEC = StreamCodec.unit(new Close());

        public static void handle(Close msg, PacketContext ctx) {
            ctx.queue(UndoPreviewClient.INSTANCE::close);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
