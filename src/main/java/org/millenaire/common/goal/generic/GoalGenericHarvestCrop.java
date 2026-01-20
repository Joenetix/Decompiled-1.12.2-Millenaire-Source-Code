package org.millenaire.common.goal.generic;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.registries.BuiltInRegistries;

import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;

public class GoalGenericHarvestCrop extends GoalGeneric {
    public static final String GOAL_TYPE = "harvesting";

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BLOCK_ID)
    @ConfigAnnotations.FieldDocumentation(explanation = "Type of plant to harvest.")
    public ResourceLocation cropType = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BONUS_ITEM_ADD)
    @ConfigAnnotations.FieldDocumentation(explanation = "Item to be harvested, with chance.")
    public List<AnnotedParameter.BonusItem> harvestItem = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM)
    @ConfigAnnotations.FieldDocumentation(explanation = "Boons for irrigated villages.")
    public InvItem irrigationBonusCrop = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BLOCKSTATE)
    @ConfigAnnotations.FieldDocumentation(explanation = "Blockstate the crop must have to be harvested. If not set, must have a meta of 7.")
    public BlockState harvestBlockState = null;

    public static int getCropBlockRipeMeta(ResourceLocation cropType) {
        return 7; // Usually CropBlock.MAX_AGE which is 7
    }

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
    public ItemStack[] getHeldItemsTravelling(MillVillager villager) throws Exception {
        return new ItemStack[] { villager.getBestHoeStack() };
    }

    @Override
    public ItemStack getIcon() {
        if (this.icon != null) {
            // return this.icon.getItemStack();
            return ItemStack.EMPTY;
        } else {
            // return !this.harvestItem.isEmpty() ?
            // this.harvestItem.get(0).item.getItemStack() : null;
            return ItemStack.EMPTY;
        }
    }

    @Override
    public String getTypeLabel() {
        return "harvesting";
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
        return true;
    }

    @Override
    public int priority(MillVillager villager) throws Exception {
        Goal.GoalInformation info = this.getDestination(villager);
        return info != null && info.getDest() != null
                ? (int) (1000.0 - villager.getPos().distanceToSquared(info.getDest().x,
                        info.getDest().y, info.getDest().z))
                : -1;
    }

    @Override
    public boolean validateGoal() {
        if (this.cropType == null) {
            MillLog.error(this, "The croptype is mandatory in custom harvest goals.");
            return false;
        } else {
            return true;
        }
    }
}
