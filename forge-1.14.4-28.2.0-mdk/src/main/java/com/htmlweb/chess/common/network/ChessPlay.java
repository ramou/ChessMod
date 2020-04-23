package com.htmlweb.chess.common.network;

import java.util.function.Supplier;

import javax.vecmath.Point2i;

import com.htmlweb.chess.ChessMod;
import com.htmlweb.chess.tileentity.WoodChessboardTileEntity;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class ChessPlay {
	private final Point2i source;
	private final Point2i target;
	private final PieceType progression;
	private final double x;
	private final double y;
	private final double z;

	public ChessPlay(Point2i source, Point2i target, PieceType progression, double x, double y, double z) {
		this.source = source;
		this.target = target;
		this.progression = progression;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static ChessPlay decode(PacketBuffer buf) {
		Point2i source = new Point2i(buf.readByte(), buf.readByte());
		Point2i target = new Point2i(buf.readByte(), buf.readByte());
		PieceType progression = PieceType.values()[buf.readByte()];
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();

		return new ChessPlay(source, target, progression, x, y, z);
	}

	public static void encode(ChessPlay msg, PacketBuffer buf) {
		buf.writeByte(msg.source.x);
		buf.writeByte(msg.source.y);
		buf.writeByte(msg.target.x);
		buf.writeByte(msg.target.y);
		buf.writeByte(msg.progression.ordinal());
		buf.writeDouble(msg.x);
		buf.writeDouble(msg.y);
		buf.writeDouble(msg.z);
	}

	public static class Handler {
		public static void handle(final ChessPlay message, final Supplier<NetworkEvent.Context> ctx) {

			ChessMod.LOGGER.debug("Getting a ChessPlay message...");
			
			if (ctx.get().getDirection().getReceptionSide().isServer()) {
				ctx.get().enqueueWork(new Runnable() {
					// Use anon - lambda causes classloading issues
					@Override
					public void run() {
						System.out.println("Queuing a change in move.");
						//EntityPlayerMP sender = ctx.get().getSender();


						World world = ctx.get().getSender().world;
						BlockPos pos = new BlockPos(message.x, message.y, message.z);
						if(world.isBlockLoaded(pos)) {
							TileEntity tileEntity = world.getTileEntity(pos);
							if (tileEntity instanceof WoodChessboardTileEntity) {
								char c = ((WoodChessboardTileEntity)tileEntity).getBoardState()[message.source.y][message.source.x];
								((WoodChessboardTileEntity)tileEntity).getBoardState()[message.source.y][message.source.x]='.';
								((WoodChessboardTileEntity)tileEntity).getBoardState()[message.target.y][message.target.x]=c;
								((WoodChessboardTileEntity)tileEntity).notifyClientOfMove();
							}
							
						}
					}
				});
			}

			ctx.get().setPacketHandled(true);
		}
	}
	
	public enum PieceType {
		KING,
		QUEEN,
		ROOK,
		KNIGHT,
		BISHOP,
		PAWN,
		;
	}
}
