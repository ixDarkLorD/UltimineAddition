package net.ixdarklord.ultimine_addition.common.event;

import net.ixdarklord.coolcatcore.api.event.v2.common.BlockEvents;
import net.ixdarklord.coolcatcore.api.event.v2.common.ServerLifecycleEvents;
import net.ixdarklord.coolcatcore.api.event.v2.core.EventResult;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.resources.Identifier;
import net.ixdarklord.coolcatcore.api.utils.SlotReference;
import net.ixdarklord.ultimine_addition.common.data.challenge.IneligibleBlocksSavedData;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.ixdarklord.ultimine_addition.util.ItemUtils;

import java.util.List;
import java.util.Optional;

public class CertificateEvents {
    public static void init() {
        legacyFunctions();
        // Let pack makers see where plugin shapes ended up.
        ServerLifecycleEvents.STARTED.register(server -> {
            List<Identifier> extra = ShapeCertificateItem.getUnlistedShapes();
            if (extra.isEmpty()) return;
            MiningSkillCardItem.Tier tier = UAServerConfig.EXTRA_SHAPES_CERTIFICATE.get().tier();
            FTBUltimineAddition.LOGGER.info("Shapes from other mods {}: {}", tier == null ? "left to the Miner Certificate" : "added to the " + tier.name() + " Shape Certificate", extra);
        });
    }

    private static void legacyFunctions() {
        BlockEvents.BREAK.register((level, pos, state, player) -> {
            if (!state.is(PlatformTags.get().ORES())) return EventResult.pass();
            if (!player.isCreative() && IneligibleBlocksSavedData.getOrCreate(player.level()).isBlockPlacedByEntity(pos)) return EventResult.pass();

            List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, ModItems.MINER_CERTIFICATE, true);
            if (slots.isEmpty()) return EventResult.pass();

            for (SlotReference.Player slot : slots) {
                MinerCertificateData data = MinerCertificateData.load(slot.get());
                Optional<MinerCertificateData.Legacy> legacy = data.getLegacy();
                if (legacy.isPresent()) {
                    legacy.get().addPoint(1);
                    data.sendToClient(slot.getIndex(), player).save();
                }
            }
            return EventResult.pass();
        });
    }
}
