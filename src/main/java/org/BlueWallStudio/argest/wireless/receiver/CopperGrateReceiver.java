package org.BlueWallStudio.argest.wireless.receiver;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;
import org.BlueWallStudio.argest.signal.SignalType;
import org.BlueWallStudio.argest.wire.WireDetector;

// Реализация приемника для медной решетки
public class CopperGrateReceiver implements WirelessReceiver {

    @Override
    public boolean canReceiveWireless(World world, BlockPos pos, SignalPacket packet) {
        return world.getBlockState(pos).isOf(Blocks.COPPER_GRATE);
    }

    @Override
    public SignalPacket processWirelessReception(World world, BlockPos pos, SignalPacket packet, Direction from) {
        // Сделаем пакет нисходящим
        int[] strengths = packet.getSignalStrengths();
        int creationTick = packet.getCreationTick();
        ServerWorld serverWorld = (ServerWorld) world;

        // 1) Предпочтительно: идти в ту же сторону, откуда пришёл пакет
        Direction preferred = from != null ? from : packet.getCurrentDirection();
        if (preferred != null) {
            BlockPos forward = pos.offset(preferred);
            if (WireDetector.isWire(world, forward) || WireDetector.isDecoder(world, forward)) {
                return new SignalPacket(strengths, SignalType.DESCENDING, forward, preferred, serverWorld, creationTick);
            }
        }

        // 2) Пытаемся найти любой соседний провод/декодер (на случай, если провод подключён не прямо вперед)
        for (Direction d : Direction.values()) {
            BlockPos np = pos.offset(d);
            if (WireDetector.isWire(world, np) || WireDetector.isDecoder(world, np)) {
                return new SignalPacket(strengths, SignalType.DESCENDING, np, d, serverWorld, creationTick);
            }
        }

        // 3) Если проводов рядом нет — уничтожаем пакет (возвращаем null)
        return null;
    }

    @Override
    public int getReceiverPriority() {
        return 100;
    }
}

