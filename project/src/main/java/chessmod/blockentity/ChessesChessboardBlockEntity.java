package chessmod.blockentity;

import chessmod.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ChessesChessboardBlockEntity extends ChessboardBlockEntity {

	public ChessesChessboardBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Registration.CHESSES_CHESSBOARD_BE.get(), pWorldPosition, pBlockState);
	}
	
}
