package org.millenaire.common.annotedparameters;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.entity.VillagerConfig;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.BlockStateUtilities;
import org.millenaire.common.utilities.MillLog;

public abstract class ValueIO {
   public String description;

   protected static List<String> createListFromValue(String value) {
      List<String> list = new ArrayList<>();
      list.add(value);
      return list;
   }

   public abstract void readValue(Object var1, Field var2, String var3) throws Exception;

   public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
      MillLog.error(this, "Trying to use readValueCulture but it is not implemented.");
   }

   public boolean skipWritingValue(Object value) {
      return false;
   }

   public boolean useCulture() {
      return false;
   }

   public abstract List<String> writeValue(Object var1) throws Exception;

   public static class BlockIdIO extends ValueIO {
      public BlockIdIO() {
         this.description = "Minecraft ID of a block ('wheat')";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         ResourceLocation rl = new ResourceLocation(value.toLowerCase());
         if (Block.REGISTRY.getObject(rl) != null) {
            field.set(targetClass, rl);
         } else {
            throw new MillLog.MillenaireException("Unknown block: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         ResourceLocation value = (ResourceLocation)rawValue;
         return value == null ? null : createListFromValue(value.toString());
      }
   }

   public static class BlockStateAddIO extends ValueIO {
      public BlockStateAddIO() {
         this.description = "a Minecraft blockstate ('red_flower;type=blue_orchid') (multiple lines possible)";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] params = value.split(";");
         Block block = Block.getBlockFromName(params[0]);
         if (block == null) {
            throw new MillLog.MillenaireException("Unknown block: " + value);
         } else {
            IBlockState bs;
            if (params.length > 1) {
               bs = BlockStateUtilities.getBlockStateWithValues(block.getDefaultState(), params[1]);
            } else {
               bs = block.getDefaultState();
            }

            ((List)field.get(targetClass)).add(bs);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         List<IBlockState> blockStates = (List<IBlockState>)rawValue;
         List<String> results = new ArrayList<>();

         for (IBlockState bs : blockStates) {
            results.add(BlockStateUtilities.getStringFromBlockState(bs));
         }

         return results;
      }
   }

   public static class BlockStateIO extends ValueIO {
      public BlockStateIO() {
         this.description = "a Minecraft blockstate ('red_flower;type=blue_orchid')";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] params = value.split(";");
         Block block = Block.getBlockFromName(params[0]);
         if (block == null) {
            throw new MillLog.MillenaireException("Unknown block: " + value);
         } else {
            if (params.length > 1) {
               field.set(targetClass, BlockStateUtilities.getBlockStateWithValues(block.getDefaultState(), params[1]));
            } else {
               field.set(targetClass, block.getDefaultState());
            }
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         IBlockState value = (IBlockState)rawValue;
         return createListFromValue(BlockStateUtilities.getStringFromBlockState(value));
      }
   }

   public static class BonusItemAddIO extends ValueIO {
      public BonusItemAddIO() {
         this.description = "item, chance and (optional) required tag ('leather,50' or 'boudin,50,oven')";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] temp2 = value.toLowerCase().split(",");
         AnnotedParameter.BonusItem bonusItem = null;
         if (temp2.length != 3 && temp2.length != 2) {
            throw new MillLog.MillenaireException(
               "bonusitem must take the form of bonusitem=goodname,chanceon100 or bonusitem=goodname,chanceon100,requiredtag (ex: leather,50 or tripes,10,oven)."
            );
         } else if (InvItem.INVITEMS_BY_NAME.containsKey(temp2[0])) {
            if (temp2.length == 3) {
               bonusItem = new AnnotedParameter.BonusItem(InvItem.INVITEMS_BY_NAME.get(temp2[0]), Integer.parseInt(temp2[1]), temp2[2].trim());
            } else {
               bonusItem = new AnnotedParameter.BonusItem(InvItem.INVITEMS_BY_NAME.get(temp2[0]), Integer.parseInt(temp2[1]));
            }

            ((List)field.get(targetClass)).add(bonusItem);
         } else {
            throw new MillLog.MillenaireException("Unknown bonusitem item :" + temp2[0]);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         List<AnnotedParameter.BonusItem> bonusItems = (List<AnnotedParameter.BonusItem>)rawValue;
         List<String> results = new ArrayList<>();

         for (AnnotedParameter.BonusItem bonusItem : bonusItems) {
            if (bonusItem.tag == null) {
               results.add(bonusItem.item.getKey() + "," + bonusItem.chance);
            } else {
               results.add(bonusItem.item.getKey() + "," + bonusItem.chance + "," + bonusItem.tag);
            }
         }

         return results;
      }
   }

   public static class BooleanIO extends ValueIO {
      public BooleanIO() {
         this.description = "boolean";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         field.set(targetClass, Boolean.parseBoolean(value));
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         Boolean value = (Boolean)rawValue;
         return createListFromValue(value ? "true" : "false");
      }
   }

   public static class BrickColourThemeAddIO extends ValueIO {
      public BrickColourThemeAddIO() {
         this.description = "Example: 'rajput:30;brown:50,red:40,orange:30;yellow:30'";
      }

      private String getAllColourNames() {
         String colours = "";

         for (EnumDyeColor color : EnumDyeColor.values()) {
            colours = colours + color.getName() + " ";
         }

         return colours;
      }

      private EnumDyeColor getColourByName(String colourName) {
         for (EnumDyeColor color : EnumDyeColor.values()) {
            if (color.getName().equals(colourName)) {
               return color;
            }
         }

         return null;
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String themeDefinition = value.split(";")[0];
         String themeKey = themeDefinition.split(":")[0];
         int themeWeight = Integer.parseInt(themeDefinition.split(":")[1]);
         Map<EnumDyeColor, Map<EnumDyeColor, Integer>> themesMapping = new HashMap<>();
         Map<EnumDyeColor, Integer> otherMapping = null;

         for (int i = 1; i < value.split(";").length; i++) {
            String themeData = value.split(";")[i];
            String key = themeData.split(":")[0];
            String possibleValues = themeData.substring(key.length() + 1, themeData.length());
            Map<EnumDyeColor, Integer> values = new HashMap<>();

            for (String weightedColour : possibleValues.split(",")) {
               String colourName = weightedColour.split(":")[0];
               EnumDyeColor colour = this.getColourByName(colourName);
               if (colour == null) {
                  throw new MillLog.MillenaireException("Unknown colour: " + colourName + ". It should be among: " + this.getAllColourNames());
               }

               int weight = Integer.parseInt(weightedColour.split(":")[1]);
               values.put(colour, weight);
            }

            if (key.equals("other")) {
               otherMapping = values;
            } else {
               EnumDyeColor inputColour = this.getColourByName(key);
               if (inputColour == null) {
                  throw new MillLog.MillenaireException("Unknown colour: " + key + ". It should be among: " + this.getAllColourNames());
               }

               themesMapping.put(inputColour, values);
            }
         }

         for (EnumDyeColor inputColour : EnumDyeColor.values()) {
            if (!themesMapping.containsKey(inputColour)) {
               themesMapping.put(inputColour, otherMapping);
            }
         }

         List<VillageType.BrickColourTheme> themeList = (List<VillageType.BrickColourTheme>)field.get(targetClass);
         themeList.add(new VillageType.BrickColourTheme(themeKey, themeWeight, themesMapping));
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         List<VillageType.BrickColourTheme> themeList = (List<VillageType.BrickColourTheme>)rawValue;
         List<String> results = new ArrayList<>();

         for (VillageType.BrickColourTheme theme : themeList) {
            String line = theme.key + ":" + theme.weight;

            for (EnumDyeColor inputColour : theme.colours.keySet()) {
               line = line + ";" + inputColour.getName() + ":";
               String values = "";

               for (EnumDyeColor outputColour : theme.colours.get(inputColour).keySet()) {
                  if (values.length() > 0) {
                     values = values + ",";
                  }

                  values = values + outputColour.getName() + ":" + theme.colours.get(inputColour).get(outputColour);
               }

               line = line + values;
            }

            results.add(line);
         }

         return results;
      }
   }

   public static class ClothAddIO extends ValueIO {
      public ClothAddIO() {
         this.description = "A cloth texture that can be worn when an item is present. The optional second parameter is the layer it will be placed on. ('free,textures/entity/byzanz/male/clothes/byz.miner.1.A.png' or 'free,0,textures/entity/norman/female/clothes/nor_housewife_0.png')";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         Map<String, List<String>> map = (Map<String, List<String>>)field.get(targetClass);
         value = value.toLowerCase();
         if (value.split(",").length < 2) {
            MillLog.error(
               null, "Two or three values are required for all clothes tag: either (cloth name, then texture file) or (cloth name, layer, then texture file)."
            );
         } else {
            int layer = 0;
            String clothname = value.split(",")[0];
            String textpath;
            if (value.split(",").length == 2) {
               textpath = value.split(",")[1];
            } else {
               layer = Integer.parseInt(value.split(",")[1]);
               textpath = value.split(",")[2];
            }

            if (!map.containsKey(clothname + "_" + layer)) {
               map.put(clothname + "_" + layer, new ArrayList<>());
            }

            map.get(clothname + "_" + layer).add(textpath);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         Map<String, List<String>> clothes = (Map<String, List<String>>)rawValue;
         List<String> results = new ArrayList<>();
         List<String> keys = new ArrayList<>(clothes.keySet());
         Collections.sort(keys);

         for (String key : keys) {
            String cloth = key.split("_")[0];
            String layer = key.split("_")[1];

            for (String texture : clothes.get(key)) {
               results.add(cloth + "," + layer + "," + texture);
            }
         }

         return results;
      }
   }

   public static class DirectionIO extends ValueIO {
      public DirectionIO() {
         this.description = "A direction, such as 'east' or 'north'";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         if (value.equalsIgnoreCase("east")) {
            field.set(targetClass, 3);
         } else if (value.equalsIgnoreCase("west")) {
            field.set(targetClass, 1);
         } else if (value.equalsIgnoreCase("north")) {
            field.set(targetClass, 0);
         } else if (value.equalsIgnoreCase("south")) {
            field.set(targetClass, 2);
         } else {
            MillLog.error(null, "Unknown direction found: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         Integer value = (Integer)rawValue;
         if (value == 3) {
            return createListFromValue("east");
         } else if (value == 1) {
            return createListFromValue("west");
         } else if (value == 0) {
            return createListFromValue("north");
         } else {
            return (List<String>)(value == 2 ? createListFromValue("south") : new ArrayList<>());
         }
      }
   }

   public static class EntityIO extends ValueIO {
      public EntityIO() {
         this.description = "Minecraft ID of an entity ('cow')";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         ResourceLocation rl = new ResourceLocation(value.toLowerCase());
         if (EntityList.getEntityNameList().contains(rl)) {
            field.set(targetClass, rl);
         } else {
            throw new MillLog.MillenaireException("Unknown entity: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         ResourceLocation value = (ResourceLocation)rawValue;
         return createListFromValue(value.toString());
      }
   }

   public static class FloatIO extends ValueIO {
      public FloatIO() {
         this.description = "floating point value";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         field.set(targetClass, Float.parseFloat(value));
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         Float value = (Float)rawValue;
         return createListFromValue("" + value);
      }
   }

   public static class GenderIO extends ValueIO {
      public GenderIO() {
         this.description = "A gender, either 'male' or 'female'";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         if (value.toLowerCase().equals("male")) {
            field.set(targetClass, 1);
         } else if (value.toLowerCase().equals("female")) {
            field.set(targetClass, 2);
         } else {
            MillLog.error(null, "Unknown gender found: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         Integer value = (Integer)rawValue;
         return value == 1 ? createListFromValue("male") : createListFromValue("female");
      }
   }

   public static class GoalAddIO extends ValueIO {
      public GoalAddIO() {
         this.description = "Id of a goal ('construction', 'gopray'...)";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         if (Goal.goals.containsKey(value.toLowerCase())) {
            ((List)field.get(targetClass)).add(Goal.goals.get(value.toLowerCase()));
         } else {
            MillLog.error(null, "Unknown goal: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         List<Goal> goals = (List<Goal>)rawValue;
         List<String> results = new ArrayList<>();

         for (Goal goal : goals) {
            results.add(goal.key);
         }

         return results;
      }
   }

   public static class IntegerArrayIO extends ValueIO {
      public IntegerArrayIO() {
         this.description = "list of integers: '1,2,3'";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] segments = value.toLowerCase().split(",");
         if (segments[0].length() > 0) {
            int[] array = new int[segments.length];

            for (int i = 0; i < segments.length; i++) {
               array[i] = Integer.parseInt(segments[i]);
            }

            field.set(targetClass, array);
         }
      }

      @Override
      public boolean skipWritingValue(Object value) {
         int[] ints = (int[])value;
         return ints.length == 1 && ints[0] == 0;
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         int[] ints = (int[])rawValue;
         String result = "";

         for (int i : ints) {
            if (result.length() > 0) {
               result = result + ",";
            }

            result = result + i;
         }

         return createListFromValue(result);
      }
   }

   public static class IntegerIO extends ValueIO {
      public IntegerIO() {
         this.description = "integer value";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         field.set(targetClass, Integer.parseInt(value));
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         int value = (Integer)rawValue;
         return createListFromValue("" + value);
      }
   }

   public static class InvItemAddIO extends ValueIO {
      public InvItemAddIO() {
         this.description = "an item: ('chickenmeat')";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         if (InvItem.INVITEMS_BY_NAME.containsKey(value.toLowerCase())) {
            ((List)field.get(targetClass)).add(InvItem.INVITEMS_BY_NAME.get(value.toLowerCase()));
         } else {
            throw new MillLog.MillenaireException("Unknown item: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         List<InvItem> invItems = (List<InvItem>)rawValue;
         List<String> results = new ArrayList<>();

         for (InvItem invItem : invItems) {
            results.add(invItem.getKey());
         }

         return results;
      }
   }

   public static class InvItemIO extends ValueIO {
      public InvItemIO() {
         this.description = "item (from itemlist.txt)";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         if (InvItem.INVITEMS_BY_NAME.containsKey(value.toLowerCase())) {
            field.set(targetClass, InvItem.INVITEMS_BY_NAME.get(value.toLowerCase()));
         } else {
            throw new MillLog.MillenaireException("Unknown item: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         InvItem value = (InvItem)rawValue;
         return createListFromValue(value.getKey());
      }
   }

   public static class InvItemNumberAddIO extends ValueIO {
      public InvItemNumberAddIO() {
         this.description = "item and number: 'bone,8'";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] temp2 = value.toLowerCase().split(",");
         if (temp2.length != 2) {
            throw new MillLog.MillenaireException("Invalid item quantity setting. They must take the form of parameter=goodname,goodquatity.");
         } else if (InvItem.INVITEMS_BY_NAME.containsKey(temp2[0])) {
            ((Map)field.get(targetClass)).put(InvItem.INVITEMS_BY_NAME.get(temp2[0]), Integer.parseInt(temp2[1]));
         } else {
            throw new MillLog.MillenaireException("Unknown item: " + temp2[0]);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         Map<InvItem, Integer> invItems = (Map<InvItem, Integer>)rawValue;
         List<String> results = new ArrayList<>();
         List<InvItem> keys = new ArrayList<>(invItems.keySet());
         Collections.sort(keys);

         for (InvItem invItem : keys) {
            results.add(invItem.getKey() + "," + invItems.get(invItem));
         }

         return results;
      }
   }

   public static class InvItemPairIO extends ValueIO {
      public InvItemPairIO() {
         this.description = "pair of items: 'stone,sand'";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] temp2 = value.toLowerCase().split(",");
         if (temp2.length != 2) {
            throw new MillLog.MillenaireException("Item pairs must take the form of parameter=firstgood,secondgood.");
         } else if (!InvItem.INVITEMS_BY_NAME.containsKey(temp2[0]) && !InvItem.INVITEMS_BY_NAME.containsKey(temp2[1])) {
            throw new MillLog.MillenaireException("Unknown item : " + temp2[0] + " or " + temp2[1]);
         } else {
            field.set(targetClass, new InvItem[]{InvItem.INVITEMS_BY_NAME.get(temp2[0]), InvItem.INVITEMS_BY_NAME.get(temp2[1])});
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         InvItem[] pair = (InvItem[])rawValue;
         return createListFromValue(pair[0].getKey() + "," + pair[1].getKey());
      }
   }

   public static class InvItemPriceAddIO extends ValueIO {
      public InvItemPriceAddIO() {
         this.description = "item and price in the form of gold/silver/bronze deniers: 'bone,1/0/0'";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] temp2 = value.toLowerCase().split(",");
         if (temp2.length != 2) {
            throw new MillLog.MillenaireException("Invalid item quantity setting. They must take the form of parameter=goodname,goodquatity.");
         } else if (InvItem.INVITEMS_BY_NAME.containsKey(temp2[0])) {
            int price = 0;
            String[] pricestr = temp2[1].split("/");
            if (pricestr.length == 1) {
               price = Integer.parseInt(pricestr[0]);
            } else if (pricestr.length == 2) {
               price = Integer.parseInt(pricestr[0]) * 64 + Integer.parseInt(pricestr[1]);
            } else if (pricestr.length == 3) {
               price = Integer.parseInt(pricestr[0]) * 64 * 64 + Integer.parseInt(pricestr[1]) * 64 + Integer.parseInt(pricestr[2]);
            } else {
               MillLog.error(this, "Could not parse the price: " + value);
            }

            ((Map)field.get(targetClass)).put(InvItem.INVITEMS_BY_NAME.get(temp2[0]), price);
         } else {
            throw new MillLog.MillenaireException("Unknown item: " + temp2[0]);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         Map<InvItem, Integer> invItems = (Map<InvItem, Integer>)rawValue;
         List<String> results = new ArrayList<>();
         List<InvItem> keys = new ArrayList<>(invItems.keySet());
         Collections.sort(keys);

         for (InvItem invItem : keys) {
            int price = invItems.get(invItem);
            int priceGold = price / 4096;
            int priceSilver = (price - priceGold * 64 * 64) / 64;
            int priceBronze = price - priceGold * 64 * 64 - priceSilver * 64;
            results.add(invItem.getKey() + "," + priceGold + "/" + priceSilver + "/" + priceBronze);
         }

         return results;
      }
   }

   public static class ItemStackArrayIO extends ValueIO {
      public ItemStackArrayIO() {
         this.description = "list of items: 'chickenmeat,chickenmeatcooked'";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] temp2 = value.toLowerCase().split(",");
         ItemStack[] itemsList = new ItemStack[temp2.length];

         for (int i = 0; i < temp2.length; i++) {
            if (!InvItem.INVITEMS_BY_NAME.containsKey(temp2[i])) {
               throw new MillLog.MillenaireException("Unknown item: " + temp2[i]);
            }

            itemsList[i] = InvItem.INVITEMS_BY_NAME.get(temp2[i]).getItemStack();
            if (itemsList[i].getItem() == null) {
               throw new MillLog.MillenaireException("Item list with null item: " + temp2[i]);
            }
         }

         field.set(targetClass, itemsList);
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         ItemStack[] stacks = (ItemStack[])rawValue;
         String result = "";

         for (ItemStack stack : stacks) {
            if (result.length() > 0) {
               result = result + ",";
            }

            result = result + InvItem.createInvItem(stack).getKey();
         }

         return createListFromValue(result);
      }
   }

   public static class MillisecondsIO extends ValueIO {
      public MillisecondsIO() {
         this.description = "milliseconds";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         field.set(targetClass, Integer.parseInt(value) * 20 / 1000);
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         int value = (Integer)rawValue;
         return createListFromValue("" + value * 1000 / 20);
      }
   }

   public static class PosTypeIO extends ValueIO {
      public PosTypeIO() {
         this.description = "Type of position point (sleeping, leasure, selling...)";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         AnnotedParameter.PosType posType = AnnotedParameter.PosType.getByType(value.toLowerCase());
         if (posType == null) {
            throw new MillLog.MillenaireException("Unknown position type: " + value + ". It should be among: " + AnnotedParameter.PosType.getAllCodes() + ".");
         } else {
            field.set(targetClass, posType);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         AnnotedParameter.PosType value = (AnnotedParameter.PosType)rawValue;
         return createListFromValue(value.code);
      }
   }

   public static class RandomBrickColourAddIO extends ValueIO {
      public RandomBrickColourAddIO() {
         this.description = "Example: 'white;white:50,yellow:40,orange:30' means that white coloured bricks can turn into white, yellow or orange bricks, with weights of 50, 40 and 30 respectively.";
      }

      private String getAllColourNames() {
         String colours = "";

         for (EnumDyeColor color : EnumDyeColor.values()) {
            colours = colours + color.getName() + " ";
         }

         return colours;
      }

      private EnumDyeColor getColourByName(String colourName) {
         for (EnumDyeColor color : EnumDyeColor.values()) {
            if (color.getName().equals(colourName)) {
               return color;
            }
         }

         return null;
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String mainColourName = value.split(";")[0];
         EnumDyeColor mainColour = this.getColourByName(mainColourName);
         if (mainColour == null) {
            throw new MillLog.MillenaireException("Unknown colour: " + mainColourName + ". It should be among: " + this.getAllColourNames());
         } else {
            String possibleValues = value.split(";")[1];
            Map<EnumDyeColor, Integer> values = new HashMap<>();

            for (String weightedColour : possibleValues.split(",")) {
               String colourName = weightedColour.split(":")[0];
               EnumDyeColor colour = this.getColourByName(colourName);
               if (colour == null) {
                  throw new MillLog.MillenaireException("Unknown colour: " + colourName + ". It should be among: " + this.getAllColourNames());
               }

               int weight = Integer.parseInt(weightedColour.split(":")[1]);
               values.put(colour, weight);
            }

            Map<EnumDyeColor, Map<EnumDyeColor, Integer>> coloursMap = (Map<EnumDyeColor, Map<EnumDyeColor, Integer>>)field.get(targetClass);
            coloursMap.put(mainColour, values);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         Map<EnumDyeColor, Map<EnumDyeColor, Integer>> coloursMap = (Map<EnumDyeColor, Map<EnumDyeColor, Integer>>)rawValue;
         List<String> results = new ArrayList<>();

         for (EnumDyeColor mainColour : coloursMap.keySet()) {
            String line = mainColour.getName() + ":";
            String values = "";

            for (EnumDyeColor colour : coloursMap.get(mainColour).keySet()) {
               if (values.length() > 0) {
                  values = values + ",";
               }

               values = values + colour.getName() + ":" + coloursMap.get(mainColour).get(colour);
            }

            line = line + values;
            results.add(line);
         }

         return results;
      }
   }

   public static class ResourceLocationIO extends ValueIO {
      public ResourceLocationIO() {
         this.description = "Minecraft resource path";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         field.set(targetClass, new ResourceLocation(value));
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         ResourceLocation resource = (ResourceLocation)rawValue;
         String value = resource.toString();
         return createListFromValue(value);
      }
   }

   public static class StartingItemAddIO extends ValueIO {
      public StartingItemAddIO() {
         this.description = "item, chance, fixed number, maximum bonus number ('leather,0.5,8,8' for between 8 and 16 leather 50% of the time)";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] params = value.toLowerCase().split(",");
         if (params.length != 4) {
            MillLog.error(null, "Error when reading starting good: expected four fields, found " + params.length + ": " + value);
         } else {
            String s = params[0];
            if (!InvItem.INVITEMS_BY_NAME.containsKey(s)) {
               MillLog.error(null, "Error when reading starting good: unknown good: " + s);
            } else {
               BuildingPlan.StartingGood sg = new BuildingPlan.StartingGood(
                  InvItem.INVITEMS_BY_NAME.get(s), Double.parseDouble(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3])
               );
               ((List)field.get(targetClass)).add(sg);
            }
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         List<BuildingPlan.StartingGood> startingGoods = (List<BuildingPlan.StartingGood>)rawValue;
         List<String> results = new ArrayList<>();

         for (BuildingPlan.StartingGood startingGood : startingGoods) {
            results.add(startingGood.item.getKey() + "," + startingGood.probability + "," + startingGood.fixedNumber + "," + startingGood.randomNumber);
         }

         return results;
      }
   }

   public static class StringAddIO extends ValueIO {
      public StringAddIO() {
         this.description = "string (case-insensitive, multiple parameters possible)";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         ((List)field.get(targetClass)).add(value.toLowerCase());
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         return (List<String>)rawValue;
      }
   }

   public static class StringCaseSensitiveAddIO extends ValueIO {
      public StringCaseSensitiveAddIO() {
         this.description = "string (multiple parameters possible)";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         ((List)field.get(targetClass)).add(value);
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         return (List<String>)rawValue;
      }
   }

   public static class StringDisplayIO extends ValueIO {
      public StringDisplayIO() {
         this.description = "string";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         field.set(targetClass, value);
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         String value = (String)rawValue;
         return createListFromValue(value);
      }
   }

   public static class StringIO extends ValueIO {
      public StringIO() {
         this.description = "string (case-insensitive)";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         field.set(targetClass, value.toLowerCase());
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         String value = (String)rawValue;
         return createListFromValue(value);
      }
   }

   public static class StringInvItemAddIO extends ValueIO {
      public StringInvItemAddIO() {
         this.description = "String and item: 'villager,wheat'";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] temp2 = value.toLowerCase().split(",");
         if (temp2.length != 2) {
            throw new MillLog.MillenaireException("Invalid setting. They must take the form of parameter=string,item.");
         } else if (InvItem.INVITEMS_BY_NAME.containsKey(temp2[1])) {
            ((Map)field.get(targetClass)).put(temp2[0], InvItem.INVITEMS_BY_NAME.get(temp2[1]));
         } else {
            throw new MillLog.MillenaireException("Unknown item: " + temp2[1]);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         Map<String, InvItem> map = (Map<String, InvItem>)rawValue;
         List<String> results = new ArrayList<>();
         List<String> keys = new ArrayList<>(map.keySet());
         Collections.sort(keys);

         for (String string : keys) {
            results.add(string + "," + map.get(string).getKey());
         }

         return results;
      }
   }

   public static class StringListIO extends ValueIO {
      public StringListIO() {
         this.description = "list of strings (value1, value2...)";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] segments = value.toLowerCase().split(",");
         List<String> result = new ArrayList<>();

         for (String s : segments) {
            s = s.trim();
            if (s.length() > 0) {
               result.add(s);
            }
         }

         field.set(targetClass, result);
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         List<String> values = (List<String>)rawValue;
         String result = "";

         for (String value : values) {
            if (result.length() > 0) {
               result = result + ",";
            }

            result = result + value;
         }

         return createListFromValue(result);
      }
   }

   public static class StringNumberAddIO extends ValueIO {
      public StringNumberAddIO() {
         this.description = "string and integer: 'test,12'";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] temp2 = value.toLowerCase().split(",");
         if (temp2.length != 2) {
            throw new MillLog.MillenaireException("Invalid parameter. Must take the form: 'value,number'.");
         } else {
            ((Map)field.get(targetClass)).put(temp2[0], Integer.parseInt(temp2[1]));
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         Map<String, Integer> map = (Map<String, Integer>)rawValue;
         List<String> results = new ArrayList<>();
         List<String> keys = new ArrayList<>(map.keySet());
         Collections.sort(keys);

         for (String key : keys) {
            results.add(key + "," + map.get(key));
         }

         return results;
      }
   }

   public static class ToolCategoriesIO extends ValueIO {
      public ToolCategoriesIO() {
         this.description = "A tool category to require, from: meleeweapons, rangedweapons, armour, pickaxes, axes, shovels and hoes.";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         List<String> categories = (List<String>)field.get(targetClass);
         if (value.equalsIgnoreCase("meleeweapons")) {
            MillLog.warning(
               targetClass,
               "Usage of 'meleeweapons' tool class is discouraged (it makes villagers gather any tools for use as weapons). Use 'toolssword' instead."
            );
            categories.add(VillagerConfig.CATEGORY_WEAPONSHANDTOHAND);
         } else if (value.equalsIgnoreCase("toolssword")) {
            categories.add(VillagerConfig.CATEGORY_TOOLSSWORD);
         } else if (value.equalsIgnoreCase("rangedweapons")) {
            categories.add(VillagerConfig.CATEGORY_WEAPONSRANGED);
         } else if (value.equalsIgnoreCase("armour")) {
            categories.add(VillagerConfig.CATEGORY_ARMOURSBOOTS);
            categories.add(VillagerConfig.CATEGORY_ARMOURSCHESTPLATE);
            categories.add(VillagerConfig.CATEGORY_ARMOURSHELMET);
            categories.add(VillagerConfig.CATEGORY_ARMOURSLEGGINGS);
         } else if (value.equalsIgnoreCase("pickaxes")) {
            categories.add(VillagerConfig.CATEGORY_TOOLSPICKAXE);
         } else if (value.equalsIgnoreCase("axes")) {
            categories.add(VillagerConfig.CATEGORY_TOOLSAXE);
         } else if (value.equalsIgnoreCase("shovels")) {
            categories.add(VillagerConfig.CATEGORY_TOOLSSHOVEL);
         } else if (value.equalsIgnoreCase("hoes")) {
            categories.add(VillagerConfig.CATEGORY_TOOLSHOE);
         } else {
            MillLog.error(null, "Unknown tool class found: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         List<String> categories = (List<String>)rawValue;
         List<String> results = new ArrayList<>();

         for (String category : categories) {
            if (category.equalsIgnoreCase(VillagerConfig.CATEGORY_WEAPONSHANDTOHAND)) {
               results.add("meleeweapons");
            } else if (category.equalsIgnoreCase(VillagerConfig.CATEGORY_WEAPONSRANGED)) {
               results.add("rangedweapons");
            } else if (category.equalsIgnoreCase(VillagerConfig.CATEGORY_ARMOURSCHESTPLATE)) {
               results.add("armour");
            } else if (category.equalsIgnoreCase(VillagerConfig.CATEGORY_TOOLSPICKAXE)) {
               results.add("pickaxes");
            } else if (category.equalsIgnoreCase(VillagerConfig.CATEGORY_TOOLSAXE)) {
               results.add("axes");
            } else if (category.equalsIgnoreCase(VillagerConfig.CATEGORY_TOOLSSHOVEL)) {
               results.add("shovels");
            } else if (category.equalsIgnoreCase(VillagerConfig.CATEGORY_TOOLSHOE)) {
               results.add("hoes");
            }
         }

         return results;
      }
   }

   public static class TranslatedStringAddIO extends ValueIO {
      public TranslatedStringAddIO() {
         this.description = "translated string, with the format: 'fr,ferme'";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         String[] temp2 = value.toLowerCase().split(",");
         if (temp2.length != 2) {
            throw new MillLog.MillenaireException("Translated strings must take the form of language,string. Ex: fr,ferme.");
         } else {
            ((Map)field.get(targetClass)).put(temp2[0].toLowerCase().trim(), temp2[1]);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         Map<String, String> map = (Map<String, String>)rawValue;
         List<String> results = new ArrayList<>();
         List<String> keys = new ArrayList<>(map.keySet());
         Collections.sort(keys);

         for (String key : keys) {
            results.add(key + "," + map.get(key));
         }

         return results;
      }
   }

   public static class VillagerConfigIO extends ValueIO {
      public VillagerConfigIO() {
         this.description = "A villager config (from millenaire/villagerconfig).";
      }

      @Override
      public void readValue(Object targetClass, Field field, String value) throws Exception {
         if (VillagerConfig.villagerConfigs.containsKey(value.toLowerCase())) {
            field.set(targetClass, VillagerConfig.villagerConfigs.get(value.toLowerCase()));
         } else {
            throw new MillLog.MillenaireException("Unknown villager config: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         VillagerConfig config = (VillagerConfig)rawValue;
         return createListFromValue(config.key);
      }
   }
}
