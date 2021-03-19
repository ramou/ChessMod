package chessmod.common.network;

import chessmod.common.dom.model.chess.PieceType;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.init.ModSounds;
import chessmod.block.entity.ChessboardBlockEntity;
import chessmod.block.entity.WoodChessboardBlockEntity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

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

	public static ArbitraryPlacement decode(PacketByteBuf buf) {
		int point  = buf.readInt();
		int piece  = buf.readInt();
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		return new ArbitraryPlacement(point, piece, x, y, z);
	}

	public static PacketByteBuf encode(ArbitraryPlacement msg, PacketByteBuf buf) {
		buf.writeInt(msg.point);
		buf.writeInt(msg.piece);
		buf.writeDouble(msg.x);
		buf.writeDouble(msg.y);
		buf.writeDouble(msg.z);
		return buf;
	}

	public static class Handler implements ServerPlayNetworking.PlayChannelHandler {
		public static final ServerPlayNetworking.PlayChannelHandler INSTANCE = new Handler();

		@Override
		public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			ArbitraryPlacement placement = ArbitraryPlacement.decode(buf);
			server.execute(() -> {
				World world = player.world;
				BlockPos pos = new BlockPos(placement.x, placement.y, placement.z);
				if(world.isChunkLoaded(pos)) {
					BlockEntity tileEntity = world.getBlockEntity(pos);
					if (tileEntity instanceof WoodChessboardBlockEntity) { //If we want this stuff for other boards, we have to reconsider move format.
						Board board = ((ChessboardBlockEntity)tileEntity).getBoard();
						Point point = Point.create(placement.point);
						Piece piece = PieceType.create(point, placement.piece);
						board.setPiece(piece, point);
						((ChessboardBlockEntity)tileEntity).notifyClientOfBoardChange();
						world.playSound(null, pos, ModSounds.placePiece, SoundCategory.BLOCKS, 1F, 1F);
					}
				}
			});
		}
	}
}
