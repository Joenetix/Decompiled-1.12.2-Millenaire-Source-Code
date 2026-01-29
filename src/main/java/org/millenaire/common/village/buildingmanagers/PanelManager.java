package org.millenaire.common.village.buildingmanagers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWallSign;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.entity.TileEntityPanel;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.MillItems;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.BuildingProject;
import org.millenaire.common.village.ConstructionIP;
import org.millenaire.common.village.VillagerRecord;

public class PanelManager {
   public static final int MAX_LINE_NB = 8;
   private static ItemStack FLOWER_PINK = new ItemStack(Blocks.RED_FLOWER, 1, 7);
   private static ItemStack FLOWER_BLUE = new ItemStack(Blocks.RED_FLOWER, 1, 1);
   private static Map<Item, ItemStack> itemStacks = new HashMap<>();
   public long lastSignUpdate = 0L;
   private final Building building;
   private final Building townHall;

   private static ItemStack stackFromBlock(Block block) {
      Item item = Item.getItemFromBlock(block);
      return stackFromItem(item);
   }

   private static ItemStack stackFromItem(Item item) {
      if (itemStacks.containsKey(item)) {
         return itemStacks.get(item);
      } else {
         ItemStack stack = new ItemStack(item);
         itemStacks.put(item, stack);
         return stack;
      }
   }

   public PanelManager(Building building) {
      this.building = building;
      this.townHall = building.getTownHall();
   }

   public PanelManager.WallStatusInfos computeWallInfos(List<BuildingProject> projects, int wallLevel) {
      Map<InvItem, Integer> resCost = new HashMap<>();
      Map<InvItem, Integer> resHas = new HashMap<>();
      int segmentsDone = 0;
      int segmentsToDo = 0;
      String wallTag = "wall_level_" + wallLevel;

      for (BuildingProject project : projects) {
         BuildingPlan startingPlan = project.getPlan(0, 0);
         if (startingPlan != null && startingPlan.isWallSegment) {
            if (project.getExistingPlan() != null && project.getExistingPlan().containsTags(wallTag)) {
               for (InvItem key : project.getExistingPlan().resCost.keySet()) {
                  if (resCost.containsKey(key)) {
                     resCost.put(key, resCost.get(key) + project.getExistingPlan().resCost.get(key));
                     resHas.put(key, resHas.get(key) + project.getExistingPlan().resCost.get(key));
                  } else {
                     resCost.put(key, project.getExistingPlan().resCost.get(key));
                     resHas.put(key, project.getExistingPlan().resCost.get(key));
                  }
               }

               segmentsDone++;
            } else if (project.getNextBuildingPlan(false) != null && project.getNextBuildingPlan(false).containsTags(wallTag)) {
               for (InvItem keyx : project.getNextBuildingPlan(false).resCost.keySet()) {
                  if (resCost.containsKey(keyx)) {
                     resCost.put(keyx, resCost.get(keyx) + project.getNextBuildingPlan(false).resCost.get(keyx));
                  } else {
                     resCost.put(keyx, project.getNextBuildingPlan(false).resCost.get(keyx));
                     resHas.put(keyx, 0);
                  }
               }

               segmentsToDo++;
            }
         }
      }

      for (ConstructionIP cip : this.building.getConstructionsInProgress()) {
         if (cip.getBuildingLocation() != null && cip.isWallConstruction()) {
            BuildingPlan plan = cip.getBuildingLocation().getPlan();

            for (InvItem keyxx : plan.resCost.keySet()) {
               if (resCost.containsKey(keyxx)) {
                  resHas.put(keyxx, resHas.get(keyxx) + plan.resCost.get(keyxx));
               }
            }
         }
      }

      for (InvItem keyxxx : resCost.keySet()) {
         int availableInTh = this.building.countGoods(keyxxx.getItem(), keyxxx.meta);
         resHas.put(keyxxx, resHas.get(keyxxx) + availableInTh);
      }

      for (InvItem keyxxx : resCost.keySet()) {
         if (resHas.get(keyxxx) > resCost.get(keyxxx)) {
            resHas.put(keyxxx, resCost.get(keyxxx));
         }
      }

      List<PanelManager.ResourceLine> resources = new ArrayList<>();

      for (InvItem keyxxxx : resCost.keySet()) {
         resources.add(new PanelManager.ResourceLine(keyxxxx, resCost.get(keyxxxx), resHas.get(keyxxxx)));
      }

      Collections.sort(resources);
      return new PanelManager.WallStatusInfos(resources, segmentsDone, segmentsToDo);
   }

   private TileEntityPanel.PanelUntranslatedLine createEmptyLine() {
      return new TileEntityPanel.PanelUntranslatedLine();
   }

   private TileEntityPanel.PanelUntranslatedLine createFullLine(String fullLine, ItemStack leftIcon, ItemStack rightIcon) {
      return this.createFullLine(new String[]{fullLine}, leftIcon, rightIcon);
   }

   private TileEntityPanel.PanelUntranslatedLine createFullLine(String[] fullLine, ItemStack leftIcon, ItemStack rightIcon) {
      TileEntityPanel.PanelUntranslatedLine line = new TileEntityPanel.PanelUntranslatedLine();
      line.setFullLine(fullLine);
      line.leftIcon = leftIcon != null ? leftIcon : ItemStack.EMPTY;
      line.rightIcon = rightIcon != null ? rightIcon : ItemStack.EMPTY;
      return line;
   }

   private void generateResourceLines(List<PanelManager.ResourceLine> resources, List<TileEntityPanel.PanelUntranslatedLine> lines) {
      int resPos = 0;

      while (resPos < Math.min(resources.size(), (8 - lines.size()) * 2)) {
         PanelManager.ResourceLine resource = resources.get(resPos);
         if (resource.cost < 100) {
            TileEntityPanel.PanelUntranslatedLine line = new TileEntityPanel.PanelUntranslatedLine();
            line.setLeftColumn(new String[]{"" + resource.has + "/" + resource.cost});
            line.leftIcon = resource.res.staticStack;
            if (resPos + 1 < resources.size()) {
               resource = resources.get(resPos + 1);
               line.rightColumn = new String[]{"" + resource.has + "/" + resource.cost};
               line.middleIcon = resource.res.staticStack;
            }

            lines.add(line);
            resPos += 2;
         } else {
            TileEntityPanel.PanelUntranslatedLine line = new TileEntityPanel.PanelUntranslatedLine();
            line.setFullLine(new String[]{"" + resource.has + "/" + resource.cost});
            line.leftIcon = resource.res.staticStack;
            line.centerLine = false;
            lines.add(line);
            resPos++;
         }
      }
   }

   private PanelManager.EnumSignType getSignType() {
      if (this.building.isTownhall) {
         if (this.building.villageType.isMarvel()) {
            return PanelManager.EnumSignType.MARVEL;
         } else if (this.building.location.showTownHallSigns) {
            return PanelManager.EnumSignType.TOWNHALL;
         } else {
            return this.building.location.getMaleResidents().size() <= 0 && this.building.location.getFemaleResidents().size() <= 0
               ? PanelManager.EnumSignType.DEFAULT
               : PanelManager.EnumSignType.HOUSE;
         }
      } else if (this.building.hasVisitors) {
         return PanelManager.EnumSignType.VISITORS;
      } else if (this.building.isInn) {
         return PanelManager.EnumSignType.INN;
      } else if (this.building.containsTags("archives")) {
         return PanelManager.EnumSignType.ARCHIVES;
      } else if (this.building.containsTags("borderpostsign")) {
         return PanelManager.EnumSignType.WALL;
      } else {
         return this.building.location.getMaleResidents().size() <= 0 && this.building.location.getFemaleResidents().size() <= 0
            ? PanelManager.EnumSignType.DEFAULT
            : PanelManager.EnumSignType.HOUSE;
      }
   }

   private void updateArchiveSigns() {
      if (!this.building.world.isRemote) {
         EntityPlayer player = this.building
            .world
            .getClosestPlayer(this.building.getPos().getiX(), this.building.getPos().getiY(), this.building.getPos().getiZ(), 16.0, false);
         if (player != null) {
            if (this.building.world.getWorldTime() - this.lastSignUpdate >= 100L) {
               if (this.building.getResManager().signs.size() != 0) {
                  for (int i = 0; i < this.building.getResManager().signs.size(); i++) {
                     Point p = this.building.getResManager().signs.get(i);
                     if (p != null && WorldUtilities.getBlock(this.building.world, p) != MillBlocks.PANEL) {
                        EnumFacing facing = WorldUtilities.guessPanelFacing(this.building.world, p);
                        if (facing != null) {
                           WorldUtilities.setBlockstate(
                              this.building.world, p, MillBlocks.PANEL.getDefaultState().withProperty(BlockWallSign.FACING, facing), true, false
                           );
                        }
                     }
                  }

                  int signId = 0;

                  for (VillagerRecord vr : this.building.getTownHall().getVillagerRecords().values()) {
                     if (!vr.raidingVillage && !vr.getType().visitor && this.building.getResManager().signs.get(signId) != null) {
                        TileEntityPanel sign = this.building.getResManager().signs.get(signId).getPanel(this.building.world);
                        if (sign != null) {
                           List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
                           lines.add(this.createFullLine(vr.firstName, vr.getType().getIcon(), vr.getType().getIcon()));
                           lines.add(this.createFullLine(vr.familyName, null, null));
                           lines.add(this.createEmptyLine());
                           if (vr.awayraiding) {
                              lines.add(this.createFullLine("panels.awayraiding", stackFromItem(Items.IRON_AXE), stackFromItem(Items.IRON_AXE)));
                           } else if (vr.awayhired) {
                              lines.add(this.createFullLine("panels.awayhired", stackFromItem(MillItems.PURSE), stackFromItem(MillItems.PURSE)));
                           } else if (vr.killed) {
                              lines.add(this.createFullLine("panels.dead", stackFromItem(Items.SKULL), stackFromItem(Items.SKULL)));
                           } else {
                              MillVillager villager = this.building.mw.getVillagerById(vr.getVillagerId());
                              if (villager == null) {
                                 lines.add(this.createFullLine("panels.missing", stackFromItem(Items.SKULL), stackFromItem(Items.SKULL)));
                              } else if (!villager.isVisitor()) {
                                 String distance = "" + Math.floor(this.building.getPos().distanceTo(villager));
                                 String direction = this.building.getPos().directionTo(villager.getPos());
                                 String occupation = "";
                                 if (villager.goalKey != null && Goal.goals.containsKey(villager.goalKey)) {
                                    occupation = "goal." + Goal.goals.get(villager.goalKey).labelKey(villager);
                                 }

                                 lines.add(this.createFullLine(new String[]{"other.shortdistancedirection", distance, direction}, null, null));
                                 lines.add(this.createFullLine(occupation, null, null));
                              }
                           }

                           sign.villager_id = vr.getVillagerId();
                           sign.untranslatedLines = lines;
                           sign.buildingPos = this.building.getTownHallPos();
                           sign.panelType = 7;
                           sign.texture = this.building.culture.panelTexture;
                           sign.triggerUpdate();
                           signId++;
                        }
                     }

                     if (signId >= this.building.getResManager().signs.size()) {
                        break;
                     }
                  }

                  for (int ix = signId; ix < this.building.getResManager().signs.size(); ix++) {
                     if (this.building.getResManager().signs.get(ix) != null) {
                        TileEntityPanel sign = this.building.getResManager().signs.get(ix).getPanel(this.building.world);
                        if (sign != null) {
                           List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
                           lines.add(this.createFullLine("ui.reservedforvillager1", null, null));
                           lines.add(this.createFullLine("ui.reservedforvillager2", null, null));
                           lines.add(this.createEmptyLine());
                           lines.add(this.createFullLine("#" + (ix + 1), null, null));
                           sign.untranslatedLines = lines;
                           sign.buildingPos = this.building.getTownHallPos();
                           sign.panelType = 0;
                           sign.texture = this.building.culture.panelTexture;
                           sign.triggerUpdate();
                        }
                     }
                  }

                  this.lastSignUpdate = this.building.world.getWorldTime();
               }
            }
         }
      }
   }

   private void updateBorderPostSign() {
      if (!this.building.world.isRemote) {
         EntityPlayer player = this.building
            .world
            .getClosestPlayer(this.building.getPos().getiX(), this.building.getPos().getiY(), this.building.getPos().getiZ(), 20.0, false);
         if (player != null) {
            if (this.building.getResManager().signs.size() != 0) {
               for (int i = 0; i < this.building.getResManager().signs.size(); i++) {
                  Point p = this.building.getResManager().signs.get(i);
                  if (p != null && WorldUtilities.getBlock(this.building.world, p) != MillBlocks.PANEL) {
                     EnumFacing facing = WorldUtilities.guessPanelFacing(this.building.world, p);
                     if (facing != null) {
                        WorldUtilities.setBlockstate(
                           this.building.world, p, MillBlocks.PANEL.getDefaultState().withProperty(BlockWallSign.FACING, facing), true, false
                        );
                     }
                  }
               }

               TileEntityPanel sign = this.building.getResManager().signs.get(0).getPanel(this.building.world);
               if (sign != null && this.building.getTownHall() != null) {
                  List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
                  int nbvill = 0;

                  for (VillagerRecord vr : this.building.getTownHall().getVillagerRecords().values()) {
                     if (vr != null) {
                        boolean belongsToVillage = !vr.raidingVillage && vr.getType() != null && !vr.getType().visitor;
                        if (belongsToVillage) {
                           nbvill++;
                        }
                     }
                  }

                  lines.add(
                     this.createFullLine(
                        this.building.getTownHall().getVillageNameWithoutQualifier(),
                        this.building.getTownHall().getBannerStack(),
                        this.building.getTownHall().getBannerStack()
                     )
                  );
                  lines.add(this.createFullLine(this.building.getTownHall().getQualifier(), null, null));
                  lines.add(this.createEmptyLine());
                  lines.add(this.createFullLine(new String[]{"ui.populationnumber", "" + nbvill}, null, null));
                  if (this.building.getTownHall().controlledBy != null) {
                     lines.add(this.createEmptyLine());
                     lines.add(
                        this.createFullLine(
                           this.building.getTownHall().controlledByName, stackFromItem(Items.GOLDEN_HELMET), stackFromItem(Items.GOLDEN_HELMET)
                        )
                     );
                  } else {
                     lines.add(this.createEmptyLine());
                     lines.add(this.createFullLine("Visits welcome", null, null));
                     lines.add(this.createFullLine("ui.borderpost_constructionforbidden", null, null));
                  }

                  sign.untranslatedLines = lines;
                  sign.buildingPos = this.building.getTownHallPos();
                  sign.panelType = 8;
                  sign.texture = this.building.culture.panelTexture;
                  sign.triggerUpdate();
               }
            }
         }
      }
   }

   private void updateDefaultSign() {
      if (!this.building.world.isRemote) {
         if (this.building.getResManager().signs.size() != 0) {
            if (this.building.getPos() != null && this.building.location != null) {
               EntityPlayer player = this.building
                  .world
                  .getClosestPlayer(this.building.getPos().getiX(), this.building.getPos().getiY(), this.building.getPos().getiZ(), 16.0, false);
               if (player != null) {
                  if (this.building.world.getWorldTime() - this.lastSignUpdate >= 100L) {
                     Point p = this.building.getResManager().signs.get(0);
                     if (p != null) {
                        if (WorldUtilities.getBlock(this.building.world, p.getiX(), p.getiY(), p.getiZ()) != MillBlocks.PANEL) {
                           EnumFacing facing = WorldUtilities.guessPanelFacing(this.building.world, p);
                           if (facing != null) {
                              WorldUtilities.setBlockstate(
                                 this.building.world, p, MillBlocks.PANEL.getDefaultState().withProperty(BlockWallSign.FACING, facing), true, false
                              );
                           }
                        }

                        TileEntityPanel sign = p.getPanel(this.building.world);
                        if (sign == null) {
                           MillLog.error(this, "No TileEntitySign at: " + p);
                        } else {
                           List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
                           lines.add(this.createFullLine(this.building.getNativeBuildingName(), this.building.getIcon(), this.building.getIcon()));
                           lines.add(this.createEmptyLine());
                           lines.add(this.createFullLine(this.building.getGameBuildingName(), null, null));
                           sign.untranslatedLines = lines;
                           sign.buildingPos = this.building.getPos();
                           sign.texture = this.building.culture.panelTexture;
                           sign.triggerUpdate();
                        }

                        this.lastSignUpdate = this.building.world.getWorldTime();
                     }
                  }
               }
            }
         }
      }
   }

   private void updateHouseSign() {
      if (!this.building.world.isRemote) {
         if (this.building.getResManager().signs.size() != 0) {
            if (this.building.getPos() != null && this.building.location != null) {
               if (!this.building.isTownhall || !this.building.location.showTownHallSigns) {
                  EntityPlayer player = this.building
                     .world
                     .getClosestPlayer(this.building.getPos().getiX(), this.building.getPos().getiY(), this.building.getPos().getiZ(), 16.0, false);
                  if (player != null) {
                     if (this.building.world.getWorldTime() - this.lastSignUpdate >= 100L) {
                        VillagerRecord wife = null;
                        VillagerRecord husband = null;
                        int nbMaleAdults = 0;
                        int nbFemaleAdults = 0;
                        int nbResidents = 0;

                        for (VillagerRecord vr : this.building.getTownHall().getVillagerRecords().values()) {
                           if (this.building.getPos().equals(vr.getHousePos())) {
                              if (vr.gender == 2 && (vr.getType() == null || !vr.getType().isChild)) {
                                 wife = vr;
                                 nbFemaleAdults++;
                              }

                              if (vr.gender == 1 && (vr.getType() == null || !vr.getType().isChild)) {
                                 husband = vr;
                                 nbMaleAdults++;
                              }

                              nbResidents++;
                           }
                        }

                        Point p = this.building.getResManager().signs.get(0);
                        if (p != null) {
                           if (WorldUtilities.getBlock(this.building.world, p.getiX(), p.getiY(), p.getiZ()) != MillBlocks.PANEL) {
                              EnumFacing facing = WorldUtilities.guessPanelFacing(this.building.world, p);
                              if (facing != null) {
                                 WorldUtilities.setBlockstate(
                                    this.building.world, p, MillBlocks.PANEL.getDefaultState().withProperty(BlockWallSign.FACING, facing), true, false
                                 );
                              }
                           }

                           TileEntityPanel sign = p.getPanel(this.building.world);
                           if (sign == null) {
                              MillLog.error(this, "No TileEntitySign at: " + p);
                           } else {
                              List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
                              lines.add(this.createFullLine(this.building.getNativeBuildingName(), this.building.getIcon(), this.building.getIcon()));
                              lines.add(this.createEmptyLine());
                              if ((wife != null || husband != null) && nbMaleAdults < 2 && nbFemaleAdults < 2) {
                                 if (husband != null && wife != null) {
                                    lines.add(
                                       this.createFullLine(new String[]{"panels.nameand", wife.firstName}, wife.getType().getIcon(), wife.getType().getIcon())
                                    );
                                    lines.add(this.createFullLine(husband.firstName, husband.getType().getIcon(), husband.getType().getIcon()));
                                    lines.add(this.createFullLine(husband.familyName, null, null));
                                 } else if (husband != null) {
                                    lines.add(this.createFullLine(husband.firstName, husband.getType().getIcon(), husband.getType().getIcon()));
                                    lines.add(this.createFullLine(husband.familyName, null, null));
                                 } else if (wife != null) {
                                    lines.add(this.createFullLine(wife.firstName, wife.getType().getIcon(), wife.getType().getIcon()));
                                    lines.add(this.createFullLine(wife.familyName, null, null));
                                 }
                              } else if (nbResidents > 0) {
                                 for (VillagerRecord vrx : this.building.getTownHall().getVillagerRecords().values()) {
                                    if (this.building.getPos().equals(vrx.getHousePos())) {
                                       lines.add(this.createFullLine(vrx.firstName, vrx.getType().getIcon(), vrx.getType().getIcon()));
                                    }
                                 }
                              } else {
                                 lines.add(this.createFullLine("ui.currentlyempty1", null, null));
                                 lines.add(this.createFullLine("ui.currentlyempty2", null, null));
                              }

                              sign.untranslatedLines = lines;
                              sign.buildingPos = this.building.getPos();
                              sign.panelType = 5;
                              sign.texture = this.building.culture.panelTexture;
                              sign.triggerUpdate();
                           }

                           this.lastSignUpdate = this.building.world.getWorldTime();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void updateInnSign() {
      if (!this.building.world.isRemote) {
         EntityPlayer player = this.building
            .world
            .getClosestPlayer(this.building.getPos().getiX(), this.building.getPos().getiY(), this.building.getPos().getiZ(), 20.0, false);
         if (player != null) {
            if (this.building.getResManager().signs.size() != 0) {
               for (int i = 0; i < this.building.getResManager().signs.size(); i++) {
                  Point p = this.building.getResManager().signs.get(i);
                  if (p != null && WorldUtilities.getBlock(this.building.world, p) != MillBlocks.PANEL) {
                     EnumFacing facing = WorldUtilities.guessPanelFacing(this.building.world, p);
                     if (facing != null) {
                        WorldUtilities.setBlockstate(
                           this.building.world, p, MillBlocks.PANEL.getDefaultState().withProperty(BlockWallSign.FACING, facing), true, false
                        );
                     }
                  }
               }

               TileEntityPanel sign = this.building.getResManager().signs.get(0).getPanel(this.building.world);
               if (sign != null) {
                  List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
                  lines.add(this.createFullLine(this.building.getNativeBuildingName(), this.building.getIcon(), this.building.getIcon()));
                  lines.add(this.createEmptyLine());
                  lines.add(this.createFullLine("ui.visitorslist1", null, null));
                  lines.add(this.createFullLine("ui.visitorslist2", null, null));
                  sign.untranslatedLines = lines;
                  sign.buildingPos = this.building.getPos();
                  sign.panelType = 11;
                  sign.texture = this.building.culture.panelTexture;
                  sign.triggerUpdate();
               }

               if (this.building.getResManager().signs.size() >= 2) {
                  sign = this.building.getResManager().signs.get(1).getPanel(this.building.world);
                  if (sign != null) {
                     List<String[]> linesFull = new ArrayList<>();
                     List<ItemStack> icons = new ArrayList<>();
                     linesFull.add(new String[]{"ui.goodstraded"});
                     linesFull.add(new String[]{""});
                     linesFull.add(new String[]{"ui.import_total", "" + MillCommonUtilities.getInvItemHashTotal(this.building.imported)});
                     linesFull.add(new String[]{"ui.export_total", "" + MillCommonUtilities.getInvItemHashTotal(this.building.exported)});
                     icons.add(stackFromBlock(Blocks.CHEST));
                     sign.buildingPos = this.building.getPos();
                     sign.panelType = 10;
                     sign.texture = this.building.culture.panelTexture;
                     sign.triggerUpdate();
                  }
               }
            }
         }
      }
   }

   private void updateMarvelDonationsSign(TileEntityPanel sign) {
      if (sign != null) {
         Set<String> villages = new HashSet<>();

         for (String s : this.townHall.getMarvelManager().getDonationList()) {
            String village = s.split(";")[1];
            villages.add(village);
         }

         List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
         lines.add(this.createFullLine("ui.paneldonations1", stackFromItem(MillItems.DENIER_OR), stackFromItem(MillItems.DENIER_OR)));
         lines.add(this.createEmptyLine());
         lines.add(this.createFullLine(new String[]{"ui.paneldonations2", "" + this.townHall.getMarvelManager().getDonationList().size()}, null, null));
         lines.add(this.createFullLine(new String[]{"ui.paneldonations3", "" + villages.size()}, null, null));
         sign.untranslatedLines = lines;
         sign.buildingPos = this.building.getPos();
         sign.panelType = 20;
         sign.texture = this.building.culture.panelTexture;
         sign.triggerUpdate();
      }
   }

   private void updateMarvelProjectsSign(TileEntityPanel sign) {
      if (sign != null) {
         List<BuildingProject> projects = this.townHall.getFlatProjectList();
         int totalProjects = 0;
         int doneProjects = 0;

         for (BuildingProject project : projects) {
            BuildingPlan plan = project.planSet.getFirstStartingPlan();
            BuildingPlan parentPlan = project.parentPlan;
            if (plan.containsTags("marvel") || parentPlan != null && parentPlan.containsTags("marvel")) {
               totalProjects += ((BuildingPlan[])project.planSet.plans.get(0)).length;
               if (project.location != null && project.location.level >= 0) {
                  boolean obsolete = project.planSet != null
                     && project.location.version != project.planSet.plans.get(project.location.getVariation())[0].version;
                  if (project.location.level + 1 >= project.getLevelsNumber(project.location.getVariation())) {
                     doneProjects += ((BuildingPlan[])project.planSet.plans.get(0)).length;
                  } else if (obsolete) {
                     doneProjects += project.location.level + 1;
                  } else {
                     doneProjects += project.location.level + 1;
                  }
               }
            }
         }

         List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
         lines.add(this.createFullLine("ui.panelmarvelprojects", stackFromItem(Items.IRON_SHOVEL), stackFromItem(Items.IRON_SHOVEL)));
         lines.add(this.createEmptyLine());
         lines.add(this.createFullLine(new String[]{"ui.panelmarvelprojectsdone", "" + doneProjects}, null, null));
         lines.add(this.createFullLine(new String[]{"ui.panelmarvelprojectstotal", "" + totalProjects}, null, null));
         sign.untranslatedLines = lines;
         sign.buildingPos = this.building.getPos();
         sign.panelType = 3;
         sign.texture = this.building.culture.panelTexture;
         sign.triggerUpdate();
      }
   }

   private void updateMarvelResourcesSign(TileEntityPanel sign) {
      if (sign != null) {
         Map<InvItem, Integer> totalCost = this.townHall.villageType.computeVillageTypeCost();
         Map<InvItem, Integer> remainingNeeds = this.townHall.getMarvelManager().computeNeeds();
         int totalCostSum = 0;
         int remainingNeedsSum = 0;

         for (Integer cost : totalCost.values()) {
            totalCostSum += cost;
         }

         for (Integer needs : remainingNeeds.values()) {
            if (needs > 0) {
               remainingNeedsSum += needs;
            }
         }

         List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
         lines.add(
            this.createFullLine(
               "ui.panelmarvelres1", stackFromItem(Item.getItemFromBlock(Blocks.CHEST)), stackFromItem(Item.getItemFromBlock(Blocks.CHEST))
            )
         );
         lines.add(this.createFullLine("ui.panelmarvelres2", null, null));
         lines.add(this.createEmptyLine());
         lines.add(
            this.createFullLine(
               new String[]{"ui.panelmarvelrescount", String.format("%,d", totalCostSum - remainingNeedsSum), String.format("%,d", totalCostSum)}, null, null
            )
         );
         sign.untranslatedLines = lines;
         sign.buildingPos = this.building.getPos();
         sign.panelType = 3;
         sign.texture = this.building.culture.panelTexture;
         sign.triggerUpdate();
      }
   }

   private void updateMarvelSigns(boolean forced) {
      if (!this.townHall.world.isRemote) {
         EntityPlayer player = this.townHall
            .world
            .getClosestPlayer(this.townHall.getPos().getiX(), this.townHall.getPos().getiY(), this.townHall.getPos().getiZ(), 20.0, false);
         if (player != null) {
            if (forced || this.townHall.world.getWorldTime() - this.lastSignUpdate >= 40L) {
               if (this.townHall.getResManager().signs.size() >= 7) {
                  for (int i = 0; i < this.townHall.getResManager().signs.size(); i++) {
                     Point p = this.townHall.getResManager().signs.get(i);
                     if (p != null && WorldUtilities.getBlock(this.townHall.world, p) != MillBlocks.PANEL) {
                        EnumFacing facing = WorldUtilities.guessPanelFacing(this.townHall.world, p);
                        if (facing != null) {
                           WorldUtilities.setBlockstate(
                              this.townHall.world, p, MillBlocks.PANEL.getDefaultState().withProperty(BlockWallSign.FACING, facing), true, false
                           );
                        }
                     }
                  }

                  int signPos = 0;
                  TileEntityPanel sign = (TileEntityPanel)this.townHall.world.getTileEntity(this.townHall.getResManager().signs.get(signPos).getBlockPos());
                  this.updateSignTHVillageName(sign);
                  sign = this.townHall.getResManager().signs.get(++signPos).getPanel(this.townHall.world);
                  this.updateSignTHResources(sign);
                  signPos++;
                  signPos++;
                  sign = this.townHall.getResManager().signs.get(++signPos).getPanel(this.townHall.world);
                  this.updateSignTHProject(sign);
                  sign = this.townHall.getResManager().signs.get(++signPos).getPanel(this.townHall.world);
                  this.updateSignTHConstruction(sign);
                  sign = this.townHall.getResManager().signs.get(++signPos).getPanel(this.townHall.world);
                  this.updateSignTHEtatCivil(sign);
                  sign = this.townHall.getResManager().signs.get(++signPos).getPanel(this.townHall.world);
                  this.updateSignTHMap(sign);
                  sign = this.townHall.getResManager().signs.get(++signPos).getPanel(this.townHall.world);
                  this.updateSignTHMilitary(sign);
                  sign = this.townHall.getResManager().signs.get(signPos).getPanel(this.townHall.world);
                  this.updateMarvelProjectsSign(sign);
                  sign = this.townHall.getResManager().signs.get(++signPos).getPanel(this.townHall.world);
                  this.updateMarvelResourcesSign(sign);
                  sign = this.townHall.getResManager().signs.get(++signPos).getPanel(this.townHall.world);
                  this.updateMarvelDonationsSign(sign);
                  signPos++;
                  this.lastSignUpdate = this.townHall.world.getWorldTime();
               }
            }
         }
      }
   }

   public void updateSigns() {
      PanelManager.EnumSignType type = this.getSignType();
      if (type == PanelManager.EnumSignType.MARVEL) {
         this.updateMarvelSigns(false);
      } else if (type == PanelManager.EnumSignType.TOWNHALL) {
         this.updateTownHallSigns(false);
      } else if (type == PanelManager.EnumSignType.ARCHIVES) {
         this.updateArchiveSigns();
      } else if (type == PanelManager.EnumSignType.VISITORS) {
         this.updateVisitorsSigns();
      } else if (type == PanelManager.EnumSignType.INN) {
         this.updateInnSign();
      } else if (type == PanelManager.EnumSignType.WALL) {
         this.updateBorderPostSign();
      } else if (type == PanelManager.EnumSignType.HOUSE) {
         this.updateHouseSign();
      } else if (type == PanelManager.EnumSignType.DEFAULT) {
         this.updateDefaultSign();
      }
   }

   private void updateSignTHConstruction(TileEntityPanel sign) {
      if (sign != null) {
         List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
         ConstructionIP activeCIP = null;
         int nbActiveCIP = 0;

         for (ConstructionIP cip : this.building.getConstructionsInProgress()) {
            if (cip.getBuildingLocation() != null) {
               nbActiveCIP++;
               if (activeCIP == null) {
                  activeCIP = cip;
               }
            }
         }

         if (nbActiveCIP == 1) {
            BuildingPlanSet planSet = this.building.culture.getBuildingPlanSet(activeCIP.getBuildingLocation().planKey);
            String planName = planSet.getNameNative();
            String[] status;
            if (activeCIP.getBuildingLocation().level == 0) {
               status = new String[]{"ui.inconstruction"};
            } else {
               status = new String[]{"ui.upgrading", "" + activeCIP.getBuildingLocation().level};
            }

            String[] loc;
            if (activeCIP.getBuildingLocation() != null) {
               int distance = MathHelper.floor(this.building.getPos().distanceTo(activeCIP.getBuildingLocation().pos));
               String direction = this.building.getPos().directionTo(activeCIP.getBuildingLocation().pos);
               loc = new String[]{"other.shortdistancedirection", "" + distance, "" + direction};
            } else {
               loc = new String[]{""};
            }

            String[] constr;
            if (activeCIP.getBblocks() != null && activeCIP.getBblocks().length > 0) {
               constr = new String[]{"ui.progress", "" + (int)Math.floor(activeCIP.getBblocksPos() * 100 / activeCIP.getBblocks().length)};
            } else {
               constr = new String[]{"ui.progressnopercent"};
            }

            lines.add(this.createFullLine(planName, planSet.getIcon(), planSet.getIcon()));
            lines.add(this.createEmptyLine());
            lines.add(this.createFullLine(constr, null, null));
            lines.add(this.createFullLine(status, null, null));
            lines.add(this.createFullLine(loc, null, null));
         } else if (nbActiveCIP > 1) {
            lines.add(this.createFullLine(new String[]{"ui.xconstructions", "" + nbActiveCIP}, null, null));
            lines.add(this.createEmptyLine());
            int cipPos = 0;

            for (ConstructionIP cipx : this.building.getConstructionsInProgress()) {
               if (cipx.getBuildingLocation() != null && cipPos < 4) {
                  String planNamex = this.building.culture.getBuildingPlanSet(cipx.getBuildingLocation().planKey).getNameNative();
                  ItemStack icon = this.building.culture.getBuildingPlanSet(cipx.getBuildingLocation().planKey).getIcon();
                  String level = "l0";
                  if (cipx.getBuildingLocation().level > 0) {
                     level = "l" + cipx.getBuildingLocation().level;
                  }

                  lines.add(this.createFullLine(planNamex + " " + level, icon, icon));
                  cipPos++;
               }
            }
         } else {
            lines.add(this.createEmptyLine());
            lines.add(this.createEmptyLine());
            lines.add(this.createFullLine("ui.noconstruction1", null, null));
            lines.add(this.createFullLine("ui.noconstruction2", null, null));
         }

         sign.untranslatedLines = lines;
         sign.buildingPos = this.building.getPos();
         sign.panelType = 2;
         sign.texture = this.building.culture.panelTexture;
         sign.triggerUpdate();
      }
   }

   private void updateSignTHEtatCivil(TileEntityPanel sign) {
      if (sign != null) {
         int nbMen = 0;
         int nbFemale = 0;
         int nbGrownBoy = 0;
         int nbGrownGirl = 0;
         int nbBoy = 0;
         int nbGirl = 0;
         List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();

         for (VillagerRecord vr : this.building.getVillagerRecords().values()) {
            boolean belongsToVillage = vr.getType() != null && !vr.getType().visitor && !vr.raidingVillage;
            if (belongsToVillage) {
               if (!vr.getType().isChild) {
                  if (vr.gender == 1) {
                     nbMen++;
                  } else {
                     nbFemale++;
                  }
               } else if (vr.size == 20) {
                  if (vr.gender == 1) {
                     nbGrownBoy++;
                  } else {
                     nbGrownGirl++;
                  }
               } else if (vr.gender == 1) {
                  nbBoy++;
               } else {
                  nbGirl++;
               }
            }
         }

         lines.add(this.createFullLine("ui.population", FLOWER_BLUE, FLOWER_PINK));
         lines.add(this.createEmptyLine());
         lines.add(this.createFullLine(new String[]{"ui.adults", "" + (nbMen + nbFemale), "" + nbMen, "" + nbFemale}, null, null));
         lines.add(this.createFullLine(new String[]{"ui.teens", "" + (nbGrownBoy + nbGrownGirl), "" + nbGrownBoy, "" + nbGrownGirl}, null, null));
         lines.add(this.createFullLine(new String[]{"ui.children", "" + (nbBoy + nbGirl), "" + nbBoy, "" + nbGirl}, null, null));
         sign.untranslatedLines = lines;
         sign.buildingPos = this.building.getPos();
         sign.panelType = 1;
         sign.texture = this.building.culture.panelTexture;
         sign.triggerUpdate();
      }
   }

   private void updateSignTHMap(TileEntityPanel sign) {
      List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
      lines.add(this.createFullLine("ui.villagemap", stackFromItem(Items.FILLED_MAP), stackFromItem(Items.FILLED_MAP)));
      lines.add(this.createEmptyLine());
      int nbBuildings = 0;

      for (Building building : this.building.getBuildings()) {
         if (building.location.isCustomBuilding || !building.location.getPlan().isWallSegment) {
            nbBuildings++;
         }
      }

      lines.add(this.createFullLine(new String[]{"ui.nbbuildings", "" + nbBuildings}, null, null));
      sign.untranslatedLines = lines;
      sign.buildingPos = this.building.getPos();
      sign.panelType = 8;
      sign.texture = this.building.culture.panelTexture;
      sign.triggerUpdate();
   }

   private void updateSignTHMilitary(TileEntityPanel sign) {
      if (sign != null) {
         String status = "";
         if (this.building.raidTarget != null) {
            if (this.building.raidStart > 0L) {
               status = "panels.raidinprogress";
            } else {
               status = "panels.planningraid";
            }
         } else if (this.building.underAttack) {
            status = "panels.underattack";
         }

         List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
         lines.add(this.createFullLine("panels.military", stackFromItem(Items.IRON_SWORD), stackFromItem(Items.IRON_SWORD)));
         lines.add(this.createEmptyLine());
         if (status.length() > 0) {
            lines.add(this.createFullLine(status, null, null));
         }

         lines.add(
            this.createFullLine(
               new String[]{"panels.offense", "" + this.building.getVillageRaidingStrength()},
               stackFromItem(Items.IRON_AXE),
               stackFromItem(Items.IRON_AXE)
            )
         );
         lines.add(
            this.createFullLine(
               new String[]{"panels.defense", "" + this.building.getVillageDefendingStrength()},
               stackFromItem(Items.IRON_CHESTPLATE),
               stackFromItem(Items.IRON_CHESTPLATE)
            )
         );
         int type;
         if (this.building.villageType.playerControlled) {
            type = 13;
         } else {
            type = 9;
         }

         sign.untranslatedLines = lines;
         sign.buildingPos = this.building.getPos();
         sign.panelType = type;
         sign.texture = this.building.culture.panelTexture;
         sign.triggerUpdate();
      }
   }

   private void updateSignTHProject(TileEntityPanel sign) {
      if (sign != null) {
         List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
         String[] status = null;
         if (this.building.buildingGoal == null) {
            lines.add(this.createEmptyLine());
            lines.add(this.createEmptyLine());
            lines.add(this.createFullLine("ui.goalscompleted1", null, null));
            lines.add(this.createFullLine("ui.goalscompleted2", null, null));
         } else {
            BuildingPlan goal = this.building.getCurrentGoalBuildingPlan();
            boolean inprogress = false;
            lines.add(this.createFullLine("ui.project", goal.getIcon(), goal.getIcon()));
            lines.add(this.createEmptyLine());
            lines.add(this.createFullLine(goal.nativeName, null, null));
            lines.add(this.createFullLine(goal.getGameNameKey(), null, null));

            for (ConstructionIP cip : this.building.getConstructionsInProgress()) {
               if (cip.getBuildingLocation() != null && cip.getBuildingLocation().planKey.equals(this.building.buildingGoal)) {
                  if (cip.getBuildingLocation().level == 0) {
                     status = new String[]{"ui.inconstruction"};
                  } else {
                     status = new String[]{"ui.upgrading", "" + cip.getBuildingLocation().level};
                  }

                  inprogress = true;
               }
            }

            if (!inprogress) {
               status = new String[]{this.building.buildingGoalIssue};
            }

            lines.add(this.createEmptyLine());
            lines.add(this.createFullLine(status, null, null));
         }

         int type;
         if (this.building.villageType.playerControlled) {
            type = 4;
         } else {
            type = 3;
         }

         sign.untranslatedLines = lines;
         sign.buildingPos = this.building.getPos();
         sign.panelType = type;
         sign.texture = this.building.culture.panelTexture;
         sign.triggerUpdate();
      }
   }

   private void updateSignTHResources(TileEntityPanel sign) {
      if (sign != null) {
         BuildingPlan goalPlan = this.building.getCurrentGoalBuildingPlan();
         List<InvItem> res = new ArrayList<>();
         List<Integer> resCost = new ArrayList<>();
         List<Integer> resHas = new ArrayList<>();
         if (goalPlan != null) {
            boolean inprogress = false;

            for (ConstructionIP cip : this.building.getConstructionsInProgress()) {
               if (cip.getBuildingLocation() != null && cip.getBuildingLocation().planKey.equals(this.building.buildingGoal)) {
                  inprogress = true;
               }
            }

            if (inprogress) {
               for (InvItem key : goalPlan.resCost.keySet()) {
                  res.add(key);
                  resCost.add(goalPlan.resCost.get(key));
                  resHas.add(goalPlan.resCost.get(key));
               }
            } else {
               for (InvItem key : goalPlan.resCost.keySet()) {
                  res.add(key);
                  resCost.add(goalPlan.resCost.get(key));
                  int has = this.building.countGoods(key.getItem(), key.meta);
                  if (has > goalPlan.resCost.get(key)) {
                     has = goalPlan.resCost.get(key);
                  }

                  resHas.add(has);
               }
            }
         }

         List<PanelManager.ResourceLine> resources = new ArrayList<>();

         for (int i = 0; i < res.size(); i++) {
            resources.add(new PanelManager.ResourceLine(res.get(i), resCost.get(i), resHas.get(i)));
         }

         List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
         if (goalPlan != null) {
            lines.add(this.createFullLine("ui.resources", stackFromBlock(Blocks.CHEST), stackFromBlock(Blocks.CHEST)));
            if (res.size() < 12) {
               lines.add(this.createEmptyLine());
            }

            this.generateResourceLines(resources, lines);
         }

         sign.untranslatedLines = lines;
         sign.buildingPos = this.building.getPos();
         sign.panelType = 6;
         sign.texture = this.building.culture.panelTexture;
         sign.triggerUpdate();
      }
   }

   private void updateSignTHVillageName(TileEntityPanel sign) {
      if (sign != null) {
         int nbvill = 0;

         for (VillagerRecord vr : this.building.getVillagerRecords().values()) {
            if (vr != null) {
               boolean belongsToVillage = !vr.raidingVillage && vr.getType() != null && !vr.getType().visitor;
               if (belongsToVillage) {
                  nbvill++;
               }
            }
         }

         List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
         lines.add(this.createFullLine(this.building.getVillageNameWithoutQualifier(), this.building.getBannerStack(), this.building.getBannerStack()));
         lines.add(this.createFullLine(this.building.getQualifier(), null, null));
         lines.add(this.createEmptyLine());
         lines.add(this.createFullLine(this.building.villageType.name, null, null));
         lines.add(this.createFullLine(new String[]{"ui.populationnumber", "" + nbvill}, null, null));
         if (this.building.controlledBy != null) {
            lines.add(this.createEmptyLine());
            lines.add(this.createFullLine(this.building.controlledByName, stackFromItem(Items.GOLDEN_HELMET), stackFromItem(Items.GOLDEN_HELMET)));
         }

         sign.untranslatedLines = lines;
         sign.buildingPos = this.building.getPos();
         sign.panelType = 1;
         sign.texture = this.building.culture.panelTexture;
         sign.triggerUpdate();
      }
   }

   private void updateSignTHWalls(TileEntityPanel sign) {
      if (sign != null) {
         List<BuildingProject> projects = this.townHall.getFlatProjectList();
         int wallLevel = this.townHall.computeCurrentWallLevel();
         List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
         lines.add(this.createFullLine("ui.panelwalls", stackFromBlock(Blocks.COBBLESTONE_WALL), stackFromBlock(Blocks.COBBLESTONE_WALL)));
         if (wallLevel == Integer.MAX_VALUE) {
            lines.add(this.createEmptyLine());
            lines.add(this.createFullLine("ui.panelwallscomplete", null, null));
         } else if (wallLevel == -1) {
            lines.add(this.createEmptyLine());
            lines.add(this.createFullLine("ui.panelwallnowalls", null, null));
         } else {
            PanelManager.WallStatusInfos wallInfos = this.computeWallInfos(projects, wallLevel);
            lines.add(
               this.createFullLine(
                  new String[]{"ui.panelwallslevel", "" + wallLevel, "" + wallInfos.segmentsDone, "" + (wallInfos.segmentsDone + wallInfos.segmentsToDo)},
                  null,
                  null
               )
            );
            if (wallInfos.resources.size() < (8 - lines.size()) * 2) {
               lines.add(this.createEmptyLine());
            }

            this.generateResourceLines(wallInfos.resources, lines);
         }

         sign.untranslatedLines = lines;
         sign.buildingPos = this.building.getPos();
         sign.panelType = 15;
         sign.texture = this.building.culture.panelTexture;
         sign.triggerUpdate();
      }
   }

   private void updateTownHallSigns(boolean forced) {
      if (!this.building.world.isRemote) {
         EntityPlayer player = this.building
            .world
            .getClosestPlayer(this.building.getPos().getiX(), this.building.getPos().getiY(), this.building.getPos().getiZ(), 20.0, false);
         if (player != null) {
            if (forced || this.building.world.getWorldTime() - this.lastSignUpdate >= 40L) {
               for (int i = 0; i < this.building.getResManager().signs.size(); i++) {
                  Point p = this.building.getResManager().signs.get(i);
                  if (p != null) {
                     if (WorldUtilities.getBlock(this.building.world, p) != MillBlocks.PANEL) {
                        EnumFacing facing = WorldUtilities.guessPanelFacing(this.building.world, p);
                        if (facing != null) {
                           WorldUtilities.setBlockstate(
                              this.building.world, p, MillBlocks.PANEL.getDefaultState().withProperty(BlockWallSign.FACING, facing), true, false
                           );
                        }
                     }

                     TileEntityPanel sign = (TileEntityPanel)this.building.world.getTileEntity(p.getBlockPos());
                     if (sign != null) {
                        switch (i) {
                           case 0:
                              this.updateSignTHVillageName(sign);
                              break;
                           case 1:
                              this.updateSignTHResources(sign);
                              break;
                           case 2:
                              this.updateSignTHWalls(sign);
                              break;
                           case 3:
                              sign.texture = this.building.culture.panelTexture;
                              sign.triggerUpdate();
                              break;
                           case 4:
                              this.updateSignTHProject(sign);
                              break;
                           case 5:
                              this.updateSignTHConstruction(sign);
                              break;
                           case 6:
                              this.updateSignTHEtatCivil(sign);
                              break;
                           case 7:
                              this.updateSignTHMap(sign);
                              break;
                           case 8:
                              this.updateSignTHMilitary(sign);
                        }
                     }
                  }
               }

               this.lastSignUpdate = this.building.world.getWorldTime();
            }
         }
      }
   }

   public void updateVisitorsSigns() {
      EntityPlayer player = this.building
         .world
         .getClosestPlayer(this.building.getPos().getiX(), this.building.getPos().getiY(), this.building.getPos().getiZ(), 20.0, false);
      if (player != null) {
         if (this.building.getResManager().signs.size() != 0 && this.building.getResManager().signs.get(0) != null) {
            for (int i = 0; i < this.building.getResManager().signs.size(); i++) {
               Point p = this.building.getResManager().signs.get(i);
               if (p != null && WorldUtilities.getBlock(this.building.world, p) != MillBlocks.PANEL) {
                  EnumFacing facing = WorldUtilities.guessPanelFacing(this.building.world, p);
                  if (facing != null) {
                     WorldUtilities.setBlockstate(
                        this.building.world, p, MillBlocks.PANEL.getDefaultState().withProperty(BlockWallSign.FACING, facing), true, false
                     );
                  }
               }
            }

            TileEntityPanel sign = this.building.getResManager().signs.get(0).getPanel(this.building.world);
            if (sign != null) {
               List<TileEntityPanel.PanelUntranslatedLine> lines = new ArrayList<>();
               lines.add(this.createFullLine(this.building.getNativeBuildingName(), this.building.getIcon(), this.building.getIcon()));
               lines.add(this.createEmptyLine());
               lines.add(this.createFullLine("ui.visitorslist2", null, null));
               int type = 0;
               byte var8;
               if (this.building.isMarket) {
                  lines.add(this.createFullLine("ui.merchants", null, null));
                  var8 = 12;
               } else {
                  lines.add(this.createFullLine("ui.visitors", null, null));
                  var8 = 14;
               }

               lines.add(this.createFullLine(new String[]{"" + this.building.getAllVillagerRecords().size()}, null, null));
               sign.untranslatedLines = lines;
               sign.buildingPos = this.building.getPos();
               sign.panelType = var8;
               sign.texture = this.building.culture.panelTexture;
               sign.triggerUpdate();
            }
         }
      }
   }

   public static enum EnumSignType {
      DEFAULT,
      HOUSE,
      TOWNHALL,
      INN,
      ARCHIVES,
      MARVEL,
      VISITORS,
      WALL,
      WALLBUILD;
   }

   public static class ResourceLine implements Comparable<PanelManager.ResourceLine> {
      public InvItem res;
      public int cost;
      public int has;

      ResourceLine(InvItem res, int cost, int has) {
         this.res = res;
         this.cost = cost;
         this.has = has;
      }

      public int compareTo(PanelManager.ResourceLine o) {
         return -this.cost + o.cost;
      }
   }

   public static class WallStatusInfos {
      public final List<PanelManager.ResourceLine> resources;
      public final int segmentsDone;
      public final int segmentsToDo;

      public WallStatusInfos(List<PanelManager.ResourceLine> resources, int segmentsDone, int segmentsToDo) {
         this.resources = resources;
         this.segmentsDone = segmentsDone;
         this.segmentsToDo = segmentsToDo;
      }
   }
}
