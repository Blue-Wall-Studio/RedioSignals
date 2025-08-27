package org.BlueWallStudio.argest.wire.types;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;
import org.BlueWallStudio.argest.wire.WireType;
import org.BlueWallStudio.argest.wire.WireRegistry;
import java.util.*;

public class GoldWireType implements WireType{
    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isOf(Blocks.GOLD_BLOCK);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, SignalPacket packet) {
        // Золото игнорирует тип пакета и отправляет прямо
        return true;
    }

    @Override
    public int getPriority() {
        return 90;
    }

    @Override
    public boolean canEnterFrom(World world, BlockPos pos, Direction from) {
        return true;
    }

    @Override
    public boolean canExitTo(World world, BlockPos pos, Direction to) {
        return true;
    }

    @Override
    public List<Direction> getExitDirections(World world, BlockPos pos,
                                             SignalPacket packet, Direction entryDirection) {
        List<Direction> exits = new ArrayList<>();

        // Золото просто идет прямо, игнорируя тип сигнала
        if (entryDirection != null) {
            Direction forward = entryDirection;
            if (hasWireAt(world, pos.offset(forward))) {
                exits.add(forward);
                return exits;
            }
        }

        // Если прямо нельзя, ищем любое доступное
        for (Direction dir : Direction.values()) {
            if (dir != entryDirection.getOpposite() && hasWireAt(world, pos.offset(dir))) {
                exits.add(dir);
                break;
            }
        }

        return exits;
    }

    private boolean hasWireAt(World world, BlockPos pos) {
        return WireRegistry.getWireType(world.getBlockState(pos)).isPresent();
    }
}
