package net.ixdarklord.ultimine_addition.integration.curios;

import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class CuriosIntegration {
    public static ItemStack getSkillsRecordItem(Player player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .map(IH -> IH.findFirstCurio(Registration.SKILLS_RECORD.get())
                .map(SlotResult::stack).orElse(ItemStack.EMPTY))
                .orElse(ItemStack.EMPTY);
    }
}
