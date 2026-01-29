package org.millenaire.common.village.buildingmanagers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import org.millenaire.client.book.TextBook;
import org.millenaire.client.book.TextPage;
import org.millenaire.client.gui.text.GuiText;
import org.millenaire.common.advancements.MillAdvancements;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.TradeGood;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.network.StreamReadWrite;
import org.millenaire.common.utilities.LanguageUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.BuildingProject;
import org.millenaire.common.village.ConstructionIP;
import org.millenaire.common.world.UserProfile;

public class MarvelManager {
   private static final float DONATION_RATIO = 0.5F;
   public static final String NORMAN_MARVEL_COMPLETION_TAG = "normanmarvel_helper";
   private final Building townHall;
   private CopyOnWriteArrayList<String> donationList = new CopyOnWriteArrayList<>();
   private boolean nightActionDone = false;
   private boolean marvelComplete = false;
   private boolean dawnActionDone;

   public MarvelManager(Building building) {
      this.townHall = building;
   }

   private void addPlanCost(BuildingPlan plan, Map<InvItem, Integer> needs) {
      for (InvItem invItem : plan.resCost.keySet()) {
         if (needs.containsKey(invItem)) {
            needs.put(invItem, needs.get(invItem) + plan.resCost.get(invItem));
         } else {
            needs.put(invItem, plan.resCost.get(invItem));
         }
      }
   }

   public Map<InvItem, Integer> computeNeeds() {
      Map<InvItem, Integer> needs = new HashMap<>();

      for (BuildingProject.EnumProjects ep : BuildingProject.EnumProjects.values()) {
         if (this.townHall.buildingProjects.containsKey(ep)) {
            for (BuildingProject project : this.townHall.buildingProjects.get(ep)) {
               if (project.planSet != null) {
                  if (project.location != null && project.location.level >= 0) {
                     boolean obsolete = project.planSet != null
                        && project.location.version != project.planSet.plans.get(project.location.getVariation())[0].version;
                     if (!obsolete && project.location.level + 1 < project.getLevelsNumber(project.location.getVariation())) {
                        List<String> subBuildingsToBuild = new ArrayList<>();
                        BuildingPlan currentPlan = project.planSet.plans.get(0)[project.location.level];

                        for (BuildingPlan plan : project.planSet.plans.get(0)) {
                           if (plan.level > project.location.level) {
                              this.addPlanCost(plan, needs);

                              for (String subBuildingKey : plan.subBuildings) {
                                 if (!subBuildingsToBuild.contains(subBuildingKey) && !currentPlan.subBuildings.contains(subBuildingKey)) {
                                    subBuildingsToBuild.add(subBuildingKey);
                                 }
                              }
                           }
                        }

                        for (String subBuildingKeyx : subBuildingsToBuild) {
                           BuildingPlanSet planSet = this.townHall.culture.getBuildingPlanSet(subBuildingKeyx);

                           for (BuildingPlan planx : planSet.plans.get(0)) {
                              this.addPlanCost(planx, needs);
                           }
                        }
                     }
                  } else {
                     for (BuildingPlan planx : project.planSet.plans.get(0)) {
                        this.addPlanCost(planx, needs);
                     }

                     for (String subBuildingKeyx : project.planSet.plans.get(0)[((BuildingPlan[])project.planSet.plans.get(0)).length - 1].subBuildings) {
                        BuildingPlanSet planSet = this.townHall.culture.getBuildingPlanSet(subBuildingKeyx);

                        for (BuildingPlan planx : planSet.plans.get(0)) {
                           this.addPlanCost(planx, needs);
                        }
                     }
                  }
               }
            }
         }
      }

      for (InvItem invItem : needs.keySet()) {
         needs.put(invItem, needs.get(invItem) - this.townHall.countGoods(invItem));

         for (ConstructionIP cip : this.townHall.getConstructionsInProgress()) {
            if (cip.getBuilder() != null) {
               needs.put(invItem, needs.get(invItem) - cip.getBuilder().countInv(invItem));
            }
         }
      }

      for (InvItem invItem : new HashSet<>(needs.keySet())) {
         if (needs.get(invItem) <= 0) {
            needs.remove(invItem);
         }
      }

      return needs;
   }

   private void gatherDonationsFrom(Building distantTH, Map<InvItem, Integer> needs) {
      String donations = "";

      for (InvItem invItem : needs.keySet()) {
         if (needs.get(invItem) > 0) {
            int gathered = 0;

            for (Building distantBuilding : distantTH.getBuildings()) {
               gathered = (int)(gathered + distantBuilding.estimateAbstractedProductionCapacity(invItem) * 0.5F);
            }

            if (gathered > 0) {
               gathered = Math.min(gathered, needs.get(invItem));
               donations = donations + ";" + distantTH.culture.getTradeGood(invItem).key + "/" + gathered;
               this.townHall.storeGoods(invItem, gathered);
            }
         }
      }

      if (donations.length() > 0) {
         this.getDonationList().add("donation;" + distantTH.getVillageQualifiedName() + donations);
      }
   }

   private void gatherDonationsFromVillages() {
      Map<InvItem, Integer> needs = this.computeNeeds();

      for (Point distantTHPos : this.townHall.getRelations().keySet()) {
         Building distantTH = this.townHall.mw.getBuilding(distantTHPos);
         if (distantTH != null
            && distantTH.culture == this.townHall.culture
            && this.townHall.getRelationWithVillage(distantTHPos) >= 90
            && (distantTH.villageType.isRegularVillage() || distantTH.villageType.isHamlet())) {
            this.gatherDonationsFrom(distantTH, needs);
         }
      }
   }

   public TextBook generateDonationPanelText() {
      TextPage page = new TextPage();
      page.addLine(
         LanguageUtilities.string("panels.marveldonationstitle", this.townHall.getVillageQualifiedName()) + ":",
         "§1",
         new GuiText.GuiButtonReference(this.townHall.villageType)
      );
      page.addLine("");

      for (int i = this.getDonationList().size() - 1; i > -1; i--) {
         String s = this.getDonationList().get(i);
         if (s.split(";").length > 1) {
            if (!s.startsWith("donation;")) {
               page.addLine(LanguageUtilities.string(s.split(";")));
            } else {
               String[] v = s.split(";");
               String givenItemsDesc = "";

               for (int j = 2; j < v.length; j++) {
                  if (givenItemsDesc.length() > 0) {
                     givenItemsDesc = givenItemsDesc + ", ";
                  }

                  givenItemsDesc = givenItemsDesc + MillCommonUtilities.parseItemString(this.townHall.culture, v[j]);
               }

               page.addLine(LanguageUtilities.string("panels.marveldonation", v[1], givenItemsDesc));
            }
         }

         page.addLine("");
      }

      TextBook text = new TextBook();
      text.addPage(page);
      return text;
   }

   public TextBook generateResourcesPanelText() {
      Map<InvItem, Integer> totalCost = this.townHall.villageType.computeVillageTypeCost();
      Map<InvItem, Integer> remainingNeeds = this.computeNeeds();
      TextPage page = new TextPage();
      page.addLine(LanguageUtilities.string("panels.marvelresources"), "§1", new GuiText.GuiButtonReference(this.townHall.villageType));
      page.addLine("");

      for (InvItem invItem : totalCost.keySet()) {
         TradeGood tradeGood = this.townHall.culture.getTradeGood(invItem);
         if (!remainingNeeds.containsKey(invItem)) {
            if (tradeGood != null) {
               page.addLine(invItem.getName() + ": " + totalCost.get(invItem) + "/" + totalCost.get(invItem), "§2", new GuiText.GuiButtonReference(tradeGood));
            } else {
               page.addLine(invItem.getName() + ": " + totalCost.get(invItem) + "/" + totalCost.get(invItem), "§2", invItem.getItemStack(), true);
            }
         } else if (tradeGood != null) {
            page.addLine(
               invItem.getName() + ": " + (totalCost.get(invItem) - remainingNeeds.get(invItem)) + "/" + totalCost.get(invItem),
               new GuiText.GuiButtonReference(tradeGood)
            );
         } else {
            page.addLine(
               invItem.getName() + ": " + (totalCost.get(invItem) - remainingNeeds.get(invItem)) + "/" + totalCost.get(invItem), invItem.getItemStack(), true
            );
         }
      }

      TextBook text = new TextBook();
      text.addPage(page);
      return text;
   }

   public CopyOnWriteArrayList<String> getDonationList() {
      return this.donationList;
   }

   public void readDataStream(PacketBuffer ds) throws IOException {
      this.donationList = StreamReadWrite.readStringList(ds);
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      NBTTagList nbttaglist = nbttagcompound.getTagList("marvelDonationList", 10);

      for (int i = 0; i < nbttaglist.tagCount(); i++) {
         NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
         this.getDonationList().add(nbttagcompound1.getString("donation"));
      }

      this.marvelComplete = nbttagcompound.getBoolean("marvelComplete");
   }

   private void ringMorningBells() {
      List<BuildingProject> projects = this.townHall.getFlatProjectList();
      Point marvelPos = null;

      for (BuildingProject project : projects) {
         if (project.location != null && project.location.containsPlanTag("marvel")) {
            marvelPos = project.location.pos;
         }
      }

      WorldUtilities.playSound(this.townHall.world, marvelPos, Mill.SOUND_NORMAN_BELLS, SoundCategory.RECORDS, 10.0F, 1.0F);

      for (Entity entityplayer : WorldUtilities.getEntitiesWithinAABB(this.townHall.world, EntityPlayer.class, marvelPos, 128, 128)) {
         EntityPlayer player = (EntityPlayer)entityplayer;
         player.addPotionEffect(new PotionEffect(MobEffects.LUCK, 12000, 1, true, true));
         ServerSender.sendTranslatedSentence(player, '9', "marvel.norman.morningbells", this.townHall.getVillageQualifiedName());
      }
   }

   public void sendBuildingPacket(PacketBuffer data) throws IOException {
      StreamReadWrite.writeStringList(this.getDonationList(), data);
   }

   private void testForCompletion() {
      if (!this.marvelComplete) {
         List<BuildingProject> projects = this.townHall.getFlatProjectList();
         boolean justCompleted = false;
         Point marvelPos = null;

         for (BuildingProject project : projects) {
            if (project.location != null
               && project.location.containsPlanTag("marvel")
               && project.location.level + 1 >= project.getLevelsNumber(project.location.getVariation())) {
               justCompleted = true;
               marvelPos = project.location.pos;
            }
         }

         if (justCompleted) {
            this.marvelComplete = true;
            WorldUtilities.playSound(this.townHall.world, marvelPos, Mill.SOUND_NORMAN_BELLS, SoundCategory.RECORDS, 10.0F, 1.0F);
            ServerSender.sendTranslatedSentenceInRange(this.townHall.world, marvelPos, Integer.MAX_VALUE, '9', "marvel.norman.marvelbuilt");
         }
      }

      if (this.marvelComplete) {
         for (UserProfile profile : this.townHall.mw.profiles.values()) {
            if (profile.getPlayer() != null && profile.isTagSet("normanmarvel_helper")) {
               MillAdvancements.MARVEL_NORMAN.grant(profile.getPlayer());
            }
         }
      }
   }

   public void update() {
      if ((this.townHall.world.getWorldTime() + this.hashCode()) % 200L == 120L) {
         this.testForCompletion();
      }

      this.updateNightAction();
      this.updateDawnAction();
   }

   private void updateDawnAction() {
      boolean isDawn = this.townHall.world.getWorldTime() % 24000L > 23500L;
      if (!isDawn) {
         this.dawnActionDone = false;
      } else if (!this.dawnActionDone) {
         if (this.marvelComplete) {
            this.ringMorningBells();
         }

         this.dawnActionDone = true;
      }
   }

   private void updateNightAction() {
      if (this.townHall.world.isDaytime()) {
         this.nightActionDone = false;
      } else if (!this.nightActionDone) {
         if (!this.marvelComplete) {
            this.gatherDonationsFromVillages();
         }

         this.nightActionDone = true;
      }
   }

   public void writeToNBT(NBTTagCompound nbttagcompound) {
      NBTTagList nbttaglist = new NBTTagList();

      for (String s : this.getDonationList()) {
         NBTTagCompound nbttagcompound1 = new NBTTagCompound();
         nbttagcompound1.setString("donation", s);
         nbttaglist.appendTag(nbttagcompound1);
      }

      nbttagcompound.setTag("marvelDonationList", nbttaglist);
      nbttagcompound.setBoolean("marvelComplete", this.marvelComplete);
   }
}
