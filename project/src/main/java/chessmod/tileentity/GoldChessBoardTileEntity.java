package chessmod.tileentity;

import chessmod.init.ModTileEntityTypes;

import net.minecraft.tileentity.TileEntityType;

public class GoldChessBoardTileEntity extends ChessboardTileEntity {

	public GoldChessBoardTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public GoldChessBoardTileEntity() {
		super(ModTileEntityTypes.gold_chessboard);
	}
	
}
