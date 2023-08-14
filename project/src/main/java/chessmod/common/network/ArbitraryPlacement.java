package chessmod.common.network;

import java.util.function.Supplier;

import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.blockentity.WoodChessboardBlockEntity;
import chessmod.common.dom.model.chess.PieceInitializer;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class ArbitraryPlacement {
	private final int point;
	private final int piece;
	private final double x;
	private final double y;
	private final double z;

	public int getPiece() {
		return piece;
	}

	public ArbitraryPlacement(Piece piece, BlockPos pos) {
		this(piece.getPosition().serialize(), piece.serialize(), pos.getX(), pos.getY(), pos.getZ());
	}

	public ArbitraryPlacement(int point, int piece, double x, double y, double z) {
		this.point = point;
		this.piece = piece;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static ArbitraryPlacement decode(FriendlyByteBuf buf) {
		int point  = buf.readInt();
		int piece  = buf.readInt();
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		return new ArbitraryPlacement(point, piece, x, y, z);
	}

	public static void encode(ArbitraryPlacement msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.point);
		buf.writeInt(msg.piece);
		buf.writeDouble(msg.x);
		buf.writeDouble(msg.y);
		buf.writeDouble(msg.z);
	}

	public static class Handler {
		public static void handle(final ArbitraryPlacement message, final Supplier<NetworkEvent.Context> ctx) {
			if (ctx.get().getDirection().getReceptionSide().isServer()) {
				ctx.get().enqueueWork(new Runnable() {
					// Use anon - lambda causes classloading issues
					@Override
					public void run() {

						Level level = ctx.get().getSender().level;
						BlockPos pos = new BlockPos(message.x, message.y, message.z);
						
						if(level.isLoaded(pos)) {
							BlockEntity blockEntity = level.getBlockEntity(pos);
							if (blockEntity instanceof WoodChessboardBlockEntity) { //If we want this stuff for other boards, we have to reconsider move format.
								Board board = ((ChessboardBlockEntity)blockEntity).getBoard();
								Point point = Point.create(message.point);
								Piece piece = PieceInitializer.create(point, message.piece);
								board.setPiece(piece, point);
								((ChessboardBlockEntity)blockEntity).notifyClientOfBoardChange();
								level.playSound(null, pos, Registration.PLACE_PIECE_SOUND.get(), SoundSource.BLOCKS, 1F, 1F);
							}
							
						}
					}
				});
			}

			ctx.get().setPacketHandled(true);
		}
	}
}
