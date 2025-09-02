package org.BlueWallStudio.argest.wireless.receiver;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Реестр беспроводных приемников
public class WirelessReceiverRegistry {
    private static final Map<Block, WirelessReceiver> receivers = new HashMap<>();

    public static void register(Block block, WirelessReceiver receiver) {
        receivers.put(block, receiver);
    }

    public static Optional<WirelessReceiver> getReceiver(BlockState state) {
        return Optional.ofNullable(receivers.get(state.getBlock()));
    }

    public static boolean isWirelessReceiver(World world, BlockPos pos) {
        return getReceiver(world.getBlockState(pos)).isPresent();
    }

    // Инициализация встроенных приемников
    public static void initializeDefaults() {
        // Медная решетка как приемник
        register(Blocks.COPPER_GRATE, new CopperGrateReceiver());

        // Можно добавить другие приемники в будущем
        // register(ModBlocks.CUSTOM_RECEIVER, new CustomReceiver());
    }
}
