package chessmod.blockentity;

import chessmod.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AIChessboardBlockEntity extends ChessboardBlockEntity {

	public AIChessboardBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Registration.AI_CHESSBOARD_BE.get(), pWorldPosition, pBlockState);
	}
	
}
