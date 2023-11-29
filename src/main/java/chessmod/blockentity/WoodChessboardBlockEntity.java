package chessmod.blockentity;

import chessmod.ChessMod;
import chessmod.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChessMod.MODID)
public class WoodChessboardBlockEntity extends ChessboardBlockEntity{
	
	public WoodChessboardBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Registration.WOOD_CHESSBOARD_BE.get(), pWorldPosition, pBlockState);
	}
	
}
