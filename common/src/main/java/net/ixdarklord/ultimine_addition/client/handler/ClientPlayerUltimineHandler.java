package net.ixdarklord.ultimine_addition.client.handler;

import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientPlayerUltimineHandler {
    public static void setCapability(boolean state) {
        Player player = Minecraft.getInstance().player;
        if (player != null) ServicePlatform.Players.setPlayerUltimineCapability(player, state);
    }
}
