package org.BlueWallStudio.argest.signal;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PersistentState;
import org.BlueWallStudio.argest.blocks.entity.DecoderBlockEntity;
import org.BlueWallStudio.argest.debug.DebugManager;
import org.BlueWallStudio.argest.wire.WireDetector;
import org.BlueWallStudio.argest.wire.WireRegistry;
import org.BlueWallStudio.argest.wire.WireType;
import org.BlueWallStudio.argest.config.ModConfig;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SignalManager extends PersistentState {
    private static final Map<ServerWorld, SignalManager> instances = new ConcurrentHashMap<>();
    private final Set<SignalPacket> activePackets = ConcurrentHashMap.newKeySet();
    private final ServerWorld world;
    private boolean dirty = false;
    private static ModConfig config = ModConfig.getInstance();

    private SignalManager(ServerWorld world) {
        this.world = world;
    }

    public static SignalManager getInstance(ServerWorld world) {
        return instances.computeIfAbsent(world, w -> {
            // Загружаем сохраненные данные
            String key = "signal_manager_" + w.getRegistryKey().getValue().toString().replace(":", "_");
            return w.getPersistentStateManager().getOrCreate(
                    new PersistentState.Type<>(
                            () -> new SignalManager(w), // supplier для создания нового
                            (nbt, registries) -> fromNbt(nbt, registries, w), // функция загрузки
                            null // datafix type (не нужен)
            ),
                    key);
        });
    }

    public boolean sendPacket(SignalPacket packet) {
        if (!packet.isValid()) {
            return false;
        }

        // Ограничиваем количество активных пакетов
        if (config.maxPacketsPerTick != -1 && activePackets.size() >= config.maxPacketsPerTick) {
            return false;
        }

        boolean added = activePackets.add(packet);
        if (added) {
            DebugManager.getInstance().onPacketCreated(packet);
            markDirty();
        }
        return added;
    }

    public void tick() {
        int currentServerTick = getCurrentServerTick();

        // Обрабатываем пакеты только каждые N тиков для производительности
        if (currentServerTick % config.signalProcessingDelay != 0) {
            return;
        }

        boolean hadChanges = processActivePackets();
        if (config.maxPacketLifetimeTicks != -1) {
            hadChanges |= cleanupOldPackets(currentServerTick);
        }

        if (hadChanges) {
            markDirty();
        }
    }

    private int getCurrentServerTick() {
        return world.getServer().getTicks();
    }

    private boolean processActivePackets() {
        if (activePackets.isEmpty()) {
            return false;
        }

        Set<SignalPacket> packetsToRemove = new HashSet<>();
        Set<SignalPacket> newPackets = new HashSet<>();
        boolean hadChanges = false;

        for (SignalPacket packet : activePackets) {
            PacketProcessingResult result = processPacket(packet);

            if (result.shouldRemove()) {
                packetsToRemove.add(packet);
                hadChanges = true;
            }

            if (!result.getNewPackets().isEmpty()) {
                newPackets.addAll(result.getNewPackets());
                hadChanges = true;
            }
        }

        activePackets.removeAll(packetsToRemove);
        activePackets.addAll(newPackets);

        return hadChanges;
    }

    private boolean cleanupOldPackets(int currentServerTick) {
        int maxLifetimeTicks = config.maxPacketLifetimeTicks;

        int sizeBefore = activePackets.size();

        activePackets.removeIf(packet -> packet.getAge(currentServerTick) > maxLifetimeTicks);

        return activePackets.size() != sizeBefore;
    }

    private PacketProcessingResult processPacket(SignalPacket packet) {
        BlockPos currentPos = packet.getCurrentPos();
        Direction currentDir = packet.getCurrentDirection();

        // Проверяем, является ли текущая позиция декодером
        if (WireDetector.isDecoder(world, currentPos)) {
            handleDecoderReception(currentPos, packet, currentDir);
            DebugManager.getInstance().onPacketDied(packet, "Reached decoder");
            return PacketProcessingResult.remove();
        }

        // Проверяем, есть ли провод в текущей позиции
        Optional<WireType> wireType = WireRegistry.getWireType(world.getBlockState(currentPos));
        if (wireType.isEmpty()) {
            DebugManager.getInstance().onPacketDied(packet, "No wire at position");
            return PacketProcessingResult.remove();
        }

        // Обрабатываем пакет в проводе
        WireType wire = wireType.get();
        if (!wire.processPacket(world, currentPos, packet)) {
            DebugManager.getInstance().onPacketDied(packet, "Wire blocked packet");
            return PacketProcessingResult.remove();
        }

        // Получаем возможные выходы
        List<Direction> exits = wire.getExitDirections(world, currentPos, packet, currentDir);
        if (exits.isEmpty()) {
            DebugManager.getInstance().onPacketDied(packet, "No exit directions");
            return PacketProcessingResult.remove();
        }

        // Создаем новые пакеты для каждого выхода
        Set<SignalPacket> newPackets = new HashSet<>();
        for (Direction exit : exits) {
            BlockPos nextPos = currentPos.offset(exit);

            // Проверяем, можем ли мы передать сигнал в эту позицию
            if (WireDetector.canTransmit(world, currentPos, nextPos, exit)) {
                SignalPacket newPacket = packet.withNewPosition(nextPos, exit);
                newPackets.add(newPacket);
                DebugManager.getInstance().onPacketMoved(packet, newPacket);
            }
        }

        return PacketProcessingResult.removeAndAdd(newPackets);
    }

    private void handleDecoderReception(BlockPos pos, SignalPacket packet, Direction entryDirection) {
        if (world.getBlockEntity(pos) instanceof DecoderBlockEntity decoder) {
            decoder.receivePacket(packet, entryDirection);
        }
    }

    public Set<SignalPacket> getActivePackets() {
        return Collections.unmodifiableSet(activePackets);
    }

    public void clearAllPackets() {
        activePackets.clear();
        markDirty();
    }

    // Реализация PersistentState
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList packetsNbt = new NbtList();

        for (SignalPacket packet : activePackets) {
            NbtCompound packetNbt = new NbtCompound();

            // Сохраняем массив сил сигнала
            int[] strengths = packet.getSignalStrengths();
            NbtList strengthsNbt = new NbtList();
            for (int strength : strengths) {
                strengthsNbt.add(NbtInt.of(strength));
            }
            packetNbt.put("strengths", strengthsNbt);

            // Сохраняем тип сигнала
            packetNbt.putString("signal_type", packet.getSignalType().name());

            // Сохраняем позицию
            NbtCompound posNbt = new NbtCompound();
            posNbt.putInt("x", packet.getCurrentPos().getX());
            posNbt.putInt("y", packet.getCurrentPos().getY());
            posNbt.putInt("z", packet.getCurrentPos().getZ());
            packetNbt.put("pos", posNbt);

            // Сохраняем направление
            packetNbt.putString("direction", packet.getCurrentDirection().name());

            // Сохраняем тик создания
            packetNbt.putInt("creation_tick", packet.getCreationTick());

            packetsNbt.add(packetNbt);
        }

        nbt.put("active_packets", packetsNbt);
        return nbt;
    }

    public static SignalManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries, ServerWorld world) {
        SignalManager manager = new SignalManager(world);

        if (nbt.contains("active_packets")) {
            NbtList packetsNbt = nbt.getList("active_packets", NbtElement.COMPOUND_TYPE);

            for (int i = 0; i < packetsNbt.size(); i++) {
                NbtCompound packetNbt = packetsNbt.getCompound(i);

                try {
                    // Загружаем силы сигнала
                    NbtList strengthsNbt = packetNbt.getList("strengths", NbtElement.INT_TYPE);
                    int[] strengths = new int[strengthsNbt.size()];
                    for (int j = 0; j < strengthsNbt.size(); j++) {
                        strengths[j] = strengthsNbt.getInt(j);
                    }

                    // Загружаем тип сигнала
                    SignalType signalType = SignalType.valueOf(packetNbt.getString("signal_type"));

                    // Загружаем позицию
                    NbtCompound posNbt = packetNbt.getCompound("pos");
                    BlockPos pos = new BlockPos(
                            posNbt.getInt("x"),
                            posNbt.getInt("y"),
                            posNbt.getInt("z"));

                    // Загружаем направление
                    Direction direction = Direction.valueOf(packetNbt.getString("direction"));

                    // Загружаем тик создания
                    int creationTick = packetNbt.getInt("creation_tick");

                    // Создаем пакет
                    SignalPacket packet = new SignalPacket(strengths, signalType, pos, direction, world, creationTick);
                    if (packet.isValid()) {
                        manager.activePackets.add(packet);
                    }
                } catch (Exception e) {
                    // Логируем ошибку и пропускаем поврежденный пакет
                    System.err.println("Failed to load signal packet: " + e.getMessage());
                }
            }
        }

        return manager;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    // Очистка экземпляров при выгрузке мира
    public static void onWorldUnload(ServerWorld world) {
        instances.remove(world);
    }

    // Вспомогательный класс для результатов обработки пакетов
    private static class PacketProcessingResult {
        private final boolean shouldRemove;
        private final Set<SignalPacket> newPackets;

        private PacketProcessingResult(boolean shouldRemove, Set<SignalPacket> newPackets) {
            this.shouldRemove = shouldRemove;
            this.newPackets = newPackets != null ? newPackets : Collections.emptySet();
        }

        public static PacketProcessingResult remove() {
            return new PacketProcessingResult(true, Collections.emptySet());
        }

        public static PacketProcessingResult removeAndAdd(Set<SignalPacket> newPackets) {
            return new PacketProcessingResult(true, newPackets);
        }

        public static PacketProcessingResult keep() {
            return new PacketProcessingResult(false, Collections.emptySet());
        }

        public boolean shouldRemove() {
            return shouldRemove;
        }

        public Set<SignalPacket> getNewPackets() {
            return newPackets;
        }
    }
}
