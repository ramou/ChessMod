package chessmod.tileentity;

import chessmod.ChessMod;
import chessmod.init.ModTileEntityTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid = ChessMod.MODID)
public class AIChessboardTileEntity extends ChessboardTileEntity {

	public AIChessboardTileEntity(final TileEntityType<?> type) {
		super(type);
	}

	public AIChessboardTileEntity() {
		super(ModTileEntityTypes.ai_chessboard);
	}
	
}
