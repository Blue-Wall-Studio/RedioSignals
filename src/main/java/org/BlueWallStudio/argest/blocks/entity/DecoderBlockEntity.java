package org.BlueWallStudio.argest.blocks.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.blocks.ModBlocks;
import org.BlueWallStudio.argest.signal.SignalPacket;

import java.util.HashMap;
import java.util.Map;

public class DecoderBlockEntity extends BlockEntity {
    private Map<Direction, Integer> outputPowers = new HashMap<>();
    private int ticksUntilReset = 0;
    private static final int SIGNAL_DURATION = 20; // 1 секунда

    public DecoderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.DECODER_BLOCK_ENTITY, pos, state);

        for (Direction dir : Direction.values()) {
            outputPowers.put(dir, 0);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, DecoderBlockEntity entity) {
        if (world.isClient) return;

        if (entity.ticksUntilReset > 0) {
            entity.ticksUntilReset--;
            if (entity.ticksUntilReset <= 0) {
                entity.resetOutputs();
                world.updateNeighbors(pos, state.getBlock());
            }
        }
    }

    public void receivePacket(SignalPacket packet, Direction entryDirection) {
        // Восстанавливаем исходные стороны сигналов
        int[] strengths = packet.getSignalStrengths();

        // Распределяем по сторонам (исключая вход)
        Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
        int index = 0;

        for (Direction dir : sides) {
            if (dir != entryDirection && index < strengths.length) {
                outputPowers.put(dir, strengths[index]);
                index++;
            }
        }

        ticksUntilReset = SIGNAL_DURATION;
        if (world != null) {
            world.updateNeighbors(pos, getCachedState().getBlock());
        }
    }

    private void resetOutputs() {
        for (Direction dir : Direction.values()) {
            outputPowers.put(dir, 0);
        }
    }

    public int getOutputPower(Direction direction) {
        return outputPowers.getOrDefault(direction, 0);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("ticksUntilReset", ticksUntilReset);
        for (Direction dir : Direction.values()) {
            nbt.putInt("power_" + dir.getName(), outputPowers.getOrDefault(dir, 0));
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        ticksUntilReset = nbt.getInt("ticksUntilReset");
        for (Direction dir : Direction.values()) {
            outputPowers.put(dir, nbt.getInt("power_" + dir.getName()));
        }
    }
}
