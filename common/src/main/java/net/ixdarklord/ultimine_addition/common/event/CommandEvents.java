package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.ixdarklord.ultimine_addition.common.command.CardsCommand;
import net.ixdarklord.ultimine_addition.common.command.PlayerAbilityCommand;

public class CommandEvents {
    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            PlayerAbilityCommand.register(dispatcher, registry, selection);
            CardsCommand.register(dispatcher, registry, selection);
        });
    }
}
