package org.millenaire.common.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.entity.MillVillager;
import net.minecraft.world.item.ItemStack;

/**
 * Goal for miner villagers to mine resources underground.
 * Digs tunnels and extracts ores.
 */
public class GoalMinerMineResource extends Goal {

    public GoalMinerMineResource() {
        this.key = "minerMineResource";
    }

    @Override
    public boolean isPossible(MillVillager MillVillager) {
        // Check if there's a village context
        if (MillVillager.getVillage() == null)
            return false;

        // Needs pickaxe
        if (MillVillager.getBestPickaxe().isEmpty())
            return false;

        // Check inventory space for cobble/stone
        if (MillVillager.countInv(InvItem.createInvItem(Blocks.COBBLESTONE)) >= 64)
            return false;

        return true;
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        BlockPos target = findNearestMineable(MillVillager);

        if (target != null) {
            double distSq = MillVillager.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);

            if (distSq > 9.0) {
                MillVillager.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.0);
            } else {
                MillVillager.swing(InteractionHand.MAIN_HAND);
                // Mine the block
                // For stone, we get cobblestone
                // Simplify: just destroy and add drops
                // But since we use auto-collect:
                BlockState state = MillVillager.level().getBlockState(target);
                MillVillager.level().destroyBlock(target, false);

                if (state.is(Blocks.STONE)) {
                    MillVillager.addToInventory(Blocks.COBBLESTONE.asItem(), 1);
                } else {
                    MillVillager.addToInventory(state.getBlock().asItem(), 1);
                }

                // durability usage
                if (MillVillager.getRandom().nextFloat() < 0.01f) {
                    ItemStack tool = MillVillager.getBestPickaxe();
                    if (!tool.isEmpty()) {
                        MillVillager.takeFromInventory(tool.getItem(), 1);
                        // MillVillager.playSound(SoundEvents.ITEM_BREAK, 1.0f, 1.0f); // effective effect
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public int priority(MillVillager MillVillager) {
        return 45;
    }

    @Override
    public int range(MillVillager MillVillager) {
        return 4; // Close range for mining interactions
    }

    private BlockPos findNearestMineable(MillVillager MillVillager) {
        BlockPos pos = MillVillager.blockPosition();
        BlockPos nearest = null;
        double minDist = Double.MAX_VALUE;

        int range = 10;

        // Scan area
        for (int x = -range; x <= range; x++) {
            for (int y = -2; y <= 3; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos p = pos.offset(x, y, z);
                    BlockState state = MillVillager.level().getBlockState(p);

                    // Basic mining targets: Stone, Ores, Sand, Gravel
                    boolean isTarget = state.is(Blocks.STONE) ||
                            state.is(Blocks.COBBLESTONE) || // Sometimes mine cobble placed by others?
                            state.is(Blocks.ANDESITE) ||
                            state.is(Blocks.DIORITE) ||
                            state.is(Blocks.GRANITE) ||
                            state.is(Tags.Blocks.ORES); // Uses Forge tag

                    // Constraint: prioritize blocks at eye level or below to dig "tunnels" not
                    // "pits" if possible
                    // And check if exposed to air? Logic simplification: Any valid block.

                    if (isTarget) {
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

