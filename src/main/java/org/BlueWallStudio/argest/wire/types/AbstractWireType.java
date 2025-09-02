package org.BlueWallStudio.argest.wire.types;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.wire.WireDetector;
import org.BlueWallStudio.argest.wire.WireType;

import java.util.List;
import java.util.function.Predicate;

/**
 * Абстрактный базовый класс, содержащий общие утилиты и безопасные реализации
 * для большинства типовых операций провода.
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
     * Есть ли провод (любой тип) в указанной позиции.
     */
    protected boolean hasWireAt(World world, BlockPos pos) {
        return WireDetector.isWire(world, pos);
    }

    /**
     * Есть ли провод ИЛИ декодер в указанной позиции.
     */
    protected boolean hasWireOrDecoderAt(World world, BlockPos pos) {
        return WireDetector.isWire(world, pos) || WireDetector.isDecoder(world, pos);
    }

    /**
     * Есть ли что-то, что может принять пакет (провод, декодер, беспроводные компоненты).
     */
    protected boolean hasValidTargetAt(World world, BlockPos pos) {
        return WireDetector.isWire(world, pos)
                || WireDetector.isDecoder(world, pos)
                || WireDetector.isWirelessReceiver(world, pos)
                || WireDetector.isWirelessTransmitter(world, pos);
    }

    /**
     * Безопасная реализация: добавляет первое горизонтальное направление по часовой,
     * начиная с next clockwise of entry (если entry горизонтален), с возможностью
     * дополнительной проверки через predicate (например, проверка canPacketGoInDirection).
     *
     * entry может быть null.
     */
    protected void addHorizontalDirections(World world, BlockPos pos,
                                           List<Direction> exits,
                                           Direction entry,
                                           Predicate<Direction> extraAllowed,
                                           boolean requireWireOrDecoder) {
        Direction[] horizontal = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

        int startIndex = 0;
        if (entry != null && entry.getAxis().isHorizontal()) {
            int idx = indexOf(horizontal, entry);
            startIndex = (idx + 1) % horizontal.length; // следующий по часовой от entry
        }

        Direction entryOpposite = (entry == null) ? null : entry.getOpposite();

        for (int i = 0; i < horizontal.length; i++) {
            Direction dir = horizontal[(startIndex + i) % horizontal.length];
            if (entryOpposite != null && dir == entryOpposite) continue;
            if (extraAllowed != null && !extraAllowed.test(dir)) continue;

            BlockPos target = pos.offset(dir);
            boolean ok = requireWireOrDecoder ? hasValidTargetAt(world, target) : hasWireAt(world, target);
            if (ok) {
                exits.add(dir);
                break; // берем первое доступное
            }
        }
    }

    /**
     * Добавляет все направления (включая вертикальные), с безопасной защитой от возврата назад.
     * Можно передать predicate для дополнительных условий (например, запретить направление).
     */
    protected void addAllDirections(World world, BlockPos pos,
                                    List<Direction> exits,
                                    Direction entry,
                                    Predicate<Direction> extraAllowed,
                                    boolean requireWireOrDecoder) {
        Direction entryOpposite = (entry == null) ? null : entry.getOpposite();

        for (Direction dir : Direction.values()) {
            if (entryOpposite != null && dir == entryOpposite) continue;
            if (extraAllowed != null && !extraAllowed.test(dir)) continue;

            BlockPos target = pos.offset(dir);
            boolean ok = requireWireOrDecoder ? hasValidTargetAt(world, target) : hasWireAt(world, target);
            if (ok) {
                exits.add(dir);
            }
        }
    }

    private int indexOf(Direction[] arr, Direction d) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == d) return i;
        return 0;
    }
}

