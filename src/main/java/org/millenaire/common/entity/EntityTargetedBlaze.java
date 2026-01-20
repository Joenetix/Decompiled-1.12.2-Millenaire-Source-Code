package org.millenaire.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.millenaire.common.utilities.Point;

public class EntityTargetedBlaze extends Blaze implements IEntityAdditionalSpawnData {
    public Point target = null;

    public EntityTargetedBlaze(EntityType<? extends Blaze> type, Level level) {
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
        StreamReadWrite.writeNullablePoint(target, buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        target = StreamReadWrite.readNullablePoint(buffer);
    }

    // Stub for helper used in writeSpawnData above
    // Or I should put this in a real helper class if I want to be clean
    // But for now I'll implement internal helper or assume StreamReadWrite exists
    // Actually, StreamReadWrite is likely a class I need to port or use.
    // I haven't ported StreamReadWrite yet. I should check if it exists or stub it.

    // Checking previous steps, StreamReadWrite was not ported.
    // I will implement simple logic here instead of relying on it.

    private static class StreamReadWrite {
        static void writeNullablePoint(Point p, FriendlyByteBuf buf) {
            if (p == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                buf.writeDouble(p.x);
                buf.writeDouble(p.y);
                buf.writeDouble(p.z);
            }
        }

        static Point readNullablePoint(FriendlyByteBuf buf) {
            if (buf.readBoolean()) {
                return new Point(buf.readDouble(), buf.readDouble(), buf.readDouble());
            }
            return null;
        }
    }
}
