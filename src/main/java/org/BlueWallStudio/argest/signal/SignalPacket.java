package org.BlueWallStudio.argest.signal;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.Objects;

public class SignalPacket {
    private final int[] signalStrengths; // [left, front, right] relative to entry
    private final SignalType signalType;
    private final BlockPos currentPos;
    private final Direction currentDirection;
    private final int creationTick; // Серверный тик создания пакета
    private final int totalStrength;
    private final ServerWorld world;

    public SignalPacket(int[] strengths, SignalType type, BlockPos pos, Direction dir, ServerWorld world, int creationTick) {
        this.signalStrengths = strengths.clone();
        this.signalType = type;
        this.currentPos = pos.toImmutable();
        this.currentDirection = dir;
        this.creationTick = creationTick;
        this.totalStrength = calculateTotalStrength(strengths);
        this.world = world;
    }

    // Конструктор для создания с текущим серверным тиком
    public SignalPacket(int[] strengths, SignalType type, BlockPos pos, Direction dir, ServerWorld world) {
        this(strengths, type, pos, dir, world, getCurrentServerTick(world));
    }

    // Конструктор для загрузки из NBT с явным указанием тика
    public static SignalPacket fromSavedData(int[] strengths, SignalType type, BlockPos pos, Direction dir, ServerWorld world, int savedTick) {
        return new SignalPacket(strengths, type, pos, dir, world, savedTick);
    }

    private static int calculateTotalStrength(int[] strengths) {
        int total = 0;
        for (int strength : strengths) {
            total += Math.max(0, strength); // Игнорируем отрицательные значения
        }
        return total;
    }

    private static int getCurrentServerTick(ServerWorld world) {
        // Используем серверный тик - это правильный способ для игровой логики
        return world.getServer().getTicks();
    }

    public boolean isValid() {
        return totalStrength > 0 && signalType != null && currentPos != null && currentDirection != null;
    }

    public SignalPacket withNewPosition(BlockPos newPos, Direction newDir) {
        // Сохраняем тот же тик создания при перемещении
        return new SignalPacket(signalStrengths, signalType, newPos, newDir, world, creationTick);
    }

    public SignalPacket withModifiedStrengths(int[] newStrengths) {
        return new SignalPacket(newStrengths, signalType, currentPos, currentDirection, world, creationTick);
    }

    public int getAge(int currentTick) {
        return currentTick - creationTick;
    }

    public boolean isExpired(int currentTick, int maxLifetimeTicks) {
        return getAge(currentTick) > maxLifetimeTicks;
    }

    // Геттеры
    public int[] getSignalStrengths() {
        return signalStrengths.clone();
    }

    public int getSignalStrength(int index) {
        if (index < 0 || index >= signalStrengths.length) {
            throw new IndexOutOfBoundsException("Signal strength index out of bounds: " + index);
        }
        return signalStrengths[index];
    }

    public int getLeftStrength() { return signalStrengths[0]; }
    public int getFrontStrength() { return signalStrengths[1]; }
    public int getRightStrength() { return signalStrengths[2]; }

    public SignalType getSignalType() { return signalType; }
    public BlockPos getCurrentPos() { return currentPos; }
    public Direction getCurrentDirection() { return currentDirection; }
    public int getCreationTick() { return creationTick; }
    public int getTotalStrength() { return totalStrength; }
    public ServerWorld getWorld() { return world; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SignalPacket that = (SignalPacket) obj;
        return creationTick == that.creationTick &&
                totalStrength == that.totalStrength &&
                Arrays.equals(signalStrengths, that.signalStrengths) &&
                Objects.equals(signalType, that.signalType) &&
                Objects.equals(currentPos, that.currentPos) &&
                currentDirection == that.currentDirection &&
                Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Arrays.hashCode(signalStrengths),
                signalType,
                currentPos,
                currentDirection,
                creationTick,
                totalStrength,
                world
        );
    }

    @Override
    public String toString() {
        return String.format(
                "SignalPacket{pos=%s, dir=%s, strengths=%s, type=%s, tick=%d, total=%d}",
                currentPos, currentDirection, Arrays.toString(signalStrengths),
                signalType, creationTick, totalStrength
        );
    }
}