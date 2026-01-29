package org.millenaire.common.entity;

import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityTargetedWitherSkeleton extends EntitySkeleton {
   public EntityTargetedWitherSkeleton(World par1World) {
      super(par1World);
      this.entityInit();
   }

   protected boolean canDespawn() {
      return false;
   }

   public void entityInit() {
      super.entityInit();
      this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
      this.tasks.taskEntries.clear();
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(5, new EntityAIWander(this, 1.0));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(6, new EntityAILookIdle(this));
      this.tasks.addTask(4, new EntityAIAttackMelee(this, 0.31F, false));
      this.targetTasks.taskEntries.clear();
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 10, true, false, null));
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      this.extinguish();
   }
}
