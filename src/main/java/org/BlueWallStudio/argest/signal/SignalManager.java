package org.BlueWallStudio.argest.signal;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
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

public class SignalManager {
    private static ModConfig config = ModConfig.getInstance();

    private static final String KEY = "signal_manager";
    private static final PersistentState.Type<SignalStorage> STORAGE_TYPE = new PersistentState.Type<>(
            SignalStorage::new, SignalStorage::createFromNbt, null);

    private static SignalStorage getStorage(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(STORAGE_TYPE, KEY);
    }

    public static boolean sendPacket(ServerWorld world, SignalPacket packet) {
        if (!packet.isValid()) {
            return false;
        }

        SignalStorage storage = getStorage(world);

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
        SignalStorage storage = getStorage(world);
        if (config.maxPacketsPerTick != -1) {
            if (storage.getPackets()
                    .removeIf(packet -> packet.getAge(getCurrentServerTick(world)) > config.maxPacketLifetime)) {
                storage.markDirty();
            }
        }
    }

    private static void processActivePackets(ServerWorld world) {
        SignalStorage storage = getStorage(world);
        Set<SignalPacket> packets = storage.getPackets();
        Set<SignalPacket> newPackets = new HashSet<>();

        var iterator = packets.iterator();
        while (iterator.hasNext()) {
            SignalPacket packet = iterator.next();

            BlockPos currentPos = packet.getCurrentPos();
            Direction currentDir = packet.getCurrentDirection();

            if (WireDetector.isDecoder(world, currentPos)) {
                handleDecoderReception(world, packet, currentPos, currentDir);
                DebugManager.getInstance().onPacketDied(world, packet, "Reached decoder");

                // Current packet is processed, kill it and skip to next iteration
                iterator.remove();
                continue;
            }

            if (WireDetector.isWirelessReceiver(world, currentPos)) {
                SignalPacket processedPacket = handleWirelessReception(world, packet, currentPos, currentDir);

                if (processedPacket != null) {
                    newPackets.add(processedPacket);
                }

                DebugManager.getInstance().onPacketDied(world, packet, "Processed by wireless receiver");

                iterator.remove();
                continue;
            }

            if (WireDetector.isWirelessTransmitter(world, currentPos)) {
                SignalPacket transmittedPacket = handleWirelessTransmission(world, packet, currentPos, currentDir);

                if (transmittedPacket != null) {
                    DebugManager.getInstance().onPacketDied(world, packet, "Transmitted wirelessly");
                    newPackets.add(transmittedPacket);
                } else {
                    DebugManager.getInstance().onPacketDied(world, packet, "Wireless transmission failed");
                }

                iterator.remove();
                continue;
            }

            // At this point, block is either wire or non electrical
            Optional<WireType> wireType = WireRegistry.getWireType(world.getBlockState(currentPos));
            if (wireType.isEmpty()) {
                DebugManager.getInstance().onPacketDied(world, packet, "No wire at position");

                iterator.remove();
                continue;
            }

            WireType wire = wireType.get();
            if (!wire.processPacket(world, currentPos, packet)) {
                DebugManager.getInstance().onPacketDied(world, packet, "Wire blocked packet");

                iterator.remove();
                continue;
            }

            // Search for exits (next valid circuit blocks) and move packet to them
            List<Direction> exits = wire.getExitDirections(world, currentPos, packet, currentDir);
            if (exits.isEmpty()) {
                DebugManager.getInstance().onPacketDied(world, packet, "No exit directions");

                iterator.remove();
                continue;
            }

            for (Direction exit : exits) {
                BlockPos nextPos = currentPos.offset(exit);

                if (WireDetector.canTransmit(world, currentPos, nextPos, exit)) {
                    SignalPacket newPacket = packet.withNewPosition(nextPos, exit);
                    newPackets.add(newPacket);
                    DebugManager.getInstance().onPacketMoved(world, packet, newPacket);
                }
            }

            iterator.remove();
        }
        storage.getPackets().addAll(newPackets);
        storage.markDirty();
    }

    /*
     * Helper functions
     */
    private static int getCurrentServerTick(ServerWorld world) {
        return world.getServer().getTicks();
    }

    private static void handleDecoderReception(ServerWorld world, SignalPacket packet, BlockPos pos,
            Direction entryDirection) {
        if (world.getBlockEntity(pos) instanceof DecoderBlockEntity decoder) {
            decoder.receivePacket(packet, entryDirection);
        }
    }

    private static SignalPacket handleWirelessReception(ServerWorld world, SignalPacket packet, BlockPos pos,
            Direction entryDirection) {
        Optional<WirelessReceiver> receiver = WirelessReceiverRegistry.getReceiver(world.getBlockState(pos));

        return receiver.get().processWirelessReception(world, pos, packet, entryDirection);
    }

    private static SignalPacket handleWirelessTransmission(ServerWorld world, SignalPacket packet, BlockPos pos,
            Direction entryDirection) {
        Optional<WirelessTransmitter> transmitter = WirelessTransmitterRegistry
                .getTransmitter(world.getBlockState(pos));
        return transmitter.get().processWirelessTransmission(world, pos, packet, entryDirection);
    }
}
