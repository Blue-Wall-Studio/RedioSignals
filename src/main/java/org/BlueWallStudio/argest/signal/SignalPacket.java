package org.BlueWallStudio.argest.signal;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SignalPacket {
    private final int[] signalStrengths; // [left, front, right] relative to entry
    private final SignalType signalType;
    private final BlockPos currentPos;
    private final Direction currentDirection;
    private final long creationTime;
    private final int totalStrength;
    private final ServerWorld world; // Добавляем поле мира

    public SignalPacket(int[] strengths, SignalType type, BlockPos pos, Direction dir, ServerWorld world) {
        this.signalStrengths = strengths.clone();
        this.signalType = type;
        this.currentPos = pos.toImmutable();
        this.currentDirection = dir;
        this.creationTime = System.currentTimeMillis();
        this.totalStrength = strengths[0] + strengths[1] + strengths[2];
        this.world = world;
    }

    public boolean isValid() {
        return totalStrength > 0;
    }

    public SignalPacket withNewPosition(BlockPos newPos, Direction newDir) {
        // Сохраняем тот же мир при создании нового пакета с новой позицией
        return new SignalPacket(signalStrengths, signalType, newPos, newDir, world);
    }

    // Геттеры
    public int[] getSignalStrengths() { return signalStrengths.clone(); }
    public SignalType getSignalType() { return signalType; }
    public BlockPos getCurrentPos() { return currentPos; }
    public Direction getCurrentDirection() { return currentDirection; }
    public long getCreationTime() { return creationTime; }
    public int getTotalStrength() { return totalStrength; }
    public ServerWorld getWorld() { return world; } // Новый геттер для мира
}
