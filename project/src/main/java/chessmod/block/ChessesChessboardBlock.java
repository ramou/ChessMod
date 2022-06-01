package chessmod.block;

import javax.annotation.Nullable;

import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.blockentity.ChessesChessboardBlockEntity;
import chessmod.client.gui.entity.GoldChessboardGui;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChessesChessboardBlock extends ChessboardBlock {
	public ChessesChessboardBlock() {
		super();
	}
	
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ChessesChessboardBlockEntity(pos, state);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	protected void openGui(final Level levelIn, final BlockPos pos) {
		final BlockEntity blockEntity = levelIn.getBlockEntity(pos);
		if (blockEntity instanceof ChessboardBlockEntity) {
			Minecraft.getInstance().setScreen(new GoldChessboardGui((ChessboardBlockEntity)blockEntity));
		}
	}
	
}
