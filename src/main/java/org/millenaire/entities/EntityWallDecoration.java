package org.millenaire.entities;

import java.util.ArrayList;
import java.util.List;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import org.millenaire.MillenaireRevived;
import org.millenaire.core.MillItems;
import org.millenaire.utilities.BlockItemUtilities;
import org.millenaire.utilities.WorldUtilities;
import org.slf4j.Logger;

/**
 * EntityWallDecoration - Hanging decorative entity for tapestries, icons, and
 * statues.
 * Ported exactly from original 1.12.2 EntityWallDecoration.java
 */
public class EntityWallDecoration extends HangingEntity implements IEntityAdditionalSpawnData {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation WALL_DECORATION = new ResourceLocation(MillenaireRevived.MODID,
            "wall_decoration");

    public static final int NORMAN_TAPESTRY = 1;
    public static final int INDIAN_STATUE = 2;
    public static final int MAYAN_STATUE = 3;
    public static final int BYZANTINE_ICON_SMALL = 4;
    public static final int BYZANTINE_ICON_MEDIUM = 5;
    public static final int BYZANTINE_ICON_LARGE = 6;
    public static final int HIDE_HANGING = 7;
    public static final int WALL_CARPET_SMALL = 8;
    public static final int WALL_CARPET_MEDIUM = 9;
    public static final int WALL_CARPET_LARGE = 10;

    public EnumWallDecoration millArt;
    public int type;

    public static EntityWallDecoration createWallDecoration(Level world, BlockPos p, int type) {
        Direction facing = guessOrientation(world, p);
        BlockPos blockpos = p.relative(facing);
        return new EntityWallDecoration(world, blockpos, facing, type, true);
    }

    private static Direction guessOrientation(Level world, BlockPos p) {
        if (BlockItemUtilities.isBlockSolid(WorldUtilities.getBlock(world, p.north()))) {
            return Direction.SOUTH;
        } else if (BlockItemUtilities.isBlockSolid(WorldUtilities.getBlock(world, p.south()))) {
            return Direction.NORTH;
        } else if (BlockItemUtilities.isBlockSolid(WorldUtilities.getBlock(world, p.east()))) {
            return Direction.WEST;
        } else if (BlockItemUtilities.isBlockSolid(WorldUtilities.getBlock(world, p.west()))) {
            return Direction.EAST;
        }
        return Direction.WEST;
    }

    public EntityWallDecoration(EntityType<? extends EntityWallDecoration> type, Level world) {
        super(type, world);
    }

    public EntityWallDecoration(Level world, BlockPos pos, Direction facing, int decorationType,
            boolean largestPossible) {
        super(org.millenaire.core.MillEntities.WALL_DECORATION.get(), world, pos);
        this.type = decorationType;

        List<EnumWallDecoration> validDecorations = new ArrayList<>();
        int maxSize = 0;

        for (EnumWallDecoration enumArt : EnumWallDecoration.values()) {
            if (enumArt.type == decorationType) {
                this.millArt = enumArt;
                this.setDirection(facing);

                if (this.survives()) {
                    if (!largestPossible && enumArt.sizeX * enumArt.sizeY > maxSize) {
                        validDecorations.clear();
                    }
                    validDecorations.add(enumArt);
                    maxSize = enumArt.sizeX * enumArt.sizeY;
                }
            }
        }

        if (!validDecorations.isEmpty()) {
            // Weighted random choice
            this.millArt = getWeightedChoice(validDecorations, null);
        }

        LOGGER.debug("Creating wall decoration: {}/{}/{}/{}. Result: {} picked among {}",
                pos, facing, decorationType, largestPossible,
                this.millArt != null ? this.millArt.title : "null",
                validDecorations.size());

        this.setDirection(facing);
    }

    private EnumWallDecoration getWeightedChoice(List<EnumWallDecoration> choices, Player player) {
        if (choices.isEmpty())
            return null;

        int totalWeight = 0;
        for (EnumWallDecoration choice : choices) {
            totalWeight += choice.getChoiceWeight(player);
        }

        if (totalWeight <= 0) {
            return choices.get(random.nextInt(choices.size()));
        }

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;

        for (EnumWallDecoration choice : choices) {
            cumulative += choice.getChoiceWeight(player);
            if (roll < cumulative) {
                return choice;
            }
        }

        return choices.get(choices.size() - 1);
    }

    public Item getDropItem() {
        switch (this.type) {
            case NORMAN_TAPESTRY:
                return MillItems.TAPESTRY.get();
            case INDIAN_STATUE:
                return MillItems.INDIAN_STATUE.get();
            case MAYAN_STATUE:
                return MillItems.MAYAN_STATUE.get();
            case BYZANTINE_ICON_SMALL:
                return MillItems.BYZANTINE_ICON_SMALL.get();
            case BYZANTINE_ICON_MEDIUM:
                return MillItems.BYZANTINE_ICON_MEDIUM.get();
            case BYZANTINE_ICON_LARGE:
                return MillItems.BYZANTINE_ICON_LARGE.get();
            case HIDE_HANGING:
                return MillItems.HIDEHANGING.get();
            case WALL_CARPET_SMALL:
                return MillItems.WALLCARPETSMALL.get();
            case WALL_CARPET_MEDIUM:
                return MillItems.WALLCARPETMEDIUM.get();
            case WALL_CARPET_LARGE:
                return MillItems.WALLCARPETLARGE.get();
            default:
                LOGGER.error("Unknown wall decoration type: {}", this.type);
                return null;
        }
    }

    @Override
    public int getWidth() {
        return this.millArt != null ? this.millArt.sizeX : 16;
    }

    @Override
    public int getHeight() {
        return this.millArt != null ? this.millArt.sizeY : 16;
    }

    @Override
    public void dropItem(Entity brokenEntity) {
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);

            if (brokenEntity instanceof Player player) {
                if (player.getAbilities().instabuild) {
                    return;
                }
            }

            Item dropItem = this.getDropItem();
            if (dropItem != null) {
                this.spawnAtLocation(new ItemStack(dropItem), 0.0F);
            }
        }
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        this.type = compound.getInt("Type");
        String motive = compound.getString("Motive");

        for (EnumWallDecoration enumArt : EnumWallDecoration.values()) {
            if (enumArt.title.equals(motive)) {
                this.millArt = enumArt;
                break;
            }
        }

        if (this.millArt == null) {
            this.millArt = EnumWallDecoration.Griffon;
        }

        if (this.type == 0) {
            this.type = NORMAN_TAPESTRY;
        }

        super.readAdditionalSaveData(compound);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Type", this.type);
        if (this.millArt != null) {
            compound.putString("Motive", this.millArt.title);
        }
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.type);
        buffer.writeUtf(this.millArt != null ? this.millArt.title : "Griffon");
        buffer.writeBlockPos(this.getPos());
        buffer.writeInt(this.direction.get2DDataValue());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.type = buffer.readInt();
        String title = buffer.readUtf(2048);

        for (EnumWallDecoration enumArt : EnumWallDecoration.values()) {
            if (enumArt.title.equals(title)) {
                this.millArt = enumArt;
                break;
            }
        }

        BlockPos pos = buffer.readBlockPos();
        this.setPos(pos.getX(), pos.getY(), pos.getZ());

        int facingId = buffer.readInt();
        this.setDirection(Direction.from2DDataValue(facingId));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public String toString() {
        return "WallDecoration (" + (millArt != null ? millArt.title : "null") + ") " + super.toString();
    }

    /**
     * Enum of all wall decoration types with their texture coordinates and sizes.
     */
    public static enum EnumWallDecoration {
        // Norman Tapestries (type 1)
        Griffon("Griffon", 16, 16, 0, 0, NORMAN_TAPESTRY),
        Oiseau("Oiseau", 16, 16, 16, 0, NORMAN_TAPESTRY),
        CorbeauRenard("CorbeauRenard", 32, 16, 32, 0, NORMAN_TAPESTRY),
        Serment("Serment", 80, 48, 0, 16, NORMAN_TAPESTRY),
        MortHarold("MortHarold", 64, 48, 80, 16, NORMAN_TAPESTRY),
        Drakar("Drakar", 96, 48, 144, 16, NORMAN_TAPESTRY),
        MontStMichel("MontStMichel", 48, 32, 0, 64, NORMAN_TAPESTRY),
        Bucherons("Bucherons", 48, 32, 48, 64, NORMAN_TAPESTRY),
        Cuisine("Cuisine", 48, 32, 96, 64, NORMAN_TAPESTRY),
        Flotte("Flotte", 240, 48, 0, 96, NORMAN_TAPESTRY),
        Chasse("Chasse", 96, 48, 0, 144, NORMAN_TAPESTRY),
        Siege("Siege", 256, 48, 0, 192, NORMAN_TAPESTRY),

        // Indian Statues (type 2)
        Ganesh("Ganesh", 32, 48, 0, 0, INDIAN_STATUE),
        Kali("Kali", 32, 48, 32, 0, INDIAN_STATUE),
        Shiva("Shiva", 32, 48, 64, 0, INDIAN_STATUE),
        Osiyan("Osiyan", 32, 48, 96, 0, INDIAN_STATUE),
        Durga("Durga", 32, 48, 128, 0, INDIAN_STATUE),

        // Mayan Statues (type 3)
        MayanTeal("MayanTeal", 32, 32, 0, 48, MAYAN_STATUE),
        MayanGold("MayanGold", 32, 32, 32, 48, MAYAN_STATUE),

        // Byzantine Icons Small (type 4)
        SmallJesus1("SmallJesus1", 16, 16, 0, 160, BYZANTINE_ICON_SMALL),
        SmallJesus2("SmallJesus2", 16, 16, 16, 160, BYZANTINE_ICON_SMALL),
        SmallSaint1("SmallSaint1", 16, 16, 32, 160, BYZANTINE_ICON_SMALL),
        SmallAngel1("SmallAngel1", 16, 16, 48, 160, BYZANTINE_ICON_SMALL),
        SmallVirgin1("SmallVirgin1", 16, 16, 64, 160, BYZANTINE_ICON_SMALL),
        SmallAngel2("SmallAngel2", 16, 16, 80, 160, BYZANTINE_ICON_SMALL),

        // Byzantine Icons Medium (type 5)
        MediumVirgin1("MediumVirgin1", 32, 32, 0, 128, BYZANTINE_ICON_MEDIUM),
        MediumVirgin2("MediumVirgin2", 32, 32, 32, 128, BYZANTINE_ICON_MEDIUM),

        // Byzantine Icons Large (type 6)
        LargeJesus("LargeJesus", 32, 48, 0, 80, BYZANTINE_ICON_LARGE),
        LargeVirgin("LargeVirgin", 32, 48, 32, 80, BYZANTINE_ICON_LARGE),

        // Hide Hangings (type 7)
        HideSmallCow("HideSmallCow", 16, 16, 0, 176, HIDE_HANGING, 10),
        HideSmallRabbit("HideSmallRabbit", 16, 16, 16, 176, HIDE_HANGING, 10),
        HideSmallSpider("HideSmallSpider", 16, 16, 32, 176, HIDE_HANGING, 1),
        HideLargeCow("HideLargeCow", 32, 32, 0, 192, HIDE_HANGING, 10),
        HideLargeBear("HideLargeBear", 32, 32, 32, 192, HIDE_HANGING, 5),
        HideLargeZombie("HideLargeZombie", 32, 32, 64, 192, HIDE_HANGING, 1),
        HideLargeWolf("HideLargeWolf", 32, 32, 96, 192, HIDE_HANGING, 5),

        // Wall Carpets Small (type 8)
        WallCarpet1("WallCarpet1", 16, 32, 0, 224, WALL_CARPET_SMALL),
        WallCarpet2("WallCarpet2", 16, 32, 16, 224, WALL_CARPET_SMALL),
        WallCarpet3("WallCarpet3", 16, 32, 32, 224, WALL_CARPET_SMALL),
        WallCarpet4("WallCarpet4", 16, 32, 48, 224, WALL_CARPET_SMALL),
        WallCarpet5("WallCarpet5", 16, 32, 64, 224, WALL_CARPET_SMALL),
        WallCarpet6("WallCarpet6", 16, 32, 80, 224, WALL_CARPET_SMALL),
        WallCarpet7("WallCarpet7", 16, 32, 96, 224, WALL_CARPET_SMALL),

        // Wall Carpets Medium (type 9)
        WallCarpet8("WallCarpet8", 32, 48, 160, 176, WALL_CARPET_MEDIUM),
        WallCarpet9("WallCarpet9", 32, 48, 192, 176, WALL_CARPET_MEDIUM),
        WallCarpet10("WallCarpet10", 32, 48, 224, 176, WALL_CARPET_MEDIUM),

        // Wall Carpets Large (type 10)
        WallCarpet11("WallCarpet11", 48, 32, 112, 224, WALL_CARPET_LARGE),
        WallCarpet12("WallCarpet12", 48, 32, 160, 224, WALL_CARPET_LARGE),
        WallCarpet13("WallCarpet13", 48, 32, 208, 224, WALL_CARPET_LARGE);

        public static final int maxArtTitleLength = "SkullAndRoses".length();
        public final String title;
        public final int sizeX;
        public final int sizeY;
        public final int offsetX;
        public final int offsetY;
        public final int type;
        private final int weight;

        EnumWallDecoration(String title, int sizeX, int sizeY, int offsetX, int offsetY, int type) {
            this(title, sizeX, sizeY, offsetX, offsetY, type, 1);
        }

        EnumWallDecoration(String title, int sizeX, int sizeY, int offsetX, int offsetY, int type, int weight) {
            this.title = title;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.type = type;
            this.weight = weight;
        }

        public int getChoiceWeight(Player player) {
            return this.weight;
        }
    }
}
