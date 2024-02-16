package net.ixdarklord.ultimine_addition.common.advancement;

import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

public class AdvancementTriggers {
    public static InventoryChangeTrigger.TriggerInstance inventoryHas(ItemLike arg) {
        return inventoryTrigger(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(new ItemLike[]{arg}).build());
    }

    public static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... args) {
        return new InventoryChangeTrigger.TriggerInstance(ContextAwarePredicate.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, args);
    }

    public static PlayerTrigger.TriggerInstance advancementTrigger(Advancement advancement) {
        return advancementTrigger(advancement.getId().getPath());
    }

    public static PlayerTrigger.TriggerInstance advancementTrigger(String name) {
        return new PlayerTrigger.TriggerInstance(CriteriaTriggers.TICK.getId(),
                ContextAwarePredicate.create(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity().subPredicate(PlayerPredicate.Builder.player().checkAdvancementDone(UltimineAddition.getLocation(name), true).build())).build()));
    }

    public static TradeTrigger.TriggerInstance tradedWithVillager(ItemPredicate.Builder itemPredicate) {
        return new TradeTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, itemPredicate.build());
    }
}
