package org.BlueWallStudio.rediosignals.wireless.transmitter;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.BlueWallStudio.rediosignals.ModTags;

import java.util.*;
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


    /**
     * НОВЫЙ МЕТОД: Возвращает все трансмиттеры, которые могут обрабатывать данный блок.
     * Это позволяет перебирать несколько трансмиттеров, если блок соответствует нескольким тегам.
     */
    public static List<WirelessTransmitter> getAllTransmitters(BlockState state) {
        if (state == null) return List.of();

        List<WirelessTransmitter> transmitters = new ArrayList<>();

        // Ищем все теги, которым соответствует блок
        for (Map.Entry<TagKey<Block>, WirelessTransmitter> entry : TAG_TRANSMITTERS.entrySet()) {
            if (state.isIn(entry.getKey())) {
                transmitters.add(entry.getValue());
            }
        }

        return transmitters;
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
