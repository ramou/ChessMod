package chessmod.common.network;

import java.util.function.Supplier;

import chessmod.common.dom.model.chess.PieceInitializer;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.init.ModSounds;
import chessmod.tileentity.ChessboardTileEntity;
import chessmod.tileentity.WoodChessboardTileEntity;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class ArbitraryPlacement {
	private final int point;
	private final int piece;
	private final double x;
	private final double y;
	private final double z;
	

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

	public static ArbitraryPlacement decode(PacketBuffer buf) {
		int point  = buf.readInt();
		int piece  = buf.readInt();
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		return new ArbitraryPlacement(point, piece, x, y, z);
	}

	public static void encode(ArbitraryPlacement msg, PacketBuffer buf) {
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
						World world = ctx.get().getSender().level;
						BlockPos pos = new BlockPos(message.x, message.y, message.z);
						if(world.isAreaLoaded(pos, 1)) {
							
							TileEntity tileEntity = world.getBlockEntity(pos);
							if (tileEntity instanceof WoodChessboardTileEntity) { //If we want this stuff for other boards, we have to reconsider move format.
								Board board = ((ChessboardTileEntity)tileEntity).getBoard();
								Point point = Point.create(message.point);
								Piece piece = PieceInitializer.create(point, message.piece);
								board.setPiece(piece, point);
								((ChessboardTileEntity)tileEntity).notifyClientOfBoardChange();
								world.playSound(null, pos, ModSounds.place_piece, SoundCategory.BLOCKS, 1F, 1F);
							}
							
						}
					}
				});
			}

			ctx.get().setPacketHandled(true);
		}
	}
}
