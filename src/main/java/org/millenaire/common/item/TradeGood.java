package org.millenaire.common.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.utilities.LanguageUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

public class TradeGood {
    public static final String HIDDEN = "hidden";
    public static final String FOREIGNTRADE = "foreigntrade";
    public Culture culture;
    public InvItem item;
    public String name;
    private final int sellingPrice;
    private final int buyingPrice;
    public int reservedQuantity;
    public int targetQuantity;
    public int foreignMerchantPrice;
    public String requiredTag;
    public boolean autoGenerate = false;
    public int minReputation;
    public String travelBookCategory = null;
    public final String key;
    public boolean travelBookDisplay = true;

    public TradeGood(String key, Culture culture, InvItem iv) {
        this.item = iv;
        this.name = this.item.getName();
        this.sellingPrice = 0;
        this.buyingPrice = 1;
        this.requiredTag = null;
        this.culture = culture;
        this.key = key;
    }

    public TradeGood(
            String key,
            Culture culture,
            String name,
            InvItem item,
            int sellingPrice,
            int buyingPrice,
            int reservedQuantity,
            int targetQuantity,
            int foreignMerchantPrice,
            boolean autoGenerate,
            String tag,
            int minReputation,
            String desc) {
        this.culture = culture;
        this.key = key;
        this.name = name;
        this.item = item;
        this.sellingPrice = sellingPrice;
        this.buyingPrice = buyingPrice;
        this.requiredTag = tag;
        this.autoGenerate = autoGenerate;
        this.reservedQuantity = reservedQuantity;
        this.targetQuantity = targetQuantity;
        this.foreignMerchantPrice = foreignMerchantPrice;
        this.minReputation = minReputation;
        this.travelBookCategory = desc;
        this.travelBookDisplay = this.travelBookCategory != null && !this.travelBookCategory.equals("hidden");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof TradeGood)) {
            return false;
        } else {
            TradeGood g = (TradeGood) obj;
            return g.item.equals(this.item);
        }
    }

    public int getBasicBuyingPrice(Building shop) {
        if (shop == null) {
            return this.buyingPrice;
        } else {
            // VillageType logic not yet fully ported/linked
            // return shop.getTownHall().villageType.buyingPrices.containsKey(this.item) ?
            // ...
            // Stub for now allowing compilation
            return this.buyingPrice;
        }
    }

    public int getBasicSellingPrice(Building shop) {
        if (shop == null) {
            return this.sellingPrice;
        } else {
            return this.sellingPrice;
        }
    }

    public int getCalculatedBuyingPrice(Building shop, Player player) {
        // shop.getBuyingPrice(this, player) needs to be implemented in Building
        return shop == null ? this.buyingPrice : this.buyingPrice; // Stub
    }

    public int getCalculatedSellingPrice(Building shop, Player player) {
        return shop == null ? this.sellingPrice : this.sellingPrice; // Stub
    }

    public int getCalculatedSellingPrice(MillVillager merchant) {
        if (merchant == null) {
            return this.foreignMerchantPrice;
        } else {
            // merchant.merchantSells logic ...
            return this.foreignMerchantPrice;
        }
    }

    public ItemStack getIcon() {
        return this.item.getItemStack();
    }

    public String getName() {
        // blocks.field_150364_r is likely Planks? Or Log?
        // For now using safe default
        return this.name;
    }

    @Override
    public int hashCode() {
        return this.item.hashCode();
    }

    @Override
    public String toString() {
        return "Goods@" + this.culture.key + ":" + this.key + "/" + this.item.getItemStack();
    }

    public void validateGood() {
        if (this.buyingPrice != 0 && this.sellingPrice != 0 && this.sellingPrice <= this.buyingPrice) {
            MillLog.minor(this.toString(), "Selling price of " + this.sellingPrice
                    + " should be superior to buying price (" + this.buyingPrice + ").");
        }
    }
}
