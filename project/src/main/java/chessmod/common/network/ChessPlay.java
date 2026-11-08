package chessmod.common.network;

import java.util.function.Supplier;
import java.util.logging.Logger;

import chessmod.ChessMod;
import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.blockentity.GoldChessboardBlockEntity;
import chessmod.blockentity.QuantumChessBoardBlockEntity;
import chessmod.common.capability.elo.Elo;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.InvalidMoveException;
import chessmod.common.dom.model.chess.piece.Knight;
import chessmod.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;


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



	public static ChessPlay decode(FriendlyByteBuf buf) {
		long move   = buf.readLong();
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		return new ChessPlay(move, x, y, z);
	}

	public static void encode(ChessPlay msg, FriendlyByteBuf buf) {
		buf.writeLong(msg.move);
		buf.writeDouble(msg.x);
		buf.writeDouble(msg.y);
		buf.writeDouble(msg.z);
	}

	public static class Handler {
		public static void handle(final ChessPlay message, final CustomPayloadEvent.Context ctx) {

			if (ctx.isServerSide()) {
				ctx.enqueueWork(new Runnable() {
					// Use anon - lambda causes classloading issues
					@Override
					public void run() {
						Level world = ctx.getSender().level();
						BlockPos pos = new BlockPos((int) message.x, (int) message.y, (int) message.z);
						if(world.isLoaded(pos)) {

							if (world.getBlockEntity(pos) instanceof ChessboardBlockEntity chessBlockEntity) {
								Board board = chessBlockEntity.getBoard();
								Move m = Move.create((int)message.move, board);

								SoundEvent sound = null;
								if(board.pieceAt(m.getSource()) instanceof Knight) {
									if(board.pieceAt(m.getTarget()) == null) {
										sound = Registration.PLACE_PIECE_SOUND.get();
									} else {
										sound = Registration.PLACE_PIECE_TAKE_SOUND.get();
									}
								} else {
									if(board.pieceAt(m.getTarget()) == null) {
										sound = Registration.SLIDE_PIECE_SOUND.get();
									} else {
										sound = Registration.SLIDE_PIECE_TAKE_SOUND.get();
									}
								}

								try { //On GoldChessBoard confirm that it is a valid move!
									if (chessBlockEntity instanceof GoldChessboardBlockEntity || chessBlockEntity instanceof QuantumChessBoardBlockEntity) {
										board.moveSafely(m);

										if (board.getCheckMate() != null) {
											Logger.getGlobal().info(ctx.getSender().getName().getString() + " has won a chess game with themselves!");
											Elo.updateElo(ctx.getSender(), ctx.getSender(), true);
										}
										if (chessBlockEntity instanceof QuantumChessBoardBlockEntity qcbe) {
											if(qcbe.getLinkedBoardEntity() == null) {
												Logger.getGlobal().info(ctx.getSender().getName().getString() + " played on an unlinked board!");
											} else {
												Logger.getGlobal().info(ctx.getSender().getName().getString() + " played on an linked board!");
												qcbe.getLinkedBoardEntity().getBoard().moveSafely(m);
											}
										}
									} else {
										board.move(m);
									}

									chessBlockEntity.notifyClientOfBoardChange();
									chessBlockEntity.playSoundForNearbyPlayers(sound);


									if (chessBlockEntity instanceof QuantumChessBoardBlockEntity qcbe){
										qcbe.getLinkedBoardEntity().notifyClientOfBoardChange();
										qcbe.getLinkedBoardEntity().playSoundForNearbyPlayers(sound);
									}

								} catch (InvalidMoveException e) {
									ChessMod.LOGGER.debug(e.getMessage());
									e.printStackTrace();
								}

							}

						}
					}
				});
			}

			ctx.setPacketHandled(true);
		}
	}
}
