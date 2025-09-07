package org.BlueWallStudio.argest.blocks.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.blocks.EncoderBlock;
import org.BlueWallStudio.argest.blocks.ModBlocks;
import org.BlueWallStudio.argest.packet.PacketManager;
import org.BlueWallStudio.argest.packet.Packet;
import org.BlueWallStudio.argest.packet.PacketType;
import org.BlueWallStudio.argest.wire.WireDetector;

import java.util.EnumMap;
import java.util.EnumSet;

// EncoderBlockEntity.java
public class EncoderBlockEntity extends BlockEntity {
    // Use EnumMap/EnumSet for Direction-keyed collections (fast + compact)
    private final EnumMap<Direction, Integer> inputPowers = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, ConnectionType> connections = new EnumMap<>(Direction.class);
    private final EnumSet<Direction> outputDirections = EnumSet.noneOf(Direction.class);

    private static final int CONNECTION_UPDATE_INTERVAL = 20;
    private static final int INPUT_PROCESS_INTERVAL = 10;

    private int tickCounter = 0;
    private boolean needsConnectionUpdate = true;

    public EncoderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ENCODER_BLOCK_ENTITY, pos, state);
        initializeConnections();
    }

    private void initializeConnections() {
        for (Direction dir : Direction.values()) {
            inputPowers.put(dir, 0);
            connections.put(dir, ConnectionType.NONE);
        }
    }

    /**
     * Server tick entrypoint.
     */
    public static void tick(World world, BlockPos ignoredPos, BlockState ignoredState, EncoderBlockEntity entity) {
        if (world.isClient)
            return;
        entity.tickCounter++;

        // Connections update periodically or when flagged dirty
        if (entity.needsConnectionUpdate || entity.tickCounter % CONNECTION_UPDATE_INTERVAL == 0) {
            entity.updateConnections((ServerWorld) world);
            entity.needsConnectionUpdate = false;
        }

        // Process inputs + try to transmit every INPUT_PROCESS_INTERVAL ticks
        if (entity.tickCounter % INPUT_PROCESS_INTERVAL == 0) {
            entity.updateInputs((ServerWorld) world);
            entity.tryTransmitPacket((ServerWorld) world);
        }
    }

    /**
     * Mark connections dirty — will be recomputed on next tick or periodic update.
     */
    public void markConnectionsDirty() {
        this.needsConnectionUpdate = true;
    }

    /**
     * Examine neighbors and update connection types & output directions.
     */
    private void updateConnections(ServerWorld world) {
        outputDirections.clear();

        for (Direction dir : Direction.values()) {
            BlockPos adjacentPos = pos.offset(dir);

            // Check for wire above, decoders, or redstone from neighbour
            if (dir == Direction.UP && WireDetector.isWire(world, adjacentPos)) {
                connections.put(dir, ConnectionType.WIRE_OUTPUT);
                outputDirections.add(dir);
            } else if (WireDetector.isDecoder(world, adjacentPos)) {
                connections.put(dir, ConnectionType.DECODER_OUTPUT);
                outputDirections.add(dir);
            } else if (world.getEmittedRedstonePower(adjacentPos, dir) > 0) {
                connections.put(dir, ConnectionType.REDSTONE_INPUT);
            } else {
                connections.put(dir, ConnectionType.NONE);
            }
        }
    }

    /**
     * Read redstone power levels from neighbor blocks for all relevant sides.
     */
    private void updateInputs(ServerWorld world) {
        for (Direction dir : Direction.values()) {
            ConnectionType type = connections.getOrDefault(dir, ConnectionType.NONE);

            // We consider it an input if it's explicitly a redstone input,
            // or if it's NONE and not an output direction (i.e., free side).
            if (type == ConnectionType.REDSTONE_INPUT
                    || (type == ConnectionType.NONE && !outputDirections.contains(dir))) {
                int power = world.getEmittedRedstonePower(pos.offset(dir), dir);
                inputPowers.put(dir, power);
            }
        }
    }

    /**
     * Determine activation and send packets to all outputs.
     */
    private void tryTransmitPacket(ServerWorld world) {
        if (outputDirections.isEmpty())
            return;

        Direction facing = getCachedState().get(EncoderBlock.FACING);
        Direction activationDir = facing.getOpposite();

        // If activation side has no power, bail.
        if (inputPowers.getOrDefault(activationDir, 0) <= 0)
            return;

        // Collect strengths in explicit order: [left, front, right] relative to facing.
        int[] strengths = getThreeInputStrengths(facing);

        // If all three are zero -> nothing to send.
        if (strengths[0] == 0 && strengths[1] == 0 && strengths[2] == 0)
            return;

        // Currently simplified: always ascending packet
        PacketType packetType = PacketType.ASCENDING;

        // Send packet to each output direction
        for (Direction outputDir : outputDirections) {
            BlockPos outputPos = pos.offset(outputDir);
            Packet packet = new Packet(strengths, packetType, outputPos, outputDir, world);
            PacketManager.sendPacket(world, packet);
        }
    }

    /**
     * Returns [left, front, right] relative to the block facing.
     */
    private int[] getThreeInputStrengths(Direction facing) {
        Direction front = facing;
        Direction left = facing.rotateYCounterclockwise();
        Direction right = facing.rotateYClockwise();

        return new int[] {
                inputPowers.getOrDefault(left, 0),
                inputPowers.getOrDefault(front, 0),
                inputPowers.getOrDefault(right, 0)
        };
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        NbtCompound connectionsNbt = new NbtCompound();
        for (Direction dir : Direction.values()) {
            connectionsNbt.putString(dir.getName(), connections.getOrDefault(dir, ConnectionType.NONE).name());
        }
        nbt.put("connections", connectionsNbt);

        NbtCompound inputsNbt = new NbtCompound();
        for (Direction dir : Direction.values()) {
            inputsNbt.putInt(dir.getName(), inputPowers.getOrDefault(dir, 0));
        }
        nbt.put("inputs", inputsNbt);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        if (nbt.contains("connections")) {
            NbtCompound connectionsNbt = nbt.getCompound("connections");
            for (Direction dir : Direction.values()) {
                String typeName = connectionsNbt.getString(dir.getName());
                try {
                    connections.put(dir, ConnectionType.valueOf(typeName));
                } catch (IllegalArgumentException e) {
                    connections.put(dir, ConnectionType.NONE);
                }
            }
        }

        if (nbt.contains("inputs")) {
            NbtCompound inputsNbt = nbt.getCompound("inputs");
            for (Direction dir : Direction.values()) {
                inputPowers.put(dir, inputsNbt.getInt(dir.getName()));
            }
        }
    }

    private enum ConnectionType {
        NONE,
        REDSTONE_INPUT,
        WIRE_OUTPUT,
        DECODER_OUTPUT
    }
}
