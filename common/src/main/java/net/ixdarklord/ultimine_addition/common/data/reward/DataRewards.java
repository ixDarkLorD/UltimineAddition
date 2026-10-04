package net.ixdarklord.ultimine_addition.common.data.reward;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.LOGGER;

// Rewards data packs hand out as cards progress (data/<namespace>/ultimine_rewards/<name>.json): when a challenge is
// completed, when a card reaches a tier, or when the daily challenge is done. A reward is a loot table, experience, commands, or any mix of them.
//
//   { "when": "challenge", "challenge": "mypack:hammer/smashing_stone", "loot_table": "mypack:rewards/gems" }
//   { "when": "tier", "card_type": "pickaxe", "tier": 2, "experience": 100, "commands": ["say @s is an Apprentice!"] }
//   { "when": "timed", "loot_table": "mypack:rewards/daily" }   (the daily or weekly challenge, see TimedChallenge)
//
// "challenge", "card_type" and "tier" narrow a reward down; one left out matches all. Commands run as the server, at the
// player (@s is the player). They are read with the challenges (ChallengesManager), and only the server needs them.
public final class DataRewards {
    private static final FileToIdConverter FILES = FileToIdConverter.json("ultimine_rewards");
    private static List<Reward> rewards = List.of();

    private record Reward(ResourceLocation id, String when, Optional<ResourceLocation> challenge, Optional<String> cardType, Optional<Integer> tier,
                          Optional<ResourceLocation> lootTable, int experience, List<String> commands) {
        boolean matches(MiningSkillCardItem.Type type, int tier, ResourceLocation challenge) {
            return (this.challenge.isEmpty() || this.challenge.get().equals(challenge))
                    && (this.cardType.isEmpty() || this.cardType.get().equals(type.getId()))
                    && (this.tier.isEmpty() || this.tier.get() == tier);
        }
    }

    private record Definition(String when, Optional<ResourceLocation> challenge, Optional<String> cardType, Optional<Integer> tier,
                              Optional<ResourceLocation> lootTable, int experience, List<String> commands) {
        static final Codec<Definition> CODEC = RecordCodecBuilder.<Definition>create(instance -> instance.group(
                Codec.STRING.fieldOf("when").forGetter(Definition::when),
                ResourceLocation.CODEC.optionalFieldOf("challenge").forGetter(Definition::challenge),
                Codec.STRING.optionalFieldOf("card_type").forGetter(Definition::cardType),
                Codec.INT.optionalFieldOf("tier").forGetter(Definition::tier),
                ResourceLocation.CODEC.optionalFieldOf("loot_table").forGetter(Definition::lootTable),
                Codec.INT.optionalFieldOf("experience", 0).forGetter(Definition::experience),
                Codec.STRING.listOf().optionalFieldOf("commands", List.of()).forGetter(Definition::commands)
        ).apply(instance, Definition::new)).flatXmap(Definition::validate, DataResult::success);

        private DataResult<Definition> validate() {
            if (!this.when.equals("challenge") && !this.when.equals("tier") && !this.when.equals("timed")) {
                return DataResult.error(() -> "\"when\" must be \"challenge\", \"tier\" or \"timed\", not \"" + this.when + "\"");
            }
            if (this.when.equals("tier") && this.challenge.isPresent()) {
                return DataResult.error(() -> "A \"tier\" reward can't name a \"challenge\"");
            }
            if (this.lootTable.isEmpty() && this.experience <= 0 && this.commands.isEmpty()) {
                return DataResult.error(() -> "A reward needs a \"loot_table\", \"experience\" or \"commands\"");
            }
            return DataResult.success(this);
        }
    }

    private DataRewards() {
    }

    /** Reads the rewards of the loaded data packs. */
    public static void load(ResourceManager resourceManager) {
        List<Reward> loaded = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Resource> file : new TreeMap<>(FILES.listMatchingResources(resourceManager)).entrySet()) {
            ResourceLocation id = FILES.fileToId(file.getKey());
            try (Reader reader = file.getValue().openAsReader()) {
                Definition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .resultOrPartial(error -> LOGGER.error("Skipping the Ultimine reward {}: {}", id, error))
                        .ifPresent(d -> loaded.add(new Reward(id, d.when(), d.challenge(), d.cardType(), d.tier(), d.lootTable(), d.experience(), d.commands())));
            } catch (Exception e) {
                LOGGER.error("Skipping the Ultimine reward {}: {}", id, e.getMessage());
            }
        }
        rewards = List.copyOf(loaded);
        if (!loaded.isEmpty()) LOGGER.info("Loaded {} Ultimine reward(s) from data packs", loaded.size());
    }

    /** A challenge of this card was completed, while the card was at this tier. */
    public static void onChallengeCompleted(ServerPlayer player, MiningSkillCardData card, MiningSkillCardItem.Tier tier, ResourceLocation challenge) {
        for (Reward reward : rewards) {
            if (reward.when().equals("challenge") && reward.matches(card.getType(), tier.getValue(), challenge)) give(player, reward);
        }
    }

    /** This card reached this tier. */
    public static void onTierReached(ServerPlayer player, MiningSkillCardData card, MiningSkillCardItem.Tier tier) {
        for (Reward reward : rewards) {
            if (reward.when().equals("tier") && reward.matches(card.getType(), tier.getValue(), null)) give(player, reward);
        }
    }

    /** The player finished the daily or weekly challenge, which was this challenge of this card type. */
    public static void onTimedChallenge(ServerPlayer player, MiningSkillCardItem.Type type, ResourceLocation challenge) {
        for (Reward reward : rewards) {
            if (reward.when().equals("timed") && reward.tier().isEmpty() && reward.matches(type, 0, challenge)) give(player, reward);
        }
    }

    private static void give(ServerPlayer player, Reward reward) {
        MinecraftServer server = player.serverLevel().getServer();
        try {
            if (reward.lootTable().isPresent()) {
                LootTable table = server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, reward.lootTable().get()));
                LootParams params = new LootParams.Builder(player.serverLevel())
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .withParameter(LootContextParams.ORIGIN, player.position())
                        .create(LootContextParamSets.GIFT);
                List<ItemStack> items = new ArrayList<>();
                table.getRandomItems(params, items::add);
                // Into the inventory, and at the player's feet when it is full.
                for (ItemStack stack : items) {
                    if (!player.getInventory().add(stack)) player.drop(stack, false);
                }
            }
            if (reward.experience() > 0) player.giveExperiencePoints(reward.experience());
            for (String command : reward.commands()) {
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withEntity(player)
                        .withPosition(player.position()).withRotation(player.getRotationVector()).withSuppressedOutput(), command);
            }
        } catch (RuntimeException e) {
            LOGGER.error("The Ultimine reward {} failed for {}: {}", reward.id(), player.getScoreboardName(), e.toString());
        }
    }
}
