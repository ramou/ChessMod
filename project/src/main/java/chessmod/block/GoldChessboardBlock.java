package chessmod.block;

import javax.annotation.Nullable;

import chessmod.client.gui.entity.GoldChessboardGui;
import chessmod.init.ModTileEntityTypes;
import chessmod.tileentity.ChessboardTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GoldChessboardBlock extends ChessboardBlock {
	public GoldChessboardBlock(Properties properties) {
		super(properties);
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
		// Always use TileEntityType#create to allow registry overrides to work.
		return ModTileEntityTypes.gold_chessboard.create();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	protected void openGui(final World worldIn, final BlockPos pos) {
		final TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity instanceof ChessboardTileEntity) {
			Minecraft.getInstance().displayGuiScreen(new GoldChessboardGui((ChessboardTileEntity)tileEntity));
		}
	}

}
