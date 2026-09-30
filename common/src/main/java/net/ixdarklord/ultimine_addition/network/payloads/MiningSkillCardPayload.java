package net.ixdarklord.ultimine_addition.network.payloads;

import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
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

public final class MiningSkillCardPayload {
    private MiningSkillCardPayload() {
    }

    public record SyncBrewing(ItemStack stack) implements CustomPacketPayload {
        public static final Type<SyncBrewing> TYPE = new Type<>(FTBUltimineAddition.id("mining_skill_card_sync_brewing"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncBrewing> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, SyncBrewing::stack,
                SyncBrewing::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(SyncBrewing message, PacketContext context) {
            context.queue(() -> {
                Player player = context.getPlayer();
                if (player.containerMenu instanceof BrewingStandMenu standMenu) {
                    standMenu.setItem(3, 0, message.stack.copy());
                }
            });
        }
    }
}
