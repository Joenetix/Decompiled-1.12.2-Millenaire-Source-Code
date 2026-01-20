package org.millenaire.core.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.millenaire.core.MillBlockEntities;

public class TileEntityPanel extends BlockEntity {

    public static final int PANEL_VILLAGE_MAP = 0;
    public static final int PANEL_CONSTRUCTIONS = 1;
    public static final int PANEL_RESOURCES = 2;
    public static final int PANEL_MARKET = 3; // Future
    public static final int PANEL_DIPLOMACY = 4;
    public static final int PANEL_HOUSE = 5;
    public static final int PANEL_ARCHIVES = 7;
    public static final int PANEL_MILITARY = 9;
    public static final int PANEL_INN_TRADE_GOODS = 10;
    public static final int PANEL_INN_VISITORS = 11;
    public static final int PANEL_MARKET_MERCHANTS = 12;
    public static final int PANEL_CONTROLLED_MILITARY = 13;
    public static final int PANEL_VISITORS = 14;
    public static final int PANEL_WALLS = 15;

    public int panelType = 0;

    // Stubbed data for visuals
    private final String[] lines = new String[8];

    public TileEntityPanel(BlockPos pos, BlockState state) {
        super(MillBlockEntities.PANEL.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.panelType = tag.getInt("panelType");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("panelType", this.panelType);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public String[] getRenderText() {
        if (level == null || level.isClientSide) {
            // Client might not have full world data sync, but we try access via
            // MillWorldData if valid
            // Note: MillWorldData is server-side only usually.
            // For client rendering, we rely on the Text array being synced?
            // Orginal mod synced text strings.
            // For parity in this version, we will generate text on server and sync it via
            // NBT/Packet?
            // Actually TileEntityPanelRenderer calls this on client.

            // Issue: Client doesn't have MillWorldData.
            // Solution: We need to store the text in the TE and update it from Server.
            // For now, let's keep the stub behavior on client if data is missing,
            // BUT we should implement an updateText() method called on server tick/update
            // logic
            // that writes to the 'lines' array, which is then synced.
            return lines;
        }

        // This logic should ideally be called on Server and synced strings to client.
        // However, for this step, let's try to fetch what we can.

        // Wait, Renderer is CLIENT side. It definitely cannot access MillWorldData
        // (Server side).
        // So we must rely on 'lines' being populated by the server.
        return lines;
    }

    public void updatePanelInfo() {
        if (level == null || level.isClientSide)
            return;

        org.millenaire.common.world.MillWorldData mw = org.millenaire.common.world.MillWorldData.get(level);
        if (mw == null)
            return;

        org.millenaire.common.village.Building building = mw.getBuildingAt(this.worldPosition);

        // Reset lines
        for (int i = 0; i < 8; i++)
            lines[i] = "";

        if (building == null) {
            lines[0] = "Panel inactive";
            lines[1] = "No Building";
            lines[2] = "Found";
            setChanged();
            return;
        }

        org.millenaire.core.Village village = building.getVillage(); // Might be null if not TownHall

        switch (panelType) {
            case PANEL_VILLAGE_MAP:
                if (village != null) {
                    lines[0] = village.getFormattedName();
                    lines[1] = village.getVillageType().getName(); // Assuming getName exists or similar
                    lines[2] = "Population: " + village.getPopulationSize();
                } else {
                    lines[0] = building.name != null ? building.name : "Building";
                    lines[1] = "Not a Village";
                }
                break;
            case PANEL_DIPLOMACY:
                if (village != null) {
                    lines[0] = "Diplomacy:";
                    java.util.Map<java.util.UUID, Integer> relations = village.diplomacyManager.getRelations();
                    int idx = 1;
                    if (relations.isEmpty()) {
                        lines[1] = "No known villages";
                    } else {
                        for (java.util.Map.Entry<java.util.UUID, Integer> entry : relations.entrySet()) {
                            if (idx >= 8)
                                break;
                            org.millenaire.core.Village v = mw.getVillage(entry.getKey());
                            String vName = (v != null) ? v.getFormattedName() : "Unknown";
                            String rel = "Neutral";
                            int val = entry.getValue();
                            if (val >= 80)
                                rel = "Ally";
                            else if (val >= 40)
                                rel = "Friendly";
                            else if (val <= -80)
                                rel = "War";
                            else if (val <= -40)
                                rel = "Hostile";

                            lines[idx++] = vName + ": " + rel;
                        }
                    }
                } else {
                    lines[0] = "No Village";
                }
                break;
            case PANEL_CONSTRUCTIONS:
                if (building.currentConstructionStep > 0) {
                    lines[0] = "Construction";
                    lines[1] = "Step: " + building.currentConstructionStep;
                    // lines[2] = "Target: " + building.getCurrentProjectName();
                } else {
                    lines[0] = "No active";
                    lines[1] = "construction";
                }
                break;

            case PANEL_RESOURCES:
                lines[0] = "Resources";
                lines[2] = "Wood: " + building.resManager.countInv(net.minecraft.world.item.Items.OAK_LOG); // Example
                lines[3] = "Stone: " + building.resManager.countInv(net.minecraft.world.item.Items.COBBLESTONE);
                lines[4] = "Food: " + building.resManager.countInv(net.minecraft.world.item.Items.BREAD);
                break;
            // Future cases
            case PANEL_MARKET:
                lines[0] = "Market";
                lines[1] = "Coming Soon";
                break;

            default:
                lines[0] = "Unknown Panel";
                lines[1] = "Type: " + panelType;
                break;
        }

        // Sync to client
        this.setChanged();
        level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    // Add to load/save
    // We need to save the lines so client sees them after load without waiting for
    // update
    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, TileEntityPanel te) {
        if (level.isClientSide)
            return;

        // Update every second (20 ticks)
        if (level.getGameTime() % 20 == 0) {
            te.updatePanelInfo();
        }
    }
    // update

}
