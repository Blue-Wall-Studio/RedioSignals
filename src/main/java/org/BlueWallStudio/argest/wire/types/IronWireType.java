package org.BlueWallStudio.argest.wire.types;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.ModTags;
import org.BlueWallStudio.argest.packet.Packet;
import org.BlueWallStudio.argest.packet.PacketType;

import java.util.ArrayList;
import java.util.List;

/**
 * Counter-clockwise wire - same as copper but rotates counter-clockwise
 */
public class IronWireType extends AbstractWireType {
    @Override
    public boolean canHandle(BlockState blockState){
        return blockState.isIn(ModTags.IRON_WIRES);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, Packet packet){
        return true;
    }

    @Override
    public int getPriority(){
        return 100;
    }

    @Override
    public List<Direction> getExitDirections(World world, BlockPos pos,
                                             Packet packet, Direction entryDirection) {
        List<Direction> exits = new ArrayList<>();
        PacketType packetType = packet.getPacketType();

        if (packetType == PacketType.ASCENDING) {
            if (hasValidTargetAt(world, pos.up())) {
                exits.add(Direction.UP);
            } else {
                // Use counter-clockwise method from AbstractWireType
                addHorizontalDirectionsCounterClockwise(world, pos, exits, entryDirection, dir -> true, true);
            }
        } else if (packetType == PacketType.DESCENDING) {
            if (hasValidTargetAt(world, pos.down())) {
                exits.add(Direction.DOWN);
            } else {
                addHorizontalDirectionsCounterClockwise(world, pos, exits, entryDirection, dir -> true, true);
            }
        } else {
            // For common packets use addAllDirections
            // (it uses standard Direction.values() order, but addAllDirectionsCounterClockwise could be created too)
            addAllDirections(world, pos, exits, entryDirection, dir -> true, true);
        }

        return exits;
    }
}
