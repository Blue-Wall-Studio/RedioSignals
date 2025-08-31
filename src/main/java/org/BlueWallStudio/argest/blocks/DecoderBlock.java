package org.BlueWallStudio.argest.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.BlueWallStudio.argest.blocks.entity.DecoderBlockEntity;
import org.jetbrains.annotations.Nullable;

public class DecoderBlock extends BlockWithEntity {
	public static final BooleanProperty POWERED = Properties.POWERED;
	public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

	public DecoderBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(POWERED, false));
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(POWERED);
		builder.add(FACING);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		// ленивое создание codec
		return createCodec(DecoderBlock::new);
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
		return new DecoderBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return world.isClient ? null
				: validateTicker(type, ModBlocks.DECODER_BLOCK_ENTITY,
						DecoderBlockEntity::tick);
	}

	@Override
	public boolean emitsRedstonePower(BlockState state) {
		return state.get(POWERED);
	}

	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		if (world.getBlockEntity(pos) instanceof DecoderBlockEntity decoder) {
			return decoder.getOutputPower(direction.getOpposite());
		}
		return 0;
	}
}
