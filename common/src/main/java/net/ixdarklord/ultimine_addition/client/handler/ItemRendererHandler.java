package net.ixdarklord.ultimine_addition.client.handler;

import net.ixdarklord.ultimine_addition.client.renderer.item.MiningSkillCardItemRenderer;
import net.ixdarklord.ultimine_addition.client.renderer.item.UAItemRenderer;

public class ItemRendererHandler {
    public static UAItemRenderer MiningSkillCardRenderer() {
        return new MiningSkillCardItemRenderer();
    }
}
