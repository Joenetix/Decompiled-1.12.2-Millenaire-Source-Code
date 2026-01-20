package org.millenaire.common.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.millenaire.common.block.BlockMillCrops;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.entity.MillVillager;

public class GoalFarmerHarvestCrops extends Goal {

    public GoalFarmerHarvestCrops() {
        this.key = "farmerHarvestCrops";
    }

    @Override
    public boolean isPossible(MillVillager MillVillager) {
        if (!MillVillager.level().isDay())
            return false;

        // Check if inventory full of typical crops (wheat/rice/maize/turmeric)
        // Simplified check: if full of wheat, stop harvesting
        if (MillVillager.countInv(InvItem.createInvItem(net.minecraft.world.item.Items.WHEAT)) >= 64)
            return false;

        return true;
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        BlockPos target = findMatureCrop(MillVillager);

        if (target != null) {
            double distSq = MillVillager.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);

            if (distSq > 9.0) {
                MillVillager.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.0);
            } else {
                MillVillager.swing(InteractionHand.MAIN_HAND);
                // Harvest logic
                BlockState state = MillVillager.level().getBlockState(target);

                // Add drops to inventory
                // For simplified parity, we assume 1 crop + 1 seed
                // Actual drop logic is in LootTables, but we are bypassing drops for direct
                // inventory mgmt
                if (state.getBlock() instanceof BlockMillCrops) {
                    // Get drops manually or via loot table?
                    // Loot table is hard to invoke here without a player context sometimes
                    // Check BlockMillCrops for seed/fruit knowledge?
                    // It has a seed supplier, but not fruit.
                    // Assume generic drop for now: 1 Item
                    MillVillager.addToInventory(state.getBlock().asItem(), 1);
                } else if (state.is(Blocks.WHEAT)) {
                    MillVillager.addToInventory(net.minecraft.world.item.Items.WHEAT, 1);
                    MillVillager.addToInventory(net.minecraft.world.item.Items.WHEAT_SEEDS, 2); // Assume bonus seeds
                } else if (state.is(Blocks.CARROTS)) {
                    MillVillager.addToInventory(net.minecraft.world.item.Items.CARROT, 2);
                } else if (state.is(Blocks.POTATOES)) {
                    MillVillager.addToInventory(net.minecraft.world.item.Items.POTATO, 2);
                } else if (state.is(Blocks.BEETROOTS)) {
                    MillVillager.addToInventory(net.minecraft.world.item.Items.BEETROOT, 1);
                    MillVillager.addToInventory(net.minecraft.world.item.Items.BEETROOT_SEEDS, 1);
                }

                // Destroy block (leaving air for replanting)
                MillVillager.level().destroyBlock(target, false);
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public int priority(MillVillager MillVillager) {
        return 50;
    }

    private BlockPos findMatureCrop(MillVillager MillVillager) {
        BlockPos pos = MillVillager.blockPosition();
        BlockPos nearest = null;
        double minDist = Double.MAX_VALUE;
        int range = 24;

        for (int x = -range; x <= range; x++) {
            for (int y = -2; y <= 3; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos p = pos.offset(x, y, z);
                    BlockState state = MillVillager.level().getBlockState(p);

                    boolean ripe = false;

                    if (state.getBlock() instanceof BlockMillCrops) {
                        ripe = ((BlockMillCrops) state.getBlock()).isMaxAge(state);
                    } else if (state.is(Blocks.WHEAT) || state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES)
                            || state.is(Blocks.BEETROOTS)) {
                        // Vanilla crops usually max age is 7 (Beetroot is 3)
                        int age = state.getValue(net.minecraft.world.level.block.CropBlock.AGE);
                        int maxAge = (state.is(Blocks.BEETROOTS)) ? 3 : 7;
                        ripe = (age >= maxAge);
                    }

                    if (ripe) {
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

