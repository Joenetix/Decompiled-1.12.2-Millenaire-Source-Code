package org.millenaire.common.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.entity.MillVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

public class GoalLumbermanChopTrees extends Goal {

    public GoalLumbermanChopTrees() {
        this.key = "lumbermanChopTrees";
    }

    @Override
    public boolean isPossible(MillVillager MillVillager) {
        if (!MillVillager.level().isDay())
            return false;

        // Requires axe
        if (MillVillager.getBestAxe().isEmpty())
            return false;

        // Requires wood space in inventory (limit to 64 for now)
        if (MillVillager.countInv(InvItem.createInvItem(Blocks.OAK_LOG)) >= 64)
            return false;

        return true;
    }

    @Override
    public int priority(MillVillager MillVillager) {
        return 50; // Standard work priority
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        // Find nearest log within range
        BlockPos target = findNearestLog(MillVillager);

        if (target != null) {
            double distSq = MillVillager.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);

            if (distSq > 9.0) {
                MillVillager.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.0);
            } else {
                // Chop
                MillVillager.swing(InteractionHand.MAIN_HAND);

                // Effective tool logic could go here (speed)
                MillVillager.level().destroyBlock(target, false); // Don't drop items, we collect directly

                // Add to inventory directly (Simple Parity)
                MillVillager.addToInventory(Blocks.OAK_LOG.asItem(), 1);

                // Durability usage
                if (MillVillager.getRandom().nextFloat() < 0.01f) {
                    ItemStack tool = MillVillager.getBestAxe();
                    if (!tool.isEmpty()) {
                        MillVillager.takeFromInventory(tool.getItem(), 1);
                    }
                }
            }
        } else {
            // No trees found
            return false;
        }

        return true;
    }

    private BlockPos findNearestLog(MillVillager MillVillager) {
        BlockPos pos = MillVillager.blockPosition();
        BlockPos nearest = null;
        double minDist = Double.MAX_VALUE;

        int range = 24; // Expanded range

        for (int x = -range; x <= range; x++) {
            for (int y = -2; y <= 8; y++) { // Check higher for trees
                for (int z = -range; z <= range; z++) {
                    BlockPos p = pos.offset(x, y, z);
                    BlockState state = MillVillager.level().getBlockState(p);
                    /**
                     * Simply check for OAK_LOG for now to be safe.
                     * In full version, use TagKey<Block> LOGS.
                     */
                    if (state.is(Blocks.OAK_LOG) || state.is(Blocks.BIRCH_LOG) || state.is(Blocks.SPRUCE_LOG)
                            || state.is(Blocks.JUNGLE_LOG) || state.is(Blocks.ACACIA_LOG)
                            || state.is(Blocks.DARK_OAK_LOG)) {
                        // Logic to prioritize finding the BOTTOM log of a tree could be added here
                        // For now, any log is fine
                        double d = p.distSqr(pos);
                        if (d < minDist) {
                            minDist = d;
                            nearest = p;
                        }
                    }
                }
            }
        }
        return nearest;
    }
}

