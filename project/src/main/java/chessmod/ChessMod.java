package chessmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chessmod.setup.ClientSetup;
import chessmod.setup.ModSetup;
import chessmod.setup.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod(ChessMod.MODID)
public class ChessMod {
	public static final String MODID = "chessmod";
	
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	

	

	public ChessMod() {
		ModSetup.setup();
		Registration.init();
		IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modbus.addListener(ClientSetup::init));

	}

    
}
