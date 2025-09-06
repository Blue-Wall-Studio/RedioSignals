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

    private static final int SIGNAL_DURATION = 10; // Changed from 20 to 2 ticks
    private int ticksUntilReset = 0;

    public DecoderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.DECODER_BLOCK_ENTITY, pos, state);
        for (Direction d : Direction.values()) {
            outputPowers.put(d, 0);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, DecoderBlockEntity entity) {
        if (world.isClient)
            return;

        if (entity.ticksUntilReset > 0) {
            entity.ticksUntilReset--;
            if (entity.ticksUntilReset <= 0 && world instanceof ServerWorld serverWorld) {
                entity.resetOutputs(serverWorld);
            }
        }
    }

    /**
     * getting packet from encoder
     * signal strengths are distributed relative to the block's direction (FACING
     * property):
     * [left, front, right]
     */
    public void receivePacket(SignalPacket packet, Direction entryDirection) {
        if (world == null || world.isClient)
            return;

        int[] strengths = packet.getSignalStrengths();
        if (strengths == null || strengths.length < 3)
            return;

        Direction facing = getCachedState().get(DecoderBlock.FACING);
        Direction left = facing.rotateYCounterclockwise();
        Direction right = facing.rotateYClockwise();

        // Store old powers to check what changed
        EnumMap<Direction, Integer> oldPowers = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            oldPowers.put(d, outputPowers.getOrDefault(d, 0));
        }

        // Reset everything
        for (Direction d : Direction.values()) {
            outputPowers.put(d, 0);
        }

        // Fill only horizontal directions
        outputPowers.put(left, strengths[0]);
        outputPowers.put(facing, strengths[1]);
        outputPowers.put(right, strengths[2]);

        ticksUntilReset = SIGNAL_DURATION;

        if (world instanceof ServerWorld serverWorld) {
            // Update neighbors and trigger block updates like redstone does
            updateNeighborsAndTriggerUpdates(serverWorld, oldPowers);
        }
    }

    private void resetOutputs(ServerWorld world) {
        // Store old powers before resetting
        EnumMap<Direction, Integer> oldPowers = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            oldPowers.put(d, outputPowers.getOrDefault(d, 0));
        }

        boolean hadAnyPower = outputPowers.values().stream().anyMatch(v -> v != null && v > 0);

        for (Direction d : Direction.values()) {
            outputPowers.put(d, 0);
        }
        ticksUntilReset = 0;

        if (hadAnyPower) {
            // Update neighbors and trigger block updates when power is removed
            updateNeighborsAndTriggerUpdates(world, oldPowers);
        }
    }

    /**
     * Update neighbors and trigger block updates like redstone does
     */
    private void updateNeighborsAndTriggerUpdates(ServerWorld world, EnumMap<Direction, Integer> oldPowers) {
        // First update the neighbors of this block
        world.updateNeighbors(pos, getCachedState().getBlock());

        // Then update each neighbor block that had power changes
        for (Direction dir : Direction.values()) {
            if (!dir.getAxis().isHorizontal()) continue; // Only horizontal outputs

            int oldPower = oldPowers.getOrDefault(dir, 0);
            int newPower = outputPowers.getOrDefault(dir, 0);

            // If power level changed, update the neighbor in that direction
            if (oldPower != newPower) {
                BlockPos neighborPos = pos.offset(dir);
                BlockState neighborState = world.getBlockState(neighborPos);

                // Trigger neighbor update on the block receiving the signal
                world.updateNeighbors(neighborPos, neighborState.getBlock());

                // Also schedule a block event for immediate response (like pistons do)
                world.scheduleBlockTick(neighborPos, neighborState.getBlock(), 0);

                // Trigger comparator updates if needed
                world.updateComparators(neighborPos, neighborState.getBlock());
            }
        }

        // Mark block entity as dirty for save
        markDirty();
    }

    public int getOutputPower(Direction direction) {
        if (!direction.getAxis().isHorizontal())
            return 0;
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
            for (Direction d : Direction.values())
                outputPowers.put(d, 0);
        }
    }
}
