package chessmod.block.entity;

import chessmod.init.ModBlockEntityTypes;

import net.minecraft.block.entity.BlockEntityType;

public class GoldChessboardBlockEntity extends ChessboardBlockEntity {

	public GoldChessboardBlockEntity(BlockEntityType<?> blockEntityType) {
		super(blockEntityType);
	}

	public GoldChessboardBlockEntity() {
		super(ModBlockEntityTypes.GOLD_CHESSBOARD);
	}
	
}
