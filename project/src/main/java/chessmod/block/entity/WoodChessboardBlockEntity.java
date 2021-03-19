package chessmod.block.entity;

import chessmod.init.ModBlockEntityTypes;

import net.minecraft.block.entity.BlockEntityType;

public class WoodChessboardBlockEntity extends ChessboardBlockEntity {
	public WoodChessboardBlockEntity(final BlockEntityType<?> blockEntityType) {
		super(blockEntityType);
	}
	
	public WoodChessboardBlockEntity() {
		super(ModBlockEntityTypes.WOOD_CHESSBOARD);
	}
}
