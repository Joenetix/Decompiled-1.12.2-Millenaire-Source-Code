package org.millenaire.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Custom sword class for Millénaire cultural weapons.
 * Supports auto-knockback enchantment and custom enchantability.
 */
public class MillenaireSword extends SwordItem {

    private final boolean autoKnockback;
    private final int customEnchantability;
    private final String cultureId;

    public MillenaireSword(Tier tier, int attackDamage, float attackSpeed, Properties properties,
            boolean autoKnockback, int enchantability, String cultureId) {
        super(tier, attackDamage, attackSpeed, properties);
        this.autoKnockback = autoKnockback;
        this.customEnchantability = enchantability;
        this.cultureId = cultureId;
    }

    public MillenaireSword(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        this(tier, attackDamage, attackSpeed, properties, false, -1, "default");
    }

    /**
     * Apply auto-enchantments when the item is crafted or first used.
     */
    public void applyAutoEnchantments(ItemStack stack) {
        if (autoKnockback && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, stack) == 0) {
            stack.enchant(Enchantments.KNOCKBACK, 2);
        }
    }

    @Override
    public void onCraftedBy(ItemStack stack, net.minecraft.world.level.Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        applyAutoEnchantments(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Apply enchantments first attack if not already applied
        if (autoKnockback && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, stack) == 0) {
            applyAutoEnchantments(stack);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public int getEnchantmentValue() {
        return customEnchantability >= 0 ? customEnchantability : super.getEnchantmentValue();
    }

    public String getCultureId() {
        return cultureId;
    }

    public boolean hasAutoKnockback() {
        return autoKnockback;
    }
}
