package org.BlueWallStudio.argest.blocks.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.blocks.ModBlocks;
import org.BlueWallStudio.argest.signal.*;
import org.BlueWallStudio.argest.wire.WireDetector;

import java.util.*;

public class EncoderBlockEntity extends BlockEntity{
    private Map<Direction, Integer> inputPowers = new HashMap<>();
    private Set<Direction> outputDirections = new HashSet<>();
    private Map<Direction, ConnectionType> connections = new HashMap<>();
    private int tickCounter = 0;
    private boolean needsConnectionUpdate = true;

    public EncoderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ENCODER_BLOCK_ENTITY, pos, state);
        initializeConnections();
    }

    private void initializeConnections() {
        for (Direction dir : Direction.values()) {
            inputPowers.put(dir, 0);
            connections.put(dir, ConnectionType.NONE);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, EncoderBlockEntity entity) {
        if (world.isClient) return;

        entity.tickCounter++;

        // Обновляем подключения каждые 20 тиков или при необходимости
        if (entity.needsConnectionUpdate || entity.tickCounter % 20 == 0) {
            entity.updateConnections(world);
            entity.needsConnectionUpdate = false;
        }

        // Обрабатываем сигналы каждые 10 тиков
        if (entity.tickCounter % 10 == 0) {
            entity.updateInputs(world);
            entity.tryTransmitSignal(world);
        }
    }

    public void updateConnections() {
        needsConnectionUpdate = true;
    }

    private void updateConnections(World world) {
        outputDirections.clear();

        for (Direction dir : Direction.values()) {
            BlockPos adjacentPos = pos.offset(dir);
            BlockState adjacentState = world.getBlockState(adjacentPos);

            if (WireDetector.isWire(world, adjacentPos)) {
                connections.put(dir, ConnectionType.WIRE_OUTPUT);
                outputDirections.add(dir);
            } else if (WireDetector.isDecoder(world, adjacentPos)) {
                connections.put(dir, ConnectionType.DECODER_OUTPUT);
                outputDirections.add(dir);
            } else if (world.getEmittedRedstonePower(adjacentPos, dir) > 0) {
                connections.put(dir, ConnectionType.REDSTONE_INPUT);
            } else {
                connections.put(dir, ConnectionType.NONE);
            }
        }
    }

    private void updateInputs(World world) {
        for (Direction dir : Direction.values()) {
            if (connections.get(dir) == ConnectionType.REDSTONE_INPUT ||
                    (connections.get(dir) == ConnectionType.NONE && !outputDirections.contains(dir))) {
                int power = world.getEmittedRedstonePower(pos.offset(dir), dir);
                inputPowers.put(dir, power);
            }
        }
    }

    private void tryTransmitSignal(World world) {
        if (outputDirections.isEmpty()) return;

        // Ищем направление активации
        Direction activationDir = findActivationDirection();
        if (activationDir == null) return;

        // Собираем силы сигналов с доступных входных направлений
        List<Direction> inputDirections = getInputDirections(activationDir);
        if (inputDirections.size() < 3) return; // Нужно минимум 3 входа

        int[] strengths = new int[3];
        for (int i = 0; i < 3 && i < inputDirections.size(); i++) {
            strengths[i] = inputPowers.get(inputDirections.get(i));
        }

        // Проверяем валидность сигнала
        int totalStrength = Arrays.stream(strengths).sum();
        if (totalStrength == 0) return;

        // Определяем тип сигнала
        SignalType signalType = determineSignalType(activationDir, strengths);

        // Отправляем сигналы во все доступные выходы
        for (Direction outputDir : outputDirections) {
            BlockPos outputPos = pos.offset(outputDir);
            SignalPacket packet = new SignalPacket(strengths, signalType, outputPos, outputDir, (ServerWorld) world);
            SignalManager.getInstance(world.getServer().getOverworld()).sendPacket(packet);
        }
    }

    private Direction findActivationDirection() {
        for (Map.Entry<Direction, Integer> entry : inputPowers.entrySet()) {
            Direction dir = entry.getKey();
            int power = entry.getValue();

            if (power > 0 && !outputDirections.contains(dir)) {
                return dir;
            }
        }
        return null;
    }

    private List<Direction> getInputDirections(Direction activationDir) {
        List<Direction> inputs = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            if (dir != activationDir && !outputDirections.contains(dir)) {
                inputs.add(dir);
            }
        }

        return inputs;
    }

    private SignalType determineSignalType(Direction activationDir, int[] strengths) {
        // Расширенная логика определения типа сигнала
        return switch (activationDir) {
            case DOWN -> SignalType.ASCENDING;
            case UP -> SignalType.DESCENDING;
            default -> {
                // Анализируем силы сигналов для определения типа
                int maxStrength = Arrays.stream(strengths).max().orElse(0);
                if (maxStrength > 10) {
                    yield SignalType.ASCENDING;
                } else if (maxStrength < 5) {
                    yield SignalType.DESCENDING;
                } else {
                    yield SignalType.NORMAL;
                }
            }
        };
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        // Сохраняем состояние подключений
        NbtCompound connectionsNbt = new NbtCompound();
        for (Map.Entry<Direction, ConnectionType> entry : connections.entrySet()) {
            connectionsNbt.putString(entry.getKey().getName(), entry.getValue().name());
        }
        nbt.put("connections", connectionsNbt);

        // Сохраняем силы входных сигналов
        NbtCompound inputsNbt = new NbtCompound();
        for (Map.Entry<Direction, Integer> entry : inputPowers.entrySet()) {
            inputsNbt.putInt(entry.getKey().getName(), entry.getValue());
        }
        nbt.put("inputs", inputsNbt);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        // Загружаем подключения
        if (nbt.contains("connections")) {
            NbtCompound connectionsNbt = nbt.getCompound("connections");
            for (Direction dir : Direction.values()) {
                String typeName = connectionsNbt.getString(dir.getName());
                try {
                    connections.put(dir, ConnectionType.valueOf(typeName));
                } catch (IllegalArgumentException e) {
                    connections.put(dir, ConnectionType.NONE);
                }
            }
        }

        // Загружаем входные сигналы
        if (nbt.contains("inputs")) {
            NbtCompound inputsNbt = nbt.getCompound("inputs");
            for (Direction dir : Direction.values()) {
                inputPowers.put(dir, inputsNbt.getInt(dir.getName()));
            }
        }
    }

    private enum ConnectionType {
        NONE,
        REDSTONE_INPUT,
        WIRE_OUTPUT,
        DECODER_OUTPUT
    }
}
