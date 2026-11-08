package chessmod.blockentity;

import chessmod.block.QuantumChessBoardBlock;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.board.BoardFactory;
import chessmod.common.dom.model.chess.board.SerializedBoard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.function.Supplier;

public abstract class ChessboardBlockEntity extends BlockEntity {
	protected Board board;



	public void initialize() {
		this.board = BoardFactory.createBoard();
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board b) {
		board = b;
	}

	public String getDimension() {
		return level.dimension().location().toString();
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
		initialize();
	}

	@Override
	protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider lookup) {
		super.saveAdditional(pTag, lookup);
		SerializedBoard sb = SerializedBoard.serialize(board);
		pTag.putLong("piece_mask", sb.piece_mask);
		pTag.putLongArray("pieces", sb.pieces);
		pTag.putLongArray("moves", sb.moves);
	}
	
	@Override
	protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider lookup) {
		super.loadAdditional(pTag, lookup);
		long pieceMask = pTag.getLong("piece_mask");
		long[] pieces = pTag.getLongArray("pieces");
		long[] moves = pTag.getLongArray("moves");
		if (pieceMask == 0 && pieces.length == 0) {
			board = BoardFactory.createBoard();
		} else {
			board = new SerializedBoard(pieceMask, pieces, moves).deSerialize();
		}
	}
	
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}


	// Forge's default onDataPacket/handleUpdateTag call loadWithComponents,
	// which in turn dispatches to loadAdditional with the registry provider.
	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
		return saveWithoutMetadata(lookup);
	}

	public void notifyClientOfBoardChange() {
		ClientboundBlockEntityDataPacket packet = getUpdatePacket();
		setChanged();
		if (packet != null && level != null) {
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
			level.blockEntityChanged(getBlockPos());
		}
	}

	public void playSoundForNearbyPlayers(SoundEvent e) {
		if(level != null) {
			level.playSound(null, getBlockPos(), e, SoundSource.BLOCKS, 1F, 1F);
		}
	}

	public void playSoundForNearbyPlayers(Supplier<SoundEvent> e) {
		playSoundForNearbyPlayers(e.get());
	}

	private final int DEFAULT_MESSAGE_RANGE = 25;
	public void sendMessageToNearbyPlayers(Component message) {
		sendMessageToNearbyPlayers(message, DEFAULT_MESSAGE_RANGE);
	}

	public void sendMessageToNearbyPlayers(Component message, double range) {
		if (this.level == null || this.level.isClientSide) {
			return; // Only run on the server side
		}

		AABB boundingBox = new AABB(
				this.worldPosition.getX() - range, this.worldPosition.getY() - range, this.worldPosition.getZ() - range,
				this.worldPosition.getX() + range, this.worldPosition.getY() + range, this.worldPosition.getZ() + range
		);

		for (Player player : this.level.players()) {
			if (boundingBox.contains(player.position())) {
				player.displayClientMessage(message, false);
			}
		}
	}



}