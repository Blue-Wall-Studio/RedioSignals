package org.BlueWallStudio.argest.wire.types;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.packet.Packet;
import org.BlueWallStudio.argest.packet.PacketType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Refactored LapisWireType - uses AbstractWireType utilities
 */
public class LapisWireType extends AbstractWireType {

    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isOf(Blocks.LAPIS_BLOCK);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, Packet packet) {
        return true; // Continue transfer
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
            Packet packet, Direction entryDirection) {
        List<Direction> exits = new ArrayList<>();
        PacketType packetType = packet.getPacketType();

        // Copy and sort local directions array by priority
        Direction[] directions = Direction.values();
        Arrays.sort(directions, (a, b) -> Integer.compare(
                getDirectionPriority(b, packetType, entryDirection),
                getDirectionPriority(a, packetType, entryDirection)));

        Direction entryOpposite = (entryDirection == null) ? null : entryDirection.getOpposite();

        for (Direction dir : directions) {
            // Don't go back
            if (entryOpposite != null && dir == entryOpposite)
                continue;

            // Save physical restrictions for packet types
            if (!canPacketGoInDirection(packetType, dir))
                continue;

            if (hasValidTargetAt(world, pos.offset(dir))) {
                exits.add(dir); // Lapis can have several exits
            }
        }

        return exits;
    }

    private boolean canPacketGoInDirection(PacketType packetType, Direction dir) {
        return switch (packetType) {
            case ASCENDING -> dir != Direction.DOWN; // ASCENDING cannot go downwards
            case DESCENDING -> dir != Direction.UP; // DESCENDING cannot go upwards
        };
    }

    private int getDirectionPriority(Direction dir, PacketType packetType, Direction entry) {
        // Don't go back
        if (entry != null && dir == entry.getOpposite()) {
            return -1;
        }

        return switch (packetType) {
            case ASCENDING -> {
                if (dir == Direction.UP)
                    yield 100;
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
                if (dir == Direction.DOWN)
                    yield 100;
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
