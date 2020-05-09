package com.htmlweb.chess.tileentity;

import com.htmlweb.chess.init.ModTileEntityTypes;

import net.minecraft.tileentity.TileEntityType;

public class GoldChessBoardTileEntity extends ChessboardTileEntity {

	public GoldChessBoardTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public GoldChessBoardTileEntity() {
		super(ModTileEntityTypes.GOLD_CHESSBOARD);
	}
	
}
