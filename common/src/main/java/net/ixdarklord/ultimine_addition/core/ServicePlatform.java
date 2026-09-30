package net.ixdarklord.ultimine_addition.core;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public interface ServicePlatform {
    @ExpectPlatform
    static ServicePlatform get() {
        throw new UnsupportedOperationException("This method has not been implemented in the loader.");
    }

    SlotAPI slotAPI();

    Players players();

    interface SlotAPI {
        String getAPIName();

        boolean isModLoaded();

        ItemStack getSkillsRecordItem(Player player);
    }

    interface Players {
        // The player's stored data (a CoolCatLib attachment, synced to that player). Read it here; change it through
        // modifyAbilityData so the change is saved and synced.
        default PlayerAbilityData getAbilityData(Player player) {
            return Registration.PLAYER_ABILITY.get(player);
        }

        default void modifyAbilityData(Player player, Consumer<PlayerAbilityData> mutator) {
            Registration.PLAYER_ABILITY.modify(player, mutator);
        }

        default boolean isPlayerUltimineCapable(Player player) {
            return this.getAbilityData(player).getAbility();
        }

        default void setPlayerUltimineCapability(Player player, boolean state) {
            this.modifyAbilityData(player, data -> data.setAbility(state));
        }

        default Set<ResourceLocation> getUnlockedShapes(Player player, String tool) {
            return this.getAbilityData(player).getUnlockedShapes(tool);
        }

        default void unlockShapes(Player player, String tool, Collection<ResourceLocation> shapes, int certificateTier) {
            this.modifyAbilityData(player, data -> data.unlockShapes(tool, shapes, certificateTier));
        }

        boolean isCorrectToolForBlock(ItemStack stack, BlockState blockState);

        boolean isToolPaxel(ItemStack stack);
    }
}
