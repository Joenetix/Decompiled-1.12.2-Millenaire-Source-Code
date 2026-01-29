package org.millenaire.common.culture;

import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Central registry for all Millenaire cultures.
 * Cultures define building styles, jobs, resources, and AI behaviors.
 * 
 * In the ported version, cultures are loaded from data files at runtime
 * using CultureLoader rather than hardcoded classes.
 */
public class CultureRegistry {

    public static void register(IEventBus modEventBus) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(CultureRegistry::onServerStarting);
    }

    private static void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
        // Load cultures from files (merging into existing static cultures)
        CultureLoader.loadCultures(event.getServer().getResourceManager());

        // Initialize village types (currently hardcoded in MillenaireCultures)
        MillenaireCultures.initVillages();
    }
}
