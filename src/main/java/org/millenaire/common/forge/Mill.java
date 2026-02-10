package org.millenaire.common.forge;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.millenaire.common.advancements.MillAdvancements;
import org.millenaire.common.annotedparameters.ParametersManager;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.buildingplan.BuildingDevUtilities;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.commands.CommandDebugResendProfiles;
import org.millenaire.common.commands.CommandDebugResetVillagers;
import org.millenaire.common.commands.CommandGiveReputation;
import org.millenaire.common.commands.CommandImportCulture;
import org.millenaire.common.commands.CommandListActiveVillages;
import org.millenaire.common.commands.CommandRenameVillage;
import org.millenaire.common.commands.CommandSpawnVillage;
import org.millenaire.common.commands.CommandSwitchVillageControl;
import org.millenaire.common.commands.CommandTeleportToVillage;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.deployer.ContentDeployer;
import org.millenaire.common.entity.EntityTargetedBlaze;
import org.millenaire.common.entity.EntityTargetedGhast;
import org.millenaire.common.entity.EntityTargetedWitherSkeleton;
import org.millenaire.common.entity.EntityWallDecoration;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.entity.VillagerConfig;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.MillItems;
import org.millenaire.common.network.ServerReceiver;
import org.millenaire.common.quest.Quest;
import org.millenaire.common.utilities.BlockItemUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.virtualdir.VirtualDir;
import org.millenaire.common.world.MillWorldData;
import org.millenaire.common.world.WorldGenVillage;

@Mod(modid = "millenaire", name = "Millenaire", version = "Millenaire 8.1.2", useMetadata = true)
public class Mill {
      public static final String MODID = "millenaire";
      public static final String MODNAME = "Millenaire";
      public static final String VERSION_NUMBER = "8.1.2";
      public static final String MINECRAFT_VERSION_NUMBER = "1.12.2";
      public static final String VERSION = "Millenaire 8.1.2";
      public static final Logger LOGGER = LogManager.getLogger("millenaire");
      @SidedProxy(clientSide = "org.millenaire.client.forge.ClientProxy", serverSide = "org.millenaire.common.forge.CommonProxy")
      public static CommonProxy proxy;
      @Instance
      public static Mill instance;
      public static List<MillWorldData> serverWorlds = new ArrayList<>();
      public static MillWorldData clientWorld = null;
      public static List<File> loadingDirs = new ArrayList<>();
      public static VirtualDir virtualLoadingDir;
      public static FMLEventChannel millChannel;
      public static boolean loadingComplete = false;
      public static final ResourceLocation ENTITY_PIG = new ResourceLocation("pig");
      public static final ResourceLocation ENTITY_COW = new ResourceLocation("cow");
      public static final ResourceLocation ENTITY_CHICKEN = new ResourceLocation("chicken");
      public static final ResourceLocation ENTITY_SHEEP = new ResourceLocation("sheep");
      public static final ResourceLocation ENTITY_SQUID = new ResourceLocation("squid");
      public static final ResourceLocation ENTITY_WOLF = new ResourceLocation("wolf");
      public static final ResourceLocation ENTITY_POLAR_BEAR = new ResourceLocation("polar_bear");
      public static final ResourceLocation ENTITY_SKELETON = new ResourceLocation("skeleton");
      public static final ResourceLocation ENTITY_CREEPER = new ResourceLocation("creeper");
      public static final ResourceLocation ENTITY_SPIDER = new ResourceLocation("spider");
      public static final ResourceLocation ENTITY_CAVESPIDER = new ResourceLocation("cave_spider");
      public static final ResourceLocation ENTITY_ZOMBIE = new ResourceLocation("zombie");
      public static final ResourceLocation ENTITY_TARGETED_GHAST = new ResourceLocation("millenaire", "millghast");
      public static final ResourceLocation ENTITY_TARGETED_BLAZE = new ResourceLocation("millenaire", "millblaze");
      public static final ResourceLocation ENTITY_TARGETED_WITHERSKELETON = new ResourceLocation("millenaire",
                  "millwitherskeleton");
      public static final ResourceLocation CROP_WHEAT = new ResourceLocation("wheat");
      public static final ResourceLocation CROP_CARROT = new ResourceLocation("carrots");
      public static final ResourceLocation CROP_POTATO = new ResourceLocation("potatoes");
      public static final ResourceLocation CROP_RICE = new ResourceLocation("millenaire", "crop_rice");
      public static final ResourceLocation CROP_TURMERIC = new ResourceLocation("millenaire", "crop_turmeric");
      public static final ResourceLocation CROP_MAIZE = new ResourceLocation("millenaire", "crop_maize");
      public static final ResourceLocation CROP_VINE = new ResourceLocation("millenaire", "crop_vine");
      public static final ResourceLocation CROP_CACAO = new ResourceLocation("cocoa");
      public static final ResourceLocation CROP_FLOWER = new ResourceLocation("flower");
      public static final ResourceLocation CROP_COTTON = new ResourceLocation("millenaire", "crop_cotton");
      public static boolean startupError = false;
      public static boolean checkedMillenaireDir = false;
      public static boolean displayMillenaireLocationError = false;
      public static SoundEvent SOUND_NORMAN_BELLS;
      static final Class[] BANNER_CLASSES = new Class[] { String.class, String.class, ItemStack.class };
      public static final String[] BANNER_SHORTNAMES = new String[] {
                  "byz",
                  "by1",
                  "by2",
                  "sjk",
                  "may",
                  "inu",
                  "ind",
                  "in1",
                  "in2",
                  "in3",
                  "in4",
                  "in5",
                  "nor",
                  "ma1",
                  "ma2",
                  "ma3",
                  "ma4",
                  "iu1",
                  "iu2",
                  "iu3",
                  "iu4",
                  "jap",
                  "jaa",
                  "jam",
                  "jar",
                  "jat",
                  "sjkr",
                  "sjkm"
      };

      public static MillWorldData getMillWorld(World world) {
            if (clientWorld != null && clientWorld.world == world) {
                  return clientWorld;
            } else {
                  for (MillWorldData mw : serverWorlds) {
                        if (mw.world == world) {
                              return mw;
                        }
                  }

                  return serverWorlds != null && serverWorlds.size() > 0 ? serverWorlds.get(0) : null;
            }
      }

      public static boolean isDistantClient() {
            return clientWorld != null && serverWorlds.isEmpty();
      }

      private void addBannerPatterns() {
            EnumHelper.addEnum(
                        BannerPattern.class, "BYZANTINE", BANNER_CLASSES,
                        new Object[] { "byzantine", BANNER_SHORTNAMES[0],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 0) });
            EnumHelper.addEnum(
                        BannerPattern.class, "BYZANTINE_1", BANNER_CLASSES,
                        new Object[] { "byzantine_1", BANNER_SHORTNAMES[1],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 1) });
            EnumHelper.addEnum(
                        BannerPattern.class, "BYZANTINE_2", BANNER_CLASSES,
                        new Object[] { "byzantine_2", BANNER_SHORTNAMES[2],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 2) });
            EnumHelper.addEnum(
                        BannerPattern.class, "SELJUK", BANNER_CLASSES,
                        new Object[] { "seljuk", BANNER_SHORTNAMES[3], new ItemStack(MillItems.BANNERPATTERN, 1, 3) });
            EnumHelper.addEnum(
                        BannerPattern.class, "MAYAN", BANNER_CLASSES,
                        new Object[] { "mayan", BANNER_SHORTNAMES[4], new ItemStack(MillItems.BANNERPATTERN, 1, 4) });
            EnumHelper.addEnum(
                        BannerPattern.class, "INUIT", BANNER_CLASSES,
                        new Object[] { "inuit", BANNER_SHORTNAMES[5], new ItemStack(MillItems.BANNERPATTERN, 1, 5) });
            EnumHelper.addEnum(
                        BannerPattern.class, "INDIAN", BANNER_CLASSES,
                        new Object[] { "indian", BANNER_SHORTNAMES[6], new ItemStack(MillItems.BANNERPATTERN, 1, 6) });
            EnumHelper.addEnum(
                        BannerPattern.class, "INDIAN_1", BANNER_CLASSES,
                        new Object[] { "indian_1", BANNER_SHORTNAMES[7],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 7) });
            EnumHelper.addEnum(
                        BannerPattern.class, "INDIAN_2", BANNER_CLASSES,
                        new Object[] { "indian_2", BANNER_SHORTNAMES[8],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 8) });
            EnumHelper.addEnum(
                        BannerPattern.class, "INDIAN_3", BANNER_CLASSES,
                        new Object[] { "indian_3", BANNER_SHORTNAMES[9],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 9) });
            EnumHelper.addEnum(
                        BannerPattern.class, "INDIAN_4", BANNER_CLASSES,
                        new Object[] { "indian_4", BANNER_SHORTNAMES[10],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 10) });
            EnumHelper.addEnum(
                        BannerPattern.class, "INDIAN_5", BANNER_CLASSES,
                        new Object[] { "indian_5", BANNER_SHORTNAMES[11],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 11) });
            EnumHelper.addEnum(
                        BannerPattern.class, "NORMAN", BANNER_CLASSES,
                        new Object[] { "norman", BANNER_SHORTNAMES[12],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 12) });
            EnumHelper.addEnum(
                        BannerPattern.class, "MAYAN_1", BANNER_CLASSES,
                        new Object[] { "mayan_1", BANNER_SHORTNAMES[13],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 13) });
            EnumHelper.addEnum(
                        BannerPattern.class, "MAYAN_2", BANNER_CLASSES,
                        new Object[] { "mayan_2", BANNER_SHORTNAMES[14],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 14) });
            EnumHelper.addEnum(
                        BannerPattern.class, "MAYAN_3", BANNER_CLASSES,
                        new Object[] { "mayan_3", BANNER_SHORTNAMES[15],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 15) });
            EnumHelper.addEnum(
                        BannerPattern.class, "MAYAN_4", BANNER_CLASSES,
                        new Object[] { "mayan_4", BANNER_SHORTNAMES[16],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 16) });
            EnumHelper.addEnum(
                        BannerPattern.class, "INUIT_1", BANNER_CLASSES,
                        new Object[] { "inuit_1", BANNER_SHORTNAMES[17],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 17) });
            EnumHelper.addEnum(
                        BannerPattern.class, "INUIT_1", BANNER_CLASSES,
                        new Object[] { "inuit_2", BANNER_SHORTNAMES[18],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 18) });
            EnumHelper.addEnum(
                        BannerPattern.class, "INUIT_1", BANNER_CLASSES,
                        new Object[] { "inuit_3", BANNER_SHORTNAMES[19],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 19) });
            EnumHelper.addEnum(
                        BannerPattern.class, "INUIT_1", BANNER_CLASSES,
                        new Object[] { "inuit_4", BANNER_SHORTNAMES[20],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 20) });
            EnumHelper.addEnum(
                        BannerPattern.class, "JAPANESE", BANNER_CLASSES,
                        new Object[] { "japanese", BANNER_SHORTNAMES[21],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 21) });
            EnumHelper.addEnum(
                        BannerPattern.class,
                        "JAPANESE_AGR",
                        BANNER_CLASSES,
                        new Object[] { "japanese_agr", BANNER_SHORTNAMES[22],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 22) });
            EnumHelper.addEnum(
                        BannerPattern.class,
                        "JAPANESE_MIL",
                        BANNER_CLASSES,
                        new Object[] { "japanese_mil", BANNER_SHORTNAMES[23],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 23) });
            EnumHelper.addEnum(
                        BannerPattern.class,
                        "JAPANESE_REL",
                        BANNER_CLASSES,
                        new Object[] { "japanese_rel", BANNER_SHORTNAMES[24],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 24) });
            EnumHelper.addEnum(
                        BannerPattern.class,
                        "JAPANESE_TRA",
                        BANNER_CLASSES,
                        new Object[] { "japanese_tra", BANNER_SHORTNAMES[25],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 25) });
            EnumHelper.addEnum(
                        BannerPattern.class, "SELJUK_rel", BANNER_CLASSES,
                        new Object[] { "seljuk_rel", BANNER_SHORTNAMES[26],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 26) });
            EnumHelper.addEnum(
                        BannerPattern.class, "SELJUK_mil", BANNER_CLASSES,
                        new Object[] { "seljuk_mil", BANNER_SHORTNAMES[27],
                                    new ItemStack(MillItems.BANNERPATTERN, 1, 27) });
      }

      @EventHandler
      public void init(FMLInitializationEvent e) {
            if (!startupError) {
                  if (MillConfigValues.stopDefaultVillages) {
                        MapGenVillage.VILLAGE_SPAWN_BIOMES = Arrays.asList();
                  }

                  MillBlocks.initBlockStates();
                  boolean error = false;
                  ProgressBar bar = ProgressManager.push("Loading Data",
                              6 + virtualLoadingDir.getChildDirectory("cultures").listSubDirs().size());
                  bar.step("Items...");
                  if (!error) {
                        InvItem.loadItemList();
                  }

                  bar.step("Building points...");
                  if (!error) {
                        error = BuildingPlan.loadBuildingPoints();
                  }

                  bar.step("Villager configs...");
                  if (!error) {
                        VillagerConfig.loadConfigs();
                  }

                  bar.step("Villager goals...");
                  if (!error) {
                        Goal.initGoals();
                  }

                  if (!error) {
                        error = Culture.loadCultures(bar);
                        System.out.println("Millenaire DEBUG: Loaded " + Culture.ListCultures.size() + " cultures.");
                        for (Culture c : Culture.ListCultures) {
                              System.out.println("Millenaire DEBUG: Loaded culture: " + c.key);
                        }
                  }

                  bar.step("Quests...");
                  if (!error) {
                        Quest.loadQuests();
                  }

                  if (MillConfigValues.generateHelpData) {
                        ParametersManager.generateHelpFiles();
                        DocumentedElement.generateHelpFiles();
                  }

                  bar.step("Registering entities & items...");
                  startupError = error;
                  System.out.println("Millenaire DEBUG: Initializing entities and items.");
                  int id = 1;
                  EntityRegistry.registerModEntity(
                              MillVillager.GENERIC_VILLAGER,
                              MillVillager.EntityGenericMale.class,
                              MillVillager.GENERIC_VILLAGER.getPath(),
                              id++,
                              "millenaire",
                              128,
                              3,
                              true);
                  EntityRegistry.registerModEntity(
                              MillVillager.GENERIC_SYMM_FEMALE,
                              MillVillager.EntityGenericSymmFemale.class,
                              MillVillager.GENERIC_SYMM_FEMALE.getPath(),
                              id++,
                              "millenaire",
                              128,
                              3,
                              true);
                  EntityRegistry.registerModEntity(
                              MillVillager.GENERIC_ASYMM_FEMALE,
                              MillVillager.EntityGenericAsymmFemale.class,
                              MillVillager.GENERIC_ASYMM_FEMALE.getPath(),
                              id++,
                              "millenaire",
                              128,
                              3,
                              true);
                  EntityRegistry.registerModEntity(
                              EntityWallDecoration.WALL_DECORATION,
                              EntityWallDecoration.class,
                              EntityWallDecoration.WALL_DECORATION.getPath(),
                              id++,
                              "millenaire",
                              64,
                              3,
                              false);
                  EntityRegistry.registerModEntity(
                              ENTITY_TARGETED_BLAZE, EntityTargetedBlaze.class, ENTITY_TARGETED_BLAZE.getPath(), id++,
                              "millenaire",
                              64, 3, true);
                  EntityRegistry.registerModEntity(
                              ENTITY_TARGETED_WITHERSKELETON, EntityTargetedWitherSkeleton.class,
                              ENTITY_TARGETED_WITHERSKELETON.getPath(), id++, "millenaire", 64, 3, true);
                  EntityRegistry.registerModEntity(
                              ENTITY_TARGETED_GHAST, EntityTargetedGhast.class, ENTITY_TARGETED_GHAST.getPath(), id++,
                              "millenaire",
                              64, 3, true);
                  GameRegistry.addSmelting(new ItemStack(MillBlocks.STONE_DECORATION, 1, 0),
                              new ItemStack(MillBlocks.PAINTED_BRICK_WHITE, 1, 0), 1.0F);
                  GameRegistry.addSmelting(new ItemStack(MillItems.BEARMEAT_RAW, 1, 0),
                              new ItemStack(MillItems.BEARMEAT_COOKED, 1, 0), 1.0F);
                  GameRegistry.addSmelting(new ItemStack(MillItems.WOLFMEAT_RAW, 1, 0),
                              new ItemStack(MillItems.WOLFMEAT_COOKED, 1, 0), 1.0F);
                  GameRegistry.addSmelting(new ItemStack(MillItems.SEAFOOD_RAW, 1, 0),
                              new ItemStack(MillItems.SEAFOOD_COOKED, 1, 0), 1.0F);
                  ResourceLocation location = new ResourceLocation("millenaire", "norman_bells");
                  SOUND_NORMAN_BELLS = new SoundEvent(location);
                  loadingComplete = true;
                  if (MillConfigValues.LogOther >= 1) {
                        if (startupError) {
                              MillLog.major(this, "Millénaire Millénaire 8.1.2 loaded unsuccessfully.");
                        } else {
                              MillLog.major(this, "Millénaire Millénaire 8.1.2 loaded successfully.");
                        }
                  }

                  FMLCommonHandler.instance().bus().register(new ServerTickHandler());
                  FMLCommonHandler.instance().bus().register(this);
                  millChannel.register(new ServerReceiver());
                  proxy.registerForgeClientClasses();
                  proxy.registerKeyBindings();
                  NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy.createGuiHandler());
                  GameRegistry.registerWorldGenerator(new WorldGenVillage(), 1000);
                  MinecraftForge.EVENT_BUS.register(new MillEventController());
                  ForgeChunkManager.setForcedChunkLoadingCallback(this, new BuildingChunkLoader.ChunkLoaderCallback());
                  MillAdvancements.registerTriggers();
                  proxy.loadLanguagesIfNeeded();
                  if (MillConfigValues.DEV && !proxy.isTrueServer()) {
                        BuildingDevUtilities.exportMissingTravelBookDesc();
                        BuildingDevUtilities.exportTravelBookDescCSV();
                  }

                  this.addBannerPatterns();
                  ProgressManager.pop(bar);
            }
      }

      @EventHandler
      public void postInit(FMLPostInitializationEvent e) {
      }

      @EventHandler
      public void preInit(FMLPreInitializationEvent event) {
            millChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel("millenaire");
            MinecraftForge.EVENT_BUS.register(instance);
            ContentDeployer.deployContent(event.getSourceFile());
            MillConfigValues.initConfig();
            proxy.refreshClientResources();
            BlockItemUtilities.initBlockTypes();
      }

      @EventHandler
      public void serverLoad(FMLServerStartingEvent event) {
            event.registerServerCommand(new CommandDebugResendProfiles());
            event.registerServerCommand(new CommandDebugResetVillagers());
            event.registerServerCommand(new CommandRenameVillage());
            event.registerServerCommand(new CommandListActiveVillages());
            event.registerServerCommand(new CommandTeleportToVillage());
            event.registerServerCommand(new CommandGiveReputation());
            event.registerServerCommand(new CommandSpawnVillage(false));
            event.registerServerCommand(new CommandSpawnVillage(true));
            event.registerServerCommand(new CommandImportCulture());
            event.registerServerCommand(new CommandSwitchVillageControl());
      }

}
