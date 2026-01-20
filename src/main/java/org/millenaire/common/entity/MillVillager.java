package org.millenaire.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillagerType;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.TradeGood;
import org.millenaire.common.network.StreamReadWrite;
import org.millenaire.common.pathing.atomicstryker.AS_PathEntity;
import org.millenaire.common.pathing.atomicstryker.AStarConfig;
import org.millenaire.common.pathing.atomicstryker.AStarNode;
import org.millenaire.common.pathing.atomicstryker.AStarPathPlannerJPS;
import org.millenaire.common.pathing.atomicstryker.IAStarPathedEntity;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;
import org.millenaire.common.world.MillWorldData;
import org.millenaire.common.village.VillagerRecord;
import org.millenaire.common.culture.VillageType;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MillVillager extends org.millenaire.entities.Citizen
        implements IEntityAdditionalSpawnData, IAStarPathedEntity {

    private static final UUID SPRINT_SPEED_BOOST_ID = UUID.fromString("B9766B59-8456-5632-BC1F-2EE2A276D836");
    private static final AttributeModifier SPRINT_SPEED_BOOST = new AttributeModifier(SPRINT_SPEED_BOOST_ID,
            "Sprint speed boost", 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);

    // ... (Fields from before, kept or inherited?)
    // Citizen has villageId, currentJob etc.
    // MillVillager has inventory, townHall etc.

    public MillVillager(EntityType<? extends org.millenaire.entities.Citizen> type, Level level) {
        super(type, level);
        this.mw = MillWorldData.get(level);
        this.inventory = new HashMap<>();
        this.client_lastupdated = level.getGameTime();
        if (!level.isClientSide) {
            boolean jpsPathing = true;
            this.pathPlannerJPS = new AStarPathPlannerJPS(level, this, jpsPathing);
        }
    }

    // START OF REPLACEMENT BLOCK
    /**
     * Get the village this villager belongs to (Town Hall).
     * Alias for getTownHall() for compatibility.
     */
    public Building getVillageOld() {
        return getTownHall();
    }

    /**
     * Get the village this villager belongs to.
     */
    @Override
    public org.millenaire.core.Village getVillage() {
        if (getTownHall() != null) {
            return getTownHall().getVillage();
        }
        // Fallback to Citizen implementation if needed, or super.getVillage()?
        // Citizen.getVillage() calls MillWorldData.
        // We prefer TownHall link if available.
        return super.getVillage();
    }

    public ItemStack getBestAxe() {
        return getBestAxeStack();
    }

    public ItemStack getBestPickaxe() {
        return getBestPickaxeStack();
    }

    public boolean isRaider() {
        return super.isRaider();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new org.millenaire.common.goal.RaidGoal(this));
    }

    public void addToInventory(Item item, int count) {
        if (item == null)
            return;
        InvItem key = InvItem.createInvItem(item, 0);
        inventory.put(key, inventory.getOrDefault(key, 0) + count);
    }

    public boolean takeFromInventory(Item item, int count) {
        if (item == null)
            return false;
        InvItem key = InvItem.createInvItem(item, 0);
        int current = inventory.getOrDefault(key, 0);
        if (current >= count) {
            inventory.put(key, current - count);
            return true;
        }
        return false;
    }

    public int getHireCost(Player player) {
        int baseCost = 64;
        if (vtype != null) {
            baseCost = 128;
        }
        return baseCost;
    }

    public boolean isHostileToPlayer(Player player) {
        return false;
    }

    // Tool Getters
    public ItemStack getBestHoeStack() {
        return ItemStack.EMPTY;
    }

    public ItemStack getBestAxeStack() {
        return ItemStack.EMPTY;
    }

    public ItemStack getBestPickaxeStack() {
        return ItemStack.EMPTY;
    }

    public ItemStack getBestShovelStack() {
        return ItemStack.EMPTY;
    }

    public ItemStack getWeapon() {
        return ItemStack.EMPTY;
    }

    private static final double DEFAULT_MOVE_SPEED = 0.5;
    public static final int ATTACK_RANGE_DEFENSIVE = 20;
    private static final String FREE_CLOTHES = "free";
    private static final String NATURAL = "natural";
    private static final int CONCEPTION_CHANCE = 2;
    private static final int VISITOR_NB_NIGHTS_BEFORE_LEAVING = 5;
    public static final int MALE = 1;
    public static final int FEMALE = 2;

    public static final ResourceLocation GENERIC_VILLAGER = new ResourceLocation("millenaire", "generic_villager");
    public static final ResourceLocation GENERIC_ASYMM_FEMALE = new ResourceLocation("millenaire",
            "generic_asimm_female");
    public static final ResourceLocation GENERIC_SYMM_FEMALE = new ResourceLocation("millenaire",
            "generic_simm_female");
    public static final ResourceLocation GENERIC_ZOMBIE = new ResourceLocation("millenaire", "generic_zombie");

    private static final AStarConfig JPS_CONFIG_DEFAULT = new AStarConfig(true, false, false, false, true);
    private static final AStarConfig JPS_CONFIG_NO_LEAVES = new AStarConfig(true, false, false, false, false);

    public VillagerType vtype;
    public int action = 0;
    public String goalKey = null;
    // private Goal.GoalInformation goalInformation = null; // Goal inner class not
    // generated?
    public Point pathDestPoint;
    private Building house = null;
    private Building townHall = null;
    public Point housePoint = null;
    public Point prevPoint = null;
    public Point townHallPoint = null;
    public boolean extraLog = false;
    public String firstName = "";
    public String familyName = "";
    public ItemStack heldItem = ItemStack.EMPTY;
    public ItemStack heldItemOffHand = ItemStack.EMPTY;
    public long timer = 0L;
    public long actionStart = 0L;
    public boolean allowRandomMoves = false;
    public boolean stopMoving = false;
    public int gender = 0;
    public boolean registered = false;
    public int longDistanceStuck;
    public boolean nightActionPerformed = false;
    public long speech_started = 0L;
    public HashMap<InvItem, Integer> inventory = new HashMap<>();
    public net.minecraft.world.level.block.Block previousBlock;
    public int previousBlockMeta;
    public long pathingTime;
    public long timeSinceLastPathingTimeDisplay;
    private long villagerId = -1L;
    public int nbPathsCalculated = 0;
    public int nbPathNoStart = 0;
    public int nbPathNoEnd = 0;
    public int nbPathAborted = 0;
    public int nbPathFailure = 0;
    public long goalStarted = 0L;
    public int constructionJobId = -1;
    public int heldItemCount = 0;
    public int heldItemId = -1;
    public int heldItemOffHandId = -1;
    public String speech_key = null;
    public int speech_variant = 0;
    public String dialogueKey = null;
    public int dialogueRole = 0;
    public long dialogueStart = 0L;
    public char dialogueColour = 'f';
    public boolean dialogueChat = false;
    public String dialogueTargetFirstName = null;
    public String dialogueTargetLastName = null;
    private Point doorToClose = null;
    public int visitorNbNights = 0;
    public int foreignMerchantStallId = -1;
    public boolean lastAttackByPlayer = false;
    public HashMap<Goal, Long> lastGoalTime = new HashMap<>();
    public String hiredBy = null;
    public boolean aggressiveStance = false;
    public long hiredUntil = 0L;
    public boolean isUsingBow;
    public boolean isUsingHandToHand;
    public boolean isRaider = false;
    public AStarPathPlannerJPS pathPlannerJPS;
    public AS_PathEntity pathEntity;
    public int updateCounter = 0;
    public long client_lastupdated;
    public MillWorldData mw;
    private boolean pathFailedSincelastTick = false;
    private List<AStarNode> pathCalculatedSinceLastTick = null;
    private int localStuck = 0;
    private final ResourceLocation[] clothTexture = new ResourceLocation[2];
    private String clothName = null;
    public boolean shouldLieDown = false;
    public LinkedHashMap<TradeGood, Integer> merchantSells = new LinkedHashMap<>();
    public ResourceLocation texture = null;
    private int attackTime;
    public boolean isDeadOnServer = false;
    public boolean travelBookMockVillager = false;

    private VillagerRecord villagerRecord;

    // Constructor used by EntityType registration usually has (EntityType, Level)
    // signature in 1.20
    // The simplified constructor in 1.12 was (World)

    public static MillVillager createMockVillager(VillagerRecord villagerRecord, Level world) {
        // EntityType lookup usually requires registry name.
        // For now assuming generic type or using a specific registered type if
        // available.
        // In 1.12 it used EntityList.createEntityByName.
        // In 1.20 we need the EntityType<?>.
        // We'll stub with a basic type or iterate registries if needed.
        // Ideally we should have EntityType<MillVillager> registered.

        // For this port, we'll assume we are creating a generic MillVillager and
        // setting properties.
        // Real implementation needs registered EntityTypes for each name if they
        // differ.
        // If strict parity is needed, we need to register entities matching
        // "millenaire.GenericVillager" etc.

        // Stub: returning null or throwing if types not registered.
        // But to proceed, we'll try to find a way to create instance.
        // Since we are inside MillVillager, we can't easily "new" it if we need
        // EntityType.
        // We'll assume a generic type for now: MillVillager.TYPE (need to define it)

        // TODO: Fix EntityType retrieval
        // MillVillager villager = new MillVillager(MillEntityTypes.MILL_VILLAGER.get(),
        // world);

        return null; // Stub until EntityType registration is handled
    }

    public static MillVillager createVillager(VillagerRecord villagerRecord, Level world, Point spawnPos,
            boolean respawn) {
        if (world.isClientSide || !(world instanceof net.minecraft.server.level.ServerLevel)) {
            MillLog.printException("Tried creating a villager in client world: " + world, new Exception());
            return null;
        } else if (villagerRecord == null) {
            MillLog.error(null, "Tried creating villager from a null record");
            return null;
        } else if (villagerRecord.getType() == null) {
            MillLog.error(null, "Tried creating villager of null type: " + villagerRecord.type);
            return null;
        } else {
            MillVillager villager = new MillVillager(org.millenaire.core.MillEntities.CITIZEN.get(), world);
            villager.setPos(villagerRecord.housePos.getX(), villagerRecord.housePos.getY(),
                    villagerRecord.housePos.getZ());
            villager.setVillagerId(villagerRecord.villagerId);
            // Initialize from record
            // We need to resolve the type from the culture and type key
            // Ideally record has it or we can look it up via the village if we had a
            // reference
            // But here we only have the record.
            // The record has 'type' (String).
            // We need the Culture to fully hydrate the type.
            // For now, let's just set the basic IDs and rely on onInitialSpawn or NBT load
            // to fix the rest if possible.
            // actually, we should try to set the texture if available in record
            if (villagerRecord.texture != null) {
                villager.texture = villagerRecord.texture;
            }
            // Set type key for NBT load
            if (villagerRecord.type != null) {
                // accessing private/protected fields might be needed or using a setter
                // We added setTypeAndTexture but we don't have Culture here yet.
                // We can't easily get the culture without the Village object.
                // However, the record should potentially store it or we find it later.
            }

            return villager;
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        StreamReadWrite.writeNullableVillagerRecord(this.getRecord(), buffer);
        buffer.writeBoolean(this.texture != null);
        if (this.texture != null) {
            buffer.writeUtf(this.texture.toString());
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        VillagerRecord record = StreamReadWrite.readNullableVillagerRecord(this.mw, buffer);
        if (record != null) {
            // Apply record logic if needed
        }
        if (buffer.readBoolean()) {
            this.texture = new ResourceLocation(buffer.readUtf());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.texture != null) {
            tag.putString("texture", this.texture.toString());
        }
        if (this.vtype != null) {
            tag.putString("type", this.vtype.key);
        }
        if (this.firstName != null) {
            tag.putString("firstName", this.firstName);
        }
        if (this.familyName != null) {
            tag.putString("familyName", this.familyName);
        }
        tag.putInt("gender", this.gender);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("texture")) {
            this.texture = new ResourceLocation(tag.getString("texture"));
        }
        if (tag.contains("type")) {
            String typeKey = tag.getString("type");
            // We need culture to look up type. Try via Town Hall or iterate cultures
            if (this.townHall != null && this.townHall.culture != null) {
                this.vtype = this.townHall.culture.getVillagerType(typeKey);
            } else {
                // Fallback: search all cultures
                for (Culture c : Culture.ListCultures) {
                    if (c.getVillagerType(typeKey) != null) {
                        this.vtype = c.getVillagerType(typeKey);
                        break;
                    }
                }
            }
        }
        if (tag.contains("firstName")) {
            this.firstName = tag.getString("firstName");
        }
        if (tag.contains("familyName")) {
            this.familyName = tag.getString("familyName");
        }
        if (tag.contains("gender")) {
            this.gender = tag.getInt("gender");
        }
    }

    public void setTypeAndTexture(String typeKey, Culture culture) {
        if (culture == null)
            return;
        this.vtype = culture.getVillagerType(typeKey);
        if (this.vtype != null) {
            if (this.texture == null) {
                this.texture = this.vtype.getNewTexture();
            }
            this.gender = this.vtype.gender;
        }
    }

    // Interface methods for IAStarPathedEntity
    @Override
    public void onFoundPath(List<AStarNode> path) {
        this.pathCalculatedSinceLastTick = path;
    }

    @Override
    public void onNoPathAvailable() {
        this.pathFailedSincelastTick = true;
    }

    // @Override
    public void onPathReached() {
        // Handle path reaching logic
        this.pathDestPoint = null;
        // Logic to notify goals etc
    }

    // @Override
    public void onPathFailed() {
        this.pathFailedSincelastTick = true;
    }

    // Inventory Management
    public void addToInv(net.minecraft.world.item.Item item, int meta, int nb) {
        InvItem key = InvItem.createInvItem(item, meta);
        int current = this.inventory.getOrDefault(key, 0);
        this.inventory.put(key, current + nb);
        if (this.inventory.get(key) <= 0) {
            this.inventory.remove(key);
        }
    }

    public int countInv(Item item, int meta) {
        InvItem key = InvItem.createInvItem(item, meta);
        return this.inventory.getOrDefault(key, 0);
    }

    public int countInv(InvItem key) {
        return this.inventory.getOrDefault(key, 0);
    }

    public void updateVillagerRecord() {
        VillagerRecord record = this.getRecord();
        if (record != null) {
            record.inventory.clear();
            for (Map.Entry<InvItem, Integer> entry : this.inventory.entrySet()) {
                record.inventory.put(entry.getKey().getItem(), entry.getValue());
            }
            // Update other fields
            if (this.housePoint != null)
                record.housePos = new BlockPos(this.housePoint.getiX(), this.housePoint.getiY(),
                        this.housePoint.getiZ());
            if (this.townHallPoint != null)
                record.townHallPos = new BlockPos(this.townHallPoint.getiX(), this.townHallPoint.getiY(),
                        this.townHallPoint.getiZ());
            if (this.prevPoint != null)
                record.originalVillagePos = new BlockPos(this.prevPoint.getiX(), this.prevPoint.getiY(),
                        this.prevPoint.getiZ());
            record.villagerId = this.villagerId;
            record.type = this.vtype != null ? this.vtype.key : null;
            record.firstName = this.firstName;
            record.familyName = this.familyName;
            record.gender = this.gender;
            record.texture = this.texture;
        }
    }

    // ... Stubbing required abstract methods from IAStarPathedEntity if any ...

    public VillagerRecord getRecord() {
        if (this.villagerRecord == null) {
            this.villagerRecord = new VillagerRecord(this.mw);
            this.villagerRecord.villagerId = this.villagerId;
        }
        return this.villagerRecord;
    }

    public void setVillagerId(long id) {
        this.villagerId = id;
        if (this.villagerRecord != null) {
            this.villagerRecord.villagerId = id;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.updateVillagerRecord();
            this.manageGoals();
            if (this.pathPlannerJPS != null) {
                // Update pathing logic if needed
            }
        }
    }

    // --- Getter Methods ---

    public long getVillagerId() {
        return this.villagerId;
    }

    public Building getHouse() {
        return this.house;
    }

    public void setHouse(Building house) {
        this.house = house;
        if (house != null) {
            this.housePoint = new Point(house.getPos());
        }
    }

    public Building getTownHall() {
        return this.townHall;
    }

    public void setTownHall(Building townHall) {
        this.townHall = townHall;
        if (townHall != null) {
            this.townHallPoint = new Point(townHall.getPos());
        }
    }

    public Culture getCulture() {
        if (vtype != null && vtype.culture != null) {
            return vtype.culture;
        }
        if (house != null && house.culture != null) {
            return house.culture;
        }
        if (townHall != null && townHall.culture != null) {
            return townHall.culture;
        }
        return null;
    }

    // --- Goal-related Methods ---

    private Goal.GoalInformation goalInformation = null;
    private Point goalDestPoint = null;
    private Entity goalDestEntity = null;
    private Building goalBuildingDest = null;
    private List<Goal> goals = new ArrayList<>();

    /**
     * Get the villager's current position as a Point.
     */
    public Point getPos() {
        return new Point(this.getX(), this.getY(), this.getZ());
    }

    /**
     * Set the goal information (destination, building, target entity).
     */
    public void setGoalInformation(Goal.GoalInformation info) {
        this.goalInformation = info;
        if (info != null) {
            this.goalDestPoint = info.getDest();
            // Lookup building by position if needed
            if (info.getDestBuildingPos() != null && this.townHall != null) {
                this.goalBuildingDest = this.townHall.getBuildingAtPos(info.getDestBuildingPos());
            }
            this.goalDestEntity = info.getTargetEnt();
            // Store building position for later lookup
            // Building lookup done via townHall.getBuildingAtPoint when needed
        }
    }

    /**
     * Get the current goal destination point.
     */
    public Point getGoalDestPoint() {
        return this.goalDestPoint;
    }

    /**
     * Get the current goal destination entity.
     */
    public Entity getGoalDestEntity() {
        return this.goalDestEntity;
    }

    /**
     * Get the current goal building destination.
     */
    public Building getGoalBuildingDest() {
        return this.goalBuildingDest;
    }

    /**
     * Get the path destination point.
     */
    public Point getPathDestPoint() {
        return this.pathDestPoint;
    }

    /**
     * Get the villager's pathing config.
     */
    public AStarConfig getVillagerPathingConfig() {
        return JPS_CONFIG_DEFAULT;
    }

    /**
     * Get all goals this villager can perform.
     */
    /**
     * Get all goals this villager can perform.
     */
    public List<Goal> getGoals() {
        if (goals.isEmpty() && vtype != null) {
            for (String key : vtype.goals) {
                Goal g = Goal.goals.get(key);
                if (g != null) {
                    goals.add(g);
                } else {
                    MillLog.error(this, "VillagerType " + vtype.key + " has unknown goal: " + key);
                }
            }
        }
        return goals;
    }

    /**
     * Main AI Loop for Millénaire Goals.
     * Selects and executes goals based on priority and availability.
     */
    private void manageGoals() {
        if (this.level().isClientSide)
            return;

        // 1. If we have a current goal, execute it
        if (this.goalKey != null) {
            Goal currentGoal = Goal.goals.get(this.goalKey);

            if (currentGoal == null) {
                this.goalKey = null;
                return;
            }

            try {
                // Determine if we continue the goal
                boolean keepGoing = currentGoal.performAction(this);

                if (!keepGoing) {
                    // Goal complete
                    currentGoal.onComplete(this);
                    this.goalKey = null;
                    this.goalInformation = null;
                }
            } catch (Exception e) {
                MillLog.printException("Exception in manageGoals for goal " + this.goalKey, e);
                this.goalKey = null;
            }
        }

        // 2. If no goal (or just finished), find a new one
        if (this.goalKey == null) {
            Goal bestGoal = null;
            int bestPriority = -1;

            for (Goal g : getGoals()) {
                try {
                    // Check conditions
                    if (!g.isPossible(this))
                        continue;

                    // Check time restrictions (min/max hour) - checked in isPossible usually, but
                    // verify
                    // Check cooldowns?

                    int priority = g.priority(this);
                    if (priority > bestPriority) {
                        bestPriority = priority;
                        bestGoal = g;
                    }
                } catch (Exception e) {
                    MillLog.printException("Exception checking capability of goal " + g.key, e);
                }
            }

            if (bestGoal != null) {
                this.goalKey = bestGoal.key;
                this.goalStarted = this.level().getGameTime();
                try {
                    bestGoal.onAccept(this);
                } catch (Exception e) {
                    MillLog.printException("Exception onAccept for goal " + bestGoal.key, e);
                    this.goalKey = null;
                }
            }
        }
    }

    /**
     * Get the villager's level (world).
     */
    public Level getLevel() {
        return this.level();
    }

}
