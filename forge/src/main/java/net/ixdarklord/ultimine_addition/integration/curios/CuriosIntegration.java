package net.ixdarklord.ultimine_addition.integration.curios;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.SlotTypeMessage;

public class CuriosIntegration {
    public static void sendIMC() {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () ->
                new SlotTypeMessage.Builder("skills_record")
                        .priority(1).size(1)
                        .icon(Constants.getLocation("item/skills_record_slot"))
                        .build());
    }

    public static ItemStack getSkillsRecordItem(Player player) {
        return CuriosApi.getCuriosHelper().findFirstCurio(player, Registration.SKILLS_RECORD.get()).map(SlotResult::stack).orElse(ItemStack.EMPTY);
    }
}
