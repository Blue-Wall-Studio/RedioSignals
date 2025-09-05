package org.BlueWallStudio.argest.wire.types;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;
import org.BlueWallStudio.argest.signal.SignalType;

import java.util.*;

/**
 * Gold wire: prefers going "forward" (in entryDirection), otherwise uses
 * direction priority (UP/DOWN/N/E/S/W) and takes first available
 */
public class GoldWireType extends AbstractWireType {

    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isOf(Blocks.GOLD_BLOCK);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, SignalPacket packet) {
        return true; // Just passes further
    }

    @Override
    public int getPriority() {
        return 90;
    }

    @Override
    public List<Direction> getExitDirections(World world, BlockPos pos,
            SignalPacket packet, Direction entryDirection) {
        List<Direction> exits = new ArrayList<>();
        SignalType signalType = packet.getSignalType();

        // Firstly, try forward
        if (entryDirection != null) {
            Direction forward = entryDirection;
            if (canPacketGoInDirection(signalType, forward) && hasValidTargetAt(world, pos.offset(forward))) {
                exits.add(forward);
                return exits;
            }
        }

        // 2) Otherwise, collect all available directions and sort by priority
        Direction[] dirs = Direction.values();
        Arrays.sort(dirs, Comparator.comparingInt((Direction d) -> getDirectionPriority(d, signalType, entryDirection))
                .reversed());

        for (Direction dir : dirs) {
            // Don't go back
            if (entryDirection != null && dir == entryDirection.getOpposite())
                continue;
            if (!canPacketGoInDirection(signalType, dir))
                continue;

            if (hasValidTargetAt(world, pos.offset(dir))) {
                exits.add(dir);
                break; // Take only first available
            }
        }
        return exits;
    }

    private boolean canPacketGoInDirection(SignalType signalType, Direction dir) {
        if (signalType == SignalType.ASCENDING)
            return dir != Direction.DOWN;
        if (signalType == SignalType.DESCENDING)
            return dir != Direction.UP;
        return true;
    }

    private int getDirectionPriority(Direction dir, SignalType signalType, Direction entry) {
        // Low priority if backwards
        if (entry != null && dir == entry.getOpposite())
            return -1000;

        // Very high priority for verticals (if signal matches type)
        if (dir == Direction.UP && signalType == SignalType.ASCENDING)
            return 100;
        if (dir == Direction.DOWN && signalType == SignalType.DESCENDING)
            return 100;
        if (dir == Direction.UP || dir == Direction.DOWN)
            return 50;

        // Horizontal priorities: N > E > S > W (as in above example)
        if (dir.getAxis().isHorizontal()) {
            return switch (dir) {
                case NORTH -> 70;
                case EAST -> 60;
                case SOUTH -> 50;
                case WEST -> 40;
                default -> 30;
            };
        }
        return 0;
    }
}
