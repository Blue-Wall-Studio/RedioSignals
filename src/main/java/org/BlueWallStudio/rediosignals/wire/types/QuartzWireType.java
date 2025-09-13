package org.BlueWallStudio.rediosignals.wire.types;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.rediosignals.ModTags;
import org.BlueWallStudio.rediosignals.packet.Packet;
import org.BlueWallStudio.rediosignals.packet.PacketType;

import java.util.*;

public class QuartzWireType extends AbstractWireType {

    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isIn(ModTags.QUARTZ_WIRES);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, Packet packet) {
		return true;
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public List<Direction> getExitDirections(World world, BlockPos pos,
                                             Packet packet, Direction entryDirection) {
        List<Direction> exits = new ArrayList<>();
        PacketType packetType = packet.getPacketType();
		 if (packet.stepsRemaining < 8) {
             packet.stepsRemaining = 99;
			 return exits;
			}
        // Quartz behaves like normal wire when allowing movement
        switch (packetType) {
            case ASCENDING:
                // Priority upward, if not available - horizontally
                if (hasValidTargetAt(world, pos.up())) {
                    exits.add(Direction.UP);
                } else {
                    addHorizontalDirections(world, pos, exits, entryDirection, dir -> true, true);
                }
                break;

            case DESCENDING:
                // Priority downward, if not available - horizontally
                if (hasValidTargetAt(world, pos.down())) {
                    exits.add(Direction.DOWN);
                } else {
                    addHorizontalDirections(world, pos, exits, entryDirection, dir -> true, true);
                }
                break;

            default:
                // Normal behavior - to all available directions
                addAllDirections(world, pos, exits, entryDirection, dir -> true, true);
                break;
        }

        return exits;
    }
}
