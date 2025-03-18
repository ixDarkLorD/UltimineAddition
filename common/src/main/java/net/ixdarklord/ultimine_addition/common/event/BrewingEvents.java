package net.ixdarklord.ultimine_addition.common.event;

import net.ixdarklord.ultimine_addition.common.brewing.MineGoJuiceRecipe;

public class BrewingEvents {
    public static void init() {
        MineGoJuiceRecipe.register();
    }
}
