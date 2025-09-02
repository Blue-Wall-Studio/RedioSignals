package org.BlueWallStudio.argest.wire.types;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.signal.SignalManager;
import org.BlueWallStudio.argest.signal.SignalPacket;
import org.BlueWallStudio.argest.wireless.receiver.WirelessReceiver;
import org.BlueWallStudio.argest.wireless.receiver.WirelessReceiverRegistry;
import org.BlueWallStudio.argest.wireless.WirelessTransmissionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


// Провод для железной цепи (беспроводная передача)
public class ChainWireType extends AbstractWireType {
    private static final int DEFAULT_WIRELESS_RANGE = 32;

    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isOf(Blocks.CHAIN);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, SignalPacket packet) {
        // Железная цепь обрабатывает пакет для беспроводной передачи
        return true;
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public List<Direction> getExitDirections(World world, BlockPos pos, SignalPacket packet, Direction entryDirection) {
        List<Direction> exits = new ArrayList<>();

        // Определяем направление передачи (вперед от входа)
        if (entryDirection != null) {
            Direction transmissionDir = entryDirection;

            // Ищем приемник в указанном направлении
            WirelessTransmissionResult result = findWirelessReceiver(world, pos, transmissionDir, packet);

            if (result.hasReceiver()) {
                // Отправляем пакет напрямую в позицию приемника
                if (world instanceof ServerWorld serverWorld) {
                    SignalPacket wirelessPacket = packet.withNewPosition(result.getReceiverPos(), transmissionDir);
                    SignalManager.getInstance(serverWorld).sendPacket(wirelessPacket);
                }

                // Возвращаем пустой список - пакет уже отправлен
                return exits;
            }
        }

        // Если приемник не найден, пакет умирает
        return exits;
    }

    private WirelessTransmissionResult findWirelessReceiver(World world, BlockPos startPos, Direction direction, SignalPacket packet) {
        int range = getWirelessRange(world, startPos);

        for (int distance = 1; distance <= range; distance++) {
            BlockPos checkPos = startPos.offset(direction, distance);

            // Проверяем, если приемник в этой позиции
            Optional<WirelessReceiver> receiver = WirelessReceiverRegistry.getReceiver(world.getBlockState(checkPos));
            if (receiver.isPresent() && receiver.get().canReceiveWireless(world, checkPos, packet)) {
                return WirelessTransmissionResult.found(checkPos, receiver.get(), distance);
            }

            // Опционально: проверяем препятствия
            if (isBlocking(world, checkPos)) {
                break; // Прекращаем поиск если встретили препятствие
            }
        }

        return WirelessTransmissionResult.noReceiver();
    }

    private int getWirelessRange(World world, BlockPos pos) {
        // В будущем можно добавить блоки-усилители, которые увеличивают дальность
        return DEFAULT_WIRELESS_RANGE;
    }

    private boolean isBlocking(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        // Только бедрок и обсидиан блокируют передачу
        return state.isOf(Blocks.BEDROCK) || state.isOf(Blocks.OBSIDIAN);
    }
}
