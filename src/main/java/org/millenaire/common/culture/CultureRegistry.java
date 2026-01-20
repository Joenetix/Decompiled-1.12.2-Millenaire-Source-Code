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
        // Cultures are now loaded from data files via CultureLoader
        // No hardcoded culture classes needed

        // TODO: Hook CultureLoader initialization here once porting is complete
        // CultureLoader.loadAllCultures();
    }
}
