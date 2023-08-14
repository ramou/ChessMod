package chessmod.common.network;

import chessmod.ChessMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;


public final class PacketHandler {

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
