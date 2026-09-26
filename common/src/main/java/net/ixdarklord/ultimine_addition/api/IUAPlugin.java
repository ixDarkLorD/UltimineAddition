package net.ixdarklord.ultimine_addition.api;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public interface IUAPlugin {
    Identifier getUid();
    void register(Registration registration);

    class Registration {
        private final Collection<MiningSkillCardItem.Type> types = new HashSet<>();
        private final Identifier ID;

        public Registration(IUAPlugin plugin) {
            this.ID = plugin.getUid();
        }

        public void registerType(String name, ItemStack defaultDisplayItem) {
//            types.add(new MiningSkillCardItem.Type(this.ID.getNamespace() + ":" + name, defaultDisplayItem));
        }

        public Collection<MiningSkillCardItem.Type> getTypes() {
            return types;
        }
    }
}
