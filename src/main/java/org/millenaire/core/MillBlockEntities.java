package org.millenaire.core;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.millenaire.MillenaireRevived;
import org.millenaire.common.block.LockedChestBlockEntity;
import org.millenaire.core.block.PanelBlock;
import org.millenaire.core.entity.TileEntityPanel;
import org.millenaire.core.entity.TileEntityFirePit;
import org.millenaire.core.entity.TileEntityImportTable;

public class MillBlockEntities {
        public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
                        .create(ForgeRegistries.BLOCK_ENTITY_TYPES, MillenaireRevived.MODID);

        public static final RegistryObject<BlockEntityType<TileEntityPanel>> PANEL = BLOCK_ENTITIES.register("panel",
                        () -> BlockEntityType.Builder.of(TileEntityPanel::new, MillBlocks.PANEL.get()).build(null));

        public static final RegistryObject<BlockEntityType<LockedChestBlockEntity>> LOCKED_CHEST = BLOCK_ENTITIES
                        .register("locked_chest",
                                        () -> BlockEntityType.Builder
                                                        .of(LockedChestBlockEntity::new, MillBlocks.LOCKED_CHEST.get())
                                                        .build(null));

        public static final RegistryObject<BlockEntityType<TileEntityFirePit>> FIRE_PIT = BLOCK_ENTITIES
                        .register("fire_pit",
                                        () -> BlockEntityType.Builder
                                                        .of(TileEntityFirePit::new, MillBlocks.FIRE_PIT.get())
                                                        .build(null));

        public static final RegistryObject<BlockEntityType<TileEntityImportTable>> IMPORT_TABLE = BLOCK_ENTITIES
                        .register("import_table",
                                        () -> BlockEntityType.Builder
                                                        .of(TileEntityImportTable::new, MillBlocks.IMPORT_TABLE.get())
                                                        .build(null));
}

