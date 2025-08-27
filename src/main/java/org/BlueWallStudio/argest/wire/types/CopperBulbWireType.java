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

public class CopperBulbWireType implements WireType{
    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isOf(Blocks.COPPER_BULB);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, SignalPacket packet) {
        // Переключаем состояние медной лампы
        BlockState currentState = world.getBlockState(pos);
        boolean currentLit = currentState.get(net.minecraft.block.BulbBlock.LIT);

        world.setBlockState(pos, currentState.with(
                net.minecraft.block.BulbBlock.LIT, !currentLit
        ));

        return true; // Продолжаем передачу
    }

    @Override
    public int getPriority() {
        return 80;
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
        // Работает как обычный провод после переключения состояния
        List<Direction> exits = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            if (dir != entryDirection.getOpposite() && hasWireAt(world, pos.offset(dir))) {
                exits.add(dir);
            }
        }

        return exits;
    }

    private boolean hasWireAt(World world, BlockPos pos) {
        return WireRegistry.getWireType(world.getBlockState(pos)).isPresent();
    }
}
