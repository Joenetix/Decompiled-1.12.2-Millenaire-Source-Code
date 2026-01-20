package org.millenaire.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.millenaire.MillenaireRevived;
import org.millenaire.core.MillBlocks;

/**
 * Client-side setup for Millénaire blocks.
 * Registers render types for transparent/translucent blocks.
 */
@Mod.EventBusSubscriber(modid = MillenaireRevived.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register translucent render type for stained glass pane
            ItemBlockRenderTypes.setRenderLayer(MillBlocks.STAINED_GLASS.get(), RenderType.translucent());
        });
    }

    @SubscribeEvent
    public static void registerRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(org.millenaire.core.MillEntities.CITIZEN.get(), CitizenRenderer::new);
        event.registerBlockEntityRenderer(org.millenaire.core.MillBlockEntities.PANEL.get(),
                org.millenaire.client.renderer.PanelBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(
            net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions event) {
        // ... (Layer definitions if any)
    }

    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        net.minecraft.client.gui.screens.MenuScreens.register(org.millenaire.core.MillMenus.TRADE.get(),
                org.millenaire.client.gui.GuiTrade::new);
    }
}
