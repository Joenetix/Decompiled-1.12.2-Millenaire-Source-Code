package org.millenaire.common.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.levelgen.material.MaterialRuleList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;
import org.millenaire.common.forge.Mill;
// import org.millenaire.common.network.ServerSender;
import org.millenaire.common.village.Building;

// import org.millenaire.common.world.MillWorldData;
// import org.millenaire.common.world.UserProfile;

public class BlockItemUtilities {
    // private static final Map<Material, String> MATERIAL_NAME_MAP =
    // createMaterialNameMap(); // Materials removed in 1.20
    private static final Set<String> FORBIDDEN_MATERIALS = new HashSet<>(20);
    private static final Set<String> FORBIDDEN_BLOCKS = new HashSet<>(20);
    private static final Set<String> FORBIDDEN_EXCEPTIONS = new HashSet<>(20);
    private static final Set<String> GROUND_MATERIALS = new HashSet<>(20);
    private static final Set<String> GROUND_BLOCKS = new HashSet<>(20);
    private static final Set<String> GROUND_EXCEPTIONS = new HashSet<>(20);
    private static final Set<String> DANGER_MATERIALS = new HashSet<>(20);
    private static final Set<String> DANGER_BLOCKS = new HashSet<>(20);
    private static final Set<String> DANGER_EXCEPTIONS = new HashSet<>(20);
    private static final Set<String> WATER_MATERIALS = new HashSet<>(20);
    private static final Set<String> WATER_BLOCKS = new HashSet<>(20);
    private static final Set<String> WATER_EXCEPTIONS = new HashSet<>(20);
    private static final Set<String> PATH_REPLACEABLE_MATERIALS = new HashSet<>(20);
    private static final Set<String> PATH_REPLACEABLE_BLOCKS = new HashSet<>(20);
    private static final Set<String> PATH_REPLACEABLE_EXCEPTIONS = new HashSet<>(20);

    public static void checkForHarvestTheft(Player player, BlockPos pos) {
        // Stubbed for now to avoid extensive dependencies on ServerSender/WorldData
    }

    public static String getBlockCanonicalName(Block block) {
        return block != null ? ForgeRegistries.BLOCKS.getKey(block).toString() : null;
    }

    public static String getBlockMaterialName(Block block) {
        // Materials are gone in 1.20.1 (replaced by map colors and tags).
        // We'll return "unknown" or map basic types if needed, but for now simplified.
        return "unknown";
    }

    public static ItemStack getItemStackFromBlockState(BlockState state, int quantity) {
        return new ItemStack(state.getBlock(), quantity);
    }

    public static void initBlockTypes() {
        // Configuration reading logic preserved
        File mainBlockTypesFile = new File(MillCommonUtilities.getMillenaireContentDir(), "blocktypes.txt");
        if (mainBlockTypesFile.exists()) {
            readBlockTypesFile(mainBlockTypesFile);
        }

        File customBlockTypesFile = new File(MillCommonUtilities.getMillenaireCustomContentDir(), "blocktypes.txt");
        if (customBlockTypesFile.exists()) {
            readBlockTypesFile(customBlockTypesFile);
        }
    }

    public static boolean isBlockDangerous(Block b) {
        if (b == null || b == Blocks.AIR || DANGER_EXCEPTIONS.contains(getBlockCanonicalName(b))) {
            return false;
        }
        return DANGER_BLOCKS.contains(getBlockCanonicalName(b));
    }

    public static boolean isBlockForbidden(Block b) {
        if (b == null || b == Blocks.AIR || FORBIDDEN_EXCEPTIONS.contains(getBlockCanonicalName(b))) {
            return false;
        }
        // TileEntity check in 1.20? (most blocks have TEs now or different logic)
        if (b.defaultBlockState().hasBlockEntity()) {
            return true;
        }
        return FORBIDDEN_BLOCKS.contains(getBlockCanonicalName(b));
    }

    public static boolean isBlockGround(Block b) {
        if (b == null || b == Blocks.AIR || GROUND_EXCEPTIONS.contains(getBlockCanonicalName(b))) {
            return false;
        }
        return GROUND_BLOCKS.contains(getBlockCanonicalName(b));
    }

    public static boolean isBlockLiquid(Block b) {
        return b instanceof LiquidBlock;
    }

    public static boolean isBlockOpaqueCube(Block block) {
        return block.defaultBlockState().canOcclude();
    }

    public static boolean isBlockPathReplaceable(Block b) {
        if (b == null || b == Blocks.AIR || PATH_REPLACEABLE_EXCEPTIONS.contains(getBlockCanonicalName(b))) {
            return false;
        }
        return PATH_REPLACEABLE_BLOCKS.contains(getBlockCanonicalName(b));
    }

    public static boolean isBlockSolid(Block b) {
        if (b == null)
            return false;
        return b.defaultBlockState().canOcclude() || b instanceof SlabBlock || b instanceof StairBlock || isFence(b);
    }

    public static boolean isBlockWalkable(Block b) {
        // Similar to solid but includes special blocks
        return isBlockSolid(b);
    }

    public static boolean isBlockWater(Block b) {
        if (b == null || b == Blocks.AIR || WATER_EXCEPTIONS.contains(getBlockCanonicalName(b))) {
            return false;
        }
        return /* b == Blocks.WATER || */ WATER_BLOCKS.contains(getBlockCanonicalName(b));
    }

    public static boolean isFence(Block block) {
        return block instanceof FenceBlock;
    }

    public static boolean isFenceGate(Block block) {
        return block instanceof FenceGateBlock;
    }

    public static boolean isWoodenDoor(Block block) {
        return block instanceof DoorBlock; // Check material if needed, but instanceof is safer
    }

    private static boolean readBlockTypesFile(File file) {
        // Simplified reading logic from 1.12.2
        if (!file.exists())
            return false;
        try {
            BufferedReader reader = MillCommonUtilities.getReader(file);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.trim().length() > 0 && !line.startsWith("//")) {
                    String[] temp = line.split("=");
                    if (temp.length == 2) {
                        String key = temp[0].trim().toLowerCase();
                        String value = temp[1];
                        if (key.equals("forbidden_blocks")) {
                            FORBIDDEN_BLOCKS.clear();
                            FORBIDDEN_BLOCKS.addAll(Arrays.asList(value.split(",")));
                        }
                        // ... other keys ...
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MillLog.printException(e);
            return false;
        }
    }
}
