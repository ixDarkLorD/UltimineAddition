package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.level.entity.trade.TradeRegistry;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import static net.ixdarklord.ultimine_addition.config.ConfigHandler.SERVER.CARD_TRADE_PRICE;
import static net.ixdarklord.ultimine_addition.config.ConfigHandler.SERVER.VILLAGER_CARD_TRADE_LEVEL;


public class TradesEvent {
    public static void init() {
        LifecycleEvent.SERVER_BEFORE_START.register(minecraftServer -> {
            FTBUltimineAddition.LOGGER.debug("[Trade Tracker] Trades have been registered!");
            TradeRegistry.registerVillagerTrade(VillagerProfession.TOOLSMITH, VILLAGER_CARD_TRADE_LEVEL.get(), (trader, rand) ->
                    new MerchantOffer(
                            new ItemCost(Items.EMERALD, rand.nextIntBetweenInclusive(CARD_TRADE_PRICE.getValue().getFirst(), CARD_TRADE_PRICE.getValue().getLast())),
                            ModItems.MINING_SKILL_CARD_EMPTY.getDefaultInstance(),
                            4, 12, 0.09F
                    )
            );
        });
    }
}
