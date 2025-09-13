package org.BlueWallStudio.rediosignals.wireless.receiver;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.rediosignals.ModTags;
import org.BlueWallStudio.rediosignals.packet.Packet;
import org.BlueWallStudio.rediosignals.packet.PacketType;
import org.BlueWallStudio.rediosignals.wire.WireDetector;

// Receiver implementation for copper grate
public class CopperGrateReceiver implements WirelessReceiver {

    @Override
    public boolean canReceiveWireless(World world, BlockPos pos, Packet packet) {
        return world.getBlockState(pos).isIn(ModTags.COPPER_GRATE_RECEIVERS);
    }

    @Override
    public Packet processWirelessReception(World world, BlockPos pos, Packet packet, Direction from) {
        // Make paket descending
        int[] strengths = packet.getPacketStrengths();
        int creationTick = packet.getCreationTick();

        // Preferrably go into same direction that package came from
        Direction preferred = from != null ? from : packet.getCurrentDirection();
        if (preferred != null) {
            BlockPos forward = pos.offset(preferred);
            BlockState forwardstate = world.getBlockState(forward);
            if (forwardstate.isIn(ModTags.ALL_WIRES) || WireDetector.isDecoder(world, forward)) {
                return new Packet(strengths, PacketType.DESCENDING, forward, preferred, creationTick);
            }
        }

        // Then, try to find any neighboring wire/decoder (if wire is not connected
        // right ahead)
        for (Direction d : Direction.values()) {
            BlockPos np = pos.offset(d);
            BlockState npstate = world.getBlockState(np);
            if (npstate.isIn(ModTags.ALL_WIRES) || WireDetector.isDecoder(world, np)) {
                return new Packet(strengths, PacketType.DESCENDING, np, d, creationTick);
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
