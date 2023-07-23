package chessmod;

import chessmod.common.capability.elo.EloCapability;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chessmod.common.network.PacketHandler;
import net.minecraftforge.fml.common.Mod;

@Mod(ChessMod.MODID)
public class ChessMod {
	public static final String MODID = "chessmod";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public ChessMod() {
		LOGGER.debug("So, you want to play some chesses?");
		PacketHandler.init();
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupAdditional);
	}

	public void setupAdditional (final FMLCommonSetupEvent event){
		EloCapability.register();
	}

}
