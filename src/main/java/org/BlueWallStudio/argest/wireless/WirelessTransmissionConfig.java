package org.BlueWallStudio.argest.wireless;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;

import java.util.Collections;
import java.util.Set;

/**
 * @param canPenetrate   может ли проходить через блоки
 * @param blockingBlocks какие блоки блокируют
 */ // Конфигурация беспроводной передачи
public record WirelessTransmissionConfig(
        Direction transmissionDirection,
        int maxRange,
        boolean canPenetrate,
        Set<Block> blockingBlocks,
        boolean requireReceiver
) {
    public WirelessTransmissionConfig(Direction transmissionDirection, int maxRange,
                                      boolean canPenetrate, Set<Block> blockingBlocks,
                                      boolean requireReceiver) {
        this.transmissionDirection = transmissionDirection;
        this.maxRange = maxRange;
        this.canPenetrate = canPenetrate;
        this.blockingBlocks = blockingBlocks != null ? blockingBlocks : Collections.emptySet();
        this.requireReceiver = requireReceiver;
    }

    // пример для цепи
    public static WirelessTransmissionConfig chain(Direction direction) {
        Set<Block> blocking = Set.of(Blocks.BEDROCK, Blocks.OBSIDIAN);
        return new WirelessTransmissionConfig(direction, 32, true, blocking, true); // requireReceiver = true
    }
}
