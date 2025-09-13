package org.BlueWallStudio.rediosignals.wire;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.BlueWallStudio.rediosignals.packet.Packet;

public interface WireType {
    /**
     * Check, can wire type work with specified block
     */
    boolean canHandle(BlockState blockState);

    /**
     * Processes packet transfer via wire
     * 
     * @return true if paket can go further, false if it stops
     */
    boolean processPacket(World world, BlockPos pos, Packet packet);

    /**
     * Get processing priority (more = higher priority)
     */
    int getPriority();

    /**
     * Can package enter wire from specified direction?
     */
    boolean canEnterFrom(World world, BlockPos pos, net.minecraft.util.math.Direction from);

    /**
     * Can wire exit wire in specified direction?
     */
    boolean canExitTo(World world, BlockPos pos, net.minecraft.util.math.Direction to);

    /**
     * Get possible exit directions for the packet
     */
    java.util.List<net.minecraft.util.math.Direction> getExitDirections(
            World world, BlockPos pos, Packet packet,
            net.minecraft.util.math.Direction entryDirection);
}
