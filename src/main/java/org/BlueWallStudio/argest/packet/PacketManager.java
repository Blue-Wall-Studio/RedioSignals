package org.BlueWallStudio.argest.packet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Optional;
import net.minecraft.world.PersistentState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.BlueWallStudio.argest.debug.DebugManager;
import org.BlueWallStudio.argest.config.ModConfig;
import org.BlueWallStudio.argest.blocks.entity.DecoderBlockEntity;
import org.BlueWallStudio.argest.wire.WireType;
import org.BlueWallStudio.argest.wire.WireDetector;
import org.BlueWallStudio.argest.wire.WireRegistry;
import org.BlueWallStudio.argest.wireless.receiver.WirelessReceiver;
import org.BlueWallStudio.argest.wireless.receiver.WirelessReceiverRegistry;
import org.BlueWallStudio.argest.wireless.transmitter.WirelessTransmitter;
import org.BlueWallStudio.argest.wireless.transmitter.WirelessTransmitterRegistry;

public class PacketManager {
    private static ModConfig config = ModConfig.getInstance();

    private static final String KEY = "packet_manager";
    private static final PersistentState.Type<PacketStorage> STORAGE_TYPE = new PersistentState.Type<>(
            PacketStorage::new, PacketStorage::createFromNbt, null);

    private static PacketStorage getStorage(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(STORAGE_TYPE, KEY);
    }

    public static boolean sendPacket(ServerWorld world, Packet packet) {
        if (!packet.isValid()) {
            return false;
        }

        PacketStorage storage = getStorage(world);

        // Limit number of active packets
        if (config.maxPacketsPerTick != -1 && getStorage(world).getPackets().size() >= config.maxPacketsPerTick) {
            return false;
        }

        boolean added = storage.getPackets().add(packet);
        if (added) {
            DebugManager.getInstance().onPacketCreated(world, packet);
        }
        return added;
    }

    // Runs every N ticks (set in mod config)
    public static void tick(ServerWorld world) {
        cleanupOldPackets(world);
        processActivePackets(world);
    }

    private static void cleanupOldPackets(ServerWorld world) {
        PacketStorage storage = getStorage(world);
        if (config.maxPacketLifetime != -1) {
            if (storage.getPackets()
                    .removeIf(packet -> packet.getAge(getCurrentServerTick(world)) > config.maxPacketLifetime)) {
                storage.markDirty();
            }
        }
    }

    private static void processActivePackets(ServerWorld world) {
        PacketStorage storage = getStorage(world);
        Set<Packet> packets = storage.getPackets();
        Set<Packet> newPackets = new HashSet<>();
        Deque<PacketStep> queue = new ArrayDeque<>();

        for (Packet packet : packets) {
            queue.add(new PacketStep(packet, 8));
        }

        packets.clear();

        while (!queue.isEmpty()) {
            PacketStep step = queue.poll();
            Packet packet = step.packet;
            int remaining = step.stepsRemaining;

            if (remaining <= 0) {
                newPackets.add(packet);
                continue;
            }

            Set<Packet> processedPackets = processPacket(world, packet);

            if (processedPackets == null) {
                continue;
            }

            for (Packet p : processedPackets) {
                queue.add(new PacketStep(p, remaining - 1));
            }
        }

        if (!newPackets.isEmpty()) {
            packets.addAll(newPackets);
            storage.markDirty();
        }
    }

    private static Set<Packet> processPacket(ServerWorld world, Packet packet) {
        BlockPos currentPos = packet.getCurrentPos();
        Direction currentDir = packet.getCurrentDirection();

        if (WireDetector.isDecoder(world, currentPos)) {
            handleDecoderReception(world, packet, currentPos, currentDir);
            DebugManager.getInstance().onPacketDied(world, packet, "Reached decoder");

            // packet is processed, no packets are created
            return null;
        }

        if (WireDetector.isWirelessReceiver(world, currentPos)) {
            Packet processedPacket = handleWirelessReception(world, packet, currentPos, currentDir);

            if (processedPacket != null) {
                // packet is processed, new packet is created
                return new HashSet<>(List.of(processedPacket));
            }

            DebugManager.getInstance().onPacketDied(world, packet, "Processed by wireless receiver");

            return null;
        }

        if (WireDetector.isWirelessTransmitter(world, currentPos)) {
            Packet transmittedPacket = handleWirelessTransmission(world, packet, currentPos, currentDir);

            if (transmittedPacket != null) {
                DebugManager.getInstance().onPacketDied(world, packet, "Transmitted wirelessly");
                return new HashSet<>(List.of(transmittedPacket));
            } else {
                DebugManager.getInstance().onPacketDied(world, packet, "Wireless transmission failed");
            }

            return null;
        }

        // At this point, block is either wire or non electrical
        Optional<WireType> wireType = WireRegistry.getWireType(world.getBlockState(currentPos));
        if (wireType.isEmpty()) {
            DebugManager.getInstance().onPacketDied(world, packet, "No wire at position");

            return null;
        }

        WireType wire = wireType.get();
        if (!wire.processPacket(world, currentPos, packet)) {
            DebugManager.getInstance().onPacketDied(world, packet, "Wire blocked packet");

            return null;
        }

        // Search for exits (next valid circuit blocks) and move packet to them
        List<Direction> exits = wire.getExitDirections(world, currentPos, packet, currentDir);
        if (exits.isEmpty()) {
            DebugManager.getInstance().onPacketDied(world, packet, "No exit directions");

            return null;
        }

        Set<Packet> newPackets = new HashSet<>();
        for (Direction exit : exits) {
            BlockPos nextPos = currentPos.offset(exit);

            if (WireDetector.canTransmit(world, currentPos, nextPos, exit)) {
                Packet newPacket = packet.withNewPosition(nextPos, exit);
                newPackets.add(newPacket);
                DebugManager.getInstance().onPacketMoved(world, packet, newPacket);
            }
        }
        return newPackets;
    }

    /*
     * Helpers
     */
    private record PacketStep(Packet packet, int stepsRemaining) {
    }

    private static int getCurrentServerTick(ServerWorld world) {
        return world.getServer().getTicks();
    }

    private static void handleDecoderReception(ServerWorld world, Packet packet, BlockPos pos,
            Direction entryDirection) {
        if (world.getBlockEntity(pos) instanceof DecoderBlockEntity decoder) {
            decoder.receivePacket(packet, entryDirection);
        }
    }

    private static Packet handleWirelessReception(ServerWorld world, Packet packet, BlockPos pos,
            Direction entryDirection) {
        Optional<WirelessReceiver> receiver = WirelessReceiverRegistry.getReceiver(world.getBlockState(pos));

        return receiver.get().processWirelessReception(world, pos, packet, entryDirection);
    }

    private static Packet handleWirelessTransmission(ServerWorld world, Packet packet, BlockPos pos,
            Direction entryDirection) {
        Optional<WirelessTransmitter> transmitter = WirelessTransmitterRegistry
                .getTransmitter(world.getBlockState(pos));
        return transmitter.get().processWirelessTransmission(world, pos, packet, entryDirection);
    }
}
