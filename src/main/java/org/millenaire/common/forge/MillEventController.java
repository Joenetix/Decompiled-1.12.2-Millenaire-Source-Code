package org.millenaire.common.forge;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.millenaire.core.MillItems;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.world.MillWorldData;
import org.millenaire.utilities.VillageUtilities;
import org.millenaire.common.world.UserProfile;
import org.millenaire.MillenaireRevived;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = MillenaireRevived.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MillEventController {

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            // Load MillWorldData for this level
            // In 1.12.2: Mill.serverWorlds.add(...)
            // In 1.20.1 we can track standard Level

            // Avoid adding duplicates
            boolean exists = false;
            for (MillWorldData mw : Mill.serverWorlds) {
                if (mw.world == serverLevel) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                MillWorldData newWorld = new MillWorldData(serverLevel);
                Mill.serverWorlds.add(newWorld);
                // newWorld.loadData(); // Assuming this method exists or we need to port it
                MillLog.info("MillWorldData loaded for level: " + serverLevel.dimension());
            }
        } else if (event.getLevel().isClientSide()) {
            // Client world logic
            // Mill.clientWorld = new MillWorldData((Level)event.getLevel());
        }
    }

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            for (MillWorldData mw : Mill.serverWorlds) {
                if (mw.world == serverLevel) {
                    mw.saveEverything();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            List<MillWorldData> toRemove = new ArrayList<>();
            for (MillWorldData mw : Mill.serverWorlds) {
                if (mw.world == serverLevel) {
                    toRemove.add(mw);
                }
            }
            Mill.serverWorlds.removeAll(toRemove);
        } else if (event.getLevel().isClientSide()) {
            // Mill.clientWorld = null;
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // Handle profile connection
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            MillLog.info("Millenaire: Player logged in: " + player.getName().getString());
            // UserProfile logic stub - fully porting UserProfile is separate task but good
            // to have hook
            // UserProfile profile = VillageUtilities.getServerProfile(player.level(),
            // player);
            // if (profile != null) profile.connectUser();
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        // Inuit drops logic
        LivingEntity entity = event.getEntity();
        // Check for specific mobs and add Millenaire items
        if (entity instanceof Guardian || entity instanceof Squid) {
            // MillItems.SEAFOOD_RAW check
        }
        if (entity instanceof Wolf) {
            // MillItems.WOLFMEAT_RAW check
        }
    }
}
