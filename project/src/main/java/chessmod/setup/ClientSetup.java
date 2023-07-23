package chessmod.setup;

import chessmod.client.render.blockentity.ChessboardBlockEntityRenderer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
    	event.enqueueWork(ChessboardBlockEntityRenderer::register);
    }
}
