package org.millenaire.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.entity.TileEntityPanel;

@SideOnly(Side.CLIENT)
public class TESRPanel extends TileEntitySpecialRenderer<TileEntityPanel> {
   private static final ResourceLocation PANEL_TEXTURE = new ResourceLocation("millenaire", "textures/entity/panels/default.png");
   private final ModelPanel model = new ModelPanel();

   private void drawIcon(int linePos, ItemStack icon, float xTranslate) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(xTranslate, -0.74F + linePos * 0.15, -0.09);
      this.renderItem2d(icon, 0.3F);
      GlStateManager.popMatrix();
   }

   public void render(TileEntityPanel te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      GlStateManager.pushMatrix();
      int k = te.getBlockMetadata();
      float f2 = 0.0F;
      if (k == 2) {
         f2 = 180.0F;
      }

      if (k == 4) {
         f2 = 90.0F;
      }

      if (k == 5) {
         f2 = -90.0F;
      }

      GlStateManager.translate((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
      GlStateManager.rotate(-f2, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(0.0F, 0.0F, -0.4375F);
      if (destroyStage >= 0) {
         this.bindTexture(DESTROY_STAGES[destroyStage]);
         GlStateManager.matrixMode(5890);
         GlStateManager.pushMatrix();
         GlStateManager.scale(4.0F, 2.0F, 1.0F);
         GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
         GlStateManager.matrixMode(5888);
      } else {
         ResourceLocation texture = te.texture != null ? te.texture : PANEL_TEXTURE;
         this.bindTexture(texture);
      }

      GlStateManager.enableRescaleNormal();
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.6666667F, -0.6666667F, -0.6666667F);
      this.model.renderSign();
      GlStateManager.translate(0.0, 0.24, 0.0);
      te.translateLines(this.getFontRenderer());

      for (int pos = 0; pos < te.displayLines.size(); pos++) {
         TileEntityPanel.PanelDisplayLine line = te.displayLines.get(pos);
         this.drawIcon(pos, line.leftIcon, -0.54F);
         this.drawIcon(pos, line.middleIcon, 0.08F);
         this.drawIcon(pos, line.rightIcon, 0.54F);
      }

      GlStateManager.popMatrix();
      FontRenderer fontrenderer = this.getFontRenderer();
      GlStateManager.translate(0.0, 0.25, 0.046666667F);
      GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);
      GlStateManager.glNormal3f(0.0F, 0.0F, -0.010416667F);
      GlStateManager.depthMask(false);
      if (destroyStage < 0) {
         for (int pos = 0; pos < te.displayLines.size(); pos++) {
            TileEntityPanel.PanelDisplayLine line = te.displayLines.get(pos);
            if (line.centerLine) {
               fontrenderer.drawString(line.fullLine, -fontrenderer.getStringWidth(line.fullLine) / 2, pos * 10 - 15, 0);
            } else {
               fontrenderer.drawString(line.fullLine, -29, pos * 10 - 15, 0);
            }

            fontrenderer.drawString(line.leftColumn, -29, pos * 10 - 15, 0);
            fontrenderer.drawString(line.rightColumn, 11, pos * 10 - 15, 0);
         }
      }

      GlStateManager.depthMask(true);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
      if (destroyStage >= 0) {
         GlStateManager.matrixMode(5890);
         GlStateManager.popMatrix();
         GlStateManager.matrixMode(5888);
      }
   }

   private void renderItem2d(ItemStack itemStack, float scale) {
      if (!itemStack.isEmpty()) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(scale / 32.0F, scale / 32.0F, -1.0E-4F);
         GlStateManager.translate(-8.0F, -11.0F, -420.0F);
         RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
         renderItem.renderItemAndEffectIntoGUI(itemStack, 0, 0);
         GlStateManager.popMatrix();
      }
   }

   private void renderItem3d(ItemStack itemstack) {
      if (!itemstack.isEmpty()) {
         GlStateManager.pushMatrix();
         GlStateManager.disableLighting();
         if (itemstack.getItem() instanceof ItemBlock) {
            GlStateManager.scale(0.25, 0.25, 0.25);
         } else {
            GlStateManager.scale(-0.15, -0.15, 0.15);
         }

         GlStateManager.pushAttrib();
         RenderHelper.enableStandardItemLighting();
         Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, TransformType.FIXED);
         RenderHelper.disableStandardItemLighting();
         GlStateManager.popAttrib();
         GlStateManager.enableLighting();
         GlStateManager.popMatrix();
      }
   }
}
