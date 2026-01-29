package org.millenaire.client.gui.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import org.millenaire.client.book.BookManager;
import org.millenaire.client.book.TextBook;
import org.millenaire.client.book.TextLine;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.culture.VillagerType;
import org.millenaire.common.item.TradeGood;
import org.millenaire.common.utilities.LanguageUtilities;
import org.millenaire.common.utilities.MillLog;

public abstract class GuiText extends GuiScreen {
   private static final ResourceLocation ICONS_TEXTURE = new ResourceLocation("millenaire", "textures/gui/icons.png");
   public static final String WHITE = "<white>";
   public static final String YELLOW = "<yellow>";
   public static final String PINK = "<pink>";
   public static final String LIGHTRED = "<lightred>";
   public static final String CYAN = "<cyan>";
   public static final String LIGHTGREEN = "<lightgreen>";
   public static final String BLUE = "<blue>";
   public static final String DARKGREY = "<darkgrey>";
   public static final String LIGHTGREY = "<lightgrey>";
   public static final String ORANGE = "<orange>";
   public static final String PURPLE = "<purple>";
   public static final String DARKRED = "<darkred>";
   public static final String LIGHTBLUE = "<lightblue>";
   public static final String DARKGREEN = "<darkgreen>";
   public static final String DARKBLUE = "<darkblue>";
   public static final String BLACK = "<black>";
   private GuiScreen callingScreen = null;
   protected int pageNum = 0;
   protected TextBook textBook = null;
   protected BookManager bookManager = null;
   List<GuiText.MillGuiTextField> textFields = new ArrayList<>();
   protected final RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();

   protected void actionPerformed(GuiButton button) throws IOException {
      if (button instanceof GuiText.GuiButtonReference) {
         GuiText.GuiButtonReference refButton = (GuiText.GuiButtonReference) button;
         GuiTravelBook guiTravelBook = new GuiTravelBook(Minecraft.getMinecraft().player);
         guiTravelBook.setCallingScreen(this);
         guiTravelBook.jumpToDetails(refButton.culture, refButton.type, refButton.key, false);
         Minecraft.getMinecraft().displayGuiScreen(guiTravelBook);
      }
   }

   public void buttonPagination() {
      int elementsId = 0;

      try {
         if (this.textBook == null) {
            return;
         }

         int xStart = (this.width - this.getXSize()) / 2;
         int yStart = (this.height - this.getYSize()) / 2;
         this.buttonList.clear();
         this.textFields.clear();
         int vpos = 6;
         if (this.pageNum < this.textBook.nbPages()) {
            for (int cp = 0; cp < this.getTextHeight() && cp < this.textBook.getPage(this.pageNum).getNbLines(); cp++) {
               TextLine line = this.textBook.getPage(this.pageNum).getLine(cp);
               int totalButtonWidth = this.getLineSizeInPx() - 20;
               if (line.buttons != null) {
                  if (line.buttons.length == 1) {
                     if (line.buttons[0] != null) {
                        line.buttons[0].x = xStart + this.getXSize() / 2 - totalButtonWidth / 2;
                        line.buttons[0].setWidth(totalButtonWidth);
                     }
                  } else if (line.buttons.length == 2) {
                     int buttonWidth = totalButtonWidth / 2 - 5;
                     if (line.buttons[0] != null) {
                        line.buttons[0].x = xStart + this.getXSize() / 2 - totalButtonWidth / 2;
                        line.buttons[0].setWidth(buttonWidth);
                     }

                     if (line.buttons[1] != null) {
                        line.buttons[1].x = xStart + this.getXSize() / 2 + 5;
                        line.buttons[1].setWidth(buttonWidth);
                     }
                  } else if (line.buttons.length == 3) {
                     int buttonWidthx = totalButtonWidth / 3 - 10;
                     if (line.buttons[0] != null) {
                        line.buttons[0].x = xStart + this.getXSize() / 2 - totalButtonWidth / 2;
                        line.buttons[0].setWidth(buttonWidthx);
                     }

                     if (line.buttons[1] != null) {
                        line.buttons[1].x = xStart + this.getXSize() / 2 - totalButtonWidth / 2 + buttonWidthx + 10;
                        line.buttons[1].setWidth(buttonWidthx);
                     }

                     if (line.buttons[2] != null) {
                        line.buttons[2].x = xStart + this.getXSize() / 2 - totalButtonWidth / 2 + buttonWidthx * 2 + 20;
                        line.buttons[2].setWidth(buttonWidthx);
                     }
                  }

                  for (int i = 0; i < line.buttons.length; i++) {
                     if (line.buttons[i] != null) {
                        line.buttons[i].y = yStart + vpos;
                        line.buttons[i].setHeight(20);
                        this.buttonList.add(line.buttons[i]);
                     }
                  }
               } else if (line.referenceButton != null) {
                  line.referenceButton.setWidth(20);
                  line.referenceButton.setHeight(20);
                  line.referenceButton.y = yStart + vpos;
                  line.referenceButton.x = xStart + 6 + line.getLineMarginLeft();
                  this.buttonList.add(line.referenceButton);
               } else if (line.textField != null) {
                  GuiText.MillGuiTextField textField = new GuiText.MillGuiTextField(
                        elementsId++, this.fontRenderer, xStart + this.getXSize() / 2 + 40, yStart + vpos, 95, 20,
                        line.textField.fieldKey);
                  textField.setText(line.textField.getText());
                  textField.setMaxStringLength(line.textField.getMaxStringLength());
                  textField.setTextColor(-1);
                  line.textField = textField;
                  line.textField.setTextColor(-1);
                  line.textField.setEnableBackgroundDrawing(false);
                  this.textFields.add(textField);
               }

               if (line.columns != null) {
                  int lineSize = this.getLineSizeInPx() - line.getTextMarginLeft() - line.getLineMarginLeft()
                        - line.getLineMarginRight();
                  int colSize = (lineSize - (line.columns.length - 1) * 10) / line.columns.length;

                  for (int col = 0; col < line.columns.length; col++) {
                     TextLine column = line.columns[col];
                     int colXStart = col * (colSize + 10) + line.getLineMarginLeft();
                     if (column.referenceButton != null) {
                        column.referenceButton.setWidth(20);
                        column.referenceButton.setHeight(20);
                        column.referenceButton.y = yStart + vpos;
                        column.referenceButton.x = xStart + colXStart + 6 + column.getLineMarginLeft();
                        this.buttonList.add(column.referenceButton);
                     }
                  }
               }

               vpos += line.getLineHeight();
            }
         }
      } catch (Exception var13) {
         MillLog.printException("Exception while doing button pagination in GUI " + this, var13);
      }
   }

   protected void closeGui() {
      if (this.callingScreen != null) {
         Minecraft.getMinecraft().displayGuiScreen(this.callingScreen);
      } else {
         this.mc.displayGuiScreen(null);
         this.mc.setIngameFocus();
      }
   }

   protected void closeWindow() {
      this.mc.displayGuiScreen(null);
      this.mc.setIngameFocus();
   }

   protected abstract void customDrawBackground(int var1, int var2, float var3);

   protected abstract void customDrawScreen(int var1, int var2, float var3);

   public void decrementPage() {
      if (this.textBook != null) {
         if (this.pageNum > 0) {
            this.pageNum--;
         }

         this.buttonPagination();
      }
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   protected void drawHoveringText(List par1List, int par2, int par3, FontRenderer font) {
      if (!par1List.isEmpty()) {
         GL11.glDisable(32826);
         RenderHelper.disableStandardItemLighting();
         GL11.glDisable(2896);
         GL11.glDisable(2929);
         int k = 0;

         for (Object o : par1List) {
            String s = (String) o;
            int l = font.getStringWidth(s);
            if (l > k) {
               k = l;
            }
         }

         int i1 = par2 + 12;
         int j1 = par3 - 12;
         int k1 = 8;
         if (par1List.size() > 1) {
            k1 += 2 + (par1List.size() - 1) * 10;
         }

         if (i1 + k > this.width) {
            i1 -= 28 + k;
         }

         if (j1 + k1 + 6 > this.height) {
            j1 = this.height - k1 - 6;
         }

         this.zLevel = 300.0F;
         this.itemRenderer.zLevel = 300.0F;
         int l1 = -267386864;
         this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, -267386864, -267386864);
         this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, -267386864, -267386864);
         this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, -267386864, -267386864);
         this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, -267386864, -267386864);
         this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, -267386864, -267386864);
         int i2 = 1347420415;
         int j2 = 1344798847;
         this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, 1347420415, 1344798847);
         this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, 1347420415, 1344798847);
         this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, 1347420415, 1347420415);
         this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, 1344798847, 1344798847);

         for (int k2 = 0; k2 < par1List.size(); k2++) {
            String s1 = (String) par1List.get(k2);
            font.drawStringWithShadow(s1, i1, j1, -1);
            if (k2 == 0) {
               j1 += 2;
            }

            j1 += 10;
         }

         this.zLevel = 0.0F;
         this.itemRenderer.zLevel = 0.0F;
         GL11.glEnable(2929);
         GL11.glEnable(32826);
      }
   }

   protected void drawItemStackTooltip(ItemStack par1ItemStack, int xPos, int yPos, boolean displayItemLegend,
         String extraLegend) {
      List<String> list;
      if (displayItemLegend) {
         list = par1ItemStack.getTooltip(
               this.mc.player, this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL);

         for (int k = 0; k < list.size(); k++) {
            if (k == 0) {
               list.set(k, par1ItemStack.getRarity().color + list.get(k));
            } else {
               list.set(k, TextFormatting.GRAY + list.get(k));
            }
         }
      } else {
         list = new ArrayList<>();
      }

      if (extraLegend != null) {
         list.addAll(
               BookManager.splitStringByLength(new GuiText.FontRendererWrapped(this.fontRenderer), extraLegend, 150));
      }

      if (!list.isEmpty()) {
         FontRenderer font = par1ItemStack.getItem().getFontRenderer(par1ItemStack);
         this.drawHoveringText(list, xPos, yPos, font == null ? this.fontRenderer : font);
      }
   }

   public void drawScreen(int mouseX, int mouseY, float f) {
      try {
         if (this.textBook == null) {
            this.initData();
         }

         boolean hasSpecialIcon = false;
         ItemStack hoverIcon = null;
         String extraLegend = null;
         boolean displayItemLegend = true;
         GuiText.GuiButtonReference hoverReferenceButton = null;
         this.drawDefaultBackground();
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.getTextureManager().bindTexture(this.getPNGPath());
         int xStart = (this.width - this.getXSize()) / 2;
         int yStart = (this.height - this.getYSize()) / 2;
         this.drawTexturedModalRect(xStart, yStart, 0, 0, this.getXSize(), this.getYSize());
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.customDrawBackground(mouseX, mouseY, f);
         GL11.glPushMatrix();
         GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
         RenderHelper.enableStandardItemLighting();
         GL11.glPopMatrix();
         GL11.glPushMatrix();
         GL11.glTranslatef(xStart, yStart, 0.0F);
         RenderHelper.disableStandardItemLighting();
         GL11.glDisable(2896);
         GL11.glDisable(2929);
         if (this.textBook != null) {
            int vpos = 6;
            if (this.pageNum < this.textBook.nbPages()) {
               if (this.textBook.getPage(this.pageNum) == null) {
                  MillLog.printException(new MillLog.MillenaireException(
                        "descText.get(pageNum)==null for pageNum: " + this.pageNum + " in GUI: " + this));
               }

               for (int linePos = 0; linePos < this.getTextHeight()
                     && linePos < this.textBook.getPage(this.pageNum).getNbLines(); linePos++) {
                  TextLine line = this.textBook.getPage(this.pageNum).getLine(linePos);
                  int textXstart = this.getTextXStart() + line.getTextMarginLeft() + line.getLineMarginLeft();
                  if (line.shadow) {
                     this.fontRenderer.drawStringWithShadow(line.style + line.text, textXstart,
                           vpos + line.getTextMarginTop(), 1052688);
                  } else {
                     this.fontRenderer.drawString(line.style + line.text, textXstart, vpos + line.getTextMarginTop(),
                           1052688);
                  }

                  if (line.columns != null) {
                     int lineSize = this.getLineSizeInPx() - line.getTextMarginLeft() - line.getLineMarginLeft()
                           - line.getLineMarginRight();
                     int colSize = (lineSize - (line.columns.length - 1) * 10) / line.columns.length;

                     for (int col = 0; col < line.columns.length; col++) {
                        TextLine column = line.columns[col];
                        int colXStart = this.getTextXStart() + col * (colSize + 10) + line.getLineMarginLeft();
                        textXstart = colXStart + column.getTextMarginLeft();
                        if (column.shadow) {
                           this.fontRenderer.drawStringWithShadow(column.style + column.text, textXstart,
                                 vpos + column.getTextMarginTop(), 1052688);
                        } else {
                           this.fontRenderer.drawString(column.style + column.text, textXstart,
                                 vpos + column.getTextMarginTop(), 1052688);
                        }
                     }
                  }

                  vpos += line.getLineHeight();
               }
            }

            this.fontRenderer.drawString(this.pageNum + 1 + "/" + this.getNbPage(), this.getXSize() / 2 - 10,
                  this.getYSize() - 10, 1052688);
            vpos = 6;
            this.zLevel = 100.0F;
            this.itemRenderer.zLevel = 100.0F;
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableColorMaterial();
            GlStateManager.enableLighting();
            this.itemRender.zLevel = 100.0F;
            if (this.pageNum < this.textBook.nbPages()) {
               for (int linePos = 0; linePos < this.getTextHeight()
                     && linePos < this.textBook.getPage(this.pageNum).getNbLines(); linePos++) {
                  TextLine linex = this.textBook.getPage(this.pageNum).getLine(linePos);
                  if (linex.icons != null) {
                     for (int ic = 0; ic < linex.icons.size(); ic++) {
                        ItemStack icon = linex.icons.get(ic);
                        int xPosition = this.getTextXStart() + 18 * ic + linex.getLineMarginLeft();
                        if (icon != null) {
                           if (linex.iconExtraLegends == null) {
                              MillLog.error(null, "Null legends!");
                           }

                           GL11.glEnable(2929);
                           this.itemRenderer.renderItemAndEffectIntoGUI(icon, xPosition, vpos);
                        }

                        if (xStart + xPosition < mouseX && yStart + vpos < mouseY && xStart + xPosition + 16 > mouseX
                              && yStart + vpos + 16 > mouseY) {
                           String legend = linex.iconExtraLegends.get(ic);
                           hoverIcon = icon;
                           extraLegend = legend;
                           displayItemLegend = linex.displayItemLegend();
                        }
                     }
                  }

                  if (linex.columns != null) {
                     int lineSize = this.getLineSizeInPx() - linex.getTextMarginLeft() - linex.getLineMarginLeft()
                           - linex.getLineMarginRight();
                     int colSize = (lineSize - (linex.columns.length - 1) * 10) / linex.columns.length;

                     for (int colx = 0; colx < linex.columns.length; colx++) {
                        TextLine column = linex.columns[colx];
                        int colXStart = this.getTextXStart() + colx * (colSize + 10) + linex.getLineMarginLeft();
                        if (column.icons != null) {
                           for (int ic = 0; ic < column.icons.size(); ic++) {
                              ItemStack iconx = column.icons.get(ic);
                              int iconXpos = colXStart + 18 * ic;
                              if (iconx != null) {
                                 if (column.iconExtraLegends == null) {
                                    MillLog.error(null, "Null legends!");
                                 }

                                 GL11.glEnable(2929);
                                 this.itemRenderer.renderItemAndEffectIntoGUI(iconx, iconXpos, vpos);
                              }

                              if (xStart + iconXpos < mouseX && yStart + vpos < mouseY
                                    && xStart + iconXpos + 16 > mouseX && yStart + vpos + 16 > mouseY) {
                                 String legend = column.iconExtraLegends.get(ic);
                                 hoverIcon = iconx;
                                 extraLegend = legend;
                                 displayItemLegend = column.displayItemLegend();
                              }
                           }
                        }
                     }
                  }

                  vpos += linex.getLineHeight();
               }
            }

            for (GuiButton button : this.buttonList) {
               if (button instanceof GuiText.MillGuiButton) {
                  GuiText.MillGuiButton millButton = (GuiText.MillGuiButton) button;
                  if (millButton.itemStackIconLeft != null) {
                     GL11.glEnable(2929);
                     this.itemRenderer
                           .renderItemAndEffectIntoGUI(millButton.itemStackIconLeft, millButton.x + 4 - xStart,
                                 millButton.y + 2 - yStart);
                  }

                  if (millButton.itemStackIconRight != null) {
                     GL11.glEnable(2929);
                     this.itemRenderer
                           .renderItemAndEffectIntoGUI(
                                 millButton.itemStackIconRight,
                                 millButton.x + millButton.width - 4 - 16 - xStart,
                                 millButton.y + 2 - yStart);
                  }

                  if (millButton.specialIconLeft != null || millButton.specialIconRight != null) {
                     hasSpecialIcon = true;
                  }
               } else if (button instanceof GuiText.GuiButtonReference) {
                  GuiText.GuiButtonReference refButton = (GuiText.GuiButtonReference) button;
                  if (refButton.getIcon() != null) {
                     GL11.glEnable(2929);
                     this.itemRenderer.renderItemAndEffectIntoGUI(refButton.getIcon(), refButton.x + 2 - xStart,
                           refButton.y + 2 - yStart);
                  }

                  if (refButton.x < mouseX
                        && refButton.y < mouseY
                        && refButton.x + refButton.width > mouseX
                        && refButton.y + refButton.height > mouseY) {
                     hoverReferenceButton = refButton;
                  }
               }
            }

            GlStateManager.disableLighting();
            this.customDrawScreen(mouseX, mouseY, f);
         }

         GL11.glPopMatrix();
         super.drawScreen(mouseX, mouseY, f);
         if (hasSpecialIcon) {
            this.mc.getTextureManager().bindTexture(ICONS_TEXTURE);

            for (GuiButton buttonx : this.buttonList) {
               if (buttonx instanceof GuiText.MillGuiButton) {
                  GuiText.MillGuiButton millButtonx = (GuiText.MillGuiButton) buttonx;
                  if (millButtonx.specialIconLeft != null) {
                     this.drawTexturedModalRect(
                           millButtonx.x + 4,
                           millButtonx.y + 2,
                           millButtonx.specialIconLeft.xpos,
                           millButtonx.specialIconLeft.ypos,
                           16,
                           16);
                  }
               }
            }
         }

         GL11.glEnable(2896);
         GL11.glEnable(2929);
         GL11.glDisable(2896);

         for (GuiText.MillGuiTextField textField : this.textFields) {
            textField.drawTextBox();
         }

         if (hoverIcon != null) {
            this.drawItemStackTooltip(hoverIcon, mouseX, mouseY, displayItemLegend, extraLegend);
         }

         if (hoverReferenceButton != null) {
            if (this instanceof GuiTravelBook) {
               this.drawHoveringText(
                     BookManager.splitStringByLength(new GuiText.FontRendererWrapped(this.fontRenderer),
                           hoverReferenceButton.getIconName(), 150),
                     hoverReferenceButton.x + 15,
                     hoverReferenceButton.y,
                     this.fontRenderer);
            } else {
               this.drawHoveringText(
                     BookManager.splitStringByLength(new GuiText.FontRendererWrapped(this.fontRenderer),
                           hoverReferenceButton.getIconFullLegend(), 150),
                     hoverReferenceButton.x + 15,
                     hoverReferenceButton.y,
                     this.fontRenderer);
            }
         }

         this.itemRenderer.zLevel = 0.0F;
         this.zLevel = 0.0F;
      } catch (Exception var23) {
         MillLog.printException("Exception in drawScreen of GUI: " + this, var23);
      }
   }

   public GuiScreen getCallingScreen() {
      return this.callingScreen;
   }

   private final int getLineSizeInPx() {
      return this.bookManager.getLineSizeInPx();
   }

   protected int getNbPage() {
      return this.textBook.nbPages();
   }

   public abstract ResourceLocation getPNGPath();

   public final int getTextHeight() {
      return this.bookManager.getTextHeight();
   }

   public final int getTextXStart() {
      return this.bookManager.getTextXStart();
   }

   public final int getXSize() {
      return this.bookManager.getXSize();
   }

   public final int getYSize() {
      return this.bookManager.getYSize();
   }

   protected void handleTextFieldPress(GuiText.MillGuiTextField textField) {
   }

   public void incrementPage() {
      if (this.textBook != null) {
         if (this.pageNum < this.getNbPage() - 1) {
            this.pageNum++;
         }

         this.buttonPagination();
      }
   }

   public abstract void initData();

   public void initGui() {
      super.initGui();
      this.initData();
      this.buttonPagination();
   }

   protected void keyTyped(char c, int i) {
      boolean keyTyped = false;

      for (GuiText.MillGuiTextField textField : this.textFields) {
         if (textField.textboxKeyTyped(c, i)) {
            keyTyped = true;
            this.handleTextFieldPress(textField);
         }
      }

      if (!keyTyped && i == 1) {
         this.closeGui();
      }
   }

   protected void mouseClicked(int i, int j, int k) throws IOException {
      int xStart = (this.width - this.getXSize()) / 2;
      int yStart = (this.height - this.getYSize()) / 2;
      int ai = i - xStart;
      int aj = j - yStart;
      if (aj > this.getYSize() - 14 && aj < this.getYSize()) {
         if (ai > 0 && ai < 33) {
            this.decrementPage();
         } else if (ai > this.getXSize() - 33 && ai < this.getXSize()) {
            this.incrementPage();
         }
      }

      for (GuiText.MillGuiTextField textField : this.textFields) {
         textField.mouseClicked(i, j, k);
      }

      super.mouseClicked(i, j, k);
   }

   public void onGuiClosed() {
      super.onGuiClosed();
   }

   public void setCallingScreen(GuiScreen callingScreen) {
      this.callingScreen = callingScreen;
   }

   public static class FontRendererGUIWrapper implements BookManager.IFontRendererWrapper {
      private final GuiText gui;

      public FontRendererGUIWrapper(GuiText gui) {
         this.gui = gui;
      }

      @Override
      public int getStringWidth(String text) {
         return this.gui.fontRenderer.getStringWidth(text);
      }

      @Override
      public boolean isAvailable() {
         return this.gui.fontRenderer != null;
      }
   }

   public static class FontRendererWrapped implements BookManager.IFontRendererWrapper {
      private final FontRenderer fontRenderer;

      public FontRendererWrapped(FontRenderer fontRenderer) {
         this.fontRenderer = fontRenderer;
      }

      @Override
      public int getStringWidth(String text) {
         return this.fontRenderer.getStringWidth(text);
      }

      @Override
      public boolean isAvailable() {
         return this.fontRenderer != null;
      }
   }

   public static class GuiButtonReference extends GuiButton {
      public Culture culture;
      public GuiText.GuiButtonReference.RefType type;
      public String key;

      public GuiButtonReference(BuildingPlanSet planSet) {
         super(0, 0, 0, 0, 0, "");
         if (planSet == null) {
            MillLog.printException(new Exception("Tried creating a ref button to a null planSet."));
         } else {
            this.culture = planSet.culture;
         }

         this.type = GuiText.GuiButtonReference.RefType.BUILDING_DETAIL;
         this.key = planSet.key;
      }

      public GuiButtonReference(Culture culture) {
         super(0, 0, 0, 0, 0, "");
         this.culture = culture;
         this.type = GuiText.GuiButtonReference.RefType.CULTURE;
         this.key = null;
      }

      public GuiButtonReference(Culture culture, GuiText.GuiButtonReference.RefType type, String key) {
         super(0, 0, 0, 0, 0, "");
         this.culture = culture;
         this.type = type;
         this.key = key;
      }

      public GuiButtonReference(TradeGood tradeGood) {
         super(0, 0, 0, 0, 0, "");
         this.culture = tradeGood.culture;
         this.type = GuiText.GuiButtonReference.RefType.TRADE_GOOD_DETAIL;
         this.key = tradeGood.key;
      }

      public GuiButtonReference(VillagerType villagerType) {
         super(0, 0, 0, 0, 0, "");
         this.culture = villagerType.culture;
         this.type = GuiText.GuiButtonReference.RefType.VILLAGER_DETAIL;
         this.key = villagerType.key;
      }

      public GuiButtonReference(VillageType villageType) {
         super(0, 0, 0, 0, 0, "");
         if (villageType.lonebuilding) {
            this.culture = villageType.culture;
            this.type = GuiText.GuiButtonReference.RefType.BUILDING_DETAIL;
            this.key = villageType.centreBuilding.key;
         } else {
            this.culture = villageType.culture;
            this.type = GuiText.GuiButtonReference.RefType.VILLAGE_DETAIL;
            this.key = villageType.key;
         }
      }

      public ItemStack getIcon() {
         if (this.type == GuiText.GuiButtonReference.RefType.BUILDING_DETAIL) {
            return this.culture.getBuildingPlanSet(this.key).getIcon();
         } else if (this.type == GuiText.GuiButtonReference.RefType.VILLAGER_DETAIL) {
            return this.culture.getVillagerType(this.key).getIcon();
         } else if (this.type == GuiText.GuiButtonReference.RefType.VILLAGE_DETAIL) {
            return this.culture.getVillageType(this.key).getIcon();
         } else if (this.type == GuiText.GuiButtonReference.RefType.TRADE_GOOD_DETAIL) {
            return this.culture.getTradeGood(this.key).getIcon();
         } else {
            return this.type == GuiText.GuiButtonReference.RefType.CULTURE ? this.culture.getIcon() : null;
         }
      }

      public String getIconFullLegend() {
         return LanguageUtilities.string("travelbook.reference_button", this.getIconName());
      }

      public String getIconFullLegendExport() {
         return LanguageUtilities.string("travelbook.reference_button_export", this.getIconNameTranslated());
      }

      public String getIconName() {
         if (this.type == GuiText.GuiButtonReference.RefType.BUILDING_DETAIL) {
            return this.culture.getBuildingPlanSet(this.key).getNameNative();
         } else if (this.type == GuiText.GuiButtonReference.RefType.VILLAGER_DETAIL) {
            return this.culture.getVillagerType(this.key).name;
         } else if (this.type == GuiText.GuiButtonReference.RefType.VILLAGE_DETAIL) {
            return this.culture.getVillageType(this.key).name;
         } else if (this.type == GuiText.GuiButtonReference.RefType.TRADE_GOOD_DETAIL) {
            return this.culture.getTradeGood(this.key).getName();
         } else {
            return this.type == GuiText.GuiButtonReference.RefType.CULTURE ? this.culture.getAdjectiveTranslated()
                  : null;
         }
      }

      public String getIconNameTranslated() {
         if (this.type == GuiText.GuiButtonReference.RefType.BUILDING_DETAIL) {
            return this.culture.getBuildingPlanSet(this.key).getNameNativeAndTranslated();
         } else if (this.type == GuiText.GuiButtonReference.RefType.VILLAGER_DETAIL) {
            return this.culture.getVillagerType(this.key).getNameNativeAndTranslated();
         } else if (this.type == GuiText.GuiButtonReference.RefType.VILLAGE_DETAIL) {
            return this.culture.getVillageType(this.key).getNameNativeAndTranslated();
         } else if (this.type == GuiText.GuiButtonReference.RefType.TRADE_GOOD_DETAIL) {
            return this.culture.getTradeGood(this.key).getName();
         } else {
            return this.type == GuiText.GuiButtonReference.RefType.CULTURE ? this.culture.getAdjectiveTranslated()
                  : null;
         }
      }

      public void setHeight(int h) {
         this.height = h;
      }

      public static enum RefType {
         BUILDING_DETAIL,
         VILLAGER_DETAIL,
         VILLAGE_DETAIL,
         TRADE_GOOD_DETAIL,
         CULTURE;
      }
   }

   public static class MillGuiButton extends GuiButton {
      public static final int HELPBUTTON = 2000;
      public static final int CHUNKBUTTON = 3000;
      public static final int CONFIGBUTTON = 4000;
      public static final int TRAVELBOOKBUTTON = 5000;
      public ItemStack itemStackIconLeft = null;
      public GuiText.SpecialIcon specialIconLeft = null;
      public ItemStack itemStackIconRight = null;
      public GuiText.SpecialIcon specialIconRight = null;

      public MillGuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String label) {
         super(buttonId, x, y, widthIn, heightIn, label);
      }

      public MillGuiButton(String label, int id) {
         super(id, 0, 0, 0, 0, label);
      }

      public MillGuiButton(String label, int id, ItemStack icon) {
         super(id, 0, 0, 0, 0, label);
         this.itemStackIconLeft = icon;
      }

      public MillGuiButton(String label, int id, ItemStack iconLeft, ItemStack iconRight) {
         super(id, 0, 0, 0, 0, label);
         this.itemStackIconLeft = iconLeft;
         this.itemStackIconRight = iconRight;
      }

      public MillGuiButton(String label, int id, GuiText.SpecialIcon icon) {
         super(id, 0, 0, 0, 0, label);
         this.specialIconLeft = icon;
      }

      public int getHeight() {
         return this.height;
      }

      public int getWidth() {
         return this.width;
      }

      public void setHeight(int h) {
         this.height = h;
      }
   }

   public static class MillGuiTextField extends GuiTextField {
      public final String fieldKey;

      public MillGuiTextField(int id, FontRenderer par1FontRenderer, int x, int y, int par5Width, int par6Height,
            String fieldKey) {
         super(id, par1FontRenderer, x, y, par5Width, par6Height);
         this.fieldKey = fieldKey;
      }
   }

   public static enum SpecialIcon {
      PLUS(0, 0),
      MINUS(16, 0);

      public int xpos;
      public int ypos;

      private SpecialIcon(int xpos, int ypos) {
         this.xpos = xpos;
         this.ypos = ypos;
      }
   }
}
