package org.millenaire.items;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Base armor class for Millénaire cultural armor pieces.
 * Supports custom armor materials defined per culture.
 */
public class MillenaireArmor extends ArmorItem {

    private final String cultureId;

    public MillenaireArmor(ArmorMaterial material, Type type, Properties properties, String cultureId) {
        super(material, type, properties);
        this.cultureId = cultureId;
    }

    public MillenaireArmor(ArmorMaterial material, Type type, Properties properties) {
        this(material, type, properties, "default");
    }

    /**
     * Get the culture this armor belongs to.
     */
    public String getCultureId() {
        return cultureId;
    }

    /**
     * Create a full armor set for a culture.
     */
    public static MillenaireArmor[] createArmorSet(ArmorMaterial material, Properties properties, String cultureId) {
        return new MillenaireArmor[] {
                new MillenaireArmor(material, Type.HELMET, properties, cultureId),
                new MillenaireArmor(material, Type.CHESTPLATE, properties, cultureId),
                new MillenaireArmor(material, Type.LEGGINGS, properties, cultureId),
                new MillenaireArmor(material, Type.BOOTS, properties, cultureId)
        };
    }
}
