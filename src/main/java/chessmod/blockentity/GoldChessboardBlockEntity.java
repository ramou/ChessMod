package chessmod.blockentity;

import chessmod.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class GoldChessboardBlockEntity extends ChessboardBlockEntity {

	public GoldChessboardBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Registration.GOLD_CHESSBOARD_BE.get(), pWorldPosition, pBlockState);
	}

	
}
