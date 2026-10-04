package net.ixdarklord.ultimine_addition.common.data.card;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.LOGGER;

// The Mining Skill Card types data packs define (data/<namespace>/mining_skill_cards/<name>.json): a tool of its own,
// with its card, its challenges (challenge files naming it in "for_card_type"), its Shape Certificates and its Mine-Go
// Juice. The type's id is the file's, like "mypack:hammer".
//
//   {
//     "name": "Hammer",                                (or a translation key)
//     "tools": ["mymod:iron_hammer", "#mymod:hammers"],
//     "icon": "mymod:iron_hammer",
//     "juice_color": "#C0C0C0",
//     "juice_name": "Mine-Go Juice: Smash Hit"
//   }
//
// They are read together with the challenges (ChallengesManager loads them first: challenges name card types), and sent
// to every player (SyncCardTypesPayload).
public final class DataCardTypes {
    private static final FileToIdConverter FILES = FileToIdConverter.json("mining_skill_cards");

    private static final Codec<Integer> COLOR = Codec.STRING.comapFlatMap(text -> {
        try {
            return DataResult.success(Integer.parseInt(text.startsWith("#") ? text.substring(1) : text, 16) & 0xFFFFFF);
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Not a color like \"#C0C0C0\": " + text);
        }
    }, color -> String.format("#%06X", color));

    private record Definition(Optional<String> name, List<String> tools, Optional<String> icon, int juiceColor, Optional<String> juiceName) {
        static final Codec<Definition> CODEC = RecordCodecBuilder.<Definition>create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("name").forGetter(Definition::name),
                Codec.STRING.listOf().fieldOf("tools").forGetter(Definition::tools),
                Codec.STRING.optionalFieldOf("icon").forGetter(Definition::icon),
                COLOR.optionalFieldOf("juice_color", 0xFFFFFF).forGetter(Definition::juiceColor),
                Codec.STRING.optionalFieldOf("juice_name").forGetter(Definition::juiceName)
        ).apply(instance, Definition::new)).flatXmap(Definition::validate, DataResult::success);

        private DataResult<Definition> validate() {
            if (this.tools.isEmpty()) return DataResult.error(() -> "\"tools\" is empty: a card needs at least one tool");
            for (String tool : this.tools) {
                if (ResourceLocation.tryParse(tool.startsWith("#") ? tool.substring(1) : tool) == null) {
                    return DataResult.error(() -> "Not an item id or #tag in \"tools\": " + tool);
                }
            }
            if (this.icon.isPresent() && ResourceLocation.tryParse(this.icon.get()) == null) {
                return DataResult.error(() -> "\"icon\" is not an item id: " + this.icon.get());
            }
            return DataResult.success(this);
        }
    }

    private DataCardTypes() {
    }

    /** Reads the card types of the loaded data packs and makes them the game's data pack types. */
    public static void load(ResourceManager resourceManager) {
        List<MiningSkillCardItem.Type> types = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Resource> file : new TreeMap<>(FILES.listMatchingResources(resourceManager)).entrySet()) {
            ResourceLocation id = FILES.fileToId(file.getKey());
            try (Reader reader = file.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                Definition.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error -> LOGGER.error("Skipping the Mining Skill Card type {}: {}", id, error))
                        .ifPresent(definition -> types.add(MiningSkillCardItem.Type.data(id.toString(), definition.name().orElse(""), definition.tools(),
                                definition.juiceColor(), definition.juiceName().orElse(""),
                                // Without an icon, the first tool that is an item (not a tag) stands on the card.
                                definition.icon().orElseGet(() -> definition.tools().stream().filter(tool -> !tool.startsWith("#")).findFirst().orElse("")))));
            } catch (Exception e) {
                LOGGER.error("Skipping the Mining Skill Card type {}: {}", id, e.getMessage());
            }
        }
        MiningSkillCardItem.Type.setDataTypes(types);
        if (!types.isEmpty()) LOGGER.info("Loaded {} Mining Skill Card type(s) from data packs", types.size());
    }
}
