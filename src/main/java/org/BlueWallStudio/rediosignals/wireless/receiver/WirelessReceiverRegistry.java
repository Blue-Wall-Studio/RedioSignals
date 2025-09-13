package org.BlueWallStudio.rediosignals.wireless.receiver;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.BlueWallStudio.rediosignals.ModTags;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class WirelessReceiverRegistry {
    private WirelessReceiverRegistry() {}

    // TagKey -> handler
    private static final Map<TagKey<Block>, WirelessReceiver> TAG_RECEIVERS = new ConcurrentHashMap<>();

    // Register a handler for a tag (ModTags.*)
    public static void register(TagKey<Block> tag, WirelessReceiver receiver) {
        Objects.requireNonNull(tag);
        Objects.requireNonNull(receiver);
        TAG_RECEIVERS.put(tag, receiver);
    }

    public static Optional<WirelessReceiver> getReceiver(BlockState state) {
        if (state == null) return Optional.empty();

        // Search by tags
        for (Map.Entry<TagKey<Block>, WirelessReceiver> e : TAG_RECEIVERS.entrySet()) {
            if (state.isIn(e.getKey())) {
                return Optional.of(e.getValue());
            }
        }

        return Optional.empty();
    }

    public static boolean isWirelessReceiver(World world, BlockPos pos) {
        return getReceiver(world.getBlockState(pos)).isPresent();
    }

    // Use tag instead of ModBlocks
    public static void initializeDefaults() {
        // Register a handler for the tag (in resources/data/.../tags/blocks/...)
        register(ModTags.COPPER_GRATE_RECEIVERS, new CopperGrateReceiver());

        // You can also register a general handler for all wireless receivers:
        // register(ModTags.WIRELESS_RECEIVERS, new GenericTaggedWirelessReceiver());
    }
}
