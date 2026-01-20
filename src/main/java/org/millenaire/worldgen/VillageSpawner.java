package org.millenaire.worldgen;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.millenaire.core.Village;
import org.millenaire.core.VillageRegistry;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Handles spawning of autonomous villages when Jigsaw structures generate.
 * 
 * When a Millenaire structure is placed by worldgen:
 * 1. Detect the structure placement
 * 2. Create a Village at that location
 * 3. Spawn initial citizens
 * 4. Enable autonomous mode
 * 
 * The village then develops on its own!
 */
public class VillageSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(IEventBus forgeEventBus) {
        forgeEventBus.register(VillageSpawner.class);
        LOGGER.info("Village spawner registered - autonomous villages enabled");
    }

    /**
     * Tick all villages in loaded levels
     */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (!(event.level instanceof ServerLevel serverLevel))
            return;

        // Tick all villages
        org.millenaire.core.VillageManager manager = org.millenaire.core.VillageManager.get(serverLevel);
        manager.tickAll(serverLevel, serverLevel.getGameTime());

        // Update MillWorldData (buildings, villagers)
        org.millenaire.common.world.MillWorldData mw = org.millenaire.common.world.MillWorldData.get(serverLevel);
        if (mw != null) {
            mw.update();
        }
    }

    /**
     * TODO: Detect when Millénaire Jigsaw structures generate
     * and create villages at those locations
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel))
            return;

        // This is where we'd detect structure generation
        // For now, this is a placeholder
        LOGGER.debug("Level loaded: {}", serverLevel.dimension().location());
    }

    /**
     * Create a new autonomous village at the given location
     */
    public static Village createVillage(ServerLevel level, BlockPos center, String culture) {
        UUID villageId = UUID.randomUUID();
        Village village = new Village(villageId, center, culture, level);
        village.setAutonomous(true);

        // Register with world
        VillageRegistry registry = VillageRegistry.get(level);
        registry.registerVillage(village);

        LOGGER.info("Created autonomous {} village at {}", culture, center);

        // TODO: Spawn initial citizens
        // TODO: Create initial buildings

        return village;
    }
}
