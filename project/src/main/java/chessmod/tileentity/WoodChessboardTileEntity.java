package chessmod.tileentity;

import chessmod.ChessMod;
import chessmod.init.ModTileEntityTypes;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChessMod.MODID)
public class WoodChessboardTileEntity extends ChessboardTileEntity{
	
	public WoodChessboardTileEntity(final TileEntityType<?> type) {
		super(type);
	}
	
	public WoodChessboardTileEntity() {
		super(ModTileEntityTypes.wood_chessboard);
	}
	
}
