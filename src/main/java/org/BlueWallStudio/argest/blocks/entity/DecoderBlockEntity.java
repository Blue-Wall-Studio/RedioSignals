package org.BlueWallStudio.argest.blocks.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.blocks.DecoderBlock;
import org.BlueWallStudio.argest.blocks.ModBlocks;
import org.BlueWallStudio.argest.signal.SignalPacket;

import java.util.EnumMap;

public class DecoderBlockEntity extends BlockEntity {
    private final EnumMap<Direction, Integer> outputPowers = new EnumMap<>(Direction.class);

    private static final int SIGNAL_DURATION = 20; // тиков (1 секунда)
    private int ticksUntilReset = 0;

    public DecoderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.DECODER_BLOCK_ENTITY, pos, state);
        for (Direction d : Direction.values()) {
            outputPowers.put(d, 0);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, DecoderBlockEntity entity) {
        if (world.isClient) return;

        if (entity.ticksUntilReset > 0) {
            entity.ticksUntilReset--;
            if (entity.ticksUntilReset <= 0 && world instanceof ServerWorld serverWorld) {
                entity.resetOutputs(serverWorld);
            }
        }
    }

    /**
     * Принимаем пакет от энкодера.
     * Сигналы распределяются относительно направления блока (свойство FACING):
     * [лево, фронт, право].
     */
    public void receivePacket(SignalPacket packet, Direction entryDirection) {
        if (world == null || world.isClient) return;

        int[] strengths = packet.getSignalStrengths();
        if (strengths == null || strengths.length < 3) return;

        Direction facing = getCachedState().get(DecoderBlock.FACING);
        Direction left = facing.rotateYCounterclockwise();
        Direction right = facing.rotateYClockwise();

        // Сбрасываем всё
        for (Direction d : Direction.values()) {
            outputPowers.put(d, 0);
        }

        // Заполняем только горизонтальные стороны
        outputPowers.put(left, strengths[0]);
        outputPowers.put(facing, strengths[1]);
        outputPowers.put(right, strengths[2]);

        ticksUntilReset = SIGNAL_DURATION;

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.updateNeighbors(pos, getCachedState().getBlock());
        }
    }

    private void resetOutputs(ServerWorld world) {
        boolean hadAny = outputPowers.values().stream().anyMatch(v -> v != null && v > 0);
        for (Direction d : Direction.values()) {
            outputPowers.put(d, 0);
        }
        ticksUntilReset = 0;

        if (hadAny) {
            world.updateNeighbors(pos, getCachedState().getBlock());
        }
    }

    public int getOutputPower(Direction direction) {
        if (!direction.getAxis().isHorizontal()) return 0;
        return outputPowers.getOrDefault(direction, 0);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("ticksUntilReset", ticksUntilReset);
        NbtCompound outputs = new NbtCompound();
        for (Direction d : Direction.values()) {
            outputs.putInt(d.getName(), outputPowers.getOrDefault(d, 0));
        }
        nbt.put("outputPowers", outputs);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        ticksUntilReset = nbt.getInt("ticksUntilReset");
        if (nbt.contains("outputPowers")) {
            NbtCompound outputs = nbt.getCompound("outputPowers");
            for (Direction d : Direction.values()) {
                outputPowers.put(d, outputs.getInt(d.getName()));
            }
        } else {
            for (Direction d : Direction.values()) outputPowers.put(d, 0);
        }
    }
}