package chessmod.tileentity;

import chessmod.ChessMod;
import chessmod.init.ModTileEntityTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = ChessMod.MODID)
public class AIChessboardTileEntity extends ChessboardTileEntity {

	@ObjectHolder(ChessMod.MODID+ ":" + "ai_chessboard") public static TileEntityType<WoodChessboardTileEntity> TYPE;
	public AIChessboardTileEntity(TileEntityType<?> type) {
		super(type);
	}


	public AIChessboardTileEntity() {
		super(ModTileEntityTypes.ai_chessboard);
	}
	
}
