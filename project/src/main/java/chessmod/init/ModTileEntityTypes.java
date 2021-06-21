package chessmod.init;

import chessmod.ChessMod;
import chessmod.ModUtil;
import chessmod.tileentity.WoodChessboardTileEntity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ChessMod.MODID)
public final class ModTileEntityTypes {
	public static final TileEntityType<WoodChessboardTileEntity> wood_chessboard = ModUtil._null();
	public static final TileEntityType<WoodChessboardTileEntity> gold_chessboard = ModUtil._null();
}
