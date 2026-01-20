package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mock block used during building generation to mark animal spawn locations.
 * During world generation, this block is converted to air and spawns the
 * appropriate animal.
 * 
 * The block identifier in building plans determines which animal to spawn:
 * - ANIMALSPAWN_COW
 * - ANIMALSPAWN_PIG
 * - ANIMALSPAWN_SHEEP
 * - ANIMALSPAWN_CHICKEN
 */
public class MockBlockAnimalSpawn extends Block {

    private final EntityType<? extends Mob> animalType;

    public MockBlockAnimalSpawn(EntityType<? extends Mob> animalType) {
        super(BlockBehaviour.Properties.of()
                .noCollission()
                .instabreak()
                .noOcclusion());
        this.animalType = animalType;
    }

    /**
     * Called after building generation to convert this block to air and spawn
     * animal.
     */
    public void convertToAnimal(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            // Replace with air
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

            // Spawn the animal
            if (animalType != null) {
                Mob animal = animalType.create(serverLevel);
                if (animal != null) {
                    animal.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    serverLevel.addFreshEntity(animal);
                }
            }
        }
    }

    public EntityType<? extends Mob> getAnimalType() {
        return animalType;
    }

    /**
     * Static helper to spawn an animal at a position based on animal type name.
     */
    public static void spawnAnimalAt(ServerLevel level, BlockPos pos, String animalTypeName) {
        EntityType<?> type = switch (animalTypeName.toLowerCase()) {
            case "cow" -> EntityType.COW;
            case "pig" -> EntityType.PIG;
            case "sheep" -> EntityType.SHEEP;
            case "chicken" -> EntityType.CHICKEN;
            case "horse" -> EntityType.HORSE;
            case "donkey" -> EntityType.DONKEY;
            case "goat" -> EntityType.GOAT;
            default -> null;
        };

        if (type != null) {
            Mob animal = (Mob) type.create(level);
            if (animal != null) {
                animal.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                level.addFreshEntity(animal);
            }
        }
    }
}

