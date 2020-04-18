package com.htmlweb.chess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.htmlweb.chess.init.ModBlocks;
import com.htmlweb.chess.init.ModItems;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ChessMod.MODID)
public class ChessMod {
	public static final String MODID = "com_htmlweb_chess";
	
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public ChessMod() {
		LOGGER.debug("So, you want to play some chesses?");
		
		//final ModLoadingContext modLoadingContext = ModLoadingContext.get();
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		// Register Deferred Registers (Does not need to be before Configs)
		ModBlocks.BLOCKS.register(modEventBus);
		ModItems.ITEMS.register(modEventBus);

		// Register Configs (Does not need to be after Deferred Registers)
		//modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);
		//modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigHolder.SERVER_SPEC);
	}
	
}
