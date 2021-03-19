package chessmod.block.entity;


import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.board.BoardFactory;
import chessmod.common.dom.model.chess.board.SerializedBoard;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;

public abstract class ChessboardBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
//	@Nullable
//	@Environment(EnvType.CLIENT)
//	public ChessboardRenderer chessboardRenderer = ChessboardRenderer.forTileEntity(this);

	protected Board board = BoardFactory.createBoard();
	
	public Board getBoard() {
		return board;
	}

	public ChessboardBlockEntity(BlockEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	/**
	 * Read saved data from disk into the tile.
	 */
	@Override
	public void fromTag(BlockState state, CompoundTag compound) {
		super.fromTag(state, compound);
		long pieceMask = compound.getLong("piece_mask");
		long[] pieces = compound.getLongArray("pieces");
		long[] moves = compound.getLongArray("moves");
		if(pieceMask == 0 && pieces.length ==0) board = BoardFactory.createBoard();
		else board = new SerializedBoard(pieceMask, pieces, moves).deSerialize();
	}

	/**
	 * Write data from the tile into a compound tag for saving to disk.
	 */
	@Override
	public CompoundTag toTag(final CompoundTag compound) {
		super.toTag(compound);
		SerializedBoard sb = SerializedBoard.serialize(board);
		compound.putLong("piece_mask", sb.piece_mask);
		compound.putLongArray("pieces", sb.pieces);
		compound.putLongArray("moves", sb.moves);
		return compound;
	}

	@Override
	public void fromClientTag(CompoundTag tag) {
		fromTag(this.getCachedState(), tag);
	}

	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		return toTag(tag);
	}

	@Nullable
	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return super.toUpdatePacket();
	}

	@SuppressWarnings("resource")
	public void notifyClientOfBoardChange() {
		this.sync();
	}
}
