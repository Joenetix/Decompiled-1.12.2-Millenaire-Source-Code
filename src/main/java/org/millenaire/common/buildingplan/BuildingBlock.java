package org.millenaire.common.buildingplan;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.millenaire.core.MillBlocks;
import org.millenaire.utilities.BlockItemUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.utilities.WorldUtilities;
import org.millenaire.common.village.Building;

/**
 * Represents a single block within a building plan.
 * Handles placing blocks in the world with appropriate special behaviors.
 * 
 * Ported from 1.12.2 to 1.20.1.
 */
public class BuildingBlock {
    // Special block type constants
    public static byte TAPESTRY = 1;
    public static byte OAKSPAWN = 2;
    public static byte PINESPAWN = 3;
    public static byte BIRCHSPAWN = 4;
    public static byte INDIANSTATUE = 5;
    public static byte PRESERVEGROUNDDEPTH = 6;
    public static byte CLEARTREE = 7;
    public static byte MAYANSTATUE = 8;
    public static byte SPAWNERSKELETON = 9;
    public static byte SPAWNERZOMBIE = 10;
    public static byte SPAWNERSPIDER = 11;
    public static byte SPAWNERCAVESPIDER = 12;
    public static byte SPAWNERCREEPER = 13;
    public static byte DISPENDERUNKNOWNPOWDER = 14;
    public static byte JUNGLESPAWN = 15;
    public static byte INVERTED_DOOR = 16;
    public static byte CLEARGROUND = 17;
    public static byte BYZANTINEICONSMALL = 18;
    public static byte BYZANTINEICONMEDIUM = 19;
    public static byte BYZANTINEICONLARGE = 20;
    public static byte PRESERVEGROUNDSURFACE = 21;
    public static byte SPAWNERBLAZE = 22;
    public static byte ACACIASPAWN = 23;
    public static byte DARKOAKSPAWN = 24;
    public static byte TORCHGUESS = 25;
    public static byte CHESTGUESS = 26;
    public static byte FURNACEGUESS = 27;
    public static byte CLEARGROUNDOUTSIDEBUILDING = 28;
    public static byte HIDEHANGING = 29;
    public static byte APPLETREESPAWN = 30;
    public static byte CLEARGROUNDBORDER = 31;
    public static byte OLIVETREESPAWN = 32;
    public static byte PISTACHIOTREESPAWN = 33;
    public static byte WALLCARPETSMALL = 40;
    public static byte WALLCARPETMEDIUM = 41;
    public static byte WALLCARPETLARGE = 42;
    public static byte CHERRYTREESPAWN = 43;
    public static byte SAKURATREESPAWN = 44;

    public Block block;
    private byte meta;
    public final Point p;
    private BlockState blockState;
    public byte special;

    public BuildingBlock(Point p, Block block, int meta) {
        this.p = p;
        this.block = block;
        this.meta = (byte) meta;
        this.blockState = block.defaultBlockState();
        this.special = 0;
    }

    public BuildingBlock(Point p, BlockState bs) {
        this.p = p;
        this.block = bs.getBlock();
        this.meta = 0;
        this.blockState = bs;
        this.special = 0;
    }

    public BuildingBlock(Point p, int special) {
        this.p = p;
        this.block = Blocks.AIR;
        this.meta = 0;
        this.special = (byte) special;
        this.blockState = Blocks.AIR.defaultBlockState();
    }

    // Constructor for reading
    public BuildingBlock(Point p) {
        this.p = p;
        this.block = Blocks.AIR;
        this.blockState = Blocks.AIR.defaultBlockState();
        this.meta = 0;
        this.special = 0;
    }

    public void read(DataInputStream ds) throws IOException {
        String blockId = ds.readUTF();
        this.block = BuiltInRegistries.BLOCK.get(new ResourceLocation(blockId));
        this.meta = ds.readByte();
        this.special = ds.readByte();
        if (this.block != null) {
            this.blockState = this.block.defaultBlockState();
        }
    }

    public void write(DataOutputStream ds) throws IOException {
        ds.writeUTF(BuiltInRegistries.BLOCK.getKey(this.block).toString());
        ds.writeByte(this.meta);
        ds.writeByte(this.special);
    }

    /**
     * Check if this block placement is already done in the world.
     */
    public boolean alreadyDone(Level world) {
        if (this.special != 0) {
            return false;
        }
        Block existingBlock = WorldUtilities.getBlock(world, this.p);
        if (this.block != existingBlock) {
            return false;
        }
        BlockState existingState = WorldUtilities.getBlockState(world, this.p);
        return this.blockState.equals(existingState);
    }

    /**
     * Main build method - places this block in the world based on its special type.
     */
    public boolean build(Level world, Building townHall, boolean worldGeneration, boolean wandimport) {
        boolean blockSet = false;

        try {
            boolean notifyBlocks = true;
            boolean playSound = !worldGeneration && !wandimport;

            if (this.special == 0) {
                blockSet = this.buildNormalBlock(world, townHall, wandimport, notifyBlocks, playSound);
            } else if (this.special == PRESERVEGROUNDDEPTH || this.special == PRESERVEGROUNDSURFACE) {
                blockSet = this.buildPreserveGround(world, worldGeneration, notifyBlocks, playSound);
            } else if (this.special == CLEARTREE) {
                blockSet = this.buildClearTree(world, worldGeneration, notifyBlocks, playSound);
            } else if (this.special == CLEARGROUND || this.special == CLEARGROUNDOUTSIDEBUILDING
                    || this.special == CLEARGROUNDBORDER) {
                blockSet = this.buildClearGround(world, worldGeneration, wandimport, notifyBlocks, playSound);
            } else if (this.special == TAPESTRY
                    || this.special == INDIANSTATUE
                    || this.special == MAYANSTATUE
                    || this.special == BYZANTINEICONSMALL
                    || this.special == BYZANTINEICONMEDIUM
                    || this.special == BYZANTINEICONLARGE
                    || this.special == HIDEHANGING
                    || this.special == WALLCARPETSMALL
                    || this.special == WALLCARPETMEDIUM
                    || this.special == WALLCARPETLARGE) {
                blockSet = this.buildPicture(world);
            } else if (this.special == OAKSPAWN
                    || this.special == PINESPAWN
                    || this.special == BIRCHSPAWN
                    || this.special == JUNGLESPAWN
                    || this.special == ACACIASPAWN
                    || this.special == DARKOAKSPAWN
                    || this.special == APPLETREESPAWN
                    || this.special == OLIVETREESPAWN
                    || this.special == PISTACHIOTREESPAWN
                    || this.special == CHERRYTREESPAWN
                    || this.special == SAKURATREESPAWN) {
                blockSet = this.buildTreeSpawn(world, worldGeneration);
            } else if (this.special == SPAWNERSKELETON) {
                WorldUtilities.setBlock(world, this.p, Blocks.SPAWNER, notifyBlocks, playSound);
                if (this.p.getTileEntity(world) instanceof SpawnerBlockEntity spawner) {
                    spawner.getSpawner().setEntityId(net.minecraft.world.entity.EntityType.SKELETON, world,
                            world.getRandom(), this.p.getBlockPos());
                }
                blockSet = true;
            } else if (this.special == SPAWNERZOMBIE) {
                WorldUtilities.setBlock(world, this.p, Blocks.SPAWNER, notifyBlocks, playSound);
                if (this.p.getTileEntity(world) instanceof SpawnerBlockEntity spawner) {
                    spawner.getSpawner().setEntityId(net.minecraft.world.entity.EntityType.ZOMBIE, world,
                            world.getRandom(), this.p.getBlockPos());
                }
                blockSet = true;
            } else if (this.special == SPAWNERSPIDER) {
                WorldUtilities.setBlock(world, this.p, Blocks.SPAWNER, notifyBlocks, playSound);
                if (this.p.getTileEntity(world) instanceof SpawnerBlockEntity spawner) {
                    spawner.getSpawner().setEntityId(net.minecraft.world.entity.EntityType.SPIDER, world,
                            world.getRandom(), this.p.getBlockPos());
                }
                blockSet = true;
            } else if (this.special == SPAWNERCAVESPIDER) {
                WorldUtilities.setBlock(world, this.p, Blocks.SPAWNER, notifyBlocks, playSound);
                if (this.p.getTileEntity(world) instanceof SpawnerBlockEntity spawner) {
                    spawner.getSpawner().setEntityId(net.minecraft.world.entity.EntityType.CAVE_SPIDER, world,
                            world.getRandom(), this.p.getBlockPos());
                }
                blockSet = true;
            } else if (this.special == SPAWNERCREEPER) {
                WorldUtilities.setBlock(world, this.p, Blocks.SPAWNER, notifyBlocks, playSound);
                if (this.p.getTileEntity(world) instanceof SpawnerBlockEntity spawner) {
                    spawner.getSpawner().setEntityId(net.minecraft.world.entity.EntityType.CREEPER, world,
                            world.getRandom(), this.p.getBlockPos());
                }
                blockSet = true;
            } else if (this.special == SPAWNERBLAZE) {
                WorldUtilities.setBlock(world, this.p, Blocks.SPAWNER, notifyBlocks, playSound);
                if (this.p.getTileEntity(world) instanceof SpawnerBlockEntity spawner) {
                    spawner.getSpawner().setEntityId(net.minecraft.world.entity.EntityType.BLAZE, world,
                            world.getRandom(), this.p.getBlockPos());
                }
                blockSet = true;
            } else if (this.special == DISPENDERUNKNOWNPOWDER) {
                WorldUtilities.setBlock(world, this.p, Blocks.DISPENSER, notifyBlocks, playSound);
                // TODO: Add unknown powder to dispenser
                blockSet = true;
            } else if (this.special == FURNACEGUESS) {
                Direction facing = this.guessChestFurnaceFacing(world, this.p);
                BlockState furnaceBS = Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, facing);
                world.setBlock(this.p.getBlockPos(), furnaceBS, 3);
                blockSet = true;
            } else if (this.special == CHESTGUESS) {
                Direction facing = this.guessChestFurnaceFacing(world, this.p);
                BlockState chestBS = MillBlocks.LOCKED_CHEST.get().defaultBlockState().setValue(ChestBlock.FACING,
                        facing);
                world.setBlock(this.p.getBlockPos(), chestBS, 3);
                blockSet = true;
            } else if (this.special == TORCHGUESS) {
                // Place torch, attempting wall mount then floor
                BlockState torchBS = Blocks.TORCH.defaultBlockState();
                world.setBlock(this.p.getBlockPos(), torchBS, 3);
                blockSet = true;
            } else if (this.special == INVERTED_DOOR) {
                world.setBlock(this.p.getBlockPos(), this.blockState, 3);
                if (this.blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) &&
                        this.blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                    BlockState upperState = this.blockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,
                            DoubleBlockHalf.UPPER);
                    if (upperState.hasProperty(BlockStateProperties.DOOR_HINGE)) {
                        upperState = upperState.setValue(BlockStateProperties.DOOR_HINGE,
                                net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT);
                    }
                    WorldUtilities.setBlockState(world, this.p.getAbove(), upperState, notifyBlocks, playSound);
                }
                blockSet = true;
            }
        } catch (Exception e) {
            MillLog.printException("Exception in BuildingBlock.build():", e);
        }

        return blockSet;
    }

    /**
     * Clear ground in building area.
     */
    private boolean buildClearGround(Level world, boolean worldGeneration, boolean wandimport, boolean notifyBlocks,
            boolean playSound) {
        boolean shouldSetBlock = false;
        boolean shouldSetBlockBelow = false;
        Block existingBlock = WorldUtilities.getBlock(world, this.p);
        BlockState targetBlockState = null;
        BlockState targetBelowBlockState = null;

        if ((!wandimport || (existingBlock != Blocks.COMMAND_BLOCK && existingBlock != MillBlocks.IMPORT_TABLE.get()))
                && !BlockItemUtilities.isBlockDecorativePlant(existingBlock)) {

            if (this.special == CLEARGROUNDBORDER && !(existingBlock instanceof LeavesBlock)
                    && existingBlock != Blocks.AIR) {
                // Check for adjacent water - place solid ground instead
                if (WorldUtilities.isLiquid(world, this.p.getEast())
                        || WorldUtilities.isLiquid(world, this.p.getWest())
                        || WorldUtilities.isLiquid(world, this.p.getNorth())
                        || WorldUtilities.isLiquid(world, this.p.getSouth())) {
                    BlockState blockStateBelow = WorldUtilities.getBlockState(world, this.p.getBelow());
                    targetBlockState = WorldUtilities.getBlockStateValidGround(blockStateBelow, true);
                    if (targetBlockState == null) {
                        targetBlockState = Blocks.DIRT.defaultBlockState();
                    }
                    if (existingBlock != targetBlockState.getBlock()) {
                        shouldSetBlock = true;
                    }
                } else if (existingBlock != Blocks.AIR) {
                    targetBlockState = Blocks.AIR.defaultBlockState();
                    shouldSetBlock = true;
                }
            } else if (existingBlock != Blocks.AIR
                    && (this.special != CLEARGROUNDOUTSIDEBUILDING && this.special != CLEARGROUNDBORDER
                            || !(existingBlock instanceof LeavesBlock))) {
                targetBlockState = Blocks.AIR.defaultBlockState();
                shouldSetBlock = true;
            }
        }

        // Convert dirt to grass for world generation
        BlockState blockStateBelowCheck = WorldUtilities.getBlockState(world, this.p.getBelow());
        targetBelowBlockState = WorldUtilities.getBlockStateValidGround(blockStateBelowCheck, true);
        if (worldGeneration && targetBelowBlockState != null && targetBelowBlockState.getBlock() == Blocks.DIRT) {
            targetBelowBlockState = Blocks.GRASS_BLOCK.defaultBlockState();
            shouldSetBlockBelow = true;
        }

        if (shouldSetBlock && targetBlockState != null) {
            WorldUtilities.setBlockState(world, this.p, targetBlockState, notifyBlocks, playSound);
        }

        if (shouldSetBlockBelow && targetBelowBlockState != null) {
            WorldUtilities.setBlockState(world, this.p.getBelow(), targetBelowBlockState, notifyBlocks, playSound);
        }

        return shouldSetBlock || shouldSetBlockBelow;
    }

    /**
     * Clear tree trunks from the building area.
     */
    private boolean buildClearTree(Level world, boolean worldGeneration, boolean notifyBlocks, boolean playSound) {
        Block block = WorldUtilities.getBlock(world, this.p);
        if (!BlockItemUtilities.isLog(block)) {
            return false;
        }
        WorldUtilities.setBlock(world, this.p, Blocks.AIR, notifyBlocks, playSound);

        // Fix grass below
        BlockState blockStateBelow = WorldUtilities.getBlockState(world, this.p.getBelow());
        BlockState targetBlockState = WorldUtilities.getBlockStateValidGround(blockStateBelow, true);
        if (worldGeneration && targetBlockState != null && targetBlockState.getBlock() == Blocks.DIRT) {
            WorldUtilities.setBlock(world, this.p.getBelow(), Blocks.GRASS_BLOCK, notifyBlocks, playSound);
        } else if (targetBlockState != null && targetBlockState != Blocks.DIRT.defaultBlockState()) {
            WorldUtilities.setBlockState(world, this.p.getBelow(), targetBlockState, notifyBlocks, playSound);
        }
        return true;
    }

    /**
     * Build a normal block (not special).
     */
    private boolean buildNormalBlock(Level world, Building townHall, boolean wandimport, boolean notifyBlocks,
            boolean playSound) {
        boolean blockSet = false;

        // Handle doors - clear space above for lower half
        if (this.block instanceof DoorBlock) {
            if (this.blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) &&
                    this.blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                WorldUtilities.setBlock(world, this.p.getAbove(), Blocks.AIR, notifyBlocks, playSound);
            }
        }

        // Don't overwrite command blocks during wand import
        if (!wandimport || this.block != Blocks.AIR || WorldUtilities.getBlock(world, this.p) != Blocks.COMMAND_BLOCK) {
            Block existingBlock = WorldUtilities.getBlock(world, this.p);

            if (this.block == Blocks.AIR) {
                if (!BlockItemUtilities.isBlockDecorativePlant(existingBlock)) {
                    WorldUtilities.setBlockState(world, this.p, this.blockState, notifyBlocks, playSound);
                    blockSet = true;
                }
            } else {
                // Don't replace grass with dirt
                if (this.blockState != Blocks.DIRT.defaultBlockState() || existingBlock != Blocks.GRASS_BLOCK) {
                    WorldUtilities.setBlockState(world, this.p, this.blockState, notifyBlocks, playSound);
                    blockSet = true;
                }
            }
        }

        // Handle door upper halves
        if (this.block instanceof DoorBlock) {
            if (this.blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) &&
                    this.blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                BlockState upperState = this.blockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,
                        DoubleBlockHalf.UPPER);
                if (this.special == INVERTED_DOOR && upperState.hasProperty(BlockStateProperties.DOOR_HINGE)) {
                    upperState = upperState.setValue(BlockStateProperties.DOOR_HINGE,
                            net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT);
                }
                WorldUtilities.setBlockState(world, this.p.getAbove(), upperState, notifyBlocks, playSound);
            }
        }
        // Handle double plants
        else if (this.block instanceof DoublePlantBlock) {
            BlockState upperState = this.blockState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
            WorldUtilities.setBlockState(world, this.p.getAbove(), upperState, notifyBlocks, playSound);
        }
        // Handle beds
        else if (this.blockState.hasProperty(BlockStateProperties.BED_PART)) {
            if (this.blockState.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD) {
                Direction facing = this.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                Point footPos = this.p.getRelative(facing.getOpposite());
                BlockState footState = this.blockState.setValue(BlockStateProperties.BED_PART, BedPart.FOOT);
                WorldUtilities.setBlockState(world, footPos, footState, notifyBlocks, playSound);
            }
        }

        return blockSet;
    }

    /**
     * Build wall decoration (tapestry, statue, etc.)
     */
    private boolean buildPicture(Level world) {
        // TODO: Implement wall decoration entity spawning
        MillLog.warning(this, "Wall decoration spawning not yet implemented for special=" + this.special);
        return false;
    }

    /**
     * Preserve existing ground blocks where appropriate.
     */
    private boolean buildPreserveGround(Level world, boolean worldGeneration, boolean notifyBlocks, boolean playSound) {
        BlockState existingBlockState = WorldUtilities.getBlockState(world, this.p);
        boolean surface = this.special == PRESERVEGROUNDSURFACE;

        // Check if we should preserve the existing block
        if (!surface && existingBlockState.canOcclude() && existingBlockState.isSolid()) {
            // Material check - preserve natural blocks
            if (BlockItemUtilities.isNaturalBlock(existingBlockState.getBlock())) {
                return false;
            }
        }

        BlockState targetGroundBlockState = WorldUtilities.getBlockStateValidGround(existingBlockState, surface);
        if (targetGroundBlockState == null) {
            // Search below for valid ground
            for (Point below = this.p.getBelow(); targetGroundBlockState == null
                    && below.getiY() > world.getMinBuildHeight(); below = below.getBelow()) {
                BlockState belowState = WorldUtilities.getBlockState(world, below);
                targetGroundBlockState = WorldUtilities.getBlockStateValidGround(belowState, surface);
            }
            if (targetGroundBlockState == null) {
                targetGroundBlockState = Blocks.DIRT.defaultBlockState();
            }
        }

        // Convert dirt to grass for world generation on surface
        if (targetGroundBlockState.getBlock() == Blocks.DIRT && worldGeneration && surface) {
            targetGroundBlockState = Blocks.GRASS_BLOCK.defaultBlockState();
        }

        // Don't use grass during non-worldgen
        if (targetGroundBlockState.getBlock() == Blocks.GRASS_BLOCK && !worldGeneration) {
            targetGroundBlockState = Blocks.DIRT.defaultBlockState();
        }

        // Fallback
        if (targetGroundBlockState == null || targetGroundBlockState.getBlock() == Blocks.AIR) {
            targetGroundBlockState = worldGeneration && surface ? Blocks.GRASS_BLOCK.defaultBlockState()
                    : Blocks.DIRT.defaultBlockState();
        }

        // Don't change if already correct
        if (targetGroundBlockState.equals(existingBlockState)) {
            return false;
        }
        // Don't replace grass with dirt
        if (existingBlockState.getBlock() == Blocks.GRASS_BLOCK && targetGroundBlockState.getBlock() == Blocks.DIRT) {
            return false;
        }

        WorldUtilities.setBlockState(world, this.p, targetGroundBlockState, notifyBlocks, playSound);
        return true;
    }

    /**
     * Spawn a tree at this location by placing a sapling.
     * Trees will grow naturally over time.
     */
    private boolean buildTreeSpawn(Level world, boolean worldGeneration) {
        if (!worldGeneration) {
            return false;
        }

        BlockPos pos = this.p.getBlockPos();
        Block sapling = Blocks.OAK_SAPLING;

        // Select appropriate sapling based on tree type
        if (this.special == OAKSPAWN || this.special == APPLETREESPAWN) {
            sapling = Blocks.OAK_SAPLING;
        } else if (this.special == PINESPAWN) {
            sapling = Blocks.SPRUCE_SAPLING;
        } else if (this.special == BIRCHSPAWN) {
            sapling = Blocks.BIRCH_SAPLING;
        } else if (this.special == JUNGLESPAWN) {
            sapling = Blocks.JUNGLE_SAPLING;
        } else if (this.special == ACACIASPAWN) {
            sapling = Blocks.ACACIA_SAPLING;
        } else if (this.special == DARKOAKSPAWN) {
            sapling = Blocks.DARK_OAK_SAPLING;
        } else if (this.special == CHERRYTREESPAWN || this.special == SAKURATREESPAWN) {
            sapling = Blocks.CHERRY_SAPLING;
        } else if (this.special == OLIVETREESPAWN || this.special == PISTACHIOTREESPAWN) {
            // Custom trees - use oak as fallback for now
            sapling = Blocks.OAK_SAPLING;
        }

        // Place sapling
        world.setBlock(pos, sapling.defaultBlockState(), 3);
        return true;
    }

    /**
     * Guess facing direction for chests and furnaces based on surroundings.
     */
    private Direction guessChestFurnaceFacing(Level world, Point p) {
        BlockState bsNorth = WorldUtilities.getBlockState(world, p.getNorth());
        BlockState bsSouth = WorldUtilities.getBlockState(world, p.getSouth());
        BlockState bsWest = WorldUtilities.getBlockState(world, p.getWest());
        BlockState bsEast = WorldUtilities.getBlockState(world, p.getEast());

        // Face away from solid blocks
        if (bsNorth.canOcclude() && bsNorth.getBlock() != Blocks.FURNACE
                && bsNorth.getBlock() != MillBlocks.LOCKED_CHEST.get()
                && !bsSouth.canOcclude()) {
            return Direction.SOUTH;
        } else if (bsSouth.canOcclude() && bsSouth.getBlock() != Blocks.FURNACE
                && bsSouth.getBlock() != MillBlocks.LOCKED_CHEST.get()
                && !bsNorth.canOcclude()) {
            return Direction.NORTH;
        } else if (bsWest.canOcclude() && bsWest.getBlock() != Blocks.FURNACE
                && bsWest.getBlock() != MillBlocks.LOCKED_CHEST.get()
                && !bsEast.canOcclude()) {
            return Direction.EAST;
        } else if (bsEast.canOcclude() && bsEast.getBlock() != Blocks.FURNACE
                && bsEast.getBlock() != MillBlocks.LOCKED_CHEST.get()
                && !bsWest.canOcclude()) {
            return Direction.WEST;
        }

        // Default fallbacks
        if (!bsSouth.canOcclude())
            return Direction.SOUTH;
        if (!bsNorth.canOcclude())
            return Direction.NORTH;
        if (!bsEast.canOcclude())
            return Direction.EAST;
        if (!bsWest.canOcclude())
            return Direction.WEST;
        return Direction.NORTH;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public void setBlockState(BlockState bs) {
        this.blockState = bs;
        this.block = bs.getBlock();
    }

    public byte getMeta() {
        return meta;
    }

    public void setMeta(byte meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "(block: " + this.block + " meta: " + this.meta + " pos:" + this.p + " special:" + this.special + ")";
    }
}
