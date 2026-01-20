package org.millenaire.items;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tier;

/**
 * Factory class for creating Millénaire cultural tools.
 * Each culture can have custom tool materials and appearances.
 */
public class MillenaireTools {

    /**
     * Custom axe with culture support.
     */
    public static class MillenaireAxe extends AxeItem {
        private final String cultureId;

        public MillenaireAxe(Tier tier, float attackDamage, float attackSpeed, Properties properties,
                String cultureId) {
            super(tier, attackDamage, attackSpeed, properties);
            this.cultureId = cultureId;
        }

        public String getCultureId() {
            return cultureId;
        }
    }

    /**
     * Custom pickaxe with culture support.
     */
    public static class MillenairePickaxe extends PickaxeItem {
        private final String cultureId;

        public MillenairePickaxe(Tier tier, int attackDamage, float attackSpeed, Properties properties,
                String cultureId) {
            super(tier, attackDamage, attackSpeed, properties);
            this.cultureId = cultureId;
        }

        public String getCultureId() {
            return cultureId;
        }
    }

    /**
     * Custom shovel with culture support.
     */
    public static class MillenaireShovel extends ShovelItem {
        private final String cultureId;

        public MillenaireShovel(Tier tier, float attackDamage, float attackSpeed, Properties properties,
                String cultureId) {
            super(tier, attackDamage, attackSpeed, properties);
            this.cultureId = cultureId;
        }

        public String getCultureId() {
            return cultureId;
        }
    }

    /**
     * Custom hoe with culture support.
     */
    public static class MillenaireHoe extends HoeItem {
        private final String cultureId;

        public MillenaireHoe(Tier tier, int attackDamage, float attackSpeed, Properties properties, String cultureId) {
            super(tier, attackDamage, attackSpeed, properties);
            this.cultureId = cultureId;
        }

        public String getCultureId() {
            return cultureId;
        }
    }
}
