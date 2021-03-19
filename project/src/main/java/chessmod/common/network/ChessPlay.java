package chessmod.common.network;

import chessmod.ChessMod;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.InvalidMoveException;
import chessmod.common.dom.model.chess.piece.Knight;
import chessmod.init.ModSounds;
import chessmod.block.entity.ChessboardBlockEntity;
import chessmod.block.entity.GoldChessboardBlockEntity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ChessPlay {
	private final long move;
	private final double x;
	private final double y;
	private final double z;
	

	public ChessPlay(long move, BlockPos pos) {
		this(move, pos.getX(), pos.getY(), pos.getZ());
	}

	public ChessPlay(long move, double x, double y, double z) {
		this.move = move;
		this.x = x;
		this.y = y;
		this.z = z;
	}



	public static ChessPlay decode(PacketByteBuf buf) {
		long move   = buf.readLong();
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		return new ChessPlay(move, x, y, z);
	}

	public static PacketByteBuf encode(ChessPlay msg, PacketByteBuf buf) {
		buf.writeLong(msg.move);
		buf.writeDouble(msg.x);
		buf.writeDouble(msg.y);
		buf.writeDouble(msg.z);
		return buf;
	}

	public static class Handler implements ServerPlayNetworking.PlayChannelHandler {
		public static final ServerPlayNetworking.PlayChannelHandler INSTANCE = new Handler();

		@Override
		public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			ChessPlay message = ChessPlay.decode(buf);
			server.execute(() -> {
				World world = player.world;
				BlockPos pos = new BlockPos(message.x, message.y, message.z);
				if(world.isChunkLoaded(pos)) {

					BlockEntity blockEntity = world.getBlockEntity(pos);
					if (blockEntity instanceof ChessboardBlockEntity) {
						Board board = ((ChessboardBlockEntity)blockEntity).getBoard();
						Move m = Move.create((int)message.move, board);

						SoundEvent sound;
						if(board.pieceAt(m.getSource()) instanceof Knight) {
							if(board.pieceAt(m.getTarget()) == null) {
								sound = ModSounds.placePiece;
							} else {
								sound = ModSounds.placePieceTake;
							}
						} else {
							if(board.pieceAt(m.getTarget()) == null) {
								sound = ModSounds.slidePiece;
							} else {
								sound = ModSounds.slidePieceTake;
							}
						}

						try { //On GoldChessBoard confirm that it is a valid move!
							if (blockEntity instanceof GoldChessboardBlockEntity) {
								board.moveSafely(m);
							} else {
								board.move(m);
							}
							((ChessboardBlockEntity)blockEntity).notifyClientOfBoardChange();
							world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1F, 1F);
						} catch (InvalidMoveException e) {
							ChessMod.LOGGER.debug(e.getMessage());
							e.printStackTrace();
						}
					}
				}
			});
		}
	}
}
