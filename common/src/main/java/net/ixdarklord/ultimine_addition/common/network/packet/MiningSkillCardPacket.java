package net.ixdarklord.ultimine_addition.common.network.packet;

import dev.architectury.networking.NetworkManager;
import net.ixdarklord.coolcatlib.api.util.InventoryHelper;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record MiningSkillCardPacket(MiningSkillCardData data, ItemStack stack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MiningSkillCardPacket> TYPE = new CustomPacketPayload.Type<>(UltimineAddition.getLocation("mining_skill_card_sync_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiningSkillCardPacket> STREAM_CODEC = StreamCodec.composite(
            MiningSkillCardData.STREAM_CODEC, MiningSkillCardPacket::data,
            ItemStack.STREAM_CODEC, MiningSkillCardPacket::stack,
            MiningSkillCardPacket::new
    );

    public static void handle(MiningSkillCardPacket message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            try {
                Player player = context.getPlayer();
                List<ItemStack> stacks = new ArrayList<>(InventoryHelper.listMatchingItem(player.getInventory(), message.stack.getItem()));
                stacks.addAll(ItemUtils.listMatchingItem(player, Registration.SKILLS_RECORD.get()).stream()
                        .map(stack -> SkillsRecordData.loadData(stack).getContainer().getItems())
                        .flatMap(Collection::stream)
                        .toList());

                for (ItemStack stack : stacks) {
                    var data1 = message.stack.get(MiningSkillCardData.DATA_COMPONENT);
                    var data2 = stack.get(MiningSkillCardData.DATA_COMPONENT);
                    if (data1 == null) break;

                    if (data2 != null && data1.getUUID().equals(data2.getUUID())) {
                        message.data.setDataHolder(stack).clientUpdate().saveData(stack);
                        break;
                    }
                }
            } catch (Exception err) {
                UltimineAddition.LOGGER.error("Error occurred in the payload!", err);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
