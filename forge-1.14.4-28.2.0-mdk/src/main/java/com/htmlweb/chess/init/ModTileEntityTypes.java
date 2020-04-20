package com.htmlweb.chess.init;

import com.htmlweb.chess.ChessMod;
import com.htmlweb.chess.ModUtil;
import com.htmlweb.chess.tileentity.WoodChessboardTileEntity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ChessMod.MODID)
public final class ModTileEntityTypes {
	public static final TileEntityType<WoodChessboardTileEntity> WOOD_CHESSBOARD = ModUtil._null();
}
