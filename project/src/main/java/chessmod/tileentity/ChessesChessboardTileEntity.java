package chessmod.tileentity;

import chessmod.ChessMod;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class ChessesChessboardTileEntity extends ChessboardTileEntity {

	@ObjectHolder(ChessMod.MODID+ ":" + "chesses_chessboard") 
	public static TileEntityType<ChessesChessboardTileEntity> TYPE;
	
	public ChessesChessboardTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public ChessesChessboardTileEntity() {
		this(TYPE);
	}

}
