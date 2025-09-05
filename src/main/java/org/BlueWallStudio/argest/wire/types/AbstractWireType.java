package org.BlueWallStudio.argest.wire.types;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.wire.WireDetector;
import org.BlueWallStudio.argest.wire.WireType;

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
     * Is there a wire OR a decoder in specified position?
     */
    protected boolean hasWireOrDecoderAt(World world, BlockPos pos) {
        return WireDetector.isWire(world, pos) || WireDetector.isDecoder(world, pos);
    }

    /**
     * Is there anything that can receive the package (wire/decoder/wireless
     * component) at specified position?
     */
    protected boolean hasValidTargetAt(World world, BlockPos pos) {
        return WireDetector.isWire(world, pos)
                || WireDetector.isDecoder(world, pos)
                || WireDetector.isWirelessReceiver(world, pos)
                || WireDetector.isWirelessTransmitter(world, pos);
    }

    /**
     * Safe implementation: add first horisontal direction clockwise, starting with
     * next clockwise entry (if entry is horisontal), with ability of additional
     * verification via predicate (i.e. the canPacketGoInDirection check)
     *
     * entry can be null.
     */
    protected void addHorizontalDirections(World world, BlockPos pos,
            List<Direction> exits,
            Direction entry,
            Predicate<Direction> extraAllowed,
            boolean requireWireOrDecoder) {
        Direction[] horizontal = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

        int startIndex = 0;
        if (entry != null && entry.getAxis().isHorizontal()) {
            int idx = indexOf(horizontal, entry);
            startIndex = (idx + 1) % horizontal.length; // next clockwise from entry
        }

        Direction entryOpposite = (entry == null) ? null : entry.getOpposite();

        for (int i = 0; i < horizontal.length; i++) {
            Direction dir = horizontal[(startIndex + i) % horizontal.length];
            if (entryOpposite != null && dir == entryOpposite)
                continue;
            if (extraAllowed != null && !extraAllowed.test(dir))
                continue;

            BlockPos target = pos.offset(dir);
            boolean ok = requireWireOrDecoder ? hasValidTargetAt(world, target) : hasWireAt(world, target);
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
