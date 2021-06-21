package chessmod.tileentity;

import chessmod.ChessMod;
import chessmod.init.ModTileEntityTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = ChessMod.MODID)
public class WoodChessboardTileEntity extends ChessboardTileEntity{
	
	@ObjectHolder(ChessMod.MODID+ ":" + "woodchessboard") public static TileEntityType<WoodChessboardTileEntity> TYPE;
	public WoodChessboardTileEntity(TileEntityType<?> type) {
		super(type);
	}
	
	public WoodChessboardTileEntity() {
		super(ModTileEntityTypes.wood_chessboard);
	}
	
}
