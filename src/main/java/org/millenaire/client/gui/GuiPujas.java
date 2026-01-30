package org.millenaire.client.gui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiContainerEvent.DrawForeground;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;
import org.millenaire.client.network.ClientSender;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.ui.ContainerPuja;
import org.millenaire.common.utilities.LanguageUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

public class GuiPujas extends GuiContainer {
   private static final ResourceLocation texturePujas = new ResourceLocation("millenaire", "textures/gui/pujas.png");
   private static final ResourceLocation textureSacrifices = new ResourceLocation("millenaire",
         "textures/gui/mayansacrifices.png");
   private final Building temple;
   private final EntityPlayer player;
   private final Method drawSlotInventory;

   public GuiPujas(EntityPlayer player, Building temple) {
      super(new ContainerPuja(player, temple));
      this.ySize = 188;
      this.temple = temple;
      this.player = player;
      if (MillConfigValues.LogPujas >= 3) {
         MillLog.debug(this, "Opening shrine GUI");
      }

      this.drawSlotInventory = MillCommonUtilities.getDrawSlotInventoryMethod(this);

   }

   protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.temple.pujas != null && this.temple.pujas.type == 1) {
         this.mc.getTextureManager().bindTexture(textureSacrifices);
      } else {
         this.mc.getTextureManager().bindTexture(texturePujas);
      }

      int x = (this.width - this.xSize) / 2;
      int y = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
      if (this.temple.pujas != null) {
         int linePos = 0;
         int colPos = 0;

         for (int cp = 0; cp < this.temple.pujas.getTargets().size(); cp++) {
            if (this.temple.pujas.currentTarget == this.temple.pujas.getTargets().get(cp)) {
               this.drawTexturedModalRect(
                     x + this.getTargetXStart() + colPos * this.getButtonWidth(),
                     y + this.getTargetYStart() + this.getButtonHeight() * linePos,
                     this.temple.pujas.getTargets().get(cp).startXact,
                     this.temple.pujas.getTargets().get(cp).startYact,
                     this.getButtonWidth(),
                     this.getButtonHeight());
            } else {
               this.drawTexturedModalRect(
                     x + this.getTargetXStart() + colPos * this.getButtonWidth(),
                     y + this.getTargetYStart() + this.getButtonHeight() * linePos,
                     this.temple.pujas.getTargets().get(cp).startX,
                     this.temple.pujas.getTargets().get(cp).startY,
                     this.getButtonWidth(),
                     this.getButtonHeight());
            }

            if (++colPos >= this.getNbPerLines()) {
               colPos = 0;
               linePos++;
            }
         }

         int progress = this.temple.pujas.getPujaProgressScaled(13);
         this.drawTexturedModalRect(x + 27, y + 39 + 13 - progress, 176, 13 - progress, 15, progress);
         progress = this.temple.pujas.getOfferingProgressScaled(16);
         this.drawTexturedModalRect(x + 84, y + 63 + 16 - progress, 176, 47 - progress, 19, progress);
      }
   }

   protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
      if (this.temple.pujas.type == 1) {
         this.fontRenderer.drawString(LanguageUtilities.string("sacrifices.offering"), 8, 6, 4210752);
         this.fontRenderer.drawString(LanguageUtilities.string("sacrifices.panditfee"), 8, 75, 4210752);
      } else {
         this.fontRenderer.drawString(LanguageUtilities.string("pujas.offering"), 8, 6, 4210752);
         this.fontRenderer.drawString(LanguageUtilities.string("pujas.panditfee"), 8, 75, 4210752);
      }

      this.fontRenderer.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 94 + 2, 4210752);
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      int i = this.guiLeft;
      int j = this.guiTop;
      this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
      GlStateManager.disableRescaleNormal();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();

      try {
         for (int k = 0; k < this.buttonList.size(); k++) {
            GuiButton guibutton = (GuiButton) this.buttonList.get(k);
            guibutton.drawButton(this.mc, i, j, partialTicks);
         }
      } catch (Exception var18) {
         MillLog.printException("Exception in button rendering: ", var18);
      }

      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.pushMatrix();
      GlStateManager.translate(i, j, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableRescaleNormal();
      int k = 240;
      int l = 240;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      Slot hoveredSlot = null;

      for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); i1++) {
         Slot slot1 = (Slot) this.inventorySlots.inventorySlots.get(i1);
         this.drawSlotInventory(slot1);
         if (this.getIsMouseOverSlot(slot1, mouseX, mouseY)) {
            hoveredSlot = slot1;
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            int j1 = slot1.xPos;
            int k1 = slot1.yPos;
            GlStateManager.colorMask(true, true, true, false);
            this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
         }
      }

      RenderHelper.disableStandardItemLighting();
      this.drawGuiContainerForegroundLayer(mouseX, mouseY);
      RenderHelper.enableGUIStandardItemLighting();
      MinecraftForge.EVENT_BUS.post(new DrawForeground(this, mouseX, mouseY));
      InventoryPlayer inventoryplayer = this.mc.player.inventory;
      if (inventoryplayer.getItemStack() != null) {
         this.itemRender.renderItemAndEffectIntoGUI(inventoryplayer.getItemStack(), i - 240 - 8, j - 240 - 8);
         this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, inventoryplayer.getItemStack(), i - 240 - 8,
               j - 240 - 8, null);
      }

      GlStateManager.popMatrix();
      GlStateManager.enableLighting();
      GlStateManager.enableDepth();
      RenderHelper.enableStandardItemLighting();
      if (inventoryplayer.getItemStack().isEmpty() && hoveredSlot != null) {
         List<String> list = null;
         ItemStack itemstack = null;
         if (hoveredSlot.getHasStack()) {
            itemstack = hoveredSlot.getStack();
            list = itemstack.getTooltip(
                  this.mc.player,
                  this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL);
            this.renderToolTipCustom(itemstack, mouseX, mouseY, list);
         } else if (hoveredSlot instanceof ContainerPuja.OfferingSlot) {
            List<String> var24 = new ArrayList();
            var24.add("§6" + LanguageUtilities.string("pujas.offeringslot"));
            var24.add("§7" + LanguageUtilities.string("pujas.offeringslot2"));
            this.renderToolTipCustom(itemstack, mouseX, mouseY, var24);
         } else if (hoveredSlot instanceof ContainerPuja.MoneySlot) {
            List<String> var25 = new ArrayList();
            var25.add("§6" + LanguageUtilities.string("pujas.moneyslot"));
            this.renderToolTipCustom(itemstack, mouseX, mouseY, var25);
         } else if (hoveredSlot instanceof ContainerPuja.ToolSlot) {
            List<String> var26 = new ArrayList();
            var26.add("§6" + LanguageUtilities.string("pujas.toolslot"));
            this.renderToolTipCustom(itemstack, mouseX, mouseY, var26);
         }
      }

      int startx = (this.width - this.xSize) / 2;
      int starty = (this.height - this.ySize) / 2;
      if (this.temple.pujas != null) {
         int linePos = 0;
         int colPos = 0;

         for (int cp = 0; cp < this.temple.pujas.getTargets().size(); cp++) {
            if (mouseX > startx + this.getTargetXStart() + colPos * this.getButtonWidth()
                  && mouseX < startx + this.getTargetXStart() + (colPos + 1) * this.getButtonWidth()
                  && mouseY > starty + this.getTargetYStart() + this.getButtonHeight() * linePos
                  && mouseY < starty + this.getTargetYStart() + this.getButtonHeight() * (linePos + 1)) {
               String s = LanguageUtilities.string(this.temple.pujas.getTargets().get(cp).mouseOver);
               int stringlength = this.fontRenderer.getStringWidth(s);
               this.drawGradientRect(mouseX + 5, mouseY - 3, mouseX + stringlength + 10, mouseY + 8 + 3, -1073741824,
                     -1073741824);
               this.fontRenderer.drawString(s, mouseX + 8, mouseY, 15790320);
            }

            if (++colPos >= this.getNbPerLines()) {
               colPos = 0;
               linePos++;
            }
         }
      }
   }

   public void drawSlotInventory(Slot slot) {
      try {
         this.drawSlotInventory.invoke(this, slot);
      } catch (Exception var3) {
         MillLog.printException("Exception when trying to access drawSlotInventory", var3);
      }
   }

   private int getButtonHeight() {
      if (this.temple.pujas == null) {
         return 0;
      } else if (this.temple.pujas.type == 0) {
         return 17;
      } else {
         return this.temple.pujas.type == 1 ? 20 : 0;
      }
   }

   private int getButtonWidth() {
      if (this.temple.pujas == null) {
         return 0;
      } else if (this.temple.pujas.type == 0) {
         return 46;
      } else {
         return this.temple.pujas.type == 1 ? 20 : 0;
      }
   }

   private boolean getIsMouseOverSlot(Slot slot, int i, int j) {
      int k = (this.width - this.xSize) / 2;
      int l = (this.height - this.ySize) / 2;
      i -= k;
      j -= l;
      return i >= slot.xPos - 1 && i < slot.xPos + 16 + 1 && j >= slot.yPos - 1 && j < slot.yPos + 16 + 1;
   }

   private int getNbPerLines() {
      if (this.temple.pujas == null) {
         return 1;
      } else if (this.temple.pujas.type == 0) {
         return 1;
      } else {
         return this.temple.pujas.type == 1 ? 3 : 1;
      }
   }

   private int getTargetXStart() {
      if (this.temple.pujas == null) {
         return 0;
      } else if (this.temple.pujas.type == 0) {
         return 118;
      } else {
         return this.temple.pujas.type == 1 ? 110 : 0;
      }
   }

   private int getTargetYStart() {
      if (this.temple.pujas == null) {
         return 0;
      } else if (this.temple.pujas.type == 0) {
         return 22;
      } else {
         return this.temple.pujas.type == 1 ? 22 : 0;
      }
   }

   protected void mouseClicked(int x, int y, int par3) throws IOException {
      super.mouseClicked(x, y, par3);
      int startx = (this.width - this.xSize) / 2;
      int starty = (this.height - this.ySize) / 2;
      if (this.temple.pujas != null) {
         int linePos = 0;
         int colPos = 0;

         for (int cp = 0; cp < this.temple.pujas.getTargets().size(); cp++) {
            if (x > startx + this.getTargetXStart() + colPos * this.getButtonWidth()
                  && x < startx + this.getTargetXStart() + (colPos + 1) * this.getButtonWidth()
                  && y > starty + this.getTargetYStart() + this.getButtonHeight() * linePos
                  && y < starty + this.getTargetYStart() + this.getButtonHeight() * (linePos + 1)) {
               ClientSender.pujasChangeEnchantment(this.player, this.temple, cp);
            }

            if (++colPos >= this.getNbPerLines()) {
               colPos = 0;
               linePos++;
            }
         }
      }
   }

   protected void renderToolTipCustom(ItemStack stack, int x, int y, List<String> customToolTip) {
      if (stack == null) {
         stack = ItemStack.EMPTY;
      }

      FontRenderer font = stack.getItem().getFontRenderer(stack);
      GuiUtils.preItemToolTip(stack);
      this.drawHoveringText(customToolTip, x, y, font == null ? this.fontRenderer : font);
      GuiUtils.postItemToolTip();
   }
}
