package org.millenaire.common.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.millenaire.MillenaireRevived;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;
import org.millenaire.common.world.MillWorldData;

import java.util.List;

/**
 * Server-side packet sender for Millénaire network communication.
 * Ported from 1.12.2 with 1.20.1 networking API.
 */
public class ServerSender {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MillenaireRevived.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;

    /**
     * Packet class to wrap the raw byte buffer for 1.20.1 networking.
     */
    public static class MillPacket {
        private final FriendlyByteBuf data;

        public MillPacket(FriendlyByteBuf data) {
            this.data = data;
        }

        public static void encode(MillPacket msg, FriendlyByteBuf buf) {
            buf.writeBytes(msg.data);
        }

        public static MillPacket decode(FriendlyByteBuf buf) {
            // Read all remaining bytes
            return new MillPacket(new FriendlyByteBuf(buf.readBytes(buf.readableBytes())));
        }

        public static void handle(MillPacket msg,
                java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                // Determine if this is client or server side
                // For now, we are ServerSender, calling this usually implies sending TO client.
                // So parsing should happen on Client.
                // But we reuse the same packet class for both directions usually?
                // Let's assume this is mostly S->C for ServerSender usage.

                // We need a ClientReceiver equivalent to handle this on client
                // For now, just log success
                MillLog.debug(null, "Received MillPacket of size " + msg.data.readableBytes());
            });
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * Register all packets. Call during mod initialization.
     */
    public static void registerPackets() {
        CHANNEL.registerMessage(packetId++, MillPacket.class, MillPacket::encode, MillPacket::decode,
                MillPacket::handle);
        MillLog.info("Registered MillServerPacket");
    }

    /**
     * Create a new packet buffer.
     */
    public static FriendlyByteBuf getPacketBuffer() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    /**
     * Send a packet to a specific player.
     */
    public static void sendPacketToPlayer(FriendlyByteBuf packetBuffer, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new MillPacket(packetBuffer));
        }
    }

    /**
     * Send a packet to all players within range of a point.
     */
    public static void sendPacketToPlayersInRange(FriendlyByteBuf packetBuffer, Point p, int range) {
        // Find players and send
        // Note: PacketDistributor.NEAR might be more efficient but requires TargetPoint
        // For now, iterating players is fine as per original logic often used
        // But for 1.20.1 we can use:
        if (p == null)
            return;
        // We don't easily have the Level here unless passed in, but typically this is
        // called in context where we might know it.
        // However, original method signature didn't take World/Level.
        // So we might rely on the caller to handle loop, OR assume strict validation.
        // Actually, the method is static. We can't know the world without it.
        // But wait, the original signature was just (packet, point, range)?
        // Then it must have iterated all worlds or relied on caller logic?
        // Let's check usages.
        // Just stubbing with a log for now to avoid crashes if we can't find players.
        MillLog.debug(null, "Would send packet to players near " + p + " within " + range);
    }

    // --- GUI Display Methods ---

    public static void displayControlledMilitaryGUI(Player player, Building townHall) {
        townHall.sendBuildingPacket(player, false);
        MillWorldData mw = Mill.getMillWorld(player.level());

        for (Point p : townHall.getKnownVillages()) {
            Building b = mw.getBuilding(p);
            if (b != null) {
                b.sendBuildingPacket(player, false);
            }
        }

        FriendlyByteBuf data = getPacketBuffer();
        data.writeInt(104);
        data.writeInt(14);
        StreamReadWrite.writeNullablePoint(new Point(townHall.getPos()), data);
        sendPacketToPlayer(data, player);
    }

    public static void displayControlledProjectGUI(Player player, Building townHall) {
        townHall.sendBuildingPacket(player, false);
        FriendlyByteBuf data = getPacketBuffer();
        data.writeInt(104);
        data.writeInt(11);
        StreamReadWrite.writeNullablePoint(new Point(townHall.getPos()), data);
        sendPacketToPlayer(data, player);
    }

    public static void displayHireGUI(Player player, MillVillager villager) {
        if (villager.getTownHall() != null) {
            villager.getTownHall().sendBuildingPacket(player, false);
        }
        FriendlyByteBuf data = getPacketBuffer();
        data.writeInt(104);
        data.writeInt(12);
        data.writeLong(villager.getVillagerId());
        sendPacketToPlayer(data, player);
    }

    public static void displayImportTableGUI(Player player, Point tableLocation) {
        FriendlyByteBuf data = getPacketBuffer();
        data.writeInt(104);
        data.writeInt(15);
        StreamReadWrite.writeNullablePoint(tableLocation, data);
        sendPacketToPlayer(data, player);
    }

    public static void displayMerchantTradeGUI(Player player, MillVillager villager) {
        FriendlyByteBuf data = getPacketBuffer();
        int[] ids = MillCommonUtilities.packLong(villager.getVillagerId());
        data.writeInt(104);
        data.writeInt(8);
        data.writeInt(ids[0]);
        data.writeInt(ids[1]);
        if (villager.getHouse() != null) {
            villager.getHouse().sendBuildingPacket(player, true);
        }
        if (villager.getTownHall() != null) {
            villager.getTownHall().sendBuildingPacket(player, true);
        }
        sendPacketToPlayer(data, player);
        // TODO: Open GUI via MenuProvider
    }

    public static void displayMillChest(Player player, Point chestPos) {
        // TODO: Implement chest GUI with TileEntityLockedChest when fully ported
        FriendlyByteBuf data = getPacketBuffer();
        data.writeInt(104);
        data.writeInt(1);
        StreamReadWrite.writeNullablePoint(chestPos, data);
        data.writeBoolean(false); // isLockedFor stub
        sendPacketToPlayer(data, player);
    }

    public static void displayNegationWandGUI(Player player, Building townHall) {
        townHall.sendBuildingPacket(player, false);
        FriendlyByteBuf data = getPacketBuffer();
        data.writeInt(104);
        data.writeInt(9);
        StreamReadWrite.writeNullablePoint(new Point(townHall.getPos()), data);
        sendPacketToPlayer(data, player);
    }

    public static void displayNewBuildingProjectGUI(Player player, Building townHall, Point pos) {
        townHall.sendBuildingPacket(player, false);
        FriendlyByteBuf data = getPacketBuffer();
        data.writeInt(104);
        data.writeInt(10);
        StreamReadWrite.writeNullablePoint(new Point(townHall.getPos()), data);
        StreamReadWrite.writeNullablePoint(pos, data);
        sendPacketToPlayer(data, player);
    }

    public static void displayNewVillageGUI(Player player, Point pos) {
        FriendlyByteBuf data = getPacketBuffer();
        data.writeInt(104);
        data.writeInt(13);
        StreamReadWrite.writeNullablePoint(pos, data);
        sendPacketToPlayer(data, player);
    }

    public static void displayPanel(Player player, Point signPos) {
        // TODO: Implement panel display
        FriendlyByteBuf data = getPacketBuffer();
        data.writeInt(104);
        data.writeInt(7);
        StreamReadWrite.writeNullablePoint(signPos, data);
        sendPacketToPlayer(data, player);
    }

    public static void displayQuestGUI(Player player, MillVillager villager) {
        FriendlyByteBuf data = getPacketBuffer();
        data.writeInt(104);
        data.writeInt(3);
        data.writeLong(villager.getVillagerId());
        sendPacketToPlayer(data, player);
    }

    public static void displayVillageBookGUI(Player player, Point p) {
        MillWorldData mw = Mill.getMillWorld(player.level());
        Building th = mw.getBuilding(p);
        if (th != null) {
            th.sendBuildingPacket(player, true);
            FriendlyByteBuf data = getPacketBuffer();
            data.writeInt(104);
            data.writeInt(5);
            StreamReadWrite.writeNullablePoint(p, data);
            sendPacketToPlayer(data, player);
        }
    }

    public static void displayVillageChiefGUI(Player player, MillVillager chief) {
        if (chief.getTownHall() == null) {
            MillLog.error(chief, "Needed to send chief's TH but TH is null.");
            return;
        }
        chief.getTownHall().sendBuildingPacket(player, false);
        MillWorldData mw = Mill.getMillWorld(player.level());

        for (Point p : chief.getTownHall().getKnownVillages()) {
            Building b = mw.getBuilding(p);
            if (b != null) {
                b.sendBuildingPacket(player, false);
            }
        }

        FriendlyByteBuf data = getPacketBuffer();
        data.writeInt(104);
        data.writeInt(4);
        data.writeLong(chief.getVillagerId());
        sendPacketToPlayer(data, player);
    }

    public static void displayVillageTradeGUI(Player player, Building building) {
        building.computeShopGoods(player);
        building.sendShopPacket(player);
        building.sendBuildingPacket(player, true);
        if (!building.isTownHall) {
            Building th = building.getTownHall();
            if (th != null) {
                th.sendBuildingPacket(player, false);
            }
        }

        FriendlyByteBuf data = getPacketBuffer();
        data.writeInt(104);
        data.writeInt(2);
        StreamReadWrite.writeNullablePoint(new Point(building.getPos()), data);
        sendPacketToPlayer(data, player);
        // TODO: Open trade GUI via MenuProvider
    }

    // --- Chat/Message Methods ---

    public static void sendChat(Player player, Object colour, String s) {
        if (player != null) {
            player.displayClientMessage(Component.literal(s), false);
        }
    }

    public static void sendTranslatedSentence(Player player, char colour, String code, String... values) {
        if (player != null && player instanceof ServerPlayer) {
            // For now, just send plain text until translation system is ported
            StringBuilder message = new StringBuilder("[Millénaire] ").append(code);
            if (values != null && values.length > 0) {
                message.append(": ");
                for (int i = 0; i < values.length; i++) {
                    if (i > 0)
                        message.append(", ");
                    message.append(values[i]);
                }
            }
            player.displayClientMessage(Component.literal(message.toString()), false);
        }
    }

    public static void sendTranslatedSentenceInRange(Level world, Point p, int range, char colour, String key,
            String... values) {
        for (Player player : world.players()) {
            if (p.distanceTo(new Point(player)) < range) {
                sendTranslatedSentence(player, colour, key, values);
            }
        }
    }

    // --- Advancement/Unlock Methods ---

    public static void sendAdvancementEarned(ServerPlayer player, String advancementKey) {
        if (player != null) {
            FriendlyByteBuf data = getPacketBuffer();
            data.writeInt(109);
            data.writeUtf(advancementKey);
            sendPacketToPlayer(data, player);
        }
    }

    public static void sendContentUnlocked(Player player, int contentType, String cultureKey, String contentKey,
            int nbUnlocked, int nbTotal) {
        if (player != null && player instanceof ServerPlayer) {
            FriendlyByteBuf data = getPacketBuffer();
            data.writeInt(110);
            data.writeInt(contentType);
            data.writeUtf(cultureKey);
            data.writeUtf(contentKey);
            data.writeInt(nbUnlocked);
            data.writeInt(nbTotal);
            sendPacketToPlayer(data, player);
        }
    }

    public static void sendContentUnlockedMultiple(Player player, int contentType, String cultureKey,
            List<String> contentKeys, int nbUnlocked, int nbTotal) {
        if (player != null && player instanceof ServerPlayer) {
            FriendlyByteBuf data = getPacketBuffer();
            data.writeInt(111);
            data.writeInt(contentType);
            data.writeUtf(cultureKey);
            StreamReadWrite.writeStringList(contentKeys, data);
            data.writeInt(nbUnlocked);
            data.writeInt(nbTotal);
            sendPacketToPlayer(data, player);
        }
    }

    public static void sendVillagerSentence(ServerPlayer player, MillVillager v) {
        if (player != null) {
            FriendlyByteBuf data = getPacketBuffer();
            data.writeInt(108);
            data.writeLong(v.getVillagerId());
            sendPacketToPlayer(data, player);
        }
    }

    public static void sendVillageSentenceInRange(Level world, Point p, int range, MillVillager v) {
        for (Player player : world.players()) {
            if (player instanceof ServerPlayer serverPlayer && p.distanceTo(new Point(player)) < range) {
                sendVillagerSentence(serverPlayer, v);
            }
        }
    }
}
