package org.millenaire.common.village;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import org.millenaire.core.Village;
import org.millenaire.entities.Citizen;
import org.millenaire.core.MillEntities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.network.chat.Component;

public class RaidManager {
    private final Village village;
    public boolean active = false;
    private List<UUID> raiders = new ArrayList<>();
    private long raidStartTime;
    private static final int RAID_DURATION = 12000; // 10 minutes

    public RaidManager(Village village) {
        this.village = village;
    }

    public void tick() {
        if (!active)
            return;

        ServerLevel level = village.getLevel();
        if (level.getGameTime() - raidStartTime > RAID_DURATION) {
            endRaid("time_expired");
            return;
        }

        // Check raider count
        int aliveCount = 0;
        int foundCount = 0;

        for (UUID id : raiders) {
            Entity e = level.getEntity(id);
            if (e != null) {
                foundCount++;
                if (e.isAlive()) {
                    aliveCount++;
                }
            }
        }

        // Only end if we found at least one entity and NONE are alive
        // Or if we found 0 entities but had some (chunk unload?) - don't end yet maybe?
        // Simple logic: if < 50% alive?
        if (foundCount > 0 && aliveCount == 0) {
            endRaid("defenders_won");
        }
    }

    public void triggerRaid() {
        if (active)
            return;
        active = true;
        raidStartTime = village.getLevel().getGameTime();
        spawnRaiders();
        village.setUnderRaid(true);
    }

    private void spawnRaiders() {
        raiders.clear();
        int count = 3 + village.getVillageLevel() * 2;
        ServerLevel level = village.getLevel();
        BlockPos center = village.getCenter();

        // Find spawn spot: 40 blocks away
        double angle = level.random.nextDouble() * Math.PI * 2;
        int dist = 40;
        int x = center.getX() + (int) (Math.cos(angle) * dist);
        int z = center.getZ() + (int) (Math.sin(angle) * dist);

        BlockPos spawnPos = new BlockPos(x, 0, z);
        spawnPos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                spawnPos);

        for (int i = 0; i < count; i++) {
            Citizen raider = MillEntities.CITIZEN.get().create(level);
            if (raider != null) {
                raider.setPos(spawnPos.getX() + (level.random.nextDouble() - 0.5) * 5, spawnPos.getY() + 1,
                        spawnPos.getZ() + (level.random.nextDouble() - 0.5) * 5);

                raider.setCustomName(Component.literal("Raider"));
                raider.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));

                // Set Raider AI Goal
                raider.setRaider(true);
                raider.setRaidTarget(center);

                level.addFreshEntity(raider);
                raiders.add(raider.getUUID());
            }
        }
    }

    public void endRaid(String reason) {
        active = false;
        village.setUnderRaid(false);
    }

    public void writeToNBT(net.minecraft.nbt.CompoundTag tag) {
        tag.putBoolean("active", active);
        tag.putLong("startTime", raidStartTime);
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (UUID id : raiders) {
            net.minecraft.nbt.CompoundTag t = new net.minecraft.nbt.CompoundTag();
            t.putUUID("id", id);
            list.add(t);
        }
        tag.put("raiders", list);
    }

    public void readFromNBT(net.minecraft.nbt.CompoundTag tag) {
        active = tag.getBoolean("active");
        raidStartTime = tag.getLong("startTime");
        village.setUnderRaid(active);

        raiders.clear();
        if (tag.contains("raiders")) {
            net.minecraft.nbt.ListTag list = tag.getList("raiders", 10);
            for (int i = 0; i < list.size(); i++) {
                raiders.add(list.getCompound(i).getUUID("id"));
            }
        }
    }
}
