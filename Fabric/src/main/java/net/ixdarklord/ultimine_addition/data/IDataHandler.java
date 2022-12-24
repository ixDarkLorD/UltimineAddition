package net.ixdarklord.ultimine_addition.data;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.data.player.PlayerUltimineData;

public interface IDataHandler {
    PlayerUltimineData getPlayerUltimineData();
    String NBT_PATH = Constants.MOD_ID+":properties";
}
