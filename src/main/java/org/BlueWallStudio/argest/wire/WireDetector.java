package org.BlueWallStudio.argest.wire;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.blocks.DecoderBlock;

import java.util.Optional;

public class WireDetector {
    /**
     * Определяет, является ли блок проводом
     */
    public static boolean isWire(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return WireRegistry.getWireType(state).isPresent();
    }

    /**
     * Определяет, является ли блок декодером (терминальным элементом)
     */
    public static boolean isDecoder(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof DecoderBlock;
    }

    /**
     * Получает тип провода в указанной позиции
     */
    public static Optional<WireType> getWireType(World world, BlockPos pos) {
        return WireRegistry.getWireType(world.getBlockState(pos));
    }

    /**
     * Проверяет, может ли пакет пройти между двумя позициями
     */
    public static boolean canTransmit(World world, BlockPos from, BlockPos to, net.minecraft.util.math.Direction direction) {
        Optional<WireType> fromWire = getWireType(world, from);
        Optional<WireType> toWire = getWireType(world, to);

        if (fromWire.isEmpty()) return false;

        // Если целевая позиция - декодер, разрешаем передачу
        if (isDecoder(world, to)) return true;

        // Если целевая позиция - провод, проверяем совместимость
        if (toWire.isPresent()) {
            return fromWire.get().canExitTo(world, from, direction) &&
                    toWire.get().canEnterFrom(world, to, direction.getOpposite());
        }

        return false;
    }
}
