package org.millenaire.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.TradeGood;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.BuildingProject;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.BuildingLocation;
import org.millenaire.common.world.MillWorldData;
import org.millenaire.common.village.VillagerRecord;
import org.millenaire.quest.Quest;
import org.millenaire.quest.QuestInstance;
import org.millenaire.quest.QuestInstanceVillager;
import org.millenaire.common.world.UserProfile;
// import org.millenaire.common.ui.PujaSacrifice; // Missing

import net.minecraft.core.registries.BuiltInRegistries;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class StreamReadWrite {

    public static final int MAX_STR_LENGTH = 2048;

    public static CopyOnWriteArrayList<Boolean> readBooleanList(FriendlyByteBuf ds) {
        CopyOnWriteArrayList<Boolean> v = new CopyOnWriteArrayList<>();
        int nb = ds.readInt();
        for (int i = 0; i < nb; i++) {
            v.add(ds.readBoolean());
        }
        return v;
    }

    public static List<BuildingLocation> readBuildingLocationList(FriendlyByteBuf ds) {
        List<BuildingLocation> v = new ArrayList<>();
        int nb = ds.readInt();
        for (int i = 0; i < nb; i++) {
            v.add(readNullableBuildingLocation(ds));
        }
        return v;
    }

    // Stubbed until BuildingPlan is confirmed/ported
    // public static BuildingPlan readBuildingPlanInfo(FriendlyByteBuf ds, Culture
    // culture) { ... }

    public static HashMap<InvItem, Integer> readInventory(FriendlyByteBuf ds) {
        HashMap<InvItem, Integer> inv = new HashMap<>();
        int nb = ds.readInt();
        for (int i = 0; i < nb; i++) {
            Item item = Item.byId(ds.readInt());
            InvItem invItem = InvItem.createInvItem(item, ds.readInt());
            inv.put(invItem, ds.readInt());
        }
        return inv;
    }

    private static ItemStack readItemStack(FriendlyByteBuf buffer) {
        try {
            if (buffer.readBoolean()) {
                return buffer.readItem();
            }
            return ItemStack.EMPTY;
        } catch (Exception e) {
            MillLog.printException("Error reading itemstack", e);
            return ItemStack.EMPTY;
        }
    }

    public static CopyOnWriteArrayList<ItemStack> readItemStackList(FriendlyByteBuf ds) {
        CopyOnWriteArrayList<ItemStack> v = new CopyOnWriteArrayList<>();
        int nb = ds.readInt();
        for (int i = 0; i < nb; i++) {
            v.add(readNullableItemStack(ds));
        }
        return v;
    }

    private static CompoundTag readNBTTagCompound(FriendlyByteBuf buffer) {
        return buffer.readNbt();
    }

    public static BuildingLocation readNullableBuildingLocation(FriendlyByteBuf ds) {
        if (!ds.readBoolean()) {
            return null;
        }
        BuildingLocation bl = new BuildingLocation();
        bl.isCustomBuilding = ds.readBoolean();
        bl.planKey = readNullableString(ds);
        bl.shop = readNullableString(ds);
        bl.minx = ds.readInt();
        bl.maxx = ds.readInt();
        bl.miny = ds.readInt();
        bl.maxy = ds.readInt();
        bl.minz = ds.readInt();
        bl.maxz = ds.readInt();
        bl.minxMargin = ds.readInt();
        bl.maxxMargin = ds.readInt();
        bl.minyMargin = ds.readInt();
        bl.maxyMargin = ds.readInt();
        bl.minzMargin = ds.readInt();
        bl.maxzMargin = ds.readInt();
        bl.orientation = ds.readInt();
        bl.length = ds.readInt();
        bl.width = ds.readInt();
        bl.level = ds.readInt();
        bl.setVariation(ds.readInt());
        bl.reputation = ds.readInt();
        bl.price = ds.readInt();
        bl.version = ds.readInt();
        bl.pos = readNullablePoint(ds);
        bl.chestPos = readNullablePoint(ds);
        bl.sleepingPos = readNullablePoint(ds);
        bl.sellingPos = readNullablePoint(ds);
        bl.craftingPos = readNullablePoint(ds);
        bl.shelterPos = readNullablePoint(ds);
        bl.defendingPos = readNullablePoint(ds);
        String cultureKey = readNullableString(ds);
        bl.culture = Culture.getCultureByName(cultureKey);
        bl.subBuildings = readStringList(ds);
        bl.showTownHallSigns = ds.readBoolean();
        bl.upgradesAllowed = ds.readBoolean();
        bl.bedrocklevel = ds.readBoolean();
        bl.isSubBuildingLocation = ds.readBoolean();
        return bl;
    }

    public static BuildingProject readNullableBuildingProject(FriendlyByteBuf ds, Culture culture) {
        if (!ds.readBoolean()) {
            return null;
        }
        BuildingProject bp = new BuildingProject();
        bp.isCustomBuilding = ds.readBoolean();
        bp.key = readNullableString(ds);
        bp.location = readNullableBuildingLocation(ds);
        // Logic to fetch plan from culture stubbed for now as BuildingProject logic
        // might vary
        return bp;
    }

    public static TradeGood readNullableGoods(FriendlyByteBuf ds, Culture culture) {
        if (!ds.readBoolean()) {
            return null;
        }
        String cultureKey = ds.readUtf(MAX_STR_LENGTH);
        Item item = Item.byId(ds.readInt());
        InvItem iv = InvItem.createInvItem(item, ds.readByte());
        TradeGood g = new TradeGood("generated", Culture.getCultureByName(cultureKey), iv);
        g.requiredTag = readNullableString(ds);
        g.travelBookCategory = readNullableString(ds);
        g.autoGenerate = ds.readBoolean();
        g.minReputation = ds.readInt();
        return g;
    }

    public static ItemStack readNullableItemStack(FriendlyByteBuf ds) {
        return ds.readBoolean() ? ds.readItem() : null;
    }

    public static Point readNullablePoint(FriendlyByteBuf ds) {
        if (!ds.readBoolean()) {
            return null;
        }
        return new Point(ds.readInt(), ds.readInt(), ds.readInt());
    }

    public static QuestInstance readNullableQuestInstance(MillWorldData mw, FriendlyByteBuf ds) {
        if (!ds.readBoolean()) {
            return null;
        }
        long id = ds.readLong();
        String questKey = ds.readUtf(MAX_STR_LENGTH);
        // Quest.quests stubbed
        // if (!Quest.quests.containsKey(questKey)) return null;
        // Quest quest = Quest.quests.get(questKey);
        Quest quest = null; // Stub

        UUID uuid = ds.readUUID();
        UserProfile profile = mw.getProfile(uuid, "");
        int currentStep = ds.readUnsignedByte();
        long startTime = ds.readLong();
        long currentStepStart = ds.readLong();

        HashMap<String, QuestInstanceVillager> villagers = new HashMap<>();
        int nb = ds.readUnsignedByte();
        for (int i = 0; i < nb; i++) {
            String key = ds.readUtf(MAX_STR_LENGTH);
            villagers.put(key, readNullableQuestVillager(mw, ds, key));
        }

        QuestInstance qi = new QuestInstance(mw, quest, profile, villagers, startTime, currentStep, currentStepStart);
        qi.uniqueid = id;
        return qi;
    }

    public static QuestInstanceVillager readNullableQuestVillager(MillWorldData mw, FriendlyByteBuf ds, String key) {
        if (!ds.readBoolean())
            return null;
        Point p = readNullablePoint(ds);
        BlockPos pos = p != null ? new BlockPos(p.getiX(), p.getiY(), p.getiZ()) : null;
        return new QuestInstanceVillager(key, ds.readLong(), pos);
    }

    public static ResourceLocation readNullableResourceLocation(FriendlyByteBuf ds) {
        return ds.readBoolean() ? new ResourceLocation(ds.readUtf(MAX_STR_LENGTH)) : null;
    }

    public static String readNullableString(FriendlyByteBuf ds) {
        return ds.readBoolean() ? ds.readUtf(MAX_STR_LENGTH) : null;
    }

    public static UUID readNullableUUID(FriendlyByteBuf ds) {
        return ds.readBoolean() ? ds.readUUID() : null;
    }

    public static VillagerRecord readNullableVillagerRecord(MillWorldData mw, FriendlyByteBuf ds) {
        if (!ds.readBoolean()) {
            return null;
        }
        VillagerRecord vr = new VillagerRecord(mw);
        vr.villagerId = ds.readLong();
        vr.type = readNullableString(ds);
        vr.firstName = readNullableString(ds);
        vr.familyName = readNullableString(ds);
        vr.texture = readNullableResourceLocation(ds);
        vr.nb = ds.readInt();
        vr.gender = ds.readInt();
        vr.size = ds.readInt();
        vr.scale = ds.readFloat();
        vr.rightHanded = ds.readBoolean();
        vr.setCulture(Culture.getCultureByName(readNullableString(ds)));
        vr.fathersName = readNullableString(ds);
        vr.mothersName = readNullableString(ds);
        vr.spousesName = readNullableString(ds);
        vr.maidenName = readNullableString(ds);
        vr.killed = ds.readBoolean();
        vr.raidingVillage = ds.readBoolean();
        vr.awayraiding = ds.readBoolean();
        vr.awayhired = ds.readBoolean();
        Point housePos = readNullablePoint(ds);
        if (housePos != null)
            vr.housePos = new BlockPos(housePos.getiX(), housePos.getiY(), housePos.getiZ());

        Point townHallPos = readNullablePoint(ds);
        if (townHallPos != null)
            vr.townHallPos = new BlockPos(townHallPos.getiX(), townHallPos.getiY(), townHallPos.getiZ());

        Point originalVillagePos = readNullablePoint(ds);
        if (originalVillagePos != null)
            vr.originalVillagePos = new BlockPos(originalVillagePos.getiX(), originalVillagePos.getiY(),
                    originalVillagePos.getiZ());

        vr.raiderSpawn = ds.readLong();

        HashMap<InvItem, Integer> readInv = readInventory(ds);
        for (Map.Entry<InvItem, Integer> entry : readInv.entrySet()) {
            if (entry.getKey() != null && entry.getKey().getItem() != null) {
                vr.inventory.put(entry.getKey().getItem(), entry.getValue());
            }
        }

        vr.questTags = readStringList(ds);
        return vr;
    }

    // PujaSacrifice methods commented out

    public static ConcurrentHashMap<Point, Integer> readPointIntegerMap(FriendlyByteBuf ds) {
        ConcurrentHashMap<Point, Integer> map = new ConcurrentHashMap<>();
        int nb = ds.readInt();
        for (int i = 0; i < nb; i++) {
            Point p = readNullablePoint(ds);
            map.put(p, ds.readInt());
        }
        return map;
    }

    public static CopyOnWriteArrayList<Point> readPointList(FriendlyByteBuf ds) {
        CopyOnWriteArrayList<Point> v = new CopyOnWriteArrayList<>();
        int nb = ds.readInt();
        for (int i = 0; i < nb; i++) {
            v.add(readNullablePoint(ds));
        }
        return v;
    }

    // readProjectListList stubbed/omitted

    public static CopyOnWriteArrayList<String> readStringList(FriendlyByteBuf ds) {
        CopyOnWriteArrayList<String> v = new CopyOnWriteArrayList<>();
        int nb = ds.readInt();
        for (int i = 0; i < nb; i++) {
            v.add(readNullableString(ds));
        }
        return v;
    }

    // Writing methods...
    public static void writeNullableString(String s, FriendlyByteBuf data) {
        data.writeBoolean(s != null);
        if (s != null) {
            data.writeUtf(s);
        }
    }

    public static void writeNullablePoint(Point p, FriendlyByteBuf data) {
        data.writeBoolean(p != null);
        if (p != null) {
            data.writeInt(p.getiX());
            data.writeInt(p.getiY());
            data.writeInt(p.getiZ());
        }
    }

    public static void writeNullableItemStack(ItemStack is, FriendlyByteBuf data) {
        data.writeBoolean(is != null);
        if (is != null) {
            data.writeItem(is);
        }
    }

    public static void writeNullableVillagerRecord(VillagerRecord vr, FriendlyByteBuf data) {
        data.writeBoolean(vr != null);
        if (vr != null) {
            data.writeLong(vr.villagerId);
            writeNullableString(vr.type, data);
            writeNullableString(vr.firstName, data);
            writeNullableString(vr.familyName, data);
            writeNullableResourceLocation(vr.texture, data);
            data.writeInt(vr.nb);
            data.writeInt(vr.gender);
            data.writeInt(vr.size);
            data.writeFloat(vr.scale);
            data.writeBoolean(vr.rightHanded);
            // FIX: Write Culture key, not Type key, because the reader expects Culture
            writeNullableString(vr.getCulture() != null ? vr.getCulture().key : null, data);
            writeNullableString(vr.fathersName, data);
            writeNullableString(vr.mothersName, data);
            writeNullableString(vr.spousesName, data);
            writeNullableString(vr.maidenName, data);
            data.writeBoolean(vr.killed);
            data.writeBoolean(vr.raidingVillage);
            data.writeBoolean(vr.awayraiding);
            data.writeBoolean(vr.awayhired);

            // Convert BlockPos to Point for serialization to match reader
            writeNullablePoint(vr.housePos != null ? new Point(vr.housePos) : null, data);
            writeNullablePoint(vr.townHallPos != null ? new Point(vr.townHallPos) : null, data);
            writeNullablePoint(vr.originalVillagePos != null ? new Point(vr.originalVillagePos) : null, data);

            data.writeLong(vr.raiderSpawn);

            // Inventory conversion
            HashMap<InvItem, Integer> invMap = new HashMap<>();
            for (Map.Entry<Item, Integer> e : vr.inventory.entrySet()) {
                invMap.put(InvItem.createInvItem(e.getKey()), e.getValue());
            }
            writeInventory(invMap, data);

            writeStringList(vr.questTags, data);
        }
    }

    public static void writeInventory(Map<InvItem, Integer> inventory, FriendlyByteBuf data) {
        data.writeInt(inventory.size());
        for (InvItem key : inventory.keySet()) {
            data.writeInt(BuiltInRegistries.ITEM.getId(key.getItem()));
            data.writeInt(0); // meta stub
            data.writeInt(inventory.get(key));
        }
    }

    public static void writeStringList(List<String> strings, FriendlyByteBuf data) {
        data.writeInt(strings.size());
        for (String s : strings) {
            writeNullableString(s, data);
        }
    }

    public static void writeNullableResourceLocation(ResourceLocation rs, FriendlyByteBuf data) {
        data.writeBoolean(rs != null);
        if (rs != null) {
            data.writeUtf(rs.toString());
        }
    }
}
