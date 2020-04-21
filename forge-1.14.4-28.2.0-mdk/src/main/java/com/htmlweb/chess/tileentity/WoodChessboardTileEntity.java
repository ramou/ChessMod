package com.htmlweb.chess.tileentity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.htmlweb.chess.client.render.WoodChessboard;
import com.htmlweb.chess.init.ModTileEntityTypes;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WoodChessboardTileEntity extends TileEntity implements ITickableTileEntity {
	
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
	private String state = "rnbqkbnrpppppppp................................PPPPPPPPRNBQKBNR";

	public String getBoardState() {
	
		return state;
	}

	public void setBoardState(String state) {
		
		this.state = state;
	}

	/**
	 * Read saved data from disk into the tile.
	 */
	@Override
	public void read(final CompoundNBT compound) {
		super.read(compound);
		this.state = compound.getString("state");
	}

	/**
	 * Write data from the tile into a compound tag for saving to disk.
	 */
	@Nonnull
	@Override
	public CompoundNBT write(final CompoundNBT compound) {
		super.write(compound);
		compound.putString("state", this.state);
		return compound;
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT tag = new CompoundNBT();
		write(tag);
		return new SUpdateTileEntityPacket(getPos(), 1, tag);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		CompoundNBT tag = pkt.getNbtCompound();
		read(tag);
	}
	
	
	/**
	 * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the
	 * chunk or when many blocks change at once.
	 * This compound comes back to you client-side in {@link #handleUpdateTag}
	 * The default implementation ({@link TileEntity#handleUpdateTag}) calls {@link #writeInternal)}
	 * which doesn't save any of our extra data so we override it to call {@link #write} instead
	 */
	@Nonnull
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}

	@Override
	public void tick() {
		SUpdateTileEntityPacket packet = getUpdatePacket();
		if (packet != null && getWorld() instanceof ServerWorld) {
			((ServerChunkProvider) getWorld().getChunkProvider()).chunkManager
			.getTrackingPlayers(new ChunkPos(pos), false)
			.forEach(e -> e.connection.sendPacket(packet));
		}
	}
	
	
}
