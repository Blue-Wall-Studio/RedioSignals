package org.BlueWallStudio.rediosignals.wireless.receiver;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.rediosignals.packet.Packet;

// interface for blocks that can receive wireless packets
public interface WirelessReceiver {
    /**
     * Check, if block can receive wireless packets
     */
    boolean canReceiveWireless(World world, BlockPos pos, Packet packet);

    /**
     * Processes wireless packet reception
     * 
     * @return modified package or null if packet must be destroyed
     */
    Packet processWirelessReception(World world, BlockPos pos, Packet packet, Direction from);

    /**
     * Receiver priority (more = higher priority)
     */
    default int getReceiverPriority() {
        return 0;
    }
}
