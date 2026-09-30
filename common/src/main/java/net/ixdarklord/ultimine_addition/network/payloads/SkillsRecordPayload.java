package net.ixdarklord.ultimine_addition.network.payloads;

import net.minecraft.commands.Commands;
import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.common.progression.ProgressionRewards;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.data.record.CardSync;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordClientCache;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordSavedData;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordSync;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.ixdarklord.coolcatcore.api.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SkillsRecordPayload {
    private SkillsRecordPayload() {
    }

    private static Optional<SkillsRecordData> getOpenRecord(Player player) {
        if (player instanceof ServerPlayer && player.containerMenu instanceof SkillsRecordMenu menu) {
            return Optional.of(menu.getData());
        }
        return Optional.empty();
    }

    public record Open() implements CustomPacketPayload {
        public static final Type<SkillsRecordPayload.Open> TYPE =
                new Type<>(FTBUltimineAddition.id("open_skills_record"));

        public static final StreamCodec<FriendlyByteBuf, Open> STREAM_CODEC = StreamCodec.unit(new Open());

        public static void handle(Open ignored, PacketContext context) {
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

        public static void handle(SelectCard msg, PacketContext ctx) {
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

        public static void handle(ToggleConsumeMode msg, PacketContext ctx) {
            ctx.queue(() -> getOpenRecord(ctx.getPlayer()).ifPresent(data -> data.setConsumeMode(msg.value).save()));
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PinChallenge(int cardSlot, ResourceLocation challengeId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PinChallenge> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("pin_challenge"));
        public static final StreamCodec<FriendlyByteBuf, PinChallenge> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, PinChallenge::cardSlot, ByteBufCodecs.RESOURCE_LOCATION, PinChallenge::challengeId, PinChallenge::new);

        public static void handle(PinChallenge msg, PacketContext ctx) {
            ctx.queue(() -> getOpenRecord(ctx.getPlayer()).ifPresent(data -> data.togglePinned(msg.cardSlot, msg.challengeId).save()));
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record EditChallenge(int cardSlot, ResourceLocation challengeId, int value) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<EditChallenge> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("edit_challenge"));
        public static final StreamCodec<FriendlyByteBuf, EditChallenge> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, EditChallenge::cardSlot, ByteBufCodecs.RESOURCE_LOCATION, EditChallenge::challengeId, ByteBufCodecs.INT, EditChallenge::value, EditChallenge::new);

        public static void handle(EditChallenge msg, PacketContext ctx) {
            ctx.queue(() -> {
                Player player = ctx.getPlayer();
                if (!player.hasPermissions(Commands.LEVEL_GAMEMASTERS)) return;
                getOpenRecord(player).ifPresent(data -> data.getCardData(msg.cardSlot).ifPresent(cardData -> {
                    MiningSkillCardItem.Tier before = cardData.getTier();
                    cardData.setAmount(msg.challengeId, msg.value).save();
                    data.save();
                    if (player instanceof ServerPlayer serverPlayer) ProgressionRewards.checkTierUp(serverPlayer, cardData, before);
                }));
            });
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RerollChallenge(int cardSlot, ResourceLocation challengeId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RerollChallenge> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("reroll_challenge"));
        public static final StreamCodec<FriendlyByteBuf, RerollChallenge> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, RerollChallenge::cardSlot, ByteBufCodecs.RESOURCE_LOCATION, RerollChallenge::challengeId, RerollChallenge::new);

        public static void handle(RerollChallenge msg, PacketContext ctx) {
            ctx.queue(() -> {
                if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
                getOpenRecord(player).ifPresent(data -> data.getCardData(msg.cardSlot).ifPresent(cardData -> {
                    int cost = player.isCreative() ? 0 : UAServerConfig.REROLL_INK_COST.get();
                    if (data.getInkAmount() < cost) return;
                    if (!cardData.rerollChallenge(msg.challengeId)) return;
                    cardData.save();
                    data.consumeInk(cost);
                    data.save();
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 0.8F);
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.PLAYERS, 0.8F, 1.2F);
                }));
            });
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClaimCertificate(int cardSlot, int tier, ResourceLocation shape) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ClaimCertificate> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("claim_shape_certificate"));
        public static final StreamCodec<FriendlyByteBuf, ClaimCertificate> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, ClaimCertificate::cardSlot, ByteBufCodecs.INT, ClaimCertificate::tier, ByteBufCodecs.RESOURCE_LOCATION, ClaimCertificate::shape, ClaimCertificate::new);

        public static void handle(ClaimCertificate msg, PacketContext ctx) {
            ctx.queue(() -> {
                if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
                MiningSkillCardItem.Tier tier;
                try {
                    tier = MiningSkillCardItem.Tier.fromInt(msg.tier);
                } catch (IllegalArgumentException e) {
                    return;
                }
                getOpenRecord(player).ifPresent(data -> data.getCardData(msg.cardSlot).ifPresent(cardData -> {
                    ShapeCertificateItem certificate = ShapeCertificateItem.forTier(tier);
                    if (certificate == null || !cardData.canClaimCertificate(tier)) return;
                    if (!ShapeCertificateItem.claimPool(player, cardData.getType().getId(), tier).contains(msg.shape)) return;
                    ItemStack reward = certificate.create(cardData.getType(), msg.shape);
                    if (!ShapeCertificateItem.hasRoomFor(player, reward)) {
                        player.displayClientMessage(Component.translatable("gui.ultimine_addition.card_viewer.certificate.no_space").withStyle(ChatFormatting.RED), true);
                        return;
                    }
                    player.getInventory().add(reward);
                    cardData.claimCertificate(tier);
                    cardData.save();
                    data.save();
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.6F, 1.2F);
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
                }));
            });
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestRecord(UUID id) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RequestRecord> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("request_skills_record"));
        public static final StreamCodec<FriendlyByteBuf, RequestRecord> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.UUID, RequestRecord::id, RequestRecord::new);

        public static void handle(RequestRecord msg, PacketContext ctx) {
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

    public record RequestCard(UUID id) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RequestCard> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("request_mining_skill_card"));
        public static final StreamCodec<FriendlyByteBuf, RequestCard> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.UUID, RequestCard::id, RequestCard::new);

        public static void handle(RequestCard msg, PacketContext ctx) {
            ctx.queue(() -> {
                if (ctx.getPlayer() instanceof ServerPlayer player) {
                    CardSync sync = SkillsRecordSavedData.get(player.level().getServer()).createSync(msg.id);
                    if (sync != null) SkillsRecordSync.sendCards(player, List.of(sync));
                }
            });
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SyncCards(List<CardSync> cards) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncCards> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("mining_skill_cards_sync"));
        public static final StreamCodec<FriendlyByteBuf, SyncCards> STREAM_CODEC = StreamCodec.composite(
                CardSync.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncCards::cards,
                SyncCards::new
        );

        public static void handle(SyncCards message, PacketContext context) {
            context.queue(() -> SkillsRecordClientCache.acceptCards(message.cards));
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SyncRecord(SkillsRecordData data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncRecord> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("skills_record_sync"));
        public static final StreamCodec<FriendlyByteBuf, SyncRecord> STREAM_CODEC = StreamCodec.composite(
                SkillsRecordData.STREAM_CODEC, SyncRecord::data,
                SyncRecord::new
        );

        public static void handle(SyncRecord message, PacketContext context) {
            context.queue(() -> SkillsRecordClientCache.accept(message.data));
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
