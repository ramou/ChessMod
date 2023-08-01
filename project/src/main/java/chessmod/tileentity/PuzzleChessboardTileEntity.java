package chessmod.tileentity;

import chessmod.ChessMod;
import chessmod.init.ModTileEntityTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = ChessMod.MODID)
public class PuzzleChessboardTileEntity extends ChessboardTileEntity {


	@ObjectHolder(ChessMod.MODID+ ":" + "puzzle_chessboard") public static TileEntityType<WoodChessboardTileEntity> TYPE;
	public PuzzleChessboardTileEntity(TileEntityType<?> type) {
		super(type);
	}

	public PuzzleChessboardTileEntity() {
		super(ModTileEntityTypes.puzzle_chessboard);
	}
	
}
