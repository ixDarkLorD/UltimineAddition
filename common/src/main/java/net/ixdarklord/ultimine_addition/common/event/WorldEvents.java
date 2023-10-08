package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.level.entity.trade.TradeRegistry;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.PlayerAbilityPacket;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

public class WorldEvents {
    public static void init() {
        EntityEvent.ADD.register((entity, level) -> {
            if (entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(new PlayerAbilityPacket(ServicePlatform.Players.isPlayerUltimineCapable(player)), player);
            }
            return EventResult.pass();
        });
        PlayerEvent.PLAYER_CLONE.register((oldPlayer, newPlayer, wonGame) -> {
            if (!wonGame) {
                boolean state = ServicePlatform.Players.isPlayerUltimineCapable(oldPlayer);
                ServicePlatform.Players.setPlayerUltimineCapability(newPlayer, state);
            }
        });

        TradeRegistry.registerVillagerTrade(VillagerProfession.TOOLSMITH, 5, (trader, rand) ->
                new MerchantOffer(new ItemStack(Items.EMERALD, rand.nextInt(24, 48)), ModItems.MINING_SKILL_CARD_EMPTY.getDefaultInstance(), 4, 12, 0.09F));
    }
}
