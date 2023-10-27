package chessmod.setup;

import chessmod.client.render.blockentity.ChessboardBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
    	event.enqueueWork(() -> {
            BlockEntityRenderers.register(Registration.WOOD_CHESSBOARD_BE.get(), ChessboardBlockEntityRenderer::new);
            BlockEntityRenderers.register(Registration.GOLD_CHESSBOARD_BE.get(), ChessboardBlockEntityRenderer::new);
            BlockEntityRenderers.register(Registration.CHESSES_CHESSBOARD_BE.get(),ChessboardBlockEntityRenderer::new);
            BlockEntityRenderers.register(Registration.AI_CHESSBOARD_BE.get(),ChessboardBlockEntityRenderer::new);
            BlockEntityRenderers.register(Registration.PUZZLE_CHESSBOARD_BE.get(), ChessboardBlockEntityRenderer::new);
            BlockEntityRenderers.register(Registration.QUANTUM_CHESSBOARD_BE.get(), ChessboardBlockEntityRenderer::new);

        });
    }
}
