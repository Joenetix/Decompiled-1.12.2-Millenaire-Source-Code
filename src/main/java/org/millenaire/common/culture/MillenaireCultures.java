package org.millenaire.common.culture;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all Millénaire cultures.
 * Defines the 5 main cultures: Norman, Indian, Japanese, Mayan, and Byzantine.
 */
public class MillenaireCultures {

    private static final Map<String, Culture> CULTURES = new HashMap<>();

    // Culture instances
    public static final Culture NORMAN;
    public static final Culture INDIAN;
    public static final Culture JAPANESE;
    public static final Culture MAYAN;
    public static final Culture BYZANTINE;

    static {
        // == NORMAN CULTURE ==
        NORMAN = new Culture("norman", "Norman")
                // Core buildings
                .addBuilding("houses", "farm")
                .addBuilding("houses", "forge")
                .addBuilding("houses", "weaverhouse")
                .addBuilding("houses", "quarry")
                .addBuilding("houses", "lumbermanhut")
                .addBuilding("houses", "glassblower")
                .addBuilding("houses", "armoury")
                .addBuilding("houses", "carpenterhouse")
                .addBuilding("houses", "guardhouse")
                .addBuilding("houses", "presbytery")
                .addBuilding("houses", "alchemyworkshop")
                .addBuilding("houses", "cattlefarm")
                .addBuilding("houses", "chickenfarmnorm")
                .addBuilding("houses", "pigfarm")
                .addBuilding("houses", "sheepchickenfarm")
                // Townhalls
                .addBuilding("townhalls", "manor")
                .addBuilding("townhalls", "fort")
                .addBuilding("townhalls", "abbey")
                // Marvels
                .addBuilding("marvel", "abbaye")
                .addBuilding("marvel", "cathcluny")
                .addBuilding("marvel", "cathedral")
                // Extra
                .addBuilding("extra", "well")
                .addBuilding("extra", "fountain")
                .addBuilding("extra", "watchtower")
                // Special buildings (groves, inns, orchards)
                .addBuilding("special", "grove")
                .addBuilding("special", "inn")
                .addBuilding("houses", "largeorchard")
                // Player-controlled town halls
                .addBuilding("townhalls", "playerlargefort")
                .addBuilding("townhalls", "playersmallfort")
                // Lone buildings (isolated structures like hermit huts)
                .addBuilding("lone", "hermithut")
                .addBuilding("lone", "lonetavern")
                .addBuilding("lone", "lonemill")
                // Walls
                .addBuilding("walls", "wall_section")
                .addBuilding("walls", "wall_corner")
                .addBuilding("walls", "gatehouse")
                // Villagers
                .addVillagerType("chief", new Culture.VillagerArchetype("Chief", "leader", true))
                .addVillagerType("farmer", new Culture.VillagerArchetype("Farmer", "worker", true))
                .addVillagerType("blacksmith", new Culture.VillagerArchetype("Blacksmith", "worker", true))
                .addVillagerType("baker", new Culture.VillagerArchetype("Baker", "worker", true))
                .addVillagerType("wife", new Culture.VillagerArchetype("Wife", "worker", false))
                .addVillagerType("child", new Culture.VillagerArchetype("Child", "child", true))
                // Trade goods
                .addTradeGood("millenaire:calva")
                .addTradeGood("millenaire:boudin")
                .addTradeGood("millenaire:cider")
                .addTradeGood("millenaire:norman_axe")
                .addTradeGood("millenaire:norman_broadsword");

        // == INDIAN/ HINDI CULTURE ==
        INDIAN = new Culture("indian", "Indian")
                // Core buildings
                .addBuilding("basic", "peasanthouse")
                .addBuilding("basic", "largepeasanthouse")
                .addBuilding("basic", "forge")
                .addBuilding("basic", "craftsmanhouse")
                .addBuilding("basic", "weaverhouse")
                .addBuilding("basic", "quarry")
                .addBuilding("basic", "lumbermanhouse")
                .addBuilding("basic", "paddy") // Rice field
                .addBuilding("basic", "spicegarden")
                .addBuilding("basic", "sugarplantation")
                .addBuilding("basic", "cottonfield")
                .addBuilding("basic", "villagetemple")
                .addBuilding("basic", "bazaar")
                .addBuilding("basic", "inn")
                .addBuilding("basic", "brickkiln")
                .addBuilding("basic", "painterhouse")
                // Townhalls
                .addBuilding("townhalls", "chiefhouse")
                .addBuilding("townhalls", "fort")
                .addBuilding("townhalls", "palace")
                // Nodes
                .addBuilding("nodes", "hindushrine")
                .addBuilding("nodes", "tank") // Water reservoir
                .addBuilding("nodes", "well")
                .addBuilding("nodes", "villagetree")
                // Villagers
                .addVillagerType("sadhu", new Culture.VillagerArchetype("Sadhu", "leader", true))
                .addVillagerType("farmer_hindi", new Culture.VillagerArchetype("Farmer", "worker", true))
                .addVillagerType("blacksmith_hindi", new Culture.VillagerArchetype("Blacksmith", "worker", true))
                .addVillagerType("wife_hindi", new Culture.VillagerArchetype("Wife", "worker", false))
                .addVillagerType("child_hindi", new Culture.VillagerArchetype("Child", "child", true))
                // Trade goods
                .addTradeGood("millenaire:rasgulla")
                .addTradeGood("millenaire:turmeric")
                .addTradeGood("millenaire:vegcurry")
                .addTradeGood("millenaire:chickencurry")
                .addTradeGood("millenaire:rice");

        // == JAPANESE CULTURE ==
        JAPANESE = new Culture("japanese", "Japanese")
                // Core buildings
                .addBuilding("core", "japanesepeasanth")
                .addBuilding("core", "japanesefarm")
                .addBuilding("core", "japaneseforge")
                .addBuilding("core", "japanesecrafter")
                .addBuilding("core", "japaneselmh") // Lumberman
                .addBuilding("core", "japanesekitchen")
                .addBuilding("core", "japanesesamuraih") // Samurai house
                .addBuilding("core", "japanesemarket")
                .addBuilding("core", "japaneseinn")
                .addBuilding("core", "japanesepaddy") // Rice field
                .addBuilding("core", "japanesefishery")
                .addBuilding("core", "japanesegrove")
                .addBuilding("core", "japanesequarry")
                .addBuilding("core", "bath")
                .addBuilding("core", "japanesepagoda")
                .addBuilding("core", "japaneseshintoshrine")
                .addBuilding("core", "japanesezen")
                .addBuilding("core", "well")
                // Townhalls
                .addBuilding("townhalls", "japanesefarmbig")
                .addBuilding("townhalls", "japanesefort")
                .addBuilding("townhalls", "japanesepalace")
                .addBuilding("townhalls", "sakebrewery")
                // Villagers
                .addVillagerType("samurai", new Culture.VillagerArchetype("Samurai", "warrior", true))
                .addVillagerType("farmer_japanese", new Culture.VillagerArchetype("Farmer", "worker", true))
                .addVillagerType("blacksmith_japanese", new Culture.VillagerArchetype("Blacksmith", "worker", true))
                .addVillagerType("wife_japanese", new Culture.VillagerArchetype("Wife", "worker", false))
                .addVillagerType("child_japanese", new Culture.VillagerArchetype("Child", "child", true))
                // Trade goods
                .addTradeGood("millenaire:sake")
                .addTradeGood("millenaire:udon")
                .addTradeGood("millenaire:tachi_sword")
                .addTradeGood("millenaire:yumi_bow")
                .addTradeGood("millenaire:ikayaki");

        // == MAYAN CULTURE ==
        MAYAN = new Culture("mayan", "Mayan")
                // Core buildings
                .addBuilding("core", "mayanhouse")
                .addBuilding("core", "mayanlargepeasanthouse")
                .addBuilding("core", "mayancornfarm")
                .addBuilding("core", "mayancacaofarm")
                .addBuilding("core", "mayanobsidiancrafter")
                .addBuilding("core", "mayancrafter")
                .addBuilding("core", "mayanlumber")
                .addBuilding("core", "mayanmining")
                .addBuilding("core", "mayanmarket")
                .addBuilding("core", "mayaninn")
                .addBuilding("core", "mayantemple")
                .addBuilding("core", "mayanshamangrounds")
                .addBuilding("core", "mayanballcourt")
                .addBuilding("core", "mayanacropolis")
                .addBuilding("core", "mayancalendar")
                .addBuilding("core", "mayanarmyforge")
                .addBuilding("core", "mayanbakery")
                .addBuilding("core", "mayansculptorhouse")
                .addBuilding("core", "mayangrove")
                // Villagers
                .addVillagerType("priest", new Culture.VillagerArchetype("Priest", "leader", true))
                .addVillagerType("farmer_mayan", new Culture.VillagerArchetype("Farmer", "worker", true))
                .addVillagerType("warrior_mayan", new Culture.VillagerArchetype("Warrior", "warrior", true))
                .addVillagerType("wife_mayan", new Culture.VillagerArchetype("Wife", "worker", false))
                .addVillagerType("child_mayan", new Culture.VillagerArchetype("Child", "child", true))
                // Trade goods
                .addTradeGood("millenaire:balche")
                .addTradeGood("millenaire:sikilpah")
                .addTradeGood("millenaire:cacauhaa")
                .addTradeGood("millenaire:mayan_axe")
                .addTradeGood("millenaire:mayan_mace")
                .addTradeGood("millenaire:maize");

        // == BYZANTINE CULTURE ==
        BYZANTINE = new Culture("byzantines", "Byzantine")
                // Core buildings
                .addBuilding("houses", "farm")
                .addBuilding("houses", "workerhouse")
                .addBuilding("houses", "smelter")
                .addBuilding("houses", "toolsmith")
                .addBuilding("houses", "mason")
                .addBuilding("houses", "woodcutter")
                .addBuilding("houses", "mine")
                .addBuilding("houses", "sheepfarm")
                .addBuilding("houses", "fishfarm")
                .addBuilding("houses", "silkfarm")
                .addBuilding("houses", "taverna")
                .addBuilding("houses", "dressmaker")
                .addBuilding("houses", "architect")
                .addBuilding("houses", "presbytery")
                // Fields
                .addBuilding("fields", "wheatfield")
                .addBuilding("fields", "carrotfield")
                .addBuilding("fields", "vineyard")
                .addBuilding("fields", "pinegrove")
                .addBuilding("fields", "acaciagrove")
                // Townhalls
                .addBuilding("townhalls", "byzfort")
                .addBuilding("townhalls", "byzpalace")
                .addBuilding("townhalls", "byzantinechurch")
                // Path nodes
                .addBuilding("pathnodes", "olivetree")
                .addBuilding("pathnodes", "pergola")
                .addBuilding("pathnodes", "garden")
                // Villagers
                .addVillagerType("bishop", new Culture.VillagerArchetype("Bishop", "leader", true))
                .addVillagerType("farmer_byzantine", new Culture.VillagerArchetype("Farmer", "worker", true))
                .addVillagerType("blacksmith_byzantine", new Culture.VillagerArchetype("Blacksmith", "worker", true))
                .addVillagerType("wife_byzantine", new Culture.VillagerArchetype("Wife", "worker", false))
                .addVillagerType("child_byzantine", new Culture.VillagerArchetype("Child", "child", true))
                // Trade goods
                .addTradeGood("millenaire:byzantine_icon_small")
                .addTradeGood("millenaire:byzantine_icon_medium")
                .addTradeGood("millenaire:byzantine_icon_large")
                .addTradeGood("millenaire:feta")
                .addTradeGood("millenaire:souvlaki")
                .addTradeGood("millenaire:wine_fancy")
                .addTradeGood("millenaire:silk");

        // Register all cultures
        CULTURES.put(NORMAN.getId(), NORMAN);
        CULTURES.put(INDIAN.getId(), INDIAN);
        CULTURES.put(JAPANESE.getId(), JAPANESE);
        CULTURES.put(MAYAN.getId(), MAYAN);
        CULTURES.put(BYZANTINE.getId(), BYZANTINE);
    }

    /**
     * Get a culture by its ID
     */
    public static Culture getCulture(String id) {
        return CULTURES.get(id);
    }

    /**
     * Get all registered cultures
     */
    public static Map<String, Culture> getAllCultures() {
        return new HashMap<>(CULTURES);
    }

    /**
     * Check if a culture exists
     */
    public static boolean hasCulture(String id) {
        return CULTURES.containsKey(id);
    }

    public static void initVillages() {
        // Norman Villages
        loadVillage(NORMAN, "agricole");
        loadVillage(NORMAN, "artisans");
        loadVillage(NORMAN, "bourg_autonome");
        loadVillage(NORMAN, "controlled");
        loadVillage(NORMAN, "ecclesiastique");
        loadVillage(NORMAN, "militaire");
        loadVillage(NORMAN, "walledcontrolled");

        // Indian Villages - just a few examples
        loadVillage(INDIAN, "indian_agricole");
        loadVillage(INDIAN, "indian_fort");

        // Japanese Villages
        loadVillage(JAPANESE, "nogyo"); // Agriculture
        loadVillage(JAPANESE, "gunji"); // Military

        // Mayan Villages
        loadVillage(MAYAN, "agriculture");
        loadVillage(MAYAN, "military");

        // Byzantine Villages
        loadVillage(BYZANTINE, "religiousvillage");
        loadVillage(BYZANTINE, "militaryvillage");
    }

    private static void loadVillage(Culture culture, String villageName) {
        System.out.println("[MillenaireCultures] Loading village: " + villageName + " for culture: " + culture.getId());
        net.minecraft.resources.ResourceLocation loc = new net.minecraft.resources.ResourceLocation("millenaire",
                "cultures/" + culture.getId() + "/villages/" + villageName + ".txt");
        VillageType type = VillageConfigLoader.loadVillageType(loc, villageName);
        if (type != null) {
            culture.addVillageType(type);
            System.out.println(
                    "[MillenaireCultures] SUCCESS: Added village type '" + villageName + "' to " + culture.getId());
        } else {
            System.err.println("[MillenaireCultures] FAILED: Could not load village type '" + villageName + "' for "
                    + culture.getId());
        }
    }
}
