package org.millenaire.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.millenaire.common.utilities.Point;

public class EntityTargetedWitherSkeleton extends WitherSkeleton implements IEntityAdditionalSpawnData {
    public Point target = null;

    public EntityTargetedWitherSkeleton(EntityType<? extends WitherSkeleton> type, Level level) {
        super(type, level);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.target != null) {
            this.target.write(tag, "targetPoint");
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.target = Point.read(tag, "targetPoint");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        if (target == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeDouble(target.x);
            buffer.writeDouble(target.y);
            buffer.writeDouble(target.z);
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            target = new Point(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        } else {
            target = null;
        }
    }
}
