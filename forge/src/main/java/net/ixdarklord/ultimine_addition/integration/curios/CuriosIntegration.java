package net.ixdarklord.ultimine_addition.integration.curios;

import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public class CuriosIntegration {
   public static ItemStack getItem(Player player) {
      return CuriosApi.getCuriosInventory(player).resolve().map((IH) -> IH.findFirstCurio(Registration.SKILLS_RECORD.get()).map(SlotResult::stack).orElse(ItemStack.EMPTY)).orElse(ItemStack.EMPTY);
   }

   public static void setItem(Player player, ItemStack stack) {
      Optional<ICuriosItemHandler> resolve = CuriosApi.getCuriosInventory(player).resolve();
      resolve.ifPresent(itemHandler -> itemHandler.setEquippedCurio("skills_record", 0, stack));
   }
}
