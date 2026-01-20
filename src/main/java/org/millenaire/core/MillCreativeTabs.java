package org.millenaire.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.millenaire.MillenaireRevived;

/**
 * Creative tabs for Millénaire 8.1.2 content organized by culture.
 * 
 * 6 tabs total:
 * - General: Currency, wands, amulets, seeds, parchments
 * - Norman: Medieval French culture
 * - Indian: North Indian/Hindi culture
 * - Japanese: Eastern culture
 * - Mayan: Mesoamerican culture
 * - Byzantine: Eastern Roman culture
 */
public class MillCreativeTabs {

        public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(
                        Registries.CREATIVE_MODE_TAB,
                        MillenaireRevived.MODID);

        // =============================================================================
        // GENERAL TAB - Currency, special items, seeds, parchments
        // =============================================================================

        public static final RegistryObject<CreativeModeTab> GENERAL_TAB = CREATIVE_TABS.register("general",
                        () -> CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup.millenaire.general"))
                                        .icon(() -> new ItemStack(MillItems.DENIER_OR.get()))
                                        .displayItems((parameters, output) -> {
                                                // Currency
                                                output.accept(MillItems.DENIER.get());
                                                output.accept(MillItems.DENIER_ARGENT.get());
                                                output.accept(MillItems.DENIER_OR.get());
                                                output.accept(MillItems.PURSE.get());

                                                // Special items
                                                output.accept(MillItems.SUMMONING_WAND.get());
                                                output.accept(MillItems.NEGATION_WAND.get());

                                                // Amulets
                                                output.accept(MillItems.AMULET_VISHNU.get());
                                                output.accept(MillItems.AMULET_ALCHEMIST.get());
                                                output.accept(MillItems.AMULET_YGGDRASIL.get());
                                                output.accept(MillItems.AMULET_SKOLL_HATI.get());

                                                // Seeds
                                                output.accept(MillItems.RICE.get());
                                                output.accept(MillItems.TURMERIC.get());
                                                output.accept(MillItems.MAIZE.get());
                                                output.accept(MillItems.GRAPES.get());
                                                output.accept(MillItems.COTTON.get());

                                                // Parchments
                                                output.accept(MillItems.PARCHMENT_VILLAGE_SCROLL.get());
                                                output.accept(MillItems.PARCHMENT_SADHU.get());
                                        })
                                        .build());

        // =============================================================================
        // NORMAN TAB - Medieval French culture
        // =============================================================================

        public static final RegistryObject<CreativeModeTab> NORMAN_TAB = CREATIVE_TABS.register("norman",
                        () -> CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup.millenaire.norman"))
                                        .icon(() -> new ItemStack(MillItems.NORMAN_SWORD.get()))
                                        .displayItems((parameters, output) -> {
                                                // Tools & Weapons
                                                output.accept(MillItems.NORMAN_PICKAXE.get());
                                                output.accept(MillItems.NORMAN_AXE.get());
                                                output.accept(MillItems.NORMAN_SHOVEL.get());
                                                output.accept(MillItems.NORMAN_HOE.get());
                                                output.accept(MillItems.NORMAN_SWORD.get());

                                                // Armor
                                                output.accept(MillItems.NORMAN_HELMET.get());
                                                output.accept(MillItems.NORMAN_CHESTPLATE.get());
                                                output.accept(MillItems.NORMAN_LEGGINGS.get());
                                                output.accept(MillItems.NORMAN_BOOTS.get());

                                                // Food
                                                output.accept(MillItems.CIDER_APPLE.get());
                                                output.accept(MillItems.CIDER.get());
                                                output.accept(MillItems.CALVA.get());
                                                output.accept(MillItems.BOUDIN.get());
                                                output.accept(MillItems.TRIPES.get());

                                                // Decorations
                                                output.accept(MillItems.TAPESTRY.get());

                                                // Blocks
                                                output.accept(MillBlocks.WOOD_DECORATION.get());
                                                output.accept(MillBlocks.STONE_DECORATION.get());
                                                output.accept(MillBlocks.EARTH_DECORATION.get());
                                                output.accept(MillBlocks.EXTENDED_MUD_BRICK.get());
                                                output.accept(MillBlocks.WALL_MUD_BRICK.get());

                                                // Parchments
                                                output.accept(MillItems.PARCHMENT_NORMAN_VILLAGERS.get());
                                                output.accept(MillItems.PARCHMENT_NORMAN_BUILDINGS.get());
                                                output.accept(MillItems.PARCHMENT_NORMAN_ITEMS.get());
                                                output.accept(MillItems.PARCHMENT_NORMAN_COMPLETE.get());
                                        })
                                        .build());

        // =============================================================================
        // INDIAN TAB - North Indian/Hindi culture
        // =============================================================================

        public static final RegistryObject<CreativeModeTab> INDIAN_TAB = CREATIVE_TABS.register("indian",
                        () -> CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup.millenaire.indian"))
                                        .icon(() -> new ItemStack(MillItems.RASGULLA.get()))
                                        .displayItems((parameters, output) -> {
                                                // Food
                                                output.accept(MillItems.VEG_CURRY.get());
                                                output.accept(MillItems.CHICKEN_CURRY.get());
                                                output.accept(MillItems.RASGULLA.get());

                                                // Items
                                                output.accept(MillItems.BRICK_MOULD.get());
                                                output.accept(MillItems.INDIAN_STATUE.get());

                                                // Blocks
                                                output.accept(MillBlocks.SANDSTONE_CARVED.get());
                                                output.accept(MillBlocks.SANDSTONE_RED_CARVED.get());
                                                output.accept(MillBlocks.SANDSTONE_OCHRE_CARVED.get());
                                                output.accept(MillBlocks.WET_BRICK.get());
                                                output.accept(MillBlocks.SILK_WORM.get());
                                                output.accept(MillBlocks.SNAIL_SOIL.get());

                                                // Parchments
                                                output.accept(MillItems.PARCHMENT_INDIAN_VILLAGERS.get());
                                                output.accept(MillItems.PARCHMENT_INDIAN_BUILDINGS.get());
                                                output.accept(MillItems.PARCHMENT_INDIAN_ITEMS.get());
                                                output.accept(MillItems.PARCHMENT_INDIAN_COMPLETE.get());
                                        })
                                        .build());

        // =============================================================================
        // JAPANESE TAB - Eastern culture
        // =============================================================================

        public static final RegistryObject<CreativeModeTab> JAPANESE_TAB = CREATIVE_TABS.register("japanese",
                        () -> CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup.millenaire.japanese"))
                                        .icon(() -> new ItemStack(MillItems.SAKE.get()))
                                        .displayItems((parameters, output) -> {
                                                // Weapons
                                                output.accept(MillItems.TACHI_SWORD.get());
                                                output.accept(MillItems.YUMI_BOW.get());

                                                // Armor - Red
                                                output.accept(MillItems.JAPANESE_RED_HELMET.get());
                                                output.accept(MillItems.JAPANESE_RED_CHESTPLATE.get());
                                                output.accept(MillItems.JAPANESE_RED_LEGGINGS.get());
                                                output.accept(MillItems.JAPANESE_RED_BOOTS.get());

                                                // Armor - Blue
                                                output.accept(MillItems.JAPANESE_BLUE_HELMET.get());
                                                output.accept(MillItems.JAPANESE_BLUE_CHESTPLATE.get());
                                                output.accept(MillItems.JAPANESE_BLUE_LEGGINGS.get());
                                                output.accept(MillItems.JAPANESE_BLUE_BOOTS.get());

                                                // Armor - Guard
                                                output.accept(MillItems.JAPANESE_GUARD_HELMET.get());
                                                output.accept(MillItems.JAPANESE_GUARD_CHESTPLATE.get());
                                                output.accept(MillItems.JAPANESE_GUARD_LEGGINGS.get());
                                                output.accept(MillItems.JAPANESE_GUARD_BOOTS.get());

                                                // Food
                                                output.accept(MillItems.UDON.get());
                                                output.accept(MillItems.SAKE.get());
                                                output.accept(MillItems.IKAYAKI.get());

                                                // Blocks
                                                output.accept(MillBlocks.GRAY_TILES.get());
                                                output.accept(MillBlocks.GREEN_TILES.get());
                                                output.accept(MillBlocks.RED_TILES.get());
                                                output.accept(MillBlocks.WOODEN_BARS.get());
                                                output.accept(MillBlocks.WOODEN_BARS_DARK.get());

                                                // Parchments
                                                output.accept(MillItems.PARCHMENT_JAPANESE_VILLAGERS.get());
                                                output.accept(MillItems.PARCHMENT_JAPANESE_BUILDINGS.get());
                                                output.accept(MillItems.PARCHMENT_JAPANESE_ITEMS.get());
                                                output.accept(MillItems.PARCHMENT_JAPANESE_COMPLETE.get());
                                        })
                                        .build());

        // =============================================================================
        // MAYAN TAB - Mesoamerican culture
        // =============================================================================

        public static final RegistryObject<CreativeModeTab> MAYAN_TAB = CREATIVE_TABS.register("mayan",
                        () -> CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup.millenaire.mayan"))
                                        .icon(() -> new ItemStack(MillItems.OBSIDIAN_FLAKE.get()))
                                        .displayItems((parameters, output) -> {
                                                // Tools & Weapons
                                                output.accept(MillItems.MAYAN_PICKAXE.get());
                                                output.accept(MillItems.MAYAN_AXE.get());
                                                output.accept(MillItems.MAYAN_SHOVEL.get());
                                                output.accept(MillItems.MAYAN_HOE.get());
                                                output.accept(MillItems.MAYAN_MACE.get());

                                                // Food
                                                output.accept(MillItems.WAH.get());
                                                output.accept(MillItems.BALCHE.get());
                                                output.accept(MillItems.SIKILPAH.get());
                                                output.accept(MillItems.MASA.get());
                                                output.accept(MillItems.CACAUHAA.get());

                                                // Items
                                                output.accept(MillItems.OBSIDIAN_FLAKE.get());
                                                output.accept(MillItems.MAYAN_QUEST_CROWN.get());
                                                output.accept(MillItems.MAYAN_STATUE.get());

                                                // Parchments
                                                output.accept(MillItems.PARCHMENT_MAYAN_VILLAGERS.get());
                                                output.accept(MillItems.PARCHMENT_MAYAN_BUILDINGS.get());
                                                output.accept(MillItems.PARCHMENT_MAYAN_ITEMS.get());
                                                output.accept(MillItems.PARCHMENT_MAYAN_COMPLETE.get());
                                        })
                                        .build());

        // =============================================================================
        // BYZANTINE TAB - Eastern Roman culture
        // =============================================================================

        public static final RegistryObject<CreativeModeTab> BYZANTINE_TAB = CREATIVE_TABS.register("byzantine",
                        () -> CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup.millenaire.byzantine"))
                                        .icon(() -> new ItemStack(MillItems.WINE_FANCY.get()))
                                        .displayItems((parameters, output) -> {
                                                // Tools & Weapons
                                                output.accept(MillItems.BYZANTINE_PICKAXE.get());
                                                output.accept(MillItems.BYZANTINE_AXE.get());
                                                output.accept(MillItems.BYZANTINE_SHOVEL.get());
                                                output.accept(MillItems.BYZANTINE_HOE.get());
                                                output.accept(MillItems.BYZANTINE_MACE.get());

                                                // Armor
                                                output.accept(MillItems.BYZANTINE_HELMET.get());
                                                output.accept(MillItems.BYZANTINE_CHESTPLATE.get());
                                                output.accept(MillItems.BYZANTINE_LEGGINGS.get());
                                                output.accept(MillItems.BYZANTINE_BOOTS.get());

                                                // Food
                                                output.accept(MillItems.OLIVES.get());
                                                output.accept(MillItems.OLIVEOIL.get());
                                                output.accept(MillItems.WINE_BASIC.get());
                                                output.accept(MillItems.WINE_FANCY.get());
                                                output.accept(MillItems.FETA.get());
                                                output.accept(MillItems.SOUVLAKI.get());

                                                // Items
                                                output.accept(MillItems.SILK.get());
                                                output.accept(MillItems.BYZANTINE_CLOTH_WOOL.get());
                                                output.accept(MillItems.BYZANTINE_CLOTH_SILK.get());
                                                output.accept(MillItems.BYZANTINE_ICON_SMALL.get());
                                                output.accept(MillItems.BYZANTINE_ICON_MEDIUM.get());
                                                output.accept(MillItems.BYZANTINE_ICON_LARGE.get());

                                                // Blocks
                                                output.accept(MillBlocks.BYZANTINE_TILES.get());
                                                output.accept(MillBlocks.WOODEN_BARS_INDIAN.get());
                                                output.accept(MillBlocks.WOODEN_BARS_ROSETTE.get());
                                                output.accept(MillBlocks.BYZANTINE_TILES_SLAB.get());
                                                output.accept(MillBlocks.STAIRS_BYZ_TILES.get());
                                                output.accept(MillBlocks.BYZANTINE_STONE_TILES.get());
                                                output.accept(MillBlocks.BYZANTINE_SANDSTONE_TILES.get());
                                                output.accept(MillBlocks.BYZANTINE_STONE_ORNAMENT.get());
                                                output.accept(MillBlocks.BYZANTINE_SANDSTONE_ORNAMENT.get());

                                                // Seljuk Items (Byzantine Sub-culture)
                                                output.accept(MillItems.SELJUK_SCIMITAR.get());
                                                output.accept(MillItems.SELJUK_BOW.get());
                                                output.accept(MillItems.SELJUK_TURBAN.get());
                                                output.accept(MillItems.SELJUK_HELMET.get());
                                                output.accept(MillItems.SELJUK_CHESTPLATE.get());
                                                output.accept(MillItems.SELJUK_LEGGINGS.get());
                                                output.accept(MillItems.SELJUK_BOOTS.get());
                                                output.accept(MillItems.PIDE.get());
                                                output.accept(MillItems.HELVA.get());
                                                output.accept(MillItems.LOKUM.get());
                                                output.accept(MillItems.AYRAN.get());
                                                output.accept(MillItems.YOGURT.get());
                                                output.accept(MillItems.PISTACHIOS.get());
                                                output.accept(MillItems.SELJUK_CLOTH_WOOL.get());
                                                output.accept(MillItems.SELJUK_CLOTH_COTTON.get());
                                                output.accept(MillItems.WALLCARPETSMALL.get());
                                                output.accept(MillItems.WALLCARPETMEDIUM.get());
                                                output.accept(MillItems.WALLCARPETLARGE.get());

                                                // Nature (Trees)
                                                output.accept(MillBlocks.SAPLING_OLIVETREE.get());
                                                output.accept(MillBlocks.LEAVES_OLIVETREE.get());
                                                output.accept(MillBlocks.SAPLING_PISTACHIO.get());
                                                output.accept(MillBlocks.LEAVES_PISTACHIO.get());

                                                // Crops
                                                output.accept(MillItems.CROP_RICE_ITEM.get());
                                                output.accept(MillItems.CROP_TURMERIC_ITEM.get());
                                                output.accept(MillItems.CROP_MAIZE_ITEM.get());
                                                output.accept(MillItems.CROP_COTTON_ITEM.get());
                                                output.accept(MillItems.CROP_VINE_ITEM.get());

                                                // Other
                                                output.accept(MillItems.PANEL_ITEM.get());
                                        })
                                        .build());
}
