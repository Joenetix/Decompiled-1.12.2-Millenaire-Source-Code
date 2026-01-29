package org.millenaire.client.forge;

import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.millenaire.client.MillClientUtilities;
import org.millenaire.client.network.ClientReceiver;
import org.millenaire.client.network.ClientSender;
import org.millenaire.client.render.RenderMillVillager;
import org.millenaire.client.render.RenderWallDecoration;
import org.millenaire.client.render.TESRFirePit;
import org.millenaire.client.render.TESRMockBanner;
import org.millenaire.client.render.TESRPanel;
import org.millenaire.client.render.TileEntityLockedChestRenderer;
import org.millenaire.client.render.TileEntityMillBedRenderer;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.entity.EntityWallDecoration;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.entity.TileEntityFirePit;
import org.millenaire.common.entity.TileEntityLockedChest;
import org.millenaire.common.entity.TileEntityMillBed;
import org.millenaire.common.entity.TileEntityMockBanner;
import org.millenaire.common.entity.TileEntityPanel;
import org.millenaire.common.forge.CommonProxy;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.item.MillItems;
import org.millenaire.common.utilities.LanguageUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.world.UserProfile;

@EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
   public static KeyBinding KB_MENU;
   public static KeyBinding KB_VILLAGES;
   public static KeyBinding KB_ESCORTS;

   @SubscribeEvent
   public static void registerModels(ModelRegistryEvent event) {
      MillItems.registerItemModels();
      MillBlocks.registerItemBlockModels();
      RenderingRegistry.registerEntityRenderingHandler(MillVillager.EntityGenericMale.class, RenderMillVillager.FACTORY_MALE);
      RenderingRegistry.registerEntityRenderingHandler(MillVillager.EntityGenericAsymmFemale.class, RenderMillVillager.FACTORY_FEMALE_ASYM);
      RenderingRegistry.registerEntityRenderingHandler(MillVillager.EntityGenericSymmFemale.class, RenderMillVillager.FACTORY_FEMALE_SYM);
      RenderingRegistry.registerEntityRenderingHandler(EntityWallDecoration.class, RenderWallDecoration.FACTORY_WALL_DECORATION);
      ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMillBed.class, new TileEntityMillBedRenderer());
      ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFirePit.class, new TESRFirePit());
      ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMockBanner.class, new TESRMockBanner());
      ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLockedChest.class, new TileEntityLockedChestRenderer());
      ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPanel.class, new TESRPanel());
   }

   @Override
   public IGuiHandler createGuiHandler() {
      return new ClientGuiHandler();
   }

   @Override
   public String getBlockName(Block block, int meta) {
      if (block == null) {
         MillLog.printException(new MillLog.MillenaireException("Trying to get the name of a null block."));
         return null;
      } else {
         if (meta == -1) {
            meta = 0;
         }

         return new ItemStack(block, 1, meta).getDisplayName();
      }
   }

   @Override
   public UserProfile getClientProfile() {
      if (Mill.proxy.getTheSinglePlayer() == null) {
         return null;
      } else if (Mill.clientWorld.profiles.containsKey(Mill.proxy.getTheSinglePlayer().getUniqueID())) {
         return Mill.clientWorld.profiles.get(Mill.proxy.getTheSinglePlayer().getUniqueID());
      } else {
         UserProfile profile = new UserProfile(Mill.clientWorld, Mill.proxy.getTheSinglePlayer());
         Mill.clientWorld.profiles.put(profile.uuid, profile);
         return profile;
      }
   }

   @Override
   public File getConfigFile() {
      return new File(MillCommonUtilities.getMillenaireContentDir(), "config.txt");
   }

   @Override
   public File getCustomConfigFile() {
      return new File(MillCommonUtilities.getMillenaireCustomContentDir(), "config-custom.txt");
   }

   @Override
   public String getItemName(Item item, int meta) {
      if (item == null) {
         MillLog.printException(new MillLog.MillenaireException("Trying to get the name of a null item."));
         return null;
      } else {
         if (meta == -1) {
            meta = 0;
         }

         return new ItemStack(item, 1, meta).getDisplayName();
      }
   }

   @Override
   public String getKeyString(int value) {
      return Keyboard.getKeyName(value);
   }

   @Override
   public File getLogFile() {
      return new File(MillCommonUtilities.getMillenaireCustomContentDir(), "millenaire.log");
   }

   @Override
   public String getQuestKeyName() {
      return Keyboard.getKeyName(KB_MENU.getKeyCode());
   }

   @Override
   public String getSinglePlayerName() {
      return this.getTheSinglePlayer() != null ? this.getTheSinglePlayer().getName() : "NULL_PLAYER";
   }

   @Override
   public EntityPlayer getTheSinglePlayer() {
      return FMLClientHandler.instance().getClient().player;
   }

   @Override
   public void handleClientGameUpdate() {
      MillClientUtilities.handleKeyPress(Mill.clientWorld.world);
      if (Mill.clientWorld.world.getWorldTime() % 20L == 0L) {
         Mill.clientWorld.clearPanelQueue();
      }

      this.loadLanguagesIfNeeded();
   }

   @Override
   public void handleClientLogin() {
      ClientSender.sendVersionInfo();
      ClientSender.sendAvailableContent();
   }

   @Override
   public void initNetwork() {
      Mill.millChannel.register(new ClientReceiver());
   }

   @Override
   public boolean isTrueServer() {
      return false;
   }

   @Override
   public int loadKeySetting(String value) {
      return Keyboard.getKeyIndex(value.toUpperCase());
   }

   @Override
   public void loadLanguagesIfNeeded() {
      Minecraft minecraft = Minecraft.getMinecraft();
      LanguageUtilities.loadLanguages(minecraft.gameSettings.language);
   }

   @Override
   public void localTranslatedSentence(EntityPlayer player, char colour, String code, String... values) {
      for (int i = 0; i < values.length; i++) {
         values[i] = LanguageUtilities.unknownString(values[i]);
      }

      this.sendLocalChat(player, colour, LanguageUtilities.string(code, values));
   }

   @Override
   public String logPrefix() {
      return "CLIENT ";
   }

   @Override
   public void refreshClientResources() {
      Minecraft.getMinecraft().refreshResources();
   }

   @Override
   public void registerForgeClientClasses() {
      FMLCommonHandler.instance().bus().register(new ClientTickHandler());
      Mill.millChannel.register(new ClientReceiver());
      Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            return worldIn != null && pos != null ? BiomeColorHelper.getFoliageColorAtPos(worldIn, pos) : ColorizerFoliage.getFoliageColorBasic();
         }
      }, new Block[]{MillBlocks.LEAVES_PISTACHIO});
      Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
         public int colorMultiplier(ItemStack stack, int tintIndex) {
            return ColorizerFoliage.getFoliageColorBasic();
         }
      }, new Block[]{MillBlocks.LEAVES_PISTACHIO});
   }

   @Override
   public void registerKeyBindings() {
      KB_MENU = new KeyBinding("key.menu", 50, "key.category.millenaire");
      KB_VILLAGES = new KeyBinding("key.villages", 47, "key.category.millenaire");
      KB_ESCORTS = new KeyBinding("key.escorts", 34, "key.category.millenaire");
      ClientRegistry.registerKeyBinding(KB_MENU);
      ClientRegistry.registerKeyBinding(KB_VILLAGES);
      ClientRegistry.registerKeyBinding(KB_ESCORTS);
   }

   @Override
   public void sendChatAdmin(String s) {
      s = s.trim();
      Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(s));
   }

   @Override
   public void sendChatAdmin(String s, TextFormatting colour) {
      s = s.trim();
      TextComponentString cc = new TextComponentString(s);
      cc.getStyle().setColor(colour);
      Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(cc);
   }

   @Override
   public void sendLocalChat(EntityPlayer player, char colour, String s) {
      s = s.trim();
      Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§" + colour + s));
   }

   @Override
   public void setGraphicsLevel(BlockLeaves blockLeaves, boolean value) {
      blockLeaves.setGraphicsLevel(value);
   }
}
