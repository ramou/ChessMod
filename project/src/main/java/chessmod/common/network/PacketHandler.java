package chessmod.common.network;

import chessmod.ChessMod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class PacketHandler {
	private static final String PROTOCOL = "2";
	public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(ChessMod.MODID, "chan"),
			() -> PROTOCOL,
			PROTOCOL::equals,
			PROTOCOL::equals
	);

	public static void init() {
		int id = 0;

		HANDLER.registerMessage(id++, ChessPlay.class, ChessPlay::encode, ChessPlay::decode, ChessPlay.Handler::handle);
		HANDLER.registerMessage(id++, ArbitraryPlacement.class, ArbitraryPlacement::encode, ArbitraryPlacement::decode, ArbitraryPlacement.Handler::handle);

		
	}

	/**
	 * Send message to all within 64 blocks that have this chunk loaded
	 */
	public static void sendToNearby(World world, BlockPos pos, Object toSend) {
		if(world instanceof ServerWorld) {
			ServerWorld ws = (ServerWorld) world;

			ServerChunkProvider chunkProvider = ws.getChunkProvider();
			chunkProvider.chunkManager.getTrackingPlayers(new ChunkPos(pos), false)
					.filter(p -> p.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64)
					.forEach(p -> HANDLER.send(PacketDistributor.PLAYER.with(() -> p), toSend));
		}
	}

	public static void sendToNearby(World world, Entity e, Object toSend) {
		sendToNearby(world, new BlockPos(e.getPositionVec()), toSend);
	}

	public static void sendTo(ServerPlayerEntity playerMP, Object toSend) {
		HANDLER.sendTo(toSend, playerMP.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendNonLocal(ServerPlayerEntity playerMP, Object toSend) {
		if(playerMP.server.isDedicatedServer() || !playerMP.getGameProfile().getName().equals(playerMP.server.getServerOwner())) {
			sendTo(playerMP, toSend);
		}
	}

	public static void sendToServer(Object msg) {
		HANDLER.sendToServer(msg);
	}

	private PacketHandler() {}

}
