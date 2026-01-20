package org.millenaire.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.millenaire.core.MillItems;
import java.util.ArrayList;
import java.util.List;

public class MoneyHelper {
    public static int getDenierValue(ItemStack stack) {
        if (stack.getItem() == MillItems.DENIER.get())
            return 1;
        if (stack.getItem() == MillItems.DENIER_ARGENT.get())
            return 64;
        if (stack.getItem() == MillItems.DENIER_OR.get())
            return 4096;
        return 0;
    }

    public static int getPlayerMoney(Player player) {
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            total += getDenierValue(player.getInventory().getItem(i)) * player.getInventory().getItem(i).getCount();
        }
        return total;
    }

    public static void takeMoney(Player player, int amount) {
        int current = getPlayerMoney(player);
        if (current < amount)
            return;

        // Remove all money
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (getDenierValue(player.getInventory().getItem(i)) > 0) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        int remaining = current - amount;
        giveMoney(player, remaining);
    }

    public static void giveMoney(Player player, int amount) {
        int gold = amount / 4096;
        amount %= 4096;
        int silver = amount / 64;
        amount %= 64;
        int copper = amount;

        if (gold > 0)
            player.getInventory().add(new ItemStack(MillItems.DENIER_OR.get(), gold));
        if (silver > 0)
            player.getInventory().add(new ItemStack(MillItems.DENIER_ARGENT.get(), silver));
        if (copper > 0)
            player.getInventory().add(new ItemStack(MillItems.DENIER.get(), copper));
    }

    public static List<ItemStack> getDenierItems(int amount) {
        List<ItemStack> list = new ArrayList<>();
        int gold = amount / 4096;
        amount %= 4096;
        int silver = amount / 64;
        amount %= 64;
        int copper = amount;

        if (gold > 0)
            list.add(new ItemStack(MillItems.DENIER_OR.get(), gold));
        if (silver > 0)
            list.add(new ItemStack(MillItems.DENIER_ARGENT.get(), silver));
        if (copper > 0)
            list.add(new ItemStack(MillItems.DENIER.get(), copper));

        return list;
    }
}
