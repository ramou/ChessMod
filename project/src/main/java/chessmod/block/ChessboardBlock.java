package chessmod.block;


import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;




public abstract class ChessboardBlock extends GlassBlock implements EntityBlock {

	public ChessboardBlock() {
		super(BlockBehaviour.Properties.of(Material.STONE));
	}

	 
	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, net.minecraft.core.BlockPos pPos, CollisionContext pContext) {
		VoxelShape BOARD = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
		VoxelShape STAND = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 12.0D, 10.0D);
		VoxelShape ALL =   Shapes.or(BOARD, STAND);
		return ALL;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, net.minecraft.core.BlockPos pPos) {
		return false;
	}
	
	
	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
			BlockHitResult pHit) {
		if(pLevel.isClientSide) openGui(pLevel, pPos);
		
		return InteractionResult.SUCCESS;
	}
	
	protected abstract void openGui(final Level pLevel, final BlockPos pos);

}