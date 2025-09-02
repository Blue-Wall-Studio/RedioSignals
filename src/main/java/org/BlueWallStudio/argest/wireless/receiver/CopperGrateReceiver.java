package org.BlueWallStudio.argest.wireless.receiver;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;
import org.BlueWallStudio.argest.signal.SignalType;

// Реализация приемника для медной решетки
public class CopperGrateReceiver implements WirelessReceiver {

    @Override
    public boolean canReceiveWireless(World world, BlockPos pos, SignalPacket packet) {
        BlockState state = world.getBlockState(pos);
        return state.isOf(Blocks.COPPER_GRATE); // Проверяем блок
    }

    @Override
    public SignalPacket processWirelessReception(World world, BlockPos pos, SignalPacket packet, Direction from) {
        // Меняем тип на нисходящий
        return new SignalPacket(
                packet.getSignalStrengths(),
                SignalType.DESCENDING, // Всегда меняем на нисходящий
                pos,
                from,
                (ServerWorld) world,
                packet.getCreationTick()
        );
    }

    @Override
    public int getReceiverPriority() {
        return 100; // Высокий приоритет
    }
}
