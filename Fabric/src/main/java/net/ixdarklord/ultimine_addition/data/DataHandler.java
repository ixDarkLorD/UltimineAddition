package net.ixdarklord.ultimine_addition.data;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.data.player.PlayerUltimineData;

public class DataHandler {
    public static PlayerUltimineData ultimineData;
    public static final String NBT_PATH = Constants.MOD_ID+":properties";

    public static void initialize() {
        ultimineData = new PlayerUltimineData();
    }
}
