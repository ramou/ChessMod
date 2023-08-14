package chessmod.client;

import chessmod.tileentity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chessmod.ChessMod;
import chessmod.client.render.tileentity.ChessboardTileEntityRenderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Subscribe to events from the MOD EventBus that should be handled on the PHYSICAL CLIENT side in this class
 *
 * @author Cadiboo
 */
@EventBusSubscriber(modid = ChessMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEventSubscriber {

	private static final Logger LOGGER = LogManager.getLogger(ChessMod.MODID + " Client Mod Event Subscriber");

	/**
	 * This method will be called by Forge when it is time for the mod to do its client-side setup
	 * This method will always be called after the Registry events.
	 * This means that all Blocks, Items, TileEntityTypes, etc. will all have been registered already
	 */
	@SubscribeEvent
	public static void onFMLClientSetupEvent(final FMLClientSetupEvent event) {

		// Register TileEntity Renderers
		ClientRegistry.bindTileEntityRenderer(WoodChessboardTileEntity.TYPE, ChessboardTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(GoldChessBoardTileEntity.TYPE, ChessboardTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(PuzzleChessboardTileEntity.TYPE,ChessboardTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(ChessesChessboardTileEntity.TYPE, ChessboardTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(AIChessboardTileEntity.TYPE, ChessboardTileEntityRenderer::new);
		LOGGER.debug("Registered TileEntity Renderers");

	}
	
	

}
