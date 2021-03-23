package chessmod.init;

import chessmod.ChessMod;
import chessmod.block.entity.GoldChessboardBlockEntity;
import chessmod.block.entity.WoodChessboardBlockEntity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class ModBlockEntityTypes {
	public static final BlockEntityType<WoodChessboardBlockEntity> WOOD_CHESSBOARD = BlockEntityType.Builder.create(WoodChessboardBlockEntity::new, ModBlocks.WOOD_CHESSBOARD).build(null);
	public static final BlockEntityType<GoldChessboardBlockEntity> GOLD_CHESSBOARD = BlockEntityType.Builder.create(GoldChessboardBlockEntity::new, ModBlocks.GOLD_CHESSBOARD).build(null);

	public static void init() {
		Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(ChessMod.MODID, "wood_chessboard"), WOOD_CHESSBOARD);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(ChessMod.MODID, "gold_chessboard"), GOLD_CHESSBOARD);
	}
}
