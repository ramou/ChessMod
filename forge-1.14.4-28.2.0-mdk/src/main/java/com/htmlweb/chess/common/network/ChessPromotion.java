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

public class ChessPromotion {
	private final char piece;
	private final Point2i target;
	private final double x;
	private final double y;
	private final double z;

	public ChessPromotion(char piece, Point2i target, double x, double y, double z) {
		this.piece = piece;
		this.target = target;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static ChessPromotion decode(PacketBuffer buf) {
		char piece = buf.readChar();
		Point2i target = new Point2i(buf.readByte(), buf.readByte());
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();

		return new ChessPromotion(piece, target, x, y, z);
	}

	public static void encode(ChessPromotion msg, PacketBuffer buf) {
		buf.writeChar(msg.piece);
		buf.writeByte(msg.target.x);
		buf.writeByte(msg.target.y);
		buf.writeDouble(msg.x);
		buf.writeDouble(msg.y);
		buf.writeDouble(msg.z);
	}

	public static class Handler {
		public static void handle(final ChessPromotion message, final Supplier<NetworkEvent.Context> ctx) {

			ChessMod.LOGGER.debug("Getting a ChessMove message...");
			
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
								((WoodChessboardTileEntity)tileEntity).getBoardState()[message.target.y][message.target.x]=message.piece;
								((WoodChessboardTileEntity)tileEntity).notifyClientOfMove();
							}
							
						}
					}
				});
			}

			ctx.get().setPacketHandled(true);
		}
	}
}
