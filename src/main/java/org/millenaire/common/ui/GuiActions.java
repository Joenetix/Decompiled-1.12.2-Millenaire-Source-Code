package org.millenaire.common.ui;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.millenaire.common.advancements.MillAdvancements;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.buildingplan.BuildingCustomPlan;
import org.millenaire.common.buildingplan.BuildingImportExport;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.item.ItemParchment;
import org.millenaire.common.item.MillItems;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.quest.QuestInstance;
import org.millenaire.common.quest.SpecialQuestActions;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.BuildingProject;
import org.millenaire.common.village.VillagerRecord;
import org.millenaire.common.world.MillWorldData;
import org.millenaire.common.world.UserProfile;
import org.millenaire.common.world.WorldGenVillage;

public class GuiActions {
   public static final int VILLAGE_SCROLL_PRICE = 128;
   public static final int VILLAGE_SCROLL_REPUTATION = 8192;
   public static final int CROP_REPUTATION = 8192;
   public static final int CROP_PRICE = 512;
   public static final int CULTURE_CONTROL_REPUTATION = 131072;

   public static void activateMillChest(EntityPlayer player, Point p) {
      World world = player.world;
      if (MillConfigValues.DEV) {
         MillWorldData mw = Mill.getMillWorld(world);
         if (mw.buildingExists(p)) {
            Building ent = mw.getBuilding(p);
            if (player.inventory.getCurrentItem() != ItemStack.EMPTY
                  && player.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(Blocks.SAND)) {
               ent.testModeGoods();
               return;
            }

            if (player.inventory.getCurrentItem() != ItemStack.EMPTY
                  && player.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(MillBlocks.PATHDIRT)) {
               ent.recalculatePaths(true);
               ent.clearOldPaths();
               ent.constructCalculatedPaths();
               return;
            }

            if (player.inventory.getCurrentItem() != ItemStack.EMPTY
                  && player.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(MillBlocks.PATHGRAVEL)) {
               ent.clearOldPaths();
               return;
            }

            if (player.inventory.getCurrentItem() != ItemStack.EMPTY
                  && player.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(MillBlocks.PATHDIRT_SLAB)) {
               ent.recalculatePaths(true);
               return;
            }

            if (player.inventory.getCurrentItem() != ItemStack.EMPTY
                  && player.inventory.getCurrentItem().getItem() == MillItems.DENIER_OR) {
               ent.displayInfos(player);
               return;
            }

            if (player.inventory.getCurrentItem() != ItemStack.EMPTY
                  && player.inventory.getCurrentItem().getItem() == Items.GLASS_BOTTLE) {
               mw.setGlobalTag("alchemy");
               MillLog.major(mw, "Set alchemy tag.");
               return;
            }

            if (player.inventory.getCurrentItem() != ItemStack.EMPTY
                  && player.inventory.getCurrentItem().getItem() == MillItems.SUMMONING_WAND) {
               ent.displayInfos(player);

               try {
                  if (ent.isTownhall) {
                     ent.rushCurrentConstructions(false);
                  }

                  if (ent.isInn) {
                     ent.attemptMerchantMove(true);
                  }

                  if (ent.hasVisitors) {
                     ent.getVisitorManager().update(true);
                  }
               } catch (Exception var6) {
                  MillLog.printException(var6);
               }

               return;
            }

            if (player.inventory.getCurrentItem() != ItemStack.EMPTY
                  && player.inventory.getCurrentItem().getItem() == Item
                        .getItemFromBlock(MillBlocks.PAINTED_BRICK_WHITE)) {
               ent.choseAndApplyBrickTheme();
               MillLog.major(mw,
                     "Changed theme of village " + ent.getVillageQualifiedName() + " to: " + ent.brickColourTheme.key);
               return;
            }
         }
      }

      ServerSender.displayMillChest(player, p);
   }

   public static void controlledBuildingsForgetBuilding(EntityPlayer player, Building townHall,
         BuildingProject project) {
      townHall.cancelBuilding(project.location);
   }

   public static void controlledBuildingsToggleUpgrades(EntityPlayer player, Building townHall, BuildingProject project,
         boolean allow) {
      project.location.upgradesAllowed = allow;
      if (allow) {
         townHall.noProjectsLeft = false;
      }
   }

   public static void controlledMilitaryCancelRaid(EntityPlayer player, Building townHall) {
      if (townHall.raidStart == 0L) {
         townHall.cancelRaid();
         if (!townHall.world.isRemote) {
            townHall.sendBuildingPacket(player, false);
         }
      }
   }

   public static void controlledMilitaryDiplomacy(EntityPlayer player, Building townHall, Point target, int level) {
      townHall.adjustRelation(target, level, true);
      if (!townHall.world.isRemote) {
         townHall.sendBuildingPacket(player, false);
      }
   }

   public static void controlledMilitaryPlanRaid(EntityPlayer player, Building townHall, Building target) {
      if (townHall.raidStart == 0L) {
         townHall.adjustRelation(target.getPos(), -100, true);
         townHall.planRaid(target);
         if (!townHall.world.isRemote) {
            townHall.sendBuildingPacket(player, false);
         }
      }
   }

   public static void hireExtend(EntityPlayer player, MillVillager villager) {
      villager.hiredBy = player.getName();
      villager.hiredUntil += 24000L;
      MillCommonUtilities.changeMoney(player.inventory, -villager.getHireCost(player), player);
   }

   public static void hireHire(EntityPlayer player, MillVillager villager) {
      villager.hiredBy = player.getName();
      villager.hiredUntil = villager.world.getWorldTime() + 24000L;
      VillagerRecord vr = villager.getRecord();
      if (vr != null) {
         vr.awayhired = true;
      }

      MillAdvancements.HIRED.grant(player);
      MillCommonUtilities.changeMoney(player.inventory, -villager.getHireCost(player), player);
   }

   public static void hireRelease(EntityPlayer player, MillVillager villager) {
      villager.hiredBy = null;
      villager.hiredUntil = 0L;
      VillagerRecord vr = villager.getRecord();
      if (vr != null) {
         vr.awayhired = false;
      }
   }

   public static void hireToggleStance(EntityPlayer player, boolean stance) {
      AxisAlignedBB surroundings = new AxisAlignedBB(
            player.posX,
            player.posY,
            player.posZ,
            player.posX + 1.0,
            player.posY + 1.0,
            player.posZ + 1.0)
            .expand(16.0, 8.0, 16.0)
            .expand(-16.0, -8.0, -16.0);

      for (Object o : player.world.getEntitiesWithinAABB(MillVillager.class, surroundings)) {
         MillVillager villager = (MillVillager) o;
         if (player.getName().equals(villager.hiredBy)) {
            villager.aggressiveStance = stance;
         }
      }
   }

   public static void newBuilding(EntityPlayer player, Building townHall, Point pos, String planKey) {
      BuildingPlanSet set = townHall.culture.getBuildingPlanSet(planKey);
      if (set != null) {
         BuildingPlan plan = set.getRandomStartingPlan();
         BuildingPlan.LocationReturn lr = plan.testSpot(
               townHall.winfo,
               townHall.regionMapper,
               townHall.getPos(),
               pos.getiX() - townHall.winfo.mapStartX,
               pos.getiZ() - townHall.winfo.mapStartZ,
               MillCommonUtilities.getRandom(),
               -1,
               true);
         if (lr.location == null) {
            String error = null;
            if (lr.errorCode == 3) {
               error = "ui.constructionforbidden";
            } else if (lr.errorCode == 2) {
               error = "ui.locationclash";
            } else if (lr.errorCode == 1) {
               error = "ui.outsideradius";
            } else if (lr.errorCode == 4) {
               error = "ui.wrongelevation";
            } else if (lr.errorCode == 5) {
               error = "ui.danger";
            } else if (lr.errorCode == 6) {
               error = "ui.notreachable";
            } else {
               error = "ui.unknownerror";
            }

            if (MillConfigValues.DEV) {
               WorldUtilities.setBlock(townHall.mw.world, lr.errorPos.getRelative(0.0, 30.0, 0.0), Blocks.GRAVEL);
            }

            ServerSender.sendTranslatedSentence(player, '6', "ui.problemat", pos.distanceDirectionShort(lr.errorPos),
                  error);
         } else {
            lr.location.level = -1;
            BuildingProject project = new BuildingProject(set);
            project.location = lr.location;
            setSign(townHall, lr.location.minx, lr.location.minz, project);
            setSign(townHall, lr.location.maxx, lr.location.minz, project);
            setSign(townHall, lr.location.minx, lr.location.maxz, project);
            setSign(townHall, lr.location.maxx, lr.location.maxz, project);
            townHall.buildingProjects.get(BuildingProject.EnumProjects.CUSTOMBUILDINGS).add(project);
            townHall.noProjectsLeft = false;
            ServerSender.sendTranslatedSentence(player, '2', "ui.projectadded");
         }
      }
   }

   public static void newCustomBuilding(EntityPlayer player, Building townHall, Point pos, String planKey) {
      BuildingCustomPlan customBuilding = townHall.culture.getBuildingCustom(planKey);
      if (customBuilding != null) {
         try {
            townHall.addCustomBuilding(customBuilding, pos);
         } catch (Exception var6) {
            MillLog.printException("Exception when creation custom building: " + planKey, var6);
         }
      }
   }

   public static void newVillageCreation(EntityPlayer player, Point pos, String cultureKey, String villageTypeKey) {
      Culture culture = Culture.getCultureByName(cultureKey);
      if (culture != null) {
         VillageType villageType = culture.getVillageType(villageTypeKey);
         if (villageType != null) {
            WorldGenVillage genVillage = new WorldGenVillage();
            boolean result = genVillage.generateVillageAtPoint(
                  player.world,
                  MillCommonUtilities.random,
                  pos.getiX(),
                  pos.getiY(),
                  pos.getiZ(),
                  player,
                  false,
                  true,
                  false,
                  0,
                  villageType,
                  null,
                  null,
                  0.0F);
            if (result) {
               MillAdvancements.SUMMONING_WAND.grant(player);
               if (villageType.playerControlled
                     && MillAdvancements.VILLAGE_LEADER_ADVANCEMENTS.containsKey(cultureKey)) {
                  MillAdvancements.VILLAGE_LEADER_ADVANCEMENTS.get(cultureKey).grant(player);
               }

               if (villageType.playerControlled && villageType.customCentre != null) {
                  MillAdvancements.AMATEUR_ARCHITECT.grant(player);
               }
            }
         }
      }
   }

   public static void pujasChangeEnchantment(EntityPlayer player, Building temple, int enchantmentId) {
      if (temple != null && temple.pujas != null) {
         temple.pujas.changeEnchantment(enchantmentId);
         temple.sendBuildingPacket(player, false);
         if (temple.pujas.type == 0) {
            MillAdvancements.PUJA.grant(player);
         } else if (temple.pujas.type == 1) {
            MillAdvancements.SACRIFICE.grant(player);
         }
      }
   }

   public static void questCompleteStep(EntityPlayer player, MillVillager villager) {
      UserProfile profile = Mill.getMillWorld(player.world).getProfile(player);
      QuestInstance qi = profile.villagersInQuests.get(villager.getVillagerId());
      if (qi == null) {
         MillLog.error(villager, "Could not find quest instance for this villager.");
      } else {
         qi.completeStep(player, villager);
      }
   }

   public static void questRefuse(EntityPlayer player, MillVillager villager) {
      UserProfile profile = Mill.getMillWorld(player.world).getProfile(player);
      QuestInstance qi = profile.villagersInQuests.get(villager.getVillagerId());
      if (qi == null) {
         MillLog.error(villager, "Could not find quest instance for this villager.");
      } else {
         qi.refuseQuest(player, villager);
      }
   }

   private static void setSign(Building townHall, int i, int j, BuildingProject project) {
      WorldUtilities.setBlockAndMetadata(townHall.world, i, WorldUtilities.findTopSoilBlock(townHall.world, i, j), j,
            Blocks.STANDING_SIGN, 0, true, false);
      TileEntitySign sign = (TileEntitySign) townHall.world
            .getTileEntity(new BlockPos(i, WorldUtilities.findTopSoilBlock(townHall.world, i, j), j));
      if (sign != null) {
         sign.signText[0] = new TextComponentString(project.getNativeName());
         sign.signText[1] = new TextComponentString("");
         sign.signText[2] = new TextComponentString(project.getGameName());
         sign.signText[3] = new TextComponentString("");
      }
   }

   public static void updateCustomBuilding(EntityPlayer player, Building building) {
      if (building.location.getCustomPlan() != null) {
         building.location.getCustomPlan().registerResources(building, building.location);
      }
   }

   public static void useNegationWand(EntityPlayer player, Building townHall) {
      ServerSender.sendTranslatedSentence(player, '4', "negationwand.destroyed", townHall.villageType.name);
      if (!townHall.villageType.lonebuilding) {
         MillAdvancements.SCIPIO.grant(player);
      }

      townHall.destroyVillage();
   }

   public static EnumActionResult useSummoningWand(EntityPlayerMP player, Point pos) {
      MillWorldData mw = Mill.getMillWorld(player.world);
      Block block = WorldUtilities.getBlock(player.world, pos);
      if (mw == null) {
         MillLog.major(null, "FATAL: MillWorldData is null for world " + player.world);
         return EnumActionResult.FAIL;
      }
      Building closestVillage = mw.getClosestVillage(pos);
      if (closestVillage != null
            && pos.squareRadiusDistance(closestVillage.getPos()) < closestVillage.villageType.radius + 10) {
         if (block == Blocks.STANDING_SIGN) {
            return EnumActionResult.FAIL;
         } else if (closestVillage.controlledBy(player)) {
            Building b = closestVillage.getBuildingAtCoordPlanar(pos);
            if (b != null) {
               if (b.location.isCustomBuilding) {
                  ServerSender.displayNewBuildingProjectGUI(player, closestVillage, pos);
               } else {
                  ServerSender.sendTranslatedSentence(player, 'e', "ui.wand_locationinuse");
               }
            } else {
               ServerSender.displayNewBuildingProjectGUI(player, closestVillage, pos);
            }

            return EnumActionResult.SUCCESS;
         } else {
            ServerSender.sendTranslatedSentence(player, 'e', "ui.wand_invillagerange",
                  closestVillage.getVillageQualifiedName());
            return EnumActionResult.FAIL;
         }
      } else if (block != Blocks.STANDING_SIGN) {
         if (block == MillBlocks.LOCKED_CHEST) {
            return EnumActionResult.PASS;
         } else if (block == Blocks.OBSIDIAN) {
            WorldGenVillage genVillage = new WorldGenVillage();
            genVillage.generateVillageAtPoint(
                  player.world, MillCommonUtilities.random, pos.getiX(), pos.getiY(), pos.getiZ(), player, false, true,
                  false, 0, null, null, null, 0.0F);
            return EnumActionResult.SUCCESS;
         } else if (block == Blocks.GOLD_BLOCK) {
            ServerSender.displayNewVillageGUI(player, pos);
            return EnumActionResult.SUCCESS;
         } else if (mw.getProfile(player).isTagSet("normanmarvel_picklocation")) {
            SpecialQuestActions.normanMarvelPickLocation(mw, player, pos);
            return EnumActionResult.SUCCESS;
         } else {
            ServerSender.sendTranslatedSentence(player, 'f', "ui.wandinstruction");
            return EnumActionResult.FAIL;
         }
      } else {
         if (Mill.proxy.isTrueServer() && !player.server.getPlayerList().canSendCommands(player.getGameProfile())) {
            ServerSender.sendTranslatedSentence(player, '4', "ui.serverimportforbidden");
         } else {
            BuildingImportExport.summoningWandImportBuildingRequest(player, Mill.serverWorlds.get(0).world, pos);
         }

         return EnumActionResult.SUCCESS;
      }
   }

   public static void villageChiefPerformBuilding(EntityPlayer player, MillVillager chief, String planKey) {
      BuildingPlan plan = chief.getTownHall().culture.getBuildingPlanSet(planKey).getRandomStartingPlan();
      chief.getTownHall().buildingsBought.add(planKey);
      MillCommonUtilities.changeMoney(player.inventory, -plan.price, player);
      ServerSender.sendTranslatedSentence(player, 'f', "ui.housebought", chief.getName(), plan.nativeName);
   }

   public static void villageChiefPerformCrop(EntityPlayer player, MillVillager chief, String value) {
      UserProfile profile = Mill.getMillWorld(player.world).getProfile(player);
      profile.setTag("cropplanting_" + value);
      MillCommonUtilities.changeMoney(player.inventory, -512, player);
      Item crop = Item.getByNameOrId("millenaire:" + value);
      ServerSender.sendTranslatedSentence(player, 'f', "ui.croplearned", chief.getName(),
            "ui.crop." + crop.getRegistryName().getPath());
   }

   public static void villageChiefPerformCultureControl(EntityPlayer player, MillVillager chief) {
      UserProfile profile = Mill.getMillWorld(player.world).getProfile(player);
      profile.setTag("culturecontrol_" + chief.getCulture().key);
      ServerSender.sendTranslatedSentence(player, 'f', "ui.control_gotten", chief.getName(),
            chief.getCulture().getAdjectiveTranslatedKey());
   }

   public static void villageChiefPerformDiplomacy(EntityPlayer player, MillVillager chief, Point village,
         boolean praise) {
      float effect = 0.0F;
      if (praise) {
         effect = 10.0F;
      } else {
         effect = -10.0F;
      }

      int reputation = Math.min(chief.getTownHall().getReputation(player), 32768);
      float coeff = (float) ((Math.log(reputation) / Math.log(32768.0) * 2.0 + reputation / 32768) / 3.0);
      effect *= coeff;
      effect = (float) (effect * ((MillCommonUtilities.randomInt(40) + 80) / 100.0));
      chief.getTownHall().adjustRelation(village, (int) effect, false);
      UserProfile profile = Mill.getMillWorld(player.world).getProfile(player);
      profile.adjustDiplomacyPoint(chief.getTownHall(), -1);
      if (MillConfigValues.LogVillage >= 1) {
         MillLog.major(chief.getTownHall(), "Adjusted relation by " + effect + " (coef: " + coeff + ")");
      }
   }

   public static void villageChiefPerformHuntingDrop(EntityPlayer player, MillVillager chief, String value) {
      UserProfile profile = Mill.getMillWorld(player.world).getProfile(player);
      profile.setTag("huntingdrop_" + value);
      MillCommonUtilities.changeMoney(player.inventory, -512, player);
      Item drop = Item.getByNameOrId("millenaire:" + value);
      ServerSender.sendTranslatedSentence(
            player, 'f', "ui.huntingdroplearned", chief.getName(),
            "ui.huntingdrop." + drop.getRegistryName().getPath());
   }

   public static void villageChiefPerformVillageScroll(EntityPlayer player, MillVillager chief) {
      for (int i = 0; i < Mill.getMillWorld(player.world).villagesList.pos.size(); i++) {
         Point p = Mill.getMillWorld(player.world).villagesList.pos.get(i);
         if (chief.getTownHall().getPos().sameBlock(p)) {
            MillCommonUtilities.changeMoney(player.inventory, -128, player);
            player.inventory.addItemStackToInventory(ItemParchment.createParchmentForVillage(chief.getTownHall()));
            ServerSender.sendTranslatedSentence(player, 'f', "ui.scrollbought", chief.getName());
         }
      }
   }
}
