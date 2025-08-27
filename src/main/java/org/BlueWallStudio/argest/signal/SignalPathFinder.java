package org.BlueWallStudio.argest.signal;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.BlueWallStudio.argest.wire.WireRegistry;
import org.BlueWallStudio.argest.wire.WireType;

import java.util.*;

public class SignalPathFinder {
    private final ServerWorld world;
    private final Set<BlockPos> visited = new HashSet<>();

    public SignalPathFinder(ServerWorld world) {
        this.world = world;
    }

    public List<Direction> findExitDirections(BlockPos pos, SignalPacket packet, Direction entryDirection) {
        Optional<WireType> wireType = WireRegistry.getWireType(world.getBlockState(pos));
        if (wireType.isEmpty()) {
            return Collections.emptyList();
        }

        return wireType.get().getExitDirections(world, pos, packet, entryDirection);
    }

    /**
     * Поиск пути до ближайшего декодера
     */
    public Optional<List<BlockPos>> findPathToDecoder(BlockPos start, SignalType signalType) {
        visited.clear();
        Queue<PathNode> queue = new ArrayDeque<>();
        queue.offer(new PathNode(start, null, new ArrayList<>()));

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            BlockPos pos = current.pos;

            if (visited.contains(pos)) continue;
            visited.add(pos);

            // Проверяем, является ли текущая позиция декодером
            if (isDecoder(pos)) {
                return Optional.of(current.path);
            }

            // Ищем соседние провода
            for (Direction dir : getDirectionsByPriority(signalType, current.entryDirection)) {
                BlockPos nextPos = pos.offset(dir);
                if (visited.contains(nextPos)) continue;

                if (isWire(nextPos) || isDecoder(nextPos)) {
                    List<BlockPos> newPath = new ArrayList<>(current.path);
                    newPath.add(nextPos);
                    queue.offer(new PathNode(nextPos, dir, newPath));
                }
            }
        }

        return Optional.empty();
    }

    private boolean isWire(BlockPos pos) {
        return WireRegistry.getWireType(world.getBlockState(pos)).isPresent();
    }

    private boolean isDecoder(BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof org.BlueWallStudio.argest.blocks.DecoderBlock;
    }

    private Direction[] getDirectionsByPriority(SignalType signalType, Direction entryDirection) {
        Direction[] directions = Direction.values();

        // Сортируем направления по приоритету в зависимости от типа сигнала
        Arrays.sort(directions, (a, b) -> {
            int priorityA = getDirectionPriority(a, signalType, entryDirection);
            int priorityB = getDirectionPriority(b, signalType, entryDirection);
            return Integer.compare(priorityB, priorityA); // Больший приоритет первым
        });

        return directions;
    }

    private int getDirectionPriority(Direction dir, SignalType signalType, Direction entry) {
        // Не идем назад
        if (entry != null && dir == entry.getOpposite()) {
            return -1;
        }

        return switch (signalType) {
            case ASCENDING -> {
                if (dir == Direction.UP) yield 100;
                if (dir.getAxis().isHorizontal()) yield 50;
                yield 0;
            }
            case DESCENDING -> {
                if (dir == Direction.DOWN) yield 100;
                if (dir.getAxis().isHorizontal()) yield 50;
                yield 0;
            }
            case NORMAL -> {
                // Приоритет по часовой стрелке с севера
                yield switch (dir) {
                    case NORTH -> 90;
                    case EAST -> 80;
                    case SOUTH -> 70;
                    case WEST -> 60;
                    case UP -> 40;
                    case DOWN -> 20;
                };
            }
        };
    }

    private static class PathNode {
        final BlockPos pos;
        final Direction entryDirection;
        final List<BlockPos> path;

        PathNode(BlockPos pos, Direction entryDirection, List<BlockPos> path) {
            this.pos = pos;
            this.entryDirection = entryDirection;
            this.path = path;
        }
    }
}
