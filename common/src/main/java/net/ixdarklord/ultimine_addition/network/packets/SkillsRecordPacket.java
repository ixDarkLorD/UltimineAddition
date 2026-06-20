package net.ixdarklord.ultimine_addition.network.packets;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.registry.menu.MenuRegistry;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class SkillsRecordPacket {
    private static ItemStack getSkillsRecord(Player p) {
        if (p instanceof ServerPlayer player) {
            AbstractContainerMenu abstractContainerMenu = player.containerMenu;
            if (abstractContainerMenu instanceof SkillsRecordMenu menu) {
                InteractionHand hand = menu.interactionHand;
                ItemStack stack;
                if (hand == null) {
                    stack = ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
                } else {
                    stack = player.getItemInHand(hand);
                }

                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static class Open extends BaseC2SMessage {
        public Open() {
        }

        public Open(FriendlyByteBuf ignored) {
        }

        public void write(FriendlyByteBuf buf) {
        }

        public MessageType getType() {
            return PacketHandler.OPEN_SKILLS_RECORD;
        }

        public void handle(NetworkManager.PacketContext context) {
            context.queue(() -> {
                Player player2 = context.getPlayer();
                if (player2 instanceof ServerPlayer player) {
                    ItemStack stack = ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
                    if (SkillsRecordData.hasData(stack)) {
                        MenuRegistry.openExtendedMenu(player, new SimpleMenuProvider((id, inv, p) -> new SkillsRecordMenu(id, inv, p, stack, null), SkillsRecordItem.TITLE), (buf) -> {
                            buf.writeItem(stack);
                            buf.writeBoolean(false);
                        });
                    }
                }

            });
        }
    }

    public static class SelectCard extends BaseC2SMessage {
        private final int selectedSlot;

        public SelectCard(int selectedSlot) {
            this.selectedSlot = selectedSlot;
        }

        public SelectCard(FriendlyByteBuf buf) {
            this(buf.readInt());
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeInt(this.selectedSlot);
        }

        public MessageType getType() {
            return PacketHandler.SELECT_CARD;
        }

        public void handle(NetworkManager.PacketContext context) {
            context.queue(() -> {
                Player player = context.getPlayer();
                ItemStack stack = SkillsRecordPacket.getSkillsRecord(player);
                if (!stack.isEmpty()) {
                    SkillsRecordData.load(stack).setSelectedCard(this.selectedSlot).save();
                }
            });
        }
    }

    public static class ToggleConsumeMode extends BaseC2SMessage {
        private final boolean value;

        public ToggleConsumeMode(boolean value) {
            this.value = value;
        }

        public ToggleConsumeMode(FriendlyByteBuf buf) {
            this(buf.readBoolean());
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeBoolean(this.value);
        }

        public MessageType getType() {
            return PacketHandler.TOGGLE_CONSUME_MODE;
        }

        public void handle(NetworkManager.PacketContext context) {
            context.queue(() -> {
                Player player = context.getPlayer();
                ItemStack stack = SkillsRecordPacket.getSkillsRecord(player);
                if (!stack.isEmpty()) {
                    SkillsRecordData.load(stack).setConsumeMode(this.value).save();
                }
            });
        }
    }

    public static class PinChallenge extends BaseC2SMessage {
        private final int cardSlot;
        private final ResourceLocation challengeId;

        public PinChallenge(int cardSlot, ResourceLocation challengeId) {
            this.cardSlot = cardSlot;
            this.challengeId = challengeId;
        }

        public PinChallenge(FriendlyByteBuf buf) {
            this(buf.readInt(), buf.readResourceLocation());
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeInt(this.cardSlot);
            buf.writeResourceLocation(this.challengeId);
        }

        public MessageType getType() {
            return PacketHandler.PIN_CHALLENGE;
        }

        public void handle(NetworkManager.PacketContext context) {
            context.queue(() -> {
                Player player = context.getPlayer();
                ItemStack stack = SkillsRecordPacket.getSkillsRecord(player);
                if (!stack.isEmpty()) {
                    SkillsRecordData.load(stack).togglePinned(this.cardSlot, this.challengeId).save();
                }
            });
        }
    }

    public static class EditChallenge extends BaseC2SMessage {
        private final int cardSlot;
        private final ResourceLocation challengeId;
        private final int value;

        public EditChallenge(int cardSlot, ResourceLocation challengeId, int value) {
            this.cardSlot = cardSlot;
            this.challengeId = challengeId;
            this.value = value;
        }

        public EditChallenge(FriendlyByteBuf buf) {
            this(buf.readInt(), buf.readResourceLocation(), buf.readInt());
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeInt(this.cardSlot);
            buf.writeResourceLocation(this.challengeId);
            buf.writeInt(this.value);
        }

        public MessageType getType() {
            return PacketHandler.EDIT_CHALLENGE;
        }

        public void handle(NetworkManager.PacketContext context) {
            context.queue(() -> {
                Player player = context.getPlayer();
                ItemStack stack = SkillsRecordPacket.getSkillsRecord(player);
                if (!stack.isEmpty()) {
                    SkillsRecordData data = SkillsRecordData.load(stack);
                    Optional<MiningSkillCardData> dataOpt = data.getCardData(this.cardSlot);
                    if (dataOpt.isPresent()) {
                        dataOpt.get().setAmount(this.challengeId, this.value).onServerUpdate().save();
                        data.save();
                    }
                }
            });
        }
    }

    public static class SyncData extends BaseS2CMessage {
        private final int slotIndex;
        private final SkillsRecordData data;

        public SyncData(FriendlyByteBuf buf) {
            this(buf.readInt(), SkillsRecordData.readBuffer(buf));
        }

        public SyncData(int slotIndex, SkillsRecordData data) {
            this.slotIndex = slotIndex;
            this.data = data;
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeInt(this.slotIndex);
            this.data.writeBuffer(buf);
        }

        public MessageType getType() {
            return PacketHandler.SYNC_SKILLS_RECORD;
        }

        public void handle(NetworkManager.PacketContext context) {
            context.queue(() -> {
                Player player = context.getPlayer();
                ItemStack stack = this.slotIndex == -1 ? ServicePlatform.get().slotAPI().getSkillsRecordItem(player) : player.getSlot(this.slotIndex).get();
                if (!stack.isEmpty() && stack.is(Registration.SKILLS_RECORD.get())) {
                    this.data.setStack(stack).onClientUpdate().save();
                } else {
                    throw new IllegalArgumentException("The assigned slot index does not contain the skills record item!");
                }
            });
        }
    }
}
