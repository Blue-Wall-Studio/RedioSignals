package org.BlueWallStudio.argest.wireless.transmitter;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.ModTags;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// Wireless transmitters registry
public final class WirelessTransmitterRegistry {
    private WirelessTransmitterRegistry() {}

    // TagKey -> handler
    private static final Map<TagKey<Block>, WirelessTransmitter> TAG_TRANSMITTERS = new ConcurrentHashMap<>();

    // Register a handler for a tag (ModTags.*)
    public static void register(TagKey<Block> tag, WirelessTransmitter transmitter) {
        Objects.requireNonNull(tag);
        Objects.requireNonNull(transmitter);
        TAG_TRANSMITTERS.put(tag, transmitter);
    }

    public static Optional<WirelessTransmitter> getTransmitter(BlockState state) {
        if (state == null) return Optional.empty();

        // Search by tags
        for (Map.Entry<TagKey<Block>, WirelessTransmitter> e : TAG_TRANSMITTERS.entrySet()) {
            if (state.isIn(e.getKey())) {
                return Optional.of(e.getValue());
            }
        }

        return Optional.empty();
    }

    public static boolean isWirelessTransmitter(World world, BlockPos pos) {
        return getTransmitter(world.getBlockState(pos)).isPresent();
    }

    // Use tag instead of ModBlocks
    public static void initializeDefaults() {
        // Register chain as transmitter using tag
        register(ModTags.IRON_TRANSMITTERS, new ChainTransmitter());

        // Add other transmitters in the future
        // register(ModTags.ANTENNA_TRANSMITTERS, new AntennaTransmitter());
    }
}
