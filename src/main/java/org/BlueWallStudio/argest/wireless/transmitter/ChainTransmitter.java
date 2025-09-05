package org.BlueWallStudio.argest.wireless.transmitter;

import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalPacket;
import org.BlueWallStudio.argest.wire.WireDetector;
import org.BlueWallStudio.argest.wireless.WirelessTransmissionConfig;

// Реализация передатчика для цепи (с проверкой оси)
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction.Axis;

public class ChainTransmitter implements WirelessTransmitter {

    @Override
    public boolean canTransmit(World world, BlockPos pos, SignalPacket packet) {
        BlockState state = world.getBlockState(pos);

        // Проверяем, что это цепь
        if (!state.isOf(Blocks.CHAIN)) {
            return false;
        }

        // Проверяем, что цепь прикреплена к проводу И что ось цепи подходит под
        // направление к проводу
        return hasAdjacentWireWithMatchingAxis(world, pos, state);
    }

    @Override
    public SignalPacket processWirelessTransmission(World world, BlockPos pos, SignalPacket packet,
            Direction entryDirection) {

        if (!this.canTransmit(world, pos, packet)) {
            return null;
        }

        WirelessTransmissionConfig config = this.getTransmissionConfig(world, pos, packet, entryDirection);
        if (config == null) {
            return null;
        }

        // Find target position for wireless transmission
        BlockPos targetPos = findWirelessTarget(world, pos, config, packet);
        if (targetPos == null) {
            return null;
        }

        // Create new packet in target position
        SignalPacket newPacket = packet.withNewPosition(targetPos, config.transmissionDirection());

        if (newPacket == null) {
            return null;
        }

        return newPacket;
    }

    // Helper function for processWirelessTransmission
    private BlockPos findWirelessTarget(World world, BlockPos startPos, WirelessTransmissionConfig config,
            SignalPacket packet) {
        Direction dir = config.transmissionDirection();
        int maxRange = config.maxRange();
        boolean canPenetrate = config.canPenetrate();
        Set<Block> blockingBlocks = config.blockingBlocks();

        for (int i = 1; i <= maxRange; i++) {
            BlockPos checkPos = startPos.offset(dir, i);
            BlockState checkState = world.getBlockState(checkPos);
            Block checkBlock = checkState.getBlock();

            if (blockingBlocks.contains(checkBlock)) {
                break;
            }

            if (!canPenetrate && !checkState.isAir() &&
                    !WireDetector.isWirelessReceiver(world, checkPos)) {
                break;
            }

            if (config.requireReceiver()) {
                if (WireDetector.isWirelessReceiver(world, checkPos)) {
                    return checkPos;
                } else {
                    continue;
                }
            }

            if (WireDetector.isWirelessReceiver(world, checkPos) || WireDetector.isWire(world, checkPos)) {
                return checkPos;
            }
        }

        return null;
    }

    /**
     * Ищем соседний провод, но учитываем ось цепи:
     * направление от цепи к соседнему блоку должно иметь ту же ось, что и цепь.
     */
    private boolean hasAdjacentWireWithMatchingAxis(World world, BlockPos chainPos, BlockState chainState) {
        // Получаем ось цепи (если по каким-то причинам свойства нет — допускаем все)
        Axis chainAxis = null;
        if (chainState.contains(Properties.AXIS)) {
            chainAxis = chainState.get(Properties.AXIS);
        }

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = chainPos.offset(dir);
            if (!WireDetector.isWire(world, neighbor))
                continue;

            // Если ось известна, требуем совпадения оси направления с осью цепи
            if (chainAxis != null) {
                if (dir.getAxis() == chainAxis) {
                    return true;
                } else {
                    // найден провод, но он идёт по оси, несовместимой с цепью — игнорируем
                    continue;
                }
            } else {
                // если ось не доступна — просто считаем, что провод рядом достаточен
                return true;
            }
        }
        return false;
    }

    @Override
    public WirelessTransmissionConfig getTransmissionConfig(World world, BlockPos pos, SignalPacket packet,
            Direction entryDirection) {
        // Опционально: здесь можно ещё дополнительно сверить entryDirection.getAxis() с
        // осью цепи,
        // если хотите двойную страховку.
        BlockState state = world.getBlockState(pos);
        if (state.isOf(Blocks.CHAIN) && state.contains(Properties.AXIS)) {
            Axis chainAxis = state.get(Properties.AXIS);
            if (entryDirection != null && entryDirection.getAxis() != chainAxis) {
                // Если входящее направление не по оси цепи — отменяем передачу.
                return null; // или вернуть конфиг, означающий "не передавать"
            }
        }

        // Цепь передает в том же направлении, откуда пришел пакет
        return WirelessTransmissionConfig.chain(entryDirection);
    }

    @Override
    public int getTransmitterPriority() {
        return 100;
    }
}
