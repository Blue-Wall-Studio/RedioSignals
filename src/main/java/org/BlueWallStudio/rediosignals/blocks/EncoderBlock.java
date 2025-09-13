package org.BlueWallStudio.rediosignals.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.BlockView;
import net.minecraft.world.block.WireOrientation;
import org.BlueWallStudio.rediosignals.blocks.entity.EncoderBlockEntity;
import org.jetbrains.annotations.Nullable;

// EncoderBlock.java
public class EncoderBlock extends BlockWithEntity {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public EncoderBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = VoxelShapes.empty();

        double t = 0.1; // Wall thickness

        // bottom and top
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(t, 0, t, 1 - t, t, 1 - t));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(t, 1 - t, t, 1 - t, 1, 1 - t));

        // sides
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, t, 1, 1)); // left
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1 - t, 0, 0, 1, 1, 1)); // right
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 1, 1, t)); // front
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 1 - t, 1, 1, 1)); // back

        return shape;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(EncoderBlock::new);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Player-facing side: the block faces opposite the player's look direction
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EncoderBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
            BlockEntityType<T> type) {
        // server-only ticker; returns null on client
        return world.isClient ? null
                : BlockWithEntity.validateTicker(type, ModBlocks.ENCODER_BLOCK_ENTITY,
                        EncoderBlockEntity::tick);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos,
            Block sourceBlock, @Nullable WireOrientation sourcePos, boolean notify) {
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof EncoderBlockEntity encoder) {
                // mark for connection update next tick
                encoder.markConnectionsDirty();
            }
        }
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    }
}
