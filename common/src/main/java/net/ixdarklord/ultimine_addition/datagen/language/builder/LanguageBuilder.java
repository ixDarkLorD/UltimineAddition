package net.ixdarklord.ultimine_addition.datagen.language.builder;

import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class LanguageBuilder {
    public static LanguageBuilder INSTANCE = new LanguageBuilder();
    private final Map<Object, String> translations = new HashMap<>();
    public LanguageBuilder() {
        add("itemGroup", "tab", "FTB Ultimine Addition");
        add(Registration.MINER_CERTIFICATE.get(), "Miner Certificate");
        add(Registration.SKILLS_RECORD.get(), "Skills Record");
        add(Registration.SHAPE_SELECTOR.get(), "Shape Selector");
        add(Registration.INK_CHAMBER.get(), "Ink Chamber");
        add(Registration.PEN.get(), "Pen");
        add(Registration.CARD_BLUEPRINT.get(), "Card Blueprint");
        add(Registration.MINING_SKILL_CARD_EMPTY.get(), "Mining Skill Card: Empty");
        add(Registration.MINING_SKILL_CARD_PICKAXE.get(), "Mining Skill Card: Pickaxe");
        add(Registration.MINING_SKILL_CARD_AXE.get(), "Mining Skill Card: Axe");
        add(Registration.MINING_SKILL_CARD_SHOVEL.get(), "Mining Skill Card: Shovel");
        add(Registration.MINING_SKILL_CARD_HOE.get(), "Mining Skill Card: Hoe");

        addPotion(Registration.KNOWLEDGE_POTION.getId().getPath(), "Knowledge", null);
        addPotion(Registration.MINE_GO_JUICE_PICKAXE_POTION.getId().getPath(), "Mine-Go%t: Rock Roulette", "Juice");
        addPotion(Registration.MINE_GO_JUICE_AXE_POTION.getId().getPath(), "Mine-Go%t: Lumberjack Limbo", "Juice");
        addPotion(Registration.MINE_GO_JUICE_SHOVEL_POTION.getId().getPath(), "Mine-Go%t: Worm Whispers", "Juice");
        addPotion(Registration.MINE_GO_JUICE_HOE_POTION.getId().getPath(), "Mine-Go%t: Soil Serenade", "Juice");

        add(Registration.MINE_GO_JUICE_PICKAXE.get(), "Mine-Go Juice: Rock Roulette");
        add(Registration.MINE_GO_JUICE_AXE.get(), "Mine-Go Juice: Lumberjack Limbo");
        add(Registration.MINE_GO_JUICE_SHOVEL.get(), "Mine-Go Juice: Worm Whispers");
        add(Registration.MINE_GO_JUICE_HOE.get(), "Mine-Go Juice: Soil Serenade");

        add("jei", "info.cards.obtain", "You can obtain this item from a level %s Toolsmith Villager.");
        add("jei", "info.cards.grade_up", "You can upgrade the tier of the Mining Skill Card by completing challenges in the Skills Record.");
        add("jei", "info.cards.mastered", "You can achieve the Mastered tier by completing challenges using the Skills Record.");
        add("jei", "tooltip.missing_card", "or Tiered up Card");
        add("jei", "category.item_storage.pen", "Refill Pen");
        add("jei", "recipe.item_storage.ink_chamber", "Ink Amount: %s+");

        addCurios("skills_record", "Skills Record");
        addTrinkets("hand.skills_record", "Skills Record");

        add("tooltip", "legacy_mode.disabled_item", "This item has been Disabled by Legacy Mode.");
        add("tooltip", "certificate.info", "Interact with this parchment to obtain the ultimine ability permanently!");
        add("tooltip", "certificate.legacy.info", "Obtain this knowledge to develop your mining skill!");
        add("tooltip", "certificate.legacy.sealed", "Sealed");
        add("tooltip", "certificate.legacy.opened", "Opened");
        add("tooltip", "certificate.legacy.quest.congrats", "You made it! You've destroyed %s ores.");
        add("tooltip", "certificate.legacy.quest.info", "To unseal the the scroll, Mine %s Ores!");
        add("tooltip", "certificate.legacy.quest", "Destroyed Ores: %s");
        add("tooltip", "skills_record.info", "A tool needed for upgrading mining skills card.");
        add("tooltip", "skills_record.contents", "Contents:");
        add("tooltip", "skills_record.press.left_click", "Press Left Click Mouse to cycle through blocks.");
        add("tooltip", "shape_selector.info", "Need a specific mining shape for your favorite tool? This is where you assign it!");
        add("tooltip", "shape_selector.selected", "Active Ultimine Shape:");
        add("tooltip", "skill_card.component", "You can use this item to craft any other skill card type.");
        add("tooltip", "skill_card.potion_point", "Potion Points: %s");
        add("tooltip", "skill_card.tier", "Tier: %s");
        add("tooltip", "skill_card.tier.unlearned", "Unlearned");
        add("tooltip", "skill_card.tier.novice", "Novice");
        add("tooltip", "skill_card.tier.apprentice", "Apprentice");
        add("tooltip", "skill_card.tier.adept", "Adept");
        add("tooltip", "skill_card.tier.mastered", "Mastered");
        add("tooltip", "skill_card.info.empty", "A mining card that can record your mining actions and skills. It can be Useful!");
        add("tooltip", "skill_card.info", "You have to put it in the Skills Record to discover the challenges given to you.");
        add("tooltip", "pen.info", "It's a required tool for the Skills Record.");
        add("tooltip", "pen.ink_chamber", "Ink Chamber: %s");

        add("gui", "color.default", "Default");
        add("gui", "color.red", "Red");
        add("gui", "color.orange", "Orange");
        add("gui", "color.yellow", "Yellow");
        add("gui", "color.green", "Green");
        add("gui", "color.blue", "Blue");
        add("gui", "color.indigo", "Indigo");
        add("gui", "color.violet", "Violet");
        add("gui", "action.set", "Set");
        add("gui", "action.clear", "Clear");
        add("gui", "shape_selector.insert", "Insert a tool!");
        add("gui", "shape_selector.selected", "You have chosen this shape!");
        add("gui", "shape_selector.blacklisted", "This shape is blacklisted!");
        add("gui", "skills_record.configuration", "Configuration");
        add("gui", "skills_record.option.soon", "Coming Soon...");
        add("gui", "skills_record.option.bg_color", "Background Color");
        add("gui", "skills_record.option.animations", "Animations");
        add("gui", "skills_record.option.progression_bar", "Progression Bar");
        add("gui", "skills_record.option.hold_keybind", "Hold %s");
        add("gui", "skills_record.option.panel_pos", "Challenges Panel Position");
        add("gui", "skills_record.option.panel_pos.disabled", "Disabled");
        add("gui", "skills_record.option.panel_pos.top_left", "Top Left");
        add("gui", "skills_record.option.panel_pos.top", "Top");
        add("gui", "skills_record.option.panel_pos.top_right", "Top Right");
        add("gui", "skills_record.option.panel_pos.left", "Left");
        add("gui", "skills_record.option.panel_pos.right", "Right");
        add("gui", "skills_record.option.panel_pos.bottom_left", "Bottom Left");
        add("gui", "skills_record.option.panel_pos.bottom_right", "Bottom Right");
        add("gui", "skills_record.option.panel_size", "Challenges Panel Size");
        add("gui", "skills_record.consume", "Consume Mode: %s");
        add("gui", "skills_record.consume.no_cards", "No cards need this mode to be toggled.");
        add("gui", "skills_record.example", "This is an example!");
        add("gui", "skills_record.no_cards", "There is no card inserted!");
        add("gui", "skills_record.select_card", "Select a card by right click it to discover the challenges.");
        add("gui", "skills_record.no_challenges", "There are no challenges added yet!");
        add("gui", "skills_record.completed_card", "Congratulations! You made it. You have completed all the challenges.");
        add("gui", "skills_record.progress", "Progress: %s");
        add("gui", "skills_record.missing_items", "You can't accomplish any challenge! It would help if you had these items available:");
        add("gui", "skills_record.not_enough_ink", "There is not enough ink in the pen! Please refill it.");
        add("gui", "skills_record.pin.click", "Click to Pin this Challenge");
        add("gui", "skills_record.pin.panel", "There are %s+ more hidden panels");
        add("gui", "skills_record.edit.click", "Click to Edit this Challenge");
        add("gui", "skills_record.edit.copy_id", "Click to Copy Challenge ID to Clipboard");
        add("gui", "skills_record.edit.copy_success", "Copied Challenge ID to Clipboard!");
        add("gui", "skills_record.edit.new_value", "Enter the new value.\nMax Value: %s");

        add("toast", "challenge.completed", "Challenge Completed!");
        add("toast", "challenge.completed.info", "You have completed Challenge %s in %s");
        add("toast", "challenge.all_completed", "Congratulations!");
        add("toast", "challenge.all_completed.info", "You have completed all the challenges for the %s.");

        add("advancement", "root.desc", "Have you been feeling weak lately? Take this journey for an improvement!");
        add("advancement", "obtain", "Obtain a %s");
        add("advancement", "craft", "Craft a %s");
        add("advancement", "amethyst_gathering", "Bring me the crystals!");
        add("advancement", "obtain.slime_balls", "Huh? oh... Yuck!");
        add("advancement", "obtain.card.empty", "Well... This is Useless!");
        add("advancement", "craft.pen", "Isn't this Exquisite?");
        add("advancement", "craft.skills_record", "It's time to take notes!");
        add("advancement", "craft.card_blueprint", "Ctrl-C + Ctrl-V = Copy-Paste");
        add("advancement", "craft.shape_selector", "When One Tool Just Isn't Enough!");
        add("advancement", "craft.card.pickaxe", "That's my Geode Gobbler!");
        add("advancement", "craft.card.axe", "Oh, I had an AXEIDENT! Did you get it?");
        add("advancement", "craft.card.shovel", "Gold Rush!");
        add("advancement", "craft.card.hoe", "You can't sit with us.");
        add("advancement", "ultimine_ability", "Powerful like a machine!");
        add("advancement", "ultimine_ability.desc", "Craft a %s using all four types of mastered mining skill cards and interact with it.");

        add("challenge", "title", "Challenge %s");
        add("challenge", "consume", "The block will be voided!");
        add("challenge", "consume.info", "In order to accomplish this challenge, You have to enable the consume mode.");
        add("challenge", "break_block", "Destroy %s.");
        add("challenge", "strip_block", "Strip the %s.");
        add("challenge", "flatten_block", "Flatten the %s.");
        add("challenge", "tilling_block", "Plow the %s.");
        add("challenge", "interact_with_block", "Interact with %s.");
        add("challenge", "various_blocks", "one of these blocks (%s)");

        add("info", "using_tool_shape", "Selected and using the %s shape");
        add("info", "placed_by_entity", "Ineligible block! This block was placed by %s.");
        add("info", "obtain", "Congratulations on learning the excavation skill!");
        add("info", "obtained_already", "You have already obtained this knowledge!");
        add("info", "incapable", "You need the excavation skill to perform this action! First, Obtain a Miner Certificate or drink a Mine-Go Juice.");
        add("info", "legacy_mode", "You can't use this! The playstyle mode has been set to Legacy.");
        add("info", "required_skill", "Required Skill for: %s");
        add("info", "required_skill.many_tools", "List of Tools");
        add("info", "required_skill.pickaxe", "Pickaxe");
        add("info", "required_skill.axe", "Axe");
        add("info", "required_skill.shovel", "Shovel");
        add("info", "required_skill.hoe", "Hoe");
        add("info", "required_skill.all", "All Tools");

        add("argument", "ultimine_shape.unknown", "Unknown ultimine shape '%s'");
        add("argument", "inventory.unknown", "Unknown inventory '%s'");
        add("argument", "challenge.unknown", "Unknown challenge '%s'");
        add("argument", "cards.tier.unknown", "Unknown card tier '%s'");

        add("command", "skills_record.edit_mode.success", "You have successfully set the Skills Record's edit mode to %s!");
        add("command", "skills_record.edit_mode.already_setted", "The Skills Record's edit mode is already set to %s.");
        add("command", "cards.not_found", "There is no card in the selected slot!");
        add("command", "challenge.not_found", "There is no challenge exists in the card with this id: %s");
        add("command", "challenge.success", "You have successfully changed the '%s' challenge current point from %s to %s!");
        add("command", "challenge.sender", "You have successfully changed the '%s' challenge current point from %s to %s for:");
        add("command", "challenge.receiver", "Your '%s' challenge current point has been changed from %s to %s! (by %s)");
        add("command", "challenge.accomplished", "The '%s' challenge is already accomplished.");
        add("command", "cards.tier.set.success", "You have successfully set the %s tier to %s!");
        add("command", "cards.tier.set.sender", "You have successfully set the %s tier to %s for:");
        add("command", "cards.tier.set.receiver", "Your %s tier has been set to %s! (by %s)");
        add("command", "cards.tier.set.already_setted", "The %s tier is already set to %s.");
        add("command", "set_ability.success", "You have successfully set the ultimine ability to %s!");
        add("command", "set_ability.sender", "You have successfully set the ultimine ability to %s for:");
        add("command", "set_ability.receiver", "Your ultimine ability has been set to %s! (by %s)");
        add("command", "set_ability.already_setted", "The ultimine ability is already set to %s.");
        add("command", "ultimine_shape.success.add", "You have successfully added the shape (%s) to the blacklist.");
        add("command", "ultimine_shape.success.remove", "You have successfully removed the shape (%s) from the blacklist.");
        add("command", "ultimine_shape.included", "The shape (%s) has already been added to the blacklist.");
        add("command", "ultimine_shape.excluded", "The shape (%s) is not listed in the blacklist.");

        add("key.category", "general", "FTB Ultimine Addition");
        add(KeyHandler.KEY_OPEN_SKILLS_RECORD, "Open Skills Record");
        add(KeyHandler.KEY_SHOW_PROGRESSION_BAR, "Skills Record: Show Progression Bar");
    }

    private void add(String key, String value) {
        this.translations.put(key, value);
    }

    private void add(Item item, String value) {
        this.translations.put(item, value);
    }

    private void add(MobEffect mobEffect, String value) {
        this.translations.put(mobEffect, value);
    }

    @SuppressWarnings("SameParameterValue")
    private void add(KeyMapping keyMapping, String value) {
        this.translations.put(keyMapping.getName(), value);
    }

    private void add(String category, String key, @NotNull String value) {
        add(String.format("%s.%s.%s", category, FTBUltimineAddition.MOD_ID, key), value);
    }

    private void addPotion(String key, String value, @Nullable String potionType) {
        String type = (potionType == null) ? "Potion" : potionType;
        String potion = (potionType == null) ? value + " Potion" : value.replace("%t", " " + type);
        String potionName = value.replace("%t", "");
        add(String.format("item.minecraft.potion.effect.%s", key), potion);
        add(String.format("item.minecraft.splash_potion.effect.%s", key), String.format("Splash %s of %s", type, potionName));
        add(String.format("item.minecraft.tipped_arrow.effect.%s", key), String.format("Arrow of %s", potion));
        add(String.format("item.minecraft.lingering_potion.effect.%s", key), String.format("Lingering %s of %s", type, potionName));
    }

    @SuppressWarnings("SameParameterValue")
    private void addCurios(String key, String value) {
        this.translations.put("curios.identifier.%s".formatted(key), value);
    }

    @SuppressWarnings("SameParameterValue")
    private void addTrinkets(String key, String value) {
        this.translations.put("trinkets.slot.%s".formatted(key), value);
    }

    public Map<Object, String> getTranslations() {
        return translations;
    }
}