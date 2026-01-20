package org.millenaire.common.utilities;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStateUtilities {
    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);

    private static Map<Property<?>, Comparable<?>> getBlockStatePropertyValueMap(Block block, String values) {
        Map<Property<?>, Comparable<?>> map = Maps.newHashMap();
        if ("default".equals(values)) {
            return block.defaultBlockState().getValues();
        } else {
            StateDefinition<Block, BlockState> container = block.getStateDefinition();
            Iterator<String> iterator = COMMA_SPLITTER.split(values).iterator();

            while (iterator.hasNext()) {
                String s = iterator.next();
                Iterator<String> iterator1 = EQUAL_SPLITTER.split(s).iterator();
                if (!iterator1.hasNext()) {
                    break;
                }

                Property<?> property = container.getProperty(iterator1.next());
                if (property == null || !iterator1.hasNext()) {
                    break;
                }

                Comparable<?> comparable = getValueHelper(property, iterator1.next());
                if (comparable == null) {
                    break;
                }

                map.put(property, comparable);
            }

            return map;
        }
    }

    private static <T extends Comparable<T>> BlockState getBlockStateWithProperty(BlockState blockState,
            Property<T> property, Comparable<?> value) {
        return blockState.setValue(property, (T) value);
    }

    public static BlockState getBlockStateWithValues(BlockState blockState, String values) {
        Map<Property<?>, Comparable<?>> properties = getBlockStatePropertyValueMap(blockState.getBlock(), values);
        if (properties == null || properties.isEmpty()) {
            // Fallback or error logging
            // MillLog.error(null, "Could not parse values line of " + values + " for block
            // " + blockState.getBlock());
        } else {
            for (Entry<Property<?>, Comparable<?>> entry : properties.entrySet()) {
                blockState = getBlockStateWithProperty(blockState, (Property) entry.getKey(), entry.getValue());
            }
        }
        return blockState;
    }

    public static Comparable getPropertyValueByName(BlockState blockState, String propertyName) {
        Property<?> property = blockState.getBlock().getStateDefinition().getProperty(propertyName);
        if (property != null) {
            return blockState.getValue(property);
        } else {
            return null;
        }
    }

    public static String getStringFromBlockState(BlockState blockState) {
        StringBuilder properties = new StringBuilder();

        for (Property<?> property : blockState.getProperties()) {
            if (properties.length() > 0) {
                properties.append(",");
            }
            properties.append(property.getName()).append("=").append(blockState.getValue(property));
        }

        return MillCommonUtilities.getBlockName(blockState.getBlock()) + ";" + properties.toString();
    }

    @Nullable
    private static <T extends Comparable<T>> T getValueHelper(Property<T> property, String value) {
        Optional<T> opt = property.getValue(value);
        return opt.orElse(null);
    }

    public static boolean hasPropertyByName(BlockState blockState, String propertyName) {
        return blockState.getBlock().getStateDefinition().getProperty(propertyName) != null;
    }

    public static BlockState setPropertyValueByName(BlockState blockState, String propertyName, Comparable value) {
        Property property = blockState.getBlock().getStateDefinition().getProperty(propertyName);
        if (property != null) {
            return blockState.setValue(property, value);
        } else {
            return null;
        }
    }
}
