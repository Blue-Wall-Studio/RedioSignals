package org.BlueWallStudio.argest.wire.types;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.ModTags;
import org.BlueWallStudio.argest.packet.Packet;
import org.BlueWallStudio.argest.packet.PacketType;

import java.util.*;

/**
 * Gold wire: prefers going "forward" (in entryDirection), otherwise uses
 * direction priority (UP/DOWN/N/E/S/W) and takes first available
 */
public class GoldWireType extends AbstractWireType {

    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isIn(ModTags.GOLD_WIRES);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, Packet packet) {
        return true; // Just passes further
    }

    @Override
    public int getPriority() {
        return 90;
    }

    @Override
    public List<Direction> getExitDirections(World world, BlockPos pos,
            Packet packet, Direction entryDirection) {
        List<Direction> exits = new ArrayList<>();
        PacketType packetType = packet.getPacketType();

        // Firstly, try forward
        if (entryDirection != null) {
            Direction forward = entryDirection;
            if (canPacketGoInDirection(packetType, forward) && hasValidTargetAt(world, pos.offset(forward))) {
                exits.add(forward);
                return exits;
            }
        }
        // Otherwise, basic CopperWire logic.
        if (packetType == PacketType.ASCENDING) {
            // ASCENDING priority
            if (hasValidTargetAt(world, pos.up())) {
                exits.add(Direction.UP);
            } else {
                addHorizontalDirections(world, pos, exits, entryDirection, dir -> true, true);
            }
        } else if (packetType == PacketType.DESCENDING) {
            // DESCENDING priority
            if (hasValidTargetAt(world, pos.down())) {
                exits.add(Direction.DOWN);
            } else {
                addHorizontalDirections(world, pos, exits, entryDirection, dir -> true, true);
            }
        } else {
            // Common packet - all directions (including vertical)
            addAllDirections(world, pos, exits, entryDirection, dir -> true, true);
        }

        return exits;
    }

    private boolean canPacketGoInDirection(PacketType packetType, Direction dir) {
        if (packetType == PacketType.ASCENDING)
            return dir != Direction.DOWN;
        if (packetType == PacketType.DESCENDING)
            return dir != Direction.UP;
        return true;
    }
}
