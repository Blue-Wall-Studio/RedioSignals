package org.BlueWallStudio.argest.wire.types;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;
import org.BlueWallStudio.argest.signal.SignalType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Refactored LapisWireType — использует AbstractWireType утилиты.
 */
public class LapisWireType extends AbstractWireType {

    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isOf(Blocks.LAPIS_BLOCK);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, SignalPacket packet) {
        return true; // Продолжаем передачу
    }

    @Override
    public int getPriority() {
        return 75;
    }

    @Override
    public boolean canEnterFrom(World world, BlockPos pos, Direction from) {
        return super.canEnterFrom(world, pos, from);
    }

    @Override
    public boolean canExitTo(World world, BlockPos pos, Direction to) {
        return super.canExitTo(world, pos, to);
    }

    @Override
    public List<Direction> getExitDirections(World world, BlockPos pos,
                                             SignalPacket packet, Direction entryDirection) {
        List<Direction> exits = new ArrayList<>();
        SignalType signalType = packet.getSignalType();

        // Копируем и сортируем локальный массив направлений по приоритету
        Direction[] directions = Direction.values();
        Arrays.sort(directions, (a, b) -> Integer.compare(
                getDirectionPriority(b, signalType, entryDirection),
                getDirectionPriority(a, signalType, entryDirection)
        ));

        Direction entryOpposite = (entryDirection == null) ? null : entryDirection.getOpposite();

        for (Direction dir : directions) {
            // Не идём назад
            if (entryOpposite != null && dir == entryOpposite) continue;

            // Проверяем физические ограничения для типов пакетов
            if (!canPacketGoInDirection(signalType, dir)) continue;

            if (hasValidTargetAt(world, pos.offset(dir))) {
                exits.add(dir); // У лазурита может быть несколько выходов
            }
        }

        return exits;
    }

    private boolean canPacketGoInDirection(SignalType signalType, Direction dir) {
        return switch (signalType) {
            case ASCENDING -> dir != Direction.DOWN;  // Восходящий не может идти вниз
            case DESCENDING -> dir != Direction.UP;   // Нисходящий не может идти вверх
        };
    }

    private int getDirectionPriority(Direction dir, SignalType signalType, Direction entry) {
        // Не идём назад
        if (entry != null && dir == entry.getOpposite()) {
            return -1;
        }

        return switch (signalType) {
            case ASCENDING -> {
                if (dir == Direction.UP) yield 100;
                if (dir.getAxis().isHorizontal()) {
                    yield switch (dir) {
                        case NORTH -> 70;
                        case EAST -> 60;
                        case SOUTH -> 50;
                        case WEST -> 40;
                        default -> 30;
                    };
                }
                yield 0;
            }
            case DESCENDING -> {
                if (dir == Direction.DOWN) yield 100;
                if (dir.getAxis().isHorizontal()) {
                    yield switch (dir) {
                        case NORTH -> 70;
                        case EAST -> 60;
                        case SOUTH -> 50;
                        case WEST -> 40;
                        default -> 30;
                    };
                }
                yield 0;
            }
        };
    }
}
