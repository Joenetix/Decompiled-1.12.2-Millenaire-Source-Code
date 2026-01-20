package org.millenaire.common.goal.generic;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

public class GoalGenericPlantCrop extends GoalGeneric {
    public static final String GOAL_TYPE = "planting";

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BLOCK_ID)
    @ConfigAnnotations.FieldDocumentation(explanation = "Type of plant to plant.")
    public ResourceLocation cropType = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BLOCKSTATE_ADD)
    @ConfigAnnotations.FieldDocumentation(explanation = "Blockstate to plant. If not set, defaults to cropType. If more than one set, picks one at random.")
    public List<BlockState> plantBlockState = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM)
    @ConfigAnnotations.FieldDocumentation(explanation = "Seed item that gets consumed when planting.")
    public InvItem seed = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BLOCK_ID, defaultValue = "minecraft:farmland")
    @ConfigAnnotations.FieldDocumentation(explanation = "Block to set below the crop.")
    public ResourceLocation soilType = null;

    @Override
    public void applyDefaultSettings() {
        this.duration = 2;
        this.lookAtGoal = true;
        this.tags.add("tag_agriculture");
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
        } else if (this.seed != null) {
            // return this.seed.getItemStack();
            return ItemStack.EMPTY;
        } else {
            return this.heldItems != null && this.heldItems.length > 0 ? this.heldItems[0] : ItemStack.EMPTY;
        }
    }

    @Override
    public String getTypeLabel() {
        return "planting";
    }

    @Override
    public boolean isDestPossibleSpecific(MillVillager villager, Building b) {
        // return this.seed == null || b.countGoods(this.seed) +
        // villager.countInv(this.seed) != 0;
        return true;
    }

    @Override
    public boolean isPossibleGenericGoal(MillVillager villager) throws Exception {
        return this.getDestination(villager) != null;
    }

    @Override
    public boolean performAction(MillVillager villager) {
        // Logic stubbed
        return true;
    }

    @Override
    public boolean validateGoal() {
        if (this.cropType == null) {
            MillLog.error(this, "The croptype is mandatory in custom planting goals.");
            return false;
        } else {
            return true;
        }
    }
}
