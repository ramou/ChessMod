package chessmod.common.network;

import java.util.function.Supplier;

import chessmod.ChessMod;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.InvalidMoveException;
import chessmod.common.dom.model.chess.piece.Knight;
import chessmod.init.ModSounds;
import chessmod.tileentity.ChessboardTileEntity;
import chessmod.tileentity.GoldChessboardTileEntity;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

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



	public static ChessPlay decode(PacketBuffer buf) {
		long move   = buf.readLong();
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		return new ChessPlay(move, x, y, z);
	}

	public static void encode(ChessPlay msg, PacketBuffer buf) {
		buf.writeLong(msg.move);
		buf.writeDouble(msg.x);
		buf.writeDouble(msg.y);
		buf.writeDouble(msg.z);
	}

	public static class Handler {
		public static void handle(final ChessPlay message, final Supplier<NetworkEvent.Context> ctx) {
			if (ctx.get().getDirection().getReceptionSide().isServer()) {
				ctx.get().enqueueWork(new Runnable() {
					// Use anon - lambda causes classloading issues
					@Override
					public void run() {
						World world = ctx.get().getSender().world;
						BlockPos pos = new BlockPos(message.x, message.y, message.z);
						if(world.isAreaLoaded(pos, 1)) {
							
							TileEntity tileEntity = world.getTileEntity(pos);
							if (tileEntity instanceof ChessboardTileEntity) {
								Board board = ((ChessboardTileEntity)tileEntity).getBoard();
								Move m = Move.create((int)message.move, board);
								
								SoundEvent sound = null;
								if(board.pieceAt(m.getSource()) instanceof Knight) {
									if(board.pieceAt(m.getTarget()) == null) {
										sound = ModSounds.place_piece;
									} else {
										sound = ModSounds.place_piece_take;
									}
								} else {
									if(board.pieceAt(m.getTarget()) == null) {
										sound = ModSounds.slide_piece;
									} else {
										sound = ModSounds.slide_piece_take;
									}
								}

								try { //On GoldChessBoard confirm that it is a valid move!
									if (tileEntity instanceof GoldChessboardTileEntity) {
										board.moveSafely(m);
									} else {
										board.move(m);
									}
									((ChessboardTileEntity)tileEntity).notifyClientOfBoardChange();
									world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1F, 1F);
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
		}
	}
}
