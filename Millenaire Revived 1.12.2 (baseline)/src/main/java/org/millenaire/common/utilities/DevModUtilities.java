package org.millenaire.common.utilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.MillItems;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.pathing.atomicstryker.AStarConfig;
import org.millenaire.common.pathing.atomicstryker.AStarNode;
import org.millenaire.common.pathing.atomicstryker.AStarPathPlannerJPS;
import org.millenaire.common.pathing.atomicstryker.IAStarPathedEntity;

public class DevModUtilities {
   private static HashMap<EntityPlayer, Integer> autoMoveDirection = new HashMap<>();
   private static HashMap<EntityPlayer, Integer> autoMoveTarget = new HashMap<>();

   public static void fillInFreeGoods(EntityPlayer player) {
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.JAPANESE_BLUE_LEGGINGS, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.JAPANESE_BLUE_BOOTS, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.JAPANESE_BLUE_HELMET, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.JAPANESE_BLUE_CHESTPLATE, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.JAPANESE_RED_LEGGINGS, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.JAPANESE_RED_BOOTS, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.JAPANESE_RED_HELMET, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.JAPANESE_RED_CHESTPLATE, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.JAPANESE_GUARD_LEGGINGS, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.JAPANESE_GUARD_BOOTS, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.JAPANESE_GUARD_HELMET, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.JAPANESE_GUARD_CHESTPLATE, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.SUMMONING_WAND, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.AMULET_SKOLL_HATI, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, Items.CLOCK, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.NORMAN_AXE, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.NORMAN_PICKAXE, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.NORMAN_SHOVEL, 1);
      MillCommonUtilities.putItemsInChest(player.inventory, Blocks.GOLD_BLOCK, 0, 64);
      MillCommonUtilities.putItemsInChest(player.inventory, Blocks.LOG, 64);
      MillCommonUtilities.putItemsInChest(player.inventory, Items.COAL, 64);
      MillCommonUtilities.putItemsInChest(player.inventory, Blocks.COBBLESTONE, 128);
      MillCommonUtilities.putItemsInChest(player.inventory, Blocks.STONE, 512);
      MillCommonUtilities.putItemsInChest(player.inventory, Blocks.SAND, 128);
      MillCommonUtilities.putItemsInChest(player.inventory, Blocks.WOOL, 64);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.CALVA, 0, 2);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.CHICKEN_CURRY, 2);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.RICE, 0, 64);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.MAIZE, 0, 64);
      MillCommonUtilities.putItemsInChest(player.inventory, MillItems.TURMERIC, 0, 64);
   }

   public static void runAutoMove(World world) {
      for (Object o : world.playerEntities) {
         if (o instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)o;
            if (autoMoveDirection.containsKey(p)) {
               if (autoMoveDirection.get(p) == 1) {
                  if (autoMoveTarget.get(p).intValue() < p.posX) {
                     autoMoveDirection.put(p, -1);
                     autoMoveTarget.put(p, (int)(p.posX - 100000.0));
                     ServerSender.sendChat(p, TextFormatting.GREEN, "Auto-move: turning back.");
                  }
               } else if (autoMoveDirection.get(p) == -1 && autoMoveTarget.get(p).intValue() > p.posX) {
                  autoMoveDirection.put(p, 1);
                  autoMoveTarget.put(p, (int)(p.posX + 100000.0));
                  ServerSender.sendChat(p, TextFormatting.GREEN, "Auto-move: turning back again.");
               }

               p.setPositionAndUpdate(p.posX + autoMoveDirection.get(p).intValue() * 0.5, p.posY, p.posZ);
               p.setPositionAndRotation(p.posX + autoMoveDirection.get(p).intValue() * 0.5, p.posY, p.posZ, p.rotationYaw, p.rotationPitch);
            }
         }
      }
   }

   public static void testGetItemFromBlock() {
      long starttime = System.nanoTime();
      Iterator<Block> iterator = Block.REGISTRY.iterator();

      int count;
      for (count = 0; iterator.hasNext(); count++) {
         Block block = iterator.next();
         Item.getItemFromBlock(block);
      }

      MillLog.temp(null, "Took " + 1.0 * (System.nanoTime() - starttime) / 1000000.0 + " ms to load " + count + " items from blocks.");
   }

   public static void testPaths(EntityPlayer player) {
      Point centre = new Point(player);
      MillLog.temp(null, "Attempting test path around: " + player);
      Point start = null;
      Point end = null;
      int toleranceMode = 0;

      for (int i = 0; i < 100 && (start == null || end == null); i++) {
         for (int j = 0; j < 100 && (start == null || end == null); j++) {
            for (int k = 0; k < 100 && (start == null || end == null); k++) {
               for (int l = 0; l < 8 && (start == null || end == null); l++) {
                  Point p = centre.getRelative(i * (1 - (l & 1) * 2), j * (1 - (l & 2)), k * (1 - (l & 4) / 2));
                  Block block = WorldUtilities.getBlock(player.world, p);
                  if (start == null && block == Blocks.GOLD_BLOCK) {
                     start = p;
                  }

                  if (end == null && block == Blocks.IRON_BLOCK) {
                     end = p.getAbove();
                     toleranceMode = 0;
                  } else if (end == null && block == Blocks.DIAMOND_BLOCK) {
                     end = p.getAbove();
                     toleranceMode = 1;
                  } else if (end == null && block == Blocks.LAPIS_BLOCK) {
                     end = p.getAbove();
                     toleranceMode = 2;
                  }
               }
            }
         }
      }

      if (start != null && end != null) {
         DevModUtilities.DevPathedEntity pathedEntity = new DevModUtilities.DevPathedEntity(player.world, player);
         AStarConfig jpsConfig;
         if (toleranceMode == 1) {
            jpsConfig = new AStarConfig(true, false, false, true, true, 2, 2);
         } else if (toleranceMode == 2) {
            jpsConfig = new AStarConfig(true, false, false, true, true, 2, 20);
         } else {
            jpsConfig = new AStarConfig(true, false, false, true, true);
         }

         ServerSender.sendChat(player, TextFormatting.DARK_GREEN, "Calculating path. Tolerance H: " + jpsConfig.toleranceHorizontal);
         AStarPathPlannerJPS jpsPathPlanner = new AStarPathPlannerJPS(player.world, pathedEntity, true);

         try {
            jpsPathPlanner.getPath(start.getiX(), start.getiY(), start.getiZ(), end.getiX(), end.getiY(), end.getiZ(), jpsConfig);
         } catch (ThreadSafeUtilities.ChunkAccessException var11) {
            MillLog.printException(var11);
         }
      } else {
         ServerSender.sendChat(player, TextFormatting.DARK_RED, "Could not find start or end: " + start + " - " + end);
      }
   }

   public static void toggleAutoMove(EntityPlayer player) {
      if (autoMoveDirection.containsKey(player)) {
         autoMoveDirection.remove(player);
         autoMoveTarget.remove(player);
         ServerSender.sendChat(player, TextFormatting.GREEN, "Auto-move disabled");
      } else {
         autoMoveDirection.put(player, 1);
         autoMoveTarget.put(player, (int)(player.posX + 100000.0));
         ServerSender.sendChat(player, TextFormatting.GREEN, "Auto-move enabled");
      }
   }

   public static void validateResourceMap(Map<InvItem, Integer> map) {
      int errors = 0;

      for (InvItem item : map.keySet()) {
         if (item == null) {
            MillLog.printException(new MillLog.MillenaireException("Found a null InvItem in map!"));
            errors++;
         } else if (!map.containsKey(item)) {
            MillLog.printException(new MillLog.MillenaireException("Key: " + item + " not present in map???"));
            errors++;
         } else if (map.get(item) == null) {
            MillLog.printException(new MillLog.MillenaireException("Key: " + item + " has null value in map."));
            errors++;
         }
      }

      if (map.size() > 0) {
         MillLog.error(null, "Validated map. Found " + errors + " amoung " + map.size() + " keys.");
      }
   }

   public static void villagerInteractDev(EntityPlayer entityplayer, MillVillager villager) {
      if (villager.isChild()) {
         villager.growSize();
         ServerSender.sendChat(entityplayer, TextFormatting.GREEN, villager.getName() + ": Size: " + villager.getSize() + " gender: " + villager.gender);
         if (entityplayer.inventory.getCurrentItem() != null && entityplayer.inventory.getCurrentItem().getItem() == MillItems.SUMMONING_WAND) {
            villager.getRecord().size = 20;
            villager.growSize();
         }
      }

      if (entityplayer.inventory.getCurrentItem() == ItemStack.EMPTY
         || entityplayer.inventory.getCurrentItem().getItem() == Items.AIR) {
         ServerSender.sendChat(
            entityplayer,
            TextFormatting.GREEN,
            villager.getName() + ": Current goal: " + villager.getGoalLabel(villager.goalKey) + " Current pos: " + villager.getPos()
         );
         ServerSender.sendChat(
            entityplayer, TextFormatting.GREEN, villager.getName() + ": House: " + villager.housePoint + " Town Hall: " + villager.townHallPoint
         );
         ServerSender.sendChat(entityplayer, TextFormatting.GREEN, villager.getName() + ": ID: " + villager.getVillagerId());
         if (villager.getRecord() != null) {
            ServerSender.sendChat(entityplayer, TextFormatting.GREEN, villager.getName() + ": Spouse: " + villager.getRecord().spousesName);
         }

         if (villager.getPathDestPoint() != null && villager.pathEntity != null && villager.pathEntity.getCurrentPathLength() > 1) {
            ServerSender.sendChat(
               entityplayer,
               TextFormatting.GREEN,
               villager.getName()
                  + ": Dest: "
                  + villager.getPathDestPoint()
                  + " distance: "
                  + villager.getPathDestPoint().distanceTo(villager)
                  + " stuck: "
                  + villager.longDistanceStuck
                  + " jump:"
                  + villager.pathEntity.getNextTargetPathPoint()
            );
         } else {
            ServerSender.sendChat(entityplayer, TextFormatting.GREEN, villager.getName() + ": No dest point.");
         }

         String s = "";
         if (villager.getRecord() != null) {
            for (String tag : villager.getRecord().questTags) {
               s = s + tag + " ";
            }
         }

         if (villager.mw.getProfile(entityplayer).villagersInQuests.containsKey(villager.getVillagerId())) {
            s = s
               + " quest: "
               + villager.mw.getProfile(entityplayer).villagersInQuests.get(villager.getVillagerId()).quest.key
               + "/"
               + villager.mw.getProfile(entityplayer).villagersInQuests.get(villager.getVillagerId()).getCurrentVillager().id;
         }

         if (s != null && s.length() > 0) {
            ServerSender.sendChat(entityplayer, TextFormatting.GREEN, "Tags: " + s);
         }

         s = "";

         for (InvItem key : villager.inventory.keySet()) {
            if (villager.inventory.get(key) > 0) {
               s = s + key + ":" + villager.inventory.get(key) + " ";
            }
         }

         if (villager.getAttackTarget() != null) {
            s = s + "attacking: " + villager.getAttackTarget() + " ";
         }

         if (s != null && s.length() > 0) {
            ServerSender.sendChat(entityplayer, TextFormatting.GREEN, "Inv: " + s);
         }
      } else if (entityplayer.inventory.getCurrentItem() != ItemStack.EMPTY
         && entityplayer.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(Blocks.SAND)) {
         if (villager.hiredBy == null) {
            villager.hiredBy = entityplayer.getName();
            ServerSender.sendChat(entityplayer, TextFormatting.GREEN, "Hired: " + entityplayer.getName());
         } else {
            villager.hiredBy = null;
            ServerSender.sendChat(entityplayer, TextFormatting.GREEN, "No longer hired");
         }
      } else if (entityplayer.inventory.getCurrentItem() != ItemStack.EMPTY
         && entityplayer.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(Blocks.DIRT)
         && villager.pathEntity != null) {
         int meta = MillCommonUtilities.randomInt(16);

         for (PathPoint p : villager.pathEntity.pointsCopy) {
            if (WorldUtilities.getBlock(villager.world, p.x, p.y - 1, p.z) != MillBlocks.LOCKED_CHEST) {
               WorldUtilities.setBlockAndMetadata(villager.world, new Point(p).getBelow(), Blocks.WOOL, meta);
            }
         }

         PathPoint px = villager.pathEntity.getCurrentTargetPathPoint();
         if (px != null && WorldUtilities.getBlock(villager.world, px.x, px.y - 1, px.z) != MillBlocks.LOCKED_CHEST
            )
          {
            WorldUtilities.setBlockAndMetadata(villager.world, new Point(px).getBelow(), Blocks.GOLD_BLOCK, 0);
         }

         px = villager.pathEntity.getNextTargetPathPoint();
         if (px != null && WorldUtilities.getBlock(villager.world, px.x, px.y - 1, px.z) != MillBlocks.LOCKED_CHEST
            )
          {
            WorldUtilities.setBlockAndMetadata(villager.world, new Point(px).getBelow(), Blocks.DIAMOND_BLOCK, 0);
         }

         px = villager.pathEntity.getPreviousTargetPathPoint();
         if (px != null && WorldUtilities.getBlock(villager.world, px.x, px.y - 1, px.z) != MillBlocks.LOCKED_CHEST
            )
          {
            WorldUtilities.setBlockAndMetadata(villager.world, new Point(px).getBelow(), Blocks.IRON_BLOCK, 0);
         }
      }

      if (villager.hasChildren()
         && entityplayer.inventory.getCurrentItem() != ItemStack.EMPTY
         && entityplayer.inventory.getCurrentItem().getItem() == MillItems.SUMMONING_WAND) {
         MillVillager child = villager.getHouse().createChild(villager, villager.getTownHall(), villager.getRecord().spousesName);
         if (child != null) {
            child.getRecord().size = 20;
            child.growSize();
         }
      }
   }

   private static class DevPathedEntity implements IAStarPathedEntity {
      World world;
      EntityPlayer caller;

      DevPathedEntity(World w, EntityPlayer p) {
         this.world = w;
         this.caller = p;
      }

      @Override
      public void onFoundPath(List<AStarNode> result) {
         int meta = MillCommonUtilities.randomInt(16);

         for (AStarNode node : result) {
            if (node != result.get(0) && node != result.get(result.size() - 1)) {
               WorldUtilities.setBlockAndMetadata(this.world, new Point(node).getBelow(), Blocks.WOOL, meta);
            }
         }
      }

      @Override
      public void onNoPathAvailable() {
         ServerSender.sendChat(this.caller, TextFormatting.DARK_RED, "No path available.");
      }
   }
}
