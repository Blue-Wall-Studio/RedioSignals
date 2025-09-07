package org.BlueWallStudio.argest.wireless.transmitter;

import java.util.Set;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.state.property.Properties;
import org.BlueWallStudio.argest.packet.Packet;
import org.BlueWallStudio.argest.wire.WireDetector;
import org.BlueWallStudio.argest.wireless.WirelessTransmissionConfig;

// Transmitter implementation for the chain (with axis check)
public class ChainTransmitter implements WirelessTransmitter {

    @Override
    public boolean canTransmit(World world, BlockPos pos, Packet packet) {
        BlockState state = world.getBlockState(pos);

        // Check if is chain
        if (!state.isOf(Blocks.CHAIN)) {
            return false;
        }

        // Check, that chain is connected to the wire and that chain axis is compatible
        // with wire direction
        return hasAdjacentWireWithMatchingAxis(world, pos, state);
    }

    @Override
    public Packet processWirelessTransmission(World world, BlockPos pos, Packet packet,
            Direction entryDirection) {

        if (!this.canTransmit(world, pos, packet)) {
            return null;
        }

        WirelessTransmissionConfig config = this.getTransmissionConfig(world, pos, packet, entryDirection);
        if (config == null) {
            return null;
        }

        // Find target position for wireless transmission
        BlockPos targetPos = findWirelessTarget(world, pos, config, packet);
        if (targetPos == null) {
            return null;
        }

        // Create new packet in target position
        Packet newPacket = packet.withNewPosition(targetPos, config.transmissionDirection());

        if (newPacket == null) {
            return null;
        }

        return newPacket;
    }

    // Helper function for processWirelessTransmission
    private BlockPos findWirelessTarget(World world, BlockPos startPos, WirelessTransmissionConfig config,
            Packet packet) {
        Direction dir = config.transmissionDirection();
        int maxRange = config.maxRange();
        boolean canPenetrate = config.canPenetrate();
        Set<Block> blockingBlocks = config.blockingBlocks();

        for (int i = 1; i <= maxRange; i++) {
            BlockPos checkPos = startPos.offset(dir, i);
            BlockState checkState = world.getBlockState(checkPos);
            Block checkBlock = checkState.getBlock();

            if (blockingBlocks.contains(checkBlock)) {
                break;
            }

            if (!canPenetrate && !checkState.isAir() &&
                    !WireDetector.isWirelessReceiver(world, checkPos)) {
                break;
            }

            if (config.requireReceiver()) {
                if (WireDetector.isWirelessReceiver(world, checkPos)) {
                    return checkPos;
                } else {
                    continue;
                }
            }

            if (WireDetector.isWirelessReceiver(world, checkPos) || WireDetector.isWire(world, checkPos)) {
                return checkPos;
            }
        }

        return null;
    }

    /**
     * Search for neighboring wire, considering chain axis:
     * direction from chain to neighbor block must have same axis, as the chain
     */
    private boolean hasAdjacentWireWithMatchingAxis(World world, BlockPos chainPos, BlockState chainState) {
        // Get chain axis (if for some reasons property is absent - allow all)
        Axis chainAxis = null;
        if (chainState.contains(Properties.AXIS)) {
            chainAxis = chainState.get(Properties.AXIS);
        }

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = chainPos.offset(dir);
            if (!WireDetector.isWire(world, neighbor))
                continue;

            // If axis is known, require direction axis to match with chain axis
            if (chainAxis != null) {
                if (dir.getAxis() == chainAxis) {
                    return true;
                } else {
                    // Wire found, but in goes by incompatioble axis - ignore
                    continue;
                }
            } else {
                // If axis is not available - assume, that neighbor wire is enough
                return true;
            }
        }
        return false;
    }

    @Override
    public WirelessTransmissionConfig getTransmissionConfig(World world, BlockPos pos, Packet packet,
            Direction entryDirection) {
        // Optionally can match entryDirection.getAxis() with chain axis, if want an
        // additional checks
        BlockState state = world.getBlockState(pos);
        if (state.isOf(Blocks.CHAIN) && state.contains(Properties.AXIS)) {
            Axis chainAxis = state.get(Properties.AXIS);
            if (entryDirection != null && entryDirection.getAxis() != chainAxis) {
                // If incoming direction isn't in the chain axis - cancel transmission
                return null; // or return config that means "don't transmit"
            }
        }

        // Chain transmits in same direction that package is received from
        return WirelessTransmissionConfig.chain(entryDirection);
    }

    @Override
    public int getTransmitterPriority() {
        return 100;
    }
}
