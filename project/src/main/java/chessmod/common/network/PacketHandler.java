package chessmod.common.network;

import chessmod.ChessMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;


public final class PacketHandler {
	public static final SimpleChannel HANDLER = ChannelBuilder
    .named(ResourceLocation.fromNamespaceAndPath(ChessMod.MODID, "messages"))
    .networkProtocolVersion(1)
    .simpleChannel();
	

	/**
	 * Send message to all within 64 blocks that have this chunk loaded
	 */
	public static void sendToNearby(Level level, BlockPos pos, Object toSend) {
		if(level instanceof ServerLevel serverLevel) {
			HANDLER.send(toSend, PacketDistributor.TRACKING_CHUNK.with(serverLevel.getChunkAt(pos)));
		}
	}

	public static void sendToServer(Object msg) {
		HANDLER.send(msg, PacketDistributor.SERVER.noArg());
	}
	private PacketHandler() {}

}
