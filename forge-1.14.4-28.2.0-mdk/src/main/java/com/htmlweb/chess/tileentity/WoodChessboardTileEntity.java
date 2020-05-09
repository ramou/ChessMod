package com.htmlweb.chess.tileentity;

import com.htmlweb.chess.ChessMod;
import com.htmlweb.chess.init.ModTileEntityTypes;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChessMod.MODID)
public class WoodChessboardTileEntity extends ChessboardTileEntity{
	
	public WoodChessboardTileEntity(final TileEntityType<?> type) {
		super(type);
	}
	
	public WoodChessboardTileEntity() {
		super(ModTileEntityTypes.WOOD_CHESSBOARD);
	}
	
}
