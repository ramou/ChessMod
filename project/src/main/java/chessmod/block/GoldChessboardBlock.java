package chessmod.block;

import chessmod.client.gui.entity.GoldChessboardScreen;
import chessmod.init.ModBlockEntityTypes;
import chessmod.block.entity.ChessboardBlockEntity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class GoldChessboardBlock extends ChessboardBlock {
	public GoldChessboardBlock(Settings settings) {
		super(settings);
	}

	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return ModBlockEntityTypes.GOLD_CHESSBOARD.instantiate();
	}
	
	@Override
	protected void openGui(final World world, final BlockPos pos) {
		final BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof ChessboardBlockEntity) {
			MinecraftClient.getInstance().openScreen(new GoldChessboardScreen((ChessboardBlockEntity)blockEntity));
		}
	}
}
