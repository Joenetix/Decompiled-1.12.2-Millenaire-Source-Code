package org.millenaire.common.goal.generic;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.village.Building;

public class GoalGenericTendFurnace extends GoalGeneric {
    public static final String GOAL_TYPE = "tendfurnace";

    // Planks meta replacement for 1.20.1
    // private static ItemStack[][] PLANKS = ...

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "4")
    @ConfigAnnotations.FieldDocumentation(explanation = "Minimum number of wood to put back in one go.")
    public int minimumFuel;

    @Override
    public void applyDefaultSettings() {
        this.lookAtGoal = true;
        // this.icon = InvItem.createInvItem(Blocks.FURNACE);
    }

    @Override
    public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
        // Logic stubbed for parity check first
        return null;
    }

    @Override
    public ItemStack[] getHeldItemsTravelling(MillVillager villager) {
        // Logic stubbed
        return null;
    }

    @Override
    public String getTypeLabel() {
        return "tendfurnace";
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
