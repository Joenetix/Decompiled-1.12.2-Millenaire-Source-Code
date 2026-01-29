package org.millenaire.common.block.mock;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.item.IMetaBlockName;
import org.millenaire.common.item.MillItems;

public class MockBlockAnimalSpawn extends Block implements IMetaBlockName {
   public static final PropertyEnum<MockBlockAnimalSpawn.Creature> CREATURE = PropertyEnum.create("creature", MockBlockAnimalSpawn.Creature.class);

   public MockBlockAnimalSpawn(String blockName) {
      super(Material.ROCK);
      this.disableStats();
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setBlockUnbreakable();
      this.setCreativeTab(MillBlocks.tabMillenaireContentCreator);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{CREATURE});
   }

   public int damageDropped(IBlockState state) {
      return ((MockBlockAnimalSpawn.Creature)state.getValue(CREATURE)).getMetadata();
   }

   public int getMetaFromState(IBlockState state) {
      return ((MockBlockAnimalSpawn.Creature)state.getValue(CREATURE)).meta;
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return "tile.millenaire."
         + this.getRegistryName().getPath()
         + "."
         + ((MockBlockAnimalSpawn.Creature)this.getStateFromMeta(stack.getMetadata()).getValue(CREATURE)).getName();
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(CREATURE, MockBlockAnimalSpawn.Creature.fromMeta(meta));
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      for (MockBlockAnimalSpawn.Creature enumtype : MockBlockAnimalSpawn.Creature.values()) {
         items.add(new ItemStack(this, 1, enumtype.getMetadata()));
      }
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (MockBlockAnimalSpawn.Creature enumtype : MockBlockAnimalSpawn.Creature.values()) {
         ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), enumtype.getMetadata(), new ModelResourceLocation(this.getRegistryName(), "creature=" + enumtype.getName())
         );
      }
   }

   public boolean onBlockActivated(
      World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ
   ) {
      if (worldIn.isRemote) {
         return true;
      } else if (playerIn.getHeldItem(EnumHand.MAIN_HAND).getItem() == MillItems.NEGATION_WAND) {
         int meta = state.getBlock().getMetaFromState(state) + 1;
         if (MockBlockAnimalSpawn.Creature.fromMeta(meta) == null) {
            meta = 0;
         }

         worldIn.setBlockState(pos, state.withProperty(CREATURE, MockBlockAnimalSpawn.Creature.fromMeta(meta)), 3);
         Mill.proxy.sendLocalChat(playerIn, 'a', MockBlockAnimalSpawn.Creature.fromMeta(meta).name);
         return true;
      } else {
         return false;
      }
   }

   public static enum Creature implements IStringSerializable {
      COW(0, "cow"),
      PIG(1, "pig"),
      SHEEP(2, "sheep"),
      CHICKEN(3, "chicken"),
      SQUID(4, "squid"),
      WOLF(5, "wolf"),
      POLARBEAR(6, "polarbear");

      public final int meta;
      public final String name;

      public static MockBlockAnimalSpawn.Creature fromMeta(int meta) {
         for (MockBlockAnimalSpawn.Creature t : values()) {
            if (t.meta == meta) {
               return t;
            }
         }

         return null;
      }

      public static int getMetaFromName(String name) {
         for (MockBlockAnimalSpawn.Creature creature : values()) {
            if (creature.name.equalsIgnoreCase(name)) {
               return creature.meta;
            }
         }

         return -1;
      }

      private Creature(int m, String n) {
         this.meta = m;
         this.name = n;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String getName() {
         return this.name;
      }

      @Override
      public String toString() {
         return "Animal Spawn (" + this.name + ")";
      }
   }
}
