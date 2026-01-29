package org.millenaire.common.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.block.MillBlocks;

public class ItemMayanQuestCrown extends ItemArmor implements InvItem.IItemInitialEnchantmens {
   public ItemMayanQuestCrown(String itemName, EntityEquipmentSlot type) {
      super(MillItems.ARMOUR_mayan_quest_crown, -1, type);
      this.setMaxDamage(0);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setTranslationKey("millenaire." + itemName);
      this.setRegistryName(itemName);
   }

   @Override
   public void applyEnchantments(ItemStack stack) {
      if (EnchantmentHelper.getEnchantmentLevel(Enchantments.RESPIRATION, stack) == 0) {
         stack.addEnchantment(Enchantments.RESPIRATION, 3);
      }

      if (EnchantmentHelper.getEnchantmentLevel(Enchantments.AQUA_AFFINITY, stack) == 0) {
         stack.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
      }

      if (EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack) == 0) {
         stack.addEnchantment(Enchantments.PROTECTION, 4);
      }
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
   }

   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos bp, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
      this.applyEnchantments(player.getHeldItem(hand));
      return super.onItemUseFirst(player, world, bp, side, hitX, hitY, hitZ, hand);
   }

   public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
      this.applyEnchantments(par1ItemStack);
      super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
   }
}
