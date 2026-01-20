package org.millenaire.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.millenaire.core.MillMenus;
import org.millenaire.core.MillItems;
import org.millenaire.entities.Citizen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuTrade extends AbstractContainerMenu {
    public final Citizen citizen;

    // Data storage
    public static class TradeEntry {
        public final Item item;
        public final int price;
        public final boolean selling; // true = village selling (player buying)

        public TradeEntry(Item item, int price, boolean selling) {
            this.item = item;
            this.price = price;
            this.selling = selling;
        }
    }

    // Lists of all available trades
    private final List<TradeEntry> sellingTrades = new ArrayList<>();
    private final List<TradeEntry> buyingTrades = new ArrayList<>();

    // Visual slots (Fixed positions)
    private final List<TradeSlot> sellingSlots = new ArrayList<>();
    private final List<TradeSlot> buyingSlots = new ArrayList<>();

    // Scroll state
    public int nbRowSelling = 0;
    public int nbRowBuying = 0;

    // Client Constructor
    public MenuTrade(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, (Citizen) playerInv.player.level().getEntity(extraData.readInt()));
    }

    // Server Constructor
    public MenuTrade(int containerId, Inventory playerInv, Citizen citizen) {
        super(MillMenus.TRADE.get(), containerId);
        this.citizen = citizen;

        // 1. Load Data (Selling)
        if (citizen.getVillage() != null && citizen.getVillage().getVillageType() != null) {
            Map<String, Float> selling = citizen.getVillage().getVillageType().getSellingPrices();
            for (Map.Entry<String, Float> entry : selling.entrySet()) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getKey()));
                if (item != null) {
                    sellingTrades.add(new TradeEntry(item, entry.getValue().intValue(), true));
                }
            }

            // Buying
            Map<String, Float> buying = citizen.getVillage().getVillageType().getBuyingPrices();
            for (Map.Entry<String, Float> entry : buying.entrySet()) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getKey()));
                if (item != null) {
                    buyingTrades.add(new TradeEntry(item, entry.getValue().intValue(), false));
                }
            }
        }

        this.nbRowSelling = (sellingTrades.size() + 12) / 13;
        this.nbRowBuying = (buyingTrades.size() + 12) / 13;

        // 2. Create Fixed Slots
        // Selling Rows (2 rows of 13) at Y=32
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 13; col++) {
                TradeSlot slot = new TradeSlot(this, 8 + col * 18, 32 + row * 18);
                this.addSlot(slot);
                this.sellingSlots.add(slot);
            }
        }

        // Buying Rows (2 rows of 13) at Y=86
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 13; col++) {
                TradeSlot slot = new TradeSlot(this, 8 + col * 18, 86 + row * 18);
                this.addSlot(slot);
                this.buyingSlots.add(slot);
            }
        }

        // Player Inventory
        layoutPlayerInventory(playerInv, 8, 140);

        // Initial Update
        updateScroll(true, 0);
        updateScroll(false, 0);
    }

    private void layoutPlayerInventory(Inventory playerInv, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, x + col * 18, y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, x + col * 18, y + 58)); // Relative to Y=140
        }
    }

    // Assign entries to slots based on scroll
    public void updateScroll(boolean selling, int row) {
        List<TradeEntry> sourceList = selling ? sellingTrades : buyingTrades;
        List<TradeSlot> targetSlots = selling ? sellingSlots : buyingSlots;

        int startIndex = row * 13;

        for (int i = 0; i < targetSlots.size(); i++) {
            TradeSlot slot = targetSlots.get(i);
            int dataIndex = startIndex + i;

            if (dataIndex < sourceList.size()) {
                slot.setEntry(sourceList.get(dataIndex));
            } else {
                slot.setEntry(null);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return citizen.isAlive() && citizen.distanceTo(player) < 8.0f;
    }

    public Citizen getCitizen() {
        return citizen;
    }

    @Override
    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot instanceof TradeSlot) {
                TradeSlot ts = (TradeSlot) slot;
                if (ts.entry != null) {
                    if (!player.level().isClientSide) {
                        handleTrade(ts, player);
                    }
                }
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void handleTrade(TradeSlot slot, Player player) {
        if (slot.entry == null)
            return;
        TradeEntry entry = slot.entry;
        int amount = 1;

        if (entry.selling) {
            // Buying from Village
            int cost = entry.price;
            int playerMoney = countItem(player, MillItems.DENIER.get());

            if (playerMoney >= cost) {
                ItemStack stackToBuy = new ItemStack(entry.item, amount);
                if (player.getInventory().add(stackToBuy)) {
                    removeItem(player, MillItems.DENIER.get(), cost);
                }
            }
        } else {
            // Selling to Village
            int count = countItem(player, entry.item);
            if (count >= amount) {
                removeItem(player, entry.item, amount);
                ItemStack money = new ItemStack(MillItems.DENIER.get(), entry.price);
                if (!player.getInventory().add(money)) {
                    player.drop(money, false);
                }
            }
        }
    }

    private int countItem(Player player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void removeItem(Player player, Item item, int amountToRemove) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (amountToRemove <= 0)
                break;
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                int shrink = Math.min(amountToRemove, stack.getCount());
                stack.shrink(shrink);
                amountToRemove -= shrink;
            }
        }
    }

    public static class TradeSlot extends Slot {
        public TradeEntry entry;

        public TradeSlot(MenuTrade container, int x, int y) {
            super(new net.minecraft.world.SimpleContainer(1), 0, x, y);
        }

        public void setEntry(TradeEntry entry) {
            this.entry = entry;
        }

        @Override
        public ItemStack getItem() {
            return entry != null ? new ItemStack(entry.item) : ItemStack.EMPTY;
        }

        @Override
        public boolean hasItem() {
            return entry != null;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
