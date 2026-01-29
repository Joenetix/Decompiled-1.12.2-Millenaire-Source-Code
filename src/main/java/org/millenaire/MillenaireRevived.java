package org.millenaire;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.millenaire.commands.MillenaireCommands;
import org.millenaire.common.culture.CultureRegistry;
import org.millenaire.core.MillBlockEntities;
import org.slf4j.Logger;

/**
 * Millenaire Revived - Autonomous cultural villages
 * 
 * A standalone mod inspired by the original Millenaire and modern MineColonies
 * architecture.
 * We build everything ourselves: custom AI, building system, resource
 * management, etc.
 */
@Mod(MillenaireRevived.MODID)
public class MillenaireRevived {
    public static final String MODID = "millenaire";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MillenaireRevived() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        // ===== PHASE 1: Modern DeferredRegister System =====
        // Register all blocks, items, block entities, and creative tabs
        org.millenaire.core.MillBlocks.BLOCKS.register(modEventBus);
        org.millenaire.core.MillItems.ITEMS.register(modEventBus);
        org.millenaire.core.MillEntities.ENTITIES.register(modEventBus);
        MillBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        org.millenaire.core.MillCreativeTabs.CREATIVE_TABS.register(modEventBus);
        org.millenaire.core.MillMenus.MENUS.register(modEventBus);

        // Register Entity Attributes
        modEventBus.addListener(this::onAttributeCreation);

        // Initialize Goals
        org.millenaire.common.goal.Goal.initGoals();

        // ===== PHASE 2: Commands and Culture System =====
        CultureRegistry.register(modEventBus);
        forgeEventBus.addListener(this::onRegisterCommands);
        forgeEventBus.addListener(this::onServerStarting);

        // ===== PHASE 3: Networking =====
        // ===== PHASE 3: Networking =====
        org.millenaire.common.network.ServerSender.registerPackets();

        LOGGER.info("Millenaire Revived - Phase 1 & 2: Core Registration Initialized");
        LOGGER.info("Registered: Blocks, Items, Entities, Creative Tabs, and 5 Cultures");
    }

    private void onAttributeCreation(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
        // Citizen entity attributes
        event.put(org.millenaire.core.MillEntities.CITIZEN.get(),
                org.millenaire.common.entity.MillVillager.createMobAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 20.0)
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.25)
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 1.0).build());

        // Targeted Blaze - uses Blaze attributes
        event.put(org.millenaire.core.MillEntities.TARGETED_BLAZE.get(),
                net.minecraft.world.entity.monster.Blaze.createAttributes().build());

        // Targeted Ghast - uses Ghast attributes
        event.put(org.millenaire.core.MillEntities.TARGETED_GHAST.get(),
                net.minecraft.world.entity.monster.Ghast.createAttributes().build());

        // Targeted Wither Skeleton - uses AbstractSkeleton attributes
        event.put(org.millenaire.core.MillEntities.TARGETED_WITHER_SKELETON.get(),
                net.minecraft.world.entity.monster.AbstractSkeleton.createAttributes().build());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        MillenaireCommands.register(event.getDispatcher());
        LOGGER.info("Registered Millénaire commands: /millenaire testbuild, listbuildings, listcultures");
    }

    private void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
        // Load village configurations (lazy load on server start to access Resource
        // Manager)
        // Load village configurations via Registry
        // org.millenaire.common.culture.MillenaireCultures.initVillages(); // Moved to
        // CultureRegistry
        LOGGER.info("Loaded Millénaire village configurations");
    }
}
