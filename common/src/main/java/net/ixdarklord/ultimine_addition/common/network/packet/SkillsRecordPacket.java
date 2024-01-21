package net.ixdarklord.ultimine_addition.common.network.packet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.registry.menu.MenuRegistry;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
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

    public static class Open extends BaseC2SMessage {
        public Open(FriendlyByteBuf ignored) {}
        public Open() {}

        @Override
        public MessageType getType() {
            return PacketHandler.OPEN_SKILLS_RECORD;
        }

        @Override
        public void write(FriendlyByteBuf buf) {}

        @Override
        public void handle(NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    ItemStack stack = ServicePlatform.SlotAPI.getSkillsRecordItem(player);
                    if (stack.getItem() instanceof SkillsRecordItem) {
                        if (stack.hasTag()) {
                            MenuRegistry.openExtendedMenu(player,
                                    new SimpleMenuProvider((id, inv, p) -> new SkillsRecordMenu(id, inv, p, stack, true), SkillsRecordItem.TITLE),
                                    buf -> {
                                        buf.writeItem(stack);
                                        buf.writeBoolean(true);
                                    });
                        }
                    }
                }
            });
        }
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
                    if (player.getOffhandItem().getItem() instanceof SkillsRecordItem) {
                        stack = player.getOffhandItem();
                    } else if (player.getMainHandItem().getItem() instanceof SkillsRecordItem) {
                        stack = player.getMainHandItem();
                    }

                    if (ServicePlatform.SlotAPI.getSkillsRecordItem(player).getItem() instanceof SkillsRecordItem) {
                        SkillsRecordData recordData = new SkillsRecordData().loadData(stack);
                        if (recordData.getUUID() == null || !recordData.getUUID().equals(data.getUUID())) {
                            stack = ServicePlatform.SlotAPI.getSkillsRecordItem(player);
                        }
                    }

                    if (stack != ItemStack.EMPTY) {
                        data.syncData(player).saveData(stack);
                    }
                }
            });
        }
    }
}
