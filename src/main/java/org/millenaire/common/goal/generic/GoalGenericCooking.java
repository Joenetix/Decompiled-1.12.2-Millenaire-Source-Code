package org.millenaire.common.goal.generic;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.core.entity.TileEntityFirePit;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;

public class GoalGenericCooking extends GoalGeneric {
    public static final String GOAL_TYPE = "cooking";

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM)
    @ConfigAnnotations.FieldDocumentation(explanation = "The item to be cooked.")
    public InvItem itemToCook = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "16")
    @ConfigAnnotations.FieldDocumentation(explanation = "Minimum number of items that can be added to a cooking.")
    public int minimumToCook;

    @Override
    public void applyDefaultSettings() {
        this.lookAtGoal = true;
    }

    @Override
    public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
        for (Building dest : this.getBuildings(villager)) {
            if (this.isDestPossible(villager, dest)) {
                // TODO: Implement countGoods
                /*
                 * int countGoods = dest.countGoods(this.itemToCook) +
                 * villager.countInv(this.itemToCook);
                 * 
                 * for (Point p : dest.getResManager().furnaces) {
                 * BlockEntity tileEntity = p.getBlockEntity(villager.getLevel());
                 * if (tileEntity instanceof AbstractFurnaceBlockEntity) {
                 * AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) tileEntity;
                 * if (countGoods >= this.minimumToCook && (furnace.getItem(0).isEmpty() ||
                 * (furnace.getItem(0).getItem() == this.itemToCook.getItem() &&
                 * furnace.getItem(0).getCount() < 32))) {
                 * return this.packDest(p, dest);
                 * }
                 * 
                 * // Check output slot (2)
                 * if (!furnace.getItem(2).isEmpty() && furnace.getItem(2).getCount() >=
                 * this.minimumToCook) {
                 * return this.packDest(p, dest);
                 * }
                 * }
                 * }
                 * 
                 * // TODO: Implement FirePit logic similarly if FirePit is ported
                 */
            }
        }

        return null;
    }

    @Override
    public ItemStack getIcon() {
        if (this.icon != null) {
            // return this.icon.getItemStack();
            return ItemStack.EMPTY;
        } else {
            // return this.itemToCook != null ? this.itemToCook.getItemStack() : null;
            return ItemStack.EMPTY;
        }
    }

    @Override
    public String getTypeLabel() {
        return "cooking";
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
        // Implementation stubbed for now until TileEntityFirePit and Furnace
        // interactions are verified
        return true;
    }

    private void performAction_firepit(Building dest, TileEntityFirePit firepit, MillVillager villager) {
        // Logic ported from decompiled source would go here
    }

    private void performAction_furnace(Building dest, AbstractFurnaceBlockEntity furnace, MillVillager villager) {
        // Logic ported from decompiled source would go here
    }

    @Override
    public boolean validateGoal() {
        if (this.itemToCook == null) {
            MillLog.error(this, "The itemtocook id is mandatory in custom cooking goals.");
            return false;
        } else {
            return true;
        }
    }
}
