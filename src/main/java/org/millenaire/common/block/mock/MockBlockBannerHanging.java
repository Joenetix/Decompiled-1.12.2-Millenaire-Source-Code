package org.millenaire.common.block.mock;

import net.minecraft.block.BlockBanner.BlockBannerHanging;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.millenaire.common.entity.TileEntityMockBanner;

public class MockBlockBannerHanging extends BlockBannerHanging {
   public final int bannerType;

   public MockBlockBannerHanging(int bannerType) {
      this.bannerType = bannerType;
   }

   public TileEntity createNewTileEntity(World worldIn, int meta) {
      return new TileEntityMockBanner();
   }
}
