package net.ixdarklord.ultimine_addition.common.network.payloads;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.utils.Env;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SkillsRecordPayload {
    public static class Open implements CustomPacketPayload {
        public static final Type<SkillsRecordPayload.Open> TYPE = new Type<>(FTBUltimineAddition.rl("open_skills_record"));
        public static final StreamCodec<FriendlyByteBuf, Open> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull Open decode(FriendlyByteBuf object) {
                return new Open();
            }

            @Override
            public void encode(FriendlyByteBuf object, Open object2) {
            }
        };

        public static void handle(Open ignored, NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    ItemStack stack = ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
                    if (stack.getItem() instanceof SkillsRecordItem) {
                        if (stack.has(SkillsRecordData.DATA_COMPONENT)) {
                            MenuRegistry.openExtendedMenu(player,
                                    new SimpleMenuProvider((id, inv, p) -> new SkillsRecordMenu(id, inv, p, stack, null), SkillsRecordItem.TITLE),
                                    buf -> {
                                        ItemStack.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(buf, player.serverLevel().registryAccess()), stack);
                                        buf.writeBoolean(false);
                                    });
                        }
                    }
                }
            });
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SyncData(Env env, int slotIndex, SkillsRecordData data) implements CustomPacketPayload {
        public static final Type<SyncData> S2C_TYPE = new Type<>(FTBUltimineAddition.rl("skills_record_sync_s2c"));
        public static final Type<SyncData> C2S_TYPE = new Type<>(FTBUltimineAddition.rl("skills_record_sync_c2s"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SyncData> STREAM_CODEC = StreamCodec.composite(
                NetworkHelper.enumStreamCodec(Env.class), SyncData::env,
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

                if (stack.isEmpty())
                    throw new IllegalArgumentException("The assigned slot index does not contain the skills record item!");

                if (context.getEnvironment() == Env.SERVER) {
                    message.data.setDataHolder(stack).syncData((ServerPlayer) player).saveData(stack);
                    return;
                }
                message.data.setDataHolder(stack).clientUpdate().saveData(stack);
            });
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return switch (env) {
                case CLIENT -> S2C_TYPE;
                case SERVER -> C2S_TYPE;
            };
        }
    }
}
