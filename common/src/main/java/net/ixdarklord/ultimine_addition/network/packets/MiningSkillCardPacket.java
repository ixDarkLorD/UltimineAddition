package net.ixdarklord.ultimine_addition.network.packets;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.ItemStack;

public final class MiningSkillCardPacket extends BaseS2CMessage {
    private final int slotIndex;
    private final MiningSkillCardData data;

    public MiningSkillCardPacket(int slotIndex, MiningSkillCardData data) {
        this.slotIndex = slotIndex;
        this.data = data;
    }

    public MiningSkillCardPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readJsonWithCodec(MiningSkillCardData.CODEC).readFinishedChallenges(buf));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.slotIndex);
        buf.writeJsonWithCodec(MiningSkillCardData.CODEC, this.data);
        this.data.writeFinishedChallenges(buf);
    }

    public MessageType getType() {
        return PacketHandler.SYNC_MINING_SKILL_CARD;
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            ItemStack stack = player.getSlot(this.slotIndex).get();
            if (stack.isEmpty()) {
                throw new IllegalArgumentException("The assigned slot index does not contain the mining skill card item!");
            } else {
                this.data.setStack(stack).onClientUpdate().save();
            }
        });
    }

    public static final class SyncBrewing extends BaseS2CMessage {
        private final ItemStack stack;

        public SyncBrewing(ItemStack stack) {
            this.stack = stack;
        }

        public SyncBrewing(FriendlyByteBuf buf) {
            this(buf.readItem());
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeItem(this.stack);
        }

        public MessageType getType() {
            return PacketHandler.SYNC_BREWING;
        }

        public void handle(NetworkManager.PacketContext context) {
            context.queue(() -> {
                Player player = context.getPlayer();
                AbstractContainerMenu abstractContainerMenu = player.containerMenu;
                if (abstractContainerMenu instanceof BrewingStandMenu standMenu) {
                    standMenu.setItem(3, 0, this.stack.copy());
                }

            });
        }
    }
}
