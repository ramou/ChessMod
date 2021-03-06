package chessmod.tileentity;

import javax.annotation.Nonnull;

import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.board.BoardFactory;
import chessmod.common.dom.model.chess.board.SerializedBoard;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ChessboardTileEntity extends TileEntity {
	protected Board board = BoardFactory.createBoard();
	
	public Board getBoard() {
		return board;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onLoad() {
		super.onLoad();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		// This, combined with isGlobalRenderer in the TileEntityRenderer makes it so that the
		// render does not disappear if the player can't see the block
		// This is useful for rendering larger models or dynamically sized models
		return INFINITE_EXTENT_AABB;
	}

	public ChessboardTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	/**
	 * Read saved data from disk into the tile.
	 */
	@Override
	public void read(BlockState state, final CompoundNBT compound) {
		super.read(state, compound);
		long pieceMask = compound.getLong("piece_mask");
		long[] pieces = compound.getLongArray("pieces");
		long[] moves = compound.getLongArray("moves");
		if(pieceMask == 0 && pieces.length ==0) board = BoardFactory.createBoard();
		else board = new SerializedBoard(pieceMask, pieces, moves).deSerialize();
	}

	/**
	 * Write data from the tile into a compound tag for saving to disk.
	 */
	@Nonnull
	@Override
	public CompoundNBT write(final CompoundNBT compound) {
		super.write(compound);
		SerializedBoard sb = SerializedBoard.serialize(board);
		compound.putLong("piece_mask", sb.piece_mask);
		compound.putLongArray("pieces", sb.pieces);
		compound.putLongArray("moves", sb.moves);
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
		read(getBlockState(), tag);
	}

	@Nonnull
	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT tag = super.getUpdateTag();
		this.write(tag);
		return tag;
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		this.read(getBlockState(), tag);
	}


	@SuppressWarnings("resource")
	public void notifyClientOfBoardChange() {
		SUpdateTileEntityPacket packet = getUpdatePacket();
		if (packet != null && getWorld() instanceof ServerWorld) {
			((ServerChunkProvider) getWorld().getChunkProvider()).chunkManager
			.getTrackingPlayers(new ChunkPos(this.getPos()), false)
			.forEach(e -> e.connection.sendPacket(packet));
		}
	}

}