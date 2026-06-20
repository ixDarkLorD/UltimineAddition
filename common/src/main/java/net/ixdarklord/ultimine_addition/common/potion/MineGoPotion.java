package net.ixdarklord.ultimine_addition.common.potion;

import net.ixdarklord.coolcatlib.api.item.ComponentItem;
import net.ixdarklord.coolcatlib.api.item.ComponentItem.ComponentType;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import org.jetbrains.annotations.NotNull;

public class MineGoPotion extends Potion {
   private final MiningSkillCardItem.@NotNull Tier tier;
   private final ComponentItem.ComponentType componentType;

   public MineGoPotion(MiningSkillCardItem.@NotNull Tier tier, MobEffectInstance mobEffectInstance) {
      super(Util.make(() -> {
         ResourceLocation id = Registration.MOB_EFFECTS.getRegistrar().getId(mobEffectInstance.getEffect());
         MobEffect effect = Registration.MOB_EFFECTS.getRegistrar().get(id);
         IllegalArgumentException exception = new IllegalArgumentException("Unknown MobEffect: " + id);
         if (effect == null) {
            throw exception;
         } else {
            ResourceKey<MobEffect> resourceKey = Registration.MOB_EFFECTS.getRegistrar().getKey(effect).orElseThrow(() -> exception);
            return resourceKey.location().getPath();
         }
      }), mobEffectInstance);
      this.componentType = ComponentType.ABILITY;
      this.tier = tier;
   }

   public ComponentItem.ComponentType getComponentType() {
      return this.componentType;
   }

   public MiningSkillCardItem.@NotNull Tier getTier() {
      return this.tier;
   }

   public @NotNull String getName(@NotNull String prefix) {
      ResourceLocation id = Registration.POTIONS.getRegistrar().getId(this);

      assert id != null;

      String modifiedString = id.getPath().replaceAll("_\\d+$", "");
      return prefix + modifiedString;
   }
}
