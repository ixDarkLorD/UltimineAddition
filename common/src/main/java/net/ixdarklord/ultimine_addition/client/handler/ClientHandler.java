package net.ixdarklord.ultimine_addition.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientHandler {
    public static Player getPlayer() {
        return Minecraft.getInstance().player;
    }
}
