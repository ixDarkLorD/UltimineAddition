package net.ixdarklord.ultimine_addition.common.advancement;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

public class AdvancementTriggers {
    public static PlayerTrigger.TriggerInstance advancementTrigger(Advancement advancement) {
        return advancementTrigger(advancement.getId());
    }

    public static PlayerTrigger.TriggerInstance advancementTrigger(ResourceLocation name) {
        ContextAwarePredicate predicate = ContextAwarePredicate.create(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                EntityPredicate.Builder.entity().subPredicate(PlayerPredicate.Builder.player().checkAdvancementDone(name, true).build())).build());
        return new PlayerTrigger.TriggerInstance(CriteriaTriggers.TICK.getId(), predicate);
    }

    public static TradeTrigger.TriggerInstance tradedWithVillager(ItemPredicate itemPredicate) {
        return new TradeTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, itemPredicate);
    }
}
