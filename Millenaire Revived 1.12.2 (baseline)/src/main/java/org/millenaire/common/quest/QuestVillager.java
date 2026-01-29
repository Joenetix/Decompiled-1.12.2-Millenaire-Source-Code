package org.millenaire.common.quest;

import java.util.ArrayList;
import java.util.List;
import org.millenaire.common.village.VillagerRecord;
import org.millenaire.common.world.UserProfile;

public class QuestVillager {
   List<String> forbiddenTags = new ArrayList<>();
   String key = null;
   String relatedto = null;
   String relation = null;
   List<String> requiredTags = new ArrayList<>();
   List<String> types = new ArrayList<>();

   QuestVillager() {
   }

   public boolean testVillager(UserProfile profile, VillagerRecord vr) {
      if (profile.villagersInQuests.containsKey(vr.getVillagerId())) {
         return false;
      } else if (!this.types.isEmpty() && !this.types.contains(vr.type)) {
         return false;
      } else {
         for (String tag : this.requiredTags) {
            String tagPlayer = profile.uuid + "_" + tag;
            if (!vr.questTags.contains(tagPlayer)) {
               return false;
            }
         }

         for (String tagx : this.forbiddenTags) {
            String tagPlayer = profile.uuid + "_" + tagx;
            if (vr.questTags.contains(tagPlayer)) {
               return false;
            }
         }

         for (String tagxx : vr.questTags) {
            String tagPlayer = profile.uuid + "_" + tagxx;
            if (this.forbiddenTags.contains(tagPlayer)) {
               return false;
            }
         }

         return true;
      }
   }
}
