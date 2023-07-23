package chessmod.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.GlassBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public abstract class ChessboardBlock extends GlassBlock {

	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	public ChessboardBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		VoxelShape BOARD = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
		VoxelShape STAND = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 12.0D, 10.0D);

		return VoxelShapes.or(BOARD, STAND);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
		return false;
	}

	@Override
	public boolean hasTileEntity(final BlockState state) {
		return true;
	}

	@Override
	public ActionResultType use(BlockState bs, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> openGui(worldIn, pos));
		return ActionResultType.PASS;
	}

	protected abstract void openGui(final World worldIn, final BlockPos pos);

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		//Don't mess around, if they're looking up or down, direction is east.
		//If we really want to make this nice we can examine relative directions
		//carefully at a later date.

		Direction face = context.getNearestLookingDirection().getOpposite();
		if(face == Direction.UP || face == Direction.DOWN) face = Direction.NORTH;
		return this.defaultBlockState().setValue(FACING, face);
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

}