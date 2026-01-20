package org.millenaire.common.culture;

import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class VillageConfigLoader {

    public static VillageType loadVillageType(ResourceLocation location, String id) {
        String path = "/assets/" + location.getNamespace() + "/" + location.getPath();

        System.out.println("[VillageConfigLoader] Attempting to load: " + path);

        try (InputStream stream = VillageConfigLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                System.err.println("[ERROR] Could not find village config: " + path);
                return null;
            }

            System.out.println("[VillageConfigLoader] SUCCESS: Found village config stream for " + id);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

                VillageType villageType = new VillageType(id, id, 10); // Default weight 10

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    // Skip comments and empty lines
                    if (line.isEmpty() || line.startsWith("//")) {
                        continue;
                    }

                    String[] parts = line.split("=", 2);
                    if (parts.length < 2) {
                        continue;
                    }

                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    switch (key) {
                        case "name":
                            break;
                        case "weight":
                            break;
                        case "biome":
                            villageType.addBiome(value);
                            break;
                        case "pathmaterial":
                            villageType.addPathMaterial(value);
                            break;
                        case "centre":
                            villageType.setCentreBuilding(value);
                            break;
                        case "start":
                            villageType.addStartingBuilding(value);
                            break;
                        case "core":
                            villageType.addCoreBuilding(value);
                            break;
                        case "secondary":
                            villageType.addSecondaryBuilding(value);
                            break;
                        case "never":
                            villageType.addForbiddenBuilding(value);
                            break;
                        case "outerwalltype":
                            villageType.setOuterWallType(value);
                            break;
                        case "innerwalltype":
                            villageType.setInnerWallType(value);
                            break;
                        case "innerwallradius":
                            try {
                                villageType.setInnerWallRadius(Integer.parseInt(value));
                            } catch (NumberFormatException e) {
                                System.err.println("[WARN] Invalid innerwallradius: " + value);
                            }
                            break;
                    }
                }
                return villageType;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
