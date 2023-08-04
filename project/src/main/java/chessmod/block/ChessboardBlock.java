package chessmod.block;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;




public abstract class ChessboardBlock extends GlassBlock implements EntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	
	public ChessboardBlock() {
		super(BlockBehaviour.Properties.of().noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}
	 
	@Override
	public VoxelShape getVisualShape(BlockState pState, BlockGetter pLevel, net.minecraft.core.BlockPos pPos, CollisionContext pContext) {
		VoxelShape BOARD = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
		VoxelShape STAND = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 12.0D, 10.0D);
		return Shapes.or(BOARD, STAND);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, net.minecraft.core.BlockPos pPos) {
		return false;
	}
	
	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
			BlockHitResult pHit) {
		/*
		 * We want to know how much to rotate the screen by based on what direction they're facing.
		 */		
		if(pLevel.isClientSide) {
			openGui(pLevel, pPos);
		}

		return InteractionResult.SUCCESS;
	}

	protected abstract void openGui(final Level pLevel, final BlockPos pos);

	
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
    	//Don't mess around, if they're looking up or down, direction is east.
    	//If we really want to make this nice we can examine relative directions 
    	//carefully at a later date. 
        Direction face = context.getNearestLookingDirection().getOpposite();
        if(face == Direction.UP || face == Direction.DOWN) face = Direction.NORTH;
		return this.defaultBlockState().setValue(BlockStateProperties.FACING, face);
    }

	
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }


    
}