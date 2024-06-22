package net.ixdarklord.ultimine_addition.client.handler;

import net.ixdarklord.ultimine_addition.client.event.impl.ClientTooltipComponentRegister;
import net.ixdarklord.ultimine_addition.client.gui.component.ClientSkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.gui.component.SkillsRecordTooltip;

public class CustomClientTooltipComponent {
    public static void init() {
        ClientTooltipComponentRegister.EVENT.register(tooltipComponent -> {
            if (tooltipComponent instanceof SkillsRecordTooltip skillsRecordTooltip) {
                return new ClientSkillsRecordTooltip(skillsRecordTooltip);
            }
            if (tooltipComponent instanceof SkillsRecordTooltip.Option option) {
                return new ClientSkillsRecordTooltip.Option(option);
            }
            return null;
        });
    }
}
