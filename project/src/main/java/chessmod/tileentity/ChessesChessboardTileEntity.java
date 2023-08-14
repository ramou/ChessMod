package chessmod.tileentity;

import chessmod.ChessMod;
import chessmod.init.ModTileEntityTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChessMod.MODID)
public class ChessesChessboardTileEntity extends ChessboardTileEntity {

	public ChessesChessboardTileEntity(final TileEntityType<?> type) {
		super(type);
	}

	public ChessesChessboardTileEntity() {
		super(ModTileEntityTypes.chesses_chessboard);
	}
	
}
