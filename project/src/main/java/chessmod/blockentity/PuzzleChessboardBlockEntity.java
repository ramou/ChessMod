package chessmod.blockentity;

import chessmod.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PuzzleChessboardBlockEntity extends ChessboardBlockEntity {

	public PuzzleChessboardBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Registration.PUZZLE_CHESSBOARD_BE.get(), pWorldPosition, pBlockState);
	}
}