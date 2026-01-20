package org.millenaire.common.goal.generic;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.AABB;

import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.utilities.WorldUtilities;
import org.millenaire.common.village.Building;

public class GoalGenericSlaughterAnimal extends GoalGeneric {
    public static final String GOAL_TYPE = "slaughteranimal";

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.ENTITY_ID)
    @ConfigAnnotations.FieldDocumentation(explanation = "The animal to be targeted.")
    public ResourceLocation animalKey = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BONUS_ITEM_ADD)
    @ConfigAnnotations.FieldDocumentation(explanation = "Extra item drop the villager can get.")
    public List<AnnotedParameter.BonusItem> bonusItem = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN)
    @ConfigAnnotations.FieldDocumentation(explanation = "If true, the villager will slaughter animals until only half the reference amount (the number of spawn points) is left.")
    public boolean aggressiveSlaughter = false;

    @Override
    public void applyDefaultSettings() {
        this.duration = 2;
        this.lookAtGoal = true;
        // this.icon = InvItem.createInvItem(Items.IRON_SWORD);
    }

    @Override
    public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
        // Logic stubbed
        return null;
    }

    @Override
    public String getTypeLabel() {
        return "slaughteranimal";
    }

    @Override
    public boolean isDestPossibleSpecific(MillVillager villager, Building b) {
        // Logic stubbed
        return true;
    }

    @Override
    public boolean isFightingGoal() {
        return true;
    }

    @Override
    public boolean isPossibleGenericGoal(MillVillager villager) throws Exception {
        return this.getDestination(villager) != null;
    }

    @Override
    public boolean performAction(MillVillager villager) throws Exception {
        Building dest = villager.getGoalBuildingDest();
        if (dest == null) {
            return true;
        } else {
            // Logic stubbed
            return true;
        }
    }

    @Override
    public int range(MillVillager villager) {
        return 1;
    }

    @Override
    public boolean validateGoal() {
        if (this.animalKey == null) {
            MillLog.error(this, "The animalKey is mandatory in custom slaughter goals.");
            return false;
        } else {
            return true;
        }
    }
}
