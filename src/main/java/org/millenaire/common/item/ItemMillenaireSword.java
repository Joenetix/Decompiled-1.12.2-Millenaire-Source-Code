package org.millenaire.common.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.advancements.MillAdvancements;
import org.millenaire.common.block.MillBlocks;

public class ItemMillenaireSword extends ItemSword implements InvItem.IItemInitialEnchantmens {
   private final boolean knockback;
   private final int enchantability;

   public ItemMillenaireSword(String itemName, ToolMaterial material, int enchantability, boolean knockback) {
      super(material);
      this.knockback = knockback;
      this.enchantability = enchantability;
      this.setTranslationKey("millenaire." + itemName);
      this.setRegistryName(itemName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
   }

   @Override
   public void applyEnchantments(ItemStack stack) {
      if (this.knockback && EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, stack) == 0) {
         stack.addEnchantment(Enchantments.KNOCKBACK, 2);
      }
   }

   public int getItemEnchantability() {
      return this.enchantability >= 0 ? this.enchantability : super.getItemEnchantability();
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
   }

   public void onCreated(ItemStack stack, World par2World, EntityPlayer par3EntityPlayer) {
      this.applyEnchantments(stack);
   }

   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos bp, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
      this.applyEnchantments(player.getHeldItem(hand));
      return super.onItemUseFirst(player, world, bp, side, hitX, hitY, hitZ, hand);
   }

   public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
      if (entity instanceof EntityPlayer) {
         MillAdvancements.MP_WEAPON.grant(player);
      }

      this.applyEnchantments(stack);
      return super.onLeftClickEntity(stack, player, entity);
   }
}
