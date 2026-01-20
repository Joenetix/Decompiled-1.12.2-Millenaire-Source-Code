package org.millenaire.events;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.millenaire.MillenaireRevived;
import org.millenaire.common.world.UserProfile;
import org.millenaire.common.world.MillWorldData;
import org.slf4j.Logger;

/**
 * Central event controller for Millénaire mod events.
 * Ported from original 1.12.2 MillEventController.java
 * 
 * Handles player login/logout, world load/save/unload events and world ticks.
 */
@Mod.EventBusSubscriber(modid = MillenaireRevived.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MillEventController {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Handle player login - initialize user profile.
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        try {
            if (!(event.getEntity() instanceof ServerPlayer player)) {
                return;
            }

            ServerLevel level = player.serverLevel();
            MillWorldData mw = MillWorldData.get(level);

            if (mw != null) {
                // Get or create user profile
                UserProfile profile = mw.getProfile(player.getUUID(), player.getName().getString());

                // Check if player name changed (for UUID tracking)
                if (profile != null && !player.getName().getString().equals(profile.playerName)) {
                    LOGGER.info("Name of player with UUID '{}' changed from '{}' to '{}'.",
                            profile.uuid, profile.playerName, player.getName().getString());
                    profile.playerName = player.getName().getString();
                    mw.setDirty();
                }

                LOGGER.debug("Player {} logged in, profile loaded", player.getName().getString());
            }
        } catch (Exception e) {
            LOGGER.error("Error in MillEventController.onPlayerLoggedIn:", e);
        }
    }

    /**
     * Handle player logout.
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        try {
            if (!(event.getEntity() instanceof ServerPlayer player)) {
                return;
            }
            LOGGER.debug("Player {} logged out", player.getName().getString());
        } catch (Exception e) {
            LOGGER.error("Error in MillEventController.onPlayerLoggedOut:", e);
        }
    }

    /**
     * Handle world load - initialize MillWorldData.
     */
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        try {
            // Initialize MillWorldData for this level
            MillWorldData mw = MillWorldData.get(level);
            if (mw != null) {
                LOGGER.info("Loaded Millénaire data for dimension: {}", level.dimension().location());
            }
        } catch (Exception e) {
            LOGGER.error("Error loading Millénaire world data:", e);
        }
    }

    /**
     * Handle world save - persist MillWorldData.
     */
    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        try {
            MillWorldData mw = MillWorldData.get(level);
            if (mw != null) {
                // Data is automatically saved via SavedData.setDirty()
                LOGGER.debug("Saved Millénaire data for dimension: {}", level.dimension().location());
            }
        } catch (Exception e) {
            LOGGER.error("Error saving Millénaire world data:", e);
        }
    }

    /**
     * Handle world unload.
     */
    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        try {
            LOGGER.debug("Unloading Millénaire data for dimension: {}", level.dimension().location());
            // Cleanup handled automatically by MC
        } catch (Exception e) {
            LOGGER.error("Error unloading Millénaire world data:", e);
        }
    }

    /**
     * Handle level tick - update MillWorldData.
     */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (event.level.isClientSide) {
            return;
        }

        // Only tick overworld or specific dimensions if needed?
        // For now, tick all server levels where we have data loaded.
        // MillWorldData.get() will load/create if needed, which might be heavy if
        // called on every dimension every tick?
        // But get() uses computeIfAbsent on the level's data storage, which is cached.

        try {
            MillWorldData mw = MillWorldData.get(event.level);
            if (mw != null) {
                mw.update();
            }

            // Also update VillageManager for new villages
            if (event.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                org.millenaire.core.VillageManager vm = org.millenaire.core.VillageManager.get(serverLevel);
                if (vm != null) {
                    vm.tickAll(serverLevel, event.level.getGameTime());
                }
            }
        } catch (Exception e) {
            // Rate limit errors to avoid spamming logs?
            // For now, log error occasionally or just logging error
            LOGGER.error("Error in MillEventController.onLevelTick:", e);
        }
    }
}
