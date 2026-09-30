package net.ixdarklord.ultimine_addition.integration.curios;

import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class CuriosIntegration {

    public static ItemStack getSkillsRecord(Player player) {
        // Curios 5 on 1.20.1 hands the inventory out as a LazyOptional.
        return CuriosApi.getCuriosInventory(player).resolve()
                .map(inv -> inv.findFirstCurio(ModItems.SKILLS_RECORD)
                        .map(SlotResult::stack)
                        .orElse(ItemStack.EMPTY))
                .orElse(ItemStack.EMPTY);
    }
}
