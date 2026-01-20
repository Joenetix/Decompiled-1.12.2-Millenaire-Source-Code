package org.millenaire.common.culture;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.entities.Citizen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * Defines a type of villager in a Millénaire culture.
 * Contains appearance, behavior, and AI configuration.
 * Ported from 1.12.2 with enhanced tag support.
 */
public class VillagerType {
    // === Villager Type Tags (from 1.12.2 DocumentedElement annotations) ===

    @DocumentedElement.Documentation("The villager is a local merchant that will travel from village to village.")
    public static final String TAG_LOCALMERCHANT = "localmerchant";

    @DocumentedElement.Documentation("The villager is a foreign merchant that comes from outside the culture.")
    public static final String TAG_FOREIGNMERCHANT = "foreignmerchant";

    @DocumentedElement.Documentation("The villager is a child.")
    public static final String TAG_CHILD = "child";

    @DocumentedElement.Documentation("The villager is the village chief/leader.")
    public static final String TAG_CHIEF = "chief";

    @DocumentedElement.Documentation("The villager can sell goods to the player.")
    public static final String TAG_SELLER = "seller";

    @DocumentedElement.Documentation("The villager is a visitor from another village.")
    public static final String TAG_VISITOR = "visitor";

    @DocumentedElement.Documentation("The villager will help defend the village during attacks.")
    public static final String TAG_HELPSINATTACKS = "helpinattacks";

    @DocumentedElement.Documentation("The villager is hostile to players.")
    public static final String TAG_HOSTILE = "hostile";

    @DocumentedElement.Documentation("The villager won't clear leaves when working.")
    public static final String TAG_NOLEAFCLEARING = "noleafclearing";

    @DocumentedElement.Documentation("The villager uses a bow for combat.")
    public static final String TAG_ARCHER = "archer";

    @DocumentedElement.Documentation("The villager is a raider/bandit.")
    public static final String TAG_RAIDER = "raider";

    @DocumentedElement.Documentation("The villager cannot be teleported.")
    public static final String TAG_NOTELEPORT = "noteleport";

    @DocumentedElement.Documentation("The villager's name is hidden above their head.")
    public static final String TAG_HIDENAME = "hidename";

    @DocumentedElement.Documentation("Show health bar above the villager.")
    public static final String TAG_SHOWHEALTH = "showhealth";

    @DocumentedElement.Documentation("The villager stays near the village center for defense.")
    public static final String TAG_DEFENSIVE = "defensive";

    @DocumentedElement.Documentation("The villager cannot be resurrected after death.")
    public static final String TAG_NORESURRECT = "noresurrect";

    @DocumentedElement.Documentation("The villager is a craftsman.")
    public static final String TAG_CRAFTSMAN = "craftsman";

    @DocumentedElement.Documentation("The villager can upgrade buildings.")
    public static final String TAG_BUILDER = "builder";

    @DocumentedElement.Documentation("The villager is a farmer.")
    public static final String TAG_FARMER = "farmer";

    @DocumentedElement.Documentation("The villager is a miner.")
    public static final String TAG_MINER = "miner";

    @DocumentedElement.Documentation("The villager is a lumberjack.")
    public static final String TAG_LUMBERMAN = "lumberman";

    @DocumentedElement.Documentation("The villager tends to cattle/livestock.")
    public static final String TAG_CATTLEFARMER = "cattlefarmer";

    @DocumentedElement.Documentation("The villager is a soldier/guard.")
    public static final String TAG_SOLDIER = "soldier";

    @DocumentedElement.Documentation("The villager is a wife/spouse.")
    public static final String TAG_WIFE = "wife";

    @DocumentedElement.Documentation("The villager stays inside buildings.")
    public static final String TAG_STAYSINSIDE = "staysinside";

    public String key;
    public Culture culture;
    public String name;
    public String altname;
    public String altkey;
    public String model;

    public float baseScale = 1.0F;
    public float baseSpeed = 0.55F;
    public int gender; // 1 = male, 2 = female

    public String maleChild;
    public String femaleChild;

    public List<String> textures = new ArrayList<>();
    public Map<String, List<String>> clothes = new HashMap<>();

    public Map<Item, Integer> requiredGoods = new HashMap<>();
    public Map<Item, Integer> startingInv = new HashMap<>();
    public List<Item> itemsNeeded = new ArrayList<>();
    public List<String> toolsCategoriesNeeded = new ArrayList<>();
    public Item startingWeapon;

    public List<Item> bringBackHomeGoods = new ArrayList<>();
    public List<Item> collectGoods = new ArrayList<>();

    public int baseAttackStrength = 1;

    public VillagerType() {
        // Default constructor
    }

    /**
     * Constructor for creating VillagerType from VillagerArchetype.
     */
    public VillagerType(String key, String name, boolean isMale, String role) {
        this.key = key;
        this.name = name;
        this.gender = isMale ? 1 : 2;
        // Role can be used to set tags
        if ("leader".equals(role)) {
            addTag(TAG_CHIEF);
        } else if ("seller".equals(role)) {
            addTag(TAG_SELLER);
        }
    }

    public int health = 20;
    public int hireCost = 0;
    public Map<Item, Integer> foreignMerchantStock = new HashMap<>();
    public int chanceWeight = 0;

    public List<String> tags = new ArrayList<>();

    // Goal AI - list of goal keys this villager can execute
    public List<String> goals = new ArrayList<>();

    // Travel book display
    public String travelBookCategory = null;
    public boolean travelBookDisplay = true;

    public boolean isChild = false;
    public boolean isChief = false;
    public boolean canSell = false;
    public boolean outputCommonResources = false;
    public boolean visitor = false;
    public boolean helpInAttacks = false;
    public boolean isLocalMerchant = false;
    public boolean isForeignMerchant = false;
    public boolean hostile = false;
    public boolean isArcher = false;
    public boolean noleafclearing = false;
    public boolean isRaider = false;
    public boolean noTeleport = false;
    public boolean hideName = false;
    public boolean showHealth = false;
    public boolean isDefensive = false;
    public boolean noResurrect = false;

    public String familyNameList;
    public String firstNameList;

    public VillagerType(Culture c, String key) {
        this.culture = c;
        this.key = key;
    }

    public boolean containsTags(String tag) {
        return this.tags.contains(tag.toLowerCase());
    }

    public void addTag(String tag) {
        if (!this.tags.contains(tag.toLowerCase())) {
            this.tags.add(tag.toLowerCase());
        }
    }

    public ResourceLocation getNewTexture() {
        if (textures.isEmpty())
            return new ResourceLocation("minecraft", "textures/entity/steve.png");
        String texture = this.textures.get((int) (Math.random() * this.textures.size()));
        return texture.contains(":") ? new ResourceLocation(texture) : new ResourceLocation("millenaire", texture);
    }

    public String getRandomFamilyName(Set<String> namesTaken) {
        // Placeholder until Culture has namelists
        return "Doe";
    }

    public String getRandomFirstName() {
        // Placeholder until Culture has namelists
        return "John";
    }

    public boolean isTextureValid(String texture) {
        for (String s : this.textures) {
            if (s.equalsIgnoreCase(texture)) {
                return true;
            }
        }
        return false;
    }
}
