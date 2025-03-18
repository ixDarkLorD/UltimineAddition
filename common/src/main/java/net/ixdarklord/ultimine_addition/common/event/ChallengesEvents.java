package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.ixdarklord.coolcatlib.api.util.SlotReference;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.challenge.IneligibleBlocksSavedData;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.common.event.impl.DatapackEvents;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.SyncChallengesPacket;
import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;

import java.util.List;
import java.util.Optional;

public class ChallengesEvents {
    public static void init() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(serverLevel -> {
            if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) {
                legacyFunctions();
            }
        });

        ReloadListenerRegistry.register(PackType.SERVER_DATA, ChallengesManager.INSTANCE);

        DatapackEvents.TAG_UPDATE.register((registryAccess, updateCause, shouldUpdateStaticData) -> {
            if (updateCause == DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD) {
                ChallengesManager.INSTANCE.validateAllChallenges();
            }
        });

        DatapackEvents.SYNC.register((player, isJoined) -> {
            if (!ChallengesManager.INSTANCE.getAllChallenges().isEmpty()) {
                PacketHandler.sendToPlayer(new SyncChallengesPacket(ChallengesManager.INSTANCE.getAllChallenges()), player);
            }
        });
    }

    private static void legacyFunctions() {
        BlockEvent.BREAK.register((level, pos, state, pl, xp) -> {
            if (!(pl instanceof ServerPlayer player)) return EventResult.pass();

            List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, ModItems.MINER_CERTIFICATE);
            if (slots.isEmpty()
                    || !(state.is(PlatformTags.get().ORES()))
                    || (FTBUltimine.instance.canUltimine(player) && FTBUltimine.instance.getOrCreatePlayerData(player).isPressed())
                    || IneligibleBlocksSavedData.getOrCreate(player.serverLevel()).isBlockPlacedByEntity(pos)
            ) return EventResult.pass();

            for (SlotReference.Player slot : slots) {
                MinerCertificateData data = MinerCertificateData.loadData(slot.getItem());
                Optional<MinerCertificateData.Legacy> legacy = data.getLegacy();
                if (legacy.isPresent()) {
                    legacy.get().addPoint(1);
                    data.sendToClient(player).saveData(slot.getItem());
                }
            }
            return EventResult.pass();
        });
    }
}
