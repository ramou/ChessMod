package chessmod.tileentity;

import chessmod.ChessMod;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class AIChessboardTileEntity extends ChessboardTileEntity {

	@ObjectHolder(ChessMod.MODID+ ":" + "ai_chessboard") 
	public static TileEntityType<AIChessboardTileEntity> TYPE;
	
	public AIChessboardTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public AIChessboardTileEntity() {
		this(TYPE);
	}
}
