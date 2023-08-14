package chessmod.init;

import chessmod.ChessMod;
import chessmod.ModUtil;
import chessmod.tileentity.*;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ChessMod.MODID)
public final class ModTileEntityTypes {
	public static final TileEntityType<WoodChessboardTileEntity> wood_chessboard = ModUtil._null();
	public static final TileEntityType<WoodChessboardTileEntity> gold_chessboard = ModUtil._null();
	public static final TileEntityType<ChessesChessboardTileEntity> chesses_chessboard = ModUtil._null();
	public static final TileEntityType<AIChessboardTileEntity> ai_chessboard = ModUtil._null();
	public static final TileEntityType<PuzzleChessboardTileEntity> puzzle_chessboard = ModUtil._null();
}
