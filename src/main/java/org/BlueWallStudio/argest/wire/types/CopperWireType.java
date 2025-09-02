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
 * Медный провод — базовая реализация маршрутизации.
 */
public class CopperWireType extends AbstractWireType {

    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isOf(Blocks.COPPER_BLOCK);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, SignalPacket packet) {
        // Медь передаёт пакеты без побочных эффектов
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
            // Вверх в приоритете
            if (hasValidTargetAt(world, pos.up())) {
                exits.add(Direction.UP);
            } else {
                addHorizontalDirections(world, pos, exits, entryDirection, dir -> true, true);
            }
        } else if (signalType == SignalType.DESCENDING) {
            // Вниз в приоритете
            if (hasValidTargetAt(world, pos.down())) {
                exits.add(Direction.DOWN);
            } else {
                addHorizontalDirections(world, pos, exits, entryDirection, dir -> true, true);
            }
        } else {
            // Обычный сигнал — все направления (включая вертикальные)
            addAllDirections(world, pos, exits, entryDirection, dir -> true, true);
        }

        return exits;
    }
}
