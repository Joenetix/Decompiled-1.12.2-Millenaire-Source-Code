package org.millenaire.common.goal;

import org.millenaire.common.entity.MillVillager;

/** Goal for building village paths. */
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.core.Village;

/** Goal for building village paths. */
public class GoalBuildPath extends Goal {

    private BlockPos targetPos;

    @Override
    public boolean isPossible(MillVillager MillVillager) {
        if (MillVillager.getVillage() == null)
            return false;

        targetPos = MillVillager.getVillage().getNextPathToBuild(MillVillager.blockPosition());
        return targetPos != null;
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        if (targetPos == null)
            return true; // Should not happen if isPossible checked

        if (MillVillager.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ()) > 4) {
            MillVillager.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
            return false;
        } else {
            // Place path block
            placePathBlock(MillVillager, targetPos);
            MillVillager.getVillage().markPathBuilt(targetPos);
            return true;
        }
    }

    private void placePathBlock(MillVillager MillVillager, BlockPos pos) {
        Village v = MillVillager.getVillage();
        Block block = Blocks.GRAVEL; // Default

        if (v.getVillageType() != null && !v.getVillageType().getPathMaterials().isEmpty()) {
            String key = v.getVillageType().getPathMaterials().get(0); // Use first preferred
            Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(key));
            if (b != Blocks.AIR) {
                block = b;
            }
        }

        // Don't replace existing hard blocks, only replace air/grass/liquid
        BlockState existing = MillVillager.level().getBlockState(pos);
        if (existing.canBeReplaced() || existing.is(Blocks.GRASS_BLOCK) || existing.is(Blocks.DIRT)) {
            MillVillager.level().setBlock(pos, block.defaultBlockState(), 3);
        }
    }

    @Override
    public int priority(MillVillager MillVillager) {
        return 10; // Lower priority than crucial survival/job tasks
    }
}

