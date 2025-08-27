package org.BlueWallStudio.argest.wire;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;

public interface WireType {
    /**
     * Проверяет, может ли этот тип провода работать с данным блоком
     */
    boolean canHandle(BlockState blockState);

    /**
     * Обрабатывает прохождение пакета через провод
     * @return true если пакет может пройти дальше, false если останавливается
     */
    boolean processPacket(World world, BlockPos pos, SignalPacket packet);

    /**
     * Получить приоритет обработки (больше = выше приоритет)
     */
    int getPriority();

    /**
     * Может ли пакет входить в этот провод с указанной стороны
     */
    boolean canEnterFrom(World world, BlockPos pos, net.minecraft.util.math.Direction from);

    /**
     * Может ли пакет выходить из этого провода в указанную сторону
     */
    boolean canExitTo(World world, BlockPos pos, net.minecraft.util.math.Direction to);

    /**
     * Получить возможные выходные направления для пакета
     */
    java.util.List<net.minecraft.util.math.Direction> getExitDirections(
            World world, BlockPos pos, SignalPacket packet,
            net.minecraft.util.math.Direction entryDirection
    );
}
