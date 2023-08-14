package chessmod.tileentity;

import chessmod.ChessMod;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class GoldChessboardTileEntity extends ChessboardTileEntity {

	@ObjectHolder(ChessMod.MODID+ ":" + "gold_chessboard") 
	public static TileEntityType<GoldChessboardTileEntity> TYPE;
	
	public GoldChessboardTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public GoldChessboardTileEntity() {
		this(TYPE);
	}

}
