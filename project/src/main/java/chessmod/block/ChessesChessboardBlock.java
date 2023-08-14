package chessmod.block;

import javax.annotation.Nullable;

import chessmod.client.gui.entity.GoldChessboardGui;
import chessmod.tileentity.ChessboardTileEntity;
import chessmod.tileentity.ChessesChessboardTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChessesChessboardBlock extends ChessboardBlock {
	public ChessesChessboardBlock(Properties properties) {
		super(properties);
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
		return new ChessesChessboardTileEntity(ChessesChessboardTileEntity.TYPE);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	protected void openGui(final World worldIn, final BlockPos pos) {
		final TileEntity tileEntity = worldIn.getBlockEntity(pos);
		if (tileEntity instanceof ChessboardTileEntity) {
			Minecraft.getInstance().setScreen(new GoldChessboardGui((ChessboardTileEntity)tileEntity));
		}
	}
	
}
