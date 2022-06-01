package chessmod.common.network;

import chessmod.ChessMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;


public final class PacketHandler {
	/*
	private static final String PROTOCOL = "2";
	
	public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(ChessMod.MODID, "chan"),
			() -> PROTOCOL,
			PROTOCOL::equals,
			PROTOCOL::equals
	);
	*/
	
	public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
    .named(new ResourceLocation(ChessMod.MODID, "messages"))
    .networkProtocolVersion(() -> "1.0")
    .clientAcceptedVersions(s -> true)
    .serverAcceptedVersions(s -> true)
    .simpleChannel();
	

	/**
	 * Send message to all within 64 blocks that have this chunk loaded
	 */
	public static void sendToNearby(Level level, BlockPos pos, Object toSend) {
		if(level instanceof ServerLevel serverLevel) {
			HANDLER.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), toSend);
		}
	}

	public static void sendToServer(Object msg) {
		HANDLER.sendToServer(msg);
	}
	private PacketHandler() {}

}
