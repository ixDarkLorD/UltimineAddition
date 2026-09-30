package net.ixdarklord.ultimine_addition.api;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public interface IUAPlugin {
    ResourceLocation getUid();
    void register(Registration registration);

    class Registration {
        private final Collection<MiningSkillCardItem.Type> types = new HashSet<>();
        private final ResourceLocation ID;

        public Registration(IUAPlugin plugin) {
            this.ID = plugin.getUid();
        }

        public void registerType(String name) {
//            types.add(new MiningSkillCardItem.Type(this.ID.getNamespace() + ":" + name));
        }

        /** @deprecated Cards no longer show a display item; use {@link #registerType(String)}. */
        @Deprecated
        public void registerType(String name, ItemStack defaultDisplayItem) {
            this.registerType(name);
        }

        public Collection<MiningSkillCardItem.Type> getTypes() {
            return types;
        }
    }
}
