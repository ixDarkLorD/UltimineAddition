package net.ixdarklord.ultimine_addition.network.payloads;

import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.coolcatcore.api.platform.Platform;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// The server's data pack Mining Skill Card types, for the client: names, tools, icons and juice colors. Sent on join
// and after a data pack reload, before the challenges (which name card types); an empty list clears them. Its codec
// comes from the record (CoolCatLib's PayloadCodecs).
public record SyncCardTypesPayload(List<Entry> types) implements CustomPacketPayload {
    public static final Type<SyncCardTypesPayload> TYPE = new Type<>(FTBUltimineAddition.id("sync_card_types"));

    public record Entry(String id, String name, List<String> tools, int juiceColor, String juiceName, String icon) {
    }

    public static SyncCardTypesPayload current() {
        return new SyncCardTypesPayload(MiningSkillCardItem.Type.getDataTypes().stream()
                .map(type -> new Entry(type.getId(), type.getName(), type.getRequiredTools(), type.getPotionColor().getRGB() & 0xFFFFFF, type.getJuiceName(), type.getIcon()))
                .toList());
    }

    public static void handle(SyncCardTypesPayload message, PacketContext context) {
        context.queue(() -> {
            // With a server in this game the types are the ones it loaded already.
            if (Platform.getServer() != null) return;
            MiningSkillCardItem.Type.setDataTypes(message.types.stream()
                    .map(entry -> MiningSkillCardItem.Type.data(entry.id(), entry.name(), entry.tools(), entry.juiceColor(), entry.juiceName(), entry.icon()))
                    .toList());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
