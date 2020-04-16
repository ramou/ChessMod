package com.htmlweb.chess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;

@Mod(ChessMod.MODID)
public class ChessMod {
	public static final String MODID = "com_htmlweb_chess";
	
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public ChessMod() {
		LOGGER.debug("So, you want to play some chesses?");
	}
	
}
