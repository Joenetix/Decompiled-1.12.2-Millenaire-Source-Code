package org.millenaire.core;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.millenaire.MillenaireRevived;

/**
 * Complete item registry for Millénaire 8.1.2 content.
 * 
 * Registers ALL custom items from the original mod organized by culture:
 * - Currency and special items (deniers, wands, amulets)
 * - Norman: Tools, armor, food (cider, calva, boudin)
 * - Indian: Food (curry, rasgulla), brick mould
 * - Japanese: Weapons, armor (3 sets), food (sake, udon)
 * - Mayan: Tools, food (wah, balche), obsidian flake
 * - Byzantine: Tools, armor, food (olives, wine, feta), silk, icons
 * - Seeds and parchments
 * - Hides and hangings
 * - BlockItems for all registered blocks
 * 
 * Phase 1: Registration only - items are visible but simplified functionality
 * Phase 2+: Full trading, quests, and village economy will use these items
 */
public class MillItems {

        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
                        ForgeRegistries.ITEMS,
                        MillenaireRevived.MODID);

        // =============================================================================
        // CURRENCY - Core economic system
        // =============================================================================

        public static final RegistryObject<Item> DENIER = ITEMS.register("denier",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> DENIER_ARGENT = ITEMS.register("denierargent",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> DENIER_OR = ITEMS.register("denieror",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PURSE = ITEMS.register("purse",
                        () -> new Item(new Item.Properties().stacksTo(1)));

        // =============================================================================
        // SPECIAL ITEMS - Wands and Amulets
        // =============================================================================

        // Summoning wand - Spawns villages on use
        public static final RegistryObject<Item> SUMMONING_WAND = ITEMS.register("summoningwand",
                        () -> new org.millenaire.items.SummoningWandItem(new Item.Properties().stacksTo(1)));

        // Negation wand - For removing villages
        public static final RegistryObject<Item> NEGATION_WAND = ITEMS.register("negationwand",
                        () -> new Item(new Item.Properties().stacksTo(1)));

        // Amulets - Special items with effects (functionality in Phase 4)
        public static final RegistryObject<Item> AMULET_VISHNU = ITEMS.register("vishnu_amulet",
                        () -> new Item(new Item.Properties().stacksTo(1))); // Indian

        public static final RegistryObject<Item> AMULET_ALCHEMIST = ITEMS.register("alchemist_amulet",
                        () -> new Item(new Item.Properties().stacksTo(1))); // Norman

        public static final RegistryObject<Item> AMULET_YGGDRASIL = ITEMS.register("yggdrasil_amulet",
                        () -> new Item(new Item.Properties().stacksTo(1))); // Norman

        public static final RegistryObject<Item> AMULET_SKOLL_HATI = ITEMS.register("skoll_hati_amulet",
                        () -> new Item(new Item.Properties().stacksTo(1))); // Norman

        // =============================================================================
        // SEEDS - For crops
        // =============================================================================

        // TODO: Implement ItemMillSeeds that plants the crop blocks (Phase 2)
        public static final RegistryObject<Item> RICE = ITEMS.register("rice",
                        () -> new Item(new Item.Properties())); // Indian

        public static final RegistryObject<Item> TURMERIC = ITEMS.register("turmeric",
                        () -> new Item(new Item.Properties())); // Indian

        public static final RegistryObject<Item> MAIZE = ITEMS.register("maize",
                        () -> new Item(new Item.Properties())); // Mayan

        public static final RegistryObject<Item> GRAPES = ITEMS.register("grapes",
                        () -> new Item(new Item.Properties())); // Byzantine

        public static final RegistryObject<Item> COTTON = ITEMS.register("cotton",
                        () -> new Item(new Item.Properties())); // Multiple cultures

        // =============================================================================
        // NORMAN CULTURE - Medieval French
        // =============================================================================

        // Tools - TODO: Implement ItemMillenaireTool classes with proper tier/stats
        // (Phase 2)
        public static final RegistryObject<Item> NORMAN_PICKAXE = ITEMS.register("normanpickaxe",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> NORMAN_AXE = ITEMS.register("normanaxe",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> NORMAN_SHOVEL = ITEMS.register("normanshovel",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> NORMAN_HOE = ITEMS.register("normanhoe",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> NORMAN_SWORD = ITEMS.register("normanbroadsword",
                        () -> new Item(new Item.Properties()));

        // Armor - TODO: Implement ItemMillenaireArmor with proper ArmorMaterial (Phase
        // 2)
        public static final RegistryObject<Item> NORMAN_HELMET = ITEMS.register("normanhelmet",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> NORMAN_CHESTPLATE = ITEMS.register("normanplate",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> NORMAN_LEGGINGS = ITEMS.register("normanlegs",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> NORMAN_BOOTS = ITEMS.register("normanboots",
                        () -> new Item(new Item.Properties()));

        // Food - TODO: Implement ItemFoodMultiple with alcohol effects (Phase 2)
        public static final RegistryObject<Item> CIDER_APPLE = ITEMS.register("ciderapple",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).build())));

        public static final RegistryObject<Item> CIDER = ITEMS.register("cider",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1F).build())));

        public static final RegistryObject<Item> CALVA = ITEMS.register("calva",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1F).build())));

        public static final RegistryObject<Item> BOUDIN = ITEMS.register("boudin",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(5).saturationMod(0.6F).build())));

        public static final RegistryObject<Item> TRIPES = ITEMS.register("tripes",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.5F).build())));

        // Decorations
        public static final RegistryObject<Item> TAPESTRY = ITEMS.register("tapestry",
                        () -> new Item(new Item.Properties()));

        // =============================================================================
        // INDIAN CULTURE - North Indian/Hindi
        // =============================================================================

        // Food
        public static final RegistryObject<Item> VEG_CURRY = ITEMS.register("vegcurry",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.7F).build())));

        public static final RegistryObject<Item> CHICKEN_CURRY = ITEMS.register("chickencurry",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(8).saturationMod(0.8F).build())));

        public static final RegistryObject<Item> RASGULLA = ITEMS.register("rasgulla",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.4F).build())));

        // Items
        public static final RegistryObject<Item> BRICK_MOULD = ITEMS.register("brickmould",
                        () -> new Item(new Item.Properties()));

        // Indian statue decoration
        public static final RegistryObject<Item> INDIAN_STATUE = ITEMS.register("indianstatue",
                        () -> new Item(new Item.Properties()));

        // =============================================================================
        // JAPANESE CULTURE
        // =============================================================================

        // Weapons - TODO: Implement custom weapon classes (Phase 2)
        public static final RegistryObject<Item> TACHI_SWORD = ITEMS.register("tachisword",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> YUMI_BOW = ITEMS.register("yumibow",
                        () -> new Item(new Item.Properties()));

        // Armor - Red set
        public static final RegistryObject<Item> JAPANESE_RED_HELMET = ITEMS.register("japaneseredhelmet",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> JAPANESE_RED_CHESTPLATE = ITEMS.register("japaneseredplate",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> JAPANESE_RED_LEGGINGS = ITEMS.register("japaneseredlegs",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> JAPANESE_RED_BOOTS = ITEMS.register("japaneseredboots",
                        () -> new Item(new Item.Properties()));

        // Armor - Blue set
        public static final RegistryObject<Item> JAPANESE_BLUE_HELMET = ITEMS.register("japanesebluehelmet",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> JAPANESE_BLUE_CHESTPLATE = ITEMS.register("japaneseblueplate",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> JAPANESE_BLUE_LEGGINGS = ITEMS.register("japanesebluelegs",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> JAPANESE_BLUE_BOOTS = ITEMS.register("japaneseblueboots",
                        () -> new Item(new Item.Properties()));

        // Armor - Guard set
        public static final RegistryObject<Item> JAPANESE_GUARD_HELMET = ITEMS.register("japaneseguardhelmet",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> JAPANESE_GUARD_CHESTPLATE = ITEMS.register("japaneseguardplate",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> JAPANESE_GUARD_LEGGINGS = ITEMS.register("japaneseguardlegs",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> JAPANESE_GUARD_BOOTS = ITEMS.register("japaneseguardboots",
                        () -> new Item(new Item.Properties()));

        // Food
        public static final RegistryObject<Item> UDON = ITEMS.register("udon",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(5).saturationMod(0.6F).build())));

        public static final RegistryObject<Item> SAKE = ITEMS.register("sake",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1F).build())));

        public static final RegistryObject<Item> IKAYAKI = ITEMS.register("ikayaki",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.5F).build())));

        // =============================================================================
        // MAYAN CULTURE
        // =============================================================================

        // Tools (Obsidian-based)
        public static final RegistryObject<Item> MAYAN_PICKAXE = ITEMS.register("mayanpickaxe",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> MAYAN_AXE = ITEMS.register("mayanaxe",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> MAYAN_SHOVEL = ITEMS.register("mayanshovel",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> MAYAN_HOE = ITEMS.register("mayanhoe",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> MAYAN_MACE = ITEMS.register("mayanmace",
                        () -> new Item(new Item.Properties()));

        // Food
        public static final RegistryObject<Item> WAH = ITEMS.register("wah",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.5F).build())));

        public static final RegistryObject<Item> BALCHE = ITEMS.register("balche",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1F).build())));

        public static final RegistryObject<Item> SIKILPAH = ITEMS.register("sikilpah",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.4F).build())));

        public static final RegistryObject<Item> MASA = ITEMS.register("masa",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.4F).build())));

        public static final RegistryObject<Item> CACAUHAA = ITEMS.register("cacauhaa",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).build())));

        // Items
        public static final RegistryObject<Item> OBSIDIAN_FLAKE = ITEMS.register("obsidianflake",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> MAYAN_QUEST_CROWN = ITEMS.register("mayanquestcrown",
                        () -> new Item(new Item.Properties().stacksTo(1)));

        // Decorations
        public static final RegistryObject<Item> MAYAN_STATUE = ITEMS.register("mayanstatue",
                        () -> new Item(new Item.Properties()));

        // =============================================================================
        // BYZANTINE CULTURE
        // =============================================================================

        // Tools
        public static final RegistryObject<Item> BYZANTINE_PICKAXE = ITEMS.register("byzantinepickaxe",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_AXE = ITEMS.register("byzantineaxe",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_SHOVEL = ITEMS.register("byzantineshovel",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_HOE = ITEMS.register("byzantinehoe",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_MACE = ITEMS.register("byzantinemace",
                        () -> new Item(new Item.Properties()));

        // Armor
        public static final RegistryObject<Item> BYZANTINE_HELMET = ITEMS.register("byzantinehelmet",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_CHESTPLATE = ITEMS.register("byzantineplate",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_LEGGINGS = ITEMS.register("byzantinelegs",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_BOOTS = ITEMS.register("byzantineboots",
                        () -> new Item(new Item.Properties()));

        // Food
        public static final RegistryObject<Item> OLIVES = ITEMS.register("olives",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).build())));

        public static final RegistryObject<Item> OLIVEOIL = ITEMS.register("oliveoil",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> WINE_BASIC = ITEMS.register("winebasic",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1F).build())));

        public static final RegistryObject<Item> WINE_FANCY = ITEMS.register("winefancy",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1F).build())));

        public static final RegistryObject<Item> FETA = ITEMS.register("feta",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.4F).build())));

        public static final RegistryObject<Item> SOUVLAKI = ITEMS.register("souvlaki",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(7).saturationMod(0.7F).build())));

        // Items
        public static final RegistryObject<Item> SILK = ITEMS.register("silk",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_CLOTH_WOOL = ITEMS.register("clothes_byz_wool",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_CLOTH_SILK = ITEMS.register("clothes_byz_silk",
                        () -> new Item(new Item.Properties()));

        // Decorations - Icons
        public static final RegistryObject<Item> BYZANTINE_ICON_SMALL = ITEMS.register("byzantineiconsmall",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_ICON_MEDIUM = ITEMS.register("byzantineiconmedium",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_ICON_LARGE = ITEMS.register("byzantineiconlarge",
                        () -> new Item(new Item.Properties()));

        // =============================================================================
        // INUIT CULTURE - Arctic peoples
        // =============================================================================

        // Weapons
        public static final RegistryObject<Item> INUIT_TRIDENT = ITEMS.register("inuittrident",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> INUIT_BOW = ITEMS.register("inuitbow",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> ULU = ITEMS.register("ulu",
                        () -> new Item(new Item.Properties()));

        // Armor - Fur set
        public static final RegistryObject<Item> FUR_HELMET = ITEMS.register("furhelmet",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> FUR_CHESTPLATE = ITEMS.register("furplate",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> FUR_LEGGINGS = ITEMS.register("furlegs",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> FUR_BOOTS = ITEMS.register("furboots",
                        () -> new Item(new Item.Properties()));

        // Food - Cooked and raw meat
        public static final RegistryObject<Item> BEARMEAT_RAW = ITEMS.register("bearmeat_raw",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.3F).meat()
                                                        .build())));

        public static final RegistryObject<Item> BEARMEAT_COOKED = ITEMS.register("bearmeat_cooked",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(8).saturationMod(0.8F).meat()
                                                        .build())));

        public static final RegistryObject<Item> WOLFMEAT_RAW = ITEMS.register("wolfmeat_raw",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2F).meat()
                                                        .build())));

        public static final RegistryObject<Item> WOLFMEAT_COOKED = ITEMS.register("wolfmeat_cooked",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.6F).meat()
                                                        .build())));

        public static final RegistryObject<Item> SEAFOOD_RAW = ITEMS.register("seafood_raw",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2F).build())));

        public static final RegistryObject<Item> SEAFOOD_COOKED = ITEMS.register("seafood_cooked",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(5).saturationMod(0.5F).build())));

        // Stews
        public static final RegistryObject<Item> INUIT_BEAR_STEW = ITEMS.register("inuitbearstew",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(10).saturationMod(1.0F).build())));

        public static final RegistryObject<Item> INUIT_MEATY_STEW = ITEMS.register("inuitmeatystew",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(8).saturationMod(0.8F).build())));

        public static final RegistryObject<Item> INUIT_POTATO_STEW = ITEMS.register("inuitpotatostew",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.6F).build())));

        // Materials
        public static final RegistryObject<Item> TANNED_HIDE = ITEMS.register("tannedhide",
                        () -> new Item(new Item.Properties()));

        // Hide Hanging (Decoration)

        // =============================================================================
        // SELJUK CULTURE ITEMS - Byzantine Sub-culture
        // =============================================================================

        // Weapons
        public static final RegistryObject<Item> SELJUK_SCIMITAR = ITEMS.register("seljukscimitar",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> SELJUK_BOW = ITEMS.register("seljukbow",
                        () -> new Item(new Item.Properties()));

        // Armor
        public static final RegistryObject<Item> SELJUK_TURBAN = ITEMS.register("seljukturban",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> SELJUK_HELMET = ITEMS.register("seljukhelmet",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> SELJUK_CHESTPLATE = ITEMS.register("seljukplate",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> SELJUK_LEGGINGS = ITEMS.register("seljuklegs",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> SELJUK_BOOTS = ITEMS.register("seljukboots",
                        () -> new Item(new Item.Properties()));

        // Food
        public static final RegistryObject<Item> PIDE = ITEMS.register("pide",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(8).saturationMod(0.8F).build())));

        public static final RegistryObject<Item> HELVA = ITEMS.register("helva",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.4F).build())));

        public static final RegistryObject<Item> LOKUM = ITEMS.register("lokum",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2F).build())));

        public static final RegistryObject<Item> AYRAN = ITEMS.register("ayran",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2F).build())));

        public static final RegistryObject<Item> YOGURT = ITEMS.register("yogurt",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.3F).build())));

        public static final RegistryObject<Item> PISTACHIOS = ITEMS.register("pistachios",
                        () -> new Item(new Item.Properties()
                                        .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1F).build())));

        // Clothes
        public static final RegistryObject<Item> SELJUK_CLOTH_WOOL = ITEMS.register("clothes_seljuk_wool",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> SELJUK_CLOTH_COTTON = ITEMS.register("clothes_seljuk_cotton",
                        () -> new Item(new Item.Properties()));

        // Decoration
        public static final RegistryObject<Item> WALLCARPETSMALL = ITEMS.register("wallcarpetsmall",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> WALLCARPETMEDIUM = ITEMS.register("wallcarpetmedium",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> WALLCARPETLARGE = ITEMS.register("wallcarpetlarge",
                        () -> new Item(new Item.Properties()));

        // =============================================================================
        // PARCHMENTS - Cultural documentation for language learning
        // =============================================================================

        // Norman
        public static final RegistryObject<Item> PARCHMENT_NORMAN_VILLAGERS = ITEMS.register(
                        "parchment_normanvillagers",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_NORMAN_BUILDINGS = ITEMS.register(
                        "parchment_normanbuildings",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_NORMAN_ITEMS = ITEMS.register("parchment_normanitems",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_NORMAN_COMPLETE = ITEMS.register("parchment_normanfull",
                        () -> new Item(new Item.Properties()));

        // Indian
        public static final RegistryObject<Item> PARCHMENT_INDIAN_VILLAGERS = ITEMS.register(
                        "parchment_indianvillagers",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_INDIAN_BUILDINGS = ITEMS.register(
                        "parchment_indianbuildings",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_INDIAN_ITEMS = ITEMS.register("parchment_indianitems",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_INDIAN_COMPLETE = ITEMS.register("parchment_indianfull",
                        () -> new Item(new Item.Properties()));

        // Japanese
        public static final RegistryObject<Item> PARCHMENT_JAPANESE_VILLAGERS = ITEMS.register(
                        "parchment_japanesevillagers",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_JAPANESE_BUILDINGS = ITEMS.register(
                        "parchment_japanesebuildings",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_JAPANESE_ITEMS = ITEMS.register("parchment_japaneseitems",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_JAPANESE_COMPLETE = ITEMS.register("parchment_japanesefull",
                        () -> new Item(new Item.Properties()));

        // Mayan
        public static final RegistryObject<Item> PARCHMENT_MAYAN_VILLAGERS = ITEMS.register("parchment_mayanvillagers",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_MAYAN_BUILDINGS = ITEMS.register("parchment_mayanbuildings",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_MAYAN_ITEMS = ITEMS.register("parchment_mayanitems",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_MAYAN_COMPLETE = ITEMS.register("parchment_mayanfull",
                        () -> new Item(new Item.Properties()));

        // Special parchments
        public static final RegistryObject<Item> PARCHMENT_VILLAGE_SCROLL = ITEMS.register("parchment_villagescroll",
                        () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> PARCHMENT_SADHU = ITEMS.register("parchment_sadhu",
                        () -> new Item(new Item.Properties()));

        // =============================================================================
        // PAINT BUCKETS - 16 colors for painted bricks
        // =============================================================================

        // TODO: Loop through DyeColor.values() and register paint_bucket_<color>

        // =============================================================================
        // BLOCK ITEMS - For all registered blocks
        // =============================================================================

        // Core decorative blocks
        public static final RegistryObject<Item> WOOD_DECORATION_ITEM = ITEMS.register("wood_deco",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.WOOD_DECORATION.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> STONE_DECORATION_ITEM = ITEMS.register("stone_deco",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.STONE_DECORATION.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> EARTH_DECORATION_ITEM = ITEMS.register("earth_deco",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.EARTH_DECORATION.get(),
                                        new Item.Properties()));

        // Norman blocks
        public static final RegistryObject<Item> EXTENDED_MUD_BRICK_ITEM = ITEMS.register("extended_mud_brick",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.EXTENDED_MUD_BRICK.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> WALL_MUD_BRICK_ITEM = ITEMS.register("wall_mud_brick",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.WALL_MUD_BRICK.get(),
                                        new Item.Properties()));

        // Norman stairs and slabs
        public static final RegistryObject<Item> STAIRS_TIMBERFRAME_ITEM = ITEMS.register("stairs_timberframe",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.STAIRS_TIMBERFRAME.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> STAIRS_MUDBRICK_ITEM = ITEMS.register("stairs_mudbrick",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.STAIRS_MUDBRICK.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> STAIRS_COOKEDBRICK_ITEM = ITEMS.register("stairs_cookedbrick",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.STAIRS_COOKEDBRICK.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> STAIRS_THATCH_ITEM = ITEMS.register("stairs_thatch",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.STAIRS_THATCH.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> SLAB_WOOD_DECO_ITEM = ITEMS.register("slab_wood_deco",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.SLAB_WOOD_DECO.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> SLAB_STONE_DECO_ITEM = ITEMS.register("slab_stone_deco",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.SLAB_STONE_DECO.get(),
                                        new Item.Properties()));

        // Norman bed
        public static final RegistryObject<Item> BED_STRAW_ITEM = ITEMS.register("bed_straw",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.BED_STRAW.get(),
                                        new Item.Properties().stacksTo(1)));

        // Indian blocks
        public static final RegistryObject<Item> SANDSTONE_CARVED_ITEM = ITEMS.register("sandstone_carved",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.SANDSTONE_CARVED.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> SANDSTONE_RED_CARVED_ITEM = ITEMS.register("sandstone_red_carved",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.SANDSTONE_RED_CARVED.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> SANDSTONE_OCHRE_CARVED_ITEM = ITEMS.register("sandstone_ochre_carved",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.SANDSTONE_OCHRE_CARVED.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> WET_BRICK_ITEM = ITEMS.register("wet_brick",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.WET_BRICK.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> SILK_WORM_ITEM = ITEMS.register("silk_worm",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.SILK_WORM.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> SNAIL_SOIL_ITEM = ITEMS.register("snail_soil",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.SNAIL_SOIL.get(),
                                        new Item.Properties()));

        // Indian bed
        public static final RegistryObject<Item> BED_CHARPOY_ITEM = ITEMS.register("bed_charpoy",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.BED_CHARPOY.get(),
                                        new Item.Properties().stacksTo(1)));

        // Japanese blocks
        public static final RegistryObject<Item> GRAY_TILES_ITEM = ITEMS.register("gray_tiles",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.GRAY_TILES.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> GREEN_TILES_ITEM = ITEMS.register("green_tiles",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.GREEN_TILES.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> RED_TILES_ITEM = ITEMS.register("red_tiles",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.RED_TILES.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> WOODEN_BARS_ITEM = ITEMS.register("wooden_bars",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.WOODEN_BARS.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> WOODEN_BARS_DARK_ITEM = ITEMS.register("wooden_bars_dark",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.WOODEN_BARS_DARK.get(),
                                        new Item.Properties()));

        // Japanese tile slabs
        public static final RegistryObject<Item> GRAY_TILES_SLAB_ITEM = ITEMS.register("gray_tiles_slab",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.GRAY_TILES_SLAB.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> GREEN_TILES_SLAB_ITEM = ITEMS.register("green_tiles_slab",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.GREEN_TILES_SLAB.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> RED_TILES_SLAB_ITEM = ITEMS.register("red_tiles_slab",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.RED_TILES_SLAB.get(),
                                        new Item.Properties()));

        // Japanese tile stairs
        public static final RegistryObject<Item> STAIRS_GRAY_TILES_ITEM = ITEMS.register("stairs_gray_tiles",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.STAIRS_GRAY_TILES.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> STAIRS_GREEN_TILES_ITEM = ITEMS.register("stairs_green_tiles",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.STAIRS_GREEN_TILES.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> STAIRS_RED_TILES_ITEM = ITEMS.register("stairs_red_tiles",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.STAIRS_RED_TILES.get(),
                                        new Item.Properties()));

        // Japanese paper wall
        public static final RegistryObject<Item> PAPER_WALL_ITEM = ITEMS.register("paper_wall",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PAPER_WALL.get(),
                                        new Item.Properties()));

        // Byzantine blocks
        public static final RegistryObject<Item> BYZANTINE_TILES_ITEM = ITEMS.register("byzantine_tiles",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.BYZANTINE_TILES.get(),
                                        new Item.Properties()));

        // Byzantine decorative
        public static final RegistryObject<Item> BYZANTINE_TILES_SLAB_ITEM = ITEMS.register("byzantine_tiles_slab",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.BYZANTINE_TILES_SLAB.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> STAIRS_BYZ_TILES_ITEM = ITEMS.register("stairs_byz_tiles",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.STAIRS_BYZ_TILES.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_STONE_TILES_ITEM = ITEMS.register("byzantine_stone_tiles",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.BYZANTINE_STONE_TILES.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_SANDSTONE_TILES_ITEM = ITEMS.register(
                        "byzantine_sandstone_tiles",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.BYZANTINE_SANDSTONE_TILES.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_STONE_ORNAMENT_ITEM = ITEMS.register(
                        "byzantine_stone_ornament",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.BYZANTINE_STONE_ORNAMENT.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> BYZANTINE_SANDSTONE_ORNAMENT_ITEM = ITEMS.register(
                        "byzantine_sandstone_ornament",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.BYZANTINE_SANDSTONE_ORNAMENT.get(),
                                        new Item.Properties()));

        // Byzantine trees
        public static final RegistryObject<Item> SAPLING_OLIVETREE_ITEM = ITEMS.register("sapling_olivetree",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.SAPLING_OLIVETREE.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> LEAVES_OLIVETREE_ITEM = ITEMS.register("leaves_olivetree",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.LEAVES_OLIVETREE.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> SAPLING_PISTACHIO_ITEM = ITEMS.register("sapling_pistachio",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.SAPLING_PISTACHIO.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> LEAVES_PISTACHIO_ITEM = ITEMS.register("leaves_pistachio",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.LEAVES_PISTACHIO.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> WOODEN_BARS_INDIAN_ITEM = ITEMS.register("wooden_bars_indian",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.WOODEN_BARS_INDIAN.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> WOODEN_BARS_ROSETTE_ITEM = ITEMS.register("wooden_bars_rosette",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.WOODEN_BARS_ROSETTE.get(),
                                        new Item.Properties()));

        // Shared decorative blocks
        public static final RegistryObject<Item> STAINED_GLASS_ITEM = ITEMS.register("stained_glass",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.STAINED_GLASS.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> ROSETTE_ITEM = ITEMS.register("rosette",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.ROSETTE.get(),
                                        new Item.Properties()));

        // Special blocks
        public static final RegistryObject<Item> ALCHEMIST_EXPLOSIVE_ITEM = ITEMS.register("alchemistexplosive",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.ALCHEMIST_EXPLOSIVE.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> SOD_ITEM = ITEMS.register("sod",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.SOD.get(),
                                        new Item.Properties()));

        // =============================================================================
        // INUIT BLOCK ITEMS - Arctic structures
        // =============================================================================

        public static final RegistryObject<Item> ICEBRICK_ITEM = ITEMS.register("icebrick",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.ICEBRICK.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> SNOWBRICK_ITEM = ITEMS.register("snowbrick",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.SNOWBRICK.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> SNOWWALL_ITEM = ITEMS.register("snowwall",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.SNOWWALL.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> INUITCARVING_ITEM = ITEMS.register("inuitcarving",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.INUITCARVING.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> FIRE_PIT_ITEM = ITEMS.register("fire_pit",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.FIRE_PIT.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> HIDEHANGING = ITEMS.register("hidehanging",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.HIDEHANGING.get(),
                                        new Item.Properties()));

        // =============================================================================
        // CROP BLOCK ITEMS - Seeds/Produce integration
        // =============================================================================

        public static final RegistryObject<Item> CROP_RICE_ITEM = ITEMS.register("crop_rice",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.CROP_RICE.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> CROP_TURMERIC_ITEM = ITEMS.register("crop_turmeric",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.CROP_TURMERIC.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> CROP_MAIZE_ITEM = ITEMS.register("crop_maize",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.CROP_MAIZE.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> CROP_COTTON_ITEM = ITEMS.register("crop_cotton",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.CROP_COTTON.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> CROP_VINE_ITEM = ITEMS.register("crop_vine",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.CROP_VINE.get(),
                                        new Item.Properties()));

        // Panel Item
        public static final RegistryObject<Item> PANEL_ITEM = ITEMS.register("panel",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PANEL.get(),
                                        new Item.Properties()));

        // =============================================================================
        // PATH BLOCK ITEMS - Enables paths as placeable blocks
        // =============================================================================

        // Path blocks
        public static final RegistryObject<Item> PATHDIRT_ITEM = ITEMS.register("pathdirt",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHDIRT.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> PATHGRAVEL_ITEM = ITEMS.register("pathgravel",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHGRAVEL.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> PATHSLABS_ITEM = ITEMS.register("pathslabs",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHSLABS.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> PATHSANDSTONE_ITEM = ITEMS.register("pathsandstone",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHSANDSTONE.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> PATHOCHRETILES_ITEM = ITEMS.register("pathochretiles",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHOCHRETILES.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> PATHGRAVELSLABS_ITEM = ITEMS.register("pathgravelslabs",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHGRAVELSLABS.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> PATHSNOW_ITEM = ITEMS.register("pathsnow",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHSNOW.get(),
                                        new Item.Properties()));

        // Path slabs
        public static final RegistryObject<Item> PATHDIRT_SLAB_ITEM = ITEMS.register("pathdirt_slab",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHDIRT_SLAB.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> PATHGRAVEL_SLAB_ITEM = ITEMS.register("pathgravel_slab",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHGRAVEL_SLAB.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> PATHSLABS_SLAB_ITEM = ITEMS.register("pathslabs_slab",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHSLABS_SLAB.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> PATHSANDSTONE_SLAB_ITEM = ITEMS.register("pathsandstone_slab",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHSANDSTONE_SLAB.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> PATHOCHRETILES_SLAB_ITEM = ITEMS.register("pathochretiles_slab",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHOCHRETILES_SLAB.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> PATHGRAVELSLABS_SLAB_ITEM = ITEMS.register("pathgravelslabs_slab",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHGRAVELSLABS_SLAB.get(),
                                        new Item.Properties()));

        public static final RegistryObject<Item> PATHSNOW_SLAB_ITEM = ITEMS.register("pathsnow_slab",
                        () -> new net.minecraft.world.item.BlockItem(MillBlocks.PATHSNOW_SLAB.get(),
                                        new Item.Properties()));
}
