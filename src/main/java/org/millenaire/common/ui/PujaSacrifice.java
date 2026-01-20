package org.millenaire.common.ui;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.core.MillItems;
import org.millenaire.common.utilities.LanguageUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a Puja (Hindu) or Sacrifice (Mayan) shrine inventory.
 * Handles offerings and enchantment rituals.
 */
public class PujaSacrifice implements Container {
    // Tool types for prayer targets
    public static final int TOOL = 1;
    public static final int ARMOUR = 2;
    public static final int HELMET = 3;
    public static final int BOOTS = 4;
    public static final int SWORD_AXE = 5;
    public static final int SWORD = 6;
    public static final int BOW = 7;
    public static final int UNBREAKABLE = 8;

    // Puja (Hindu) prayer targets - tools/armor enchantments
    public static PrayerTarget[] PUJA_TARGETS = new PrayerTarget[] {
            new PrayerTarget(Enchantments.UNBREAKING, "pujas.god0", 0, 188, 46, 188, TOOL),
            new PrayerTarget(Enchantments.BLOCK_EFFICIENCY, "pujas.god1", 0, 205, 46, 205, TOOL),
            new PrayerTarget(Enchantments.BLOCK_FORTUNE, "pujas.god2", 0, 222, 46, 222, TOOL),
            new PrayerTarget(Enchantments.SILK_TOUCH, "pujas.god3", 0, 239, 46, 239, TOOL)
    };

    // Mayan sacrifice prayer targets - various enchantments
    public static PrayerTarget[] MAYAN_TARGETS = new PrayerTarget[] {
            new PrayerTarget(Enchantments.SHARPNESS, "mayan.god0", 0, 188, 120, 188, ARMOUR),
            new PrayerTarget(Enchantments.SMITE, "mayan.god1", 20, 188, 140, 188, ARMOUR),
            new PrayerTarget(Enchantments.BANE_OF_ARTHROPODS, "mayan.god2", 40, 188, 160, 188, ARMOUR),
            new PrayerTarget(Enchantments.KNOCKBACK, "mayan.god3", 60, 188, 180, 188, ARMOUR),
            new PrayerTarget(Enchantments.FIRE_ASPECT, "mayan.god4", 80, 188, 200, 188, ARMOUR),
            new PrayerTarget(Enchantments.MOB_LOOTING, "mayan.god5", 100, 188, 120, 188, HELMET),
            new PrayerTarget(Enchantments.SWEEPING_EDGE, "mayan.god6", 0, 208, 120, 208, HELMET),
            new PrayerTarget(Enchantments.ALL_DAMAGE_PROTECTION, "mayan.god7", 20, 208, 140, 208, BOOTS),
            new PrayerTarget(Enchantments.FIRE_PROTECTION, "mayan.god8", 40, 208, 160, 208, SWORD_AXE),
            new PrayerTarget(Enchantments.FALL_PROTECTION, "mayan.god9", 0, 188, 120, 188, SWORD_AXE),
            new PrayerTarget(Enchantments.BLAST_PROTECTION, "mayan.god10", 80, 188, 200, 188, SWORD_AXE),
            new PrayerTarget(Enchantments.PROJECTILE_PROTECTION, "mayan.god11", 60, 208, 180, 208, SWORD),
            new PrayerTarget(Enchantments.RESPIRATION, "mayan.god12", 20, 188, 140, 188, SWORD),
            new PrayerTarget(Enchantments.AQUA_AFFINITY, "mayan.god13", 80, 208, 200, 208, SWORD),
            new PrayerTarget(Enchantments.THORNS, "mayan.god14", 40, 208, 160, 208, BOW),
            new PrayerTarget(Enchantments.DEPTH_STRIDER, "mayan.god15", 60, 208, 180, 208, BOW),
            new PrayerTarget(Enchantments.FROST_WALKER, "mayan.god16", 20, 188, 140, 188, BOW),
            new PrayerTarget(Enchantments.BINDING_CURSE, "mayan.god17", 80, 208, 200, 208, BOW),
            new PrayerTarget(Enchantments.BLOCK_EFFICIENCY, "mayan.god18", 100, 208, 220, 208, UNBREAKABLE)
    };

    public static int PUJA_DURATION = 30;
    public static final short PUJA = 0;
    public static final short MAYAN = 1;

    private NonNullList<ItemStack> items;
    public PrayerTarget currentTarget = null;
    public int offeringProgress = 0;
    public int offeringNeeded = 1;
    public short pujaProgress = 0;
    public Building temple = null;
    public MillVillager priest = null;
    public short type = 0;

    /**
     * Check if an item is valid for a specific tool type.
     */
    public static boolean validForItem(int type, Item item) {
        return switch (type) {
            case TOOL -> item instanceof ShovelItem || item instanceof AxeItem || item instanceof PickaxeItem;
            case ARMOUR -> item instanceof ArmorItem;
            case HELMET -> item instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.HEAD;
            case BOOTS -> item instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.FEET;
            case SWORD_AXE -> item instanceof SwordItem || item instanceof AxeItem;
            case SWORD -> item instanceof SwordItem;
            case BOW -> item instanceof BowItem;
            case UNBREAKABLE -> item instanceof SwordItem || item instanceof ArmorItem || item instanceof BowItem;
            default -> false;
        };
    }

    public PujaSacrifice(Building temple, CompoundTag tag) {
        this.temple = temple;
        if (temple.containsTags("sacrifices")) {
            this.type = MAYAN;
        }
        this.readFromNBT(tag);
    }

    public PujaSacrifice(Building temple, short type) {
        this.temple = temple;
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        this.type = type;
    }

    /**
     * Calculate the offerings needed for current enchantment.
     */
    public void calculateOfferingsNeeded() {
        this.offeringNeeded = 0;
        if (!this.items.get(4).isEmpty() && this.currentTarget != null) {
            ItemStack tool = this.items.get(4);
            int currentLevel = EnchantmentHelper.getItemEnchantmentLevel(this.currentTarget.enchantment, tool);

            if (currentLevel < this.currentTarget.enchantment.getMaxLevel()) {
                if (this.currentTarget.enchantment.canEnchant(tool)) {
                    int nbother = 0;

                    if (tool.isEnchanted()) {
                        Map<Enchantment, Integer> existingEnchantments = EnchantmentHelper.getEnchantments(tool);
                        nbother = existingEnchantments.size();

                        for (Enchantment enchId : existingEnchantments.keySet()) {
                            if (enchId != this.currentTarget.enchantment
                                    && !enchId.isCompatibleWith(this.currentTarget.enchantment)) {
                                return; // Incompatible enchantment
                            }
                        }
                    }

                    if (currentLevel > 0) {
                        nbother--;
                    }

                    int cost = 50 + this.currentTarget.enchantment.getMinCost(currentLevel + 1) * 10;
                    cost *= nbother / 2 + 1;

                    if (false) {
                        MillLog.minor(this, "Offering needed: " + cost);
                    }

                    this.offeringNeeded = cost;
                }
            }
        }
    }

    public boolean canPray() {
        return this.offeringNeeded > this.offeringProgress && !this.items.get(0).isEmpty();
    }

    public void changeEnchantment(int i) {
        List<PrayerTarget> targets = getTargets();
        if (i >= 0 && i < targets.size() && this.currentTarget != targets.get(i)) {
            this.currentTarget = targets.get(i);
            this.offeringProgress = 0;
            this.calculateOfferingsNeeded();
        }
    }

    private void completeOffering() {
        int currentLevel = EnchantmentHelper.getItemEnchantmentLevel(this.currentTarget.enchantment, this.items.get(4));

        if (currentLevel == 0) {
            this.items.get(4).enchant(this.currentTarget.enchantment, 1);
        } else {
            // Upgrade existing enchantment
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(this.items.get(4));
            enchantments.put(this.currentTarget.enchantment, currentLevel + 1);
            EnchantmentHelper.setEnchantments(enchantments, this.items.get(4));
        }

        this.offeringProgress = 0;
        this.calculateOfferingsNeeded();
        this.temple.getTownHall().requestSave("Puja/sacrifice offering complete");
    }

    private void endPuja() {
        ItemStack offer = this.items.get(0);
        if (!offer.isEmpty()) {
            int offerValue = this.getOfferingValue(offer);
            this.offeringProgress += offerValue;
            offer.shrink(1);

            if (offer.isEmpty()) {
                this.items.set(0, ItemStack.EMPTY);
            }

            if (this.offeringProgress >= this.offeringNeeded) {
                this.completeOffering();
            }
        }
    }

    public int getOfferingProgressScaled(int scale) {
        return this.offeringNeeded == 0 ? 0 : this.offeringProgress * scale / this.offeringNeeded;
    }

    public int getOfferingValue(ItemStack is) {
        if (this.type == PUJA) {
            return this.getOfferingValuePuja(is);
        } else if (this.type == MAYAN) {
            return this.getOfferingValueMayan(is);
        }
        return 0;
    }

    public int getOfferingValueMayan(ItemStack is) {
        Item item = is.getItem();
        if (item == Items.NETHER_STAR)
            return 4096;
        if (item == Items.GHAST_TEAR)
            return 384;
        if (item == Items.BLAZE_ROD)
            return 64;
        if (item == MillItems.CACAUHAA.get())
            return 64;
        if (item == Items.PORKCHOP)
            return 1;
        if (item == Items.BEEF)
            return 1;
        if (item == Items.CHICKEN)
            return 1;
        if (item == Items.RABBIT)
            return 1;
        if (item == Items.MUTTON)
            return 1;
        if (item == Items.COD)
            return 1;
        if (item == Items.SALMON)
            return 1;
        if (item == Items.ROTTEN_FLESH)
            return 2;
        if (item == Items.SPIDER_EYE)
            return 2;
        if (item == Items.ENDER_PEARL)
            return 4;
        if (item == Items.BONE)
            return 4;
        if (item == Items.BLAZE_POWDER)
            return 4;
        if (item == Items.GUNPOWDER)
            return 6;
        return 0;
    }

    public int getOfferingValuePuja(ItemStack is) {
        Item item = is.getItem();
        if (item == Items.DIAMOND)
            return 384;
        if (item == Items.EMERALD)
            return 128;
        if (item == Items.LAPIS_LAZULI)
            return 96;
        if (item == Items.GOLD_INGOT)
            return 64;
        if (item == MillItems.RICE.get())
            return 8;
        if (item == MillItems.RASGULLA.get())
            return 64;
        if (item == Items.DANDELION || item == Items.POPPY)
            return 16;
        if (item == Items.ROSE_BUSH || item == Items.APPLE)
            return 8;
        if (item == Items.WHITE_WOOL)
            return 8;
        if (item == Items.MILK_BUCKET)
            return 4;
        return 0;
    }

    public int getPujaProgressScaled(int scale) {
        return this.pujaProgress * scale / PUJA_DURATION;
    }

    public List<PrayerTarget> getTargets() {
        if (this.items.get(4).isEmpty()) {
            return new ArrayList<>();
        }

        PrayerTarget[] targets = (this.type == PUJA) ? PUJA_TARGETS : MAYAN_TARGETS;
        List<PrayerTarget> validTargets = new ArrayList<>();

        for (PrayerTarget t : targets) {
            if (t.validForItem(this.items.get(4).getItem())) {
                validTargets.add(t);
            }
        }

        return validTargets;
    }

    public boolean isActive() {
        return this.pujaProgress > 0;
    }

    public boolean performPuja(MillVillager priest) {
        this.priest = priest;

        if (this.pujaProgress == 0) {
            boolean success = this.startPuja();
            if (success) {
                this.pujaProgress = 1;
            }
            return success;
        } else if (this.pujaProgress >= PUJA_DURATION) {
            this.endPuja();
            this.pujaProgress = 0;
            return this.canPray();
        } else {
            this.pujaProgress++;
            return this.canPray();
        }
    }

    private boolean startPuja() {
        int money = MillCommonUtilities.countMoney(this);
        if (money == 0)
            return false;
        if (this.offeringNeeded == 0 || this.offeringProgress >= this.offeringNeeded)
            return false;
        if (this.items.get(0).isEmpty())
            return false;

        money -= 8;
        int denier = money % 64;
        int denier_argent = (money - denier) / 64 % 64;
        int denier_or = (money - denier - denier_argent * 64) / 4096;

        this.items.set(1, denier == 0 ? ItemStack.EMPTY : new ItemStack(MillItems.DENIER.get(), denier));
        this.items.set(2,
                denier_argent == 0 ? ItemStack.EMPTY : new ItemStack(MillItems.DENIER_ARGENT.get(), denier_argent));
        this.items.set(3, denier_or == 0 ? ItemStack.EMPTY : new ItemStack(MillItems.DENIER_OR.get(), denier_or));

        return true;
    }

    public void readFromNBT(CompoundTag tag) {
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);

        int enchId = tag.getShort("enchantmentTarget");
        Enchantment ench = Enchantment.byId(enchId);
        if (ench != null) {
            for (PrayerTarget t : this.getTargets()) {
                if (t.enchantment == ench) {
                    this.currentTarget = t;
                    break;
                }
            }
        }

        if (false) {
            MillLog.minor(this, "Reading enchantmentTarget: " + ench + ", " + this.currentTarget);
        }

        this.offeringProgress = tag.getShort("offeringProgress");
        this.pujaProgress = tag.getShort("pujaProgress");
        this.calculateOfferingsNeeded();
    }

    public void writeToNBT(CompoundTag tag) {
        if (this.currentTarget != null) {
            tag.putShort("enchantmentTarget", (short) net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT
                    .getId(this.currentTarget.enchantment));
            if (false) {
                MillLog.minor(this, "Writing enchantmentTarget: " + this.currentTarget.enchantment);
            }
        }

        tag.putShort("offeringProgress", (short) this.offeringProgress);
        tag.putShort("pujaProgress", this.pujaProgress);
        ContainerHelper.saveAllItems(tag, this.items);
    }

    // Container implementation
    @Override
    public int getContainerSize() {
        return 5;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(this.items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        // Mark dirty
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    public Component getName() {
        return Component.translatable(LanguageUtilities.string("pujas.invanme"));
    }

    /**
     * Represents a prayer target (enchantment) for the shrine.
     */
    public static class PrayerTarget {
        public final Enchantment enchantment;
        public final String mouseOver;
        public final int startX;
        public final int startY;
        public final int startXact;
        public final int startYact;
        public final int toolType;

        public PrayerTarget(Enchantment enchantment, String mouseOver, int startX, int startY,
                int startXact, int startYact, int toolType) {
            this.enchantment = enchantment;
            this.mouseOver = mouseOver;
            this.startX = startX;
            this.startY = startY;
            this.startXact = startXact;
            this.startYact = startYact;
            this.toolType = toolType;
        }

        public boolean validForItem(Item item) {
            return PujaSacrifice.validForItem(this.toolType, item);
        }
    }
}
