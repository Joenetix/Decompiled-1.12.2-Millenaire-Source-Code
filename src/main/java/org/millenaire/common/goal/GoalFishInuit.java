package org.millenaire.common.goal;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.utilities.MillCommonUtilities;

@DocumentedElement.Documentation("Extension of basic fishing goal that also brings in bone block.")
public class GoalFishInuit extends GoalFish {
   @Override
   protected void addFishResults(MillVillager villager) {
      villager.addToInv(Items.FISH, 1);
      if (MillCommonUtilities.chanceOn(4)) {
         villager.addToInv(Blocks.BONE_BLOCK, 1);
      }
   }
}
