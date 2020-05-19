package chessmod;

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
	}
	
}
