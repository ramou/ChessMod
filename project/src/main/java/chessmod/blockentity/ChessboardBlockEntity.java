package chessmod.blockentity;

import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.board.BoardFactory;
import chessmod.common.dom.model.chess.board.SerializedBoard;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class ChessboardBlockEntity extends BlockEntity {
	protected Board board = BoardFactory.createBoard();
	
	public Board getBoard() {
		return board;
	}

	@Override
	public void onLoad() {
		super.onLoad();
	}

	
	@Override
	public AABB getRenderBoundingBox() {
		// This, combined with isGlobalRenderer in the BlockEntityRenderer makes it so that the
		// render does not disappear if the player can't see the block
		// This is useful for rendering larger models or dynamically sized models
		return INFINITE_EXTENT_AABB;
	}

	public ChessboardBlockEntity(BlockEntityType<?> blockEntityTypeIn, BlockPos pWorldPosition, BlockState pBlockState) {
		super(blockEntityTypeIn, pWorldPosition, pBlockState);
	}


	@Override
	public CompoundTag save(CompoundTag pTag) {
		super.save(pTag);
		SerializedBoard sb = SerializedBoard.serialize(board);
		pTag.putLong("piece_mask", sb.piece_mask);
		pTag.putLongArray("pieces", sb.pieces);
		pTag.putLongArray("moves", sb.moves);
		return pTag;
	}
	
	@Override
	public void load(CompoundTag pTag) {
		super.load(pTag);
		long pieceMask = pTag.getLong("piece_mask");
		long[] pieces = pTag.getLongArray("pieces");
		long[] moves = pTag.getLongArray("moves");
		if(pieceMask == 0 && pieces.length ==0) board = BoardFactory.createBoard();
		else board = new SerializedBoard(pieceMask, pieces, moves).deSerialize();
	}
	
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		CompoundTag packet = new CompoundTag();
		save(packet);
		return new ClientboundBlockEntityDataPacket(getBlockPos(),0,packet);
	}


	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		load(pkt.getTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		save(tag);
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		load(tag);
	}

	public void notifyClientOfBoardChange() {
		ClientboundBlockEntityDataPacket packet = getUpdatePacket();
		setChanged();
		if (packet != null) {
			getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
			getLevel().blockEntityChanged(getBlockPos());
		}
	}

}