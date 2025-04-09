package net.ixdarklord.ultimine_addition.network.payloads;

import dev.architectury.networking.NetworkManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record MiningSkillCardPayload(int slotIndex, MiningSkillCardData data) implements CustomPacketPayload {
    public static final Type<MiningSkillCardPayload> TYPE = new Type<>(FTBUltimineAddition.rl("mining_skill_card_sync_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiningSkillCardPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MiningSkillCardPayload::slotIndex,
            MiningSkillCardData.STREAM_CODEC, MiningSkillCardPayload::data,
            MiningSkillCardPayload::new
    );

    public static void handle(MiningSkillCardPayload message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            ItemStack stack = player.getSlot(message.slotIndex).get();
            if (stack.isEmpty())
                throw new IllegalArgumentException("The assigned slot index does not contain the mining skill card item!");

            message.data.setDataHolder(stack).clientUpdate().saveData(stack);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record SyncBrewing(ItemStack stack) implements CustomPacketPayload {
        public static final Type<SyncBrewing> TYPE = new Type<>(FTBUltimineAddition.rl("mining_skill_card_sync_brewing"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncBrewing> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, SyncBrewing::stack,
                SyncBrewing::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(SyncBrewing message, NetworkManager.PacketContext context) {
            context.queue(() -> {
                Player player = context.getPlayer();
                if (player.containerMenu instanceof BrewingStandMenu standMenu) {
                    standMenu.setItem(3, 0, message.stack.copy());
                }
            });
        }
    }
}
