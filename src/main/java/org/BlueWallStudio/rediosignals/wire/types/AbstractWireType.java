package org.BlueWallStudio.rediosignals.wire.types;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.rediosignals.ModTags;
import org.BlueWallStudio.rediosignals.wire.WireDetector;
import org.BlueWallStudio.rediosignals.wire.WireType;

import java.util.List;
import java.util.function.Predicate;

/**
 * Abstract base class, containing general utilities and safe implementations
 * for most typical wire operations
 */
public abstract class AbstractWireType implements WireType {

    @Override
    public boolean canEnterFrom(World world, BlockPos pos, Direction from) {
        return true;
    }

    @Override
    public boolean canExitTo(World world, BlockPos pos, Direction to) {
        return true;
    }

    /**
     * Is there (any) wire in specified position?
     */
    protected boolean hasWireAt(World world, BlockPos pos) {
        return WireDetector.isWire(world, pos);
    }

    /**
     * Is there anything that can receive the package (wire/decoder/wireless
     * component) at specified position?
     */
    protected boolean hasValidTargetAt(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isIn(ModTags.ALL_WIRES)
                || WireDetector.isDecoder(world, pos)
                || state.isIn(ModTags.WIRELESS_TRANSMITTERS)
                || state.isIn(ModTags.WIRELESS_RECEIVERS);
    }

    /**
     * Checks whether the chain (iron transmitter) can accept a packet from the given direction
     */
    protected boolean canTransmitterAcceptFromDirection(World world, BlockPos chainPos, Direction fromDirection) {
        BlockState chainState = world.getBlockState(chainPos);

        // If this is not a chain - do not apply constraints
        if (!chainState.isIn(ModTags.IRON_TRANSMITTERS)) {
            return true;
        }

        // If the chain does not have an axis property - allow
        if (!chainState.contains(Properties.AXIS)) {
            return true;
        }

        Direction.Axis chainAxis = chainState.get(Properties.AXIS);

        // Direction must match the chain's axis
        return fromDirection.getAxis() == chainAxis;
    }

    /**
     * Safe implementation: add first horizontal direction clockwise, starting with
     * next clockwise entry (if entry is horizontal), with ability of additional
     * verification via predicate (i.e. the canPacketGoInDirection check)
     *
     * entry can be null.
     */
    protected void addHorizontalDirections(World world, BlockPos pos,
                                           List<Direction> exits,
                                           Direction entry,
                                           Predicate<Direction> extraAllowed,
                                           boolean requireWireOrDecoder) {
        addHorizontalDirectionsWithRotation(world, pos, exits, entry, extraAllowed, requireWireOrDecoder, true);
    }

    /**
     * Safe implementation: add first horizontal direction counter-clockwise, starting with
     * next counter-clockwise entry (if entry is horizontal)
     *
     * entry can be null.
     */
    protected void addHorizontalDirectionsCounterClockwise(World world, BlockPos pos,
                                                           List<Direction> exits,
                                                           Direction entry,
                                                           Predicate<Direction> extraAllowed,
                                                           boolean requireWireOrDecoder) {
        addHorizontalDirectionsWithRotation(world, pos, exits, entry, extraAllowed, requireWireOrDecoder, false);
    }

    /**
     * Internal method for horizontal direction addition with configurable rotation
     * @param clockwise true for clockwise rotation, false for counter-clockwise
     */
    private void addHorizontalDirectionsWithRotation(World world, BlockPos pos,
                                                     List<Direction> exits,
                                                     Direction entry,
                                                     Predicate<Direction> extraAllowed,
                                                     boolean requireWireOrDecoder,
                                                     boolean clockwise) {

        Direction[] horizontal = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

        int startIndex = 0;
        if (entry != null && entry.getAxis().isHorizontal()) {
            int idx = indexOf(horizontal, entry);
            if (clockwise) {
                startIndex = (idx + 1) % horizontal.length; // next clockwise from entry
            } else {
                startIndex = (idx - 1 + horizontal.length) % horizontal.length; // next counter-clockwise from entry
            }
        }

        Direction entryOpposite = (entry == null) ? null : entry.getOpposite();

        for (int i = 0; i < horizontal.length; i++) {
            int currentIndex;
            if (clockwise) {
                currentIndex = (startIndex + i) % horizontal.length;
            } else {
                currentIndex = (startIndex - i + horizontal.length) % horizontal.length;
            }

            Direction dir = horizontal[currentIndex];
            if (entryOpposite != null && dir == entryOpposite)
                continue;
            if (extraAllowed != null && !extraAllowed.test(dir))
                continue;

            BlockPos target = pos.offset(dir);
            boolean ok = requireWireOrDecoder ? hasValidTargetAt(world, target) : hasWireAt(world, target);

            // Additional check for chains: can the chain accept a packet from that direction
            if (ok && !canTransmitterAcceptFromDirection(world, target, dir)) {
                continue; // Skip this chain if it cannot accept a packet from that direction
            }

            if (ok) {
                exits.add(dir);
                break; // take first available
            }
        }
    }

    /**
     * Adds all directions (including vertical) with safety from going back
     * Can pass predicate for additional conditions (i.e. prohibit directions)
     */
    protected void addAllDirections(World world, BlockPos pos,
                                    List<Direction> exits,
                                    Direction entry,
                                    Predicate<Direction> extraAllowed,
                                    boolean requireWireOrDecoder) {
        Direction entryOpposite = (entry == null) ? null : entry.getOpposite();

        for (Direction dir : Direction.values()) {
            if (entryOpposite != null && dir == entryOpposite)
                continue;
            if (extraAllowed != null && !extraAllowed.test(dir))
                continue;

            BlockPos target = pos.offset(dir);
            boolean ok = requireWireOrDecoder ? hasValidTargetAt(world, target) : hasWireAt(world, target);

            if (ok) {
                // Check chains: if this is a chain, verify axis compatibility
                if (!canTransmitterAcceptFromDirection(world, target, dir)) {
                    continue; // Skip chain with incompatible axis
                }

                // Add suitable target
                exits.add(dir);
            }
        }
    }

    private int indexOf(Direction[] arr, Direction d) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i] == d)
                return i;
        return 0;
    }
}
