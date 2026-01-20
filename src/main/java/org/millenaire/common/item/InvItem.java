package org.millenaire.common.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.millenaire.common.utilities.MillLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InvItem implements Comparable<InvItem> {
    private static Map<Integer, InvItem> CACHE = new HashMap<>();
    public static final int ANYENCHANTED = 1;
    public static final int ENCHANTEDSWORD = 2;
    public static final List<InvItem> freeGoods = new ArrayList<>();
    public static final HashMap<String, InvItem> INVITEMS_BY_NAME = new HashMap<>();

    public final Item item;
    public final Block block;
    public final ItemStack staticStack;
    public final ItemStack[] staticStackArray;
    public final int meta;
    public final int special;
    private String key = null;

    private static int computeHash(Item item, int meta, int special) {
        return item == null ? (meta << 8) + (special << 12) : item.hashCode() + (meta << 8) + (special << 12);
    }

    public static InvItem createInvItem(Block block) {
        return createInvItem(block, 0);
    }

    public static InvItem createInvItem(Block block, int meta) {
        Item item = Item.byBlock(block);
        int hash = computeHash(item, meta, 0);
        if (CACHE.containsKey(hash)) {
            return CACHE.get(hash);
        }
        InvItem ii = new InvItem(block, meta);
        CACHE.put(hash, ii);
        return ii;
    }

    public static InvItem createInvItem(BlockState bs) {
        return createInvItem(bs.getBlock(), 0);
    }

    public static InvItem createInvItem(int special) {
        int hash = computeHash(null, 0, special);
        if (CACHE.containsKey(hash)) {
            return CACHE.get(hash);
        }
        InvItem ii = new InvItem(special);
        CACHE.put(hash, ii);
        return ii;
    }

    public static InvItem createInvItem(Item item) {
        return createInvItem(item, 0);
    }

    public static InvItem createInvItem(Item item, int meta) {
        int hash = computeHash(item, meta, 0);
        if (CACHE.containsKey(hash)) {
            return CACHE.get(hash);
        }
        InvItem ii = new InvItem(item, meta);
        CACHE.put(hash, ii);
        return ii;
    }

    public static InvItem createInvItem(ItemStack is) {
        return createInvItem(is.getItem(), 0);
    }

    private InvItem(Block block, int meta) {
        this.block = block;
        this.item = Item.byBlock(block);
        this.meta = meta;
        this.staticStack = new ItemStack(this.item, 1);
        this.staticStackArray = new ItemStack[] { this.staticStack };
        this.special = 0;
    }

    private InvItem(int special) {
        this.special = special;
        this.staticStack = ItemStack.EMPTY;
        this.staticStackArray = new ItemStack[] { this.staticStack };
        this.item = null;
        this.block = null;
        this.meta = 0;
    }

    private InvItem(Item item, int meta) {
        this.item = item;
        this.block = Block.byItem(item);
        this.meta = meta;
        this.staticStack = new ItemStack(item, 1);
        this.staticStackArray = new ItemStack[] { this.staticStack };
        this.special = 0;
    }

    @Override
    public int compareTo(InvItem ii) {
        if (this.special > 0 || ii.special > 0) {
            return this.special - ii.special;
        } else {
            // Rough comparison
            return this.item != null && ii.item != null
                    ? BuiltInRegistries.ITEM.getKey(this.item).compareTo(BuiltInRegistries.ITEM.getKey(ii.item))
                            + this.meta - ii.meta
                    : this.special - ii.special;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof InvItem)) {
            return false;
        } else {
            InvItem other = (InvItem) obj;
            return other.item == this.item && other.meta == this.meta && other.special == this.special;
        }
    }

    public Item getItem() {
        return this.item;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        if (item != null) {
            return item.getDescriptionId();
        }
        return "InvItem";
    }

    public ItemStack getItemStack() {
        return staticStack;
    }

    public String getKey() {
        return key;
    }

    public ItemStack toStack() {
        return getItemStack();
    }

    public ItemStack toItemStack(int count) {
        ItemStack stack = getItemStack().copy();
        stack.setCount(count);
        return stack;
    }

    public boolean matches(ItemStack stack) {
        return createInvItem(stack).equals(this);
    }

    @Override
    public int hashCode() {
        return computeHash(this.item, this.meta, this.special);
    }

    @Override
    public String toString() {
        return (item != null ? BuiltInRegistries.ITEM.getKey(item).toString() : "null") + "/" + meta;
    }

    // Static init for freeGoods
    static {
        freeGoods.add(createInvItem(Blocks.DIRT, 0));
        freeGoods.add(createInvItem(Blocks.COBBLESTONE, 0));
        freeGoods.add(createInvItem(Blocks.GRAVEL, 0));
        freeGoods.add(createInvItem(Blocks.SAND, 0));
        // Add more as needed
    }
}
