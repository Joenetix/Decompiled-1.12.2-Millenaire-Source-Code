package org.millenaire.common.deployer;

import com.mojang.logging.LogUtils;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ContentDeployer {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEV_VERSION_NUMBER = "@VERSION@";

    private static void copyFolder(String modJarPath, String deployLocation, String folder, File destDir)
            throws IOException {
        if (!destDir.exists() && !destDir.mkdir()) {
            LOGGER.warn("Failed to create dest dir: " + destDir.getAbsolutePath());
        }

        try (JarFile file = new JarFile(modJarPath)) {
            Enumeration<JarEntry> e = file.entries();

            while (e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                String jarEntryName = entry.getName();

                // Check if entry is within the target folder in the JAR
                if (jarEntryName.startsWith(deployLocation + folder)) {
                    // Calculate destination file path
                    // e.g. jarEntryName = "todeploy/millenaire/config.txt"
                    // deployLocation = "todeploy/"
                    // relativePath = "millenaire/config.txt"
                    String relativePath = jarEntryName.substring(deployLocation.length());
                    File destination = new File(destDir.getParentFile(), relativePath); // destDir is modsDir? No,
                                                                                        // destDir passed is modsDir in
                                                                                        // calling code

                    // The original code passed 'destDir' as the root to deploy TO.
                    // And calculated destination relative to it.
                    // Original: File destination = new File(destDir,
                    // jarEntryName.substring(deployLocation.length(), jarEntryName.length()));

                    if (entry.isDirectory()) {
                        if (!destination.exists() && !destination.mkdirs()) {
                            LOGGER.warn("Failed to create dest dirs: " + destination.getAbsolutePath());
                        }
                    } else {
                        // Ensure parent dir exists
                        if (destination.getParentFile() != null && !destination.getParentFile().exists()) {
                            destination.getParentFile().mkdirs();
                        }

                        try (InputStream stream = file.getInputStream(entry);
                                OutputStream out = new FileOutputStream(destination)) {
                            stream.transferTo(out);
                        }
                    }
                }
            }
        }
    }

    public static void deployContent(File ourJar) {
        // Check if running from JAR or Dev environment
        if (ContentDeployer.class.getResource("ContentDeployer.class") == null ||
                !ContentDeployer.class.getResource("ContentDeployer.class").toString().startsWith("jar")) {
            LOGGER.info("No need to redeploy Millénaire content (Dev environment detected).");
        } else {
            File modsDir = MillCommonUtilities.getModsDir();

            try {
                boolean redeployMillenaire = false;
                File millenaireDir = new File(modsDir, "millenaire");

                // Version check logic
                // In dev, @VERSION@ is not replaced, so it remains literal
                if ("8.1.2".equals(DEV_VERSION_NUMBER) || DEV_VERSION_NUMBER.contains("@")) {
                    // Assume dev or unversioned - might want to deploy if missing
                    // But checking "8.1.2" seems specific to the original mod's hardcoded logic.
                    // Original logic: if "@VERSION@" or "8.1.2", force redeploy?

                    // Let's stick to the logic: if millenaire folder doesn't exist, deploy.
                    if (!millenaireDir.exists()) {
                        redeployMillenaire = true;
                        LOGGER.warn("Deploying millenaire/ folder as it can't be found.");
                    } else {
                        // Check version file validity
                        File versionFile = new File(millenaireDir, "version.txt");
                        if (!versionFile.exists()) {
                            redeployMillenaire = true;
                            MillCommonUtilities.deleteDir(millenaireDir);
                            LOGGER.warn("Redeploying millenaire/ as it has no version file.");
                        } else {
                            try (BufferedReader reader = MillCommonUtilities.getReader(versionFile)) {
                                String versionString = reader.readLine();
                                // TODO: Use actual current version instead of hardcoded 8.1.2
                                if (versionString == null || !versionString.equals("8.1.2")) {
                                    redeployMillenaire = true;
                                    MillCommonUtilities.deleteDir(millenaireDir);
                                    LOGGER.warn("Redeploying millenaire/ as version mismatch: found " + versionString);
                                } else {
                                    LOGGER.info("Millénaire content is up to date (" + versionString + ").");
                                }
                            }
                        }
                    }

                }

                if (redeployMillenaire && ourJar != null && ourJar.exists()) {
                    try {
                        long startTime = System.currentTimeMillis();
                        // Extract "todeploy/millenaire/" content to "mods/millenaire/"
                        // The original passed modsDir as destDir.
                        // And looked for entries starting with "todeploy/millenaire/"
                        // And wrote them to modsDir + entry.substring("todeploy/".length()) ->
                        // "millenaire/..."

                        copyFolder(ourJar.getAbsolutePath(), "todeploy/", "millenaire/", modsDir);

                        // Write version file
                        Files.write(Paths.get(modsDir.getAbsolutePath(), "millenaire", "version.txt"),
                                "8.1.2".getBytes());

                        LOGGER.info(
                                "Deployed millenaire folder in " + (System.currentTimeMillis() - startTime) + " ms.");
                    } catch (IOException e) {
                        LOGGER.error("Error deploying millenaire folder", e);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error unzipping millenaire content", e);
            }

            try {
                File millenaireCustomDir = new File(modsDir, "millenaire-custom");
                if (!millenaireCustomDir.exists()) {
                    LOGGER.info("Deploying millenaire-custom/ folder...");

                    try {
                        long startTime = System.currentTimeMillis();
                        if (ourJar != null && ourJar.exists()) {
                            copyFolder(ourJar.getAbsolutePath(), "todeploy/", "millenaire-custom/", modsDir);
                            LOGGER.info("Deployed millenaire-custom folder in "
                                    + (System.currentTimeMillis() - startTime) + " ms.");
                        } else {
                            LOGGER.warn("Could not deploy millenaire-custom: Mod JAR file not found at "
                                    + (ourJar != null ? ourJar.getAbsolutePath() : "null"));
                        }
                    } catch (IOException e) {
                        LOGGER.error("Error deploying millenaire-custom folder", e);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error unzipping millenaire-custom content", e);
            }
        }
    }
}
