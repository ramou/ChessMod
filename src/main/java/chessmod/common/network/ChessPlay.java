package chessmod.common.network;

import java.util.function.Supplier;
import java.util.logging.Logger;

import chessmod.ChessMod;
import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.blockentity.GoldChessboardBlockEntity;
import chessmod.common.capability.elo.Elo;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.InvalidMoveException;
import chessmod.common.dom.model.chess.piece.Knight;
import chessmod.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;


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
		public static boolean handle(final ChessPlay message, final Supplier<NetworkEvent.Context> ctx) {
			if (ctx.get().getDirection().getReceptionSide().isServer()) {
				ctx.get().enqueueWork(new Runnable() {
					// Use anon - lambda causes classloading issues
					@Override
					public void run() {
						Level world = ctx.get().getSender().level();
						BlockPos pos = new BlockPos((int) message.x, (int) message.y, (int) message.z);
						if(world.isLoaded(pos)) {
							
							BlockEntity blockEntity = world.getBlockEntity(pos);
							if (blockEntity instanceof ChessboardBlockEntity) {
								Board board = ((ChessboardBlockEntity)blockEntity).getBoard();
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
									if (blockEntity instanceof GoldChessboardBlockEntity) {
										board.moveSafely(m);
										if(board.getCheckMate() != null) {
											Logger.getGlobal().info(ctx.get().getSender().getName().getString() + " has won a chess game with themselves!");
											Elo.updateElo(ctx.get().getSender(), ctx.get().getSender(), true);
										}
									} else {
										board.move(m);
									}
									((ChessboardBlockEntity)blockEntity).notifyClientOfBoardChange();
									world.playSound(null, pos, sound, SoundSource.BLOCKS, 1F, 1F);
								} catch (InvalidMoveException e) {
									ChessMod.LOGGER.debug(e.getMessage());
									e.printStackTrace();
								}

							}
							
						}
					}
				});
			}

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
