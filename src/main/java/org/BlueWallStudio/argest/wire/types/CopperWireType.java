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
 * Copper wire - basic routing implementation
 */
public class CopperWireType extends AbstractWireType {

    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isOf(Blocks.COPPER_BLOCK);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, SignalPacket packet) {
        // Copper transmits packages without side effects
        return true;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public List<Direction> getExitDirections(World world, BlockPos pos,
            SignalPacket packet, Direction entryDirection) {
        List<Direction> exits = new ArrayList<>();
        SignalType signalType = packet.getSignalType();

        if (signalType == SignalType.ASCENDING) {
            // ASCENDING priority
            if (hasValidTargetAt(world, pos.up())) {
                exits.add(Direction.UP);
            } else {
                addHorizontalDirections(world, pos, exits, entryDirection, dir -> true, true);
            }
        } else if (signalType == SignalType.DESCENDING) {
            // DESCENDING priority
            if (hasValidTargetAt(world, pos.down())) {
                exits.add(Direction.DOWN);
            } else {
                addHorizontalDirections(world, pos, exits, entryDirection, dir -> true, true);
            }
        } else {
            // Common signal - all directions (including vertical)
            addAllDirections(world, pos, exits, entryDirection, dir -> true, true);
        }

        return exits;
    }
}
