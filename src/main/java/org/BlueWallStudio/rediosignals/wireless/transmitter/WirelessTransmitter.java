package org.BlueWallStudio.rediosignals.wireless.transmitter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.rediosignals.packet.Packet;
import org.BlueWallStudio.rediosignals.wireless.WirelessTransmissionConfig;

// Interface for wireless transmitters
public interface WirelessTransmitter {
    /**
     * Check if block can work as wireless transmitter
     */
    boolean canTransmit(World world, BlockPos pos, Packet packet);

    /**
     * Handles wireless packet transmission
     * 
     * @return modified packet or null if packet should be killed
     */
    Packet processWirelessTransmission(World world, BlockPos pos, Packet packet, Direction entryDirection);

    /**
     * Receives transmission configuration for given transmitter
     */
    WirelessTransmissionConfig getTransmissionConfig(World world, BlockPos pos, Packet packet,
            Direction entryDirection);

    /**
     * Transmitter priority (more = higher priority)
     */
    default int getTransmitterPriority() {
        return 0;
    }
}
