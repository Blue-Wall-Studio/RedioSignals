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
import org.BlueWallStudio.argest.wire.WireDetector;
import org.BlueWallStudio.argest.wire.WireRegistry;
import org.BlueWallStudio.argest.wire.WireType;
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

    public static void tick(ServerWorld world) {
        // Clean up old packets every tick
        cleanupOldPackets(world);

        // Process packets every tick, but move them by SPEED blocks per tick
        processActivePackets(world);
    }

    private static void cleanupOldPackets(ServerWorld world) {
        SignalStorage storage = getStorage(world);
        // fix: check lifetime config, not maxPacketsPerTick
        if (config.maxPacketLifetimeTicks != -1) {
            if (storage.getPackets()
                    .removeIf(packet -> packet.getAge(getCurrentServerTick(world)) > config.maxPacketLifetimeTicks)) {
                storage.markDirty();
            }
        }
    }

    private static void processActivePackets(ServerWorld world) {
        SignalStorage storage = getStorage(world);

        // number of blocks a packet should move per tick
        final int SPEED = 8; // <- сюда можно подставить config.blocksPerTick если добавите параметр в конфиг

        // start from current set of packets
        Set<SignalPacket> current = new HashSet<>(storage.getPackets());

        // We'll perform SPEED iterations; на каждой итерации перемещаем пакет на 1 блок.
        for (int step = 0; step < SPEED; step++) {
            if (current.isEmpty()) break;

            Set<SignalPacket> next = new HashSet<>();

            for (SignalPacket packet : current) {
                BlockPos currentPos = packet.getCurrentPos();
                Direction currentDir = packet.getCurrentDirection();

                // decoder
                if (WireDetector.isDecoder(world, currentPos)) {
                    handleDecoderReception(world, packet, currentPos, currentDir);
                    DebugManager.getInstance().onPacketDied(world, packet, "Reached decoder");
                    continue; // packet consumed
                }

                // wireless transmitter
                if (WireDetector.isWirelessTransmitter(world, currentPos)) {
                    DebugManager.getInstance().onWirelessTransmission(world, packet);

                    SignalPacket transmittedPacket = handleWirelessTransmission(world, packet, currentPos, currentDir);

                    if (transmittedPacket != null) {
                        BlockPos targetPos = transmittedPacket.getCurrentPos();
                        Direction targetDir = transmittedPacket.getCurrentDirection();

                        if (WireDetector.isWirelessReceiver(world, targetPos)) {
                            DebugManager.getInstance().onWirelessReception(world, transmittedPacket);

                            SignalPacket processed = handleWirelessReception(world, transmittedPacket, targetPos, targetDir);
                            if (processed != null) {
                                next.add(processed);
                                DebugManager.getInstance().onPacketMoved(world, packet, processed);
                            } else {
                                DebugManager.getInstance().onPacketDied(world, packet, "Wireless receiver blocked packet");
                            }
                        } else {
                            next.add(transmittedPacket);
                            DebugManager.getInstance().onPacketMoved(world, packet, transmittedPacket);
                        }
                    } else {
                        DebugManager.getInstance().onPacketDied(world, packet, "Wireless transmission failed");
                    }
                    continue;
                }

                // wireless receiver (direct hit)
                if (WireDetector.isWirelessReceiver(world, currentPos)) {
                    DebugManager.getInstance().onWirelessReception(world, packet);

                    SignalPacket processed = handleWirelessReception(world, packet, currentPos, currentDir);
                    if (processed != null) {
                        next.add(processed);
                        DebugManager.getInstance().onPacketMoved(world, packet, processed);
                    } else {
                        DebugManager.getInstance().onPacketDied(world, packet, "Wireless receiver blocked packet");
                    }
                    continue;
                }

                // normal wire processing
                Optional<WireType> wireType = WireRegistry.getWireType(world.getBlockState(currentPos));
                if (wireType.isEmpty()) {
                    DebugManager.getInstance().onPacketDied(world, packet, "No wire at position");
                    continue;
                }

                WireType wire = wireType.get();
                if (!wire.processPacket(world, currentPos, packet)) {
                    DebugManager.getInstance().onPacketDied(world, packet, "Wire blocked packet");
                    continue;
                }

                List<Direction> exits = wire.getExitDirections(world, currentPos, packet, currentDir);
                if (exits.isEmpty()) {
                    DebugManager.getInstance().onPacketDied(world, packet, "No exit directions");
                    continue;
                }

                // Разветвления: создаём новый пакет для каждого выхода
                for (Direction exit : exits) {
                    BlockPos nextPos = currentPos.offset(exit);

                    if (WireDetector.canTransmit(world, currentPos, nextPos, exit)) {
                        SignalPacket newPacket = packet.withNewPosition(nextPos, exit);
                        next.add(newPacket);
                        DebugManager.getInstance().onPacketMoved(world, packet, newPacket);
                    }
                }
            } // end loop current packets

            // готовимся к следующему шагу — обработаем все пакеты, которые получились после этого шага
            current = next;
        } // end SPEED loop

        // сохраняем результаты (в текущем наборе current — пакеты после SPEED шагов)
        storage.getPackets().clear();
        storage.getPackets().addAll(current);
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
