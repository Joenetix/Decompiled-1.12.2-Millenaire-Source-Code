package org.millenaire.common.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.millenaire.common.world.MillWorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Mill {
    public static final String MODID = "millenaire";
    public static final String FIELD_PREFIX = MODID + ":";

    public static final Logger LOGGER = LogManager.getLogger("millenaire");
    public static CommonProxy proxy = new CommonProxy();
    public static List<File> loadingDirs = new ArrayList<>();
    public static org.millenaire.common.utilities.virtualdir.VirtualDir virtualLoadingDir = new org.millenaire.common.utilities.virtualdir.VirtualDir(
            new File("."));

    // Global State
    public static boolean startupError = false;
    public static boolean loadingComplete = false;
    public static boolean displayMillenaireLocationError = false;

    // World Data Tracking
    public static List<MillWorldData> serverWorlds = new ArrayList<>();
    public static MillWorldData clientWorld = null;

    // Entity Constants
    public static final ResourceLocation ENTITY_TARGETED_GHAST = new ResourceLocation(MODID, "millghast");
    public static final ResourceLocation ENTITY_TARGETED_BLAZE = new ResourceLocation(MODID, "millblaze");
    public static final ResourceLocation ENTITY_TARGETED_WITHERSKELETON = new ResourceLocation(MODID,
            "millwitherskeleton");

    // Crop Constants
    public static final ResourceLocation CROP_WHEAT = new ResourceLocation("wheat");
    public static final ResourceLocation CROP_CARROT = new ResourceLocation("carrots");
    public static final ResourceLocation CROP_POTATO = new ResourceLocation("potatoes");
    public static final ResourceLocation CROP_RICE = new ResourceLocation(MODID, "crop_rice");
    public static final ResourceLocation CROP_TURMERIC = new ResourceLocation(MODID, "crop_turmeric");
    public static final ResourceLocation CROP_MAIZE = new ResourceLocation(MODID, "crop_maize");
    public static final ResourceLocation CROP_VINE = new ResourceLocation(MODID, "crop_vine");
    public static final ResourceLocation CROP_CACAO = new ResourceLocation("cocoa");
    // public static final ResourceLocation CROP_FLOWER = new
    // ResourceLocation("flower"); // Unsure of 1.20 mapping
    public static final ResourceLocation CROP_COTTON = new ResourceLocation(MODID, "crop_cotton");

    public static MillWorldData getMillWorld(Level world) {
        if (world.isClientSide) {
            if (clientWorld != null && clientWorld.world == world) {
                return clientWorld;
            }
            return null;
        } else {
            for (MillWorldData mw : serverWorlds) {
                if (mw.world == world) {
                    return mw;
                }
            }
            // Fallback or create? Usually created on Load event.
            return null;
        }
    }

    public static boolean isDistantClient() {
        return clientWorld != null && serverWorlds.isEmpty();
    }
}
