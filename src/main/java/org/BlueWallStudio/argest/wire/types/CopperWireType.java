package org.BlueWallStudio.argest.wire.types;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;
import org.BlueWallStudio.argest.signal.SignalType;
import org.BlueWallStudio.argest.wire.WireRegistry;
import org.BlueWallStudio.argest.wire.WireType;
import java.util.*;

public class CopperWireType implements WireType{
    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isOf(Blocks.COPPER_BLOCK);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, SignalPacket packet) {
        // Медный провод передает пакеты с учетом их типа
        return true;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean canEnterFrom(World world, BlockPos pos, Direction from) {
        return true; // Медь принимает со всех сторон
    }

    @Override
    public boolean canExitTo(World world, BlockPos pos, Direction to) {
        return true;
    }

    @Override
    public List<Direction> getExitDirections(World world, BlockPos pos,
                                             SignalPacket packet, Direction entryDirection) {
        List<Direction> exits = new ArrayList<>();
        SignalType signalType = packet.getSignalType();

        // Логика выбора направления в зависимости от типа сигнала
        if (signalType == SignalType.ASCENDING) {
            // Приоритет вверх, потом горизонтально
            if (hasWireAt(world, pos.up())) {
                exits.add(Direction.UP);
            } else {
                addHorizontalDirections(world, pos, exits, entryDirection);
            }
        } else if (signalType == SignalType.DESCENDING) {
            // Приоритет вниз, потом горизонтально
            if (hasWireAt(world, pos.down())) {
                exits.add(Direction.DOWN);
            } else {
                addHorizontalDirections(world, pos, exits, entryDirection);
            }
        } else {
            // Обычный сигнал - прямо или поворот по часовой
            addAllDirections(world, pos, exits, entryDirection);
        }

        return exits;
    }

    private boolean hasWireAt(World world, BlockPos pos) {
        return WireRegistry.getWireType(world.getBlockState(pos)).isPresent();
    }

    private void addHorizontalDirections(World world, BlockPos pos,
                                         List<Direction> exits, Direction entry) {
        Direction[] horizontal = {Direction.NORTH, Direction.EAST,
                Direction.SOUTH, Direction.WEST};

        // Поворот по часовой стрелке с приоритетом севера
        int startIndex = entry == null ? 0 : getNextClockwiseIndex(entry);

        for (int i = 0; i < 4; i++) {
            Direction dir = horizontal[(startIndex + i) % 4];
            if (dir != entry.getOpposite() && hasWireAt(world, pos.offset(dir))) {
                exits.add(dir);
                break; // Берем первое доступное
            }
        }
    }

    private void addAllDirections(World world, BlockPos pos,
                                  List<Direction> exits, Direction entry) {
        for (Direction dir : Direction.values()) {
            if (dir != entry.getOpposite() && hasWireAt(world, pos.offset(dir))) {
                exits.add(dir);
            }
        }
    }

    private int getNextClockwiseIndex(Direction from) {
        return switch (from) {
            case NORTH -> 1; // Start from EAST
            case EAST -> 2;  // Start from SOUTH
            case SOUTH -> 3; // Start from WEST
            case WEST -> 0;  // Start from NORTH
            default -> 0;
        };
    }
}
