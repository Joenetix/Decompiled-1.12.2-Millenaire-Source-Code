package org.millenaire.common.goal.generic;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;

import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

public class GoalGenericPlantSapling extends GoalGeneric {
    public static final String GOAL_TYPE = "plantsapling";

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
        // return this.icon != null ? this.icon.getItemStack() :
        // InvItem.createInvItem(Blocks.SAPLING).getItemStack();
        return ItemStack.EMPTY;
    }

    @Override
    public String getTypeLabel() {
        return "plantsapling";
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
    public boolean validateGoal() {
        return true;
    }
}
