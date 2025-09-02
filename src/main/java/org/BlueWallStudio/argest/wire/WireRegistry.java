package org.BlueWallStudio.argest.wire;

import net.minecraft.block.BlockState;
import org.BlueWallStudio.argest.wire.types.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class WireRegistry {
    private static final List<WireType> wireTypes = new CopyOnWriteArrayList<>();

    public static void init() {
        register(new CopperWireType());
        register(new GoldWireType());
        register(new CopperBulbWireType());
        register(new LapisWireType());
    }

    public static void register(WireType wireType) {
        wireTypes.add(wireType);
        wireTypes.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
    }

    public static Optional<WireType> getWireType(BlockState blockState) {
        return wireTypes.stream()
                .filter(type -> type.canHandle(blockState))
                .findFirst();
    }

    public static List<WireType> getAllWireTypes() {
        return new ArrayList<>(wireTypes);
    }
}
