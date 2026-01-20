package org.millenaire.resources;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages a collection of resources (items and quantities).
 * Used for village storage, building requirements, etc.
 */
public class ResourceInventory {
    private final Map<Item, Integer> resources = new HashMap<>();

    /**
     * Add resources to inventory
     */
    public void add(Item item, int amount) {
        resources.merge(item, amount, Integer::sum);
    }

    /**
     * Remove resources from inventory
     */
    public void remove(Item item, int amount) {
        resources.computeIfPresent(item, (k, v) -> Math.max(0, v - amount));
    }

    /**
     * Check if inventory has at least this amount of an item
     */
    public boolean has(Item item, int amount) {
        return resources.getOrDefault(item, 0) >= amount;
    }

    /**
     * Check if inventory has all resources from another inventory
     */
    public boolean hasAll(ResourceInventory other) {
        for (Map.Entry<Item, Integer> entry : other.resources.entrySet()) {
            if (!has(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add all resources from another inventory
     */
    public void addAll(ResourceInventory other) {
        for (Map.Entry<Item, Integer> entry : other.resources.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Remove all resources from another inventory
     */
    public void removeAll(ResourceInventory other) {
        for (Map.Entry<Item, Integer> entry : other.resources.entrySet()) {
            remove(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Get amount of specific item
     */
    public int getAmount(Item item) {
        return resources.getOrDefault(item, 0);
    }

    /**
     * Get all resources
     */
    public Map<Item, Integer> getAll() {
        return new HashMap<>(resources);
    }

    /**
     * Clear all resources
     */
    public void clear() {
        resources.clear();
    }

    /**
     * Save to NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag itemList = new ListTag();

        for (Map.Entry<Item, Integer> entry : resources.entrySet()) {
            CompoundTag itemTag = new CompoundTag();
            ItemStack stack = new ItemStack(entry.getKey(), entry.getValue());
            stack.save(itemTag);
            itemList.add(itemTag);
        }

        tag.put("Items", itemList);
        return tag;
    }

    /**
     * Load from NBT
     */
    public void load(CompoundTag tag) {
        resources.clear();
        ListTag itemList = tag.getList("Items", Tag.TAG_COMPOUND);

        for (Tag itemTag : itemList) {
            ItemStack stack = ItemStack.of((CompoundTag) itemTag);
            if (!stack.isEmpty()) {
                add(stack.getItem(), stack.getCount());
            }
        }
    }
}
