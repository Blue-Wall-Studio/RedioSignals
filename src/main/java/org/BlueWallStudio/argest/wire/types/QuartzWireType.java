package org.BlueWallStudio.argest.wire.types;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.ModTags;
import org.BlueWallStudio.argest.packet.Packet;
import org.BlueWallStudio.argest.packet.PacketType;

import java.util.*;

public class QuartzWireType extends AbstractWireType {

    // Static map to track packets that have already moved this tick (anywhere, not just on quartz)
    private static final Map<String, Integer> packetMovedThisTick = new HashMap<>();

    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isIn(ModTags.QUARTZ_WIRES);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, Packet packet) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return true;
        }

        int currentTick = serverWorld.getServer().getTicks();
        String packetKey = getPacketKey(packet);

        // Check if this packet has already moved this tick (anywhere in the circuit)
        Integer lastMoveTick = packetMovedThisTick.get(packetKey);
        if (lastMoveTick != null && lastMoveTick == currentTick) {
            // Packet already moved this tick - stop it on quartz until next tick
            return false;
        }

        // Mark that this packet has moved this tick
        packetMovedThisTick.put(packetKey, currentTick);

        // Clean up old entries to prevent memory leaks
        cleanupOldEntries(currentTick);

        // If this is the first movement this tick and it's on quartz,
        // allow it to continue like normal wire
        return true;
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public List<Direction> getExitDirections(World world, BlockPos pos,
                                             Packet packet, Direction entryDirection) {
        List<Direction> exits = new ArrayList<>();
        PacketType packetType = packet.getPacketType();

        // Quartz behaves like normal wire when allowing movement
        switch (packetType) {
            case ASCENDING:
                // Priority upward, if not available - horizontally
                if (hasValidTargetAt(world, pos.up())) {
                    exits.add(Direction.UP);
                } else {
                    addHorizontalDirections(world, pos, exits, entryDirection, dir -> true, true);
                }
                break;

            case DESCENDING:
                // Priority downward, if not available - horizontally
                if (hasValidTargetAt(world, pos.down())) {
                    exits.add(Direction.DOWN);
                } else {
                    addHorizontalDirections(world, pos, exits, entryDirection, dir -> true, true);
                }
                break;

            default:
                // Normal behavior - to all available directions
                addAllDirections(world, pos, exits, entryDirection, dir -> true, true);
                break;
        }

        return exits;
    }

    /**
     * Creates a unique key for the packet based on its properties
     */
    private String getPacketKey(Packet packet) {
        return String.format("%d_%s_%s_%d",
                packet.getCreationTick(),
                packet.getCurrentPos().asLong(),
                packet.getCurrentDirection().getName(),
                Arrays.hashCode(packet.getPacketStrengths()));
    }

    /**
     * Cleans up old entries from the map to prevent memory leaks
     */
    private void cleanupOldEntries(int currentTick) {
        // Remove entries older than 10 ticks
        packetMovedThisTick.entrySet().removeIf(entry ->
                currentTick - entry.getValue() > 10);
    }
}
