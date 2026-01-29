package org.millenaire.common.goal.generic;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockDoublePlant.EnumBlockHalf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.block.BlockGrapeVine;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.BlockItemUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;

public class GoalGenericPlantCrop extends GoalGeneric {
   public static final String GOAL_TYPE = "planting";
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BLOCK_ID)
   @ConfigAnnotations.FieldDocumentation(explanation = "Type of plant to plant.")
   public ResourceLocation cropType = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BLOCKSTATE_ADD)
   @ConfigAnnotations.FieldDocumentation(explanation = "Blockstate to plant. If not set, defaults to cropType. If more than one set, picks one at random.")
   public List<IBlockState> plantBlockState = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM)
   @ConfigAnnotations.FieldDocumentation(explanation = "Seed item that gets consumed when planting.")
   public InvItem seed = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BLOCK_ID, defaultValue = "minecraft:farmland")
   @ConfigAnnotations.FieldDocumentation(explanation = "Block to set below the crop.")
   public ResourceLocation soilType = null;

   public static int getCropBlockMeta(ResourceLocation cropType2) {
      return 0;
   }

   @Override
   public void applyDefaultSettings() {
      this.duration = 2;
      this.lookAtGoal = true;
      this.tags.add("tag_agriculture");
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws MillLog.MillenaireException {
      Point dest = null;
      Building destBuilding = null;

      for (Building buildingDest : this.getBuildings(villager)) {
         if (this.isDestPossible(villager, buildingDest)) {
            List<Point> soils = buildingDest.getResManager().getSoilPoints(this.cropType);
            if (soils != null) {
               for (Point p : soils) {
                  if (this.isValidPlantingLocation(villager.world, p) && (dest == null || p.distanceTo(villager) < dest.distanceTo(villager))) {
                     dest = p.getAbove();
                     destBuilding = buildingDest;
                  }
               }
            }
         }
      }

      return dest == null ? null : this.packDest(dest, destBuilding);
   }

   @Override
   public ItemStack getIcon() {
      if (this.icon != null) {
         return this.icon.getItemStack();
      } else if (this.seed != null) {
         return this.seed.getItemStack();
      } else {
         return this.heldItems != null && this.heldItems.length > 0 ? this.heldItems[0] : null;
      }
   }

   @Override
   public String getTypeLabel() {
      return "planting";
   }

   @Override
   public boolean isDestPossibleSpecific(MillVillager villager, Building b) {
      return this.seed == null || b.countGoods(this.seed) + villager.countInv(this.seed) != 0;
   }

   @Override
   public boolean isPossibleGenericGoal(MillVillager villager) throws Exception {
      return this.getDestination(villager) != null;
   }

   private boolean isValidPlantingLocation(World world, Point p) {
      Block blockTwoAbove = p.getAbove().getAbove().getBlock(world);
      Block blockAbove = p.getAbove().getBlock(world);
      Block farmBlock = p.getBlock(world);
      if (blockAbove != Blocks.AIR && blockAbove != Blocks.SNOW && blockAbove != Blocks.LEAVES
         || blockTwoAbove != Blocks.AIR && blockTwoAbove != Blocks.SNOW && blockTwoAbove != Blocks.LEAVES
         || farmBlock != Blocks.GRASS && farmBlock != Blocks.DIRT && farmBlock != Blocks.FARMLAND) {
         if (BlockItemUtilities.isBlockDecorativePlant(blockAbove)) {
            if (!this.cropType.equals(Mill.CROP_FLOWER)) {
               return true;
            }

            if (blockAbove != Blocks.RED_FLOWER && blockAbove != Blocks.YELLOW_FLOWER && blockAbove != Blocks.DOUBLE_PLANT) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   @Override
   public boolean performAction(MillVillager villager) {
      Building dest = villager.getGoalBuildingDest();
      if (dest == null) {
         return true;
      } else if (!this.isValidPlantingLocation(villager.world, villager.getGoalDestPoint().getBelow())) {
         return true;
      } else {
         if (this.seed != null) {
            int taken = villager.takeFromInv(this.seed, 1);
            if (taken == 0) {
               dest.takeGoods(this.seed, 1);
            }
         }

         Block soil = (Block)Block.REGISTRY.getObject(this.soilType);
         if (villager.getGoalDestPoint().getBelow().getBlock(villager.world) != soil) {
            villager.setBlockAndMetadata(villager.getGoalDestPoint().getBelow(), soil, 0);
         }

         if (!this.plantBlockState.isEmpty()) {
            IBlockState cropState = this.plantBlockState.get(MillCommonUtilities.randomInt(this.plantBlockState.size()));
            villager.setBlockstate(villager.getGoalDestPoint(), cropState);
            if (cropState.getBlock() instanceof BlockDoublePlant) {
               villager.setBlockstate(villager.getGoalDestPoint().getAbove(), cropState.withProperty(BlockDoublePlant.HALF, EnumBlockHalf.UPPER));
            }
         } else {
            Block cropBlock = (Block)Block.REGISTRY.getObject(this.cropType);
            int cropMeta = getCropBlockMeta(this.cropType);
            villager.setBlockAndMetadata(villager.getGoalDestPoint(), cropBlock, cropMeta);
            if (cropBlock instanceof BlockDoublePlant || cropBlock instanceof BlockGrapeVine) {
               villager.setBlockAndMetadata(villager.getGoalDestPoint().getAbove(), cropBlock, cropMeta | 8);
            }
         }

         villager.swingArm(EnumHand.MAIN_HAND);
         if (this.isDestPossibleSpecific(villager, villager.getGoalBuildingDest())) {
            try {
               villager.setGoalInformation(this.getDestination(villager));
            } catch (MillLog.MillenaireException var6) {
               MillLog.printException(var6);
            }

            return false;
         } else {
            return true;
         }
      }
   }

   @Override
   public int priority(MillVillager villager) throws MillLog.MillenaireException {
      Goal.GoalInformation info = this.getDestination(villager);
      return info != null && info.getDest() != null ? (int)(100.0 - villager.getPos().distanceTo(info.getDest())) : -1;
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
