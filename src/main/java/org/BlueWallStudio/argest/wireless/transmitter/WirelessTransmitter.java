package org.BlueWallStudio.argest.wireless.transmitter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;
import org.BlueWallStudio.argest.wireless.WirelessTransmissionConfig;

// Interface for wireless transmitters
public interface WirelessTransmitter {
    /**
     * Check if block can work as wireless transmitter
     */
    boolean canTransmit(World world, BlockPos pos, SignalPacket packet);

    /**
     * Handles wireless signal transmission
     * 
     * @return modified packet or null if packet should be killed
     */
    SignalPacket processWirelessTransmission(World world, BlockPos pos, SignalPacket packet, Direction entryDirection);

    /**
     * Receives transmission configuration for given transmitter
     */
    WirelessTransmissionConfig getTransmissionConfig(World world, BlockPos pos, SignalPacket packet,
            Direction entryDirection);

    /**
     * Transmitter priority (more = higher priority)
     */
    default int getTransmitterPriority() {
        return 0;
    }
}
