package org.BlueWallStudio.argest.wireless.receiver;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;
import org.BlueWallStudio.argest.signal.SignalType;
import org.BlueWallStudio.argest.wire.WireDetector;

// Receiver implementation for copper grate
public class CopperGrateReceiver implements WirelessReceiver {

    @Override
    public boolean canReceiveWireless(World world, BlockPos pos, SignalPacket packet) {
        return world.getBlockState(pos).isOf(Blocks.COPPER_GRATE);
    }

    @Override
    public SignalPacket processWirelessReception(World world, BlockPos pos, SignalPacket packet, Direction from) {
        // Make paket descending
        int[] strengths = packet.getSignalStrengths();
        int creationTick = packet.getCreationTick();

        // Preferrably go into same direction that package came from
        Direction preferred = from != null ? from : packet.getCurrentDirection();
        if (preferred != null) {
            BlockPos forward = pos.offset(preferred);
            if (WireDetector.isWire(world, forward) || WireDetector.isDecoder(world, forward)) {
                return new SignalPacket(strengths, SignalType.DESCENDING, forward, preferred, creationTick);
            }
        }

        // Then, try to find any neighboring wire/decoder (if wire is not connected
        // right ahead)
        for (Direction d : Direction.values()) {
            BlockPos np = pos.offset(d);
            if (WireDetector.isWire(world, np) || WireDetector.isDecoder(world, np)) {
                return new SignalPacket(strengths, SignalType.DESCENDING, np, d, creationTick);
            }
        }

        // If no wires nearby - kill the packet (return null)
        return null;
    }

    @Override
    public int getReceiverPriority() {
        return 100;
    }
}
