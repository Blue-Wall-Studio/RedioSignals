package org.BlueWallStudio.argest.signal;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.Objects;

public class SignalPacket {
    private final int[] signalStrengths; // [left, front, right] relative to entry
    private final SignalType signalType;
    private final BlockPos currentPos;
    private final Direction currentDirection;
    private final int creationTick; // Server tick for packet creation
    private final int totalStrength;

    public SignalPacket(int[] strengths, SignalType type, BlockPos pos, Direction dir, int creationTick) {
        this.signalStrengths = strengths.clone();
        this.signalType = type;
        this.currentPos = pos.toImmutable();
        this.currentDirection = dir;
        this.creationTick = creationTick;
        this.totalStrength = calculateTotalStrength(strengths);
    }

    // Constructor for creating packet with current server tick as creation tick
    public SignalPacket(int[] strengths, SignalType type, BlockPos pos, Direction dir, ServerWorld world) {
        this(strengths, type, pos, dir, getCurrentServerTick(world));
    }

    /*
     * Serialization
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putIntArray("strength", signalStrengths);
        nbt.putString("signalType", signalType.name());
        nbt.putLong("pos", currentPos.asLong());
        nbt.putInt("dir", currentDirection.getId());
        nbt.putInt("creationTick", creationTick);
        return nbt;
    }

    public static SignalPacket fromNbt(NbtCompound nbt) {
        int[] strength = nbt.getIntArray("strength");
        SignalType signalType = SignalType.valueOf(nbt.getString("signalType"));
        BlockPos pos = BlockPos.fromLong(nbt.getLong("pos"));
        Direction dir = Direction.byId(nbt.getInt("dir"));
        int creationTick = nbt.getInt("creationTick");
        return new SignalPacket(strength, signalType, pos, dir, creationTick);
    }

    private static int calculateTotalStrength(int[] strengths) {
        int total = 0;
        for (int strength : strengths) {
            total += Math.max(0, strength); // Ignore negative values
        }
        return total;
    }

    private static int getCurrentServerTick(ServerWorld world) {
        // Use server tick - it's the correct method for game logic
        return world.getServer().getTicks();
    }

    public boolean isValid() {
        return totalStrength > 0 && signalType != null && currentPos != null && currentDirection != null;
    }

    public SignalPacket withNewPosition(BlockPos newPos, Direction newDir) {
        // Save same creation tick when moving
        return new SignalPacket(signalStrengths, signalType, newPos, newDir, creationTick);
    }

    public SignalPacket withModifiedStrengths(int[] newStrengths) {
        return new SignalPacket(newStrengths, signalType, currentPos, currentDirection, creationTick);
    }

    public int getAge(int currentTick) {
        return currentTick - creationTick;
    }

    public boolean isExpired(int currentTick, int maxLifetimeTicks) {
        return getAge(currentTick) > maxLifetimeTicks;
    }

    // Getters
    public int[] getSignalStrengths() {
        return signalStrengths.clone();
    }

    public int getSignalStrength(int index) {
        if (index < 0 || index >= signalStrengths.length) {
            throw new IndexOutOfBoundsException("Signal strength index out of bounds: " + index);
        }
        return signalStrengths[index];
    }

    public int getLeftStrength() {
        return signalStrengths[0];
    }

    public int getFrontStrength() {
        return signalStrengths[1];
    }

    public int getRightStrength() {
        return signalStrengths[2];
    }

    public SignalType getSignalType() {
        return signalType;
    }

    public BlockPos getCurrentPos() {
        return currentPos;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public int getCreationTick() {
        return creationTick;
    }

    public int getTotalStrength() {
        return totalStrength;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        SignalPacket that = (SignalPacket) obj;
        return creationTick == that.creationTick &&
                totalStrength == that.totalStrength &&
                Arrays.equals(signalStrengths, that.signalStrengths) &&
                Objects.equals(signalType, that.signalType) &&
                Objects.equals(currentPos, that.currentPos) &&
                currentDirection == that.currentDirection;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Arrays.hashCode(signalStrengths),
                signalType,
                currentPos,
                currentDirection,
                creationTick,
                totalStrength);
    }

    @Override
    public String toString() {
        return String.format(
                "SignalPacket{pos=%s, dir=%s, strengths=%s, type=%s, tick=%d, total=%d}",
                currentPos, currentDirection, Arrays.toString(signalStrengths),
                signalType, creationTick, totalStrength);
    }
}
