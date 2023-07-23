package chessmod.tileentity;

import chessmod.ChessMod;
import chessmod.init.ModTileEntityTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChessMod.MODID)
public class PuzzleChessboardTileEntity extends ChessboardTileEntity {

	public PuzzleChessboardTileEntity(final TileEntityType<?> type) {
		super(type);
	}

	public PuzzleChessboardTileEntity() {
		super(ModTileEntityTypes.puzzle_chessboard);
	}
	
}
