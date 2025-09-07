package org.BlueWallStudio.argest.packet;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.Objects;

public class Packet {
    private final int[] packetStrengths; // [left, front, right] relative to entry
    private final PacketType packetType;
    private final BlockPos currentPos;
    private final Direction currentDirection;
    private final int creationTick; // Server tick for packet creation
    private final int totalStrength;

    public Packet(int[] strengths, PacketType type, BlockPos pos, Direction dir, int creationTick) {
        this.packetStrengths = strengths;
        this.packetType = type;
        this.currentPos = pos;
        this.currentDirection = dir;
        this.creationTick = creationTick;
        this.totalStrength = calculateTotalStrength(strengths);
    }

    // Constructor for creating packet with current server tick as creation tick
    public Packet(int[] strengths, PacketType type, BlockPos pos, Direction dir, ServerWorld world) {
        this(strengths, type, pos, dir, getCurrentServerTick(world));
    }

    /*
     * Serialization
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putIntArray("strength", packetStrengths);
        nbt.putString("packetType", packetType.name());
        nbt.putLong("pos", currentPos.asLong());
        nbt.putInt("dir", currentDirection.getId());
        nbt.putInt("creationTick", creationTick);
        return nbt;
    }

    public static Packet fromNbt(NbtCompound nbt) {
        int[] strength = nbt.getIntArray("strength");
        PacketType packetType = PacketType.valueOf(nbt.getString("packetType"));
        BlockPos pos = BlockPos.fromLong(nbt.getLong("pos"));
        Direction dir = Direction.byId(nbt.getInt("dir"));
        int creationTick = nbt.getInt("creationTick");
        return new Packet(strength, packetType, pos, dir, creationTick);
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
        return totalStrength > 0 && packetType != null && currentPos != null && currentDirection != null;
    }

    public Packet withNewPosition(BlockPos newPos, Direction newDir) {
        // Save same creation tick when moving
        return new Packet(packetStrengths, packetType, newPos, newDir, creationTick);
    }

    public Packet withModifiedStrengths(int[] newStrengths) {
        return new Packet(newStrengths, packetType, currentPos, currentDirection, creationTick);
    }

    public int getAge(int currentTick) {
        return currentTick - creationTick;
    }

    public boolean isExpired(int currentTick, int maxLifetimeTicks) {
        return getAge(currentTick) > maxLifetimeTicks;
    }

    // Getters
    public int[] getPacketStrengths() {
        return packetStrengths.clone();
    }

    public int getPacketStrength(int index) {
        if (index < 0 || index >= packetStrengths.length) {
            throw new IndexOutOfBoundsException("Packet strength index out of bounds: " + index);
        }
        return packetStrengths[index];
    }

    public int getLeftStrength() {
        return packetStrengths[0];
    }

    public int getFrontStrength() {
        return packetStrengths[1];
    }

    public int getRightStrength() {
        return packetStrengths[2];
    }

    public PacketType getPacketType() {
        return packetType;
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

        Packet that = (Packet) obj;
        return creationTick == that.creationTick &&
                totalStrength == that.totalStrength &&
                Arrays.equals(packetStrengths, that.packetStrengths) &&
                Objects.equals(packetType, that.packetType) &&
                Objects.equals(currentPos, that.currentPos) &&
                currentDirection == that.currentDirection;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Arrays.hashCode(packetStrengths),
                packetType,
                currentPos,
                currentDirection,
                creationTick,
                totalStrength);
    }

    @Override
    public String toString() {
        return String.format(
                "Packet{pos=%s, dir=%s, strengths=%s, type=%s, tick=%d, total=%d}",
                currentPos, currentDirection, Arrays.toString(packetStrengths),
                packetType, creationTick, totalStrength);
    }
}
