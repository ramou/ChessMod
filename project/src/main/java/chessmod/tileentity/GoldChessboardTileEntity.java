package chessmod.tileentity;

import chessmod.ChessMod;
import chessmod.init.ModTileEntityTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class GoldChessboardTileEntity extends ChessboardTileEntity {

	@ObjectHolder(ChessMod.MODID+ ":" + "gold_chessboard") public static TileEntityType<GoldChessboardTileEntity> TYPE;
	public GoldChessboardTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public GoldChessboardTileEntity() {
		super(ModTileEntityTypes.gold_chessboard);
	}
	
}
