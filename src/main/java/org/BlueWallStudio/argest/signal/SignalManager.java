package org.BlueWallStudio.argest.signal;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.BlueWallStudio.argest.blocks.entity.DecoderBlockEntity;
import org.BlueWallStudio.argest.debug.DebugManager;
import org.BlueWallStudio.argest.wire.WireDetector;
import org.BlueWallStudio.argest.wire.WireRegistry;
import org.BlueWallStudio.argest.wire.WireType;
import org.BlueWallStudio.argest.config.ModConfig;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SignalManager {
    private static final Map<ServerWorld, SignalManager> instances = new ConcurrentHashMap<>();
    private final Set<SignalPacket> activePackets = ConcurrentHashMap.newKeySet();
    private final ServerWorld world;
    private int tickCounter = 0;

    private SignalManager(ServerWorld world) {
        this.world = world;
    }

    public static SignalManager getInstance(ServerWorld world) {
        return instances.computeIfAbsent(world, SignalManager::new);
    }

    public void sendPacket(SignalPacket packet) {
        if (!packet.isValid()) return;

        // Ограничиваем количество активных пакетов
        if (activePackets.size() >= ModConfig.getInstance().maxPacketsPerTick) {
            return;
        }

        activePackets.add(packet);
        DebugManager.getInstance().onPacketCreated(packet);
    }

    public void tick() {
        tickCounter++;

        // Обрабатываем пакеты только каждые N тиков для производительности
        if (tickCounter % ModConfig.getInstance().signalProcessingDelay != 0) {
            return;
        }

        Set<SignalPacket> packetsToRemove = new HashSet<>();
        Set<SignalPacket> newPackets = new HashSet<>();

        for (SignalPacket packet : activePackets) {
            if (processPacket(packet, newPackets)) {
                packetsToRemove.add(packet);
            }
        }

        activePackets.removeAll(packetsToRemove);
        activePackets.addAll(newPackets);

        // Очистка старых пакетов
        long currentTime = System.currentTimeMillis();
        int maxLifetime = ModConfig.getInstance().maxPacketLifetime;
        activePackets.removeIf(p -> currentTime - p.getCreationTime() > maxLifetime);
    }

    private boolean processPacket(SignalPacket packet, Set<SignalPacket> newPackets) {
        BlockPos currentPos = packet.getCurrentPos();
        Direction currentDir = packet.getCurrentDirection();

        // Проверяем, является ли текущая позиция декодером
        if (WireDetector.isDecoder(world, currentPos)) {
            handleDecoderReception(currentPos, packet, currentDir);
            DebugManager.getInstance().onPacketDied(packet, "Reached decoder");
            return true;
        }

        // Проверяем, есть ли провод в текущей позиции
        Optional<WireType> wireType = WireRegistry.getWireType(world.getBlockState(currentPos));
        if (wireType.isEmpty()) {
            DebugManager.getInstance().onPacketDied(packet, "No wire at position");
            return true;
        }

        // Обрабатываем пакет в проводе
        if (!wireType.get().processPacket(world, currentPos, packet)) {
            DebugManager.getInstance().onPacketDied(packet, "Wire blocked packet");
            return true;
        }

        // Получаем возможные выходы
        List<Direction> exits = wireType.get().getExitDirections(
                world, currentPos, packet, currentDir
        );

        if (exits.isEmpty()) {
            DebugManager.getInstance().onPacketDied(packet, "No exit directions");
            return true;
        }

        // Создаем новые пакеты для каждого выхода
        for (Direction exit : exits) {
            BlockPos nextPos = currentPos.offset(exit);

            // Проверяем, можем ли мы передать сигнал в эту позицию
            if (WireDetector.canTransmit(world, currentPos, nextPos, exit)) {
                SignalPacket newPacket = packet.withNewPosition(nextPos, exit);
                newPackets.add(newPacket);
                DebugManager.getInstance().onPacketMoved(packet, newPacket);
            }
        }

        return true; // Удаляем исходный пакет
    }

    private void handleDecoderReception(BlockPos pos, SignalPacket packet, Direction entryDirection) {
        if (world.getBlockEntity(pos) instanceof DecoderBlockEntity decoder) {
            decoder.receivePacket(packet, entryDirection);
        }
    }

    public Set<SignalPacket> getActivePackets() {
        return new HashSet<>(activePackets);
    }

    public void clearAllPackets() {
        activePackets.clear();
    }
}
