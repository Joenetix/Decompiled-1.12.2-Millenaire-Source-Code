package org.millenaire.common.entity;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.Point;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityWallDecoration extends HangingEntity implements IEntityAdditionalSpawnData {

    public static final ResourceLocation WALL_DECORATION = new ResourceLocation("millenaire", "wall_decoration");
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

    public EntityWallDecoration(EntityType<? extends HangingEntity> type, Level level) {
        super(type, level);
    }

    public EntityWallDecoration(EntityType<? extends HangingEntity> entityType, Level level, BlockPos pos,
            Direction direction, int type, boolean largestPossible) {
        super(entityType, level, pos);
        this.setDirection(direction);
        this.type = type;

        List<EnumWallDecoration> candidates = new ArrayList<>();
        int maxSize = 0;

        for (EnumWallDecoration art : EnumWallDecoration.values()) {
            if (art.type == type) {
                this.millArt = art;
                this.setDirection(direction);
                if (this.survives()) {
                    if (!largestPossible && art.sizeX * art.sizeY > maxSize) {
                        candidates.clear();
                    }
                    candidates.add(art);
                    maxSize = art.sizeX * art.sizeY;
                }
            }
        }

        if (!candidates.isEmpty()) {
            this.millArt = MillCommonUtilities.getWeightedChoice(candidates, null);
        }

        // Fallback or verify
        if (this.millArt != null) {
            this.setDirection(direction);
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
    public void dropItem(@Nullable Entity brokenEntity) {
        if (this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (brokenEntity instanceof Player player && player.getAbilities().instabuild) {
                return;
            }
            // TODO: Drop specific item based on type
            // this.spawnAtLocation(this.getDropItem());
        }
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Type", this.type);
        if (this.millArt != null) {
            tag.putString("Motive", this.millArt.title);
        }
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.type = tag.getInt("Type");
        String motive = tag.getString("Motive");
        for (EnumWallDecoration art : EnumWallDecoration.values()) {
            if (art.title.equals(motive)) {
                this.millArt = art;
                break;
            }
        }
        if (this.millArt == null) {
            this.millArt = EnumWallDecoration.Griffon;
        }
        super.readAdditionalSaveData(tag);
        // Recalculate bounding box?
        this.setDirection(this.getDirection());
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.type);
        buffer.writeUtf(this.millArt != null ? this.millArt.title : "");
        buffer.writeBlockPos(this.pos);
        buffer.writeInt(this.direction.get2DDataValue());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.type = buffer.readInt();
        String title = buffer.readUtf();
        for (EnumWallDecoration art : EnumWallDecoration.values()) {
            if (art.title.equals(title)) {
                this.millArt = art;
                break;
            }
        }
        this.pos = buffer.readBlockPos();
        int dirIndex = buffer.readInt();
        this.setDirection(Direction.from2DDataValue(dirIndex));
    }

    public enum EnumWallDecoration implements MillCommonUtilities.WeightedChoice {
        Griffon("Griffon", 16, 16, 0, 0, 1),
        Oiseau("Oiseau", 16, 16, 16, 0, 1),
        CorbeauRenard("CorbeauRenard", 32, 16, 32, 0, 1),
        Serment("Serment", 80, 48, 0, 16, 1),
        MortHarold("MortHarold", 64, 48, 80, 16, 1),
        Drakar("Drakar", 96, 48, 144, 16, 1),
        MontStMichel("MontStMichel", 48, 32, 0, 64, 1),
        Bucherons("Bucherons", 48, 32, 48, 64, 1),
        Cuisine("Cuisine", 48, 32, 96, 64, 1),
        Flotte("Flotte", 240, 48, 0, 96, 1),
        Chasse("Chasse", 96, 48, 0, 144, 1),
        Siege("Siege", 256, 48, 0, 192, 1),
        Ganesh("Ganesh", 32, 48, 0, 0, 2),
        Kali("Kali", 32, 48, 32, 0, 2),
        Shiva("Shiva", 32, 48, 64, 0, 2),
        Osiyan("Osiyan", 32, 48, 96, 0, 2),
        Durga("Durga", 32, 48, 128, 0, 2),
        MayanTeal("MayanTeal", 32, 32, 0, 48, 3),
        MayanGold("MayanGold", 32, 32, 32, 48, 3),
        LargeJesus("LargeJesus", 32, 48, 0, 80, 6),
        LargeVirgin("LargeVirgin", 32, 48, 32, 80, 6),
        MediumVirgin1("MediumVirgin1", 32, 32, 0, 128, 5),
        MediumVirgin2("MediumVirgin2", 32, 32, 32, 128, 5),
        SmallJesus1("SmallJesus1", 16, 16, 0, 160, 4),
        SmallJesus2("SmallJesus2", 16, 16, 16, 160, 4),
        SmallSaint1("SmallSaint1", 16, 16, 32, 160, 4),
        SmallAngel1("SmallAngel1", 16, 16, 48, 160, 4),
        SmallVirgin1("SmallVirgin1", 16, 16, 64, 160, 4),
        SmallAngel2("SmallAngel2", 16, 16, 80, 160, 4),
        HideSmallCow("HideSmallCow", 16, 16, 0, 176, 7, 10),
        HideSmallRabbit("HideSmallRabbit", 16, 16, 16, 176, 7, 10),
        HideSmallSpider("HideSmallSpider", 16, 16, 32, 176, 7, 1),
        HideLargeCow("HideLargeCow", 32, 32, 0, 192, 7, 10),
        HideLargeBear("HideLargeBear", 32, 32, 32, 192, 7, 5),
        HideLargeZombie("HideLargeZombie", 32, 32, 64, 192, 7, 1),
        HideLargeWolf("HideLargeWolf", 32, 32, 96, 192, 7, 5),
        WallCarpet1("WallCarpet1", 16, 32, 0, 224, 8),
        WallCarpet2("WallCarpet2", 16, 32, 16, 224, 8),
        WallCarpet3("WallCarpet3", 16, 32, 32, 224, 8),
        WallCarpet4("WallCarpet4", 16, 32, 48, 224, 8),
        WallCarpet5("WallCarpet5", 16, 32, 64, 224, 8),
        WallCarpet6("WallCarpet6", 16, 32, 80, 224, 8),
        WallCarpet7("WallCarpet7", 16, 32, 96, 224, 8),
        WallCarpet8("WallCarpet8", 32, 48, 160, 176, 9),
        WallCarpet9("WallCarpet9", 32, 48, 192, 176, 9),
        WallCarpet10("WallCarpet10", 32, 48, 224, 176, 9),
        WallCarpet11("WallCarpet11", 48, 32, 112, 224, 10),
        WallCarpet12("WallCarpet12", 48, 32, 160, 224, 10),
        WallCarpet13("WallCarpet13", 48, 32, 208, 224, 10);

        public final String title;
        public final int sizeX;
        public final int sizeY;
        public final int offsetX;
        public final int offsetY;
        public final int type;
        public final int weight;

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

        @Override
        public int getChoiceWeight(Player player) {
            return this.weight;
        }
    }
}
