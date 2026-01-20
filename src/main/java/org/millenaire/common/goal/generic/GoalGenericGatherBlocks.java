package org.millenaire.common.goal.generic;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.pathing.atomicstryker.AStarConfig;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

public class GoalGenericGatherBlocks extends GoalGeneric {
    public static final String GOAL_TYPE = "gatherblocks";

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BONUS_ITEM_ADD)
    @ConfigAnnotations.FieldDocumentation(explanation = "Item to be harvested, with chance.")
    public List<AnnotedParameter.BonusItem> harvestItem = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BLOCKSTATE)
    @ConfigAnnotations.FieldDocumentation(explanation = "Blockstate to gather.")
    public BlockState gatherBlockState = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BLOCKSTATE)
    @ConfigAnnotations.FieldDocumentation(explanation = "Blockstate to place instead of the 'gathered' block. If null, the block will be left as-is.")
    public BlockState resultingBlockState = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "4")
    @ConfigAnnotations.FieldDocumentation(explanation = "Minimum number of available blocks in a building for the goal to start.")
    public Integer minimumAvailableBlocks;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "false")
    @ConfigAnnotations.FieldDocumentation(explanation = "Whether to store the collected goods in the destination building (if false, they go in the villager's inventory).")
    public boolean collectInBuilding;

    @Override
    public void applyDefaultSettings() {
        this.duration = 2;
        this.lookAtGoal = true;
    }

    @Override
    public Goal.GoalInformation getDestination(MillVillager villager) throws MillLog.MillenaireException {
        // Logic stubbed
        return null;
    }

    @Override
    public ItemStack getIcon() {
        if (this.icon != null) {
            // return this.icon.getItemStack();
            return ItemStack.EMPTY;
        } else {
            // return !this.harvestItem.isEmpty() ?
            // this.harvestItem.get(0).item.getItemStack() : new
            // ItemStack(this.gatherBlockState.getBlock(), 1);
            return ItemStack.EMPTY;
        }
    }

    @Override
    public AStarConfig getPathingConfig(MillVillager villager) {
        // return !villager.canVillagerClearLeaves() ? JPS_CONFIG_CHOPLUMBER_NO_LEAVES :
        // JPS_CONFIG_CHOPLUMBER;
        return null; // Stub
    }

    @Override
    public String getTypeLabel() {
        return "gatherblocks";
    }

    @Override
    public boolean isDestPossibleSpecific(MillVillager villager, Building b) {
        return true;
    }

    @Override
    public boolean isPossibleGenericGoal(MillVillager villager) throws Exception {
        return this.getDestination(villager) != null;
    }

    @Override
    public boolean performAction(MillVillager villager) throws Exception {
        // Logic stubbed
        return true;
    }

    @Override
    public int range(MillVillager villager) {
        return 8;
    }

    @Override
    public boolean validateGoal() {
        if (this.gatherBlockState == null) {
            MillLog.error(this, "The gather block state is mandatory in custom gather block goals.");
            return false;
        } else {
            return true;
        }
    }
}
