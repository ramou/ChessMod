package chessmod.client.render.blockentity;

import chessmod.blockentity.GoldChessboardBlockEntity;
import chessmod.client.gui.entity.ChessboardGUI;
import chessmod.common.Point2f;
import chessmod.common.dom.model.chess.Side;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

public class TurnBasedChessboardBlockEntityRenderer<T extends GoldChessboardBlockEntity> extends ChessboardBlockEntityRenderer<T>  {

    public TurnBasedChessboardBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        super.render(pBlockEntity, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay);

        //Draw current-turn indicator:
        Side s = pBlockEntity.getBoard().getCurrentPlayer();
        //It does not seem to care what the texture is for this...
        VertexConsumer bufferbuilder = pBufferSource.getBuffer(RenderType.entitySolid(ChessboardGUI.WHITE));

        pPoseStack.pushPose();
        float c = 0;

        if(s.equals(Side.WHITE)) {
            c = 1;
        }

        float x1Outter = 26f/256f;
        float x1Inner = 29f/256f;
        float x2Inner = 227f/256f;
        float x2Outter = 230f/256f;

        float z1Outter = 26f/256f;
        float z1Inner = 29f/256f;
        float z2Inner = 227f/256f;
        float z2Outter = 230f/256f;

        //top
        Point2f p1 = new Point2f(x1Outter, z1Outter);
        Point2f p2 = new Point2f(x2Outter, z1Inner);
        draw2DRect(bufferbuilder, pPoseStack, p1, p2, c, c, c, 1f, pPackedLight, pPackedOverlay);
        //left
        p2 = new Point2f(x1Inner, z2Outter);
        draw2DRect(bufferbuilder, pPoseStack, p1, p2, c, c, c, 1f, pPackedLight, pPackedOverlay);

        //right
        p1 = new Point2f(x2Inner, z1Outter);
        p2 = new Point2f(x2Outter, z2Outter);
        draw2DRect(bufferbuilder, pPoseStack, p1, p2, c, c, c, 1f, pPackedLight, pPackedOverlay);

        //bottom
        p1 = new Point2f(x1Outter, z2Inner);
        draw2DRect(bufferbuilder, pPoseStack, p1, p2, c, c, c, 1f, pPackedLight, pPackedOverlay);

        pPoseStack.popPose();

    }

    private void draw2DRect(VertexConsumer  bufferbuilder, PoseStack pPoseStack, Point2f p1, Point2f p2, float r, float g, float b, float a, int pPackedLight, int pPackedOverlay) {
        Matrix4f model = pPoseStack.last().pose() ;
                bufferbuilder.addVertex(model, p2.x, 1.001f, p1.y).setColor(r, g, b, a).setUv(0, 0).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0, 1, 0);
        bufferbuilder.addVertex(model, p1.x, 1.001f, p1.y).setColor(r, g, b, a).setUv(0, 0).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0, 1, 0);
        bufferbuilder.addVertex(model, p1.x, 1.001f, p2.y).setColor(r, g, b, a).setUv(0, 0).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0, 1, 0);
        bufferbuilder.addVertex(model, p2.x, 1.001f, p2.y).setColor(r, g, b, a).setUv(0, 0).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0, 1, 0);
    }

}
