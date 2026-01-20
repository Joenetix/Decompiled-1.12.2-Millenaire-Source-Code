package org.millenaire.common.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.millenaire.common.block.BlockMillCrops;
import org.millenaire.core.MillBlocks;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.entity.MillVillager;

public class GoalFarmerPlantCrops extends Goal {

    public GoalFarmerPlantCrops() {
        this.key = "farmerPlantCrops";
    }

    @Override
    public boolean isPossible(MillVillager MillVillager) {
        if (!MillVillager.level().isDay())
            return false;

        // Check if villager has any seeds
        return hasSeeds(MillVillager);
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        BlockPos target = findEmptyFarmland(MillVillager);

        if (target != null) {
            double distSq = MillVillager.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);

            if (distSq > 9.0) {
                MillVillager.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.0);
            } else {
                // Plant seeds
                MillVillager.swing(InteractionHand.MAIN_HAND);

                Item seedToPlant = getFirstSeed(MillVillager);
                if (seedToPlant != null) {
                    BlockPos planterPos = target.above();
                    if (MillVillager.level().isEmptyBlock(planterPos)) {
                        // Determine block to place based on seed
                        if (seedToPlant == Items.WHEAT_SEEDS)
                            MillVillager.level().setBlock(planterPos, Blocks.WHEAT.defaultBlockState(), 3);
                        else if (seedToPlant == Items.CARROT)
                            MillVillager.level().setBlock(planterPos, Blocks.CARROTS.defaultBlockState(), 3);
                        else if (seedToPlant == Items.POTATO)
                            MillVillager.level().setBlock(planterPos, Blocks.POTATOES.defaultBlockState(), 3);
                        else if (seedToPlant == Items.BEETROOT_SEEDS)
                            MillVillager.level().setBlock(planterPos, Blocks.BEETROOTS.defaultBlockState(), 3);
                        // Millenaire crop mappings
                        else if (seedToPlant == org.millenaire.core.MillItems.RICE.get())
                            MillVillager.level().setBlock(planterPos, MillBlocks.CROP_RICE.get().defaultBlockState(), 3);
                        else if (seedToPlant == org.millenaire.core.MillItems.TURMERIC.get())
                            MillVillager.level().setBlock(planterPos, MillBlocks.CROP_TURMERIC.get().defaultBlockState(), 3);
                        else if (seedToPlant == org.millenaire.core.MillItems.MAIZE.get())
                            MillVillager.level().setBlock(planterPos, MillBlocks.CROP_MAIZE.get().defaultBlockState(), 3);
                        else if (seedToPlant == org.millenaire.core.MillItems.COTTON.get())
                            MillVillager.level().setBlock(planterPos, MillBlocks.CROP_COTTON.get().defaultBlockState(), 3);
                        else if (seedToPlant == org.millenaire.core.MillItems.GRAPES.get())
                            MillVillager.level().setBlock(planterPos, MillBlocks.CROP_VINE.get().defaultBlockState(), 3);

                        // Remove from inventory
                        MillVillager.takeFromInventory(seedToPlant, 1);
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
        return 40; // Lower than harvesting
    }

    private boolean hasSeeds(MillVillager MillVillager) {
        return getFirstSeed(MillVillager) != null;
    }

    private Item getFirstSeed(MillVillager MillVillager) {
        // Check standard seeds
        if (MillVillager.countInv(InvItem.createInvItem(Items.WHEAT_SEEDS)) > 0)
            return Items.WHEAT_SEEDS;
        if (MillVillager.countInv(InvItem.createInvItem(Items.CARROT)) > 0)
            return Items.CARROT;
        if (MillVillager.countInv(InvItem.createInvItem(Items.POTATO)) > 0)
            return Items.POTATO;
        if (MillVillager.countInv(InvItem.createInvItem(Items.BEETROOT_SEEDS)) > 0)
            return Items.BEETROOT_SEEDS;
        if (MillVillager.countInv(InvItem.createInvItem(Items.BEETROOT_SEEDS)) > 0)
            return Items.BEETROOT_SEEDS;

        // Check Millenaire crops
        for (org.millenaire.common.culture.Culture c : org.millenaire.common.culture.Culture.getAllCultures()
                .values()) {
            for (org.millenaire.common.culture.CropDefinition crop : c.getListCrops()) {
                net.minecraft.world.item.Item seed = net.minecraftforge.registries.ForgeRegistries.ITEMS
                        .getValue(new net.minecraft.resources.ResourceLocation(crop.seedItem()));
                if (seed != null && MillVillager.countInv(InvItem.createInvItem(seed)) > 0) {
                    return seed;
                }
            }
        }
        return null;
    }

    private BlockPos findEmptyFarmland(MillVillager MillVillager) {
        BlockPos pos = MillVillager.blockPosition();
        BlockPos nearest = null;
        double minDist = Double.MAX_VALUE;
        int range = 24;

        for (int x = -range; x <= range; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos p = pos.offset(x, y, z);
                    BlockState state = MillVillager.level().getBlockState(p);

                    if (state.is(Blocks.FARMLAND)) {
                        if (MillVillager.level().isEmptyBlock(p.above())) {
                            double d = p.distSqr(pos);
                            if (d < minDist) {
                                minDist = d;
                                nearest = p;
                            }
                        }
                    }
                }
            }
        }
        return nearest;
    }
}

