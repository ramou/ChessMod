package com.htmlweb.chess.tileentity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.htmlweb.chess.ChessMod;
import com.htmlweb.chess.client.render.WoodChessboard;
import com.htmlweb.chess.init.ModTileEntityTypes;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChessMod.MODID)
public class WoodChessboardTileEntity extends TileEntity{
	
	@Nullable // May be accessed before onLoad
	// @OnlyIn(Dist.CLIENT) Makes it so this field will be removed from the class on the PHYSICAL SERVER
	// This is because we only want the MiniModel on the physical client - its rendering only.
	@OnlyIn(Dist.CLIENT)
	public WoodChessboard woodChessboard;
	
	public WoodChessboardTileEntity(final TileEntityType<?> type) {
		super(type);
	}
	
	public WoodChessboardTileEntity() {
		super(ModTileEntityTypes.WOOD_CHESSBOARD);
	}

	// @OnlyIn(Dist.CLIENT) Makes it so this method will be removed from the class on the PHYSICAL SERVER
	// This is because we only want the MiniModel on the physical client - its rendering only.
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onLoad() {
		super.onLoad();
		World world = getWorld();
		if (world == null || !world.isRemote)
			return; // Return if the world is null or if we are on the logical server
		woodChessboard = WoodChessboard.forTileEntity(this);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		// This, combined with isGlobalRenderer in the TileEntityRenderer makes it so that the
		// render does not disappear if the player can't see the block
		// This is useful for rendering larger models or dynamically sized models
		return INFINITE_EXTENT_AABB;
	}
	
	//in FEN notation
	private char[][] boardState = {
			{'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'}, 
			{'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'}, 
			{'.', '.', '.', '.', '.', '.', '.', '.'}, 
			{'.', '.', '.', '.', '.', '.', '.', '.'}, 
			{'.', '.', '.', '.', '.', '.', '.', '.'}, 
			{'.', '.', '.', '.', '.', '.', '.', '.'}, 
			{'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'}, 
			{'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'} 
	};

	public char[][] getBoardState() {
	
		return boardState;
	}

	public void setBoardState(char[][] boardState) {
		
		this.boardState = boardState;
	}

	/**
	 * Read saved data from disk into the tile.
	 */
	@Override
	public void read(final CompoundNBT compound) {
		super.read(compound);
		String s = compound.getString("boardState");
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				boardState[y][x] = s.charAt(y*8+x);
			}
		}
	}

	/**
	 * Write data from the tile into a compound tag for saving to disk.
	 */
	@Nonnull
	@Override
	public CompoundNBT write(final CompoundNBT compound) {
		super.write(compound);
		StringBuilder sb = new StringBuilder();
		for(int y = 0; y < 8; y++) {
			sb.append(boardState[y]);
		}
		compound.putString("boardState", sb.toString());
		return compound;
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT tag = new CompoundNBT();
		write(tag);
		return new SUpdateTileEntityPacket(getPos(), 42, tag); //What minecraft uses... cause 42.
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		CompoundNBT tag = pkt.getNbtCompound();
		read(tag);
	}

	@Nonnull
	public CompoundNBT getUpdateTag() {
		CompoundNBT tag = super.getUpdateTag();
		this.write(tag);
		return tag;
	}
	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		this.read(tag);
	}
	
	public void notifyClientOfMove() {
		SUpdateTileEntityPacket packet = getUpdatePacket();
		if (packet != null && getWorld() instanceof ServerWorld) {
			((ServerChunkProvider) getWorld().getChunkProvider()).chunkManager
			.getTrackingPlayers(new ChunkPos(pos), false)
			.forEach(e -> e.connection.sendPacket(packet));
		}
	}
	
}
