package chessmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chessmod.block.AIChessboardBlock;
import chessmod.block.ChessesChessboardBlock;
import chessmod.block.GoldChessboardBlock;
import chessmod.block.PuzzleChessboardBlock;
import chessmod.block.WoodChessboardBlock;
import chessmod.common.capability.elo.EloCapability;
import chessmod.common.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(ChessMod.MODID)
public class ChessMod {
	public static final String MODID = "chessmod";
	
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final RegistryObject<Block> wood_chessboard = BLOCKS.register("wood_chessboard", () -> new WoodChessboardBlock());
	public static final RegistryObject<Block> gold_chessboard = BLOCKS.register("gold_chessboard", () -> new GoldChessboardBlock());
	public static final RegistryObject<Block> chesses_chessboard = BLOCKS.register("chesses_chessboard", () -> new ChessesChessboardBlock());
	public static final RegistryObject<Block> ai_chessboard = BLOCKS.register("ai_chessboard", () -> new AIChessboardBlock());
	public static final RegistryObject<Block> puzzle_chessboard = BLOCKS.register("puzzle_chessboard", () -> new PuzzleChessboardBlock());
	

	public ChessMod() {
		LOGGER.debug("So, you want to play some chesses?");
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
		PacketHandler.init();
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupAdditional);
	}
	
    public void setupAdditional (final FMLCommonSetupEvent event){
        EloCapability.register();
    }
	
}
