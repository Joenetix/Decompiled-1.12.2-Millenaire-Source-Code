package org.millenaire.common.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.ICriterionTrigger.Listener;

public class PlayerListeners {
   private final PlayerAdvancements playerAdvancements;
   private final Set<Listener<AlwaysTrueCriterionInstance>> listeners = Sets.newHashSet();

   public PlayerListeners(PlayerAdvancements playerAdvancementsIn) {
      this.playerAdvancements = playerAdvancementsIn;
   }

   public void add(Listener<AlwaysTrueCriterionInstance> listener) {
      this.listeners.add(listener);
   }

   public void grantAndNotify() {
      List<Listener<AlwaysTrueCriterionInstance>> list = null;

      for (Listener<AlwaysTrueCriterionInstance> listener : this.listeners) {
         if (((AlwaysTrueCriterionInstance)listener.getCriterionInstance()).test()) {
            if (list == null) {
               list = Lists.newArrayList();
            }

            list.add(listener);
         }
      }

      if (list != null) {
         for (Listener<AlwaysTrueCriterionInstance> listenerx : list) {
            listenerx.grantCriterion(this.playerAdvancements);
         }
      }
   }

   public boolean isEmpty() {
      return this.listeners.isEmpty();
   }

   public void remove(Listener<AlwaysTrueCriterionInstance> listener) {
      this.listeners.remove(listener);
   }
}
