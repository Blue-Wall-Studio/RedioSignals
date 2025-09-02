package org.BlueWallStudio.argest.wire;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.blocks.DecoderBlock;
import org.BlueWallStudio.argest.wireless.receiver.WirelessReceiverRegistry;
import org.BlueWallStudio.argest.wireless.transmitter.WirelessTransmitterRegistry;

import java.util.Optional;

// Обновленный WireDetector с поддержкой беспроводных передатчиков
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
     * Определяет, является ли блок беспроводным приемником
     */
    public static boolean isWirelessReceiver(World world, BlockPos pos) {
        return WirelessReceiverRegistry.isWirelessReceiver(world, pos);
    }

    /**
     * Определяет, является ли блок беспроводным передатчиком
     */
    public static boolean isWirelessTransmitter(World world, BlockPos pos) {
        return WirelessTransmitterRegistry.isWirelessTransmitter(world, pos);
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
    public static boolean canTransmit(World world, BlockPos from, BlockPos to, Direction direction) {
        // Проверяем, есть ли провод в исходной позиции
        Optional<WireType> fromWire = getWireType(world, from);
        if (fromWire.isEmpty()) return false;

        // Проверяем, может ли провод передавать в данном направлении
        if (!fromWire.get().canExitTo(world, from, direction)) return false;

        // Проверяем целевую позицию
        // 1. Если это декодер - разрешаем
        if (isDecoder(world, to)) return true;

        // 2. Если это беспроводной приемник - разрешаем
        if (isWirelessReceiver(world, to)) return true;

        // 3. Если это беспроводной передатчик - разрешаем
        if (isWirelessTransmitter(world, to)) return true;

        // 4. Если это провод - проверяем совместимость
        Optional<WireType> toWire = getWireType(world, to);
        if (toWire.isPresent()) {
            return toWire.get().canEnterFrom(world, to, direction.getOpposite());
        }

        // Во всех остальных случаях - запрещаем
        return false;
    }
}
