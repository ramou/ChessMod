package chessmod.tileentity;

import chessmod.ChessMod;
import chessmod.init.ModTileEntityTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class PuzzleChessboardTileEntity extends ChessboardTileEntity {

	@ObjectHolder(ChessMod.MODID+ ":" + "puzzle_chessboard") 
	public static TileEntityType<PuzzleChessboardTileEntity> TYPE;
	
	public PuzzleChessboardTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public PuzzleChessboardTileEntity() {
		super(ModTileEntityTypes.puzzle_chessboard);
	}
	
}
