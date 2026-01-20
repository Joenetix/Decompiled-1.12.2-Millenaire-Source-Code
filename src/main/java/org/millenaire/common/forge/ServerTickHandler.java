package org.millenaire.common.forge;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.millenaire.common.world.MillWorldData;
import org.millenaire.common.utilities.MillLog;

import java.util.ArrayList;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = org.millenaire.MillenaireRevived.MODID)
public class ServerTickHandler {

    @SubscribeEvent
    public static void tickStart(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (!Mill.startupError) {
                // Iterate over a copy to avoid concurrent modification if worlds are unloaded
                // during tick
                for (MillWorldData mw : new ArrayList<>(Mill.serverWorlds)) {
                    try {
                        mw.update();
                    } catch (Exception e) {
                        MillLog.error(null, "Exception in MillWorldData update: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
