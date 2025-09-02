package org.BlueWallStudio.argest.wireless.receiver;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;

// Интерфейс для блоков, которые могут принимать беспроводные сигналы
public interface WirelessReceiver {
    /**
     * Проверяет, может ли этот блок принимать беспроводные сигналы
     */
    boolean canReceiveWireless(World world, BlockPos pos, SignalPacket packet);

    /**
     * Обрабатывает получение беспроводного сигнала
     * @return модифицированный пакет или null если пакет должен быть уничтожен
     */
    SignalPacket processWirelessReception(World world, BlockPos pos, SignalPacket packet, Direction from);

    /**
     * Приоритет приемника (больше = выше приоритет)
     */
    default int getReceiverPriority() {
        return 0;
    }
}
