package net.ixdarklord.ultimine_addition.common.item;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

// The Mining Skill Card of every type a data pack defines (data/<namespace>/mining_skill_cards): one item, the type on
// the stack. Items can't be registered from data packs, so the cards of data pack types share this one. It is in no
// creative tab (so not in JEI either): it is crafted from an empty card and one of the type's tools. A card whose type
// is gone (its data pack removed) is inert, like an empty card, until the type is back.
public class GenericMiningSkillCardItem extends MiningSkillCardItem {
    // The card type's id, like "mypack:hammer".
    public static final DataComponentType<String> TYPE_COMPONENT = DataComponentType.<String>builder()
            .persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build();

    public GenericMiningSkillCardItem(Properties properties) {
        super(Type.GENERIC, properties);
    }

    public static ItemStack create(Type type) {
        ItemStack stack = new ItemStack(ModItems.MINING_SKILL_CARD_GENERIC);
        stack.set(TYPE_COMPONENT, type.getId());
        return stack;
    }

    @Override
    public Type getType(ItemStack stack) {
        String id = stack.get(TYPE_COMPONENT);
        Type type = id == null ? null : Type.byId(id);
        return type != null && type.isData() ? type : Type.EMPTY;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        Type type = this.getType(stack);
        return Component.translatable("item.ultimine_addition.mining_skill_card_generic", type == Type.EMPTY
                ? Component.translatable("item.ultimine_addition.mining_skill_card_generic.unknown") : type.displayName());
    }
}
