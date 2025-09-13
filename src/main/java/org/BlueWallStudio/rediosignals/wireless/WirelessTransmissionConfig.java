package org.BlueWallStudio.rediosignals.wireless;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Direction;
import org.BlueWallStudio.rediosignals.ModTags;

import java.util.Collections;
import java.util.Set;

/**
 * @param canPenetrate   can go through blocks?
 */

// Wireless transmission configuration
public record WirelessTransmissionConfig(
        Direction transmissionDirection,
        int maxRange,
        boolean canPenetrate,
        Set<Block> blockingBlocks,
        Set<TagKey<Block>> blockingTags,  // Add support for tags
        boolean requireReceiver) {

    public WirelessTransmissionConfig(Direction transmissionDirection, int maxRange,
                                      boolean canPenetrate, Set<Block> blockingBlocks, Set<TagKey<Block>> blockingTags,
                                      boolean requireReceiver) {
        this.transmissionDirection = transmissionDirection;
        this.maxRange = maxRange;
        this.canPenetrate = canPenetrate;
        this.blockingBlocks = blockingBlocks != null ? blockingBlocks : Collections.emptySet();
        this.blockingTags = blockingTags != null ? blockingTags : Collections.emptySet();
        this.requireReceiver = requireReceiver;
    }

    // Constructor for backward compatibility (without tags)
    public WirelessTransmissionConfig(Direction transmissionDirection, int maxRange,
                                      boolean canPenetrate, Set<Block> blockingBlocks, boolean requireReceiver) {
        this(transmissionDirection, maxRange, canPenetrate, blockingBlocks, null, requireReceiver);
    }

    // Method to check if the given block blocks transmission
    public boolean isBlocked(BlockState blockState) {
        Block block = blockState.getBlock();

        // Check specific blocks
        if (blockingBlocks.contains(block)) {
            return true;
        }

        // Check tags
        for (TagKey<Block> tag : blockingTags) {
            if (blockState.isIn(tag)) {
                return true;
            }
        }

        return false;
    }

    // Examples of factory methods

    // Chain example with tags
    public static WirelessTransmissionConfig chain(Direction direction) {
        Set<TagKey<Block>> blockingTags = Set.of(ModTags.WIRELESS_BLOCKING_BLOCKS);
        // Pass an empty set of specific blocks and pass blockingTags in the fifth position
        return new WirelessTransmissionConfig(direction, 32, true, Collections.emptySet(), blockingTags, true);
    }
}
