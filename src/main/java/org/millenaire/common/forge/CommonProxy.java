package org.millenaire.common.forge;

import net.minecraftforge.fml.loading.FMLPaths;
import java.io.File;

public class CommonProxy {

    public File getConfigFile() {
        return new File(getMillenaireContentDir(), "config-server.txt");
    }

    public File getCustomConfigFile() {
        return new File(getMillenaireCustomContentDir(), "config-server-custom.txt");
    }

    public File getLogFile() {
        return new File(getMillenaireCustomContentDir(), "millenaire-server.log");
    }

    private File getMillenaireContentDir() {
        // In 1.20, typically internal mods dir or config dir
        return new File(FMLPaths.GAMEDIR.get().toFile(), "mods/millenaire");
    }

    private File getMillenaireCustomContentDir() {
        return new File(FMLPaths.GAMEDIR.get().toFile(), "mods/millenaire-custom");
    }

    public String logPrefix() {
        return "SRV ";
    }

    public boolean isTrueServer() {
        return true;
    }

    // Stub for client-side only methods if called from shared code
    public void registerForgeClientClasses() {
    }

    public void registerKeyBindings() {
    }

    public void loadLanguagesIfNeeded() {
    }

    public void refreshClientResources() {
    }

    // Restored stubs referenced by Config/Utilities
    public String getKeyString(int value) {
        return "";
    }

    public int loadKeySetting(String value) {
        return 0;
    }

    public String getSinglePlayerName() {
        return "Player";
    }

    public net.minecraft.world.entity.player.Player getTheSinglePlayer() {
        return null;
    }
}
