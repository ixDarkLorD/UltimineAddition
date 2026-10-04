package net.ixdarklord.ultimine_addition.network.payloads;

import io.netty.buffer.ByteBuf;
import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.ultimine_addition.client.undo.UndoGrowthClient;
import net.ixdarklord.ultimine_addition.client.undo.UndoPreviewClient;
import net.ixdarklord.ultimine_addition.client.undo.UndoProgressHud;
import net.ixdarklord.ultimine_addition.common.undo.UltimineUndo;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.ixdarklord.coolcatcore.api.network.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class UndoPayload {
    private UndoPayload() {}

    // First press asks for a preview; pressing again while it's shown confirms. partial: the player agreed to undo
    // with items missing (only what they can pay for comes back).
    public record Request(boolean confirm, boolean partial) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(FTBUltimineAddition.id("undo_request"));
        public static final StreamCodec<FriendlyByteBuf, Request> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Request::confirm,
                ByteBufCodecs.BOOL, Request::partial,
                Request::new);

        public static void handle(Request msg, PacketContext ctx) {
            ctx.queue(() -> {
                if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
                if (msg.confirm) UltimineUndo.confirm(player, msg.partial);
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
                          int available, int maxHistory, long expiresIn,
                          // For each of positions: whether it comes back when undoing with what's missing (all true
                          // when nothing is). The others are the ones the preview shows in red.
                          List<Boolean> comesBack) implements CustomPacketPayload {
        public static final Type<Preview> TYPE = new Type<>(FTBUltimineAddition.id("undo_preview"));
        // CoolCatLib's StreamCodec.composite (as 1.21.1's) takes at most six fields, so this one is written out.
        private static final StreamCodec<ByteBuf, List<BlockPos>> POSITIONS_CODEC = ByteBufCodecs.BLOCK_POS.apply(ByteBufCodecs.list());
        private static final StreamCodec<ByteBuf, List<BlockState>> STATES_CODEC = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY).apply(ByteBufCodecs.list());
        private static final StreamCodec<FriendlyByteBuf, List<ItemStack>> COST_CODEC = ByteBufCodecs.OPTIONAL_ITEM_STACK.apply(ByteBufCodecs.<ByteBuf, ItemStack>list()).cast();
        private static final StreamCodec<ByteBuf, List<Integer>> FROM_GROUND_CODEC = ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list());
        public static final StreamCodec<FriendlyByteBuf, Preview> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
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
            buf.writeVarInt(msg.comesBack().size());
            for (boolean back : msg.comesBack()) buf.writeBoolean(back);
        }, buf -> new Preview(POSITIONS_CODEC.decode(buf), STATES_CODEC.decode(buf), COST_CODEC.decode(buf), FROM_GROUND_CODEC.decode(buf),
                buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarLong(), readFlags(buf)));

        private static List<Boolean> readFlags(ByteBuf buf) {
            int size = ByteBufCodecs.VAR_INT.decode(buf);
            List<Boolean> flags = new java.util.ArrayList<>(size);
            for (int i = 0; i < size; i++) flags.add(buf.readBoolean());
            return flags;
        }

        // How many of positions come back.
        public int restorable() {
            int count = 0;
            for (boolean back : this.comesBack) if (back) count++;
            return count;
        }

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
        public static final StreamCodec<FriendlyByteBuf, Progress> STREAM_CODEC = StreamCodec.composite(
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
        public static final StreamCodec<FriendlyByteBuf, Grow> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.UUID, Grow::owner,
                ByteBufCodecs.BLOCK_POS.apply(ByteBufCodecs.list()), Grow::positions,
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

    // Asks for the list of operations the player can still undo.
    public record HistoryRequest() implements CustomPacketPayload {
        public static final Type<HistoryRequest> TYPE = new Type<>(FTBUltimineAddition.id("undo_history_request"));
        public static final StreamCodec<FriendlyByteBuf, HistoryRequest> STREAM_CODEC = StreamCodec.unit(new HistoryRequest());

        public static void handle(HistoryRequest msg, PacketContext ctx) {
            ctx.queue(() -> {
                if (ctx.getPlayer() instanceof ServerPlayer player) UltimineUndo.history(player);
            });
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // One stored operation: an item standing for its blocks, how many come back, what it costs, how long it stays
    // undoable, and whether the player could undo it now (state) and pay for all of it (affordable).
    public record HistoryEntry(ItemStack icon, int blocks, List<ItemStack> cost, int xp, long expiresIn, boolean free, boolean affordable, int state) {
        public static final int READY = 0, TOO_FAR = 1, BLOCKED = 2;
        // Written out: StreamCodec.composite takes at most six fields here.
        private static final StreamCodec<FriendlyByteBuf, List<ItemStack>> COST_CODEC = ByteBufCodecs.OPTIONAL_ITEM_STACK.apply(ByteBufCodecs.<ByteBuf, ItemStack>list()).cast();
        public static final StreamCodec<FriendlyByteBuf, HistoryEntry> STREAM_CODEC = StreamCodec.of((buf, entry) -> {
            ByteBufCodecs.OPTIONAL_ITEM_STACK.encode(buf, entry.icon());
            buf.writeVarInt(entry.blocks());
            COST_CODEC.encode(buf, entry.cost());
            buf.writeVarInt(entry.xp());
            buf.writeVarLong(entry.expiresIn());
            buf.writeBoolean(entry.free());
            buf.writeBoolean(entry.affordable());
            buf.writeVarInt(entry.state());
        }, buf -> new HistoryEntry(ByteBufCodecs.OPTIONAL_ITEM_STACK.decode(buf), buf.readVarInt(), COST_CODEC.decode(buf), buf.readVarInt(), buf.readVarLong(),
                buf.readBoolean(), buf.readBoolean(), buf.readVarInt()));
    }

    // The player's stored operations, newest first (the order they are undone in).
    public record History(List<HistoryEntry> entries, int maxHistory, boolean enabled) implements CustomPacketPayload {
        public static final Type<History> TYPE = new Type<>(FTBUltimineAddition.id("undo_history"));
        public static final StreamCodec<FriendlyByteBuf, History> STREAM_CODEC = StreamCodec.composite(
                HistoryEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), History::entries,
                ByteBufCodecs.VAR_INT, History::maxHistory,
                ByteBufCodecs.BOOL, History::enabled,
                History::new);

        public static void handle(History msg, PacketContext ctx) {
            ctx.queue(() -> UndoPreviewClient.INSTANCE.openHistory(msg));
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Close() implements CustomPacketPayload {
        public static final Type<Close> TYPE = new Type<>(FTBUltimineAddition.id("undo_close"));
        public static final StreamCodec<FriendlyByteBuf, Close> STREAM_CODEC = StreamCodec.unit(new Close());

        public static void handle(Close msg, PacketContext ctx) {
            ctx.queue(UndoPreviewClient.INSTANCE::close);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
