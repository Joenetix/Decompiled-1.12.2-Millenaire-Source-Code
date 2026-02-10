package org.millenaire.common.entity;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import org.millenaire.common.advancements.MillAdvancements;
import org.millenaire.common.block.BlockFruitLeaves;
import org.millenaire.common.block.BlockMillCrops;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.CultureLanguage;
import org.millenaire.common.culture.VillagerType;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.ItemClothes;
import org.millenaire.common.item.ItemMillenaireBow;
import org.millenaire.common.item.TradeGood;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.network.StreamReadWrite;
import org.millenaire.common.pathing.atomicstryker.AS_PathEntity;
import org.millenaire.common.pathing.atomicstryker.AStarConfig;
import org.millenaire.common.pathing.atomicstryker.AStarNode;
import org.millenaire.common.pathing.atomicstryker.AStarPathPlannerJPS;
import org.millenaire.common.pathing.atomicstryker.AStarStatic;
import org.millenaire.common.pathing.atomicstryker.IAStarPathedEntity;
import org.millenaire.common.quest.QuestInstance;
import org.millenaire.common.utilities.BlockItemUtilities;
import org.millenaire.common.utilities.BlockStateUtilities;
import org.millenaire.common.utilities.DevModUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.ThreadSafeUtilities;
import org.millenaire.common.utilities.VillageUtilities;
import org.millenaire.common.utilities.WorldUtilities;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.BuildingLocation;
import org.millenaire.common.village.ConstructionIP;
import org.millenaire.common.village.VillagerRecord;
import org.millenaire.common.world.MillWorldData;
import org.millenaire.common.world.UserProfile;

public abstract class MillVillager extends EntityCreature implements IEntityAdditionalSpawnData, IAStarPathedEntity {
   private static final UUID SPRINT_SPEED_BOOST_ID = UUID.fromString("B9766B59-8456-5632-BC1F-2EE2A276D836");
   private static final AttributeModifier SPRINT_SPEED_BOOST = new AttributeModifier(SPRINT_SPEED_BOOST_ID,
         "Sprint speed boost", 0.1, 1);
   private static final double DEFAULT_MOVE_SPEED = 0.5;
   public static final int ATTACK_RANGE_DEFENSIVE = 20;
   private static final String FREE_CLOTHES = "free";
   private static final String NATURAL = "natural";
   private static final int CONCEPTION_CHANCE = 2;
   private static final int VISITOR_NB_NIGHTS_BEFORE_LEAVING = 5;
   public static final int MALE = 1;
   public static final int FEMALE = 2;
   public static final ResourceLocation GENERIC_VILLAGER = new ResourceLocation("millenaire", "GenericVillager");
   public static final ResourceLocation GENERIC_ASYMM_FEMALE = new ResourceLocation("millenaire", "GenericAsimmFemale");
   public static final ResourceLocation GENERIC_SYMM_FEMALE = new ResourceLocation("millenaire", "GenericSimmFemale");
   public static final ResourceLocation GENERIC_ZOMBIE = new ResourceLocation("millenaire", "GenericZombie");
   private static ItemStack[] WOODDEN_HOE_STACK = new ItemStack[] { new ItemStack(Items.WOODEN_HOE, 1) };
   private static ItemStack[] WOODDEN_SHOVEL_STACK = new ItemStack[] { new ItemStack(Items.WOODEN_SHOVEL, 1) };
   private static ItemStack[] WOODDEN_PICKAXE_STACK = new ItemStack[] { new ItemStack(Items.WOODEN_PICKAXE, 1) };
   private static ItemStack[] WOODDEN_AXE_STACK = new ItemStack[] { new ItemStack(Items.WOODEN_AXE, 1) };
   static final int GATHER_RANGE = 20;
   private static final int HOLD_DURATION = 20;
   public static final int ATTACK_RANGE = 80;
   public static final int ARCHER_RANGE = 20;
   public static final int MAX_CHILD_SIZE = 20;
   private static final AStarConfig JPS_CONFIG_DEFAULT = new AStarConfig(true, false, false, false, true);
   private static final AStarConfig JPS_CONFIG_NO_LEAVES = new AStarConfig(true, false, false, false, false);
   public VillagerType vtype;
   public int action = 0;
   public String goalKey = null;
   private Goal.GoalInformation goalInformation = null;
   private Point pathDestPoint;
   private Building house = null;
   private Building townHall = null;
   public Point housePoint = null;
   public Point prevPoint = null;
   public Point townHallPoint = null;
   public boolean extraLog = false;
   public String firstName = "";
   public String familyName = "";
   public ItemStack heldItem = ItemStack.EMPTY;
   public ItemStack heldItemOffHand = ItemStack.EMPTY;
   public long timer = 0L;
   public long actionStart = 0L;
   public boolean allowRandomMoves = false;
   public boolean stopMoving = false;
   public int gender = 0;
   public boolean registered = false;
   public int longDistanceStuck;
   public boolean nightActionPerformed = false;
   public long speech_started = 0L;
   public HashMap<InvItem, Integer> inventory;
   public Block previousBlock;
   public int previousBlockMeta;
   public long pathingTime;
   public long timeSinceLastPathingTimeDisplay;
   private long villagerId = -1L;
   public int nbPathsCalculated = 0;
   public int nbPathNoStart = 0;
   public int nbPathNoEnd = 0;
   public int nbPathAborted = 0;
   public int nbPathFailure = 0;
   public long goalStarted = 0L;
   public int constructionJobId = -1;
   public int heldItemCount = 0;
   public int heldItemId = -1;
   public int heldItemOffHandId = -1;
   public String speech_key = null;
   public int speech_variant = 0;
   public String dialogueKey = null;
   public int dialogueRole = 0;
   public long dialogueStart = 0L;
   public char dialogueColour = 'f';
   public boolean dialogueChat = false;
   public String dialogueTargetFirstName = null;
   public String dialogueTargetLastName = null;
   private Point doorToClose = null;
   public int visitorNbNights = 0;
   public int foreignMerchantStallId = -1;
   public boolean lastAttackByPlayer = false;
   public HashMap<Goal, Long> lastGoalTime = new HashMap<>();
   public String hiredBy = null;
   public boolean aggressiveStance = false;
   public long hiredUntil = 0L;
   public boolean isUsingBow;
   public boolean isUsingHandToHand;
   public boolean isRaider = false;
   public AStarPathPlannerJPS pathPlannerJPS;
   public AS_PathEntity pathEntity;
   public int updateCounter = 0;
   public long client_lastupdated;
   public MillWorldData mw;
   private boolean pathFailedSincelastTick = false;
   private List<AStarNode> pathCalculatedSinceLastTick = null;
   private int localStuck = 0;
   private final ResourceLocation[] clothTexture = new ResourceLocation[2];
   private String clothName = null;
   public boolean shouldLieDown = false;
   public LinkedHashMap<TradeGood, Integer> merchantSells = new LinkedHashMap<>();
   public ResourceLocation texture = null;
   private int attackTime;
   public boolean isDeadOnServer = false;
   public boolean travelBookMockVillager = false;

   public static MillVillager createMockVillager(VillagerRecord villagerRecord, World world) {
      MillVillager villager = (MillVillager) EntityList
            .createEntityByIDFromName(villagerRecord.getType().getEntityName(), world);
      if (villager == null) {
         MillLog.error(
               villagerRecord,
               "Could not create mock villager of dynamic type: " + villagerRecord.getType() + " entity: "
                     + villagerRecord.getType().getEntityName());
         return null;
      } else {
         villager.vtype = villagerRecord.getType();
         villager.gender = villagerRecord.getType().gender;
         villager.firstName = villagerRecord.firstName;
         villager.familyName = villagerRecord.familyName;
         villager.texture = villagerRecord.texture;
         villager.setHealth(villager.getMaxHealth());
         villager.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(villagerRecord.getType().health);
         villager.updateClothTexturePath();
         return villager;
      }
   }

   public static MillVillager createVillager(VillagerRecord villagerRecord, World world, Point spawnPos,
         boolean respawn) {
      if (world.isRemote || !(world instanceof WorldServer)) {
         MillLog.printException("Tried creating a villager in client world: " + world, new Exception());
         return null;
      } else if (villagerRecord == null) {
         MillLog.error(villagerRecord, "Tried creating villager from a null record");
         return null;
      } else if (villagerRecord.getType() == null) {
         MillLog.error(null, "Tried creating villager of null type: " + villagerRecord.getType());
         return null;
      } else {
         MillVillager villager = (MillVillager) EntityList
               .createEntityByIDFromName(villagerRecord.getType().getEntityName(), world);
         if (villager == null) {
            MillLog.error(
                  villagerRecord,
                  "Could not create villager of dynamic type: " + villagerRecord.getType() + " entity: "
                        + villagerRecord.getType().getEntityName());
            return null;
         } else {
            villager.housePoint = villagerRecord.getHousePos();
            villager.townHallPoint = villagerRecord.getTownHallPos();
            villager.vtype = villagerRecord.getType();
            villager.setVillagerId(villagerRecord.getVillagerId());
            villager.gender = villagerRecord.getType().gender;
            villager.firstName = villagerRecord.firstName;
            villager.familyName = villagerRecord.familyName;
            villager.texture = villagerRecord.texture;
            villager.setHealth(villager.getMaxHealth());
            villager.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                  .setBaseValue(villagerRecord.getType().health);
            villager.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                  .setBaseValue(villagerRecord.getType().baseSpeed);
            villager.updateClothTexturePath();
            if (!respawn) {
               for (InvItem item : villagerRecord.getType().startingInv.keySet()) {
                  villager.addToInv(item.getItem(), item.meta, villagerRecord.getType().startingInv.get(item));
               }
            }

            villager.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
            if (MillConfigValues.LogVillagerSpawn >= 1) {
               MillLog.major(villager, "Created new villager from record.");
            }

            return villager;
         }
      }
   }

   public static void readVillagerPacket(PacketBuffer data) {
      try {
         long villager_id = data.readLong();
         if (Mill.clientWorld.getVillagerById(villager_id) != null) {
            Mill.clientWorld.getVillagerById(villager_id).readVillagerStreamdata(data);
         } else if (MillConfigValues.LogNetwork >= 2) {
            MillLog.minor(null, "readVillagerPacket for unknown villager: " + villager_id);
         }
      } catch (IOException var3) {
         MillLog.printException(var3);
      }
   }

   public MillVillager(World world) {
      super(world);
      this.world = world;
      this.mw = Mill.getMillWorld(world);
      this.inventory = new HashMap<>();
      this.setHealth(this.getMaxHealth());
      this.isImmuneToFire = true;
      this.client_lastupdated = world.getWorldTime();
      if (!world.isRemote) {
         this.pathPlannerJPS = new AStarPathPlannerJPS(world, this, MillConfigValues.jpsPathing);
      }

      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
      if (MillConfigValues.LogVillagerSpawn >= 3) {
         Exception e = new Exception();
         MillLog.printException("Creating villager " + this + " in world: " + world, e);
      }
   }

   public void addToInv(Block block, int nb) {
      this.addToInv(Item.getItemFromBlock(block), 0, nb);
   }

   public void addToInv(Block block, int meta, int nb) {
      this.addToInv(Item.getItemFromBlock(block), meta, nb);
   }

   public void addToInv(IBlockState bs, int nb) {
      this.addToInv(Item.getItemFromBlock(bs.getBlock()), bs.getBlock().getMetaFromState(bs), nb);
   }

   public void addToInv(InvItem iv, int nb) {
      this.addToInv(iv.getItem(), iv.meta, nb);
   }

   public void addToInv(Item item, int nb) {
      this.addToInv(item, 0, nb);
   }

   public void addToInv(Item item, int meta, int nb) {
      InvItem key = InvItem.createInvItem(item, meta);
      if (this.inventory.containsKey(key)) {
         this.inventory.put(key, this.inventory.get(key) + nb);
      } else {
         this.inventory.put(key, nb);
      }

      this.updateVillagerRecord();
      this.updateClothTexturePath();
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.computeMaxHealth());
   }

   private void applyPathCalculatedSinceLastTick() {
      try {
         AS_PathEntity path = AStarStatic.translateAStarPathtoPathEntity(this.world, this.pathCalculatedSinceLastTick,
               this.getPathingConfig());
         this.registerNewPath(path);
      } catch (Exception var2) {
         MillLog.printException("Exception when finding JPS path:", var2);
      }

      this.pathCalculatedSinceLastTick = null;
   }

   public boolean attackEntity(Entity entity) {
      double distance = this.getPos().distanceTo(entity);
      if (this.vtype.isArcher && distance > 5.0 && this.hasBow()) {
         this.isUsingBow = true;
         this.attackEntity_testHiredGoon(entity);
         if (distance < 20.0 && entity instanceof EntityLivingBase) {
            if (this.attackTime <= 0) {
               this.attackTime = 100;
               this.swingArm(EnumHand.MAIN_HAND);
               float distanceFactor = (float) (distance / 20.0);
               distanceFactor = MathHelper.clamp(distanceFactor, 0.1F, 1.0F);
               this.attackEntityWithRangedAttack((EntityLivingBase) entity, distanceFactor);
            } else {
               this.attackTime--;
            }
         }
      } else {
         if (this.attackTime <= 0
               && distance < 2.0
               && entity.getEntityBoundingBox().maxY > this.getEntityBoundingBox().minY
               && entity.getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY) {
            this.attackTime = 20;
            this.swingArm(EnumHand.MAIN_HAND);
            this.attackEntity_testHiredGoon(entity);
            return entity.attackEntityFrom(DamageSource.causeMobDamage(this), this.getAttackStrength());
         }

         this.attackTime--;
         this.isUsingHandToHand = true;
      }

      return true;
   }

   private void attackEntity_testHiredGoon(Entity targetedEntity) {
      if (targetedEntity instanceof EntityPlayer && this.hiredBy != null) {
         EntityPlayer owner = this.world.getPlayerEntityByName(this.hiredBy);
         if (owner != null && owner != targetedEntity) {
            MillAdvancements.MP_HIREDGOON.grant(owner);
         }
      }
   }

   public boolean attackEntityFrom(DamageSource ds, float i) {
      if (ds.getTrueSource() == null && ds != DamageSource.OUT_OF_WORLD) {
         return false;
      } else {
         boolean hadFullHealth = this.getMaxHealth() == this.getHealth();
         boolean b = super.attackEntityFrom(ds, i);
         Entity entity = ds.getTrueSource();
         this.lastAttackByPlayer = false;
         if (entity != null && entity instanceof EntityLivingBase) {
            if (entity instanceof EntityPlayer) {
               if (!((EntityPlayer) entity).isSpectator() && !((EntityPlayer) entity).isCreative()) {
                  this.lastAttackByPlayer = true;
                  EntityPlayer player = (EntityPlayer) entity;
                  if (!this.isRaider) {
                     if (this.vtype != null && !this.vtype.hostile) {
                        UserProfile serverProfile = VillageUtilities.getServerProfile(player.world, player);
                        if (serverProfile != null) {
                           serverProfile.adjustReputation(this.getTownHall(), (int) (-i * 10.0F));
                        }
                     }

                     if (this.world.getDifficulty() != EnumDifficulty.PEACEFUL
                           && this.getHealth() < this.getMaxHealth() - 10.0F) {
                        this.setAttackTarget((EntityLivingBase) entity);
                        this.clearGoal();
                        if (this.getTownHall() != null) {
                           this.getTownHall().callForHelp((EntityLivingBase) entity);
                        }
                     }

                     if (this.vtype != null
                           && !this.vtype.hostile
                           && hadFullHealth
                           && (player.getHeldItem(EnumHand.MAIN_HAND) == null
                                 || MillCommonUtilities
                                       .getItemWeaponDamage(player.getActiveItemStack().getItem()) <= 1.0)
                           && !this.world.isRemote) {
                        ServerSender.sendTranslatedSentence(player, '6', "ui.communicationexplanations");
                     }
                  }

                  if (this.lastAttackByPlayer && this.getHealth() <= 0.0F) {
                     if (this.vtype != null && this.vtype.hostile) {
                        MillAdvancements.SELF_DEFENSE.grant(player);
                     } else {
                        MillAdvancements.DARK_SIDE.grant(player);
                     }
                  }
               }
            } else if (entity instanceof MillVillager) {
               MillVillager attackingVillager = (MillVillager) entity;
               if (this.isRaider != attackingVillager.isRaider
                     || this.getTownHall() != attackingVillager.getTownHall()) {
                  this.setAttackTarget((EntityLivingBase) entity);
                  this.clearGoal();
                  if (this.getTownHall() != null) {
                     this.getTownHall().callForHelp((EntityLivingBase) entity);
                  }
               }
            } else {
               this.setAttackTarget((EntityLivingBase) entity);
               this.clearGoal();
               if (this.getTownHall() != null) {
                  this.getTownHall().callForHelp((EntityLivingBase) entity);
               }
            }
         }

         return b;
      }
   }

   public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
      EntityArrow entityarrow = this.getArrow(distanceFactor);
      double d0 = target.posX - this.posX;
      double d1 = target.getEntityBoundingBox().minY + target.height / 3.0F - entityarrow.posY;
      double d2 = target.posZ - this.posZ;
      double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
      float speedFactor = 1.0F;
      float damageBonus = 0.0F;
      ItemStack weapon = this.getWeapon();
      if (weapon != null) {
         Item item = weapon.getItem();
         if (item instanceof ItemMillenaireBow) {
            ItemMillenaireBow bow = (ItemMillenaireBow) item;
            if (bow.speedFactor > speedFactor) {
               speedFactor = bow.speedFactor;
            }

            if (bow.damageBonus > damageBonus) {
               damageBonus = bow.damageBonus;
            }
         }
      }

      entityarrow.setDamage(entityarrow.getDamage() + damageBonus);
      entityarrow.shoot(d0, d1 + d3 * 0.2F, d2, 1.6F, (14 - this.world.getDifficulty().getId() * 4) * speedFactor);
      this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
      this.world.spawnEntity(entityarrow);
   }

   public boolean attemptChildConception() {
      int nbChildren = 0;

      for (MillVillager villager : this.getHouse().getKnownVillagers()) {
         if (villager.isChild()) {
            nbChildren++;
         }
      }

      if (nbChildren > 1) {
         if (MillConfigValues.LogChildren >= 3) {
            MillLog.debug(this, "Wife already has " + nbChildren + " children, no need for more.");
         }

         return true;
      } else {
         int nbChildVillage = this.getTownHall().countChildren();
         if (nbChildVillage > MillConfigValues.maxChildrenNumber) {
            if (MillConfigValues.LogChildren >= 3) {
               MillLog.debug(this, "Village already has " + nbChildVillage + ", no need for more.");
            }

            return true;
         } else {
            boolean couldMoveIn = false;

            for (Point housePoint : this.getTownHall().buildings) {
               Building house = this.mw.getBuilding(housePoint);
               if (house != null
                     && !house.equals(this.getHouse())
                     && house.isHouse()
                     && (house.canChildMoveIn(1, this.familyName) || house.canChildMoveIn(2, this.familyName))) {
                  couldMoveIn = true;
               }
            }

            if (nbChildVillage > 5 && !couldMoveIn) {
               if (MillConfigValues.LogChildren >= 3) {
                  MillLog.debug(this,
                        "Village already has " + nbChildVillage + " and no slot is available for the new child.");
               }

               return true;
            } else {
               List<Entity> entities = WorldUtilities.getEntitiesWithinAABB(this.world, MillVillager.class,
                     this.getPos(), 4, 2);
               boolean manFound = false;

               for (Entity ent : entities) {
                  MillVillager villagerx = (MillVillager) ent;
                  if (villagerx.gender == 1 && !villagerx.isChild()) {
                     manFound = true;
                  }
               }

               if (!manFound) {
                  return false;
               } else {
                  if (MillConfigValues.LogChildren >= 3) {
                     MillLog.debug(this, "Less than two kids and man present, trying for new child.");
                  }

                  boolean createChild = false;
                  int conceptionChances = 2;
                  InvItem conceptionFood = this.getConfig().getBestConceptionFood(this.getHouse());
                  if (conceptionFood != null) {
                     this.getHouse().takeGoods(conceptionFood, 1);
                     conceptionChances += this.getConfig().foodsConception.get(conceptionFood);
                  }

                  if (MillCommonUtilities.randomInt(10) < conceptionChances) {
                     createChild = true;
                     if (MillConfigValues.LogChildren >= 2) {
                        MillLog.minor(this, "Conceiving child. Food available: " + conceptionFood);
                     }
                  } else if (MillConfigValues.LogChildren >= 2) {
                     MillLog.minor(this, "Failed to conceive child. Food available: " + conceptionFood);
                  }

                  if (MillConfigValues.DEV) {
                     createChild = true;
                  }

                  if (createChild) {
                     this.getHouse().createChild(this, this.getTownHall(), this.getRecord().spousesName);
                  }

                  return true;
               }
            }
         }
      }
   }

   public void calculateMerchantGoods() {
      for (InvItem key : this.vtype.foreignMerchantStock.keySet()) {
         if (this.getCulture().getTradeGood(key) != null && this.getBasicForeignMerchantPrice(key) > 0) {
            this.merchantSells.put(this.getCulture().getTradeGood(key), this.getBasicForeignMerchantPrice(key));
         }
      }
   }

   public boolean canBeLeashedTo(EntityPlayer player) {
      return false;
   }

   public boolean canDespawn() {
      return false;
   }

   public boolean canMeditate() {
      return this.vtype.canMeditate;
   }

   public boolean canPerformSacrifices() {
      return this.vtype.canPerformSacrifices;
   }

   public boolean canVillagerClearLeaves() {
      return !this.vtype.noleafclearing;
   }

   private void checkGoalHeldItems(Goal goal, Point target) throws Exception {
      if (this.heldItemCount > 20) {
         ItemStack[] heldItems = null;
         if (target != null && target.horizontalDistanceTo(this) < goal.range(this)) {
            heldItems = goal.getHeldItemsDestination(this);
         } else {
            heldItems = goal.getHeldItemsTravelling(this);
         }

         if (heldItems != null && heldItems.length > 0) {
            this.heldItemId = (this.heldItemId + 1) % heldItems.length;
            this.heldItem = heldItems[this.heldItemId];
         }

         heldItems = null;
         if (target != null && target.horizontalDistanceTo(this) < goal.range(this)) {
            heldItems = goal.getHeldItemsOffHandDestination(this);
         } else {
            heldItems = goal.getHeldItemsOffHandTravelling(this);
         }

         if (heldItems != null && heldItems.length > 0) {
            this.heldItemOffHandId = (this.heldItemOffHandId + 1) % heldItems.length;
            this.heldItemOffHand = heldItems[this.heldItemOffHandId];
         }

         this.heldItemCount = 0;
      }

      if (this.heldItemCount == 0 && goal.swingArms(this)) {
         this.swingArm(EnumHand.MAIN_HAND);
      }

      this.heldItemCount++;
   }

   public void checkGoals() throws Exception {
      Goal goal = Goal.goals.get(this.goalKey);
      if (goal == null) {
         MillLog.error(this, "Invalid goal key: " + this.goalKey);
         this.goalKey = null;
      } else {
         if (this.getGoalDestEntity() != null) {
            if (this.getGoalDestEntity().isDead) {
               this.setGoalDestEntity(null);
               this.setPathDestPoint(null, 0);
            } else {
               this.setPathDestPoint(new Point(this.getGoalDestEntity()), 2);
            }
         }

         Point target = null;
         boolean continuingGoal = true;
         if (this.getPathDestPoint() != null) {
            target = this.getPathDestPoint();
            if (this.pathEntity != null && this.pathEntity.getCurrentPathLength() > 0) {
               target = new Point(this.pathEntity.getFinalPathPoint());
            }
         }

         this.speakSentence(goal.sentenceKey());
         if (this.getGoalDestPoint() == null && this.getGoalDestEntity() == null) {
            goal.setVillagerDest(this);
            if (MillConfigValues.LogGeneralAI >= 2 && this.extraLog) {
               MillLog.minor(this, "Goal destination: " + this.getGoalDestPoint() + "/" + this.getGoalDestEntity());
            }
         } else if (target != null && target.horizontalDistanceTo(this) < goal.range(this)) {
            if (this.actionStart == 0L) {
               this.stopMoving = goal.stopMovingWhileWorking();
               this.actionStart = this.world.getWorldTime();
               this.shouldLieDown = goal.shouldVillagerLieDown();
               if (MillConfigValues.LogGeneralAI >= 2 && this.extraLog) {
                  MillLog.minor(this, "Starting action: " + this.actionStart);
               }
            }

            if (this.world.getWorldTime() - this.actionStart >= goal.actionDuration(this)) {
               if (goal.performAction(this)) {
                  this.clearGoal();
                  this.goalKey = goal.nextGoal(this);
                  this.stopMoving = false;
                  this.shouldLieDown = false;
                  this.heldItem = ItemStack.EMPTY;
                  this.heldItemOffHand = ItemStack.EMPTY;
                  continuingGoal = false;
                  if (MillConfigValues.LogGeneralAI >= 2 && this.extraLog) {
                     MillLog.minor(this, "Goal performed. Now doing: " + this.goalKey);
                  }
               } else {
                  this.stopMoving = goal.stopMovingWhileWorking();
               }

               this.actionStart = 0L;
               this.goalStarted = this.world.getWorldTime();
            }
         } else {
            this.stopMoving = false;
            this.shouldLieDown = false;
         }

         if (continuingGoal) {
            if (goal.isStillValid(this)) {
               if (this.world.getWorldTime() - this.goalStarted > goal.stuckDelay(this)) {
                  boolean actionDone = goal.stuckAction(this);
                  if (actionDone) {
                     this.goalStarted = this.world.getWorldTime();
                  }

                  if (goal.isStillValid(this)) {
                     this.allowRandomMoves = goal.allowRandomMoves();
                     if (this.stopMoving) {
                        this.navigator.clearPath();
                        this.pathEntity = null;
                     }

                     this.checkGoalHeldItems(goal, target);
                  }
               } else {
                  this.checkGoalHeldItems(goal, target);
               }
            } else {
               this.stopMoving = false;
               this.shouldLieDown = false;
               goal.onComplete(this);
               this.clearGoal();
               this.goalKey = goal.nextGoal(this);
               this.heldItemCount = 21;
               this.heldItemId = -1;
               this.heldItemOffHandId = -1;
            }
         }
      }
   }

   public void clearGoal() {
      this.setGoalDestPoint(null);
      this.setGoalBuildingDestPoint(null);
      this.setGoalDestEntity(null);
      this.goalKey = null;
      this.shouldLieDown = false;
   }

   private boolean closeFenceGate(int i, int j, int k) {
      Point p = new Point(i, j, k);
      IBlockState state = p.getBlockActualState(this.world);
      if (BlockItemUtilities.isFenceGate(state.getBlock()) && (Boolean) state.getValue(BlockFenceGate.OPEN)) {
         p.setBlockState(this.world, state.withProperty(BlockFenceGate.OPEN, false));
         return true;
      } else {
         return false;
      }
   }

   public void computeChildScale() {
      if (this.getRecord() != null) {
         if (this.getSize() == 20) {
            if (this.gender == 1) {
               this.getRecord().scale = 0.9F;
            } else {
               this.getRecord().scale = 0.8F;
            }
         } else {
            this.getRecord().scale = 0.5F + this.getSize() / 100.0F;
         }
      }
   }

   public float computeMaxHealth() {
      if (this.vtype == null || this.getRecord() == null) {
         return 40.0F;
      } else {
         return this.isChild() ? 10 + this.getSize() : this.vtype.health;
      }
   }

   private List<PathPoint> computeNewPath(Point dest) {
      if (this.getPos().sameBlock(dest)) {
         return null;
      } else {
         try {
            if (this.goalKey != null && Goal.goals.containsKey(this.goalKey)) {
               Goal goal = Goal.goals.get(this.goalKey);
               if (goal.range(this) >= this.getPos().horizontalDistanceTo(this.getPathDestPoint())) {
                  return null;
               }
            }

            if (this.pathPlannerJPS.isBusy()) {
               this.pathPlannerJPS.stopPathSearch(true);
            }

            AStarNode destNode = null;
            AStarNode[] possibles = AStarStatic.getAccessNodesSorted(
                  this.world,
                  this.doubleToInt(this.posX),
                  this.doubleToInt(this.posY),
                  this.doubleToInt(this.posZ),
                  this.getPathDestPoint().getiX(),
                  this.getPathDestPoint().getiY(),
                  this.getPathDestPoint().getiZ(),
                  this.getPathingConfig());
            if (possibles.length != 0) {
               destNode = possibles[0];
            }

            if (destNode != null) {
               Point startPos = this.getPos().getBelow();
               if (!startPos.isBlockPassable(this.world)) {
                  startPos = startPos.getAbove();
                  if (!startPos.isBlockPassable(this.world)) {
                     startPos = startPos.getAbove();
                  }
               }

               this.pathPlannerJPS
                     .getPath(
                           this.doubleToInt(this.posX),
                           this.doubleToInt(this.posY),
                           this.doubleToInt(this.posZ),
                           destNode.x,
                           destNode.y,
                           destNode.z,
                           this.getPathingConfig());
            } else {
               this.onNoPathAvailable();
            }
         } catch (ThreadSafeUtilities.ChunkAccessException var5) {
            if (MillConfigValues.LogChunkLoader >= 2) {
               MillLog.minor(this, "Chunk access violation while calculating path.");
            }
         }

         return null;
      }
   }

   public int countInv(Block block, int meta) {
      return this.countInv(InvItem.createInvItem(Item.getItemFromBlock(block), meta));
   }

   public int countInv(IBlockState blockState) {
      return this.countInv(InvItem.createInvItem(Item.getItemFromBlock(blockState.getBlock()),
            blockState.getBlock().getMetaFromState(blockState)));
   }

   public int countInv(InvItem key) {
      if (key.block == Blocks.LOG && key.meta == -1) {
         int nb = 0;
         InvItem tkey = InvItem.createInvItem(Item.getItemFromBlock(Blocks.LOG), 0);
         if (this.inventory.containsKey(tkey)) {
            nb += this.inventory.get(tkey);
         }

         tkey = InvItem.createInvItem(Item.getItemFromBlock(Blocks.LOG), 1);
         if (this.inventory.containsKey(tkey)) {
            nb += this.inventory.get(tkey);
         }

         tkey = InvItem.createInvItem(Item.getItemFromBlock(Blocks.LOG), 2);
         if (this.inventory.containsKey(tkey)) {
            nb += this.inventory.get(tkey);
         }

         tkey = InvItem.createInvItem(Item.getItemFromBlock(Blocks.LOG), 3);
         if (this.inventory.containsKey(tkey)) {
            nb += this.inventory.get(tkey);
         }

         tkey = InvItem.createInvItem(Item.getItemFromBlock(Blocks.LOG2), 0);
         if (this.inventory.containsKey(tkey)) {
            nb += this.inventory.get(tkey);
         }

         tkey = InvItem.createInvItem(Item.getItemFromBlock(Blocks.LOG2), 1);
         if (this.inventory.containsKey(tkey)) {
            nb += this.inventory.get(tkey);
         }

         return nb;
      } else if (key.meta == -1) {
         int nbx = 0;

         for (int i = 0; i < 16; i++) {
            InvItem tkeyx = InvItem.createInvItem(key.item, i);
            if (this.inventory.containsKey(tkeyx)) {
               nbx += this.inventory.get(tkeyx);
            }
         }

         return nbx;
      } else {
         return this.inventory.containsKey(key) ? this.inventory.get(key) : 0;
      }
   }

   public int countInv(Item item) {
      return this.countInv(item, 0);
   }

   public int countInv(Item item, int meta) {
      return this.countInv(InvItem.createInvItem(item, meta));
   }

   public int countItemsAround(Item[] items, int radius) {
      List<Entity> list = WorldUtilities.getEntitiesWithinAABB(this.world, EntityItem.class, this.getPos(), radius,
            radius);
      int count = 0;
      if (list != null) {
         for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getClass() == EntityItem.class) {
               EntityItem entity = (EntityItem) list.get(i);
               if (!entity.isDead) {
                  for (Item id : items) {
                     if (id == entity.getItem().getItem()) {
                        count++;
                     }
                  }
               }
            }
         }
      }

      return count;
   }

   public void despawnVillager() {
      if (!this.world.isRemote) {
         if (this.hiredBy != null) {
            EntityPlayer owner = this.world.getPlayerEntityByName(this.hiredBy);
            if (owner != null) {
               ServerSender.sendTranslatedSentence(owner, '4', "hire.hiredied", this.getName());
            }
         }

         this.mw.clearVillagerOfId(this.getVillagerId());
         super.setDead();
      }
   }

   public void despawnVillagerSilent() {
      if (MillConfigValues.LogVillagerSpawn >= 3) {
         Exception e = new Exception();
         MillLog.printException("Despawning villager: " + this, e);
      }

      this.mw.clearVillagerOfId(this.getVillagerId());
      super.setDead();
   }

   public void detrampleCrops() {
      if (this.getPos().sameBlock(this.prevPoint)
            && (this.previousBlock == Blocks.WHEAT || this.previousBlock instanceof BlockMillCrops)
            && this.getBlock(this.getPos()) != Blocks.AIR
            && this.getBlock(this.getPos().getBelow()) == Blocks.DIRT) {
         this.setBlock(this.getPos(), this.previousBlock);
         this.setBlockMetadata(this.getPos(), this.previousBlockMeta);
         this.setBlock(this.getPos().getBelow(), Blocks.FARMLAND);
      }

      this.previousBlock = this.getBlock(this.getPos());
      this.previousBlockMeta = this.getBlockMeta(this.getPos());
   }

   public int doubleToInt(double input) {
      return AStarStatic.getIntCoordFromDoubleCoord(input);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && obj instanceof MillVillager) {
         MillVillager v = (MillVillager) obj;
         return this.getVillagerId() == v.villagerId;
      } else {
         return false;
      }
   }

   public void faceEntity(Entity par1Entity, float par2, float par3) {
   }

   public void faceEntityMill(Entity entityIn, float par2, float par3) {
      this.getLookHelper().setLookPositionWithEntity(entityIn, par2, par3);
   }

   public void facePoint(Point p, float par2, float par3) {
      double x = p.x + 0.5;
      double z = p.z + 0.5;
      double y = p.y + 1.0;
      this.getLookHelper().setLookPosition(x, y, z, 10.0F, this.getVerticalFaceSpeed());
   }

   private void foreignMerchantUpdate() {
      if (this.foreignMerchantStallId < 0) {
         for (int i = 0; i < this.getHouse().getResManager().stalls.size() && this.foreignMerchantStallId < 0; i++) {
            boolean taken = false;

            for (MillVillager v : this.getHouse().getKnownVillagers()) {
               if (v.foreignMerchantStallId == i) {
                  taken = true;
               }
            }

            if (!taken) {
               this.foreignMerchantStallId = i;
            }
         }
      }

      if (this.foreignMerchantStallId < 0) {
         this.foreignMerchantStallId = 0;
      }
   }

   private Goal getActiveGoal() {
      return this.goalKey != null && Goal.goals.containsKey(this.goalKey) ? Goal.goals.get(this.goalKey) : null;
   }

   protected EntityArrow getArrow(float distanceFactor) {
      EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.world, this);
      entitytippedarrow.setEnchantmentEffectsFromEntity(this, distanceFactor);
      return entitytippedarrow;
   }

   public int getAttackStrength() {
      int attackStrength = this.vtype.baseAttackStrength;
      ItemStack weapon = this.getWeapon();
      if (weapon != null) {
         attackStrength = (int) (attackStrength
               + Math.ceil((float) MillCommonUtilities.getItemWeaponDamage(weapon.getItem()) / 2.0F));
      }

      return attackStrength;
   }

   public int getBasicForeignMerchantPrice(InvItem item) {
      if (this.getTownHall() == null) {
         return 0;
      } else if (this.getCulture().getTradeGood(item) != null) {
         return this.getCulture() != this.getTownHall().culture
               ? (int) (this.getCulture().getTradeGood(item).foreignMerchantPrice * 1.5)
               : this.getCulture().getTradeGood(item).foreignMerchantPrice;
      } else {
         return 0;
      }
   }

   public float getBedOrientationInDegrees() {
      Point ref = this.getPos();
      if (this.getGoalDestPoint() != null) {
         ref = this.getGoalDestPoint();
      }

      Block block = WorldUtilities.getBlock(this.world, ref);
      if (block instanceof BlockBed) {
         IBlockState state = ref.getBlockActualState(this.world);
         EnumFacing side = (EnumFacing) state.getValue(BlockHorizontal.FACING);
         if (side == EnumFacing.SOUTH) {
            return 0.0F;
         }

         if (side == EnumFacing.NORTH) {
            return 180.0F;
         }

         if (side == EnumFacing.EAST) {
            return 270.0F;
         }

         if (side == EnumFacing.WEST) {
            return 90.0F;
         }
      } else {
         if (WorldUtilities.getBlock(this.world, ref.getSouth()) == Blocks.AIR) {
            return 0.0F;
         }

         if (WorldUtilities.getBlock(this.world, ref.getWest()) == Blocks.AIR) {
            return 90.0F;
         }

         if (WorldUtilities.getBlock(this.world, ref.getNorth()) == Blocks.AIR) {
            return 180.0F;
         }

         if (WorldUtilities.getBlock(this.world, ref.getEast()) == Blocks.AIR) {
            return 270.0F;
         }
      }

      return 0.0F;
   }

   public ItemTool getBestAxe() {
      InvItem bestItem = this.getConfig().getBestAxe(this);
      return bestItem != null ? (ItemTool) bestItem.item : (ItemTool) Items.WOODEN_AXE;
   }

   public ItemStack[] getBestAxeStack() {
      InvItem bestItem = this.getConfig().getBestAxe(this);
      return bestItem != null ? bestItem.staticStackArray : WOODDEN_AXE_STACK;
   }

   public ItemStack[] getBestHoeStack() {
      InvItem bestItem = this.getConfig().getBestHoe(this);
      return bestItem != null ? bestItem.staticStackArray : WOODDEN_HOE_STACK;
   }

   public ItemTool getBestPickaxe() {
      InvItem bestItem = this.getConfig().getBestPickaxe(this);
      return bestItem != null ? (ItemTool) bestItem.item : (ItemTool) Items.WOODEN_PICKAXE;
   }

   public ItemStack[] getBestPickaxeStack() {
      InvItem bestItem = this.getConfig().getBestPickaxe(this);
      return bestItem != null ? bestItem.staticStackArray : WOODDEN_PICKAXE_STACK;
   }

   public ItemTool getBestShovel() {
      InvItem bestItem = this.getConfig().getBestShovel(this);
      return bestItem != null ? (ItemTool) bestItem.item : (ItemTool) Items.WOODEN_SHOVEL;
   }

   public ItemStack[] getBestShovelStack() {
      InvItem bestItem = this.getConfig().getBestShovel(this);
      return bestItem != null ? bestItem.staticStackArray : WOODDEN_SHOVEL_STACK;
   }

   public Block getBlock(Point p) {
      return WorldUtilities.getBlock(this.world, p);
   }

   public int getBlockMeta(Point p) {
      return WorldUtilities.getBlockMeta(this.world, p);
   }

   public float getBlockPathWeight(BlockPos pos) {
      if (!this.allowRandomMoves) {
         if (MillConfigValues.LogPathing >= 3 && this.extraLog) {
            MillLog.debug(this,
                  "Forbiding random moves. Current goal: " + Goal.goals.get(this.goalKey) + " Returning: " + -99999.0F);
         }

         return Float.NEGATIVE_INFINITY;
      } else {
         Point rp = new Point(pos);
         double dist = rp.distanceTo(this.housePoint);
         if (WorldUtilities.getBlock(this.world, rp.getBelow()) == Blocks.FARMLAND) {
            return -50.0F;
         } else {
            return dist > 10.0 ? -((float) dist) : MillCommonUtilities.randomInt(10);
         }
      }
   }

   public EntityItem getClosestItemVertical(List<InvItem> goods, int radius, int vertical) {
      return WorldUtilities.getClosestItemVertical(this.world, this.getPos(), goods, radius, vertical);
   }

   public ResourceLocation getClothTexturePath(int layer) {
      return this.clothTexture[layer];
   }

   public VillagerConfig getConfig() {
      return this.vtype != null && this.vtype.villagerConfig != null ? this.vtype.villagerConfig
            : VillagerConfig.DEFAULT_CONFIG;
   }

   public Culture getCulture() {
      return this.vtype == null ? null : this.vtype.culture;
   }

   public ConstructionIP getCurrentConstruction() {
      if (this.constructionJobId > -1
            && this.constructionJobId < this.getTownHall().getConstructionsInProgress().size()) {
         ConstructionIP cip = this.getTownHall().getConstructionsInProgress().get(this.constructionJobId);
         if (cip.getBuilder() == null || cip.getBuilder() == this) {
            return cip;
         }
      }

      return null;
   }

   public Goal getCurrentGoal() {
      return Goal.goals.containsKey(this.goalKey) ? Goal.goals.get(this.goalKey) : null;
   }

   protected int getExperiencePoints(EntityPlayer par1EntityPlayer) {
      return this.vtype.expgiven;
   }

   public String getFemaleChild() {
      return this.vtype.femaleChild;
   }

   public String getGameOccupationName(String playername) {
      if (this.getCulture() == null || this.vtype == null || this.getRecord() == null) {
         return "";
      } else if (!this.getCulture().canReadVillagerNames()) {
         return "";
      } else {
         return this.isChild() && this.getSize() == 20
               ? this.getCulture().getCultureString("villager." + this.vtype.altkey)
               : this.getCulture().getCultureString("villager." + this.vtype.key);
      }
   }

   public String getGameSpeech(String playername) {
      if (this.getCulture() == null) {
         return null;
      } else {
         String speech = VillageUtilities.getVillagerSentence(this, playername, false);
         if (speech != null) {
            int duration = 10 + speech.length() / 5;
            duration = Math.min(duration, 30);
            if (this.speech_started + 20 * duration < this.world.getWorldTime()) {
               return null;
            }
         }

         return speech;
      }
   }

   public int getGatheringRange() {
      return 20;
   }

   public String getGenderString() {
      return this.gender == 1 ? "male" : "female";
   }

   public Building getGoalBuildingDest() {
      return this.mw.getBuilding(this.getGoalBuildingDestPoint());
   }

   public Point getGoalBuildingDestPoint() {
      return this.goalInformation == null ? null : this.goalInformation.getDestBuildingPos();
   }

   public Entity getGoalDestEntity() {
      return this.goalInformation == null ? null : this.goalInformation.getTargetEnt();
   }

   public Point getGoalDestPoint() {
      return this.goalInformation == null ? null : this.goalInformation.getDest();
   }

   public String getGoalLabel(String goal) {
      return Goal.goals.containsKey(goal) ? Goal.goals.get(goal).gameName(this) : "none";
   }

   public List<Goal> getGoals() {
      return this.vtype != null ? this.vtype.goals : null;
   }

   public List<InvItem> getGoodsToBringBackHome() {
      return this.vtype.bringBackHomeGoods;
   }

   public List<InvItem> getGoodsToCollect() {
      return this.vtype.collectGoods;
   }

   public int getHireCost(EntityPlayer player) {
      int cost = this.vtype.hireCost;
      if (this.getTownHall().controlledBy(player)) {
         cost /= 2;
      }

      return cost;
   }

   public Building getHouse() {
      if (this.house != null) {
         return this.house;
      } else {
         if (MillConfigValues.LogVillager >= 3 && this.extraLog) {
            MillLog.debug(this, "Seeking uncached house");
         }

         if (this.mw != null) {
            this.house = this.mw.getBuilding(this.housePoint);
            return this.house;
         } else {
            return null;
         }
      }
   }

   public Set<InvItem> getInventoryKeys() {
      return this.inventory.keySet();
   }

   public List<InvItem> getItemsNeeded() {
      return this.vtype.itemsNeeded;
   }

   public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
      if (slotIn == EntityEquipmentSlot.HEAD) {
         for (InvItem item : this.getConfig().armoursHelmetSorted) {
            if (this.countInv(item) > 0) {
               return item.getItemStack();
            }
         }

         return ItemStack.EMPTY;
      } else if (slotIn == EntityEquipmentSlot.CHEST) {
         for (InvItem itemx : this.getConfig().armoursChestplateSorted) {
            if (this.countInv(itemx) > 0) {
               return itemx.getItemStack();
            }
         }

         return ItemStack.EMPTY;
      } else if (slotIn == EntityEquipmentSlot.LEGS) {
         for (InvItem itemxx : this.getConfig().armoursLeggingsSorted) {
            if (this.countInv(itemxx) > 0) {
               return itemxx.getItemStack();
            }
         }

         return ItemStack.EMPTY;
      } else if (slotIn == EntityEquipmentSlot.FEET) {
         for (InvItem itemxxx : this.getConfig().armoursBootsSorted) {
            if (this.countInv(itemxxx) > 0) {
               return itemxxx.getItemStack();
            }
         }

         return ItemStack.EMPTY;
      } else if (this.heldItem != null && slotIn == EntityEquipmentSlot.MAINHAND) {
         return this.heldItem;
      } else {
         return this.heldItemOffHand != null && slotIn == EntityEquipmentSlot.OFFHAND ? this.heldItemOffHand
               : ItemStack.EMPTY;
      }
   }

   public String getMaleChild() {
      return this.vtype.maleChild;
   }

   public String getName() {
      return this.firstName + " " + this.familyName;
   }

   public String getNameKey() {
      if (this.vtype != null && this.getRecord() != null) {
         return this.isChild() && this.getSize() == 20 ? this.vtype.altkey : this.vtype.key;
      } else {
         return "";
      }
   }

   public String getNativeOccupationName() {
      if (this.vtype == null) {
         return null;
      } else {
         return this.isChild() && this.getSize() == 20 ? this.vtype.altname : this.vtype.name;
      }
   }

   public String getNativeSpeech(String playername) {
      if (this.getCulture() == null) {
         return null;
      } else {
         String speech = VillageUtilities.getVillagerSentence(this, playername, true);
         if (speech != null) {
            int duration = 10 + speech.length() / 5;
            duration = Math.min(duration, 30);
            if (this.speech_started + 20 * duration < this.world.getWorldTime()) {
               return null;
            }
         }

         return speech;
      }
   }

   public Point getPathDestPoint() {
      return this.pathDestPoint;
   }

   private AStarConfig getPathingConfig() {
      return this.getActiveGoal() != null ? this.getActiveGoal().getPathingConfig(this)
            : this.getVillagerPathingConfig();
   }

   public PathPoint getPathPointPos() {
      return new PathPoint(
            MathHelper.floor(this.getEntityBoundingBox().minX),
            MathHelper.floor(this.getEntityBoundingBox().minY),
            MathHelper.floor(this.getEntityBoundingBox().minZ));
   }

   public Point getPos() {
      return new Point(this.posX, this.posY, this.posZ);
   }

   public EnumHandSide getPrimaryHand() {
      return this.getRecord() != null && this.getRecord().rightHanded ? EnumHandSide.RIGHT : EnumHandSide.LEFT;
   }

   public String getRandomFamilyName() {
      return this.getCulture().getRandomNameFromList(this.vtype.familyNameList);
   }

   public VillagerRecord getRecord() {
      return this.mw == null ? null : this.mw.getVillagerRecordById(this.getVillagerId());
   }

   public int getSize() {
      return this.getRecord() == null ? 0 : this.getRecord().size;
   }

   public MillVillager getSpouse() {
      if (this.getHouse() != null && !this.isChild()) {
         for (MillVillager v : this.getHouse().getKnownVillagers()) {
            if (!v.isChild() && v.gender != this.gender) {
               return v;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public ResourceLocation getTexture() {
      return this.texture;
   }

   public List<String> getToolsCategoriesNeeded() {
      return this.vtype.toolsCategoriesNeeded;
   }

   public int getTotalArmorValue() {
      return this.getRecord() == null ? 0 : this.getRecord().getTotalArmorValue();
   }

   public Building getTownHall() {
      if (this.townHall != null) {
         return this.townHall;
      } else {
         if (MillConfigValues.LogVillager >= 3 && this.extraLog) {
            MillLog.debug(this, "Seeking uncached townHall");
         }

         if (this.mw != null) {
            this.townHall = this.mw.getBuilding(this.townHallPoint);
            return this.townHall;
         } else {
            return null;
         }
      }
   }

   public long getVillagerId() {
      return this.villagerId;
   }

   public AStarConfig getVillagerPathingConfig() {
      return this.vtype.noleafclearing ? JPS_CONFIG_NO_LEAVES : JPS_CONFIG_DEFAULT;
   }

   public ItemStack getWeapon() {
      if (this.vtype == null) {
         return ItemStack.EMPTY;
      } else {
         if (this.isUsingBow) {
            InvItem weapon = this.getConfig().getBestWeaponRanged(this);
            if (weapon != null) {
               return weapon.getItemStack();
            }
         }

         if (this.isUsingHandToHand || !this.vtype.isArcher) {
            InvItem weapon = this.getConfig().getBestWeaponHandToHand(this);
            if (weapon != null) {
               return weapon.getItemStack();
            }
         }

         return this.vtype.startingWeapon != null ? this.vtype.startingWeapon.getItemStack() : ItemStack.EMPTY;
      }
   }

   public void growSize() {
      if (this.getRecord() != null) {
         int growth = 2;
         int nb = 0;
         nb = this.getHouse().takeGoods(Items.EGG, 1);
         if (nb == 1) {
            growth += 1 + MillCommonUtilities.randomInt(5);
         }

         for (InvItem food : this.getConfig().foodsGrowthSorted) {
            if (growth < 10 && this.getRecord().size + growth < 20 && this.getHouse().countGoods(food) > 0) {
               this.getHouse().takeGoods(food, 1);
               growth += this.getConfig().foodsGrowth.get(food)
                     + MillCommonUtilities.randomInt(this.getConfig().foodsGrowth.get(food));
            }
         }

         this.getRecord().size += growth;
         if (this.getRecord().size > 20) {
            this.getRecord().size = 20;
         }

         this.computeChildScale();
         if (MillConfigValues.LogChildren >= 2) {
            MillLog.minor(this, "Child growing by " + growth + ", new size: " + this.getRecord().size);
         }
      }
   }

   private void handleDoorsAndFenceGates() {
      if (this.doorToClose != null
            && (this.pathEntity == null
                  || this.pathEntity.getCurrentPathLength() == 0
                  || this.pathEntity.getPastTargetPathPoint(2) != null
                        && this.doorToClose.sameBlock(this.pathEntity.getPastTargetPathPoint(2)))) {
         if (BlockItemUtilities.isWoodenDoor(this.getBlock(this.doorToClose))) {
            if ((Boolean) this.doorToClose.getBlockActualState(this.world).getValue(BlockDoor.OPEN)) {
               this.toggleDoor(this.doorToClose);
            }

            for (Point nearbyDoor : new Point[] {
                  this.doorToClose.getNorth(), this.doorToClose.getSouth(), this.doorToClose.getEast(),
                  this.doorToClose.getWest()
            }) {
               if (BlockItemUtilities.isWoodenDoor(this.getBlock(nearbyDoor))
                     && (Boolean) nearbyDoor.getBlockActualState(this.world).getValue(BlockDoor.OPEN)) {
                  this.toggleDoor(nearbyDoor);
               }
            }

            this.doorToClose = null;
         } else if (BlockItemUtilities.isFenceGate(this.getBlock(this.doorToClose))) {
            if (this.closeFenceGate(this.doorToClose.getiX(), this.doorToClose.getiY(), this.doorToClose.getiZ())) {
               this.doorToClose = null;
            }
         } else {
            this.doorToClose = null;
         }
      }

      if (this.pathEntity != null && this.pathEntity.getCurrentPathLength() > 0) {
         PathPoint p = null;
         if (this.pathEntity.getCurrentTargetPathPoint() != null) {
            Block currentTargetPathPointBlock = WorldUtilities.getBlock(
                  this.world,
                  this.pathEntity.getCurrentTargetPathPoint().x,
                  this.pathEntity.getCurrentTargetPathPoint().y,
                  this.pathEntity.getCurrentTargetPathPoint().z);
            if (BlockItemUtilities.isWoodenDoor(currentTargetPathPointBlock)) {
               p = this.pathEntity.getCurrentTargetPathPoint();
            }
         } else if (this.pathEntity.getNextTargetPathPoint() != null) {
            Block nextTargetPathPointBlock = WorldUtilities.getBlock(
                  this.world,
                  this.pathEntity.getNextTargetPathPoint().x,
                  this.pathEntity.getNextTargetPathPoint().y,
                  this.pathEntity.getNextTargetPathPoint().z);
            if (BlockItemUtilities.isWoodenDoor(nextTargetPathPointBlock)) {
               p = this.pathEntity.getNextTargetPathPoint();
            }
         }

         if (p != null) {
            Point point = new Point(p);
            if (!(Boolean) point.getBlockActualState(this.world).getValue(BlockDoor.OPEN)) {
               this.toggleDoor(new Point(p));
            }

            this.doorToClose = new Point(p);
         } else {
            if (this.pathEntity.getNextTargetPathPoint() != null
                  && BlockItemUtilities.isFenceGate(
                        WorldUtilities.getBlock(
                              this.world,
                              this.pathEntity.getNextTargetPathPoint().x,
                              this.pathEntity.getNextTargetPathPoint().y,
                              this.pathEntity.getNextTargetPathPoint().z))) {
               p = this.pathEntity.getNextTargetPathPoint();
            } else if (this.pathEntity.getCurrentTargetPathPoint() != null
                  && BlockItemUtilities.isFenceGate(
                        WorldUtilities.getBlock(
                              this.world,
                              this.pathEntity.getCurrentTargetPathPoint().x,
                              this.pathEntity.getCurrentTargetPathPoint().y,
                              this.pathEntity.getCurrentTargetPathPoint().z))) {
               p = this.pathEntity.getCurrentTargetPathPoint();
            }

            if (p != null) {
               Point point = new Point(p);
               this.openFenceGate(p.x, p.y, p.z);
               this.doorToClose = point;
            }
         }
      }
   }

   private void handleLeaveClearing() {
      if (this.pathEntity != null && this.pathEntity.getCurrentPathLength() > 0) {
         List<Point> pointsToCheck = new ArrayList<>();
         if (this.pathEntity.getCurrentTargetPathPoint() != null) {
            Point p = new Point(this.pathEntity.getCurrentTargetPathPoint());
            pointsToCheck.add(p);
            pointsToCheck.add(p.getAbove());
         }

         if (this.pathEntity.getNextTargetPathPoint() != null) {
            Point p = new Point(this.pathEntity.getNextTargetPathPoint());

            for (int dx = -1; dx < 2; dx++) {
               for (int dz = -1; dz < 2; dz++) {
                  pointsToCheck.add(p.getRelative(dx, 0.0, dz));
                  pointsToCheck.add(p.getRelative(dx, 1.0, dz));
               }
            }
         }

         for (Point point : pointsToCheck) {
            IBlockState blockState = point.getBlockActualState(this.world);
            if (blockState.getBlock() instanceof BlockLeaves) {
               if (blockState.getBlock() != Blocks.LEAVES && blockState.getBlock() != Blocks.LEAVES2) {
                  if (!(blockState.getBlock() instanceof BlockFruitLeaves)) {
                     if (BlockStateUtilities.hasPropertyByName(blockState, "decayable")) {
                        if ((Boolean) blockState.getValue(BlockLeaves.DECAYABLE)) {
                           WorldUtilities.setBlock(this.world, point, Blocks.AIR, true, true);
                        }
                     } else {
                        WorldUtilities.setBlock(this.world, point, Blocks.AIR, true, true);
                     }
                  }
               } else if ((Boolean) blockState.getValue(BlockLeaves.DECAYABLE)) {
                  WorldUtilities.setBlock(this.world, point, Blocks.AIR, true, true);
               }
            }
         }
      }
   }

   private boolean hasBow() {
      return this.getConfig().getBestWeaponRanged(this) != null;
   }

   public boolean hasChildren() {
      return this.vtype.maleChild != null && this.vtype.femaleChild != null;
   }

   @Override
   public int hashCode() {
      return (int) this.getVillagerId();
   }

   public boolean helpsInAttacks() {
      return this.vtype.helpInAttacks;
   }

   public void interactDev(EntityPlayer entityplayer) {
      DevModUtilities.villagerInteractDev(entityplayer, this);
   }

   public boolean interactSpecial(EntityPlayer entityplayer) {
      if (this.getTownHall() == null) {
         MillLog.error(this, "Trying to interact with a villager with no TH.");
      }

      if (this.getHouse() == null) {
         MillLog.error(this, "Trying to interact with a villager with no house.");
      }

      if (this.isChief()) {
         ServerSender.displayVillageChiefGUI(entityplayer, this);
         return true;
      } else {
         UserProfile profile = this.mw.getProfile(entityplayer);
         if (this.canMeditate() && this.mw.isGlobalTagSet("pujas")
               || this.canPerformSacrifices() && this.mw.isGlobalTagSet("mayansacrifices")) {
            if (MillConfigValues.LogPujas >= 3) {
               MillLog.debug(this, "canMeditate");
            }

            if (this.getTownHall().getReputation(entityplayer) < -1024) {
               ServerSender.sendTranslatedSentence(entityplayer, 'f', "ui.sellerboycott", this.getName());
               return false;
            }

            for (BuildingLocation l : this.getTownHall().getLocations()) {
               if (l.level >= 0 && l.getSellingPos() != null && l.getSellingPos().distanceTo(this) < 8.0) {
                  Building b = l.getBuilding(this.world);
                  if (b.pujas != null) {
                     if (MillConfigValues.LogPujas >= 3) {
                        MillLog.debug(this, "Found shrine: " + b);
                     }

                     Point p = b.getPos();
                     entityplayer.openGui(Mill.instance, 6, this.world, p.getiX(), p.getiY(), p.getiZ());
                     return true;
                  }
               }
            }
         }

         System.out.println("[MillVillager DEBUG] interactSpecial - isSeller=" + this.isSeller()
               + " controlledBy="
               + (this.getTownHall() != null ? this.getTownHall().controlledBy(entityplayer) : "null TH"));
         if (this.isSeller() && !this.getTownHall().controlledBy(entityplayer)) {
            System.out.println("[MillVillager DEBUG] Seller check passed. chestLocked=" + this.getTownHall().chestLocked
                  + " reputation=" + this.getTownHall().getReputation(entityplayer));
            if (this.getTownHall().getReputation(entityplayer) < -1024 || !this.getTownHall().chestLocked) {
               if (!this.getTownHall().chestLocked) {
                  System.out.println("[MillVillager DEBUG] Chest NOT locked - cannot trade");
                  ServerSender.sendTranslatedSentence(entityplayer, 'f', "ui.sellernotcurrently possible",
                        this.getName());
                  return false;
               }

               ServerSender.sendTranslatedSentence(entityplayer, 'f', "ui.sellerboycott", this.getName());
               return false;
            }

            System.out.println("[MillVillager DEBUG] Searching for shop in " + this.getTownHall().getLocations().size()
                  + " locations");
            for (BuildingLocation lx : this.getTownHall().getLocations()) {
               double sellingDist = lx.getSellingPos() != null ? lx.getSellingPos().distanceTo(this) : 999;
               double sleepingDist = lx.sleepingPos != null ? lx.sleepingPos.distanceTo(this) : 999;
               System.out.println("[MillVillager DEBUG] Location: level=" + lx.level + " shop=" + lx.shop
                     + " sellingDist=" + sellingDist + " sleepingDist=" + sleepingDist);
               if (lx.level >= 0
                     && lx.shop != null
                     && lx.shop.length() > 0
                     && (lx.getSellingPos() != null && lx.getSellingPos().distanceTo(this) < 5.0
                           || lx.sleepingPos.distanceTo(this) < 5.0)) {
                  System.out.println("[MillVillager DEBUG] Found matching shop! Opening trade GUI");
                  ServerSender.displayVillageTradeGUI(entityplayer, lx.getBuilding(this.world));
                  return true;
               }
            }
            System.out.println("[MillVillager DEBUG] No matching shop found within range");
         }

         if (this.isForeignMerchant()) {
            ServerSender.displayMerchantTradeGUI(entityplayer, this);
            return true;
         } else if (this.vtype.hireCost > 0) {
            if (this.hiredBy != null && !this.hiredBy.equals(entityplayer.getName())) {
               ServerSender.sendTranslatedSentence(entityplayer, 'f', "hire.hiredbyotherplayer", this.getName(),
                     this.hiredBy);
               return false;
            } else {
               ServerSender.displayHireGUI(entityplayer, this);
               return true;
            }
         } else if (this.isLocalMerchant() && !profile.villagersInQuests.containsKey(this.getVillagerId())) {
            ServerSender.sendTranslatedSentence(entityplayer, '6', "other.localmerchantinteract", this.getName());
            return false;
         } else {
            return false;
         }
      }
   }

   public boolean isChief() {
      return this.vtype.isChief;
   }

   public boolean isChild() {
      return this.vtype == null ? false : this.vtype.isChild;
   }

   public boolean isForeignMerchant() {
      return this.vtype.isForeignMerchant;
   }

   public boolean isHostile() {
      return this.vtype.hostile;
   }

   public boolean isLocalMerchant() {
      return this.vtype.isLocalMerchant;
   }

   protected boolean isMovementBlocked() {
      return this.getHealth() <= 0.0F || this.isVillagerSleeping();
   }

   public boolean isReallyDead() {
      return this.isDead && this.getHealth() <= 0.0F;
   }

   public boolean isSeller() {
      return this.vtype.canSell;
   }

   public boolean isTextureValid(String texture) {
      return this.vtype != null ? this.vtype.isTextureValid(texture) : true;
   }

   public boolean isVillagerSleeping() {
      return this.shouldLieDown;
   }

   public boolean isVisitor() {
      return this.vtype == null ? false : this.vtype.visitor;
   }

   private void jumpToDest() {
      Point jumpTo = WorldUtilities.findVerticalStandingPos(this.world, this.getPathDestPoint());
      if (jumpTo != null && jumpTo.distanceTo(this.getPathDestPoint()) < 4.0) {
         if (MillConfigValues.LogPathing >= 1 && this.extraLog) {
            MillLog.major(this, "Jumping from " + this.getPos() + " to " + jumpTo);
         }

         this.setPosition(jumpTo.getiX() + 0.5, jumpTo.getiY() + 0.5, jumpTo.getiZ() + 0.5);
         this.longDistanceStuck = 0;
         this.localStuck = 0;
      } else if (this.goalKey != null && Goal.goals.containsKey(this.goalKey)) {
         Goal goal = Goal.goals.get(this.goalKey);

         try {
            goal.unreachableDestination(this);
         } catch (Exception var4) {
            MillLog.printException(this + ": Exception in handling unreachable dest for goal " + this.goalKey, var4);
         }
      }
   }

   public void killVillager() {
      if (!this.world.isRemote && this.world instanceof WorldServer) {
         for (InvItem iv : this.inventory.keySet()) {
            if (this.inventory.get(iv) > 0) {
               WorldUtilities.spawnItem(this.world, this.getPos(),
                     new ItemStack(iv.getItem(), this.inventory.get(iv), iv.meta), 0.0F);
            }
         }

         if (this.hiredBy != null) {
            EntityPlayer owner = this.world.getPlayerEntityByName(this.hiredBy);
            if (owner != null) {
               ServerSender.sendTranslatedSentence(owner, 'f', "hire.hiredied", this.getName());
            }
         }

         VillagerRecord vr = this.getRecord();
         if (vr != null) {
            if (MillConfigValues.LogGeneralAI >= 1) {
               MillLog.major(this, this.getTownHall() + ": Villager has been killed!");
            }

            vr.killed = true;
         }

         super.setDead();
      } else {
         super.setDead();
      }
   }

   private void leaveVillage() {
      for (InvItem iv : this.vtype.foreignMerchantStock.keySet()) {
         this.getHouse().takeGoods(iv.getItem(), iv.meta, this.vtype.foreignMerchantStock.get(iv));
      }

      this.mw.removeVillagerRecord(this.villagerId);
      this.despawnVillager();
   }

   public void localMerchantUpdate() throws Exception {
      if (this.getHouse() != null && this.getHouse() == this.getTownHall()) {
         List<Building> buildings = this.getTownHall().getBuildingsWithTag("inn");
         Building inn = null;

         for (Building building : buildings) {
            if (building.merchantRecord == null) {
               inn = building;
            }
         }

         if (inn == null) {
            this.mw.removeVillagerRecord(this.villagerId);
            this.despawnVillager();
            MillLog.error(this, "Merchant had Town Hall as house and inn is full. Killing him.");
         } else {
            this.setHousePoint(inn.getPos());
            VillagerRecord vr = this.getRecord();
            vr.updateRecord(this);
            this.mw.registerVillagerRecord(vr, true);
            MillLog.error(this, "Merchant had Town Hall as house. Moving him to the inn.");
         }
      }
   }

   public void onDeath(DamageSource cause) {
      super.onDeath(cause);
   }

   @Override
   public void onFoundPath(List<AStarNode> result) {
      this.pathCalculatedSinceLastTick = result;
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      this.updateArmSwingProgress();
      this.setFacingDirection();
      if (this.isVillagerSleeping()) {
         this.motionX = 0.0;
         this.motionY = 0.0;
         this.motionZ = 0.0;
      }
   }

   @Override
   public void onNoPathAvailable() {
      this.pathFailedSincelastTick = true;
   }

   public void onUpdate() {
      long startTime = System.nanoTime();
      // Only check dimension on server side
      if (!this.world.isRemote && this.world.provider.getDimension() != 0) {
         this.despawnVillagerSilent();
      }

      try {
         // On client, just run super.onUpdate() if vtype is null (waiting for spawn
         // data)
         if (this.vtype == null) {
            if (this.world.isRemote) {
               // Client side: vtype may be null before spawn data arrives, just wait
               super.onUpdate();
               return;
            }
            // Server side: vtype should never be null, despawn if so
            if (!this.isDead) {
               MillLog.error(this, "Unknown villager type. Killing him.");
               this.despawnVillagerSilent();
            }
            return;
         }

         if (this.pathFailedSincelastTick) {
            this.pathFailedSinceLastTick();
         }

         if (this.pathCalculatedSinceLastTick != null) {
            this.applyPathCalculatedSinceLastTick();
         }

         if (this.world.isRemote) {
            super.onUpdate();
            return;
         }

         if (this.isDead) {
            super.onUpdate();
            return;
         }

         if (Math.abs(this.world.getWorldTime() + this.hashCode()) % 10L == 2L) {
            this.sendVillagerPacket();
         }

         if (Math.abs(this.world.getWorldTime() + this.hashCode()) % 40L == 4L) {
            this.unlockForNearbyPlayers();
         }

         if (this.hiredBy != null) {
            this.updateHired();
            super.onUpdate();
            return;
         }

         if (this.getTownHall() == null || this.getHouse() == null) {
            return;
         }

         if (this.getTownHall() != null && !this.getTownHall().isActive) {
            return;
         }

         if (this.getPos().distanceTo(this.getTownHall().getPos()) > this.getTownHall().villageType.radius + 100) {
            MillLog.error(this, "Villager is far away from village. Despawning him.");
            this.despawnVillagerSilent();
         }

         try {
            this.timer++;
            if (this.getHealth() < this.getMaxHealth() & MillCommonUtilities.randomInt(1600) == 0) {
               this.setHealth(this.getHealth() + 1.0F);
            }

            this.detrampleCrops();
            this.allowRandomMoves = true;
            this.stopMoving = false;
            if (this.getTownHall() == null || this.getHouse() == null) {
               super.onUpdate();
               return;
            }

            if (Goal.beSeller.key.equals(this.goalKey)) {
               this.townHall.seller = this;
            } else if (Goal.getResourcesForBuild.key.equals(this.goalKey)
                  || Goal.construction.key.equals(this.goalKey)) {
               if (MillConfigValues.LogTileEntityBuilding >= 3) {
                  MillLog.debug(this, "Registering as builder for: " + this.townHall);
               }

               if (this.constructionJobId > -1
                     && this.townHall.getConstructionsInProgress().size() > this.constructionJobId) {
                  this.townHall.getConstructionsInProgress().get(this.constructionJobId).setBuilder(this);
               }
            }

            if (this.getTownHall().underAttack) {
               if (this.goalKey == null
                     || !this.goalKey.equals(Goal.raidVillage.key) && !this.goalKey.equals(Goal.defendVillage.key)
                           && !this.goalKey.equals(Goal.hide.key)) {
                  this.clearGoal();
               }

               if (this.isRaider) {
                  this.goalKey = Goal.raidVillage.key;
                  this.targetDefender();
               } else if (this.helpsInAttacks()) {
                  this.goalKey = Goal.defendVillage.key;
                  this.targetRaider();
               } else {
                  this.goalKey = Goal.hide.key;
               }

               this.checkGoals();
            }

            if (this.getAttackTarget() == null) {
               if (this.isHostile()
                     && this.world.getDifficulty() != EnumDifficulty.PEACEFUL
                     && this.getTownHall().closestPlayer != null
                     && this.getPos().distanceTo(this.getTownHall().closestPlayer) <= 80.0) {
                  int range = 80;
                  if (this.vtype.isDefensive) {
                     range = 20;
                  }

                  this.setAttackTarget(this.world.getClosestPlayer(this.posX, this.posY, this.posZ, range, true));
                  this.clearGoal();
               }
            } else {
               if (this.vtype.isDefensive
                     && this.getPos().distanceTo(this.getHouse().getResManager().getDefendingPos()) > 20.0) {
                  this.setAttackTarget(null);
               } else if (!this.getAttackTarget().isEntityAlive()
                     || this.getPos().distanceTo(this.getAttackTarget()) > 80.0
                     || this.world.getDifficulty() == EnumDifficulty.PEACEFUL
                           && this.getAttackTarget() instanceof EntityPlayer) {
                  this.setAttackTarget(null);
               }

               if (this.getAttackTarget() != null) {
                  this.shouldLieDown = false;
                  this.attackEntity(this.getAttackTarget());
                  if (!this.getAttackTarget().isAirBorne) {
                     this.setPathDestPoint(new Point(this.getAttackTarget()), 1);
                  } else {
                     Point posToAttack = new Point(this.getAttackTarget());

                     while (posToAttack.y > 0.0 && posToAttack.isBlockPassable(this.world)) {
                        posToAttack = posToAttack.getBelow();
                     }

                     if (posToAttack != null) {
                        this.setPathDestPoint(posToAttack.getAbove(), 3);
                     }
                  }
               }
            }

            if (this.getAttackTarget() != null) {
               this.setGoalDestPoint(new Point(this.getAttackTarget()));
               this.heldItem = this.getWeapon();
               this.heldItemOffHand = ItemStack.EMPTY;
               if (this.goalKey != null && !Goal.goals.get(this.goalKey).isFightingGoal()) {
                  this.clearGoal();
               }
            } else if (!this.getTownHall().underAttack) {
               if (this.world.isDaytime()) {
                  this.speakSentence("greeting", 12000, 3, 10);
                  this.nightActionPerformed = false;
                  List<InvItem> goods = this.getGoodsToCollect();
                  if (goods != null && (this.world.getWorldTime() + this.getVillagerId()) % 20L == 0L) {
                     EntityItem item = this.getClosestItemVertical(goods, 5, 30);
                     if (item != null) {
                        item.setDead();
                        if (item.getItem().getItem() == Item.getItemFromBlock(Blocks.SAPLING)) {
                           this.addToInv(item.getItem().getItem(), item.getItem().getItemDamage() & 3, 1);
                        } else {
                           this.addToInv(item.getItem().getItem(), item.getItem().getItemDamage(), 1);
                        }
                     }
                  }

                  this.specificUpdate();
                  if (!this.isRaider) {
                     if (this.goalKey == null) {
                        this.setNextGoal();
                     }

                     if (this.goalKey != null) {
                        this.checkGoals();
                     } else {
                        this.shouldLieDown = false;
                     }
                  }
               } else if (!this.isRaider) {
                  if (this.goalKey == null) {
                     this.setNextGoal();
                  }

                  if (this.goalKey != null) {
                     this.checkGoals();
                  } else {
                     this.shouldLieDown = false;
                  }
               }
            }

            if (this.getPathDestPoint() != null && this.pathEntity != null && this.pathEntity.getCurrentPathLength() > 0
                  && !this.stopMoving) {
               double olddistance = this.prevPoint.horizontalDistanceToSquared(this.getPathDestPoint());
               double newdistance = this.getPos().horizontalDistanceToSquared(this.getPathDestPoint());
               if (olddistance - newdistance < 2.0E-4) {
                  this.longDistanceStuck++;
               } else {
                  this.longDistanceStuck--;
               }

               if (this.longDistanceStuck < 0) {
                  this.longDistanceStuck = 0;
               }

               if (this.pathEntity != null && this.pathEntity.getCurrentPathLength() > 1
                     && MillConfigValues.LogPathing >= 2 && this.extraLog) {
                  MillLog.minor(
                        this,
                        "Stuck: "
                              + this.longDistanceStuck
                              + " pos "
                              + this.getPos()
                              + " node: "
                              + this.pathEntity.getCurrentTargetPathPoint()
                              + " next node: "
                              + this.pathEntity.getNextTargetPathPoint()
                              + " dest: "
                              + this.getPathDestPoint());
               }

               if (this.longDistanceStuck > 3000
                     && (!this.vtype.noTeleport || this.getRecord() != null && this.getRecord().raidingVillage)) {
                  this.jumpToDest();
               }

               PathPoint nextPoint = this.pathEntity.getNextTargetPathPoint();
               if (nextPoint != null) {
                  olddistance = this.prevPoint.distanceToSquared(nextPoint);
                  newdistance = this.getPos().distanceToSquared(nextPoint);
                  if (olddistance - newdistance < 2.0E-4) {
                     this.localStuck += 4;
                  } else {
                     this.localStuck--;
                  }

                  if (this.localStuck < 0) {
                     this.localStuck = 0;
                  }

                  if (this.localStuck > 30) {
                     this.navigator.clearPath();
                     this.pathEntity = null;
                  }

                  if (this.localStuck > 100) {
                     this.setPosition(nextPoint.x + 0.5, nextPoint.y + 0.5, nextPoint.z + 0.5);
                     this.localStuck = 0;
                  }
               }
            } else {
               this.longDistanceStuck = 0;
               this.localStuck = 0;
            }

            if (this.getPathDestPoint() != null && !this.stopMoving) {
               this.updatePathIfNeeded(this.getPathDestPoint());
            }

            if (this.stopMoving || this.pathPlannerJPS.isBusy()) {
               this.navigator.clearPath();
               this.pathEntity = null;
            }

            this.prevPoint = this.getPos();
            if (this.canVillagerClearLeaves() && Math.abs(this.world.getWorldTime() + this.hashCode()) % 10L == 6L) {
               this.handleLeaveClearing();
            }

            this.handleDoorsAndFenceGates();
            if (System.currentTimeMillis() - this.timeSinceLastPathingTimeDisplay > 10000L) {
               if (this.pathingTime > 500L) {
                  if (this.getPathDestPoint() != null) {
                     MillLog.warning(
                           this,
                           "Pathing time in last 10 secs: "
                                 + this.pathingTime
                                 + " dest: "
                                 + this.getPathDestPoint()
                                 + " dest bid: "
                                 + WorldUtilities.getBlock(this.world, this.getPathDestPoint())
                                 + " above bid: "
                                 + WorldUtilities.getBlock(this.world, this.getPathDestPoint().getAbove()));
                  } else {
                     MillLog.warning(this, "Pathing time in last 10 secs: " + this.pathingTime + " null dest point.");
                  }

                  MillLog.warning(
                        this,
                        "nbPathsCalculated: "
                              + this.nbPathsCalculated
                              + " nbPathNoStart: "
                              + this.nbPathNoStart
                              + " nbPathNoEnd: "
                              + this.nbPathNoEnd
                              + " nbPathAborted: "
                              + this.nbPathAborted
                              + " nbPathFailure: "
                              + this.nbPathFailure);
                  if (this.goalKey != null) {
                     MillLog.warning(this, "Current goal: " + Goal.goals.get(this.goalKey));
                  }
               }

               this.timeSinceLastPathingTimeDisplay = System.currentTimeMillis();
               this.pathingTime = 0L;
               this.nbPathsCalculated = 0;
               this.nbPathNoStart = 0;
               this.nbPathNoEnd = 0;
               this.nbPathAborted = 0;
               this.nbPathFailure = 0;
            }
         } catch (MillLog.MillenaireException var8) {
            Mill.proxy.sendChatAdmin(this.getName() + ": Error in onUpdate(). Check millenaire.log.");
            MillLog.error(this, var8.getMessage());
         } catch (Exception var9) {
            Mill.proxy.sendChatAdmin(this.getName() + ": Error in onUpdate(). Check millenaire.log.");
            MillLog.error(this, "Exception in Villager.onUpdate(): ");
            MillLog.printException(var9);
         }

         if (Math.abs(this.world.getWorldTime() + this.hashCode()) % 10L == 5L) {
            this.triggerMobAttacks();
         }

         this.updateDialogue();
         this.isUsingBow = false;
         this.isUsingHandToHand = false;
         super.onUpdate();
         if (MillConfigValues.DEV) {
            if (this.getPathDestPoint() != null && !this.pathPlannerJPS.isBusy() && this.pathEntity == null) {
            }

            if (this.getPathDestPoint() != null && this.getGoalDestPoint() != null
                  && this.getPathDestPoint().distanceTo(this.getGoalDestPoint()) > 20.0) {
            }
         }
      } catch (Exception var10) {
         MillLog.printException("Exception in onUpdate() of villager: " + this, var10);
      }

      if (this.getTownHall() != null) {
         this.mw.reportTime(this.getTownHall(), System.nanoTime() - startTime, true);
      }
   }

   private boolean openFenceGate(int i, int j, int k) {
      Point p = new Point(i, j, k);
      IBlockState state = p.getBlockActualState(this.world);
      if (BlockItemUtilities.isFenceGate(state.getBlock()) && !(Boolean) state.getValue(BlockFenceGate.OPEN)) {
         p.setBlockState(this.world, state.withProperty(BlockFenceGate.OPEN, true));
      }

      return true;
   }

   private void pathFailedSinceLastTick() {
      if (!this.vtype.noTeleport || this.getRecord() != null && this.getRecord().raidingVillage) {
         this.jumpToDest();
      }

      this.pathFailedSincelastTick = false;
   }

   public boolean performNightAction() {
      if (this.getRecord() != null && this.getHouse() != null && this.getTownHall() != null) {
         if (this.isChild()) {
            if (this.getSize() < 20) {
               this.growSize();
            } else {
               this.teenagerNightAction();
            }
         }

         if (this.getHouse().hasVisitors) {
            this.visitorNightAction();
         }

         return this.hasChildren() ? this.attemptChildConception() : true;
      } else {
         return false;
      }
   }

   public boolean processInteract(EntityPlayer entityplayer, EnumHand hand) {
      if (this.isVillagerSleeping()) {
         return true;
      } else {
         MillAdvancements.FIRST_CONTACT.grant(entityplayer);
         if (this.vtype != null && (this.vtype.key.equals("indian_sadhu") || this.vtype.key.equals("alchemist"))) {
            MillAdvancements.MAITRE_A_PENSER.grant(entityplayer);
         }

         if (this.world.isRemote) {
            return true;
         } else {
            UserProfile profile = this.mw.getProfile(entityplayer);
            if (profile.villagersInQuests.containsKey(this.getVillagerId())) {
               QuestInstance qi = profile.villagersInQuests.get(this.getVillagerId());
               if (qi.getCurrentVillager().id == this.getVillagerId()) {
                  ServerSender.displayQuestGUI(entityplayer, this);
               } else {
                  this.interactSpecial(entityplayer);
               }
            } else {
               this.interactSpecial(entityplayer);
            }

            if (MillConfigValues.DEV) {
               this.interactDev(entityplayer);
            }

            return true;
         }
      }
   }

   public int putInBuilding(Building building, Item item, int meta, int nb) {
      nb = this.takeFromInv(item, meta, nb);
      building.storeGoods(item, meta, nb);
      return nb;
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      String type = nbttagcompound.getString("vtype");
      String culture = nbttagcompound.getString("culture");
      if (Culture.getCultureByName(culture) != null) {
         if (Culture.getCultureByName(culture).getVillagerType(type) != null) {
            this.vtype = Culture.getCultureByName(culture).getVillagerType(type);
         } else {
            MillLog.error(this, "Could not load dynamic NPC: unknown type: " + type + " in culture: " + culture);
         }
      } else {
         MillLog.error(this, "Could not load dynamic NPC: unknown culture: " + culture);
      }

      this.texture = new ResourceLocation("millenaire", nbttagcompound.getString("texture"));
      this.housePoint = Point.read(nbttagcompound, "housePos");
      if (this.housePoint == null) {
         MillLog.error(this, "Error when loading villager: housePoint null");
         Mill.proxy.sendChatAdmin(this.getName() + ": Could not load house position. Check millenaire.log");
      }

      this.townHallPoint = Point.read(nbttagcompound, "townHallPos");
      if (this.townHallPoint == null) {
         MillLog.error(this, "Error when loading villager: townHallPoint null");
         Mill.proxy.sendChatAdmin(this.getName() + ": Could not load town hall position. Check millenaire.log");
      }

      this.setGoalDestPoint(Point.read(nbttagcompound, "destPoint"));
      this.setPathDestPoint(Point.read(nbttagcompound, "pathDestPoint"), 0);
      this.setGoalBuildingDestPoint(Point.read(nbttagcompound, "destBuildingPoint"));
      this.prevPoint = Point.read(nbttagcompound, "prevPoint");
      this.doorToClose = Point.read(nbttagcompound, "doorToClose");
      this.action = nbttagcompound.getInteger("action");
      this.goalKey = nbttagcompound.getString("goal");
      if (this.goalKey.trim().length() == 0) {
         this.goalKey = null;
      }

      if (this.goalKey != null && !Goal.goals.containsKey(this.goalKey)) {
         this.goalKey = null;
      }

      this.constructionJobId = nbttagcompound.getInteger("constructionJobId");
      this.dialogueKey = nbttagcompound.getString("dialogueKey");
      this.dialogueStart = nbttagcompound.getLong("dialogueStart");
      this.dialogueRole = nbttagcompound.getInteger("dialogueRole");
      this.dialogueColour = (char) nbttagcompound.getInteger("dialogueColour");
      this.dialogueChat = nbttagcompound.getBoolean("dialogueChat");
      if (this.dialogueKey.trim().length() == 0) {
         this.dialogueKey = null;
      }

      this.familyName = nbttagcompound.getString("familyName");
      this.firstName = nbttagcompound.getString("firstName");
      this.gender = nbttagcompound.getInteger("gender");
      if (nbttagcompound.hasKey("villager_lid")) {
         this.setVillagerId(Math.abs(nbttagcompound.getLong("villager_lid")));
      }

      if (!this.isTextureValid(this.texture.getPath())) {
         ResourceLocation newTexture = this.vtype.getNewTexture();
         MillLog.major(this,
               "Texture " + this.texture.getPath() + " cannot be found, replacing it with " + newTexture.getPath());
         this.texture = newTexture;
      }

      NBTTagList nbttaglist = nbttagcompound.getTagList("inventoryNew", 10);
      MillCommonUtilities.readInventory(nbttaglist, this.inventory);
      this.previousBlock = Block.getBlockById(nbttagcompound.getInteger("previousBlock"));
      this.previousBlockMeta = nbttagcompound.getInteger("previousBlockMeta");
      this.hiredBy = nbttagcompound.getString("hiredBy");
      this.hiredUntil = nbttagcompound.getLong("hiredUntil");
      this.aggressiveStance = nbttagcompound.getBoolean("aggressiveStance");
      this.isRaider = nbttagcompound.getBoolean("isRaider");
      this.visitorNbNights = nbttagcompound.getInteger("visitorNbNights");
      if (this.hiredBy.equals("")) {
         this.hiredBy = null;
      }

      if (nbttagcompound.hasKey("clothTexture")) {
         this.clothTexture[0] = new ResourceLocation("millenaire", nbttagcompound.getString("clothTexture"));
      } else {
         for (int i = 0; i < 2; i++) {
            if (nbttagcompound.getString("clothTexture_" + i).length() > 0) {
               String texture = nbttagcompound.getString("clothTexture_" + i);
               if (texture.contains(":")) {
                  this.clothTexture[i] = new ResourceLocation(texture);
               } else {
                  this.clothTexture[i] = new ResourceLocation("millenaire", texture);
               }
            } else {
               this.clothTexture[i] = null;
            }
         }
      }

      this.clothName = nbttagcompound.getString("clothName");
      if (this.clothName.equals("")) {
         this.clothName = null;

         for (int ix = 0; ix < 2; ix++) {
            this.clothTexture[ix] = null;
         }
      }

      this.updateClothTexturePath();

      // Register villager in global list after loading from NBT
      // This ensures the villager is tracked by MillWorldData after world reload
      if (!this.world.isRemote && this.mw != null && this.getVillagerId() > 0) {
         this.mw.registerVillager(this.getVillagerId(), this);
      }
   }

   public void readFromNBT(NBTTagCompound compound) {
      super.readFromNBT(compound);
   }

   public void readSpawnData(ByteBuf ds) {
      PacketBuffer data = new PacketBuffer(ds);

      try {
         this.setVillagerId(data.readLong());
         this.readVillagerStreamdata(data);
         // Register this villager with the client world so sync packets can find it
         if (this.world.isRemote && Mill.clientWorld != null) {
            Mill.clientWorld.registerVillager(this.getVillagerId(), this);
         }
      } catch (IOException var4) {
         MillLog.printException("Error in readSpawnData for villager " + this, var4);
      }
   }

   private void readVillagerStreamdata(PacketBuffer data) throws IOException {
      Culture culture = Culture.getCultureByName(StreamReadWrite.readNullableString(data));
      String vt = StreamReadWrite.readNullableString(data);
      if (culture != null) {
         this.vtype = culture.getVillagerType(vt);
      }

      this.texture = StreamReadWrite.readNullableResourceLocation(data);
      this.goalKey = StreamReadWrite.readNullableString(data);
      this.constructionJobId = data.readInt();
      this.housePoint = StreamReadWrite.readNullablePoint(data);
      this.townHallPoint = StreamReadWrite.readNullablePoint(data);
      this.firstName = StreamReadWrite.readNullableString(data);
      this.familyName = StreamReadWrite.readNullableString(data);
      this.gender = data.readInt();
      this.hiredBy = StreamReadWrite.readNullableString(data);
      this.aggressiveStance = data.readBoolean();
      this.hiredUntil = data.readLong();
      this.isUsingBow = data.readBoolean();
      this.isUsingHandToHand = data.readBoolean();
      this.isRaider = data.readBoolean();
      this.speech_key = StreamReadWrite.readNullableString(data);
      this.speech_variant = data.readInt();
      this.speech_started = data.readLong();
      this.heldItem = StreamReadWrite.readNullableItemStack(data);
      this.heldItemOffHand = StreamReadWrite.readNullableItemStack(data);
      this.inventory = StreamReadWrite.readInventory(data);
      this.clothName = StreamReadWrite.readNullableString(data);

      for (int i = 0; i < 2; i++) {
         this.clothTexture[i] = StreamReadWrite.readNullableResourceLocation(data);
      }

      this.setGoalDestPoint(StreamReadWrite.readNullablePoint(data));
      this.shouldLieDown = data.readBoolean();
      this.dialogueTargetFirstName = StreamReadWrite.readNullableString(data);
      this.dialogueTargetLastName = StreamReadWrite.readNullableString(data);
      this.dialogueColour = data.readChar();
      this.dialogueChat = data.readBoolean();
      this.setHealth(data.readFloat());
      this.visitorNbNights = data.readInt();
      UUID uuid = StreamReadWrite.readNullableUUID(data);
      if (uuid != null) {
         Entity targetEntity = WorldUtilities.getEntityByUUID(this.world, uuid);
         if (targetEntity != null && targetEntity instanceof EntityLivingBase) {
            this.setAttackTarget((EntityLivingBase) targetEntity);
         } else {
            this.setAttackTarget(null);
         }
      } else {
         this.setAttackTarget(null);
      }

      int nbMerchantSells = data.readInt();
      if (nbMerchantSells > -1) {
         this.merchantSells.clear();

         for (int i = 0; i < nbMerchantSells; i++) {
            try {
               TradeGood g = StreamReadWrite.readNullableGoods(data, culture);
               this.merchantSells.put(g, data.readInt());
            } catch (MillLog.MillenaireException var9) {
               MillLog.printException(var9);
            }
         }
      }

      int goalDestEntityID = data.readInt();
      if (goalDestEntityID != -1) {
         Entity ent = this.world.getEntityByID(goalDestEntityID);
         if (ent != null) {
            this.setGoalDestEntity(ent);
         }
      }

      this.isDeadOnServer = data.readBoolean();
      this.client_lastupdated = this.world.getWorldTime();
   }

   public void registerNewPath(AS_PathEntity path) throws Exception {
      if (path == null) {
         boolean handled = false;
         if (this.goalKey != null) {
            Goal goal = Goal.goals.get(this.goalKey);
            handled = goal.unreachableDestination(this);
         }

         if (!handled) {
            this.clearGoal();
         }
      } else {
         try {
            this.navigator.setPath(path, 0.5);
         } catch (Exception var4) {
            MillLog.major(null, "Goal : " + this.goalKey);
            MillLog.major(null,
                  "Path to : " + this.pathDestPoint.x + "/" + this.pathDestPoint.y + "/" + this.pathDestPoint.z);
            MillLog.printException(this + ": Pathing error detected", var4);
         }

         this.pathEntity = path;
         this.moveStrafing = 0.0F;
      }
   }

   public void registerNewPath(List<PathPoint> result) throws Exception {
      AS_PathEntity path = null;
      if (result != null) {
         PathPoint[] pointsCopy = new PathPoint[result.size()];
         int i = 0;

         for (PathPoint p : result) {
            if (p == null) {
               pointsCopy[i] = null;
            } else {
               PathPoint p2 = new PathPoint(p.x, p.y, p.z);
               pointsCopy[i] = p2;
            }

            i++;
         }

         path = new AS_PathEntity(pointsCopy);
      }

      this.registerNewPath(path);
   }

   public HashMap<InvItem, Integer> requiresGoods() {
      if (this.isChild() && this.getSize() < 20) {
         return this.vtype.requiredFoodAndGoods;
      } else {
         return this.hasChildren() && this.getHouse() != null && this.getHouse().getKnownVillagers().size() < 4
               ? this.vtype.requiredFoodAndGoods
               : this.vtype.requiredGoods;
      }
   }

   private void sendVillagerPacket() {
      PacketBuffer data = ServerSender.getPacketBuffer();

      try {
         data.writeInt(3);
         this.writeVillagerStreamData(data, false);
      } catch (IOException var3) {
         MillLog.printException(this + ": Error in sendVillagerPacket", var3);
      }

      ServerSender.sendPacketToPlayersInRange(data, this.getPos(), 100);
   }

   public boolean setBlock(Point p, Block block) {
      return WorldUtilities.setBlock(this.world, p, block, true, true);
   }

   public boolean setBlockAndMetadata(Point p, Block block, int metadata) {
      return WorldUtilities.setBlockAndMetadata(this.world, p, block, metadata, true, true);
   }

   public boolean setBlockMetadata(Point p, int metadata) {
      return WorldUtilities.setBlockMetadata(this.world, p, metadata);
   }

   public boolean setBlockstate(Point p, IBlockState bs) {
      return WorldUtilities.setBlockstate(this.world, p, bs, true, true);
   }

   public void setDead() {
      if (this.getHealth() <= 0.0F) {
         this.killVillager();
      }

      super.setDead();
   }

   private void setFacingDirection() {
      if (this.getAttackTarget() != null) {
         this.faceEntityMill(this.getAttackTarget(), 30.0F, 30.0F);
      } else {
         if (this.goalKey != null && (this.getGoalDestPoint() != null || this.getGoalDestEntity() != null)) {
            Goal goal = Goal.goals.get(this.goalKey);
            if (goal.lookAtGoal()) {
               if (this.getGoalDestEntity() != null
                     && this.getPos().distanceTo(this.getGoalDestEntity()) < goal.range(this)) {
                  this.faceEntityMill(this.getGoalDestEntity(), 10.0F, 10.0F);
               } else if (this.getGoalDestPoint() != null
                     && this.getPos().distanceTo(this.getGoalDestPoint()) < goal.range(this)) {
                  this.facePoint(this.getGoalDestPoint(), 10.0F, 10.0F);
               }
            }

            if (goal.lookAtPlayer()) {
               EntityPlayer player = this.world.getClosestPlayerToEntity(this, 10.0);
               if (player != null) {
                  this.faceEntityMill(player, 10.0F, 10.0F);
                  return;
               }
            }
         }
      }
   }

   public void setGoalBuildingDestPoint(Point newDest) {
      if (this.goalInformation == null) {
         this.goalInformation = new Goal.GoalInformation(null, null, null);
      }

      this.goalInformation.setDestBuildingPos(newDest);
   }

   public void setGoalDestEntity(Entity ent) {
      if (this.goalInformation == null) {
         this.goalInformation = new Goal.GoalInformation(null, null, null);
      }

      this.goalInformation.setTargetEnt(ent);
      if (ent != null) {
         this.setPathDestPoint(new Point(ent), 2);
      }

      if (ent instanceof MillVillager) {
         MillVillager v = (MillVillager) ent;
         this.dialogueTargetFirstName = v.firstName;
         this.dialogueTargetLastName = v.familyName;
      }
   }

   public void setGoalDestPoint(Point newDest) {
      if (this.goalInformation == null) {
         this.goalInformation = new Goal.GoalInformation(null, null, null);
      }

      this.goalInformation.setDest(newDest);
      this.setPathDestPoint(newDest, 0);
   }

   public void setGoalInformation(Goal.GoalInformation info) {
      this.goalInformation = info;
      if (info != null) {
         if (info.getTargetEnt() != null) {
            this.setPathDestPoint(new Point(info.getTargetEnt()), 2);
         } else if (info.getDest() != null) {
            this.setPathDestPoint(info.getDest(), 0);
         } else {
            this.setPathDestPoint(null, 0);
         }
      } else {
         this.setPathDestPoint(null, 0);
      }
   }

   public void setHousePoint(Point p) {
      this.housePoint = p;
      this.house = null;
   }

   public void setInv(Item item, int meta, int nb) {
      this.inventory.put(InvItem.createInvItem(item, meta), nb);
      this.updateVillagerRecord();
   }

   public void setNextGoal() throws Exception {
      Goal nextGoal = null;
      this.clearGoal();

      for (Goal goal : this.getGoals()) {
         if (goal.isPossible(this)) {
            if (MillConfigValues.LogGeneralAI >= 2 && this.extraLog) {
               MillLog.minor(this, "Priority for goal " + goal.gameName(this) + ": " + goal.priority(this));
            }

            if (nextGoal != null && (!nextGoal.leasure || goal.leasure)) {
               if (nextGoal == null || nextGoal.priority(this) < goal.priority(this)) {
                  nextGoal = goal;
               }
            } else {
               nextGoal = goal;
            }
         }
      }

      if (MillConfigValues.LogGeneralAI >= 2 && this.extraLog) {
         MillLog.minor(this, "Selected this: " + nextGoal);
      }

      if (nextGoal != null) {
         this.speakSentence(nextGoal.key + ".chosen");
         this.goalKey = nextGoal.key;
         this.heldItem = ItemStack.EMPTY;
         this.heldItemOffHand = ItemStack.EMPTY;
         this.heldItemCount = Integer.MAX_VALUE;
         nextGoal.onAccept(this);
         this.goalStarted = this.world.getWorldTime();
         this.lastGoalTime.put(nextGoal, this.world.getWorldTime());
         IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
         iattributeinstance.removeModifier(SPRINT_SPEED_BOOST);
         if (nextGoal.sprint) {
            iattributeinstance.applyModifier(SPRINT_SPEED_BOOST);
         }
      } else {
         this.goalKey = null;
      }

      if (MillConfigValues.LogBuildingPlan >= 1 && nextGoal != null
            && nextGoal.key.equals(Goal.getResourcesForBuild.key)) {
         ConstructionIP cip = this.getCurrentConstruction();
         if (cip != null) {
            MillLog.major(
                  this,
                  this.getName()
                        + " is new builder, for: "
                        + cip.getBuildingLocation().planKey
                        + "_"
                        + cip.getBuildingLocation().level
                        + ". Blocks loaded: "
                        + cip.getBblocks().length);
         }
      }
   }

   public void setPathDestPoint(Point newDest, int tolerance) {
      if ((newDest == null || !newDest.equals(this.pathDestPoint))
            && (this.pathDestPoint == null || newDest == null || tolerance < newDest.distanceTo(this.pathDestPoint))) {
         this.navigator.clearPath();
         this.pathEntity = null;
      }

      this.pathDestPoint = newDest;
   }

   public void setTexture(ResourceLocation tx) {
      this.texture = tx;
   }

   public void setTownHallPoint(Point p) {
      this.townHallPoint = p;
      this.townHall = null;
   }

   public void setVillagerId(long villagerId) {
      this.villagerId = villagerId;
   }

   public void speakSentence(String key) {
      this.speakSentence(key, 600, 3, 1);
   }

   public void speakSentence(String key, int delay, int distance, int chanceOn) {
      if (delay <= this.world.getWorldTime() - this.speech_started) {
         if (MillCommonUtilities.chanceOn(chanceOn)) {
            if (this.getTownHall() != null
                  && this.getTownHall().closestPlayer != null
                  && !(this.getPos().distanceTo(this.getTownHall().closestPlayer) > distance)) {
               key = key.toLowerCase();
               this.speech_key = null;
               if (this.getCulture().hasSentences(this.getNameKey() + "." + key)) {
                  this.speech_key = this.getNameKey() + "." + key;
               } else if (this.getCulture().hasSentences(this.getGenderString() + "." + key)) {
                  this.speech_key = this.getGenderString() + "." + key;
               } else if (this.getCulture().hasSentences("villager." + key)) {
                  this.speech_key = "villager." + key;
               }

               if (this.speech_key != null) {
                  this.speech_variant = MillCommonUtilities
                        .randomInt(this.getCulture().getSentences(this.speech_key).size());
                  this.speech_started = this.world.getWorldTime();
                  this.sendVillagerPacket();
                  ServerSender.sendVillageSentenceInRange(this.world, this.getPos(), 30, this);
               }
            }
         }
      }
   }

   public void specificUpdate() throws Exception {
      if (this.isLocalMerchant()) {
         this.localMerchantUpdate();
      }

      if (this.isForeignMerchant()) {
         this.foreignMerchantUpdate();
      }
   }

   public int takeFromBuilding(Building building, Item item, int meta, int nb) {
      if (item == Item.getItemFromBlock(Blocks.LOG) && meta == -1) {
         int total = 0;
         int nb2 = building.takeGoods(item, 0, nb);
         this.addToInv(item, 0, nb2);
         total += nb2;
         nb2 = building.takeGoods(item, 1, nb - total);
         this.addToInv(item, 0, nb2);
         total += nb2;
         nb2 = building.takeGoods(item, 2, nb - total);
         this.addToInv(item, 0, nb2);
         total += nb2;
         nb2 = building.takeGoods(item, 3, nb - total);
         this.addToInv(item, 0, nb2);
         total += nb2;
         nb2 = building.takeGoods(Item.getItemFromBlock(Blocks.LOG2), 0, nb - total);
         this.addToInv(item, 0, nb2);
         total += nb2;
         nb2 = building.takeGoods(Item.getItemFromBlock(Blocks.LOG2), 1, nb - total);
         this.addToInv(item, 0, nb2);
         return total + nb2;
      } else {
         nb = building.takeGoods(item, meta, nb);
         this.addToInv(item, meta, nb);
         return nb;
      }
   }

   public int takeFromInv(Block block, int meta, int nb) {
      return this.takeFromInv(Item.getItemFromBlock(block), meta, nb);
   }

   public int takeFromInv(IBlockState blockState, int nb) {
      return this.takeFromInv(Item.getItemFromBlock(blockState.getBlock()),
            blockState.getBlock().getMetaFromState(blockState), nb);
   }

   public int takeFromInv(InvItem item, int nb) {
      return this.takeFromInv(item.getItem(), item.meta, nb);
   }

   public int takeFromInv(Item item, int meta, int nb) {
      if (item == Item.getItemFromBlock(Blocks.LOG) && meta == -1) {
         int total = 0;

         for (int i = 0; i < 16; i++) {
            InvItem key = InvItem.createInvItem(item, i);
            if (this.inventory.containsKey(key)) {
               int nb2 = Math.min(nb, this.inventory.get(key));
               this.inventory.put(key, this.inventory.get(key) - nb2);
               total += nb2;
            }
         }

         for (int ix = 0; ix < 16; ix++) {
            InvItem key = InvItem.createInvItem(Item.getItemFromBlock(Blocks.LOG2), ix);
            if (this.inventory.containsKey(key)) {
               int nb2 = Math.min(nb, this.inventory.get(key));
               this.inventory.put(key, this.inventory.get(key) - nb2);
               total += nb2;
            }
         }

         this.updateVillagerRecord();
         return total;
      } else {
         InvItem key = InvItem.createInvItem(item, meta);
         if (this.inventory.containsKey(key)) {
            nb = Math.min(nb, this.inventory.get(key));
            this.inventory.put(key, this.inventory.get(key) - nb);
            this.updateVillagerRecord();
            this.updateClothTexturePath();
            return nb;
         } else {
            return 0;
         }
      }
   }

   private void targetDefender() {
      int bestDist = Integer.MAX_VALUE;
      MillVillager target = null;

      for (MillVillager v : this.getTownHall().getKnownVillagers()) {
         if (v.helpsInAttacks() && !v.isRaider && this.getPos().distanceToSquared(v) < bestDist) {
            target = v;
            bestDist = (int) this.getPos().distanceToSquared(v);
         }
      }

      if (target != null && this.getPos().distanceToSquared(target) <= 100.0) {
         this.setAttackTarget(target);
      }
   }

   private void targetRaider() {
      int bestDist = Integer.MAX_VALUE;
      MillVillager target = null;

      for (MillVillager v : this.getTownHall().getKnownVillagers()) {
         if (v.isRaider && this.getPos().distanceToSquared(v) < bestDist) {
            target = v;
            bestDist = (int) this.getPos().distanceToSquared(v);
         }
      }

      if (target != null && this.getPos().distanceToSquared(target) <= 25.0) {
         this.setAttackTarget(target);
      }
   }

   private void teenagerNightAction() {
      for (Point p : this.getTownHall().getKnownVillages()) {
         if (this.getTownHall().getRelationWithVillage(p) > 90) {
            Building distantVillage = this.mw.getBuilding(p);
            if (distantVillage != null && distantVillage.culture == this.getCulture()
                  && distantVillage != this.getTownHall()) {
               boolean canMoveIn = false;
               if (MillConfigValues.LogChildren >= 1) {
                  MillLog.major(this, "Attempting to move to village: " + distantVillage.getVillageQualifiedName());
               }

               Building distantInn = null;

               for (Building distantBuilding : distantVillage.getBuildings()) {
                  if (!canMoveIn && distantBuilding != null && distantBuilding.isHouse()) {
                     if (distantBuilding.canChildMoveIn(this.gender, this.familyName)) {
                        canMoveIn = true;
                     }
                  } else if (distantInn == null && distantBuilding.isInn
                        && distantBuilding.getAllVillagerRecords().size() < 2) {
                     distantInn = distantBuilding;
                  }
               }

               if (canMoveIn && distantInn != null) {
                  if (MillConfigValues.LogChildren >= 1) {
                     MillLog.major(this, "Moving to village: " + distantVillage.getVillageQualifiedName());
                  }

                  this.getHouse().transferVillagerPermanently(this.getRecord(), distantInn);
                  distantInn.visitorsList.add(
                        "panels.childarrived;" + this.getName() + ";" + this.getTownHall().getVillageQualifiedName());
               }
            }
         }
      }
   }

   public boolean teleportTo(double d, double d1, double d2) {
      double d3 = this.posX;
      double d4 = this.posY;
      double d5 = this.posZ;
      this.posX = d;
      this.posY = d1;
      this.posZ = d2;
      boolean flag = false;
      int i = MathHelper.floor(this.posX);
      int j = MathHelper.floor(this.posY);
      int k = MathHelper.floor(this.posZ);
      if (this.world.isBlockLoaded(new BlockPos(i, j, k))) {
         boolean flag1 = false;

         while (!flag1 && j > 0) {
            IBlockState bs = WorldUtilities.getBlockState(this.world, i, j - 1, k);
            if (bs.getBlock() != Blocks.AIR && bs.getMaterial().blocksMovement()) {
               flag1 = true;
            } else {
               this.posY--;
               j--;
            }
         }

         if (flag1) {
            this.setPosition(this.posX, this.posY, this.posZ);
            if (this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).size() == 0
                  && !this.world.containsAnyLiquid(this.getEntityBoundingBox())) {
               flag = true;
            }
         }
      }

      if (!flag) {
         this.setPosition(d3, d4, d5);
         return false;
      } else {
         return true;
      }
   }

   public boolean teleportToEntity(Entity entity) {
      Vec3d vec3d = new Vec3d(
            this.posX - entity.posX,
            this.getEntityBoundingBox().minY + this.height / 2.0F - entity.posY + entity.getEyeHeight(),
            this.posZ - entity.posZ);
      vec3d = vec3d.normalize();
      double d = 16.0;
      double d1 = this.posX + (this.rand.nextDouble() - 0.5) * 8.0 - vec3d.x * 16.0;
      double d2 = this.posY + (this.rand.nextInt(16) - 8) - vec3d.y * 16.0;
      double d3 = this.posZ + (this.rand.nextDouble() - 0.5) * 8.0 - vec3d.z * 16.0;
      return this.teleportTo(d1, d2, d3);
   }

   private void toggleDoor(Point p) {
      IBlockState state = p.getBlockActualState(this.world);
      if ((Boolean) state.getValue(BlockDoor.OPEN)) {
         state = state.withProperty(BlockDoor.OPEN, false);
      } else {
         state = state.withProperty(BlockDoor.OPEN, true);
      }

      p.setBlockState(this.world, state);
   }

   @Override
   public String toString() {
      return this.vtype != null
            ? this.getName() + "/" + this.vtype.key + "/" + this.getVillagerId() + "/" + this.getPos()
            : this.getName() + "/none/" + this.getVillagerId() + "/" + this.getPos();
   }

   private void triggerMobAttacks() {
      for (Entity ent : WorldUtilities.getEntitiesWithinAABB(this.world, EntityMob.class, this.getPos(), 16, 5)) {
         EntityMob mob = (EntityMob) ent;
         if (mob.getAttackTarget() == null && mob.canEntityBeSeen(this)) {
            mob.setAttackTarget(this);
         }
      }
   }

   private void unlockForNearbyPlayers() {
      EntityPlayer player = this.world.getClosestPlayer(this.posX, this.posY, this.posZ, 5.0, false);
      if (player != null) {
         UserProfile profile = this.mw.getProfile(player);
         if (profile != null) {
            profile.unlockVillager(this.getCulture(), this.vtype);
         }
      }
   }

   private void updateClothTexturePath() {
      if (this.vtype != null) {
         boolean[] naturalLayers = this.vtype.getClothLayersOfType("natural");
         String bestClothName = null;
         int clothLevel = -1;
         if (this.vtype.hasClothTexture("free")) {
            bestClothName = "free";
            clothLevel = 0;
         }

         for (InvItem iv : this.inventory.keySet()) {
            if (iv.item instanceof ItemClothes && this.inventory.get(iv) > 0) {
               ItemClothes clothes = (ItemClothes) iv.item;
               if (clothes.getClothPriority(iv.meta) > clothLevel
                     && this.vtype.hasClothTexture(clothes.getClothName(iv.meta))) {
                  bestClothName = clothes.getClothName(iv.meta);
                  clothLevel = clothes.getClothPriority(iv.meta);
               }
            }
         }

         if (bestClothName != null) {
            if (!bestClothName.equals(this.clothName)) {
               this.clothName = bestClothName;

               for (int layer = 0; layer < 2; layer++) {
                  String texture;
                  if (naturalLayers[layer]) {
                     texture = this.vtype.getRandomClothTexture("natural", layer);
                  } else {
                     texture = this.vtype.getRandomClothTexture(bestClothName, layer);
                  }

                  if (texture == null || texture.length() <= 0) {
                     this.clothTexture[layer] = null;
                  } else if (texture.contains(":")) {
                     this.clothTexture[layer] = new ResourceLocation(texture);
                  } else {
                     this.clothTexture[layer] = new ResourceLocation("millenaire", texture);
                  }
               }
            }
         } else {
            this.clothName = null;

            for (int i = 0; i < 2; i++) {
               this.clothTexture[i] = null;
            }
         }
      }
   }

   private void updateDialogue() {
      if (this.dialogueKey != null) {
         CultureLanguage.Dialogue d = this.getCulture().getDialogue(this.dialogueKey);
         if (d == null) {
            this.dialogueKey = null;
         } else {
            long timePassed = this.world.getWorldTime() - this.dialogueStart;
            if (d.timeDelays.get(d.timeDelays.size() - 1) + 100 < timePassed) {
               this.dialogueKey = null;
            } else {
               String toSpeakKey = null;

               for (int i = 0; i < d.speechBy.size(); i++) {
                  if (this.dialogueRole == d.speechBy.get(i) && timePassed >= d.timeDelays.get(i).intValue()) {
                     toSpeakKey = "chat_" + d.key + "_" + i;
                  }
               }

               if (toSpeakKey != null && (this.speech_key == null || !this.speech_key.contains(toSpeakKey))) {
                  this.speakSentence(toSpeakKey, 0, 10, 1);
               }
            }
         }
      }
   }

   private void updateHired() {
      try {
         if (this.getHealth() < this.getMaxHealth() & MillCommonUtilities.randomInt(1600) == 0) {
            this.setHealth(this.getHealth() + 1.0F);
         }

         EntityPlayer entityplayer = this.world.getPlayerEntityByName(this.hiredBy);
         if (this.world.getWorldTime() > this.hiredUntil) {
            if (entityplayer != null) {
               ServerSender.sendTranslatedSentence(entityplayer, 'f', "hire.hireover", this.getName());
            }

            this.hiredBy = null;
            this.hiredUntil = 0L;
            VillagerRecord vr = this.getRecord();
            if (vr != null) {
               vr.awayhired = false;
            }

            return;
         }

         if (this.getAttackTarget() != null) {
            if (this.getPos().distanceTo(this.getAttackTarget()) > 80.0
                  || this.world.getDifficulty() == EnumDifficulty.PEACEFUL
                  || this.getAttackTarget().isDead) {
               this.setAttackTarget(null);
            }
         } else if (this.isHostile()
               && this.world.getDifficulty() != EnumDifficulty.PEACEFUL
               && this.getTownHall().closestPlayer != null
               && this.getPos().distanceTo(this.getTownHall().closestPlayer) <= 80.0) {
            this.setAttackTarget(this.world.getClosestPlayer(this.posX, this.posY, this.posZ, 100.0, true));
         }

         if (this.getAttackTarget() == null) {
            for (Object o : this.world
                  .getEntitiesWithinAABB(
                        EntityCreature.class,
                        new AxisAlignedBB(
                              this.posX,
                              this.posY,
                              this.posZ,
                              this.posX + 1.0,
                              this.posY + 1.0,
                              this.posZ + 1.0)
                              .expand(16.0, 8.0, 16.0))) {
               if (this.getAttackTarget() == null) {
                  EntityCreature creature = (EntityCreature) o;
                  if (creature.getAttackTarget() == entityplayer && !(creature instanceof EntityCreeper)) {
                     this.setAttackTarget(creature);
                  }
               }
            }

            if (this.getAttackTarget() == null && this.aggressiveStance) {
               List<?> var7 = this.world
                     .getEntitiesWithinAABB(
                           EntityMob.class,
                           new AxisAlignedBB(
                                 this.posX,
                                 this.posY,
                                 this.posZ,
                                 this.posX + 1.0,
                                 this.posY + 1.0,
                                 this.posZ + 1.0)
                                 .expand(16.0, 8.0, 16.0));
               if (!var7.isEmpty()) {
                  this.setAttackTarget((EntityLivingBase) var7.get(this.world.rand.nextInt(var7.size())));
                  if (this.getAttackTarget() instanceof EntityCreeper) {
                     this.setAttackTarget(null);
                  }
               }

               if (this.getAttackTarget() == null) {
                  for (Object ox : this.world
                        .getEntitiesWithinAABB(
                              MillVillager.class,
                              new AxisAlignedBB(
                                    this.posX,
                                    this.posY,
                                    this.posZ,
                                    this.posX + 1.0,
                                    this.posY + 1.0,
                                    this.posZ + 1.0)
                                    .expand(16.0, 8.0, 16.0))) {
                     if (this.getAttackTarget() == null) {
                        MillVillager villager = (MillVillager) ox;
                        if (villager.isHostile()) {
                           this.setAttackTarget(villager);
                        }
                     }
                  }
               }
            }
         }

         Entity target = null;
         if (this.getAttackTarget() != null) {
            Entity var10 = this.getAttackTarget();
            this.heldItem = this.getWeapon();
            this.heldItemOffHand = ItemStack.EMPTY;
            Path newPathEntity = this.getNavigator().getPathToEntityLiving(var10);
            if (newPathEntity != null) {
               this.getNavigator().setPath(newPathEntity, 0.5);
            }

            this.attackEntity(this.getAttackTarget());
         } else {
            this.heldItem = ItemStack.EMPTY;
            this.heldItemOffHand = ItemStack.EMPTY;
            int dist = (int) this.getPos().distanceTo(entityplayer);
            if (dist > 16) {
               this.teleportToEntity(entityplayer);
            } else if (dist > 4) {
               boolean rebuildPath = false;
               if (this.getNavigator().getPath() == null) {
                  rebuildPath = true;
               } else {
                  Point currentTargetPoint = new Point(this.getNavigator().getPath().getFinalPathPoint());
                  if (currentTargetPoint.distanceTo(entityplayer) > 2.0) {
                     rebuildPath = true;
                  }
               }

               if (rebuildPath) {
                  Path newPathEntity = this.getNavigator().getPathToEntityLiving(entityplayer);
                  if (newPathEntity != null) {
                     this.getNavigator().setPath(newPathEntity, 0.5);
                  }
               }
            }
         }

         this.prevPoint = this.getPos();
         this.handleDoorsAndFenceGates();
      } catch (Exception var6) {
         MillLog.printException("Error in hired onUpdate():", var6);
      }
   }

   private void updatePathIfNeeded(Point dest) throws Exception {
      if (dest != null) {
         if (this.pathEntity != null
               && this.pathEntity.getCurrentPathLength() > 0
               && !MillCommonUtilities.chanceOn(50)
               && this.pathEntity.getCurrentTargetPathPoint() != null) {
            this.getNavigator().setPath(this.pathEntity, 0.5);
         } else if (!this.pathPlannerJPS.isBusy()) {
            this.computeNewPath(dest);
         }
      }
   }

   public float updateRotation(float f, float f1, float f2) {
      float f3 = f1 - f;

      while (f3 < -180.0F) {
         f3 += 360.0F;
      }

      while (f3 >= 180.0F) {
         f3 -= 360.0F;
      }

      if (f3 > f2) {
         f3 = f2;
      }

      if (f3 < -f2) {
         f3 = -f2;
      }

      return f + f3;
   }

   public void updateVillagerRecord() {
      if (!this.world.isRemote) {
         this.getRecord().updateRecord(this);
      }
   }

   private boolean visitorNightAction() {
      this.visitorNbNights++;
      if (this.visitorNbNights > 5) {
         this.leaveVillage();
      } else if (this.isForeignMerchant()) {
         boolean hasItems = false;

         for (InvItem key : this.vtype.foreignMerchantStock.keySet()) {
            if (this.getHouse().countGoods(key) > 0) {
               hasItems = true;
            }
         }

         if (!hasItems) {
            this.leaveVillage();
         }
      }

      return true;
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      try {
         if (this.vtype == null) {
            MillLog.error(this, "Not saving villager due to null vtype.");
            return;
         }

         super.writeEntityToNBT(nbttagcompound);
         nbttagcompound.setString("vtype", this.vtype.key);
         nbttagcompound.setString("culture", this.getCulture().key);
         nbttagcompound.setString("texture", this.texture.getPath());
         if (this.housePoint != null) {
            this.housePoint.write(nbttagcompound, "housePos");
         }

         if (this.townHallPoint != null) {
            this.townHallPoint.write(nbttagcompound, "townHallPos");
         }

         if (this.getGoalDestPoint() != null) {
            this.getGoalDestPoint().write(nbttagcompound, "destPoint");
         }

         if (this.getGoalBuildingDestPoint() != null) {
            this.getGoalBuildingDestPoint().write(nbttagcompound, "destBuildingPoint");
         }

         if (this.getPathDestPoint() != null) {
            this.getPathDestPoint().write(nbttagcompound, "pathDestPoint");
         }

         if (this.prevPoint != null) {
            this.prevPoint.write(nbttagcompound, "prevPoint");
         }

         if (this.doorToClose != null) {
            this.doorToClose.write(nbttagcompound, "doorToClose");
         }

         nbttagcompound.setInteger("action", this.action);
         if (this.goalKey != null) {
            nbttagcompound.setString("goal", this.goalKey);
         }

         nbttagcompound.setInteger("constructionJobId", this.constructionJobId);
         nbttagcompound.setString("firstName", this.firstName);
         nbttagcompound.setString("familyName", this.familyName);
         nbttagcompound.setInteger("gender", this.gender);
         nbttagcompound.setLong("lastSpeechLong", this.speech_started);
         nbttagcompound.setLong("villager_lid", this.getVillagerId());
         if (this.dialogueKey != null) {
            nbttagcompound.setString("dialogueKey", this.dialogueKey);
            nbttagcompound.setLong("dialogueStart", this.dialogueStart);
            nbttagcompound.setInteger("dialogueRole", this.dialogueRole);
            nbttagcompound.setInteger("dialogueColour", this.dialogueColour);
            nbttagcompound.setBoolean("dialogueChat", this.dialogueChat);
         }

         NBTTagList nbttaglist = MillCommonUtilities.writeInventory(this.inventory);
         nbttagcompound.setTag("inventoryNew", nbttaglist);
         nbttagcompound.setInteger("previousBlock", Block.getIdFromBlock(this.previousBlock));
         nbttagcompound.setInteger("previousBlockMeta", this.previousBlockMeta);
         if (this.hiredBy != null) {
            nbttagcompound.setString("hiredBy", this.hiredBy);
            nbttagcompound.setLong("hiredUntil", this.hiredUntil);
            nbttagcompound.setBoolean("aggressiveStance", this.aggressiveStance);
         }

         nbttagcompound.setBoolean("isRaider", this.isRaider);
         nbttagcompound.setInteger("visitorNbNights", this.visitorNbNights);
         if (this.clothName != null) {
            nbttagcompound.setString("clothName", this.clothName);

            for (int layer = 0; layer < 2; layer++) {
               if (this.clothTexture[layer] != null) {
                  nbttagcompound.setString("clothTexture_" + layer, this.clothTexture[layer].toString());
               }
            }
         }
      } catch (Exception var4) {
         MillLog.printException("Exception when attempting to save villager " + this, var4);
      }
   }

   public void writeSpawnData(ByteBuf ds) {
      try {
         this.writeVillagerStreamData(ds, true);
      } catch (IOException var3) {
         MillLog.printException("Error in writeSpawnData for villager " + this, var3);
      }
   }

   private void writeVillagerStreamData(ByteBuf bb, boolean isSpawn) throws IOException {
      if (this.vtype == null) {
         MillLog.error(this, "Cannot write stream data due to null vtype.");
      } else {
         PacketBuffer data;
         if (bb instanceof PacketBuffer) {
            data = (PacketBuffer) bb;
         } else {
            data = new PacketBuffer(bb);
         }

         data.writeLong(this.getVillagerId());
         StreamReadWrite.writeNullableString(this.vtype.culture.key, data);
         StreamReadWrite.writeNullableString(this.vtype.key, data);
         StreamReadWrite.writeNullableResourceLocation(this.texture, data);
         StreamReadWrite.writeNullableString(this.goalKey, data);
         data.writeInt(this.constructionJobId);
         StreamReadWrite.writeNullablePoint(this.housePoint, data);
         StreamReadWrite.writeNullablePoint(this.townHallPoint, data);
         StreamReadWrite.writeNullableString(this.firstName, data);
         StreamReadWrite.writeNullableString(this.familyName, data);
         data.writeInt(this.gender);
         StreamReadWrite.writeNullableString(this.hiredBy, data);
         data.writeBoolean(this.aggressiveStance);
         data.writeLong(this.hiredUntil);
         data.writeBoolean(this.isUsingBow);
         data.writeBoolean(this.isUsingHandToHand);
         data.writeBoolean(this.isRaider);
         StreamReadWrite.writeNullableString(this.speech_key, data);
         data.writeInt(this.speech_variant);
         data.writeLong(this.speech_started);
         StreamReadWrite.writeNullableItemStack(this.heldItem, data);
         StreamReadWrite.writeNullableItemStack(this.heldItemOffHand, data);
         StreamReadWrite.writeInventory(this.inventory, data);
         StreamReadWrite.writeNullableString(this.clothName, data);

         for (int i = 0; i < 2; i++) {
            StreamReadWrite.writeNullableResourceLocation(this.clothTexture[i], data);
         }

         StreamReadWrite.writeNullablePoint(this.getGoalDestPoint(), data);
         data.writeBoolean(this.shouldLieDown);
         StreamReadWrite.writeNullableString(this.dialogueTargetFirstName, data);
         StreamReadWrite.writeNullableString(this.dialogueTargetLastName, data);
         data.writeChar(this.dialogueColour);
         data.writeBoolean(this.dialogueChat);
         data.writeFloat(this.getHealth());
         data.writeInt(this.visitorNbNights);
         if (this.getAttackTarget() != null) {
            StreamReadWrite.writeNullableUUID(this.getAttackTarget().getUniqueID(), data);
         } else {
            StreamReadWrite.writeNullableUUID(null, data);
         }

         if (isSpawn) {
            this.calculateMerchantGoods();
            data.writeInt(this.merchantSells.size());

            for (TradeGood g : this.merchantSells.keySet()) {
               StreamReadWrite.writeNullableGoods(g, data);
               data.writeInt(this.merchantSells.get(g));
            }
         } else {
            data.writeInt(-1);
         }

         if (this.getGoalDestEntity() != null) {
            data.writeInt(this.getGoalDestEntity().getEntityId());
         } else {
            data.writeInt(-1);
         }

         data.writeBoolean(this.isDead);
      }
   }

   public static class EntityGenericAsymmFemale extends MillVillager {
      public EntityGenericAsymmFemale(World world) {
         super(world);
      }
   }

   public static class EntityGenericMale extends MillVillager {
      public EntityGenericMale(World world) {
         super(world);
      }
   }

   public static class EntityGenericSymmFemale extends MillVillager {
      public EntityGenericSymmFemale(World world) {
         super(world);
      }
   }

   public static class InvItemAlphabeticalComparator implements Comparator<InvItem> {
      public int compare(InvItem arg0, InvItem arg1) {
         return arg0.getName().compareTo(arg1.getName());
      }
   }
}
