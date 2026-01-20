package org.millenaire.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.millenaire.MillenaireRevived;
import org.millenaire.core.MillMenus;
import org.millenaire.common.culture.VillageType;
import org.millenaire.entities.Citizen;
import org.millenaire.gui.MenuTrade;

import java.util.List;
import java.util.Map;
import org.millenaire.core.MillItems;

public class GuiTrade extends AbstractContainerScreen<MenuTrade> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(MillenaireRevived.MODID,
            "textures/gui/trade.png");
    private final Citizen citizen;

    private int sellingRow = 0;
    private int buyingRow = 0;

    public GuiTrade(MenuTrade menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.citizen = menu.getCitizen();
        this.imageWidth = 256;
        this.imageHeight = 222; // Original size roughly
        this.inventoryLabelX = 44;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();

        // Scroll Buttons
        // Selling Scroll Up
        this.addRenderableWidget(Button.builder(Component.literal("^"), (btn) -> {
            if (sellingRow > 0) {
                sellingRow--;
                this.menu.updateScroll(true, sellingRow);
            }
        }).bounds(this.leftPos + 216, this.topPos + 68, 11, 7).build());

        // Selling Scroll Down
        this.addRenderableWidget(Button.builder(Component.literal("v"), (btn) -> {
            if (sellingRow < this.menu.nbRowSelling - 2) {
                sellingRow++;
                this.menu.updateScroll(true, sellingRow);
            }
        }).bounds(this.leftPos + 230, this.topPos + 68, 11, 7).build());

        // Buying Scroll Up
        this.addRenderableWidget(Button.builder(Component.literal("^"), (btn) -> {
            if (buyingRow > 0) {
                buyingRow--;
                this.menu.updateScroll(false, buyingRow);
            }
        }).bounds(this.leftPos + 216, this.topPos + 122, 11, 7).build());

        // Buying Scroll Down
        this.addRenderableWidget(Button.builder(Component.literal("v"), (btn) -> {
            if (buyingRow < this.menu.nbRowBuying - 2) {
                buyingRow++;
                this.menu.updateScroll(false, buyingRow);
            }
        }).bounds(this.leftPos + 230, this.topPos + 122, 11, 7).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Draw Trade List manually for now?
        // VillageType vt = citizen.getVillage().getVillageType(); // Needs Village ref
        // on client?
        // Citizen interaction usually syncs this data or we need a packet.
        // For MVP, just assuming we have data.
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        guiGraphics.drawString(this.font, Component.literal("Selling:"), 8, 22, 4210752, false);
        guiGraphics.drawString(this.font, Component.literal("Buying:"), 8, 76, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                4210752, false);

        // Calculate Player Money
        long money = 0;
        if (this.minecraft != null && this.minecraft.player != null) {
            for (ItemStack stack : this.minecraft.player.getInventory().items) {
                if (stack.getItem() == MillItems.DENIER.get()) {
                    money += stack.getCount();
                }
                if (stack.getItem() == MillItems.DENIER_ARGENT.get()) {
                    money += stack.getCount() * 64;
                }
                if (stack.getItem() == MillItems.DENIER_OR.get()) {
                    money += stack.getCount() * 64 * 64;
                }
            }
        }
        guiGraphics.drawString(this.font, Component.literal("Purse: " + money + " deniers"), 120, 6, 4210752, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack stack = this.hoveredSlot.getItem();
            List<Component> tooltip = getTooltipFromItem(this.minecraft, stack);

            // Add Price Info
            if (this.hoveredSlot instanceof MenuTrade.TradeSlot) {
                MenuTrade.TradeSlot ts = (MenuTrade.TradeSlot) this.hoveredSlot;
                if (ts.entry != null) {
                    tooltip.add(Component
                            .literal((ts.entry.selling ? "Buy for: " : "Sell for: ") + ts.entry.price + " deniers")
                            .withStyle(net.minecraft.ChatFormatting.GOLD));
                }
            }

            guiGraphics.renderTooltip(this.font, tooltip, stack.getTooltipImage(), stack, x, y);
        }
    }
}
