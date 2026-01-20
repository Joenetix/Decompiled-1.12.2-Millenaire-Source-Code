package org.millenaire.common.goal.generic;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.core.MillItems;
import org.millenaire.common.pathing.atomicstryker.AStarConfig;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.utilities.WorldUtilities;
import org.millenaire.common.village.Building;

public class GoalGenericMining extends GoalGeneric {
    // private static final ItemStack[] IS_ULU = new ItemStack[]{new
    // ItemStack(MillItems.ULU, 1)};
    public static final String GOAL_TYPE = "mining";

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BLOCKSTATE)
    @ConfigAnnotations.FieldDocumentation(explanation = "Blockstate of the source, like stone (not necessarily the block being harvest - stone gives cobblestone for example).")
    public BlockState sourceBlockState = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM_NUMBER_ADD, paramName = "loot")
    @ConfigAnnotations.FieldDocumentation(explanation = "Blocks or items gained when mining.")
    public Map<InvItem, Integer> loots = new HashMap<>();

    @Override
    public int actionDuration(MillVillager villager) throws Exception {
        return 70;
    }

    @Override
    public void applyDefaultSettings() {
        this.lookAtGoal = true;
    }

    @Override
    public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
        // Logic stubbed
        return null;
    }

    @Override
    public ItemStack[] getHeldItemsTravelling(MillVillager villager) throws Exception {
        return new ItemStack[] { villager.getBestPickaxeStack() };
    }

    @Override
    public ItemStack getIcon() {
        if (this.icon != null) {
            // return this.icon.getItemStack();
            return ItemStack.EMPTY;
        } else {
            // return this.sourceBlockState != null ? new
            // ItemStack(this.sourceBlockState.getBlock(), 1) : null;
            return ItemStack.EMPTY;
        }
    }

    @Override
    public AStarConfig getPathingConfig(MillVillager villager) {
        // return !villager.canVillagerClearLeaves() ? JPS_CONFIG_WIDE_NO_LEAVES :
        // JPS_CONFIG_WIDE;
        return null; // Stub
    }

    @Override
    public String getTypeLabel() {
        return "mining";
    }

    @Override
    public boolean isDestPossibleSpecific(MillVillager villager, Building b) {
        return true;
    }

    @Override
    public boolean isPossibleGenericGoal(MillVillager villager) throws Exception {
        return true;
    }

    @Override
    public boolean performAction(MillVillager villager) throws Exception {
        // Logic stubbed
        return true;
    }

    @Override
    public boolean stuckAction(MillVillager villager) throws Exception {
        return this.performAction(villager);
    }

    @Override
    public boolean swingArms() {
        return true;
    }

    @Override
    public boolean validateGoal() {
        return true;
    }
}

