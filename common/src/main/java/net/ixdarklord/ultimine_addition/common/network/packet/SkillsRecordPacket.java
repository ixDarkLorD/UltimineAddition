package net.ixdarklord.ultimine_addition.common.network.packet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SkillsRecordPacket extends BaseS2CMessage {
    private final SkillsRecordData data;

    public SkillsRecordPacket(FriendlyByteBuf buf) {
        this(SkillsRecordData.fromNetwork(buf));
    }
    public SkillsRecordPacket(SkillsRecordData data) {
        this.data = data;
    }

    @Override
    public MessageType getType() {
        return PacketHandler.SYNC_SKILLS_RECORD;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        this.data.toNetwork(buf);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> data.saveData(data.get()));
    }

    public static class Toggle extends BaseC2SMessage {
        private final SkillsRecordData data;

        public Toggle(FriendlyByteBuf buf) {
            this(SkillsRecordData.fromNetwork(buf));
        }
        public Toggle(SkillsRecordData data) {
            this.data = data;
        }

        @Override
        public MessageType getType() {
            return PacketHandler.TOGGLE_SKILLS_RECORD;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            this.data.toNetwork(buf);
        }

        @Override
        public void handle(NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    ItemStack stack = ItemStack.EMPTY;
                    Inventory inv = player.getInventory();
                    int slotIndex = inv.findSlotMatchingItem(data.get());
                    if (player.getMainHandItem().getItem() instanceof SkillsRecordItem) {
                        stack = player.getMainHandItem();
                    } else if (player.getOffhandItem().getItem() instanceof SkillsRecordItem) {
                        stack = player.getOffhandItem();
                    } else if (slotIndex != -1 && inv.getItem(slotIndex).getItem() instanceof SkillsRecordItem)
                        stack = inv.getItem(slotIndex);

                    if (stack != ItemStack.EMPTY) {
                        data.syncData(player).saveData(stack);
                    }
                }
            });
        }
    }
}
