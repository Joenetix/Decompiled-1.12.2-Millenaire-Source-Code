package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Alchemist explosive block for Millénaire.
 * Used in Byzantine culture for alchemy and explosives.
 * When exploded, clears a 32-block radius sphere.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockAlchemistExplosive extends Block {

    private static final int EXPLOSION_RADIUS = 32;

    public BlockAlchemistExplosive() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    public BlockAlchemistExplosive(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Triggered when block is destroyed by explosion.
     * Creates a massive sphere of destruction.
     */
    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) {
            alchemistExplosion(level, pos);
        }
        super.onBlockExploded(state, level, pos, explosion);
    }

    /**
     * Creates a 32-block radius sphere of air, clearing all blocks within.
     */
    private void alchemistExplosion(Level level, BlockPos pos) {
        int centreX = pos.getX();
        int centreY = pos.getY();
        int centreZ = pos.getZ();

        // Clear the block itself first
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        // Clear sphere from top to bottom for proper block updates
        for (int dy = EXPLOSION_RADIUS; dy >= -EXPLOSION_RADIUS; dy--) {
            int worldY = centreY + dy;
            if (worldY >= level.getMinBuildHeight() && worldY < level.getMaxBuildHeight()) {
                for (int dx = -EXPLOSION_RADIUS; dx <= EXPLOSION_RADIUS; dx++) {
                    for (int dz = -EXPLOSION_RADIUS; dz <= EXPLOSION_RADIUS; dz++) {
                        // Check if within sphere (radius squared = 1024)
                        if (dx * dx + dy * dy + dz * dz <= EXPLOSION_RADIUS * EXPLOSION_RADIUS) {
                            BlockPos targetPos = new BlockPos(centreX + dx, worldY, centreZ + dz);
                            BlockState targetState = level.getBlockState(targetPos);
                            if (!targetState.isAir()) {
                                level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }
}
