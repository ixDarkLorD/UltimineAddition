package net.ixdarklord.ultimine_addition.network.payloads;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.MenuRegistry;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class SkillsRecordPayload {
    private SkillsRecordPayload() {
    }

    private static ItemStack getSkillsRecord(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (player.containerMenu instanceof SkillsRecordMenu menu) {
                InteractionHand hand = menu.interactionHand;
                return hand == null ? ServicePlatform.get().slotAPI().getSkillsRecordItem(serverPlayer) : serverPlayer.getItemInHand(hand);
            }
        }

        return ItemStack.EMPTY;
    }

    public record Open() implements CustomPacketPayload {
        public static final Type<SkillsRecordPayload.Open> TYPE =
                new Type<>(FTBUltimineAddition.id("open_skills_record"));

        public static final StreamCodec<FriendlyByteBuf, Open> STREAM_CODEC = StreamCodec.unit(new Open());

        public static void handle(Open ignored, NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    ItemStack stack = ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
                    if (!(stack.getItem() instanceof SkillsRecordItem) || !stack.has(SkillsRecordData.DATA_COMPONENT))
                        return;

                    MenuRegistry.openExtendedMenu(player, new SimpleMenuProvider((id, inv, p) -> new SkillsRecordMenu(id, inv, p, stack, null), SkillsRecordItem.TITLE), buf -> {
                        ItemStack.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(buf, player.serverLevel().registryAccess()), stack);
                        buf.writeBoolean(false);
                    });
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
        public static final StreamCodec<FriendlyByteBuf, SelectCard> STREAM_CODEC;

        public static void handle(SelectCard msg, NetworkManager.PacketContext ctx) {
            ctx.queue(() -> {
                Player player = ctx.getPlayer();
                ItemStack stack = SkillsRecordPayload.getSkillsRecord(player);
                if (!stack.isEmpty()) {
                    SkillsRecordData.load(stack).setSelectedCard(msg.slot).save();
                }
            });
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        static {
            STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, SelectCard::slot, SelectCard::new);
        }
    }

    public record ToggleConsumeMode(boolean value) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ToggleConsumeMode> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("sync_consume_mode"));
        public static final StreamCodec<FriendlyByteBuf, ToggleConsumeMode> STREAM_CODEC;

        public static void handle(ToggleConsumeMode msg, NetworkManager.PacketContext ctx) {
            ctx.queue(() -> {
                Player player = ctx.getPlayer();
                ItemStack stack = SkillsRecordPayload.getSkillsRecord(player);
                if (!stack.isEmpty()) {
                    SkillsRecordData.load(stack).setConsumeMode(msg.value).save();
                }
            });
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        static {
            STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, ToggleConsumeMode::value, ToggleConsumeMode::new);
        }
    }

    public record PinChallenge(int cardSlot, ResourceLocation challengeId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PinChallenge> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("pin_challenge"));
        public static final StreamCodec<FriendlyByteBuf, PinChallenge> STREAM_CODEC;

        public static void handle(PinChallenge msg, NetworkManager.PacketContext ctx) {
            ctx.queue(() -> {
                Player player = ctx.getPlayer();
                ItemStack stack = SkillsRecordPayload.getSkillsRecord(player);
                if (!stack.isEmpty()) {
                    SkillsRecordData.load(stack).togglePinned(msg.cardSlot, msg.challengeId).save();
                }
            });
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        static {
            STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, PinChallenge::cardSlot, ResourceLocation.STREAM_CODEC, PinChallenge::challengeId, PinChallenge::new);
        }
    }

    public record EditChallenge(int cardSlot, ResourceLocation challengeId, int value) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<EditChallenge> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("edit_challenge"));
        public static final StreamCodec<FriendlyByteBuf, EditChallenge> STREAM_CODEC;

        public static void handle(EditChallenge msg, NetworkManager.PacketContext ctx) {
            ctx.queue(() -> {
                Player player = ctx.getPlayer();
                ItemStack stack = SkillsRecordPayload.getSkillsRecord(player);
                if (!stack.isEmpty()) {
                    SkillsRecordData data = SkillsRecordData.load(stack);
                    Optional<MiningSkillCardData> dataOpt = data.getCardData(msg.cardSlot);
                    if (dataOpt.isPresent()) {
                        MiningSkillCardData cardData = dataOpt.get();
                        cardData.setAmount(msg.challengeId, msg.value).onServerUpdate().save();
                        data.save();
                    }
                }
            });
        }

        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        static {
            STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, EditChallenge::cardSlot, ResourceLocation.STREAM_CODEC, EditChallenge::challengeId, ByteBufCodecs.INT, EditChallenge::value, EditChallenge::new);
        }
    }

    public record SyncData(int slotIndex, SkillsRecordData data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncData> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("skills_record_sync"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SyncData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, SyncData::slotIndex,
                SkillsRecordData.STREAM_CODEC, SyncData::data,
                SyncData::new
        );

        public static void handle(SyncData message, NetworkManager.PacketContext context) {
            context.queue(() -> {
                Player player = context.getPlayer();
                ItemStack stack = message.slotIndex == -1
                        ? ServicePlatform.get().slotAPI().getSkillsRecordItem(player)
                        : player.getSlot(message.slotIndex).get();

                if (stack.isEmpty() || !stack.is(Registration.SKILLS_RECORD.get()))
                    throw new IllegalArgumentException("The assigned slot index does not contain the skills record item!");

                message.data.setStack(stack).onClientUpdate().save();
            });
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
