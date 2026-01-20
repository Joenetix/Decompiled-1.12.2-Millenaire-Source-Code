package org.millenaire.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.network.NetworkHooks;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.core.Village;
import org.millenaire.gui.MenuTrade;
import org.millenaire.entities.jobs.Job;
import org.millenaire.common.village.Building;
import org.millenaire.common.world.MillWorldData;
import org.millenaire.common.village.VillagerRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.millenaire.pathing.PathingHandler;
import org.millenaire.common.pathing.atomicstryker.AS_PathEntity;
import org.millenaire.common.pathing.atomicstryker.AStarNode;
import org.millenaire.common.pathing.atomicstryker.AStarPathPlannerJPS;
import org.millenaire.common.pathing.atomicstryker.AStarStatic;
import org.millenaire.common.pathing.atomicstryker.IAStarPathedEntity;

/**
 * Millenaire Citizen - autonomous NPC with job and village
 * 
 * Inspired by MineColonies but implemented from scratch.
 */
public class Citizen extends PathfinderMob implements IAStarPathedEntity {
    private UUID villageId;
    private long villagerId;
    private Optional<Job> currentJob = Optional.empty();

    // Reference to home building (stub for now - will be connected to Building
    // later)
    public Building home;

    // Pathing
    public AStarPathPlannerJPS pathPlanner;
    public List<AStarNode> currentPath;

    public Citizen(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        if (!level.isClientSide) {
            this.pathPlanner = PathingHandler.createPlanner(level, this);
        }
    }

    @Override
    public void onFoundPath(List<AStarNode> result) {
        this.currentPath = result;
        if (result != null && !result.isEmpty()) {
            // Convert to vanilla Path
            // Using default config for translation
            try {
                AS_PathEntity path = AStarStatic.translateAStarPathtoPathEntity(level(), result,
                        PathingHandler.getDefaultConfig());
                this.getNavigation().moveTo(path, 1.0D);
            } catch (Exception e) {
                // Log and ignore
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNoPathAvailable() {
        // Handle failure (e.g., idle, retry, log)
        this.currentPath = null;
    }

    @Override
    protected void registerGoals() {
        // Basic survival goals
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Panic only if NOT raider? Raiders should fight.
        if (!isRaider) {
            this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        } else {
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        }

        // Job-specific goals will be added when job is assigned
        // this.goalSelector.addGoal(2, new WorkGoal(this));

        this.targetSelector.addGoal(2,
                new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Citizen.class, 10,
                        true, false,
                        (entity) -> this.isRaider() && entity instanceof Citizen && !((Citizen) entity).isRaider()));

        // Social goals
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 0.6D));
    }

    /**
     * Main citizen tick - handle job tasks
     */
    @Override
    public void aiStep() {
        super.aiStep();

        if (!level().isClientSide) {
            // Execute job tasks if assigned
            currentJob.ifPresent(Job::tick);
        }
    }

    /**
     * Assign a job to this citizen
     */
    public void assignJob(Job job) {
        // Remove old job goals if any
        currentJob.ifPresent(j -> j.removeGoals(this));

        // Assign new job
        currentJob = Optional.of(job);
        job.addGoals(this);
    }

    public void removeJob() {
        currentJob.ifPresent(j -> j.removeGoals(this));
        currentJob = Optional.empty();
    }

    public void initializeFromRecord(VillagerRecord vr) {
        this.villagerId = vr.villagerId;
        this.setCustomName(net.minecraft.network.chat.Component.literal(vr.firstName + " " + vr.familyName));
        // TODO: specific attributes based on type
    }

    /**
     * Get the village this citizen belongs to
     */
    public UUID getVillageId() {
        return villageId;
    }

    public long getVillagerId() {
        return villagerId;
    }

    /**
     * Set the village this citizen belongs to
     */
    public void setVillageId(UUID villageId) {
        this.villageId = villageId;
    }

    /**
     * Get current job
     */
    public Optional<Job> getJob() {
        return currentJob;
    }

    /**
     * Check if citizen has a job
     */
    public boolean hasJob() {
        return currentJob.isPresent();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (villageId != null) {
            tag.putUUID("VillageId", villageId);
        }
        if (villagerId != 0) {
            tag.putLong("VillagerId", villagerId);
        }
        currentJob.ifPresent(job -> tag.putString("Job", job.getType()));

        if (isRaider) {
            tag.putBoolean("IsRaider", true);
            if (raidTarget != null)
                tag.putLong("RaidTarget", raidTarget.asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("VillageId")) {
            villageId = tag.getUUID("VillageId");
        }
        if (tag.contains("VillagerId")) {
            villagerId = tag.getLong("VillagerId");
        }
        // Job will be reassigned by village on load

        if (tag.contains("IsRaider")) {
            isRaider = tag.getBoolean("IsRaider");
            if (tag.contains("RaidTarget")) {
                raidTarget = net.minecraft.core.BlockPos.of(tag.getLong("RaidTarget"));
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            // Check if we can trade (simple check for now)
            // In future: check if in shop/townhall or has trade capability
            if (this.isAlive()) { // && canTrade()
                NetworkHooks.openScreen((ServerPlayer) player,
                        new SimpleMenuProvider(
                                (id, inv, p) -> new MenuTrade(id, inv, this),
                                Component.literal("Trade with " + this.getName().getString())),
                        buf -> buf.writeInt(this.getId()));
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    public void setVillagerId(long id) {
        this.villagerId = id;
    }

    // Raider Logic
    private boolean isRaider = false;
    private net.minecraft.core.BlockPos raidTarget = null;

    public boolean isRaider() {
        return isRaider;
    }

    public void setRaider(boolean val) {
        this.isRaider = val;
    }

    public net.minecraft.core.BlockPos getRaidTarget() {
        return raidTarget;
    }

    public void setRaidTarget(net.minecraft.core.BlockPos pos) {
        this.raidTarget = pos;
    }

    /**
     * Get the Village object for this citizen.
     */
    public Village getVillage() {
        if (!level().isClientSide) {
            MillWorldData data = MillWorldData.get(level());
            VillagerRecord record = data.getVillagerRecord(villagerId);
            if (record != null && record.townHallPos != null) {
                Building b = data.getBuilding(record.townHallPos);
                if (b != null) {
                    return b.getVillage();
                }
            }
        }
        return null;
    }

    public VillagerRecord getVillagerRecord() {
        if (!level().isClientSide) {
            return MillWorldData.get(level()).getVillagerRecord(villagerId);
        }
        return null;
    }

    /**
     * Get the best axe from inventory.
     */
    public ItemStack getBestAxe() {
        VillagerRecord record = getVillagerRecord();
        if (record == null)
            return ItemStack.EMPTY;

        ItemStack bestAxe = ItemStack.EMPTY;
        int bestScore = -1;

        for (net.minecraft.world.item.Item item : record.inventory.keySet()) {
            if (item instanceof net.minecraft.world.item.AxeItem) {
                net.minecraft.world.item.AxeItem axe = (net.minecraft.world.item.AxeItem) item;
                int score = axe.getTier().getLevel();
                if (score > bestScore) {
                    bestScore = score;
                    bestAxe = new ItemStack(item);
                }
            }
        }
        return bestAxe;
    }

    public ItemStack getBestPickaxe() {
        VillagerRecord record = getVillagerRecord();
        if (record == null)
            return ItemStack.EMPTY;

        ItemStack bestTool = ItemStack.EMPTY;
        int bestScore = -1;

        for (net.minecraft.world.item.Item item : record.inventory.keySet()) {
            if (item instanceof net.minecraft.world.item.PickaxeItem) {
                net.minecraft.world.item.PickaxeItem tool = (net.minecraft.world.item.PickaxeItem) item;
                int score = tool.getTier().getLevel();
                if (score > bestScore) {
                    bestScore = score;
                    bestTool = new ItemStack(item);
                }
            }
        }
        return bestTool;
    }

    public ItemStack getBestShovel() {
        VillagerRecord record = getVillagerRecord();
        if (record == null)
            return ItemStack.EMPTY;

        ItemStack bestTool = ItemStack.EMPTY;
        int bestScore = -1;

        for (net.minecraft.world.item.Item item : record.inventory.keySet()) {
            if (item instanceof net.minecraft.world.item.ShovelItem) {
                net.minecraft.world.item.ShovelItem tool = (net.minecraft.world.item.ShovelItem) item;
                int score = tool.getTier().getLevel();
                if (score > bestScore) {
                    bestScore = score;
                    bestTool = new ItemStack(item);
                }
            }
        }
        return bestTool;
    }

    public ItemStack getBestHoe() {
        VillagerRecord record = getVillagerRecord();
        if (record == null)
            return ItemStack.EMPTY;

        ItemStack bestTool = ItemStack.EMPTY;
        int bestScore = -1;

        for (net.minecraft.world.item.Item item : record.inventory.keySet()) {
            if (item instanceof net.minecraft.world.item.HoeItem) {
                net.minecraft.world.item.HoeItem tool = (net.minecraft.world.item.HoeItem) item;
                int score = tool.getTier().getLevel();
                if (score > bestScore) {
                    bestScore = score;
                    bestTool = new ItemStack(item);
                }
            }
        }
        return bestTool;
    }

    /**
     * Count items in inventory matching the given InvItem.
     */
    public int countInv(InvItem item) {
        VillagerRecord record = getVillagerRecord();
        if (record == null)
            return 0;

        int count = 0;
        for (java.util.Map.Entry<net.minecraft.world.item.Item, Integer> entry : record.inventory.entrySet()) {
            if (item.matches(new ItemStack(entry.getKey()))) {
                count += entry.getValue();
            }
        }
        return count;
    }

    public void addToInventory(net.minecraft.world.item.Item item, int count) {
        VillagerRecord record = getVillagerRecord();
        if (record != null) {
            record.inventory.merge(item, count, Integer::sum);
        }
    }

    public boolean takeFromInventory(net.minecraft.world.item.Item item, int count) {
        VillagerRecord record = getVillagerRecord();
        if (record != null) {
            int current = record.inventory.getOrDefault(item, 0);
            if (current >= count) {
                if (current == count) {
                    record.inventory.remove(item);
                } else {
                    record.inventory.put(item, current - count);
                }
                return true;
            }
        }
        return false;
    }
}
