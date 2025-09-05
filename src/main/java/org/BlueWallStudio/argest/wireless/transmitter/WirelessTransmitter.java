package org.BlueWallStudio.argest.wireless.transmitter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;
import org.BlueWallStudio.argest.wireless.WirelessTransmissionConfig;

// Интерфейс для беспроводных передатчиков
public interface WirelessTransmitter {
    /**
     * Проверяет, может ли этот блок работать как беспроводной передатчик
     */
    boolean canTransmit(World world, BlockPos pos, SignalPacket packet);

    /**
     * Handles wireless signal transmission
     * 
     * @return modified packet or null if packet should be killed
     */
    SignalPacket processWirelessTransmission(World world, BlockPos pos, SignalPacket packet, Direction entryDirection);

    /**
     * Получает конфигурацию передачи для данного передатчика
     */
    WirelessTransmissionConfig getTransmissionConfig(World world, BlockPos pos, SignalPacket packet,
            Direction entryDirection);

    /**
     * Приоритет передатчика (больше = выше приоритет)
     */
    default int getTransmitterPriority() {
        return 0;
    }
}
