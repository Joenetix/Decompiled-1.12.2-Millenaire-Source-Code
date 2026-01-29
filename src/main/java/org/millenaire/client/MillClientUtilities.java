package org.millenaire.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.input.Keyboard;
import org.millenaire.client.book.TextBook;
import org.millenaire.client.book.TextLine;
import org.millenaire.client.book.TextPage;
import org.millenaire.client.forge.ClientProxy;
import org.millenaire.client.gui.DisplayActions;
import org.millenaire.client.gui.text.GuiPanelParchment;
import org.millenaire.client.gui.text.GuiText;
import org.millenaire.client.network.ClientSender;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.entity.TileEntityPanel;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.item.TradeGood;
import org.millenaire.common.quest.QuestInstance;
import org.millenaire.common.utilities.DevModUtilities;
import org.millenaire.common.utilities.LanguageUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.VillageUtilities;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.buildingmanagers.PanelContentGenerator;
import org.millenaire.common.world.UserProfile;

public class MillClientUtilities {
   private static final int VILLAGE_RADIUS_WARNING_LEVEL = 120;
   private static long lastPing = 0L;
   private static long lastFreeRes = 0L;

   public static void displayChunkPanel(World world, EntityPlayer player) {
      List<List<String>> pages = new ArrayList<>();
      List<String> page = new ArrayList<>();
      page.add(LanguageUtilities.string("chunk.chunkmap"));
      pages.add(page);
      page = new ArrayList<>();
      page.add(LanguageUtilities.string("chunk.caption"));
      page.add(LanguageUtilities.string(""));
      page.add(LanguageUtilities.string("chunk.captiongeneral"));
      page.add(LanguageUtilities.string("chunk.captiongreen"));
      page.add(LanguageUtilities.string("chunk.captionblue"));
      page.add(LanguageUtilities.string("chunk.captionpurple"));
      page.add(LanguageUtilities.string("chunk.captionwhite"));
      page.add(LanguageUtilities.string(""));
      page.add(LanguageUtilities.string("chunk.playerposition", (int)player.posX + "/" + (int)player.posZ));
      page.add(LanguageUtilities.string(""));
      page.add(LanguageUtilities.string("chunk.settings", "" + MillConfigValues.KeepActiveRadius, "" + ForgeChunkManager.getMaxTicketLengthFor("millenaire")));
      page.add(LanguageUtilities.string(""));
      page.add(LanguageUtilities.string("chunk.explanations"));
      pages.add(page);
      TextBook book = TextBook.convertStringsToBook(pages);
      Minecraft.getMinecraft().displayGuiScreen(new GuiPanelParchment(player, book, null, 2, true));
   }

   public static void displayInfoPanel(World world, EntityPlayer player) {
      TextBook book = new TextBook();
      TextPage page = new TextPage();
      page.addLine(new TextLine(new GuiText.MillGuiButton(LanguageUtilities.string("ui.helpbutton"), 2000, new ItemStack(Items.SIGN, 1, 0))));
      page.addLine(new TextLine(new GuiText.MillGuiButton(LanguageUtilities.string("ui.travelbookbutton"), 5000, new ItemStack(Items.WRITABLE_BOOK, 1, 0))));
      page.addLine(new TextLine(new GuiText.MillGuiButton(LanguageUtilities.string("ui.configbutton"), 4000, new ItemStack(Items.REDSTONE, 1, 0))));
      if (!Mill.serverWorlds.isEmpty()) {
         page.addLine(new TextLine(new GuiText.MillGuiButton(LanguageUtilities.string("ui.chunkbutton"), 3000, new ItemStack(Items.MAP, 1, 0))));
      }

      page.addLine(LanguageUtilities.string("info.culturetitle"), "§1");
      page.addBlankLine();

      for (Culture culture : Culture.ListCultures) {
         page.addLine(LanguageUtilities.string("info.culture", culture.getAdjectiveTranslated()));
         String colour = "";
         if (culture.getLocalPlayerReputation() > 0) {
            colour = "§2";
         } else if (culture.getLocalPlayerReputation() < 0) {
            colour = "§4";
         }

         page.addLine(LanguageUtilities.string("info.culturereputation", culture.getLocalPlayerReputationString()), colour);
         if (MillConfigValues.languageLearning) {
            page.addLine(LanguageUtilities.string("info.culturelanguage", culture.getLanguageLevelString()));
         }

         page.addBlankLine();
      }

      book.addPage(page);
      page = new TextPage();
      page.addLine(LanguageUtilities.string("quest.creationqueststatus"), "§1");
      page.addBlankLine();

      for (String s : Mill.proxy.getClientProfile().getWorldQuestStatus()) {
         page.addLine(s);
      }

      page.addBlankLine();
      page.addLine(LanguageUtilities.string("quest.questlist"));
      page.addBlankLine();
      boolean questShown = false;
      UserProfile profile = Mill.proxy.getClientProfile();

      for (QuestInstance qi : profile.questInstances) {
         String s = qi.getListing(profile);
         if (s != null) {
            questShown = true;
            page.addLine(s);
            long timeLeft = qi.currentStepStart + qi.getCurrentStep().duration * 1000 - world.getWorldTime();
            timeLeft = Math.round((float)(timeLeft / 1000L));
            if (timeLeft == 0L) {
               page.addLine(LanguageUtilities.string("quest.lessthananhourleft"), "§4");
            } else {
               page.addLine(LanguageUtilities.string("quest.timeremaining") + ": " + timeLeft + " " + LanguageUtilities.string("quest.hours"));
            }
         }
      }

      if (!questShown) {
         page.addLine(LanguageUtilities.string("quest.noquestsvisible"));
      }

      book.addPage(page);
      Minecraft.getMinecraft().displayGuiScreen(new GuiPanelParchment(player, book, null, 0, true));
   }

   public static void displayPanel(World world, EntityPlayer player, Point p) {
      TileEntityPanel panel = p.getPanel(world);
      if (panel != null && panel.buildingPos != null) {
         Building building = Mill.clientWorld.getBuilding(panel.buildingPos);
         if (building != null) {
            TextBook book = panel.getFullText(player);
            if (book != null) {
               DisplayActions.displayParchmentPanelGUI(player, book, building, panel.getMapType(), false);
            }
         }
      }
   }

   public static void displayStartupText(boolean error) {
      if (error) {
         Mill.proxy.sendChatAdmin(LanguageUtilities.string("startup.loadproblem", "Millénaire 8.1.2"));
         Mill.proxy.sendChatAdmin(LanguageUtilities.string("startup.checkload"));
         MillLog.error(null, "There was an error when trying to load Millénaire 8.1.2.");
      } else {
         if (MillConfigValues.displayStart) {
            String bonus = "";
            if (MillConfigValues.bonusEnabled) {
               bonus = " " + LanguageUtilities.string("startup.bonus");
            }

            Mill.proxy
               .sendChatAdmin(
                  LanguageUtilities.string("startup.millenaireloaded", "Millénaire 8.1.2", Keyboard.getKeyName(ClientProxy.KB_VILLAGES.getKeyCode()))
               );
            Mill.proxy.sendChatAdmin(LanguageUtilities.string("startup.bonus", "Millénaire 8.1.2", bonus), TextFormatting.BLUE);
         }

         if (MillConfigValues.DEV) {
            Mill.proxy.sendChatAdmin(LanguageUtilities.string("startup.devmode1"), TextFormatting.RED);
            Mill.proxy.sendChatAdmin(LanguageUtilities.string("startup.devmode2"), TextFormatting.RED);
         }

         if (MillConfigValues.VillageRadius > 120) {
            Mill.proxy.sendChatAdmin(LanguageUtilities.string("startup.radiuswarning", "100"));
         }
      }
   }

   public static void displayTradeHelp(Building shop, EntityPlayer player, GuiScreen callingGui) {
      List<List<TextLine>> pages = new ArrayList<>();
      List<TextLine> page = new ArrayList<>();
      page.add(new TextLine("<darkblue>" + LanguageUtilities.string("tradehelp.title", shop.getNativeBuildingName())));
      page.add(new TextLine(""));
      page.add(new TextLine("<darkblue>" + LanguageUtilities.string("tradehelp.goodssold")));
      page.add(new TextLine(""));
      List<TradeGood> tradeGood = shop.calculateSellingGoods(null);
      if (tradeGood != null) {
         String lastDesc = null;
         List<ItemStack> stacks = new ArrayList<>();
         List<Integer> prices = new ArrayList<>();

         for (TradeGood g : tradeGood) {
            if (lastDesc != null && !lastDesc.equals(g.travelBookCategory)) {
               List<String> vprices = new ArrayList<>();

               for (int p : prices) {
                  vprices.add(LanguageUtilities.string("tradehelp.sellingprice") + " " + MillCommonUtilities.getShortPrice(p));
               }

               page.add(new TextLine(stacks, vprices, LanguageUtilities.string(lastDesc), 72));
               page.add(new TextLine());
               stacks = new ArrayList<>();
               prices = new ArrayList<>();
            }

            stacks.add(new ItemStack(g.item.item, 1, g.item.meta));
            prices.add(g.getBasicSellingPrice(shop));
            if (g.travelBookCategory != null) {
               lastDesc = g.travelBookCategory;
            } else {
               lastDesc = "";
            }
         }

         if (lastDesc != null) {
            List<String> vprices = new ArrayList<>();

            for (int p : prices) {
               vprices.add(LanguageUtilities.string("tradehelp.sellingprice") + " " + MillCommonUtilities.getShortPrice(p));
            }

            page.add(new TextLine(stacks, vprices, LanguageUtilities.string(lastDesc), 72));
            page.add(new TextLine());
         }
      }

      page.add(new TextLine("<darkblue>" + LanguageUtilities.string("tradehelp.goodsbought")));
      page.add(new TextLine(""));
      tradeGood = shop.calculateBuyingGoods(null);
      if (tradeGood != null) {
         String lastDesc = null;
         List<ItemStack> stacks = new ArrayList<>();
         List<Integer> prices = new ArrayList<>();

         for (TradeGood g : tradeGood) {
            if (lastDesc != null && !lastDesc.equals(g.travelBookCategory)) {
               List<String> vprices = new ArrayList<>();

               for (int p : prices) {
                  vprices.add(LanguageUtilities.string("tradehelp.buyingprice") + " " + MillCommonUtilities.getShortPrice(p));
               }

               page.add(new TextLine(stacks, vprices, LanguageUtilities.string(lastDesc), 72));
               page.add(new TextLine());
               stacks = new ArrayList<>();
               prices = new ArrayList<>();
            }

            stacks.add(new ItemStack(g.item.item, 1, g.item.meta));
            prices.add(g.getBasicBuyingPrice(shop));
            if (g.travelBookCategory != null) {
               lastDesc = g.travelBookCategory;
            } else {
               lastDesc = "";
            }
         }

         if (lastDesc != null) {
            List<String> vprices = new ArrayList<>();

            for (int p : prices) {
               vprices.add(LanguageUtilities.string("tradehelp.buyingprice") + " " + MillCommonUtilities.getShortPrice(p));
            }

            page.add(new TextLine(stacks, vprices, LanguageUtilities.string(lastDesc), 72));
            page.add(new TextLine());
         }
      }

      pages.add(page);
      page = new ArrayList<>();
      page.add(new TextLine("<darkblue>" + LanguageUtilities.string("tradehelp.helptitle")));
      page.add(new TextLine());
      page.add(new TextLine(LanguageUtilities.string("tradehelp.helptext")));
      pages.add(page);
      TextBook book = TextBook.convertLinesToBook(pages);
      GuiText helpGui = new GuiPanelParchment(player, null, book, 0, true);
      helpGui.setCallingScreen(callingGui);
      Minecraft.getMinecraft().displayGuiScreen(helpGui);
   }

   public static void displayVillageBook(World world, EntityPlayer player, Point p) {
      Building townHall = Mill.clientWorld.getBuilding(p);
      if (townHall != null) {
         TextBook book = new TextBook();
         TextPage page = new TextPage();
         page.addLine(LanguageUtilities.string("panels.villagescroll") + ": " + townHall.getVillageQualifiedName());
         page.addLine("");
         book.addPage(page);
         TextBook newBook = PanelContentGenerator.generateSummary(townHall);
         book.addBook(newBook);
         newBook = PanelContentGenerator.generateEtatCivil(townHall);
         book.addBook(newBook);
         newBook = PanelContentGenerator.generateConstructions(townHall);
         book.addBook(newBook);
         newBook = PanelContentGenerator.generateProjects(player, townHall);
         book.addBook(newBook);
         newBook = PanelContentGenerator.generateResources(townHall);
         book.addBook(newBook);
         newBook = PanelContentGenerator.generateInnGoods(townHall);
         book.addBook(newBook);
         DisplayActions.displayParchmentPanelGUI(player, book, townHall, 1, true);
      }
   }

   public static void handleKeyPress(World world) {
      Minecraft minecraft = FMLClientHandler.instance().getClient();
      if (minecraft.currentScreen == null) {
         EntityPlayer player = minecraft.player;
         if (System.currentTimeMillis() - lastPing > 2000L) {
            try {
               if (player.dimension == 0) {
                  if (Keyboard.isKeyDown(ClientProxy.KB_VILLAGES.getKeyCode())) {
                     ClientSender.displayVillageList(false);
                     lastPing = System.currentTimeMillis();
                  }

                  if (Keyboard.isKeyDown(ClientProxy.KB_ESCORTS.getKeyCode())) {
                     boolean stance = !Keyboard.isKeyDown(42);
                     ClientSender.hireToggleStance(player, stance);
                     lastPing = System.currentTimeMillis();
                  }

                  if (Keyboard.isKeyDown(ClientProxy.KB_MENU.getKeyCode())) {
                     displayInfoPanel(world, player);
                     lastPing = System.currentTimeMillis();
                  }

                  if (MillConfigValues.DEV) {
                     if (Keyboard.isKeyDown(42) && Keyboard.isKeyDown(19) && System.currentTimeMillis() - lastFreeRes > 5000L) {
                        DevModUtilities.fillInFreeGoods(player);
                        lastFreeRes = System.currentTimeMillis();
                     }

                     if (Keyboard.isKeyDown(42) && Keyboard.isKeyDown(203)) {
                        player.setPosition(player.posX + 10000.0, player.posY + 10.0, player.posZ);
                        lastPing = System.currentTimeMillis();
                     }

                     if (Keyboard.isKeyDown(42) && Keyboard.isKeyDown(205)) {
                        player.setPosition(player.posX - 10000.0, player.posY + 10.0, player.posZ);
                        lastPing = System.currentTimeMillis();
                     }

                     if (Keyboard.isKeyDown(38)) {
                        ClientSender.displayVillageList(true);
                        lastPing = System.currentTimeMillis();
                     }

                     if (Keyboard.isKeyDown(50) && Keyboard.isKeyDown(42)) {
                        ClientSender.devCommand(1);
                        lastPing = System.currentTimeMillis();
                     }

                     if (Keyboard.isKeyDown(21) && Keyboard.isKeyDown(29)) {
                        Mill.proxy.sendChatAdmin("Sending test path request.");
                        ClientSender.devCommand(2);
                        lastPing = System.currentTimeMillis();
                     }

                     if (Keyboard.isKeyDown(20)) {
                        Mill.clientWorld.displayTagActionData(player);
                        lastPing = System.currentTimeMillis();
                     }
                  }
               }
            } catch (Exception var4) {
               MillLog.printException("Exception while handling key presses:", var4);
            }
         }
      }
   }

   public static void putVillagerSentenceInChat(MillVillager v) {
      if (v.dialogueTargetFirstName == null || v.dialogueChat) {
         int radius = 0;
         if (Mill.isDistantClient()) {
            radius = MillConfigValues.VillagersSentenceInChatDistanceClient;
         } else {
            radius = MillConfigValues.VillagersSentenceInChatDistanceSP;
         }

         EntityPlayer player = Mill.proxy.getTheSinglePlayer();
         if (!(v.getPos().distanceTo(player) > radius)) {
            String gameSpeech = VillageUtilities.getVillagerSentence(v, player.getName(), false);
            String nativeSpeech = VillageUtilities.getVillagerSentence(v, player.getName(), true);
            if (nativeSpeech != null || gameSpeech != null) {
               String s;
               if (v.dialogueTargetFirstName != null) {
                  s = LanguageUtilities.string("other.chattosomeone", v.getName(), v.dialogueTargetFirstName + " " + v.dialogueTargetLastName) + ": ";
               } else {
                  s = v.getName() + ": ";
               }

               if (nativeSpeech != null) {
                  s = s + "§9" + nativeSpeech;
               }

               if (nativeSpeech != null && gameSpeech != null) {
                  s = s + " ";
               }

               if (gameSpeech != null) {
                  s = s + "§4" + gameSpeech;
               }

               Mill.proxy.sendLocalChat(Mill.proxy.getTheSinglePlayer(), v.dialogueColour, s);
            }
         }
      }
   }
}
