package org.millenaire.common.village.buildingmanagers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.millenaire.client.book.BookManager;
import org.millenaire.client.book.TextBook;
import org.millenaire.client.book.TextLine;
import org.millenaire.client.book.TextPage;
import org.millenaire.client.gui.text.GuiText;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.TradeGood;
import org.millenaire.common.utilities.LanguageUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.BuildingLocation;
import org.millenaire.common.village.BuildingProject;
import org.millenaire.common.village.ConstructionIP;
import org.millenaire.common.village.VillagerRecord;

public class PanelContentGenerator {
   private static void addProjectToList(EntityPlayer player, BuildingProject project, Building townHall, TextPage page) {
      if (project.planSet != null) {
         if (project.location != null && project.location.level >= 0) {
            boolean obsolete = project.planSet != null && project.location.version != project.planSet.plans.get(project.location.getVariation())[0].version;
            if (project.location.level + 1 >= project.getLevelsNumber(project.location.getVariation())) {
               BuildingPlan plan = project.getPlan(project.location.getVariation(), project.location.level);
               BuildingLocation l = project.location;
               page.addLine(plan.getNameNativeAndTranslated(), "§2", new GuiText.GuiButtonReference(project.planSet));
               page.addLine(
                  MathHelper.floor(l.pos.distanceTo(townHall.getPos()))
                     + "m "
                     + townHall.getPos().directionToShort(l.pos)
                     + ", "
                     + LanguageUtilities.string("panels.finished")
                     + "."
               );
            } else if (obsolete) {
               BuildingPlan plan = project.getPlan(project.location.getVariation(), project.location.level);
               BuildingLocation l = project.location;
               page.addLine(plan.getNameNativeAndTranslated(), "§4", new GuiText.GuiButtonReference(project.planSet));
               page.addLine(
                  MathHelper.floor(l.pos.distanceTo(townHall.getPos()))
                     + "m "
                     + townHall.getPos().directionToShort(l.pos)
                     + ", "
                     + LanguageUtilities.string("panels.obsolete")
                     + "."
               );
            } else {
               BuildingPlan plan = project.getPlan(project.location.getVariation(), project.location.level + 1);
               BuildingLocation l = project.location;
               page.addLine(plan.getNameNativeAndTranslated(), new GuiText.GuiButtonReference(project.planSet));
               page.addLine(
                  MathHelper.floor(l.pos.distanceTo(townHall.getPos()))
                     + "m "
                     + townHall.getPos().directionToShort(l.pos)
                     + ", "
                     + LanguageUtilities.string(
                        "panels.nbupgradesleft", "" + (project.getLevelsNumber(project.location.getVariation()) - project.location.level - 1)
                     )
               );
            }
         } else {
            BuildingPlan plan = project.planSet.getRandomStartingPlan();
            page.addLine(plan.getNameNativeAndTranslated(), "§1", new GuiText.GuiButtonReference(project.planSet));
            page.addLine(LanguageUtilities.string("panels.notyetbuilt") + ".");
         }
      }
   }

   public static TextBook generateArchives(Building townHall, long villager_id) {
      if (townHall == null) {
         return null;
      } else {
         VillagerRecord vr = townHall.mw.getVillagerRecordById(villager_id);
         if (vr == null) {
            return null;
         } else {
            TextBook text = new TextBook();
            TextPage page = new TextPage();
            page.addLine(vr.getName(), "§1", new GuiText.GuiButtonReference(vr.getType()));
            page.addLine(vr.getGameOccupation());
            page.addLine("");
            if (vr.mothersName != null && vr.mothersName.length() > 0) {
               page.addLine(LanguageUtilities.string("panels.mother") + ": " + vr.mothersName);
            }

            if (vr.fathersName != null && vr.fathersName.length() > 0) {
               page.addLine(LanguageUtilities.string("panels.father") + ": " + vr.fathersName);
            }

            if (vr.spousesName != null && vr.spousesName.length() > 0) {
               page.addLine(LanguageUtilities.string("panels.spouse") + ": " + vr.spousesName);
            }

            page.addLine("");
            MillVillager villager = null;

            for (MillVillager v : townHall.getKnownVillagers()) {
               if (v.getVillagerId() == vr.getVillagerId()) {
                  villager = v;
               }
            }

            page.addLine("");
            if (villager == null) {
               if (vr.killed) {
                  page.addLine(LanguageUtilities.string("panels.dead"), "§4");
               } else if (vr.awayraiding) {
                  page.addLine(LanguageUtilities.string("panels.awayraiding"));
               } else if (vr.awayhired) {
                  page.addLine(LanguageUtilities.string("panels.awayhired"));
               } else if (vr.raidingVillage && townHall.world.getWorldTime() < vr.raiderSpawn + 500L) {
                  page.addLine(LanguageUtilities.string("panels.invaderincoming"));
               } else {
                  page.addLine(LanguageUtilities.string("panels.missing"), "§4");
               }
            } else {
               String occupation = "";
               if (villager.goalKey != null && Goal.goals.containsKey(villager.goalKey)) {
                  occupation = Goal.goals.get(villager.goalKey).gameName(villager);
               }

               page.addLine(LanguageUtilities.string("panels.currentoccupation") + ": " + occupation);
            }

            text.addPage(page);
            return text;
         }
      }
   }

   public static TextBook generateConstructions(Building townHall) {
      TextPage page = new TextPage();
      page.addLine(
         LanguageUtilities.string("panels.constructions") + " : " + townHall.getVillageQualifiedName(),
         "§1",
         new GuiText.GuiButtonReference(townHall.villageType)
      );
      page.addLine("");

      for (ConstructionIP cip : townHall.getConstructionsInProgress()) {
         if (cip.getBuildingLocation() != null) {
            BuildingPlanSet buildingPlanSet = townHall.culture.getBuildingPlanSet(cip.getBuildingLocation().planKey);
            String planName = buildingPlanSet.getNameNative();
            String status;
            if (cip.getBuildingLocation().level == 0) {
               status = LanguageUtilities.string("ui.inconstruction");
            } else {
               status = LanguageUtilities.string("ui.upgrading", "" + cip.getBuildingLocation().level);
            }

            String loc;
            if (cip.getBuildingLocation() != null) {
               int distance = MathHelper.floor(townHall.getPos().distanceTo(cip.getBuildingLocation().pos));
               String direction = LanguageUtilities.string(townHall.getPos().directionTo(cip.getBuildingLocation().pos));
               loc = LanguageUtilities.string("other.shortdistancedirection", "" + distance, "" + direction);
            } else {
               loc = "";
            }

            page.addLine(planName, "§1", new GuiText.GuiButtonReference(buildingPlanSet));
            page.addLine(status + ", " + loc);
            page.addLine("");
         }
      }

      page.addLine("");

      for (BuildingProject.EnumProjects ep : BuildingProject.EnumProjects.values()) {
         if (townHall.buildingProjects.containsKey(ep)) {
            for (BuildingProject project : townHall.buildingProjects.get(ep)) {
               if (project.location != null) {
                  String level = null;
                  if (project.location.level < 0) {
                     level = LanguageUtilities.string("ui.notyetbuilt");
                  }

                  if (project.location.level > 0) {
                     level = LanguageUtilities.string("panels.upgrade") + " " + project.location.level;
                  }

                  List<String> effects = project.location.getBuildingEffects(townHall.world);
                  String effect = null;
                  if (effects.size() > 0) {
                     effect = "";

                     for (String s : effects) {
                        if (effect.length() > 0) {
                           effect = effect + ", ";
                        }

                        effect = effect + s;
                     }
                  }

                  if (project.location.isCustomBuilding) {
                     page.addLine(project.location.getNativeName(), "§1");
                  } else {
                     page.addLine(project.location.getNativeName(), "§1", new GuiText.GuiButtonReference(project.planSet));
                  }

                  if (project.location.getPlan() != null) {
                     page.addLine(
                        project.location.getPlan().getNameTranslated()
                           + ", "
                           + MathHelper.floor(project.location.pos.distanceTo(townHall.getPos()))
                           + "m "
                           + townHall.getPos().directionToShort(project.location.pos)
                     );
                  } else {
                     page.addLine(
                        MathHelper.floor(project.location.pos.distanceTo(townHall.getPos()))
                           + "m "
                           + townHall.getPos().directionToShort(project.location.pos)
                     );
                  }

                  if (level != null) {
                     page.addLine(level);
                  }

                  if (effect != null) {
                     page.addLine(effect);
                  }

                  page.addLine("");
               }
            }
         }
      }

      TextBook book = new TextBook();
      book.addPage(page);
      return book;
   }

   public static TextBook generateEtatCivil(Building townHall) {
      if (townHall == null) {
         return null;
      } else {
         TextBook book = new TextBook();
         TextPage page = new TextPage();
         TextPage visitorsPage = new TextPage();
         page.addLine(
            LanguageUtilities.string("ui.population") + " " + townHall.getVillageQualifiedName(), "§1", new GuiText.GuiButtonReference(townHall.villageType)
         );
         page.addBlankLine();
         visitorsPage.addLine(LanguageUtilities.string("panels.visitors") + ":", "§1");
         visitorsPage.addBlankLine();

         for (VillagerRecord vr : townHall.getAllVillagerRecords()) {
            int nbFound = 0;
            boolean belongsToVillage = true;
            MillVillager foundVillager = null;

            for (MillVillager villager : townHall.getKnownVillagers()) {
               if (villager.getVillagerId() == vr.getVillagerId()) {
                  nbFound++;
                  belongsToVillage = !villager.isVisitor();
                  foundVillager = villager;
               }
            }

            String error = "";
            if (nbFound != 0) {
               if (nbFound > 1) {
                  error = " (" + LanguageUtilities.string("panels.multiple", "" + nbFound).toLowerCase() + ")";
               }
            } else {
               if (vr.killed) {
                  error = " (" + LanguageUtilities.string("panels.dead").toLowerCase() + ")";
               } else if (vr.awayraiding) {
                  error = " (" + LanguageUtilities.string("panels.awayraiding").toLowerCase() + ")";
               } else if (vr.awayhired) {
                  error = " (" + LanguageUtilities.string("panels.awayhired").toLowerCase() + ")";
               } else if (vr.raidingVillage && townHall.world.getWorldTime() < vr.raiderSpawn + 500L) {
                  error = " (" + LanguageUtilities.string("panels.invaderincoming").toLowerCase() + ")";
               } else if (vr.raidingVillage) {
                  error = " (" + LanguageUtilities.string("panels.raider").toLowerCase() + ")";
               } else {
                  error = " (" + LanguageUtilities.string("panels.missing").toLowerCase() + ")";
               }

               if (MillConfigValues.LogVillagerSpawn >= 1 && Mill.serverWorlds.size() > 0) {
                  Building thServer = Mill.serverWorlds.get(0).getBuilding(townHall.getPos());
                  if (thServer != null) {
                     int nbOnServer = 0;

                     for (MillVillager villagerx : thServer.getKnownVillagers()) {
                        if (villagerx.getVillagerId() == vr.getVillagerId()) {
                           nbOnServer++;
                        }
                     }

                     error = error + " nbOnServer:" + nbOnServer;
                  }
               }
            }

            String debugLine = "Is seller: " + vr.getType().canSell;
            if (foundVillager != null) {
               debugLine = debugLine + ", isDead client: " + foundVillager.isDead + ", isDead server: " + foundVillager.isDeadOnServer;
            }

            if (belongsToVillage) {
               page.addLine(vr.getName() + ", " + vr.getGameOccupation().toLowerCase() + error, new GuiText.GuiButtonReference(vr.getType()));
               if (MillConfigValues.LogVillagerSpawn >= 1) {
                  page.addLine(debugLine);
               }

               page.addBlankLine();
            } else {
               visitorsPage.addLine(vr.getName() + ", " + vr.getGameOccupation().toLowerCase() + error, new GuiText.GuiButtonReference(vr.getType()));
               page.addBlankLine();
            }
         }

         if (MillConfigValues.DEV && Mill.serverWorlds.size() > 0) {
            int nbClient = WorldUtilities.getEntitiesWithinAABB(townHall.world, MillVillager.class, townHall.getPos(), 64, 16).size();
            Building thServer = Mill.serverWorlds.get(0).getBuilding(townHall.getPos());
            int nbServer = WorldUtilities.getEntitiesWithinAABB(thServer.world, MillVillager.class, townHall.getPos(), 64, 16).size();
            page.addLine("Client: " + nbClient + ", server: " + nbServer);
         }

         book.addPage(page);
         book.addPage(visitorsPage);
         return book;
      }
   }

   public static TextBook generateHouse(Building house) {
      TextPage page = new TextPage();
      if (house.location.isCustomBuilding) {
         page.addLine(LanguageUtilities.string("panels.house") + " : " + house.getNativeBuildingName(), "§1");
      } else {
         page.addLine(
            LanguageUtilities.string("panels.house") + " : " + house.getNativeBuildingName(),
            "§1",
            new GuiText.GuiButtonReference(house.culture.getBuildingPlanSet(house.location.planKey))
         );
      }

      page.addLine("");
      VillagerRecord wife = null;
      VillagerRecord husband = null;
      int nbMaleAdults = 0;
      int nbFemaleAdults = 0;

      for (VillagerRecord vr : house.getAllVillagerRecords()) {
         if (vr.gender == 2 && !vr.getType().isChild) {
            wife = vr;
            nbFemaleAdults++;
         }

         if (vr.gender == 1 && !vr.getType().isChild) {
            husband = vr;
            nbMaleAdults++;
         }
      }

      if (house.getAllVillagerRecords().size() == 0) {
         page.addLine(LanguageUtilities.string("panels.houseunoccupied"));
      } else if ((wife != null || husband != null) && nbMaleAdults < 2 && nbFemaleAdults < 2) {
         if (wife == null) {
            page.addLine(
               LanguageUtilities.string("panels.man") + ": " + husband.getName() + ", " + husband.getGameOccupation(),
               new GuiText.GuiButtonReference(husband.getType())
            );
            page.addLine("");
            if (house.location.getFemaleResidents().size() == 0) {
               page.addLine(LanguageUtilities.string("panels.nofemaleresident"));
            } else {
               page.addLine(LanguageUtilities.string("panels.bachelor"));
            }
         } else if (husband == null) {
            page.addLine(
               LanguageUtilities.string("panels.woman") + ": " + wife.getName() + ", " + wife.getGameOccupation(),
               new GuiText.GuiButtonReference(wife.getType())
            );
            page.addLine("");
            if (house.location.getMaleResidents() != null && house.location.getMaleResidents().size() != 0) {
               page.addLine(LanguageUtilities.string("panels.spinster"));
            } else {
               page.addLine(LanguageUtilities.string("panels.nomaleresident"));
            }
         } else {
            page.addLine(
               LanguageUtilities.string("panels.woman") + ": " + wife.getName() + ", " + wife.getGameOccupation().toLowerCase(),
               new GuiText.GuiButtonReference(wife.getType())
            );
            page.addLine(
               LanguageUtilities.string("panels.man") + ": " + husband.getName() + ", " + husband.getGameOccupation().toLowerCase(),
               new GuiText.GuiButtonReference(husband.getType())
            );
            if (house.getAllVillagerRecords().size() > 2) {
               page.addLine("");
               page.addLine(LanguageUtilities.string("panels.children") + ":");
               page.addLine("");

               for (VillagerRecord vr : house.getAllVillagerRecords()) {
                  if (vr.getType().isChild) {
                     page.addLine(vr.getName() + ", " + vr.getGameOccupation().toLowerCase(), new GuiText.GuiButtonReference(vr.getType()));
                  }
               }
            }
         }
      } else {
         for (VillagerRecord vrx : house.getAllVillagerRecords()) {
            page.addLine(vrx.getName() + ", " + vrx.getGameOccupation().toLowerCase(), new GuiText.GuiButtonReference(vrx.getType()));
         }
      }

      TextBook book = new TextBook();
      book.addPage(page);
      return book;
   }

   public static TextBook generateInnGoods(Building house) {
      TextPage page = new TextPage();
      if (house.location.isCustomBuilding) {
         page.addLine(house.getNativeBuildingName(), "§1");
      } else {
         page.addLine(house.getNativeBuildingName(), "§1", new GuiText.GuiButtonReference(house.culture.getBuildingPlanSet(house.location.planKey)));
      }

      page.addBlankLine();
      page.addLine(LanguageUtilities.string("panels.goodstraded") + ":");
      page.addLine("");
      page.addLine(LanguageUtilities.string("panels.goodsimported") + ":");
      page.addLine("");

      for (InvItem good : house.imported.keySet()) {
         TradeGood tradeGood = house.culture.getTradeGood(good);
         if (tradeGood == null) {
            page.addLine(good.getName() + ": " + house.imported.get(good), good.getItemStack(), true);
         } else {
            page.addLine(good.getName() + ": " + house.imported.get(good), new GuiText.GuiButtonReference(tradeGood));
         }
      }

      page.addLine("");
      page.addLine(LanguageUtilities.string("panels.goodsexported") + ":");
      page.addLine("");

      for (InvItem goodx : house.exported.keySet()) {
         TradeGood tradeGood = house.culture.getTradeGood(goodx);
         if (tradeGood == null) {
            page.addLine(goodx.getName() + ": " + house.exported.get(goodx), goodx.getItemStack(), true);
         } else {
            page.addLine(goodx.getName() + ": " + house.exported.get(goodx), new GuiText.GuiButtonReference(tradeGood));
         }
      }

      TextBook text = new TextBook();
      text.addPage(page);
      return text;
   }

   public static TextBook generateInnVisitors(Building house) {
      TextPage page = new TextPage();
      if (house.location.isCustomBuilding) {
         page.addLine(LanguageUtilities.string("panels.innvisitors", house.getNativeBuildingName()) + ":", "§1");
      } else {
         page.addLine(
            LanguageUtilities.string("panels.innvisitors", house.getNativeBuildingName()) + ":",
            "§1",
            new GuiText.GuiButtonReference(house.culture.getBuildingPlanSet(house.location.planKey))
         );
      }

      page.addLine("");

      for (int i = house.visitorsList.size() - 1; i > -1; i--) {
         String s = house.visitorsList.get(i);
         if (s.split(";").length <= 1) {
            page.addLine(s);
         } else if (s.startsWith("storedexports;")) {
            String[] v = s.split(";");
            String taken = "";

            for (int j = 2; j < v.length; j++) {
               if (taken.length() > 0) {
                  taken = taken + ", ";
               }

               taken = taken + MillCommonUtilities.parseItemString(house.culture, v[j]);
            }

            page.addLine(LanguageUtilities.string("panels.storedexports", v[1], taken));
         } else if (!s.startsWith("broughtimport;")) {
            page.addLine(LanguageUtilities.string(s.split(";")));
         } else {
            String[] v = s.split(";");
            String taken = "";

            for (int j = 2; j < v.length; j++) {
               if (taken.length() > 0) {
                  taken = taken + ", ";
               }

               taken = taken + MillCommonUtilities.parseItemString(house.culture, v[j]);
            }

            page.addLine(LanguageUtilities.string("panels.broughtimport", v[1], taken));
         }

         page.addLine("");
      }

      TextBook text = new TextBook();
      text.addPage(page);
      return text;
   }

   public static TextBook generateMilitary(Building townHall) {
      TextBook book = new TextBook();
      TextPage page = new TextPage();
      page.addLine(
         LanguageUtilities.string("panels.military") + " : " + townHall.getVillageQualifiedName(), "§1", new GuiText.GuiButtonReference(townHall.villageType)
      );
      page.addLine("");
      int nbAttackers = 0;
      Point attackingVillagePos = null;
      if (townHall.raidTarget != null) {
         Building target = Mill.clientWorld.getBuilding(townHall.raidTarget);
         if (target != null) {
            if (townHall.raidStart > 0L) {
               page.addLine(
                  LanguageUtilities.string(
                     "panels.raidinprogresslong",
                     target.getVillageQualifiedName(),
                     "" + Math.round((float)((townHall.world.getWorldTime() - townHall.raidStart) / 1000L))
                  )
               );
            } else {
               page.addLine(
                  LanguageUtilities.string(
                     "panels.planningraidlong",
                     target.getVillageQualifiedName(),
                     "" + Math.round((float)((townHall.world.getWorldTime() - townHall.raidPlanningStart) / 1000L))
                  )
               );
            }

            page.addLine("");
         }
      } else {
         for (VillagerRecord vr : townHall.getAllVillagerRecords()) {
            if (vr.raidingVillage) {
               nbAttackers++;
               attackingVillagePos = vr.originalVillagePos;
            }
         }

         if (nbAttackers > 0) {
            Building attackingVillage = Mill.clientWorld.getBuilding(attackingVillagePos);
            String attackedBy;
            if (attackingVillage != null) {
               attackedBy = attackingVillage.getVillageQualifiedName();
            } else {
               attackedBy = LanguageUtilities.string("panels.unknownattacker");
            }

            page.addLine(LanguageUtilities.string("panels.underattacklong", "" + nbAttackers, "" + townHall.getVillageAttackerStrength(), attackedBy));
            page.addLine("");
         }
      }

      page.addLine(LanguageUtilities.string("panels.offenselong", "" + townHall.getVillageRaidingStrength()));
      page.addLine(LanguageUtilities.string("panels.defenselong", "" + townHall.getVillageDefendingStrength()));
      book.addPage(page);
      page = new TextPage();
      page.addLine(LanguageUtilities.string("panels.villagefighters"), "§1");
      page.addLine("");

      for (VillagerRecord vrx : townHall.getAllVillagerRecords()) {
         if ((vrx.getType().isRaider || vrx.getType().helpInAttacks) && !vrx.raidingVillage) {
            String status = "";
            if (vrx.getType().helpInAttacks) {
               status = status + LanguageUtilities.string("panels.defender");
            }

            if (vrx.getType().isRaider) {
               if (status.length() > 0) {
                  status = status + ", ";
               }

               status = status + LanguageUtilities.string("panels.raider");
            }

            if (vrx.awayraiding) {
               status = status + ", " + LanguageUtilities.string("panels.awayraiding");
            } else if (vrx.awayhired) {
               status = status + ", " + LanguageUtilities.string("panels.awayhired");
            } else if (vrx.raidingVillage && townHall.world.getWorldTime() < vrx.raiderSpawn + 500L) {
               status = status + ", " + LanguageUtilities.string("panels.invaderincoming");
            } else if (vrx.killed) {
               status = status + ", " + LanguageUtilities.string("panels.dead");
            }

            String weapon = "";
            Item bestMelee = vrx.getBestMeleeWeapon();
            if (bestMelee != null) {
               weapon = new ItemStack(bestMelee).getDisplayName();
            }

            if (vrx.getType().isArcher && vrx.countInv(Items.BOW) > 0) {
               if (weapon.length() > 0) {
                  weapon = weapon + ", ";
               }

               weapon = weapon + new ItemStack(Items.BOW).getDisplayName();
            }

            page.addLine(vrx.getName() + ", " + vrx.getGameOccupation(), new GuiText.GuiButtonReference(vrx.getType()));
            page.addLine(status);
            page.addLine(
               LanguageUtilities.string("panels.health")
                  + ": "
                  + vrx.getMaxHealth()
                  + ", "
                  + LanguageUtilities.string("panels.armour")
                  + ": "
                  + vrx.getTotalArmorValue()
                  + ", "
                  + LanguageUtilities.string("panels.weapons")
                  + ": "
                  + weapon
                  + ", "
                  + LanguageUtilities.string("panels.militarystrength")
                  + ": "
                  + vrx.getMilitaryStrength()
            );
            page.addLine("");
         }
      }

      book.addPage(page);
      if (nbAttackers > 0) {
         page = new TextPage();
         page.addLine(LanguageUtilities.string("panels.attackers"), "§4");
         page.addLine("");

         for (VillagerRecord vrxx : townHall.getAllVillagerRecords()) {
            if (vrxx.raidingVillage) {
               String statusx = "";
               if (vrxx.killed) {
                  statusx = LanguageUtilities.string("panels.dead");
               }

               String weaponx = "";
               Item bestMeleex = vrxx.getBestMeleeWeapon();
               if (bestMeleex != null) {
                  weaponx = new ItemStack(bestMeleex).getDisplayName();
               }

               if (vrxx.getType().isArcher && vrxx.countInv(Items.BOW) > 0) {
                  if (weaponx.length() > 0) {
                     weaponx = weaponx + ", ";
                  }

                  weaponx = weaponx + new ItemStack(Items.BOW).getDisplayName();
               }

               page.addLine(vrxx.getName() + ", " + vrxx.getGameOccupation(), new GuiText.GuiButtonReference(vrxx.getType()));
               page.addLine(statusx);
               page.addLine(
                  LanguageUtilities.string("panels.health")
                     + ": "
                     + vrxx.getMaxHealth()
                     + ", "
                     + LanguageUtilities.string("panels.armour")
                     + ": "
                     + vrxx.getTotalArmorValue()
                     + ", "
                     + LanguageUtilities.string("panels.weapons")
                     + ": "
                     + weaponx
                     + ", "
                     + LanguageUtilities.string("panels.militarystrength")
                     + ": "
                     + vrxx.getMilitaryStrength()
               );
               page.addLine("");
            }
         }

         book.addPage(page);
      }

      if (townHall.raidsPerformed.size() > 0) {
         page = new TextPage();
         page.addLine(LanguageUtilities.string("panels.raidsperformed"), "§1");
         page.addLine("");

         for (int i = townHall.raidsPerformed.size() - 1; i >= 0; i--) {
            String s = townHall.raidsPerformed.get(i);
            if (s.split(";").length <= 1) {
               page.addLine(townHall.raidsPerformed.get(i));
            } else if (s.split(";")[0].equals("failure")) {
               page.addLine(LanguageUtilities.string("raid.historyfailure", s.split(";")[1]), "§4");
            } else {
               String[] v = s.split(";");
               String taken = "";

               for (int j = 2; j < v.length; j++) {
                  if (taken.length() > 0) {
                     taken = taken + ", ";
                  }

                  taken = taken + MillCommonUtilities.parseItemString(townHall.culture, v[j]);
               }

               if (taken.length() == 0) {
                  taken = LanguageUtilities.string("raid.nothing");
               }

               page.addLine(LanguageUtilities.string("raid.historysuccess", s.split(";")[1], taken), "§2");
            }

            page.addLine("");
         }

         book.addPage(page);
      }

      if (townHall.raidsSuffered.size() > 0) {
         page = new TextPage();
         page.addLine(LanguageUtilities.string("panels.raidssuffered"), "§4");
         page.addLine("");

         for (int i = townHall.raidsSuffered.size() - 1; i >= 0; i--) {
            String s = townHall.raidsSuffered.get(i);
            if (s.split(";").length <= 1) {
               page.addLine(townHall.raidsSuffered.get(i));
            } else if (s.split(";")[0].equals("failure")) {
               page.addLine(LanguageUtilities.string("raid.historydefended", s.split(";")[1]), "§2");
            } else {
               String[] v = s.split(";");
               String taken = "";

               for (int j = 2; j < v.length; j++) {
                  if (taken.length() > 0) {
                     taken = taken + ", ";
                  }

                  taken = taken + MillCommonUtilities.parseItemString(townHall.culture, v[j]);
               }

               if (taken.length() == 0) {
                  taken = LanguageUtilities.string("raid.nothing");
               }

               page.addLine(LanguageUtilities.string("raid.historyraided", s.split(";")[1], taken), "§4");
            }

            page.addLine("");
         }

         book.addPage(page);
      }

      return book;
   }

   public static TextBook generateProjects(EntityPlayer player, Building townHall) {
      if (townHall.villageType == null) {
         return null;
      } else {
         TextPage page = new TextPage();
         page.addLine(
            LanguageUtilities.string("panels.buildingprojects") + " : " + townHall.getVillageQualifiedName(),
            "§1",
            new GuiText.GuiButtonReference(townHall.villageType)
         );
         page.addLine("");

         for (BuildingProject.EnumProjects ep : BuildingProject.EnumProjects.values()) {
            if (townHall.buildingProjects.containsKey(ep)) {
               boolean addCategory = false;
               if (ep == BuildingProject.EnumProjects.WALLBUILDING) {
                  addCategory = false;
               } else if (townHall.villageType.playerControlled) {
                  if (ep == BuildingProject.EnumProjects.CENTRE || ep == BuildingProject.EnumProjects.START || ep == BuildingProject.EnumProjects.CORE) {
                     addCategory = true;
                  }
               } else if (ep != BuildingProject.EnumProjects.CUSTOMBUILDINGS) {
                  addCategory = true;
               }

               if (addCategory) {
                  List<BuildingProject> projectsLevel = townHall.buildingProjects.get(ep);
                  page.addLine(LanguageUtilities.string(ep.labelKey) + ":", "§1");
                  page.addLine("");

                  for (BuildingProject project : projectsLevel) {
                     if (townHall.isDisplayableProject(project)) {
                        addProjectToList(player, project, townHall, page);
                     }
                  }

                  page.addLine("");
               }
            }
         }

         TextBook book = new TextBook();
         book.addPage(page);
         return book;
      }
   }

   public static TextBook generateResources(Building house) {
      TextPage page = new TextPage();
      TextBook book = new TextBook();
      page.addLine(LanguageUtilities.string("panels.resources") + ": " + house.getNativeBuildingName(), "§1", new GuiText.GuiButtonReference(house.villageType));
      page.addLine("");
      BuildingPlan goalPlan = house.getCurrentGoalBuildingPlan();
      List<InvItem> res = new ArrayList<>();
      HashMap<InvItem, Integer> resCost = new HashMap<>();
      HashMap<InvItem, Integer> resHas = new HashMap<>();
      if (goalPlan != null) {
         for (InvItem key : goalPlan.resCost.keySet()) {
            res.add(key);
            resCost.put(key, goalPlan.resCost.get(key));
            int has = house.countGoods(key.getItem(), key.meta);

            for (ConstructionIP cip : house.getConstructionsInProgress()) {
               if (cip.getBuilder() != null && cip.getBuildingLocation() != null && cip.getBuildingLocation().planKey.equals(house.buildingGoal)) {
                  has += cip.getBuilder().countInv(key.getItem(), key.meta);
               }
            }

            if (has > goalPlan.resCost.get(key)) {
               has = goalPlan.resCost.get(key);
            }

            resHas.put(key, has);
         }

         page.addLine(LanguageUtilities.string("panels.resourcesneeded") + ":");
         String gameName = goalPlan.getNameTranslated();
         String name;
         if (goalPlan.nativeName != null && goalPlan.nativeName.length() > 0) {
            name = goalPlan.nativeName;
         } else if (goalPlan.getNameTranslated() != null && goalPlan.getNameTranslated().length() > 0) {
            name = goalPlan.getNameTranslated();
            gameName = "";
         } else {
            name = "";
         }

         if (gameName != null && gameName.length() > 0) {
            name = name + " (" + gameName + ")";
         }

         String status = "";
         ConstructionIP projectCIP = null;

         for (ConstructionIP cipx : house.getConstructionsInProgress()) {
            if (projectCIP == null && cipx.getBuildingLocation() != null && cipx.getBuildingLocation().planKey.equals(house.buildingGoal)) {
               projectCIP = cipx;
            }
         }

         if (projectCIP != null) {
            if (projectCIP.getBuildingLocation().level == 0) {
               status = LanguageUtilities.string("ui.inconstruction");
            } else {
               status = LanguageUtilities.string("ui.upgrading") + " (" + projectCIP.getBuildingLocation().level + ")";
            }
         } else {
            status = LanguageUtilities.string(house.buildingGoalIssue);
         }

         page.addLine(name + " - " + status);
         page.addLine("");
         Collections.sort(res, new MillVillager.InvItemAlphabeticalComparator());

         for (int i = 0; i < res.size(); i += 2) {
            String colour;
            if (resHas.get(res.get(i)) >= resCost.get(res.get(i))) {
               colour = "§2";
            } else {
               colour = "§4";
            }

            TradeGood tradeGood = house.culture.getTradeGood(res.get(i));
            TextLine column1;
            if (tradeGood == null) {
               column1 = new TextLine(resHas.get(res.get(i)) + "/" + resCost.get(res.get(i)), colour, res.get(i).getItemStack(), true);
            } else {
               column1 = new TextLine(resHas.get(res.get(i)) + "/" + resCost.get(res.get(i)), colour, new GuiText.GuiButtonReference(tradeGood));
            }

            if (i + 1 < res.size()) {
               if (resHas.get(res.get(i + 1)) >= resCost.get(res.get(i + 1))) {
                  colour = "§2";
               } else {
                  colour = "§4";
               }

               tradeGood = house.culture.getTradeGood(res.get(i + 1));
               TextLine column2;
               if (tradeGood == null) {
                  column2 = new TextLine(resHas.get(res.get(i + 1)) + "/" + resCost.get(res.get(i + 1)), colour, res.get(i + 1).getItemStack(), true);
               } else {
                  column2 = new TextLine(resHas.get(res.get(i + 1)) + "/" + resCost.get(res.get(i + 1)), colour, new GuiText.GuiButtonReference(tradeGood));
               }

               page.addLineWithColumns(column1, column2);
            } else {
               page.addLine(column1);
            }
         }

         book.addPage(page);
         page = new TextPage();
      }

      page.addLine(LanguageUtilities.string("panels.resourcesavailable") + ":", "§1");
      page.addLine("");
      HashMap<InvItem, Integer> contents = house.getResManager().getChestsContent();
      List<InvItem> keys = new ArrayList<>(contents.keySet());
      Collections.sort(keys, new MillVillager.InvItemAlphabeticalComparator());
      List<TextLine> infoColumns = new ArrayList<>();

      for (InvItem key : keys) {
         TradeGood tradeGoodx = house.culture.getTradeGood(key);
         if (tradeGoodx == null) {
            infoColumns.add(new TextLine("" + contents.get(key), key.getItemStack(), true));
         } else {
            infoColumns.add(new TextLine("" + contents.get(key), new GuiText.GuiButtonReference(tradeGoodx)));
         }
      }

      for (TextLine l : BookManager.splitInColumns(infoColumns, 4)) {
         page.addLine(l);
      }

      book.addPage(page);
      return book;
   }

   public static TextBook generateSummary(Building townHall) {
      TextPage page = new TextPage();
      TextBook text = new TextBook();
      page.addLine(
         LanguageUtilities.string("panels.villagesummary") + ": " + townHall.getVillageQualifiedName(),
         "§1",
         new GuiText.GuiButtonReference(townHall.villageType)
      );
      page.addLine("");
      int nbMen = 0;
      int nbFemale = 0;
      int nbGrownBoy = 0;
      int nbGrownGirl = 0;
      int nbBoy = 0;
      int nbGirl = 0;

      for (VillagerRecord vr : townHall.getAllVillagerRecords()) {
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

      page.addLine(LanguageUtilities.string("ui.populationnumber", "" + (nbMen + nbFemale + nbGrownBoy + nbGrownGirl + nbBoy + nbGirl)));
      page.addLine(LanguageUtilities.string("ui.adults", "" + (nbMen + nbFemale), "" + nbMen, "" + nbFemale));
      page.addLine(LanguageUtilities.string("ui.teens", "" + (nbGrownBoy + nbGrownGirl), "" + nbGrownBoy, "" + nbGrownGirl));
      page.addLine(LanguageUtilities.string("ui.children", "" + (nbBoy + nbGirl), "" + nbBoy, "" + nbGirl));
      page.addLine("");
      if (townHall.buildingGoal == null) {
         page.addLine(LanguageUtilities.string("ui.goalscompleted1") + " " + LanguageUtilities.string("ui.goalscompleted2"));
      } else {
         BuildingPlan goal = townHall.getCurrentGoalBuildingPlan();
         ConstructionIP goalCIP = null;

         for (ConstructionIP cip : townHall.getConstructionsInProgress()) {
            if (cip.getBuildingLocation() != null && cip.getBuildingLocation().planKey.equals(townHall.buildingGoal)) {
               goalCIP = cip;
            }
         }

         String status;
         if (goalCIP != null) {
            if (goalCIP.getBuildingLocation().level == 0) {
               status = LanguageUtilities.string("ui.inconstruction");
            } else {
               status = LanguageUtilities.string("ui.upgrading", "" + goalCIP.getBuildingLocation().level);
            }
         } else {
            status = LanguageUtilities.string(townHall.buildingGoalIssue);
         }

         page.addLine(LanguageUtilities.string("panels.buildingproject") + " " + goal.nativeName + " " + goal.getNameTranslated() + ": " + status);
         List<InvItem> res = new ArrayList<>();
         HashMap<InvItem, Integer> resCost = new HashMap<>();
         HashMap<InvItem, Integer> resHas = new HashMap<>();

         for (InvItem key : goal.resCost.keySet()) {
            res.add(key);
            resCost.put(key, goal.resCost.get(key));
            int has = townHall.countGoods(key.getItem(), key.meta);

            for (ConstructionIP cipx : townHall.getConstructionsInProgress()) {
               if (cipx.getBuilder() != null && cipx.getBuildingLocation() != null && cipx.getBuildingLocation().planKey.equals(townHall.buildingGoal)) {
                  has += cipx.getBuilder().countInv(key.getItem(), key.meta);
               }
            }

            if (has > goal.resCost.get(key)) {
               has = goal.resCost.get(key);
            }

            resHas.put(key, has);
         }

         page.addLine("");
         page.addLine(LanguageUtilities.string("panels.resourcesneeded") + ":");
         page.addLine("");
         Collections.sort(res, new MillVillager.InvItemAlphabeticalComparator());
         List<TextLine> infoColumns = new ArrayList<>();

         for (int i = 0; i < res.size(); i++) {
            TradeGood tradeGood = townHall.culture.getTradeGood(res.get(i));
            String colour;
            if (resHas.get(res.get(i)) >= resCost.get(res.get(i))) {
               colour = "§2";
            } else {
               colour = "§4";
            }

            tradeGood = townHall.culture.getTradeGood(res.get(i));
            TextLine line;
            if (tradeGood == null) {
               line = new TextLine(resHas.get(res.get(i)) + "/" + resCost.get(res.get(i)), colour, res.get(i).getItemStack(), true);
            } else {
               line = new TextLine(resHas.get(res.get(i)) + "/" + resCost.get(res.get(i)), colour, new GuiText.GuiButtonReference(tradeGood));
            }

            infoColumns.add(line);
         }

         for (TextLine l : BookManager.splitInColumns(infoColumns, 2)) {
            page.addLine(l);
         }
      }

      page.addLine("");
      page.addLine(LanguageUtilities.string("panels.currentconstruction"));
      boolean constructionIP = false;

      for (ConstructionIP cipxx : townHall.getConstructionsInProgress()) {
         if (cipxx.getBuildingLocation() != null) {
            String planName = townHall.culture.getBuildingPlanSet(cipxx.getBuildingLocation().planKey).getNameNative();
            String status;
            if (cipxx.getBuildingLocation().level == 0) {
               status = LanguageUtilities.string("ui.inconstruction");
            } else {
               status = LanguageUtilities.string("ui.upgrading", "" + cipxx.getBuildingLocation().level);
            }

            int distance = MathHelper.floor(townHall.getPos().distanceTo(cipxx.getBuildingLocation().pos));
            String direction = LanguageUtilities.string(townHall.getPos().directionTo(cipxx.getBuildingLocation().pos));
            String loc = LanguageUtilities.string("other.shortdistancedirection", "" + distance, "" + direction);
            MillVillager builder = null;

            for (MillVillager v : townHall.getKnownVillagers()) {
               if (v.constructionJobId == cipxx.getId()) {
                  builder = v;
               }
            }

            String builderStr = "";
            if (builder != null) {
               builderStr = " - " + builder.getName();
            }

            page.addLine(planName + ": " + status + " - " + loc + builderStr);
            page.addLine("");
         }
      }

      text.addPage(page);
      return text;
   }

   public static TextBook generateVillageMap(Building house) {
      TextBook text = new TextBook();
      TextPage page = new TextPage();
      page.addLine(LanguageUtilities.string("ui.villagemap") + ": " + house.getNativeBuildingName(), "§1", new GuiText.GuiButtonReference(house.villageType));
      text.addPage(page);
      page = new TextPage();
      page.addLine(LanguageUtilities.string("panels.mapwarning"));
      page.addLine("");
      page.addLine(LanguageUtilities.string("panels.mappurple"));
      page.addLine(LanguageUtilities.string("panels.mapblue"));
      page.addLine(LanguageUtilities.string("panels.mapgreen"));
      page.addLine(LanguageUtilities.string("panels.maplightgreen"));
      page.addLine(LanguageUtilities.string("panels.mapred"));
      page.addLine(LanguageUtilities.string("panels.mapyellow"));
      page.addLine(LanguageUtilities.string("panels.maporange"));
      page.addLine(LanguageUtilities.string("panels.maplightblue"));
      page.addLine(LanguageUtilities.string("panels.mapbrown"));
      text.addPage(page);
      return text;
   }

   public static TextBook generateVisitors(Building building, boolean isMarket) {
      if (building == null) {
         return null;
      } else {
         TextBook text = new TextBook();
         TextPage page = new TextPage();
         if (building.location.isCustomBuilding) {
            page.addLine(building.getNativeBuildingName(), "§1");
         } else {
            page.addLine(building.getNativeBuildingName(), "§1", new GuiText.GuiButtonReference(building.culture.getBuildingPlanSet(building.location.planKey)));
         }

         page.addBlankLine();
         if (isMarket) {
            page.addLine(LanguageUtilities.string("panels.merchantlist") + ": ");
            page.addLine("(" + LanguageUtilities.string("panels.capacity") + ": " + building.getResManager().stalls.size() + ")");
            page.addLine("");
         } else {
            page.addLine(LanguageUtilities.string("panels.visitorlist") + ": ");
            page.addLine("(" + LanguageUtilities.string("panels.capacity") + ": " + building.location.getVisitors().size() + ")");
            page.addLine("");
         }

         for (VillagerRecord vr : building.getAllVillagerRecords()) {
            MillVillager v = null;

            for (MillVillager av : building.getKnownVillagers()) {
               if (vr.matches(av)) {
                  v = av;
               }
            }

            page.addLine(vr.getName());
            if (v == null) {
               if (vr.killed) {
                  page.addLine(LanguageUtilities.string("panels.dead"));
               } else {
                  page.addLine(LanguageUtilities.string("panels.missing"));
               }
            } else {
               page.addLine(v.getNativeOccupationName());
               page.addLine(LanguageUtilities.string("panels.nbnightsin", "" + v.visitorNbNights));
               page.addLine("");
            }
         }

         text.addPage(page);
         return text;
      }
   }

   public static TextBook generateWalls(EntityPlayer player, Building townHall) {
      if (townHall.villageType == null) {
         return null;
      } else {
         TextPage page = new TextPage();
         page.addLine(
            LanguageUtilities.string("panels.walls") + " : " + townHall.getVillageQualifiedName(), "§1", new GuiText.GuiButtonReference(townHall.villageType)
         );
         page.addLine("");
         int wallLevel = townHall.computeCurrentWallLevel();
         if (wallLevel >= 0 && wallLevel < Integer.MAX_VALUE) {
            PanelManager.WallStatusInfos wallInfos = townHall.getPanelManager().computeWallInfos(townHall.getFlatProjectList(), wallLevel);
            page.addLine(
               LanguageUtilities.string(
                  "panels.wallslevel", "" + wallLevel, "" + wallInfos.segmentsDone, "" + (wallInfos.segmentsDone + wallInfos.segmentsToDo)
               )
            );
            page.addLine("");
            page.addLine(LanguageUtilities.string("panels.wallsres"));

            for (int i = 0; i < wallInfos.resources.size(); i += 2) {
               PanelManager.ResourceLine resLineLeft = wallInfos.resources.get(i);
               String colour;
               if (resLineLeft.has >= resLineLeft.cost) {
                  colour = "§2";
               } else {
                  colour = "§4";
               }

               TradeGood tradeGood = townHall.culture.getTradeGood(resLineLeft.res);
               TextLine column1;
               if (tradeGood == null) {
                  column1 = new TextLine(resLineLeft.has + "/" + resLineLeft.cost, colour, resLineLeft.res.getItemStack(), true);
               } else {
                  column1 = new TextLine(resLineLeft.has + "/" + resLineLeft.cost, colour, new GuiText.GuiButtonReference(tradeGood));
               }

               if (i + 1 < wallInfos.resources.size()) {
                  PanelManager.ResourceLine resLineRight = wallInfos.resources.get(i + 1);
                  if (resLineRight.has >= resLineRight.cost) {
                     colour = "§2";
                  } else {
                     colour = "§4";
                  }

                  tradeGood = townHall.culture.getTradeGood(resLineRight.res);
                  TextLine column2;
                  if (tradeGood == null) {
                     column2 = new TextLine(resLineRight.has + "/" + resLineRight.cost, colour, resLineRight.res.getItemStack(), true);
                  } else {
                     column2 = new TextLine(resLineRight.has + "/" + resLineRight.cost, colour, new GuiText.GuiButtonReference(tradeGood));
                  }

                  page.addLineWithColumns(column1, column2);
               } else {
                  page.addLine(column1);
               }
            }

            page.addLine("");
         }

         if (townHall.buildingProjects.containsKey(BuildingProject.EnumProjects.WALLBUILDING)) {
            BuildingProject.EnumProjects ep = BuildingProject.EnumProjects.WALLBUILDING;
            List<BuildingProject> projectsLevel = townHall.buildingProjects.get(ep);
            page.addLine(LanguageUtilities.string("panels.wallssegments"));

            for (BuildingProject project : projectsLevel) {
               if (townHall.isDisplayableProject(project)) {
                  addProjectToList(player, project, townHall, page);
               }
            }

            page.addLine("");
         }

         TextBook book = new TextBook();
         book.addPage(page);
         return book;
      }
   }
}
