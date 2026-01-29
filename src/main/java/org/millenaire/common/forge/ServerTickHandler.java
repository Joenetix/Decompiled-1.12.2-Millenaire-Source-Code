package org.millenaire.common.forge;

import java.util.ArrayList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import org.millenaire.common.world.MillWorldData;

public class ServerTickHandler {
   @SubscribeEvent
   public void tickStart(ServerTickEvent event) {
      if (event.phase == net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END && !Mill.startupError) {
         for (MillWorldData mw : new ArrayList<>(Mill.serverWorlds)) {
            mw.updateWorldServer();
         }
      }
   }
}
