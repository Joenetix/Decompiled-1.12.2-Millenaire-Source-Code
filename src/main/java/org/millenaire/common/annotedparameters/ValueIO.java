package org.millenaire.common.annotedparameters;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.millenaire.common.utilities.BlockStateUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.goal.Goal;
import org.millenaire.entities.VillagerConfig;

/**
 * Base class for reading and writing configuration values.
 * Each inner class handles a specific type of value (string, int, item, etc.)
 * Ported from 1.12.2 to 1.20.1.
 */
public abstract class ValueIO {
    public String description;

    protected static List<String> createListFromValue(String value) {
        List<String> list = new ArrayList<>();
        list.add(value);
        return list;
    }

    public abstract void readValue(Object targetClass, Field field, String value) throws Exception;

    public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
        MillLog.error(this, "Trying to use readValueCulture but it is not implemented.");
    }

    public boolean skipWritingValue(Object value) {
        return false;
    }

    public boolean useCulture() {
        return false;
    }

    public abstract List<String> writeValue(Object rawValue) throws Exception;

    // === STRING I/O ===

    public static class StringIO extends ValueIO {
        public StringIO() {
            this.description = "text value";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            field.set(targetClass, value.trim());
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            return createListFromValue((String) rawValue);
        }
    }

    public static class StringDisplayIO extends ValueIO {
        public StringDisplayIO() {
            this.description = "display text value";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            field.set(targetClass, value.trim());
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            return createListFromValue((String) rawValue);
        }
    }

    public static class StringListIO extends ValueIO {
        public StringListIO() {
            this.description = "list of text values";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            List<String> list = (List<String>) field.get(targetClass);
            if (list == null) {
                list = new ArrayList<>();
                field.set(targetClass, list);
            }
            for (String s : value.split(",")) {
                list.add(s.trim().toLowerCase());
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            return (List<String>) rawValue;
        }
    }

    public static class StringAddIO extends ValueIO {
        public StringAddIO() {
            this.description = "text value (multiple lines allowed)";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            ((List<String>) field.get(targetClass)).add(value.toLowerCase().trim());
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            return (List<String>) rawValue;
        }
    }

    public static class StringCaseSensitiveAddIO extends ValueIO {
        public StringCaseSensitiveAddIO() {
            this.description = "text value, case-sensitive (multiple lines allowed)";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            ((List<String>) field.get(targetClass)).add(value.trim());
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            return (List<String>) rawValue;
        }
    }

    public static class StringNumberAddIO extends ValueIO {
        public StringNumberAddIO() {
            this.description = "text and number: 'key,value'";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String[] parts = value.split(",");
            if (parts.length >= 2) {
                ((Map<String, Integer>) field.get(targetClass)).put(parts[0].toLowerCase().trim(),
                        Integer.parseInt(parts[1].trim()));
            } else {
                throw new MillLog.MillenaireException("Expected format: key,value but got: " + value);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            Map<String, Integer> map = (Map<String, Integer>) rawValue;
            List<String> results = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                results.add(entry.getKey() + "," + entry.getValue());
            }
            return results;
        }
    }

    public static class StringInvItemAddIO extends ValueIO {
        public StringInvItemAddIO() {
            this.description = "text and item: 'key,itemname'";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String[] parts = value.split(",");
            if (parts.length >= 2) {
                String key = parts[0].toLowerCase().trim();
                String itemName = parts[1].toLowerCase().trim();
                if (InvItem.INVITEMS_BY_NAME.containsKey(itemName)) {
                    ((Map<String, InvItem>) field.get(targetClass)).put(key, InvItem.INVITEMS_BY_NAME.get(itemName));
                } else {
                    throw new MillLog.MillenaireException("Unknown item: " + itemName);
                }
            } else {
                throw new MillLog.MillenaireException("Expected format: key,itemname but got: " + value);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            Map<String, InvItem> map = (Map<String, InvItem>) rawValue;
            List<String> results = new ArrayList<>();
            for (Map.Entry<String, InvItem> entry : map.entrySet()) {
                results.add(entry.getKey() + "," + entry.getValue().getKey());
            }
            return results;
        }
    }

    public static class TranslatedStringAddIO extends ValueIO {
        public TranslatedStringAddIO() {
            this.description = "translated text (multiple lines allowed)";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            ((List<String>) field.get(targetClass)).add(value.trim());
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            return (List<String>) rawValue;
        }
    }

    // === PRIMITIVE I/O ===

    public static class BooleanIO extends ValueIO {
        public BooleanIO() {
            this.description = "boolean";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            field.set(targetClass, Boolean.parseBoolean(value.trim()));
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            Boolean value = (Boolean) rawValue;
            return createListFromValue(value ? "true" : "false");
        }
    }

    public static class BrickColourThemeAddIO extends ValueIO {
        public BrickColourThemeAddIO() {
            this.description = "Example: 'rajput:30;brown:50,red:40,orange:30;yellow:30'";
        }

        private String getAllColourNames() {
            String colours = "";
            for (DyeColor color : DyeColor.values()) {
                colours = colours + color.getName() + " ";
            }
            return colours;
        }

        private DyeColor getColourByName(String colourName) {
            for (DyeColor color : DyeColor.values()) {
                if (color.getName().equals(colourName)) {
                    return color;
                }
            }
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String themeDefinition = value.split(";")[0];
            String themeKey = themeDefinition.split(":")[0];
            int themeWeight = Integer.parseInt(themeDefinition.split(":")[1]);
            Map<DyeColor, Map<DyeColor, Integer>> themesMapping = new HashMap<>();
            Map<DyeColor, Integer> otherMapping = null;

            for (int i = 1; i < value.split(";").length; i++) {
                String themeData = value.split(";")[i];
                String key = themeData.split(":")[0];
                String possibleValues = themeData.substring(key.length() + 1);
                Map<DyeColor, Integer> values = new HashMap<>();

                for (String weightedColour : possibleValues.split(",")) {
                    String colourName = weightedColour.split(":")[0];
                    DyeColor colour = this.getColourByName(colourName);
                    if (colour == null) {
                        throw new MillLog.MillenaireException("Unknown colour: " + colourName + ". It should be among: "
                                + this.getAllColourNames());
                    }

                    int weight = Integer.parseInt(weightedColour.split(":")[1]);
                    values.put(colour, weight);
                }

                if (key.equals("other")) {
                    otherMapping = values;
                } else {
                    DyeColor inputColour = this.getColourByName(key);
                    if (inputColour == null) {
                        throw new MillLog.MillenaireException(
                                "Unknown colour: " + key + ". It should be among: " + this.getAllColourNames());
                    }

                    themesMapping.put(inputColour, values);
                }
            }

            for (DyeColor inputColour : DyeColor.values()) {
                if (!themesMapping.containsKey(inputColour)) {
                    themesMapping.put(inputColour, otherMapping);
                }
            }

            List<VillageType.BrickColourTheme> themeList = (List<VillageType.BrickColourTheme>) field.get(targetClass);
            themeList.add(new VillageType.BrickColourTheme(themeKey, themeWeight, themesMapping));
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            List<VillageType.BrickColourTheme> themeList = (List<VillageType.BrickColourTheme>) rawValue;
            List<String> results = new ArrayList<>();

            for (VillageType.BrickColourTheme theme : themeList) {
                String line = theme.key + ":" + theme.weight;

                for (DyeColor inputColour : theme.colours.keySet()) {
                    line = line + ";" + inputColour.getName() + ":";
                    String values = "";

                    for (DyeColor outputColour : theme.colours.get(inputColour).keySet()) {
                        if (values.length() > 0) {
                            values = values + ",";
                        }
                        values = values + outputColour.getName() + ":"
                                + theme.colours.get(inputColour).get(outputColour);
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
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            Map<String, List<String>> map = (Map<String, List<String>>) field.get(targetClass);
            value = value.toLowerCase();
            if (value.split(",").length < 2) {
                MillLog.error(null,
                        "Two or three values are required for all clothes tag: either (cloth name, then texture file) or (cloth name, layer, then texture file).");
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
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            Map<String, List<String>> clothes = (Map<String, List<String>>) rawValue;
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

    public static class IntegerIO extends ValueIO {
        public IntegerIO() {
            this.description = "integer value";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            field.set(targetClass, Integer.parseInt(value.trim()));
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            return createListFromValue(String.valueOf(rawValue));
        }
    }

    public static class IntegerArrayIO extends ValueIO {
        public IntegerArrayIO() {
            this.description = "list of integers: '1,2,3'";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String[] segments = value.trim().split(",");
            if (segments[0].length() > 0) {
                int[] array = new int[segments.length];
                for (int i = 0; i < segments.length; i++) {
                    array[i] = Integer.parseInt(segments[i].trim());
                }
                field.set(targetClass, array);
            }
        }

        @Override
        public boolean skipWritingValue(Object value) {
            int[] ints = (int[]) value;
            return ints.length == 1 && ints[0] == 0;
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            int[] ints = (int[]) rawValue;
            StringBuilder result = new StringBuilder();
            for (int i : ints) {
                if (result.length() > 0)
                    result.append(",");
                result.append(i);
            }
            return createListFromValue(result.toString());
        }
    }

    public static class FloatIO extends ValueIO {
        public FloatIO() {
            this.description = "floating point value";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            field.set(targetClass, Float.parseFloat(value.trim()));
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            return createListFromValue(String.valueOf(rawValue));
        }
    }

    public static class MillisecondsIO extends ValueIO {
        public MillisecondsIO() {
            this.description = "milliseconds";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            field.set(targetClass, Integer.parseInt(value.trim()) * 20 / 1000);
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            int value = (Integer) rawValue;
            return createListFromValue(String.valueOf(value * 1000 / 20));
        }
    }

    // === RESOURCE/BLOCK I/O ===

    public static class ResourceLocationIO extends ValueIO {
        public ResourceLocationIO() {
            this.description = "Minecraft resource location ('minecraft:stone')";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            field.set(targetClass, new ResourceLocation(value.toLowerCase().trim()));
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            ResourceLocation value = (ResourceLocation) rawValue;
            return value == null ? null : createListFromValue(value.toString());
        }
    }

    public static class BlockIdIO extends ValueIO {
        public BlockIdIO() {
            this.description = "Minecraft ID of a block ('wheat')";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            ResourceLocation rl = new ResourceLocation(value.toLowerCase().trim());
            if (ForgeRegistries.BLOCKS.containsKey(rl)) {
                field.set(targetClass, rl);
            } else {
                throw new MillLog.MillenaireException("Unknown block: " + value);
            }
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            ResourceLocation value = (ResourceLocation) rawValue;
            return value == null ? null : createListFromValue(value.toString());
        }
    }

    public static class BlockStateIO extends ValueIO {
        public BlockStateIO() {
            this.description = "a Minecraft blockstate ('red_flower;type=blue_orchid')";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String[] params = value.split(";");
            ResourceLocation rl = new ResourceLocation(params[0].trim());
            Block block = ForgeRegistries.BLOCKS.getValue(rl);
            if (block == null) {
                throw new MillLog.MillenaireException("Unknown block: " + value);
            }
            if (params.length > 1) {
                field.set(targetClass,
                        BlockStateUtilities.getBlockStateWithValues(block.defaultBlockState(), params[1]));
            } else {
                field.set(targetClass, block.defaultBlockState());
            }
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            BlockState value = (BlockState) rawValue;
            return createListFromValue(BlockStateUtilities.getStringFromBlockState(value));
        }
    }

    public static class BlockStateAddIO extends ValueIO {
        public BlockStateAddIO() {
            this.description = "a Minecraft blockstate ('red_flower;type=blue_orchid') (multiple lines possible)";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String[] params = value.split(";");
            ResourceLocation rl = new ResourceLocation(params[0].trim());
            Block block = ForgeRegistries.BLOCKS.getValue(rl);
            if (block == null) {
                throw new MillLog.MillenaireException("Unknown block: " + value);
            }
            BlockState bs;
            if (params.length > 1) {
                bs = BlockStateUtilities.getBlockStateWithValues(block.defaultBlockState(), params[1]);
            } else {
                bs = block.defaultBlockState();
            }
            ((List<BlockState>) field.get(targetClass)).add(bs);
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            List<BlockState> blockStates = (List<BlockState>) rawValue;
            List<String> results = new ArrayList<>();
            for (BlockState bs : blockStates) {
                results.add(BlockStateUtilities.getStringFromBlockState(bs));
            }
            return results;
        }
    }

    public static class EntityIO extends ValueIO {
        public EntityIO() {
            this.description = "Minecraft ID of an entity ('cow')";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            ResourceLocation rl = new ResourceLocation(value.toLowerCase().trim());
            if (ForgeRegistries.ENTITY_TYPES.containsKey(rl)) {
                field.set(targetClass, rl);
            } else {
                throw new MillLog.MillenaireException("Unknown entity: " + value);
            }
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            ResourceLocation value = (ResourceLocation) rawValue;
            return createListFromValue(value.toString());
        }
    }

    // === ITEM I/O ===

    public static class InvItemIO extends ValueIO {
        public InvItemIO() {
            this.description = "item (from itemlist.txt)";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String key = value.toLowerCase().trim();
            if (InvItem.INVITEMS_BY_NAME.containsKey(key)) {
                field.set(targetClass, InvItem.INVITEMS_BY_NAME.get(key));
            } else {
                throw new MillLog.MillenaireException("Unknown item: " + value);
            }
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            InvItem value = (InvItem) rawValue;
            return createListFromValue(value.getKey());
        }
    }

    public static class InvItemAddIO extends ValueIO {
        public InvItemAddIO() {
            this.description = "an item ('chickenmeat') (multiple lines allowed)";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String key = value.toLowerCase().trim();
            if (InvItem.INVITEMS_BY_NAME.containsKey(key)) {
                ((List<InvItem>) field.get(targetClass)).add(InvItem.INVITEMS_BY_NAME.get(key));
            } else {
                throw new MillLog.MillenaireException("Unknown item: " + value);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            List<InvItem> invItems = (List<InvItem>) rawValue;
            List<String> results = new ArrayList<>();
            for (InvItem invItem : invItems) {
                results.add(invItem.getKey());
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
                throw new MillLog.MillenaireException(
                        "Item pairs must take the form of parameter=firstgood,secondgood.");
            }
            String key1 = temp2[0].trim();
            String key2 = temp2[1].trim();
            if (!InvItem.INVITEMS_BY_NAME.containsKey(key1) || !InvItem.INVITEMS_BY_NAME.containsKey(key2)) {
                throw new MillLog.MillenaireException("Unknown item: " + key1 + " or " + key2);
            }
            field.set(targetClass,
                    new InvItem[] { InvItem.INVITEMS_BY_NAME.get(key1), InvItem.INVITEMS_BY_NAME.get(key2) });
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            InvItem[] pair = (InvItem[]) rawValue;
            return createListFromValue(pair[0].getKey() + "," + pair[1].getKey());
        }
    }

    public static class InvItemNumberAddIO extends ValueIO {
        public InvItemNumberAddIO() {
            this.description = "item and number: 'bone,8'";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String[] temp2 = value.toLowerCase().split(",");
            if (temp2.length != 2) {
                throw new MillLog.MillenaireException(
                        "Invalid item quantity setting. They must take the form of parameter=goodname,quantity.");
            }
            String key = temp2[0].trim();
            if (InvItem.INVITEMS_BY_NAME.containsKey(key)) {
                ((Map<InvItem, Integer>) field.get(targetClass)).put(InvItem.INVITEMS_BY_NAME.get(key),
                        Integer.parseInt(temp2[1].trim()));
            } else {
                throw new MillLog.MillenaireException("Unknown item: " + key);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            Map<InvItem, Integer> invItems = (Map<InvItem, Integer>) rawValue;
            List<String> results = new ArrayList<>();
            List<InvItem> keys = new ArrayList<>(invItems.keySet());
            Collections.sort(keys);
            for (InvItem invItem : keys) {
                results.add(invItem.getKey() + "," + invItems.get(invItem));
            }
            return results;
        }
    }

    public static class InvItemPriceAddIO extends ValueIO {
        public InvItemPriceAddIO() {
            this.description = "item and price in the form of gold/silver/bronze deniers: 'bone,1/0/0'";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String[] temp2 = value.toLowerCase().split(",");
            if (temp2.length != 2) {
                throw new MillLog.MillenaireException(
                        "Invalid item price setting. They must take the form of parameter=goodname,price.");
            }
            String key = temp2[0].trim();
            if (InvItem.INVITEMS_BY_NAME.containsKey(key)) {
                int price = 0;
                String[] pricestr = temp2[1].split("/");
                if (pricestr.length == 1) {
                    price = Integer.parseInt(pricestr[0].trim());
                } else if (pricestr.length == 2) {
                    price = Integer.parseInt(pricestr[0].trim()) * 64 + Integer.parseInt(pricestr[1].trim());
                } else if (pricestr.length == 3) {
                    price = Integer.parseInt(pricestr[0].trim()) * 64 * 64 + Integer.parseInt(pricestr[1].trim()) * 64
                            + Integer.parseInt(pricestr[2].trim());
                } else {
                    MillLog.error(this, "Could not parse the price: " + value);
                }
                ((Map<InvItem, Integer>) field.get(targetClass)).put(InvItem.INVITEMS_BY_NAME.get(key), price);
            } else {
                throw new MillLog.MillenaireException("Unknown item: " + key);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            Map<InvItem, Integer> invItems = (Map<InvItem, Integer>) rawValue;
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
                String key = temp2[i].trim();
                if (!InvItem.INVITEMS_BY_NAME.containsKey(key)) {
                    throw new MillLog.MillenaireException("Unknown item: " + key);
                }
                itemsList[i] = InvItem.INVITEMS_BY_NAME.get(key).toStack();
                if (itemsList[i].isEmpty()) {
                    throw new MillLog.MillenaireException("Item list with null item: " + key);
                }
            }
            field.set(targetClass, itemsList);
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            ItemStack[] stacks = (ItemStack[]) rawValue;
            StringBuilder result = new StringBuilder();
            for (ItemStack stack : stacks) {
                if (result.length() > 0)
                    result.append(",");
                result.append(InvItem.createInvItem(stack).getKey());
            }
            return createListFromValue(result.toString());
        }
    }

    // === SPECIAL I/O ===

    public static class BonusItemAddIO extends ValueIO {
        public BonusItemAddIO() {
            this.description = "item, chance and (optional) required tag ('leather,50' or 'boudin,50,oven')";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String[] temp2 = value.toLowerCase().split(",");
            AnnotedParameter.BonusItem bonusItem;
            if (temp2.length != 3 && temp2.length != 2) {
                throw new MillLog.MillenaireException(
                        "bonusitem must take the form of bonusitem=goodname,chanceon100 or bonusitem=goodname,chanceon100,requiredtag");
            }
            String key = temp2[0].trim();
            if (InvItem.INVITEMS_BY_NAME.containsKey(key)) {
                if (temp2.length == 3) {
                    bonusItem = new AnnotedParameter.BonusItem(InvItem.INVITEMS_BY_NAME.get(key),
                            Integer.parseInt(temp2[1].trim()), temp2[2].trim());
                } else {
                    bonusItem = new AnnotedParameter.BonusItem(InvItem.INVITEMS_BY_NAME.get(key),
                            Integer.parseInt(temp2[1].trim()));
                }
                ((List<AnnotedParameter.BonusItem>) field.get(targetClass)).add(bonusItem);
            } else {
                throw new MillLog.MillenaireException("Unknown bonusitem item: " + key);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            List<AnnotedParameter.BonusItem> bonusItems = (List<AnnotedParameter.BonusItem>) rawValue;
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

    public static class StartingItemAddIO extends ValueIO {
        public StartingItemAddIO() {
            this.description = "item and number for starting inventory: 'bread,10'";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String[] temp2 = value.toLowerCase().split(",");
            if (temp2.length != 2) {
                throw new MillLog.MillenaireException("Starting items must be: itemname,quantity");
            }
            String key = temp2[0].trim();
            if (InvItem.INVITEMS_BY_NAME.containsKey(key)) {
                ((Map<InvItem, Integer>) field.get(targetClass)).put(InvItem.INVITEMS_BY_NAME.get(key),
                        Integer.parseInt(temp2[1].trim()));
            } else {
                throw new MillLog.MillenaireException("Unknown item: " + key);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            Map<InvItem, Integer> items = (Map<InvItem, Integer>) rawValue;
            List<String> results = new ArrayList<>();
            for (Map.Entry<InvItem, Integer> entry : items.entrySet()) {
                results.add(entry.getKey().getKey() + "," + entry.getValue());
            }
            return results;
        }
    }

    public static class PosTypeIO extends ValueIO {
        public PosTypeIO() {
            this.description = "Type of position point (sleeping, leasure, selling...)";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            AnnotedParameter.PosType posType = AnnotedParameter.PosType.getByType(value.toLowerCase().trim());
            if (posType == null) {
                throw new MillLog.MillenaireException("Unknown position type: " + value + ". It should be among: "
                        + AnnotedParameter.PosType.getAllCodes() + ".");
            }
            field.set(targetClass, posType);
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            AnnotedParameter.PosType value = (AnnotedParameter.PosType) rawValue;
            return createListFromValue(value.code);
        }
    }

    public static class RandomBrickColourAddIO extends ValueIO {
        public RandomBrickColourAddIO() {
            this.description = "Example: 'white;white:50,yellow:40,orange:30' means that white coloured bricks can turn into white, yellow or orange bricks, with weights of 50, 40 and 30 respectively.";
        }

        private String getAllColourNames() {
            String colours = "";
            for (DyeColor color : DyeColor.values()) {
                colours = colours + color.getName() + " ";
            }
            return colours;
        }

        private DyeColor getColourByName(String colourName) {
            for (DyeColor color : DyeColor.values()) {
                if (color.getName().equals(colourName)) {
                    return color;
                }
            }
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String mainColourName = value.split(";")[0];
            DyeColor mainColour = this.getColourByName(mainColourName);
            if (mainColour == null) {
                throw new MillLog.MillenaireException("Unknown colour: " + mainColourName + ". It should be among: "
                        + this.getAllColourNames());
            } else {
                String possibleValues = value.split(";")[1];
                Map<DyeColor, Integer> values = new HashMap<>();

                for (String weightedColour : possibleValues.split(",")) {
                    String colourName = weightedColour.split(":")[0];
                    DyeColor colour = this.getColourByName(colourName);
                    if (colour == null) {
                        throw new MillLog.MillenaireException("Unknown colour: " + colourName + ". It should be among: "
                                + this.getAllColourNames());
                    }

                    int weight = Integer.parseInt(weightedColour.split(":")[1]);
                    values.put(colour, weight);
                }

                Map<DyeColor, Map<DyeColor, Integer>> map = (Map<DyeColor, Map<DyeColor, Integer>>) field
                        .get(targetClass);
                map.put(mainColour, values);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            Map<DyeColor, Map<DyeColor, Integer>> map = (Map<DyeColor, Map<DyeColor, Integer>>) rawValue;
            List<String> results = new ArrayList<>();

            for (DyeColor inputColour : map.keySet()) {
                String line = inputColour.getName() + ";";
                String values = "";

                for (DyeColor outputColour : map.get(inputColour).keySet()) {
                    if (values.length() > 0) {
                        values = values + ",";
                    }
                    values = values + outputColour.getName() + ":" + map.get(inputColour).get(outputColour);
                }
                line = line + values;
                results.add(line);
            }
            return results;
        }
    }

    public static class GoalAddIO extends ValueIO {
        public GoalAddIO() {
            this.description = "Id of a goal ('construction', 'gopray'...)";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            if (Goal.goals.containsKey(value.toLowerCase().trim())) {
                ((List<Goal>) field.get(targetClass)).add(Goal.goals.get(value.toLowerCase().trim()));
            } else {
                MillLog.error(null, "Unknown goal: " + value);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            return (List<String>) rawValue;
        }
    }

    public static class ToolCategoriesIO extends ValueIO {
        public ToolCategoriesIO() {
            this.description = "tool categories: 'axe,pickaxe'";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            for (String cat : value.toLowerCase().split(",")) {
                ((List<String>) field.get(targetClass)).add(cat.trim());
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            return (List<String>) rawValue;
        }
    }

    public static class GenderIO extends ValueIO {
        public GenderIO() {
            this.description = "A gender, either 'male' or 'female'";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            if (value.trim().equalsIgnoreCase("male")) {
                field.set(targetClass, 1);
            } else if (value.trim().equalsIgnoreCase("female")) {
                field.set(targetClass, 2);
            } else {
                MillLog.error(null, "Unknown gender found: " + value);
            }
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            Integer value = (Integer) rawValue;
            return value == 1 ? createListFromValue("male") : createListFromValue("female");
        }
    }

    public static class DirectionIO extends ValueIO {
        public DirectionIO() {
            this.description = "A direction, such as 'east' or 'north'";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String v = value.trim().toLowerCase();
            switch (v) {
                case "north" -> field.set(targetClass, 0);
                case "west" -> field.set(targetClass, 1);
                case "south" -> field.set(targetClass, 2);
                case "east" -> field.set(targetClass, 3);
                default -> MillLog.error(null, "Unknown direction found: " + value);
            }
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            Integer value = (Integer) rawValue;
            return switch (value) {
                case 0 -> createListFromValue("north");
                case 1 -> createListFromValue("west");
                case 2 -> createListFromValue("south");
                case 3 -> createListFromValue("east");
                default -> new ArrayList<>();
            };
        }
    }

    public static class VillagerConfigIO extends ValueIO {
        public VillagerConfigIO() {
            this.description = "Villager configuration reference";
        }

        @Override
        public void readValue(Object targetClass, Field field, String value) throws Exception {
            String key = value.trim().toLowerCase();
            if (VillagerConfig.villagerConfigs.containsKey(key)) {
                field.set(targetClass, VillagerConfig.villagerConfigs.get(key));
            } else {
                throw new MillLog.MillenaireException("Unknown villager config: " + key);
            }
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            return createListFromValue((String) rawValue);
        }
    }
}
