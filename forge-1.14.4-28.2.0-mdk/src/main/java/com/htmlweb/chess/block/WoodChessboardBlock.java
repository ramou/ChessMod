package com.htmlweb.chess.block;

import javax.annotation.Nullable;

import com.htmlweb.chess.client.gui.entity.WoodChessBoardGUI;
import com.htmlweb.chess.init.ModTileEntityTypes;
import com.htmlweb.chess.tileentity.WoodChessboardTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.GlassBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class WoodChessboardBlock extends GlassBlock {
	public WoodChessboardBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		VoxelShape BOARD = Block.makeCuboidShape(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
		VoxelShape STAND = Block.makeCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 12.0D, 10.0D);
		VoxelShape ALL =   VoxelShapes.or(BOARD, STAND);
		return ALL;
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
		return false;
	}

	@Override
	public boolean hasTileEntity(final BlockState state) {
		return true;
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
		// Always use TileEntityType#create to allow registry overrides to work.
		return ModTileEntityTypes.WOOD_CHESSBOARD.create();
	}

	@Override
	public boolean onBlockActivated(final BlockState state, final World worldIn, final BlockPos pos, final PlayerEntity player, final Hand handIn, final BlockRayTraceResult hit) {
		// Only open the gui on the physical client
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> openGui(worldIn, pos));
		return true;
	}

	// @OnlyIn(Dist.CLIENT) Makes it so this method will be removed from the class on the PHYSICAL SERVER
	// This is because we only want to handle opening the GUI on the physical client.
	@OnlyIn(Dist.CLIENT)
	private void openGui(final World worldIn, final BlockPos pos) {
		final TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity instanceof WoodChessboardTileEntity) {
			Minecraft.getInstance().displayGuiScreen(new WoodChessBoardGUI((WoodChessboardTileEntity)tileEntity));
		}
	}
	
	
}
