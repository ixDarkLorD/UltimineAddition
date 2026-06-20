package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.registry.level.entity.trade.TradeRegistry;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

public final class TradesEvent {
    public static void init() {
        FTBUltimineAddition.LOGGER.debug("[Trade Tracker] Trades have been registered!");
        TradeRegistry.registerVillagerTrade(VillagerProfession.TOOLSMITH, ConfigHandler.COMMON.VILLAGER_CARD_TRADE_LEVEL.get(), (trader, rand) -> new MerchantOffer(new ItemStack(Items.EMERALD, rand.nextIntBetweenInclusive(ConfigHandler.COMMON.VILLAGER_CARD_TRADE_PRICE.getMin(), ConfigHandler.COMMON.VILLAGER_CARD_TRADE_PRICE.getMax())), ModItems.MINING_SKILL_CARD_EMPTY.getDefaultInstance(), 4, 12, 0.09F));
    }
}
