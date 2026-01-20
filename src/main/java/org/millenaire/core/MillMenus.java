package org.millenaire.core;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.millenaire.MillenaireRevived;
import org.millenaire.gui.MenuTrade;

public class MillMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES,
            MillenaireRevived.MODID);

    public static final RegistryObject<MenuType<MenuTrade>> TRADE = MENUS.register("trade",
            () -> IForgeMenuType.create(MenuTrade::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
