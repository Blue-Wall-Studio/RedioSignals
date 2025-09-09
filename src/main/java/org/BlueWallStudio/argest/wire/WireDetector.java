package org.BlueWallStudio.argest.wire;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.ModTags;
import org.BlueWallStudio.argest.blocks.DecoderBlock;
import org.BlueWallStudio.argest.wireless.receiver.WirelessReceiverRegistry;
import org.BlueWallStudio.argest.wireless.transmitter.WirelessTransmitterRegistry;

import java.util.Optional;

// Updated WireDetector with wireless receivers support
public class WireDetector {
    /**
     * Determine, if block is wire
     */
    public static boolean isWire(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return WireRegistry.getWireType(state).isPresent();
    }

    /**
     * Determine, if block is decoder (terminal element)
     */
    public static boolean isDecoder(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof DecoderBlock;
    }

    /**
     * Determine, if block is wireless receiver
     */
    public static boolean isWirelessReceiver(World world, BlockPos pos) {
        return WirelessReceiverRegistry.isWirelessReceiver(world, pos);
    }

    /**
     * Determine, if block is wireless transmitter
     */
    public static boolean isWirelessTransmitter(World world, BlockPos pos) {
        return WirelessTransmitterRegistry.isWirelessTransmitter(world, pos);
    }

    /**
     * Get wire type in specified direction
     */
    public static Optional<WireType> getWireType(World world, BlockPos pos) {
        return WireRegistry.getWireType(world.getBlockState(pos));
    }

    /**
     * Checks if packet can go between two directions
     */
    public static boolean canTransmit(World world, BlockPos from, BlockPos to, Direction direction) {
        // Check if wire is present at starting position
        Optional<WireType> fromWire = getWireType(world, from);
        if (fromWire.isEmpty())
            return false;

        // Check if wire can transfer if specified direction
        if (!fromWire.get().canExitTo(world, from, direction))
            return false;

        // Check target position
        // 1. If decoder - allow;
        if (isDecoder(world, to))
            return true;

        // 2. If wireless receiver - allow;
        if (isWirelessReceiver(world, to))
            return true;

        // 3. If wireless transmitter - allow;
        if (isWirelessTransmitter(world, to))
            return true;

        // 4. If wire - check compatibility
        Optional<WireType> toWire = getWireType(world, to);
        if (toWire.isPresent()) {
            return toWire.get().canEnterFrom(world, to, direction.getOpposite());
        }

        // In all other cases - disallow
        return false;
    }
}
