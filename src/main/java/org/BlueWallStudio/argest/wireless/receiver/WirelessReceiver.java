package org.BlueWallStudio.argest.wireless.receiver;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;

// interface for blocks that can receive wireless signals
public interface WirelessReceiver {
    /**
     * Check, if block can receive wireless signals
     */
    boolean canReceiveWireless(World world, BlockPos pos, SignalPacket packet);

    /**
     * Processes wireless packet reception
     * 
     * @return modified package or null if packet must be destroyed
     */
    SignalPacket processWirelessReception(World world, BlockPos pos, SignalPacket packet, Direction from);

    /**
     * Receiver priority (more = higher priority)
     */
    default int getReceiverPriority() {
        return 0;
    }
}
