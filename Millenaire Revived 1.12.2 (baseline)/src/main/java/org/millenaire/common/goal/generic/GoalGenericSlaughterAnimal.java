package org.millenaire.common.goal.generic;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.pathing.atomicstryker.AStarConfig;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;
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
   @ConfigAnnotations.FieldDocumentation(
      explanation = "If true, the villager will slaughter animals until only half the reference amount (the number of spawn points) is left."
   )
   public boolean aggressiveSlaughter = false;

   @Override
   public void applyDefaultSettings() {
      this.duration = 2;
      this.lookAtGoal = true;
      this.icon = InvItem.createInvItem(Items.IRON_AXE);
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      Point pos = villager.getPos();
      Entity closest = null;
      Building destBuilding = null;
      double bestDist = Double.MAX_VALUE;

      for (Building dest : this.getBuildings(villager)) {
         if (this.isDestPossible(villager, dest)) {
            for (Entity ent : WorldUtilities.getEntitiesWithinAABB(villager.world, EntityList.getClass(this.animalKey), dest.getPos(), 15, 10)) {
               if (!ent.isDead && !this.isEntityChild(ent) && (closest == null || pos.distanceTo(ent) < bestDist)) {
                  closest = ent;
                  destBuilding = dest;
                  bestDist = pos.distanceTo(ent);
               }
            }
         }
      }

      return closest == null ? null : this.packDest(null, destBuilding, closest);
   }

   @Override
   public AStarConfig getPathingConfig(MillVillager villager) {
      if (!villager.canVillagerClearLeaves()) {
         return this.animalKey.equals(Mill.ENTITY_SQUID) ? JPS_CONFIG_SLAUGHTERSQUIDS_NO_LEAVES : JPS_CONFIG_TIGHT_NO_LEAVES;
      } else {
         return this.animalKey.equals(Mill.ENTITY_SQUID) ? JPS_CONFIG_SLAUGHTERSQUIDS : JPS_CONFIG_TIGHT;
      }
   }

   @Override
   public String getTypeLabel() {
      return "slaughteranimal";
   }

   @Override
   public boolean isDestPossibleSpecific(MillVillager villager, Building b) {
      List<Entity> animals = WorldUtilities.getEntitiesWithinAABB(villager.world, EntityList.getClass(this.animalKey), b.getPos(), 25, 10);
      if (animals == null) {
         return false;
      } else {
         int nbanimals = 0;

         for (Entity ent : animals) {
            if (!ent.isDead && !this.isEntityChild(ent)) {
               nbanimals++;
            }
         }

         int targetAnimals = 0;

         for (int i = 0; i < b.getResManager().spawns.size(); i++) {
            if (b.getResManager().spawnTypes.get(i).equals(this.animalKey)) {
               targetAnimals = b.getResManager().spawns.get(i).size();
            }
         }

         return !this.aggressiveSlaughter ? nbanimals > targetAnimals : nbanimals > targetAnimals / 2;
      }
   }

   private boolean isEntityChild(Entity ent) {
      if (!(ent instanceof EntityAgeable)) {
         return false;
      } else {
         EntityAgeable animal = (EntityAgeable)ent;
         return animal.isChild();
      }
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
         for (Entity ent : WorldUtilities.getEntitiesWithinAABB(villager.world, EntityList.getClass(this.animalKey), villager.getPos(), 1, 5)) {
            if (!ent.isDead && ent instanceof EntityLivingBase && !this.isEntityChild(ent) && villager.canEntityBeSeen(ent)) {
               EntityLivingBase entLiving = (EntityLivingBase)ent;
               villager.setAttackTarget(entLiving);

               for (AnnotedParameter.BonusItem bonusItem : this.bonusItem) {
                  if ((bonusItem.tag == null || dest.containsTags(bonusItem.tag)) && MillCommonUtilities.randomInt(100) <= bonusItem.chance) {
                     villager.addToInv(bonusItem.item, 1);
                  }
               }

               villager.swingArm(EnumHand.MAIN_HAND);
               return true;
            }
         }

         for (Entity entx : WorldUtilities.getEntitiesWithinAABB(villager.world, EntityList.getClass(this.animalKey), villager.getPos(), 2, 5)) {
            if (!entx.isDead && entx instanceof EntityLivingBase && !this.isEntityChild(entx) && villager.canEntityBeSeen(entx)) {
               EntityLivingBase entLiving = (EntityLivingBase)entx;
               villager.setAttackTarget(entLiving);

               for (AnnotedParameter.BonusItem bonusItemx : this.bonusItem) {
                  if ((bonusItemx.tag == null || dest.containsTags(bonusItemx.tag)) && MillCommonUtilities.randomInt(100) <= bonusItemx.chance) {
                     villager.addToInv(bonusItemx.item, 1);
                  }
               }

               villager.swingArm(EnumHand.MAIN_HAND);
               return true;
            }
         }

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
