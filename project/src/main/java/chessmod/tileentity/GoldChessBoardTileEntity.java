package chessmod.tileentity;

import chessmod.ChessMod;
import chessmod.init.ModTileEntityTypes;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class GoldChessBoardTileEntity extends ChessboardTileEntity {
	@ObjectHolder(ChessMod.MODID+ ":" + "gold_chessboard") public static TileEntityType<GoldChessBoardTileEntity> TYPE;
	public GoldChessBoardTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public GoldChessBoardTileEntity() {
		super(ModTileEntityTypes.gold_chessboard);
	}
	
}
