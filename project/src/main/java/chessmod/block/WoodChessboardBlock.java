package chessmod.block;

import javax.annotation.Nullable;

import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.blockentity.WoodChessboardBlockEntity;
import chessmod.client.gui.entity.WoodChessboardGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WoodChessboardBlock extends ChessboardBlock {
	public WoodChessboardBlock() {
		super();
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WoodChessboardBlockEntity(pos, state);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	protected void openGui(final Level levelIn, final BlockPos pos) {
		final BlockEntity blockEntity = levelIn.getBlockEntity(pos);
		if (blockEntity instanceof ChessboardBlockEntity) {
			Minecraft.getInstance().setScreen(new WoodChessboardGUI((ChessboardBlockEntity)blockEntity));
		}
	}
	
	
}
