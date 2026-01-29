package org.millenaire.common.goal.generic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.entity.TileEntityFirePit;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.BlockItemUtilities;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;

public class GoalGenericTendFurnace extends GoalGeneric {
   public static final String GOAL_TYPE = "tendfurnace";
   private static ItemStack[][] PLANKS = new ItemStack[][]{
      {new ItemStack(Blocks.PLANKS, 1, 0)},
      {new ItemStack(Blocks.PLANKS, 1, 1)},
      {new ItemStack(Blocks.PLANKS, 1, 2)},
      {new ItemStack(Blocks.PLANKS, 1, 3)},
      {new ItemStack(Blocks.PLANKS, 1, 4)},
      {new ItemStack(Blocks.PLANKS, 1, 5)}
   };
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "4")
   @ConfigAnnotations.FieldDocumentation(explanation = "Minimum number of wood to put back in one go.")
   public int minimumFuel;

   @Override
   public void applyDefaultSettings() {
      this.lookAtGoal = true;
      this.icon = InvItem.createInvItem(Blocks.FURNACE);
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      for (Building dest : this.getBuildings(villager)) {
         if (this.isDestPossible(villager, dest)) {
            int anyWoodAvailable = dest.countGoods(Blocks.LOG, -1)
               + villager.countInv(Blocks.LOG, -1)
               + villager.getHouse().countGoods(Blocks.LOG, -1);

            for (Point p : dest.getResManager().furnaces) {
               TileEntityFurnace furnace = p.getFurnace(villager.world);
               if (furnace != null) {
                  if (furnace.getStackInSlot(1) == ItemStack.EMPTY && anyWoodAvailable > 4) {
                     return this.packDest(p, dest);
                  }

                  if (furnace.getStackInSlot(1).getCount() < 32 && furnace.getStackInSlot(1).getItem() == Item.getItemFromBlock(Blocks.PLANKS)) {
                     int woodMeta = furnace.getStackInSlot(1).getMetadata();
                     int woodAvailable = this.getWoodCountByMeta(villager, dest, woodMeta);
                     if (woodAvailable >= this.minimumFuel) {
                        return this.packDest(p, dest);
                     }
                  }
               }
            }

            for (Point px : dest.getResManager().firepits) {
               TileEntityFirePit firepit = px.getFirePit(villager.world);
               if (firepit != null) {
                  ItemStack stack = firepit.fuel.getStackInSlot(0);
                  if (stack.isEmpty() && anyWoodAvailable > 4) {
                     return this.packDest(px, dest);
                  }

                  if (stack.getCount() < 32 && stack.getItem() == Item.getItemFromBlock(Blocks.PLANKS)) {
                     int woodMeta = stack.getMetadata();
                     int woodAvailable = this.getWoodCountByMeta(villager, dest, woodMeta);
                     if (woodAvailable > 4) {
                        return this.packDest(px, dest);
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   @Override
   public ItemStack[] getHeldItemsTravelling(MillVillager villager) {
      Building dest = villager.getGoalBuildingDest();
      TileEntity tileEntity = villager.getGoalDestPoint().getTileEntity(villager.world);
      if (dest != null && tileEntity != null) {
         if (tileEntity instanceof TileEntityFurnace) {
            TileEntityFurnace furnace = (TileEntityFurnace)tileEntity;
            if (furnace.getStackInSlot(1) == ItemStack.EMPTY) {
               int mostWoodAvailable = 0;
               int mostWoodAvailableMeta = -1;

               for (int woodMeta = 0; woodMeta < 6; woodMeta++) {
                  int woodAvailable = this.getWoodCountByMeta(villager, dest, woodMeta);
                  if (woodAvailable > mostWoodAvailable) {
                     mostWoodAvailable = woodAvailable;
                     mostWoodAvailableMeta = woodMeta;
                  }
               }

               if (mostWoodAvailableMeta > -1) {
                  return PLANKS[mostWoodAvailableMeta];
               }
            } else if (furnace.getStackInSlot(1).getCount() < 64 && furnace.getStackInSlot(1).getItem() == Item.getItemFromBlock(Blocks.PLANKS)) {
               int woodMetax = furnace.getStackInSlot(1).getMetadata();
               return PLANKS[woodMetax];
            }
         } else if (tileEntity instanceof TileEntityFirePit) {
            TileEntityFirePit firepit = (TileEntityFirePit)tileEntity;
            if (firepit.fuel.getStackInSlot(0) == ItemStack.EMPTY) {
               int mostWoodAvailable = 0;
               int mostWoodAvailableMeta = 0;

               for (int woodMetax = 0; woodMetax < 6; woodMetax++) {
                  int woodAvailable = this.getWoodCountByMeta(villager, dest, woodMetax);
                  if (woodAvailable > mostWoodAvailable) {
                     mostWoodAvailable = woodAvailable;
                     mostWoodAvailableMeta = woodMetax;
                  }
               }

               return PLANKS[mostWoodAvailableMeta];
            }

            if (firepit.fuel.getStackInSlot(0).getCount() < 64
               && firepit.fuel.getStackInSlot(0).getItem() == Item.getItemFromBlock(Blocks.PLANKS)) {
               int woodMetaxx = firepit.fuel.getStackInSlot(0).getMetadata();
               return PLANKS[woodMetaxx];
            }
         }
      }

      return null;
   }

   @Override
   public String getTypeLabel() {
      return "tendfurnace";
   }

   private int getWoodCountByMeta(MillVillager villager, Building dest, int woodMeta) {
      IBlockState logsToTake = BlockItemUtilities.getLogBlockstateFromPlankMeta(woodMeta);
      return dest.countGoods(logsToTake) + villager.countInv(logsToTake) + villager.getHouse().countGoods(logsToTake);
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
      Building dest = villager.getGoalBuildingDest();
      TileEntity tileEntity = villager.getGoalDestPoint().getTileEntity(villager.world);
      if (dest != null && tileEntity != null) {
         if (tileEntity instanceof TileEntityFurnace) {
            this.performAction_furnace(villager, (TileEntityFurnace)tileEntity, dest);
         } else if (tileEntity instanceof TileEntityFirePit) {
            this.performAction_firepit(villager, (TileEntityFirePit)tileEntity, dest);
         }
      }

      return true;
   }

   private void performAction_firepit(MillVillager villager, TileEntityFirePit firepit, Building dest) {
      if (firepit.fuel.getStackInSlot(0).isEmpty()) {
         int mostWoodAvailable = 0;
         int mostWoodAvailableMeta = -1;

         for (int woodMeta = 0; woodMeta < 6; woodMeta++) {
            int woodAvailable = this.getWoodCountByMeta(villager, dest, woodMeta);
            if (woodAvailable > mostWoodAvailable) {
               mostWoodAvailable = woodAvailable;
               mostWoodAvailableMeta = woodMeta;
            }
         }

         int nbplanks = Math.min(64, mostWoodAvailable * 4);
         firepit.fuel.setStackInSlot(0, new ItemStack(Blocks.PLANKS, nbplanks, mostWoodAvailableMeta));
         IBlockState logsToTake = BlockItemUtilities.getLogBlockstateFromPlankMeta(mostWoodAvailableMeta);
         int nbTaken = dest.takeGoods(logsToTake, nbplanks / 4);
         if (nbTaken < nbplanks / 4) {
            nbTaken += villager.takeFromInv(logsToTake, nbplanks / 4 - nbTaken);
         }

         if (nbTaken < nbplanks / 4) {
            nbTaken += villager.getHouse().takeGoods(logsToTake, nbplanks / 4 - nbTaken);
         }
      } else if (firepit.fuel.getStackInSlot(0).getCount() < 64
         && firepit.fuel.getStackInSlot(0).getItem() == Item.getItemFromBlock(Blocks.PLANKS)) {
         int woodMetax = firepit.fuel.getStackInSlot(0).getMetadata();
         IBlockState logsToTakex = BlockItemUtilities.getLogBlockstateFromPlankMeta(woodMetax);
         int woodAvailable = this.getWoodCountByMeta(villager, dest, woodMetax);
         int nbplanksx = Math.min(64 - firepit.fuel.getStackInSlot(0).getCount(), woodAvailable * 4);
         firepit.fuel.setStackInSlot(1, new ItemStack(Blocks.PLANKS, firepit.fuel.getStackInSlot(0).getCount() + nbplanksx, woodMetax));
         int nbTakenx = dest.takeGoods(logsToTakex, nbplanksx / 4);
         if (nbTakenx < nbplanksx / 4) {
            nbTakenx += villager.takeFromInv(logsToTakex, nbplanksx / 4 - nbTakenx);
         }

         if (nbTakenx < nbplanksx / 4) {
            nbTakenx += villager.getHouse().takeGoods(logsToTakex, nbplanksx / 4 - nbTakenx);
         }
      }
   }

   private void performAction_furnace(MillVillager villager, TileEntityFurnace furnace, Building dest) {
      if (furnace.getStackInSlot(1) == ItemStack.EMPTY) {
         int mostWoodAvailable = 0;
         int mostWoodAvailableMeta = -1;

         for (int woodMeta = 0; woodMeta < 6; woodMeta++) {
            int woodAvailable = this.getWoodCountByMeta(villager, dest, woodMeta);
            if (woodAvailable > mostWoodAvailable) {
               mostWoodAvailable = woodAvailable;
               mostWoodAvailableMeta = woodMeta;
            }
         }

         int nbplanks = Math.min(64, mostWoodAvailable * 4);
         furnace.setInventorySlotContents(1, new ItemStack(Blocks.PLANKS, nbplanks, mostWoodAvailableMeta));
         IBlockState logsToTake = BlockItemUtilities.getLogBlockstateFromPlankMeta(mostWoodAvailableMeta);
         int nbTaken = dest.takeGoods(logsToTake, nbplanks / 4);
         if (nbTaken < nbplanks / 4) {
            nbTaken += villager.takeFromInv(logsToTake, nbplanks / 4 - nbTaken);
         }

         if (nbTaken < nbplanks / 4) {
            nbTaken += villager.getHouse().takeGoods(logsToTake, nbplanks / 4 - nbTaken);
         }
      } else if (furnace.getStackInSlot(1).getCount() < 64 && furnace.getStackInSlot(1).getItem() == Item.getItemFromBlock(Blocks.PLANKS)) {
         int woodMetax = furnace.getStackInSlot(1).getMetadata();
         IBlockState logsToTakex = BlockItemUtilities.getLogBlockstateFromPlankMeta(woodMetax);
         int woodAvailable = this.getWoodCountByMeta(villager, dest, woodMetax);
         int nbplanksx = Math.min(64 - furnace.getStackInSlot(1).getCount(), woodAvailable * 4);
         furnace.setInventorySlotContents(1, new ItemStack(Blocks.PLANKS, furnace.getStackInSlot(1).getCount() + nbplanksx, woodMetax));
         int nbTakenx = dest.takeGoods(logsToTakex, nbplanksx / 4);
         if (nbTakenx < nbplanksx / 4) {
            nbTakenx += villager.takeFromInv(logsToTakex, nbplanksx / 4 - nbTakenx);
         }

         if (nbTakenx < nbplanksx / 4) {
            nbTakenx += villager.getHouse().takeGoods(logsToTakex, nbplanksx / 4 - nbTakenx);
         }
      }
   }

   @Override
   public boolean validateGoal() {
      return true;
   }
}
