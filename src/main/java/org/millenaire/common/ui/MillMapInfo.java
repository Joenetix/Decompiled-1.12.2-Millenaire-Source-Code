package org.millenaire.common.ui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.network.StreamReadWrite;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.VillageMapInfo;

/**
 * Represents map information for a village, used for the village map UI.
 * Tracks terrain types and building locations for display.
 */
public class MillMapInfo {
    public static final byte WATER = 1;
    public static final byte DANGER = 2;
    public static final byte BUILDING_FORBIDDEN = 3;
    public static final byte BUILDING_LOC = 4;
    public static final byte TREE = 5;
    public static final byte UNREACHABLE = 6;
    public static final byte UNBUILDABLE = 7;
    public static final byte OUTOFRANGE = 8;
    public static final byte OTHER = 9;
    public static final byte PATH = 10;
    public static final byte INSIDE_BUILDING = 11;

    public byte[][] data;
    public int width;
    public int length;
    public int mapStartX = 0;
    public int mapStartZ = 0;
    public Building townHall;

    /**
     * Read map info from a network packet.
     */
    public static void readPacket(FriendlyByteBuf data2) {
        Point pos = StreamReadWrite.readNullablePoint(data2);
        Building building = Mill.clientWorld.getBuilding(pos);

        if (building != null) {
            MillMapInfo minfo = new MillMapInfo(building);
            minfo.length = data2.readInt();
            minfo.width = data2.readInt();
            minfo.mapStartX = data2.readInt();
            minfo.mapStartZ = data2.readInt();
            minfo.data = new byte[minfo.length][];

            for (int x = 0; x < minfo.length; x++) {
                minfo.data[x] = new byte[minfo.width];
                for (int z = 0; z < minfo.width; z++) {
                    minfo.data[x][z] = data2.readByte();
                }
            }

            building.mapInfo = minfo;
            if (MillConfigValues.LogNetwork >= 3) {
                MillLog.debug(null, "Receiving map info packet.");
            }
        }
    }

    private MillMapInfo(Building townHall) {
        this.townHall = townHall;
    }

    /**
     * Create map info from village map data.
     */
    public MillMapInfo(Building townHall, VillageMapInfo winfo) {
        this.townHall = townHall;
        short thRegionId = 0;

        try {
            townHall.rebuildRegionMapper(true);
        } catch (MillLog.MillenaireException e) {
            MillLog.printException(e);
        }

        if (townHall.regionMapper != null) {
            thRegionId = townHall.regionMapper.thRegion;
        }

        Point centre = townHall.location.pos;
        int relcentreX = centre.getiX() - winfo.mapStartX;
        int relcentreZ = centre.getiZ() - winfo.mapStartZ;
        this.width = winfo.width;
        this.length = winfo.length;
        this.mapStartX = winfo.mapStartX;
        this.mapStartZ = winfo.mapStartZ;
        this.data = new byte[winfo.length][];

        for (int x = 0; x < winfo.length; x++) {
            this.data[x] = new byte[winfo.width];

            for (int y = 0; y < winfo.width; y++) {
                if (winfo.water[x][y]) {
                    this.data[x][y] = WATER;
                } else if (winfo.danger[x][y]) {
                    this.data[x][y] = DANGER;
                } else if (winfo.buildingForbidden[x][y]) {
                    this.data[x][y] = BUILDING_FORBIDDEN;
                } else if (winfo.buildingLocRef[x][y] != null) {
                    this.data[x][y] = BUILDING_LOC;
                } else if (winfo.tree[x][y]) {
                    this.data[x][y] = TREE;
                } else if (winfo.path[x][y]) {
                    this.data[x][y] = PATH;
                } else if (townHall.regionMapper != null && townHall.regionMapper.regions[x][y] != thRegionId) {
                    this.data[x][y] = UNREACHABLE;
                } else if (!winfo.canBuild[x][y]) {
                    this.data[x][y] = UNBUILDABLE;
                } else if (Math.abs(relcentreX - x) <= townHall.villageType.radius
                        && Math.abs(relcentreZ - y) <= townHall.villageType.radius) {
                    this.data[x][y] = OTHER;
                } else {
                    this.data[x][y] = OUTOFRANGE;
                }
            }
        }
    }

    /**
     * Send map info packet to a player.
     */
    public void sendMapInfoPacket(Player player) {
        FriendlyByteBuf ds = ServerSender.getPacketBuffer();
        ds.writeInt(7); // Packet ID for map info
        StreamReadWrite.writeNullablePoint(new Point(this.townHall.getPos()), ds);
        ds.writeInt(this.length);
        ds.writeInt(this.width);
        ds.writeInt(this.mapStartX);
        ds.writeInt(this.mapStartZ);

        for (int x = 0; x < this.length; x++) {
            for (int z = 0; z < this.width; z++) {
                ds.writeByte(this.data[x][z]);
            }
        }

        ServerSender.sendPacketToPlayer(ds, player);
    }
}
