package org.BlueWallStudio.argest.wireless.transmitter;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Wireless transmitters registry
public class WirelessTransmitterRegistry {
    private static final Map<Block, WirelessTransmitter> transmitters = new HashMap<>();

    public static void register(Block block, WirelessTransmitter transmitter) {
        transmitters.put(block, transmitter);
    }

    public static Optional<WirelessTransmitter> getTransmitter(BlockState state) {
        return Optional.ofNullable(transmitters.get(state.getBlock()));
    }

    public static boolean isWirelessTransmitter(World world, BlockPos pos) {
        return getTransmitter(world.getBlockState(pos)).isPresent();
    }

    public static void initializeDefaults() {
        // Register chain as transmitter
        register(Blocks.CHAIN, new ChainTransmitter());

        // Add other transmitters in the future
        // register(ModBlocks.RADIO_ANTENNA, new AntennaTransmitter());
    }
}
