package org.millenaire.common.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLPaths;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.item.InvItem;
import org.millenaire.core.MillItems;

public class MillCommonUtilities {
    public static Random random = new Random();
    private static File baseDir = null;
    private static File customDir = null;

    /**
     * Interface for weighted random selection.
     */
    public interface WeightedChoice {
        int getChoiceWeight(Player player);
    }

    /**
     * Get a random choice from a list of weighted choices.
     */
    public static <T extends WeightedChoice> T getWeightedChoice(List<T> choices, Player player) {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        int totalWeight = 0;
        for (T choice : choices) {
            totalWeight += choice.getChoiceWeight(player);
        }
        if (totalWeight <= 0) {
            return choices.get(getRandom().nextInt(choices.size()));
        }
        int r = getRandom().nextInt(totalWeight);
        int cumulative = 0;
        for (T choice : choices) {
            cumulative += choice.getChoiceWeight(player);
            if (r < cumulative) {
                return choice;
            }
        }
        return choices.get(choices.size() - 1);
    }

    public static boolean chanceOn(int i) {
        return getRandom().nextInt(i) == 0;
    }

    public static Random getRandom() {
        if (random == null) {
            random = new Random();
        }
        return random;
    }

    /**
     * Get a random integer from 0 (inclusive) to bound (exclusive).
     */
    public static int randomInt(int bound) {
        return getRandom().nextInt(bound);
    }

    public static File getModsDir() {
        return FMLPaths.MODSDIR.get().toFile();
    }

    public static File getMillenaireContentDir() {
        if (baseDir == null) {
            baseDir = new File(getModsDir(), "millenaire");
        }
        return baseDir;
    }

    public static File getMillenaireCustomContentDir() {
        if (customDir == null) {
            customDir = new File(getModsDir(), "millenaire-custom");
        }
        return customDir;
    }

    public static File getWorldSaveDir(net.minecraft.world.level.Level world) {
        if (world instanceof net.minecraft.server.level.ServerLevel) {
            return ((net.minecraft.server.level.ServerLevel) world).getServer()
                    .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile();
        }
        return null;
    }

    public static File getBuildingsDir(Level world) {
        if (world == null)
            return getMillenaireCustomContentDir();
        // Return world-specific directory or generic
        File saveDir = ((net.minecraft.server.level.ServerLevel) world).getServer()
                .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile();
        File millDir = new File(saveDir, "millenaire/buildings");
        if (!millDir.exists())
            millDir.mkdirs();
        return millDir;
    }

    /**
     * Get the export directory for building plans.
     */
    public static File getExportDir() {
        File exportDir = new File(getMillenaireCustomContentDir(), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        return exportDir;
    }

    /**
     * Get the help directory for generated documentation.
     */
    public static File getMillenaireHelpDir() {
        File helpDir = new File(getMillenaireCustomContentDir(), "help");
        if (!helpDir.exists()) {
            helpDir.mkdirs();
        }
        return helpDir;
    }

    public static BufferedWriter getWriter(File file) throws UnsupportedEncodingException, FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
    }

    /**
     * Get a BufferedWriter for appending to a file.
     */
    public static BufferedWriter getAppendWriter(File file) throws UnsupportedEncodingException, FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF8"));
    }

    /**
     * Read all lines from a file.
     */
    public static List<String> getFileLines(File file) {
        List<String> lines = new ArrayList<>();
        if (file == null || !file.exists()) {
            return lines;
        }
        try (BufferedReader reader = getReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            MillLog.printException("Error reading file: " + file.getAbsolutePath(), e);
        }
        return lines;
    }

    /**
     * Get a BufferedReader for a file.
     */
    public static BufferedReader getReader(File file) throws UnsupportedEncodingException, FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
    }

    public static class BonusThread extends Thread {
        String login;

        public BonusThread(String login) {
            this.login = login;
        }

        @Override
        public void run() {
            try {
                InputStream stream = new URL("http://millenaire.org/php/bonuscheck.php?login=" + this.login)
                        .openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String result = reader.readLine();
                if (result != null && result.trim().equals("thik hai")) {
                    MillConfigValues.bonusEnabled = true;
                    MillConfigValues.bonusCode = MillConfigValues.calculateLoginMD5(this.login);
                    MillConfigValues.writeConfigFile();
                }
            } catch (Exception var4) {
            }
        }
    }

    // Stubbing missing methods until dependencies (Culture, TradeGood, InvItem
    // complete) are ready
    // But keeping file structure exact.

    public static void generateHearts(Entity ent) {
        for (int var3 = 0; var3 < 7; var3++) {
            double var4 = random.nextGaussian() * 0.02;
            double var6 = random.nextGaussian() * 0.02;
            double var8 = random.nextGaussian() * 0.02;
            ent.level().addParticle(
                    ParticleTypes.HEART,
                    ent.getX() + random.nextFloat() * ent.getBbWidth() * 2.0F - ent.getBbWidth(),
                    ent.getY() + 0.5 + random.nextFloat() * ent.getBbHeight(),
                    ent.getZ() + random.nextFloat() * ent.getBbWidth() * 2.0F - ent.getBbWidth(),
                    var4,
                    var6,
                    var8);
        }
    }

    public static class LogThread extends Thread {
        String url;

        public LogThread(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                InputStream stream = new URL(this.url).openStream();
                stream.close();
            } catch (Exception var2) {
                if (MillConfigValues.DEV) {
                    MillLog.error(null, "Exception when calling statistic service:" + var2.getMessage());
                }
            }
        }
    }

    public static String getBlockName(net.minecraft.world.level.block.Block block) {
        return net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block).toString();
    }

    public static class ExtFileFilter implements java.io.FileFilter, java.io.FilenameFilter {
        String ext;

        public ExtFileFilter(String ext) {
            this.ext = ext;
        }

        public boolean accept(java.io.File file) {
            return file.getName().toLowerCase().endsWith("." + this.ext);
        }

        public boolean accept(java.io.File dir, String name) {
            return name.toLowerCase().endsWith("." + this.ext);
        }
    }

    public static class PrefixExtFileFilter implements java.io.FileFilter {
        String prefix;
        String ext;

        public PrefixExtFileFilter(String prefix, String ext) {
            this.prefix = prefix;
            this.ext = ext;
        }

        public boolean accept(java.io.File file) {
            return file.getName().toLowerCase().endsWith(this.ext) && file.getName().startsWith(this.prefix);
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

    // --- Money Handling Methods ---

    /**
     * Count the total money value in the player's inventory.
     * Uses Millenaire's denier currency system.
     */
    public static int countMoney(net.minecraft.world.Container inventory) {
        int total = 0;
        // TODO: Implement proper money counting with denier items when ported
        // For now, count gold/iron ingots as currency
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            net.minecraft.world.item.ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == org.millenaire.core.MillItems.DENIER.get()) {
                    total += stack.getCount();
                } else if (stack.getItem() == org.millenaire.core.MillItems.DENIER_ARGENT.get()) {
                    total += stack.getCount() * 64;
                } else if (stack.getItem() == org.millenaire.core.MillItems.DENIER_OR.get()) {
                    total += stack.getCount() * 64 * 64;
                }
            }
        }
        return total;
    }

    /**
     * Change the money in a player's inventory by the given amount.
     * Positive values add money, negative values remove.
     */
    public static boolean changeMoney(net.minecraft.world.Container inventory, int amount,
            net.minecraft.world.entity.player.Player player) {
        if (amount == 0)
            return true;

        if (amount < 0) {
            // Remove money
            int toRemove = -amount;
            int available = countMoney(inventory);
            if (available < toRemove) {
                return false; // Not enough money
            }
            // TODO: Implement proper removal logic
            return true;
        } else {
            // Add money
            // TODO: Implement proper addition logic with proper denier stacking
            return true;
        }
    }

    /**
     * Count items in an inventory container.
     */
    public static int countChestItems(net.minecraft.world.entity.player.Inventory inventory,
            net.minecraft.world.item.Item item, int meta) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            net.minecraft.world.item.ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Pack a long into two ints for network transmission.
     */
    public static int[] packLong(long value) {
        return new int[] { (int) (value >> 32), (int) value };
    }

    /**
     * Unpack two ints into a long.
     */
    public static long unpackLong(int high, int low) {
        return ((long) high << 32) | (low & 0xFFFFFFFFL);
    }

    /**
     * Attempts to put items into a container.
     * Returns the actual amount added.
     */
    public static int putItemsInChest(Container inventory, Item item, int meta, int amount) {
        int leftToAdd = amount;
        int maxStackSize = item.getMaxStackSize();

        // 1. Try to stack with existing
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (leftToAdd <= 0)
                break;
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item && stack.getCount() < maxStackSize) {
                int room = maxStackSize - stack.getCount();
                int toAdd = Math.min(room, leftToAdd);
                stack.grow(toAdd);
                leftToAdd -= toAdd;
            }
        }

        // 2. Add to empty slots
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (leftToAdd <= 0)
                break;
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                int toAdd = Math.min(leftToAdd, maxStackSize);
                inventory.setItem(i, new ItemStack(item, toAdd));
                leftToAdd -= toAdd;
            }
        }

        return amount - leftToAdd;
    }

    /**
     * Removes items from a container.
     * Returns the actual amount removed.
     */
    public static int getItemsFromChest(Container inventory, Item item, int meta, int amount) {
        int leftToRemove = amount;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (leftToRemove <= 0)
                break;
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                int toTake = Math.min(leftToRemove, stack.getCount());
                inventory.removeItem(i, toTake);
                leftToRemove -= toTake;
            }
        }

        return amount - leftToRemove;
    }
}
