package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.registry.level.entity.trade.TradeRegistry;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import static net.ixdarklord.ultimine_addition.config.ConfigHandler.COMMON.VILLAGER_CARD_TRADE_PRICE;
import static net.ixdarklord.ultimine_addition.config.ConfigHandler.COMMON.VILLAGER_CARD_TRADE_LEVEL;


public class TradesEvent {
    public static void init() {
        FTBUltimineAddition.LOGGER.debug("[Trade Tracker] Trades have been registered!");
        TradeRegistry.registerVillagerTrade(VillagerProfession.TOOLSMITH, VILLAGER_CARD_TRADE_LEVEL.get(), (trader, rand) ->
                new MerchantOffer(
                        new ItemCost(Items.EMERALD, rand.nextIntBetweenInclusive(VILLAGER_CARD_TRADE_PRICE.getMin(), VILLAGER_CARD_TRADE_PRICE.getMax())),
                        ModItems.MINING_SKILL_CARD_EMPTY.getDefaultInstance(),
                        4, 12, 0.09F
                )
        );
    }
}
