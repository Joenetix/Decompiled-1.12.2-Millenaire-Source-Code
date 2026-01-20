package org.millenaire.common.village;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillagerType;
import org.millenaire.common.world.MillWorldData;
import org.millenaire.entities.Citizen;

import java.util.*;

public class VillagerRecord {
    public static final double RIGHT_HANDED_CHANCE = 0.8;

    public MillWorldData mw;

    // Identity
    public long villagerId;
    public UUID uuid; // Tracks the actual entity UUID if alive
    public String type; // Key in Culture.villagerTypes
    public String firstName;
    public String familyName;
    public String fathersName = "";
    public String mothersName = "";
    public String spousesName = "";
    public String maidenName = "";
    public int gender; // 1=male, 2=female

    // State
    public boolean killed = false;
    public boolean raidingVillage = false;
    public boolean awayraiding = false;
    public boolean awayhired = false;
    public boolean flawedRecord = false;

    // Locations
    public BlockPos housePos;
    public BlockPos townHallPos;
    public BlockPos originalVillagePos;

    // Data
    public int nb = 1; // Used for serialization version or count? Original says 'nb'
    public int size = 0; // 0 for child, 20 for adult usually
    public float scale = 1.0F;
    public boolean rightHanded = true;
    public ResourceLocation texture;

    // Inventory & Tags
    public Map<Item, Integer> inventory = new HashMap<>();
    public List<String> questTags = new ArrayList<>();

    // References (Non-serialized usually, or lazily loaded)
    // References (Non-serialized usually, or lazily loaded)
    private Culture culture;

    public void setCulture(Culture culture) {
        this.culture = culture;
    }

    public Culture getCulture() {
        return this.culture;
    }

    public long raiderSpawn = 0;

    public VillagerRecord(MillWorldData mw) {
        this.mw = mw;
    }

    public VillagerRecord(MillWorldData mw, Culture c) {
        this.mw = mw;
        this.culture = c;
    }

    public static VillagerRecord createVillagerRecord(Culture c, String type, MillWorldData worldData,
            BlockPos housePos, BlockPos thPos, String firstName, String familyName, long villagerId) {
        VillagerRecord vr = new VillagerRecord(worldData, c);

        VillagerType vType = c.getVillagerType(type);
        if (vType == null) {
            // Log error
            return null;
        }

        vr.type = type;
        vr.housePos = housePos;
        vr.townHallPos = thPos;

        if (familyName != null) {
            vr.familyName = familyName;
        } else {
            // TODO: Get taken names from Building
            Set<String> taken = new HashSet<>();
            vr.familyName = vType.getRandomFamilyName(taken);
        }

        if (firstName != null) {
            vr.firstName = firstName;
        } else {
            vr.firstName = vType.getRandomFirstName();
        }

        vr.villagerId = villagerId;
        vr.gender = vType.gender;
        vr.texture = vType.getNewTexture();
        vr.rightHanded = Math.random() < RIGHT_HANDED_CHANCE;

        // Initial setup for child vs adult handled by size/scale later
        // worldData.registerVillagerRecord(vr, true); // TODO: Implement register

        return vr;
    }

    public void updateRecord(Citizen citizen) {
        this.firstName = citizen.getName().getString().split(" ")[0]; // Rough approximation if name set
        // Update inventory, pos, etc from entity
    }

    public void save(CompoundTag tag, String label) {
        tag.putLong(label + "_lid", villagerId);
        tag.putString(label + "_type", type);
        tag.putString(label + "_firstName", firstName);
        tag.putString(label + "_familyName", familyName);
        tag.putInt(label + "_gender", gender);
        if (texture != null)
            tag.putString(label + "_texture", texture.toString());

        if (culture != null)
            tag.putString(label + "_culture", culture.getId());

        if (housePos != null)
            tag.putLong(label + "_housePos", housePos.asLong());
        if (townHallPos != null)
            tag.putLong(label + "_townHallPos", townHallPos.asLong());

        // Inventory
        ListTag invList = new ListTag();
        for (Map.Entry<Item, Integer> entry : inventory.entrySet()) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putString("item", ForgeRegistries.ITEMS.getKey(entry.getKey()).toString());
            itemTag.putInt("amount", entry.getValue());
            invList.add(itemTag);
        }
        tag.put(label + "_inventory", invList);
    }

    public static VillagerRecord load(MillWorldData mw, CompoundTag tag, String label) {
        VillagerRecord vr = new VillagerRecord(mw);
        vr.villagerId = tag.getLong(label + "_lid");
        vr.type = tag.getString(label + "_type");
        vr.firstName = tag.getString(label + "_firstName");
        vr.familyName = tag.getString(label + "_familyName");
        vr.gender = tag.getInt(label + "_gender");

        if (tag.contains(label + "_texture")) {
            vr.texture = new ResourceLocation(tag.getString(label + "_texture"));
        }

        String cultureKey = tag.getString(label + "_culture");
        vr.culture = Culture.getCultureByName(cultureKey);

        if (tag.contains(label + "_housePos"))
            vr.housePos = BlockPos.of(tag.getLong(label + "_housePos"));
        if (tag.contains(label + "_townHallPos"))
            vr.townHallPos = BlockPos.of(tag.getLong(label + "_townHallPos"));

        // Inventory
        if (tag.contains(label + "_inventory")) {
            ListTag invList = tag.getList(label + "_inventory", 10);
            for (int i = 0; i < invList.size(); i++) {
                CompoundTag itemTag = invList.getCompound(i);
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemTag.getString("item")));
                if (item != null) {
                    vr.inventory.put(item, itemTag.getInt("amount"));
                }
            }
        }

        return vr;
    }

    public VillagerType getType() {
        if (culture == null)
            return null;
        return culture.getVillagerType(this.type);
    }

    public String getName() {
        return firstName + " " + familyName;
    }
}
