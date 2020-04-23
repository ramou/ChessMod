package com.htmlweb.chess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.htmlweb.chess.common.network.PacketHandler;
import com.htmlweb.chess.config.ConfigHolder;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ChessMod.MODID)
public class ChessMod {
	public static final String MODID = "com_htmlweb_chess";
	
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public ChessMod() {
		LOGGER.debug("So, you want to play some chesses?");

		final ModLoadingContext modLoadingContext = ModLoadingContext.get();
		
		PacketHandler.init();
		
		// Register Configs
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);
		modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigHolder.SERVER_SPEC);
		
	}
	
}
