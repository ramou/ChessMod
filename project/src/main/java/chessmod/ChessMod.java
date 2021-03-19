package chessmod;

import chessmod.common.network.PacketHandler;
import chessmod.init.ModBlocks;
import chessmod.init.ModSounds;
import chessmod.init.ModBlockEntityTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;

public class ChessMod implements ModInitializer {
	public static final String MODID = "chessmod";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	@Override
	public void onInitialize() {
		LOGGER.info("So, you want to play some chesses?");
		PacketHandler.init();
		ModBlocks.init();
		ModBlockEntityTypes.init();
		ModSounds.init();
	}
}
