package org.millenaire.common.village;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.millenaire.core.MillBlocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BuildingResManager {

    public CopyOnWriteArrayList<BlockPos> brickspot = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> chests = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> fishingspots = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> sugarcanesoils = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> healingspots = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> furnaces = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> firepits = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> brewingStands = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> signs = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> banners = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> cultureBanners = new CopyOnWriteArrayList<>();

    // Mapping ResourceLocation types to lists of points
    public CopyOnWriteArrayList<CopyOnWriteArrayList<BlockPos>> sources = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockState> sourceTypes = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<CopyOnWriteArrayList<BlockPos>> spawns = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<ResourceLocation> spawnTypes = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<CopyOnWriteArrayList<BlockPos>> mobSpawners = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<ResourceLocation> mobSpawnerTypes = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<CopyOnWriteArrayList<BlockPos>> soils = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<ResourceLocation> soilTypes = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<BlockPos> stalls = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> woodspawn = new CopyOnWriteArrayList<>();
    public ConcurrentHashMap<BlockPos, String> woodspawnTypes = new ConcurrentHashMap<>();

    public CopyOnWriteArrayList<BlockPos> netherwartsoils = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> silkwormblock = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<BlockPos> snailsoilblock = new CopyOnWriteArrayList<>();

    private BlockPos sleepingPos = null;
    private BlockPos sellingPos = null;
    private BlockPos craftingPos = null;
    private BlockPos defendingPos = null;
    private BlockPos shelterPos = null;
    private BlockPos pathStartPos = null;
    private BlockPos leasurePos = null;

    private final Building building;

    public BuildingResManager(Building b) {
        this.building = b;
    }

    public void addSoilPoint(ResourceLocation type, BlockPos p) {
        if (!this.soilTypes.contains(type)) {
            CopyOnWriteArrayList<BlockPos> list = new CopyOnWriteArrayList<>();
            list.add(p);
            this.soils.add(list);
            this.soilTypes.add(type);
        } else {
            int idx = this.soilTypes.indexOf(type);
            if (!this.soils.get(idx).contains(p)) {
                this.soils.get(idx).add(p);
            }
        }
    }

    public HashMap<Item, Integer> getChestsContent() {
        HashMap<Item, Integer> contents = new HashMap<>();
        Level level = building.world;
        if (level == null)
            return contents;

        for (BlockPos p : this.chests) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof BaseContainerBlockEntity) {
                BaseContainerBlockEntity chest = (BaseContainerBlockEntity) be;
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    ItemStack stack = chest.getItem(i);
                    if (!stack.isEmpty()) {
                        Item item = stack.getItem();
                        contents.put(item, contents.getOrDefault(item, 0) + stack.getCount());
                    }
                }
            }
        }
        return contents;
    }

    public BlockPos getCocoaHarvestLocation() {
        int idx = this.soilTypes.indexOf(ForgeRegistries.ITEMS.getKey(Items.COCOA_BEANS));
        if (idx == -1)
            return null;

        for (BlockPos p : this.soils.get(idx)) {
            BlockState state = building.world.getBlockState(p);
            if (state.getBlock() == Blocks.COCOA && state.getValue(CocoaBlock.AGE) >= 2) {
                return p;
            }
        }
        return null;
    }

    public BlockPos getCocoaPlantingLocation() {
        int idx = this.soilTypes.indexOf(ForgeRegistries.ITEMS.getKey(Items.COCOA_BEANS));
        if (idx == -1)
            return null;

        for (BlockPos p : this.soils.get(idx)) {
            if (building.world.isEmptyBlock(p)) {
                // Check neighbors for Jungle Wood
                if (isJungleWood(p.north()) || isJungleWood(p.east()) || isJungleWood(p.south())
                        || isJungleWood(p.west())) {
                    return p;
                }
            }
        }
        return null;
    }

    public int countInv(Item item) {
        return getChestsContent().getOrDefault(item, 0);
    }

    public int addToInventory(Item item, int count) {
        Level level = building.world;
        if (level == null)
            return 0;

        int remaining = count;

        for (BlockPos p : this.chests) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof BaseContainerBlockEntity) {
                BaseContainerBlockEntity chest = (BaseContainerBlockEntity) be;

                // First pass: add to existing stacks
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    ItemStack stack = chest.getItem(i);
                    if (!stack.isEmpty() && stack.getItem() == item && stack.getCount() < stack.getMaxStackSize()) {
                        int space = stack.getMaxStackSize() - stack.getCount();
                        int toAdd = Math.min(space, remaining);
                        stack.grow(toAdd);
                        remaining -= toAdd;
                        if (remaining <= 0)
                            break;
                    }
                }

                // Second pass: add to empty slots
                if (remaining > 0) {
                    for (int i = 0; i < chest.getContainerSize(); i++) {
                        if (chest.getItem(i).isEmpty()) {
                            int toAdd = Math.min(64, remaining); // Assume 64 max stack for new items
                            chest.setItem(i, new ItemStack(item, toAdd));
                            remaining -= toAdd;
                            if (remaining <= 0)
                                break;
                        }
                    }
                }

                if (remaining <= 0)
                    break;
            }
        }
        return count - remaining;
    }

    public boolean takeFromInventory(Item item, int count) {
        if (countInv(item) < count)
            return false;

        Level level = building.world;
        if (level == null)
            return false;

        int remaining = count;

        for (BlockPos p : this.chests) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof BaseContainerBlockEntity) {
                BaseContainerBlockEntity chest = (BaseContainerBlockEntity) be;
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    ItemStack stack = chest.getItem(i);
                    if (!stack.isEmpty() && stack.getItem() == item) {
                        int toTake = Math.min(stack.getCount(), remaining);
                        stack.shrink(toTake);
                        remaining -= toTake;
                        if (stack.isEmpty()) {
                            chest.setItem(i, ItemStack.EMPTY);
                        }
                        if (remaining <= 0)
                            break;
                    }
                }
                if (remaining <= 0)
                    break;
            }
        }
        return true;
    }

    private boolean isJungleWood(BlockPos p) {
        Block b = building.world.getBlockState(p).getBlock();
        return b == Blocks.JUNGLE_LOG || b == Blocks.JUNGLE_WOOD;
    }

    // TODO: Implement other accessors similar to original

    public BlockPos getSleepingPos() {
        return sleepingPos;
    }

    public void setSleepingPos(BlockPos pos) {
        this.sleepingPos = pos;
    }

    public BlockPos getSellingPos() {
        return sellingPos != null ? sellingPos : sleepingPos;
    }

    public void setSellingPos(BlockPos pos) {
        this.sellingPos = pos;
    }

    public BlockPos getCraftingPos() {
        return craftingPos;
    }

    public void setCraftingPos(BlockPos pos) {
        this.craftingPos = pos;
    }

    public BlockPos getDefendingPos() {
        return defendingPos;
    }

    public void setDefendingPos(BlockPos pos) {
        this.defendingPos = pos;
    }

    public BlockPos getShelterPos() {
        return shelterPos;
    }

    public void setShelterPos(BlockPos pos) {
        this.shelterPos = pos;
    }

    public BlockPos getPathStartPos() {
        return pathStartPos;
    }

    public void setPathStartPos(BlockPos pos) {
        this.pathStartPos = pos;
    }

    public BlockPos getLeasurePos() {
        return leasurePos;
    }

    public void setLeasurePos(BlockPos pos) {
        this.leasurePos = pos;
    }

    // Mob Spawner Methods
    public void addMobSpawnerPoint(ResourceLocation type, BlockPos p) {
        if (!this.mobSpawnerTypes.contains(type)) {
            CopyOnWriteArrayList<BlockPos> list = new CopyOnWriteArrayList<>();
            list.add(p);
            this.mobSpawners.add(list);
            this.mobSpawnerTypes.add(type);
        } else {
            int idx = this.mobSpawnerTypes.indexOf(type);
            if (!this.mobSpawners.get(idx).contains(p)) {
                this.mobSpawners.get(idx).add(p);
            }
        }
    }

    // Source Point Methods
    public void addSourcePoint(BlockState blockState, BlockPos p) {
        if (!blockState.isAir()) {
            if (!this.sourceTypes.contains(blockState)) {
                CopyOnWriteArrayList<BlockPos> list = new CopyOnWriteArrayList<>();
                list.add(p);
                this.sources.add(list);
                this.sourceTypes.add(blockState);
            } else {
                int idx = this.sourceTypes.indexOf(blockState);
                if (!this.sources.get(idx).contains(p)) {
                    this.sources.get(idx).add(p);
                }
            }
        }
    }

    // Spawn Point Methods
    public void addSpawnPoint(ResourceLocation type, BlockPos p) {
        if (!this.spawnTypes.contains(type)) {
            CopyOnWriteArrayList<BlockPos> list = new CopyOnWriteArrayList<>();
            list.add(p);
            this.spawns.add(list);
            this.spawnTypes.add(type);
        } else {
            int idx = this.spawnTypes.indexOf(type);
            if (!this.spawns.get(idx).contains(p)) {
                this.spawns.get(idx).add(p);
            }
        }
    }

    // Brick Spot Methods
    public BlockPos getEmptyBrickLocation() {
        Level level = building.world;
        if (level == null)
            return null;

        for (BlockPos p : this.brickspot) {
            if (level.isEmptyBlock(p)) {
                return p;
            }
        }
        return null;
    }

    public BlockPos getFullBrickLocation() {
        Level level = building.world;
        if (level == null)
            return null;

        for (BlockPos p : this.brickspot) {
            BlockState state = level.getBlockState(p);
            // TODO: Replace with MillBlocks.DRIED_BRICK when registered
            if (state.getBlock() == Blocks.BRICKS) {
                return p;
            }
        }
        return null;
    }

    public int getNbEmptyBrickLocation() {
        Level level = building.world;
        if (level == null)
            return 0;

        int count = 0;
        for (BlockPos p : this.brickspot) {
            if (level.isEmptyBlock(p)) {
                count++;
            }
        }
        return count;
    }

    public int getNbFullBrickLocation() {
        Level level = building.world;
        if (level == null)
            return 0;

        int count = 0;
        for (BlockPos p : this.brickspot) {
            BlockState state = level.getBlockState(p);
            // TODO: Replace with MillBlocks.DRIED_BRICK when registered
            if (state.getBlock() == Blocks.BRICKS) {
                count++;
            }
        }
        return count;
    }

    // Nether Wart Methods
    public BlockPos getNetherWartsHarvestLocation() {
        Level level = building.world;
        if (level == null)
            return null;

        for (BlockPos p : this.netherwartsoils) {
            BlockState state = level.getBlockState(p);
            if (state.getBlock() == Blocks.NETHER_WART && state.getValue(NetherWartBlock.AGE) >= 3) {
                return p;
            }
        }
        return null;
    }

    public BlockPos getNetherWartsPlantingLocation() {
        Level level = building.world;
        if (level == null)
            return null;

        for (BlockPos p : this.netherwartsoils) {
            if (level.isEmptyBlock(p)) {
                BlockState below = level.getBlockState(p.below());
                if (below.getBlock() == Blocks.SOUL_SAND) {
                    return p;
                }
            }
        }
        return null;
    }

    public int getNbNetherWartHarvestLocation() {
        Level level = building.world;
        if (level == null)
            return 0;

        int count = 0;
        for (BlockPos p : this.netherwartsoils) {
            BlockState state = level.getBlockState(p);
            if (state.getBlock() == Blocks.NETHER_WART && state.getValue(NetherWartBlock.AGE) >= 3) {
                count++;
            }
        }
        return count;
    }

    public int getNbNetherWartPlantingLocation() {
        Level level = building.world;
        if (level == null)
            return 0;

        int count = 0;
        for (BlockPos p : this.netherwartsoils) {
            if (level.isEmptyBlock(p)) {
                BlockState below = level.getBlockState(p.below());
                if (below.getBlock() == Blocks.SOUL_SAND) {
                    count++;
                }
            }
        }
        return count;
    }

    // Sugar Cane Methods
    public int getNbSugarCaneHarvestLocation() {
        Level level = building.world;
        if (level == null)
            return 0;

        int count = 0;
        for (BlockPos p : this.sugarcanesoils) {
            BlockState state = level.getBlockState(p);
            if (state.getBlock() == Blocks.SUGAR_CANE) {
                // Check if there's more sugar cane above
                if (level.getBlockState(p.above()).getBlock() == Blocks.SUGAR_CANE) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getNbSugarCanePlantingLocation() {
        Level level = building.world;
        if (level == null)
            return 0;

        int count = 0;
        for (BlockPos p : this.sugarcanesoils) {
            if (level.isEmptyBlock(p)) {
                count++;
            }
        }
        return count;
    }

    public BlockPos getSugarCaneHarvestLocation() {
        Level level = building.world;
        if (level == null)
            return null;

        for (BlockPos p : this.sugarcanesoils) {
            BlockState state = level.getBlockState(p);
            if (state.getBlock() == Blocks.SUGAR_CANE) {
                if (level.getBlockState(p.above()).getBlock() == Blocks.SUGAR_CANE) {
                    return p.above();
                }
            }
        }
        return null;
    }

    public BlockPos getSugarCanePlantingLocation() {
        Level level = building.world;
        if (level == null)
            return null;

        for (BlockPos p : this.sugarcanesoils) {
            if (level.isEmptyBlock(p)) {
                return p;
            }
        }
        return null;
    }

    // Silkworm Methods
    public int getNbSilkWormHarvestLocation() {
        Level level = building.world;
        if (level == null)
            return 0;

        int count = 0;
        for (BlockPos p : this.silkwormblock) {
            BlockState state = level.getBlockState(p);
            // TODO: Replace with MillBlocks.SILK_WORM_BLOCK when registered
            if (state.getBlock() == Blocks.MOSSY_COBBLESTONE) {
                count++;
            }
        }
        return count;
    }

    // Snail Soil Methods
    public int getNbSnailSoilHarvestLocation() {
        Level level = building.world;
        if (level == null)
            return 0;

        int count = 0;
        for (BlockPos p : this.snailsoilblock) {
            if (!level.isEmptyBlock(p)) {
                count++;
            }
        }
        return count;
    }

    public void readFromNBT(CompoundTag tag) {
        // Basic pos reading
        if (tag.contains("sleepingPos"))
            sleepingPos = BlockPos.of(tag.getLong("sleepingPos"));
        if (tag.contains("sellingPos"))
            sellingPos = BlockPos.of(tag.getLong("sellingPos"));

        // Lists reading logic (Simplified for brevity, can expand later)
        readBlockPosList(tag, "chests", chests);
        readBlockPosList(tag, "furnaces", furnaces);
        // ... others
    }

    public void writeToNBT(CompoundTag tag) {
        if (sleepingPos != null)
            tag.putLong("sleepingPos", sleepingPos.asLong());
        if (sellingPos != null)
            tag.putLong("sellingPos", sellingPos.asLong());

        writeBlockPosList(tag, "chests", chests);
        writeBlockPosList(tag, "furnaces", furnaces);
    }

    private void readBlockPosList(CompoundTag tag, String key, List<BlockPos> list) {
        if (tag.contains(key)) {
            ListTag l = tag.getList(key, 10); // 10 = Compound
            for (int i = 0; i < l.size(); i++) {
                CompoundTag t = l.getCompound(i);
                if (t.contains("pos"))
                    list.add(BlockPos.of(t.getLong("pos")));
                else if (t.contains("x")) { // Legacy support? Original used x,y,z in Point serialization usually
                    list.add(new BlockPos(t.getInt("x"), t.getInt("y"), t.getInt("z")));
                }
            }
        }
    }

    private void writeBlockPosList(CompoundTag tag, String key, List<BlockPos> list) {
        ListTag l = new ListTag();
        for (BlockPos p : list) {
            CompoundTag t = new CompoundTag();
            t.putLong("pos", p.asLong());
            l.add(t);
        }
        tag.put(key, l);
    }

    /**
     * Take goods from the building's inventory using InvItem.
     */
    public boolean takeGoods(org.millenaire.common.item.InvItem item, int count) {
        if (item == null || item.getItem() == null)
            return false;
        return takeFromInventory(item.getItem(), count);
    }

    /**
     * Store goods in the building's inventory using InvItem.
     */
    public int storeGoods(org.millenaire.common.item.InvItem item, int count) {
        if (item == null || item.getItem() == null)
            return 0;
        return addToInventory(item.getItem(), count);
    }

    /**
     * Invalidate cached inventory data.
     */
    public void invalidateCache() {
        // Currently using live inventory checks, so no cache to invalidate
        // This would be used if we cached getChestsContent results
    }

    // ============== Point-based overloads for BuildingLocation compatibility
    // ==============

    /**
     * Convert Point to BlockPos helper.
     */
    private static BlockPos pointToBlockPos(org.millenaire.common.utilities.Point p) {
        if (p == null)
            return null;
        return new BlockPos(p.getiX(), p.getiY(), p.getiZ());
    }

    /**
     * Set sleeping position from Point.
     */
    public void setSleepingPos(org.millenaire.common.utilities.Point pos) {
        this.sleepingPos = pointToBlockPos(pos);
    }

    /**
     * Set selling position from Point.
     */
    public void setSellingPos(org.millenaire.common.utilities.Point pos) {
        this.sellingPos = pointToBlockPos(pos);
    }

    /**
     * Set crafting position from Point.
     */
    public void setCraftingPos(org.millenaire.common.utilities.Point pos) {
        this.craftingPos = pointToBlockPos(pos);
    }

    /**
     * Set defending position from Point.
     */
    public void setDefendingPos(org.millenaire.common.utilities.Point pos) {
        this.defendingPos = pointToBlockPos(pos);
    }

    /**
     * Set shelter position from Point.
     */
    public void setShelterPos(org.millenaire.common.utilities.Point pos) {
        this.shelterPos = pointToBlockPos(pos);
    }

    /**
     * Set path start position from Point.
     */
    public void setPathStartPos(org.millenaire.common.utilities.Point pos) {
        this.pathStartPos = pointToBlockPos(pos);
    }

    /**
     * Set leisure position from Point.
     */
    public void setLeasurePos(org.millenaire.common.utilities.Point pos) {
        this.leasurePos = pointToBlockPos(pos);
    }

    /**
     * Add a chest position from Point.
     */
    public void addChest(org.millenaire.common.utilities.Point pos) {
        if (pos != null) {
            this.chests.add(pointToBlockPos(pos));
        }
    }

    /**
     * Add a chest position from BlockPos.
     */
    public void addChest(BlockPos pos) {
        if (pos != null) {
            this.chests.add(pos);
        }
    }

    /**
     * Get list of chest positions as Points (for BuildingLocation compatibility).
     */
    public java.util.List<org.millenaire.common.utilities.Point> getChests() {
        java.util.List<org.millenaire.common.utilities.Point> result = new java.util.ArrayList<>();
        for (BlockPos pos : this.chests) {
            result.add(new org.millenaire.common.utilities.Point(pos.getX(), pos.getY(), pos.getZ()));
        }
        return result;
    }

    /**
     * Alias for invalidateCache for compatibility.
     */
    public void invalidateInventoryCache() {
        invalidateCache();
    }
}
