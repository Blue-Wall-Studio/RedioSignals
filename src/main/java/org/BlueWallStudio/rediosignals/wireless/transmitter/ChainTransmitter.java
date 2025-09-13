package org.BlueWallStudio.rediosignals.wireless.transmitter;

import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.state.property.Properties;
import org.BlueWallStudio.rediosignals.ModTags;
import org.BlueWallStudio.rediosignals.packet.Packet;
import org.BlueWallStudio.rediosignals.wireless.WirelessTransmissionConfig;

// Transmitter implementation for the chain (with axis check)
public class ChainTransmitter implements WirelessTransmitter {

    @Override
    public boolean canTransmit(World world, BlockPos pos, Packet packet) {
        BlockState state = world.getBlockState(pos);

        // Check if this is a chain
        if (!state.isIn(ModTags.IRON_TRANSMITTERS)) {
            return false;
        }

        // Check that chain is connected to the wire and that chain axis is compatible
        // with wire direction
        return hasAdjacentWireWithMatchingAxis(world, pos, state);
    }

    @Override
    public Packet processWirelessTransmission(World world, BlockPos pos, Packet packet,
                                              Direction entryDirection) {

        if (!this.canTransmit(world, pos, packet)) {
            return null;
        }

        // Additional axis check for this specific chain
        BlockState state = world.getBlockState(pos);
        if (state.contains(Properties.AXIS)) {
            Axis chainAxis = state.get(Properties.AXIS);
            if (entryDirection != null && entryDirection.getAxis() != chainAxis) {
                // If the entry direction does not match this chain's axis - do not transmit
                return null;
            }
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

        // Queue packet creation in target position
        Packet newPacket = packet.withNewPosition(targetPos, config.transmissionDirection());

        if (newPacket == null) {
            return null;
        }

        return newPacket;
    }

    // Helper function for processWirelessTransmission
    private BlockPos findWirelessTarget(World world, BlockPos startPos, WirelessTransmissionConfig config,
                                        Packet ignoredPacket) {
        Direction dir = config.transmissionDirection();
        int maxRange = config.maxRange();
        boolean canPenetrate = config.canPenetrate();

        for (int i = 1; i <= maxRange; i++) {
            BlockPos checkPos = startPos.offset(dir, i);
            BlockState checkState = world.getBlockState(checkPos);

            // Use the config's isBlocked method instead of checking specific blocks
            if (config.isBlocked(checkState)) {
                break;
            }

            if (!canPenetrate && !checkState.isAir() &&
                    !checkState.isIn(ModTags.WIRELESS_RECEIVERS)) {
                break;
            }

            if (config.requireReceiver()) {
                if (checkState.isIn(ModTags.WIRELESS_RECEIVERS)) {
                    return checkPos;
                } else {
                    continue;
                }
            }

            if (checkState.isIn(ModTags.WIRELESS_RECEIVERS) || checkState.isIn(ModTags.ALL_WIRES)) {
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
        // Get chain axis (if for some reason the property is absent - allow all)
        Axis chainAxis = null;
        if (chainState.contains(Properties.AXIS)) {
            chainAxis = chainState.get(Properties.AXIS);
        }

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = chainPos.offset(dir);
            BlockState neighborState = world.getBlockState(neighbor);
            if (!neighborState.isIn(ModTags.ALL_WIRES))
                continue;

            // If axis is known, require direction axis to match with chain axis
            if (chainAxis != null) {
                if (dir.getAxis() == chainAxis) {
                    return true;
                } else {
                    // Wire found, but it's along an incompatible axis - ignore
                    continue;
                }
            } else {
                // If axis is not available - assume the neighboring wire is sufficient
                return true;
            }
        }
        return false;
    }

    @Override
    public WirelessTransmissionConfig getTransmissionConfig(World world, BlockPos pos, Packet packet,
                                                            Direction entryDirection) {
        // Axis checking now occurs at the wire level in AbstractWireType
        // Chain transmits in same direction that package is received from
        return WirelessTransmissionConfig.chain(entryDirection);
    }

    @Override
    public int getTransmitterPriority() {
        return 100;
    }
}
