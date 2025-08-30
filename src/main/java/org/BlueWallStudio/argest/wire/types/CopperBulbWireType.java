package org.BlueWallStudio.argest.wire.types;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;

import java.util.*;

/**
 * Лампочка на медном проводе. Повторно использует логику меди.
 * Меняет состояние LIT каждый раз при получении пакета, если НЕ запитана редстоуном.
 */
public class CopperBulbWireType extends CopperWireType {

    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isOf(Blocks.COPPER_BULB);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, SignalPacket packet) {
        BlockState currentState = world.getBlockState(pos);

        // Проверяем, запитана ли лампа внешним редстоуном
        boolean isPowered = world.isReceivingRedstonePower(pos);

        if (!isPowered) {
            // Берём текущее значение LIT (предполагается, что BulbBlock имеет свойство LIT)
            boolean currentLit = currentState.get(net.minecraft.block.BulbBlock.LIT);
            world.setBlockState(pos, currentState.with(net.minecraft.block.BulbBlock.LIT, !currentLit));
        }

        // Продолжаем передачу как обычный провод
        return true;
    }

    // Не переопределяем getExitDirections — используем поведение CopperWireType
}
