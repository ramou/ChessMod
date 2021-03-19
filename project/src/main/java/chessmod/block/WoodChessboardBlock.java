package chessmod.block;

import chessmod.client.gui.entity.WoodChessboardScreen;
import chessmod.init.ModBlockEntityTypes;
import chessmod.block.entity.ChessboardBlockEntity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class WoodChessboardBlock extends ChessboardBlock {
	public WoodChessboardBlock(Settings settings) {
		super(settings);
	}

	@Override
	public BlockEntity createBlockEntity(final BlockView world) {
		// Always use TileEntityType#create to allow registry overrides to work.
		return ModBlockEntityTypes.WOOD_CHESSBOARD.instantiate();
	}
	
	@Override
	protected void openGui(final World world, final BlockPos pos) {
		final BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity instanceof ChessboardBlockEntity) {
			MinecraftClient.getInstance().openScreen(new WoodChessboardScreen((ChessboardBlockEntity) tileEntity));
		}
	}
}
