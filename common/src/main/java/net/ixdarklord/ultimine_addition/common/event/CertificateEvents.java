package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.ixdarklord.coolcatlib.api.utils.SlotReference;
import net.ixdarklord.ultimine_addition.common.data.challenge.IneligibleBlocksSavedData;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class CertificateEvents {
    public static void init() {
        legacyFunctions();
    }

    private static void legacyFunctions() {
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (!state.is(PlatformTags.get().ORES())) {
                return EventResult.pass();
            } else if (!player.isCreative() && IneligibleBlocksSavedData.getOrCreate(player.serverLevel()).isBlockPlacedByEntity(pos)) {
                return EventResult.pass();
            } else {
                List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, ModItems.MINER_CERTIFICATE, true);
                if (!slots.isEmpty()) {
                    for (SlotReference.Player slot : slots) {
                        ItemStack stack = slot.get();
                        if (MinerCertificateData.hasData(stack)) {
                            MinerCertificateData data = MinerCertificateData.load(stack);
                            Optional<MinerCertificateData.Legacy> legacy = data.getLegacy();
                            if (legacy.isPresent()) {
                                legacy.get().addPoint(1);
                                data.sendToClient(slot.getIndex(), player).save();
                            }
                        }
                    }

                }
                return EventResult.pass();
            }
        });
    }
}
