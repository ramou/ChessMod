package chessmod.tileentity;

import chessmod.ChessMod;
import chessmod.init.ModTileEntityTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = ChessMod.MODID)
public class ChessesChessboardTileEntity extends ChessboardTileEntity {

	@ObjectHolder(ChessMod.MODID+ ":" + "chesses_chessboard") public static TileEntityType<WoodChessboardTileEntity> TYPE;
	public ChessesChessboardTileEntity(TileEntityType<?> type) {
		super(type);
	}

	public ChessesChessboardTileEntity() {super(ModTileEntityTypes.chesses_chessboard);
	}
	
}
