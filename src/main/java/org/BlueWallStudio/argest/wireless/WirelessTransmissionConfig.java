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
public record WirelessTransmissionConfig(Direction transmissionDirection, int maxRange, boolean canPenetrate,
                                         Set<Block> blockingBlocks) {
    public WirelessTransmissionConfig(Direction transmissionDirection, int maxRange, boolean canPenetrate, Set<Block> blockingBlocks) {
        this.transmissionDirection = transmissionDirection;
        this.maxRange = maxRange;
        this.canPenetrate = canPenetrate;
        this.blockingBlocks = blockingBlocks != null ? blockingBlocks : Collections.emptySet();
    }

    public static WirelessTransmissionConfig chain(Direction direction) {
        // Конфигурация для цепи: 32 блока, проходит сквозь все кроме бедрока и обсидиана
        Set<Block> blocking = Set.of(Blocks.BEDROCK, Blocks.OBSIDIAN);
        return new WirelessTransmissionConfig(direction, 32, true, blocking);
    }
}
