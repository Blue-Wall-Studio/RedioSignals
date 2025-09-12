package org.BlueWallStudio.argest.wireless.transmitter;

import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.state.property.Properties;
import org.BlueWallStudio.argest.ModTags;
import org.BlueWallStudio.argest.packet.Packet;
import org.BlueWallStudio.argest.wireless.WirelessTransmissionConfig;

// Transmitter implementation for the chain (with axis check)
public class ChainTransmitter implements WirelessTransmitter {

    @Override
    public boolean canTransmit(World world, BlockPos pos, Packet packet) {
        BlockState state = world.getBlockState(pos);

        // Check if is chain
        if (!state.isIn(ModTags.IRON_TRANSMITTERS)) {
            return false;
        }

        // Check, that chain is connected to the wire and that chain axis is compatible
        // with wire direction
        return hasAdjacentWireWithMatchingAxis(world, pos, state);
    }

    @Override
    public Packet processWirelessTransmission(World world, BlockPos pos, Packet packet,
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

        // Queue packet creation in target position
        Packet newPacket = packet.withNewPosition(targetPos, config.transmissionDirection());

        if (newPacket == null) {
            return null;
        }

        return newPacket;
    }

    // Helper function for processWirelessTransmission
    private BlockPos findWirelessTarget(World world, BlockPos startPos, WirelessTransmissionConfig config,
                                        Packet ignoredPacket) {
        Direction dir = config.transmissionDirection();
        int maxRange = config.maxRange();
        boolean canPenetrate = config.canPenetrate();

        for (int i = 1; i <= maxRange; i++) {
            BlockPos checkPos = startPos.offset(dir, i);
            BlockState checkState = world.getBlockState(checkPos);

            // Use the config's isBlocked method instead of checking specific blocks
            if (config.isBlocked(checkState)) {
                break;
            }

            if (!canPenetrate && !checkState.isAir() &&
                    !checkState.isIn(ModTags.WIRELESS_RECEIVERS)) {
                break;
            }

            if (config.requireReceiver()) {
                if (checkState.isIn(ModTags.WIRELESS_RECEIVERS)) {
                    return checkPos;
                } else {
                    continue;
                }
            }

            if (checkState.isIn(ModTags.WIRELESS_RECEIVERS) || checkState.isIn(ModTags.ALL_WIRES)) {
                return checkPos;
            }
        }

        return null;
    }

    /**
     * Search for neighboring wire, considering chain axis:
     * direction from chain to neighbor block must have same axis, as the chain
     */
    private boolean hasAdjacentWireWithMatchingAxis(World world, BlockPos chainPos, BlockState chainState) {
        // Get chain axis (if for some reasons property is absent - allow all)
        Axis chainAxis = null;
        if (chainState.contains(Properties.AXIS)) {
            chainAxis = chainState.get(Properties.AXIS);
        }

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = chainPos.offset(dir);
            BlockState neighborState = world.getBlockState(neighbor);
            if (!neighborState.isIn(ModTags.ALL_WIRES))
                continue;

            // If axis is known, require direction axis to match with chain axis
            if (chainAxis != null) {
                if (dir.getAxis() == chainAxis) {
                    return true;
                } else {
                    // Wire found, but in goes by incompatible axis - ignore
                    continue;
                }
            } else {
                // If axis is not available - assume, that neighbor wire is enough
                return true;
            }
        }
        return false;
    }

    @Override
    public WirelessTransmissionConfig getTransmissionConfig(World world, BlockPos pos, Packet packet,
                                                            Direction entryDirection) {
        BlockState state = world.getBlockState(pos);

        // Проверяем совместимость оси цепи с направлением входа
        if (state.isIn(ModTags.IRON_TRANSMITTERS) && state.contains(Properties.AXIS)) {
            Axis chainAxis = state.get(Properties.AXIS);

            // КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: Если направление входа не совместимо с осью цепи,
            // возвращаем null, чтобы система попробовала другие трансмиттеры
            if (entryDirection != null && entryDirection.getAxis() != chainAxis) {
                return null;
            }
        }

        // Chain transmits in same direction that package is received from
        return WirelessTransmissionConfig.chain(entryDirection);
    }

    @Override
    public int getTransmitterPriority() {
        return 100;
    }

    /**
     * НОВЫЙ МЕТОД: Проверяет, может ли этот конкретный трансмиттер обработать пакет
     * с данным направлением входа. Это позволяет системе выбрать правильный трансмиттер
     * среди нескольких доступных.
     */
    public boolean canHandleEntryDirection(World world, BlockPos pos, Direction entryDirection) {
        BlockState state = world.getBlockState(pos);

        if (!state.isIn(ModTags.IRON_TRANSMITTERS)) {
            return false;
        }

        // Если у цепи есть ось, проверяем совместимость
        if (state.contains(Properties.AXIS)) {
            Axis chainAxis = state.get(Properties.AXIS);
            return entryDirection == null || entryDirection.getAxis() == chainAxis;
        }

        // Если оси нет, принимаем любое направление
        return true;
    }
}
