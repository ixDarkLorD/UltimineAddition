package net.ixdarklord.ultimine_addition.network.payloads;

import dev.architectury.networking.NetworkManager;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.data.record.CardHistory;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordClientCache;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordSavedData;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordSync;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SkillsRecordPayload {
    private SkillsRecordPayload() {
    }

    /** The record of the Skills Record menu the player has open (server side). */
    private static Optional<SkillsRecordData> getOpenRecord(Player player) {
        if (player instanceof ServerPlayer && player.containerMenu instanceof SkillsRecordMenu menu) {
            return Optional.of(menu.getData());
        }
        return Optional.empty();
    }

    /** Opens the record worn in the accessory slot (Curios/Trinkets keybind). */
    public record Open() implements CustomPacketPayload {
        public static final Type<SkillsRecordPayload.Open> TYPE =
                new Type<>(FTBUltimineAddition.id("open_skills_record"));

        public static final StreamCodec<FriendlyByteBuf, Open> STREAM_CODEC = StreamCodec.unit(new Open());

        public static void handle(Open ignored, NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    ItemStack stack = ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
                    if (stack.getItem() instanceof SkillsRecordItem item && !item.isLegacyMode()) {
                        SkillsRecordMenu.open(player, stack, null);
                    }
                }
            });
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SelectCard(int slot) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SelectCard> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("select_card"));
        public static final StreamCodec<FriendlyByteBuf, SelectCard> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, SelectCard::slot, SelectCard::new);

        public static void handle(SelectCard msg, NetworkManager.PacketContext ctx) {
            ctx.queue(() -> getOpenRecord(ctx.getPlayer()).ifPresent(data -> {
                if (msg.slot >= -1 && msg.slot < SkillsRecordData.CARD_SLOTS) data.setSelectedCard(msg.slot).save();
            }));
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ToggleConsumeMode(boolean value) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ToggleConsumeMode> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("sync_consume_mode"));
        public static final StreamCodec<FriendlyByteBuf, ToggleConsumeMode> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, ToggleConsumeMode::value, ToggleConsumeMode::new);

        public static void handle(ToggleConsumeMode msg, NetworkManager.PacketContext ctx) {
            ctx.queue(() -> getOpenRecord(ctx.getPlayer()).ifPresent(data -> data.setConsumeMode(msg.value).save()));
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PinChallenge(int cardSlot, Identifier challengeId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PinChallenge> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("pin_challenge"));
        public static final StreamCodec<FriendlyByteBuf, PinChallenge> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, PinChallenge::cardSlot, Identifier.STREAM_CODEC, PinChallenge::challengeId, PinChallenge::new);

        public static void handle(PinChallenge msg, NetworkManager.PacketContext ctx) {
            ctx.queue(() -> getOpenRecord(ctx.getPlayer()).ifPresent(data -> data.togglePinned(msg.cardSlot, msg.challengeId).save()));
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record EditChallenge(int cardSlot, Identifier challengeId, int value) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<EditChallenge> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("edit_challenge"));
        public static final StreamCodec<FriendlyByteBuf, EditChallenge> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, EditChallenge::cardSlot, Identifier.STREAM_CODEC, EditChallenge::challengeId, ByteBufCodecs.INT, EditChallenge::value, EditChallenge::new);

        public static void handle(EditChallenge msg, NetworkManager.PacketContext ctx) {
            ctx.queue(() -> {
                Player player = ctx.getPlayer();
                if (!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) return;
                getOpenRecord(player).ifPresent(data -> data.getCardData(msg.cardSlot).ifPresent(cardData -> {
                    cardData.setAmount(msg.challengeId, msg.value).save();
                    data.save();
                }));
            });
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Asks the server for a record the client doesn't know yet (e.g. hovered in a chest). */
    public record RequestRecord(UUID id) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RequestRecord> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("request_skills_record"));
        public static final StreamCodec<FriendlyByteBuf, RequestRecord> STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, RequestRecord::id, RequestRecord::new);

        public static void handle(RequestRecord msg, NetworkManager.PacketContext ctx) {
            ctx.queue(() -> {
                if (ctx.getPlayer() instanceof ServerPlayer player) {
                    SkillsRecordSavedData storage = SkillsRecordSavedData.get(player.level().getServer());
                    SkillsRecordData data = storage.get(msg.id);
                    if (data != null) SkillsRecordSync.send(player, storage, data);
                }
            });
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** A record's contents and settings, plus the history of the cards inside it. */
    public record SyncRecord(SkillsRecordData data, Map<UUID, CardHistory> histories) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncRecord> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("skills_record_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncRecord> STREAM_CODEC = StreamCodec.composite(
                SkillsRecordData.STREAM_CODEC, SyncRecord::data,
                ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, CardHistory.STREAM_CODEC), SyncRecord::histories,
                SyncRecord::new
        );

        public static void handle(SyncRecord message, NetworkManager.PacketContext context) {
            context.queue(() -> SkillsRecordClientCache.accept(message.data, message.histories));
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
