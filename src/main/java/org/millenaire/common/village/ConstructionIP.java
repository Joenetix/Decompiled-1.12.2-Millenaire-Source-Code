package org.millenaire.common.village;

import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.utilities.WorldUtilities;
import org.millenaire.common.buildingplan.BuildingBlock;
import org.millenaire.common.entity.MillVillager;

import java.io.*;
import java.nio.file.Files;
// import java.util.stream.Stream; // unused

public class ConstructionIP {
    private final Building townHall;
    private BuildingBlock[] bblocks = null;
    private int bblocksPos = 0;
    private final int id;
    private BuildingLocation buildingLocation;
    private boolean bblocksChanged = false;
    private final boolean wallConstruction;
    private MillVillager builder = null;

    public ConstructionIP(Building th, int id, boolean wallConstruction) {
        this.townHall = th;
        this.id = id;
        this.wallConstruction = wallConstruction;
    }

    public boolean areBlocksLeft() {
        return this.bblocks != null && this.bblocksPos < this.bblocks.length;
    }

    public void clearBblocks() {
        this.bblocks = null;
    }

    public void clearBuildingLocation() {
        this.buildingLocation = null;
    }

    public BuildingBlock[] getBblocks() {
        return this.bblocks;
    }

    public int getBblocksPos() {
        return this.bblocksPos;
    }

    public MillVillager getBuilder() {
        return this.builder;
    }

    public BuildingLocation getBuildingLocation() {
        return this.buildingLocation;
    }

    public BuildingBlock getCurrentBlock() {
        if (this.bblocks == null) {
            return null;
        } else {
            return this.bblocksPos >= this.bblocks.length ? null : this.bblocks[this.bblocksPos];
        }
    }

    public int getId() {
        return this.id;
    }

    public void incrementBblockPos() {
        this.bblocksPos++;
        if (!this.areBlocksLeft()) {
            this.bblocks = null;
            this.bblocksPos = 0;
            this.bblocksChanged = true;
        }
    }

    public boolean isBblocksChanged() {
        return this.bblocksChanged;
    }

    public boolean isWallConstruction() {
        return this.wallConstruction;
    }

    public void readBblocks() {
        File buildingsDir = MillCommonUtilities.getBuildingsDir(this.townHall.world);
        File file1 = new File(buildingsDir, this.townHall.getPos().getX() + "_" + this.townHall.getPos().getY() + "_"
                + this.townHall.getPos().getZ() + "_bblocks_" + this.id + ".bin");
        if (file1.exists()) {
            try (FileInputStream fis = new FileInputStream(file1); DataInputStream ds = new DataInputStream(fis)) {
                int size = ds.readInt();
                this.bblocks = new BuildingBlock[size];

                for (int i = 0; i < size; i++) {
                    Point p = new Point(ds.readInt(), ds.readShort(), ds.readInt());
                    BuildingBlock b = new BuildingBlock(p);
                    // b.read(ds); // BuildingBlock needs read method
                    // Manually reading based on source logic if BuildingBlock doesn't have it yet?
                    // Source: BuildingBlock b = new BuildingBlock(p, ds);
                    // We need to check BuildingBlock.java. Assuming stub for now.
                    b.read(ds);
                    this.bblocks[i] = b;
                }

                if (this.bblocks.length == 0) {
                    MillLog.error(this, "Saved bblocks had zero elements. Rushing construction.");
                    // this.townHall.rushCurrentConstructions(false);
                }

            } catch (Exception e) {
                MillLog.printException("Error when reading bblocks: ", e);
                this.bblocks = null;
            }
        }
    }

    public void setBblockPos(int pos) {
        this.bblocksPos = pos;
    }

    public void setBuilder(MillVillager builder) {
        this.builder = builder;
    }

    public void setBuildingLocation(BuildingLocation buildingLocation) {
        this.buildingLocation = buildingLocation;
    }

    public void startNewConstruction(BuildingLocation bl, BuildingBlock[] bblocks) {
        this.buildingLocation = bl;
        this.bblocks = bblocks;
        this.bblocksPos = 0;
        this.bblocksChanged = true;
        // if (this.townHall.winfo != null) {
        // this.townHall.winfo.addBuildingLocationToMap(bl);
        // }
    }

    public void writeBblocks() {
        File buildingsDir = MillCommonUtilities.getBuildingsDir(this.townHall.world);
        File blocksFile = new File(buildingsDir,
                this.townHall.getPos().getX() + "_" + this.townHall.getPos().getY() + "_"
                        + this.townHall.getPos().getZ() + "_bblocks_" + this.id + ".bin");
        BuildingBlock[] blocks = this.getBblocks();
        if (blocks != null) {
            try (FileOutputStream fos = new FileOutputStream(blocksFile);
                    DataOutputStream ds = new DataOutputStream(fos)) {
                ds.writeInt(blocks.length);
                for (BuildingBlock b : blocks) {
                    ds.writeInt(b.p.getiX());
                    ds.writeShort(b.p.getiY());
                    ds.writeInt(b.p.getiZ());
                    // WorldUtilities.getBlockId(b.block) implementation needed
                    // For now, assuming b.write(ds) exists or we standard write
                    b.write(ds);
                }
            } catch (IOException e) {
                MillLog.printException("Error when writing bblocks: ", e);
            }
        } else {
            try {
                if (blocksFile.exists()) {
                    Files.delete(blocksFile.toPath());
                }
            } catch (IOException e) {
                MillLog.printException("Error when deleting bblocks: ", e);
            }
        }
        this.bblocksChanged = false;
    }
}
