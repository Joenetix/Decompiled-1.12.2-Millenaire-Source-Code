package org.millenaire.common.goal;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.world.World;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;

@DocumentedElement.Documentation("Go to sleep at home at night. This goal is auto-added to all villagers.")
public class GoalSleep extends Goal {
   public GoalSleep() {
      this.travelBookShow = false;
      this.sprint = false;
   }

   @Override
   public int actionDuration(MillVillager villager) throws Exception {
      return 10;
   }

   @Override
   public boolean allowRandomMoves() throws Exception {
      return false;
   }

   @Override
   public boolean canBeDoneAtNight() {
      return true;
   }

   @Override
   public boolean canBeDoneInDayTime() {
      return false;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      World world = villager.world;
      Point sleepPos = villager.getHouse().getResManager().getSleepingPos();
      List<Point> beds = new ArrayList<>();

      for (int xDelta = 0; xDelta < 6; xDelta++) {
         for (int yDelta = 0; yDelta < 6; yDelta++) {
            for (int zDelta = 0; zDelta < 6; zDelta++) {
               for (int l = 0; l < 8; l++) {
                  Point p = sleepPos.getRelative(xDelta * (1 - (l & 1) * 2), yDelta * (1 - (l & 2)), zDelta * (1 - (l & 4) / 2));
                  Block block = WorldUtilities.getBlock(world, p);
                  if (block instanceof BlockBed) {
                     int meta = WorldUtilities.getBlockMeta(world, p);
                     if ((meta & 8) == 0) {
                        boolean alreadyTaken = false;

                        for (MillVillager v : villager.getHouse().getKnownVillagers()) {
                           if (v != villager && v.getGoalDestPoint() != null && v.getGoalDestPoint().equals(p)) {
                              alreadyTaken = true;
                           }
                        }

                        if (!alreadyTaken) {
                           beds.add(p);
                        }
                     }
                  }
               }
            }
         }
      }

      if (beds.size() > 0) {
         return this.packDest(beds.get(0), villager.getHouse());
      } else {
         List<Point> feetPos = new ArrayList<>();

         for (int xDelta = 0; xDelta < 6; xDelta++) {
            for (int yDelta = 0; yDelta < 6; yDelta++) {
               for (int zDelta = 0; zDelta < 6; zDelta++) {
                  for (int lx = 0; lx < 8; lx++) {
                     Point p = sleepPos.getRelative(xDelta * (1 - (lx & 1) * 2), yDelta * (1 - (lx & 2)), zDelta * (1 - (lx & 4) / 2));
                     if (!p.isBlockPassable(world) && p.getAbove().isBlockPassable(world) && p.getRelative(0.0, 2.0, 0.0).isBlockPassable(world)) {
                        Point topBlock = WorldUtilities.findTopNonPassableBlock(world, p.getiX(), p.getiZ());
                        if (topBlock != null && topBlock.y > p.y + 1.0) {
                           float angle = villager.getBedOrientationInDegrees();
                           int dx = 0;
                           int dz = 0;
                           if (angle == 0.0F) {
                              dx = 1;
                           } else if (angle == 90.0F) {
                              dz = 1;
                           } else if (angle == 180.0F) {
                              dx = -1;
                           } else if (angle == 270.0F) {
                              dz = -1;
                           }

                           Point p2 = p.getRelative(dx, 0.0, dz);
                           if (!p2.isBlockPassable(world) && p2.getAbove().isBlockPassable(world) && p2.getRelative(0.0, 2.0, 0.0).isBlockPassable(world)) {
                              topBlock = WorldUtilities.findTopNonPassableBlock(world, p2.getiX(), p2.getiZ());
                              if (topBlock != null && topBlock.y > p2.y + 1.0) {
                                 p = p.getAbove();
                                 boolean alreadyTaken = false;

                                 for (MillVillager vx : villager.getHouse().getKnownVillagers()) {
                                    if (vx != villager && vx.getGoalDestPoint() != null) {
                                       if (vx.getGoalDestPoint().equals(p)) {
                                          alreadyTaken = true;
                                       }

                                       if (vx.getGoalDestPoint().equals(p.getRelative(1.0, 0.0, 0.0))) {
                                          alreadyTaken = true;
                                       }

                                       if (vx.getGoalDestPoint().equals(p.getRelative(0.0, 0.0, 1.0))) {
                                          alreadyTaken = true;
                                       }

                                       if (vx.getGoalDestPoint().equals(p.getRelative(-1.0, 0.0, 0.0))) {
                                          alreadyTaken = true;
                                       }

                                       if (vx.getGoalDestPoint().equals(p.getRelative(0.0, 0.0, -1.0))) {
                                          alreadyTaken = true;
                                       }
                                    }
                                 }

                                 if (!alreadyTaken) {
                                    feetPos.add(p);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         for (MillVillager vxx : villager.getHouse().getKnownVillagers()) {
            if (vxx != villager && vxx.getGoalDestPoint() != null) {
               feetPos.remove(vxx.getGoalDestPoint());
               feetPos.remove(vxx.getGoalDestPoint().getRelative(1.0, 0.0, 0.0));
               feetPos.remove(vxx.getGoalDestPoint().getRelative(0.0, 0.0, 1.0));
               feetPos.remove(vxx.getGoalDestPoint().getRelative(-1.0, 0.0, 0.0));
               feetPos.remove(vxx.getGoalDestPoint().getRelative(0.0, 0.0, -1.0));
            }
         }

         return feetPos.size() > 0 ? this.packDest(feetPos.get(0), villager.getHouse()) : this.packDest(sleepPos, villager.getHouse());
      }
   }

   @Override
   public String labelKeyWhileTravelling(MillVillager villager) {
      return this.key + "_travelling";
   }

   @Override
   public boolean performAction(MillVillager villager) throws Exception {
      if (!villager.nightActionPerformed) {
         villager.nightActionPerformed = villager.performNightAction();
      }

      villager.shouldLieDown = true;
      float angle = villager.getBedOrientationInDegrees();
      double dx = 0.5;
      double dz = 0.5;
      double fdx = 0.0;
      double fdz = 0.0;
      if (angle == 0.0F) {
         dx = 0.95;
         fdx = -10.0;
      } else if (angle == 90.0F) {
         dz = 0.95;
         fdz = -10.0;
      } else if (angle == 180.0F) {
         dx = 0.05;
         fdx = 10.0;
      } else if (angle == 270.0F) {
         dz = 0.05;
         fdz = 10.0;
      }

      float floatingHeight;
      if (villager.getBlock(villager.getGoalDestPoint()) instanceof BlockBed) {
         floatingHeight = 0.7F;
      } else {
         floatingHeight = 0.2F;
      }

      villager.setPosition(villager.getGoalDestPoint().x + dx, villager.getGoalDestPoint().y + floatingHeight, villager.getGoalDestPoint().z + dz);
      villager.facePoint(villager.getPos().getRelative(fdx, 1.0, fdz), 100.0F, 100.0F);
      return false;
   }

   @Override
   public int priority(MillVillager villager) throws Exception {
      return 50;
   }

   @Override
   public int range(MillVillager villager) {
      return 2;
   }

   @Override
   public boolean shouldVillagerLieDown() {
      return true;
   }
}
