package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.ixdarklord.ultimine_addition.common.command.CardsCommand;
import net.ixdarklord.ultimine_addition.common.command.PlayerAbilityCommand;

public class CommandEvents {
    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, selection) -> {
            PlayerAbilityCommand.register(dispatcher, selection);
            CardsCommand.register(dispatcher, selection);
        });
    }
}
