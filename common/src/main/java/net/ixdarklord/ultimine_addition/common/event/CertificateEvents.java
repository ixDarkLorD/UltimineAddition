package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.ixdarklord.coolcatlib.api.util.SlotReference;
import net.ixdarklord.ultimine_addition.common.data.challenge.IneligibleBlocksSavedData;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

public class CertificateEvents {
    public static void init() {
        if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) {
            legacyFunctions();
        }
    }

    private static void legacyFunctions() {
        BlockEvent.BREAK.register((level, pos, state, pl, xp) -> {
            if (!(pl instanceof ServerPlayer player)) return EventResult.pass();
            if (!state.is(PlatformTags.get().ORES())) return EventResult.pass();
            if (!player.isCreative() && IneligibleBlocksSavedData.getOrCreate(player.serverLevel()).isBlockPlacedByEntity(pos)) return EventResult.pass();

            List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, ModItems.MINER_CERTIFICATE, true);
            if (slots.isEmpty()) return EventResult.pass();

            for (SlotReference.Player slot : slots) {
                System.out.println(slot.getIndex());
                MinerCertificateData data = MinerCertificateData.loadData(slot.getItem());
                Optional<MinerCertificateData.Legacy> legacy = data.getLegacy();
                if (legacy.isPresent()) {
                    legacy.get().addPoint(1);
                    data.sendToClient(slot.getIndex(), player).saveData(slot.getItem());
                }
            }
            return EventResult.pass();
        });
    }
}
