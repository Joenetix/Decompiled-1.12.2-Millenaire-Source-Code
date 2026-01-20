package org.millenaire.core;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.millenaire.MillenaireRevived;
import org.millenaire.entities.Citizen;
import org.millenaire.entities.EntityWallDecoration;
import org.millenaire.entities.EntityTargetedBlaze;
import org.millenaire.entities.EntityTargetedGhast;
import org.millenaire.entities.EntityTargetedWitherSkeleton;

/**
 * Registers all entities for Millénaire Revived.
 */
public class MillEntities {

        public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(
                        ForgeRegistries.ENTITY_TYPES,
                        MillenaireRevived.MODID);

        // Citizen entity - the main villager NPC (uses MillVillager implementation)
        public static final RegistryObject<EntityType<Citizen>> CITIZEN = ENTITIES.register("citizen",
                        () -> EntityType.Builder
                                        .<Citizen>of(org.millenaire.common.entity.MillVillager::new,
                                                        MobCategory.CREATURE)
                                        .sized(0.6f, 1.95f)
                                        .clientTrackingRange(10)
                                        .build("citizen"));

        // Wall decoration entity - tapestries, icons, statues
        public static final RegistryObject<EntityType<EntityWallDecoration>> WALL_DECORATION = ENTITIES.register(
                        "wall_decoration",
                        () -> EntityType.Builder.<EntityWallDecoration>of(EntityWallDecoration::new, MobCategory.MISC)
                                        .sized(0.5F, 0.5F)
                                        .clientTrackingRange(10)
                                        .updateInterval(Integer.MAX_VALUE)
                                        .build("wall_decoration"));

        // Targeted Blaze - quest monster
        public static final RegistryObject<EntityType<EntityTargetedBlaze>> TARGETED_BLAZE = ENTITIES.register(
                        "targeted_blaze",
                        () -> EntityType.Builder.of(EntityTargetedBlaze::new, MobCategory.MONSTER)
                                        .sized(0.6F, 1.8F)
                                        .fireImmune()
                                        .clientTrackingRange(8)
                                        .build("targeted_blaze"));

        // Targeted Ghast - quest monster
        public static final RegistryObject<EntityType<EntityTargetedGhast>> TARGETED_GHAST = ENTITIES.register(
                        "targeted_ghast",
                        () -> EntityType.Builder.of(EntityTargetedGhast::new, MobCategory.MONSTER)
                                        .sized(4.0F, 4.0F)
                                        .fireImmune()
                                        .clientTrackingRange(10)
                                        .build("targeted_ghast"));

        // Targeted Wither Skeleton - quest monster
        public static final RegistryObject<EntityType<EntityTargetedWitherSkeleton>> TARGETED_WITHER_SKELETON = ENTITIES
                        .register("targeted_wither_skeleton",
                                        () -> EntityType.Builder
                                                        .of(EntityTargetedWitherSkeleton::new, MobCategory.MONSTER)
                                                        .sized(0.7F, 2.4F)
                                                        .fireImmune()
                                                        .clientTrackingRange(8)
                                                        .build("targeted_wither_skeleton"));
}
