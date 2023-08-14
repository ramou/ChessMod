package chessmod.client.render.tileentity;

import chessmod.ChessMod;
import chessmod.block.ChessboardBlock;
import chessmod.common.Point2f;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.tileentity.ChessboardTileEntity;
import chessmod.tileentity.GoldChessboardTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

public class ChessboardTileEntityRenderer extends TileEntityRenderer<ChessboardTileEntity> {
    public static final ResourceLocation black = new ResourceLocation("chessmod", "textures/block/black.png");
    public static final ResourceLocation white = new ResourceLocation("chessmod", "textures/block/white.png");
    public static final RenderType BLACK_PIECE;
    public static final RenderType WHITE_PIECE;
    public static final RenderType TURN_STATE;

    static {
        RenderType.State glState = RenderType.State.builder().setTextureState(new RenderState.TextureState(black, true, true)).createCompositeState(true);
        BLACK_PIECE = RenderType.create(ChessMod.MODID + "piece", DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 64, glState);
        glState = RenderType.State.builder().setTextureState(new RenderState.TextureState(white, true, true)).createCompositeState(true);
        WHITE_PIECE = RenderType.create(ChessMod.MODID + "piece", DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 64, glState);
        glState = RenderType.State.builder().createCompositeState(true);
        TURN_STATE = RenderType.create(ChessMod.MODID + "turn", DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 64, glState);

    }

    public ChessboardTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }


    public void draw2DRect(IVertexBuilder bufferbuilder, MatrixStack matrixStackIn, Point2f p1, Point2f p2, float r, float g, float b, float a) {
        Matrix4f model = matrixStackIn.last().pose();
        bufferbuilder.vertex(model, p2.x, 1.001f, p1.y).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(model, p1.x, 1.001f, p1.y).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(model, p1.x, 1.001f, p2.y).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(model, p2.x, 1.001f, p2.y).color(r, g, b, a).endVertex();
    }

    protected void showTurnColor(IVertexBuilder bufferbuilder, MatrixStack matrixStackIn, Side s) {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();

        float c = 0;

        if (s.equals(Side.WHITE)) c = 1;

        float x1Outter = 26f / 256f;
        float x1Inner = 29f / 256f;
        float x2Inner = 227f / 256f;
        float x2Outter = 230f / 256f;

        float z1Outter = 26f / 256f;
        float z1Inner = 29f / 256f;
        float z2Inner = 227f / 256f;
        float z2Outter = 230f / 256f;

        //top
        Point2f p1 = new Point2f(x1Outter, z1Outter);
        Point2f p2 = new Point2f(x2Outter, z1Inner);
        draw2DRect(bufferbuilder, matrixStackIn, p1, p2, c, c, c, 1f);

        //left
        p2 = new Point2f(x1Inner, z2Outter);
        draw2DRect(bufferbuilder, matrixStackIn, p1, p2, c, c, c, 1f);

        //right
        p1 = new Point2f(x2Inner, z1Outter);
        p2 = new Point2f(x2Outter, z2Outter);
        draw2DRect(bufferbuilder, matrixStackIn, p1, p2, c, c, c, 1f);

        //bottom
        p1 = new Point2f(x1Outter, z2Inner);
        draw2DRect(bufferbuilder, matrixStackIn, p1, p2, c, c, c, 1f);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }


    /**
     * Render our TileEntity
     */
    @Override
    public void render(ChessboardTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer b, int combinedLightIn, int combinedOverlayIn) {

        matrixStackIn.pushPose();
        IVertexBuilder bufferbuilder = b.getBuffer(TURN_STATE);
        if (tileEntityIn instanceof GoldChessboardTileEntity) {
            //Draw current-turn indicator:
            showTurnColor(bufferbuilder, matrixStackIn, tileEntityIn.getBoard().getCurrentPlayer());
        }
        matrixStackIn.popPose();

        RenderSystem.disableCull();

        RenderSystem.enableTexture();

        matrixStackIn.pushPose();
		rotateForBoardFacing(tileEntityIn, matrixStackIn);
        bufferbuilder = b.getBuffer(BLACK_PIECE);


        //Minecraft.getInstance().getTextureManager().bindTexture(black);

        for (int by = 0; by < 8; by++) {
            for (int bx = 0; bx < 8; bx++) {
                Piece piece = tileEntityIn.getBoard().pieceAt(Point.create(bx, by));
                if (piece != null) switch (piece.getCharacter()) {
                    case 'r':
                        drawRook(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn);
                        break;
                    case 'n':
                        drawKnight(bx, by, false, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn);
                        break;
                    case 'b':
                        drawBishop(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn);
                        break;
                    case 'q':
                        drawQueen(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn);
                        break;
                    case 'k':
                        drawKing(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn);
                        break;
                    case 'p':
                        drawPawn(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn);
                        break;
                    default:
                }
            }
        }
        matrixStackIn.popPose();

        matrixStackIn.pushPose();
		rotateForBoardFacing(tileEntityIn, matrixStackIn);
        bufferbuilder = b.getBuffer(WHITE_PIECE);
        //Minecraft.getInstance().getTextureManager().bindTexture(white);

        for (int by = 0; by < 8; by++) {
            for (int bx = 0; bx < 8; bx++) {
                Piece piece = tileEntityIn.getBoard().pieceAt(Point.create(bx, by));
                if (piece != null) switch (tileEntityIn.getBoard().pieceAt(Point.create(bx, by)).getCharacter()) {
                    case 'R':
                        drawRook(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn);
                        break;
                    case 'N':
                        drawKnight(bx, by, true, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn);
                        break;
                    case 'B':
                        drawBishop(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn);
                        break;
                    case 'Q':
                        drawQueen(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn);
                        break;
                    case 'K':
                        drawKing(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn);
                        break;
                    case 'P':
                        drawPawn(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn);
                        break;
                    default:
                }
            }
        }

        matrixStackIn.popPose();
        RenderSystem.enableCull();

    }

    private void rotateForBoardFacing(ChessboardTileEntity tileEntityIn, MatrixStack matrixStackIn) {
        switch (tileEntityIn.getBlockState().getValue(ChessboardBlock.FACING)) {
            case UP:

            case DOWN:

            case NORTH:
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180F));
                matrixStackIn.translate(-1.0D, 0.0D, -1.0D);
                break;
            case EAST:
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90F));
                matrixStackIn.translate(-1.0D, 0.0D, 0.0D);
                break;
            case SOUTH:
                break;
            case WEST:
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(270F));
                matrixStackIn.translate(0.0D, 0.0D, -1.0D);
                break;
        }
    }


    private void drawBishop(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn) {
        drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, 0, 0, 0);
        drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, 0, 0.04, 0);
    }

    private void drawKnight(int bx, int bz, boolean flip, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn) {
        float x = flip ? 0.01f : -0.01f;
        float z = flip ? -0.01f : 0.01f;

        drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, 0, z);
        drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, -x, 0.04, -z);
    }

    private void drawRook(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn) {
        drawPiece(0.03f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, 0, 0, 0);
    }

    private void drawKing(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn) {
        drawPiece(0.04f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, 0, 0, 0);
    }

    private void drawQueen(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn) {
        drawPiece(0.03f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, 0, 0, 0);
        drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, 0, 0.06, 0);
    }

    private void drawPawn(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn) {
        drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, 0, 0, 0);
    }

    private void drawPiece(float size, int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int overlay, final double x, final double y, final double z) {
        float xOff = (float) (x + 2.75f / 16f + bx * 1.5f / 16f);
        float yOff = (float) (y + 1 + size);
        float zOff = (float) (z + 2.75f / 16f + bz * 1.5f / 16f);


        final float PieceTileSize = 240;
        final float PieceTileBorderSize = 12;
        final float UnitPieceSize = 0.04f;
        float scaledTextureOffset = (2 - UnitPieceSize / size) / 2 * PieceTileBorderSize / PieceTileSize;

        Matrix4f model = matrixStackIn.last().pose();

        //south side [pos z] [parent x]
        bufferbuilder.vertex(model, xOff + size, yOff - size, zOff + size).uv(1 - scaledTextureOffset, 1 - scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff + size, yOff + size, zOff + size).uv(1 - scaledTextureOffset, scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff - size, yOff + size, zOff + size).uv(scaledTextureOffset, scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff - size, yOff - size, zOff + size).uv(scaledTextureOffset, 1 - scaledTextureOffset).endVertex();

        //north side [neg z] [parent x]
        bufferbuilder.vertex(model, xOff - size, yOff - size, zOff - size).uv(1 - scaledTextureOffset, 1 - scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff - size, yOff + size, zOff - size).uv(1 - scaledTextureOffset, scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff + size, yOff + size, zOff - size).uv(scaledTextureOffset, scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff + size, yOff - size, zOff - size).uv(scaledTextureOffset, 1 - scaledTextureOffset).endVertex();

        //east side [pos x] [parent z]
        bufferbuilder.vertex(model, xOff + size, yOff - size, zOff - size).uv(1 - scaledTextureOffset, 1 - scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff + size, yOff + size, zOff - size).uv(1 - scaledTextureOffset, scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff + size, yOff + size, zOff + size).uv(scaledTextureOffset, scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff + size, yOff - size, zOff + size).uv(scaledTextureOffset, 1 - scaledTextureOffset).endVertex();

        //west side [neg x] [parent z]
        bufferbuilder.vertex(model, xOff - size, yOff - size, zOff + size).uv(1 - scaledTextureOffset, 1 - scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff - size, yOff + size, zOff + size).uv(1 - scaledTextureOffset, scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff - size, yOff + size, zOff - size).uv(scaledTextureOffset, scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff - size, yOff - size, zOff - size).uv(scaledTextureOffset, 1 - scaledTextureOffset).endVertex();

        //top [pos y] [parent x & y]
        bufferbuilder.vertex(model, xOff + size, yOff + size, zOff - size).uv(1 - scaledTextureOffset, 1 - scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff - size, yOff + size, zOff - size).uv(1 - scaledTextureOffset, scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff - size, yOff + size, zOff + size).uv(scaledTextureOffset, scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff + size, yOff + size, zOff + size).uv(scaledTextureOffset, 1 - scaledTextureOffset).endVertex();

        //bottom [neg y] [parent x & y]
        bufferbuilder.vertex(model, xOff - size, yOff - size, zOff - size).uv(1 - scaledTextureOffset, 1 - scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff + size, yOff - size, zOff - size).uv(1 - scaledTextureOffset, scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff + size, yOff - size, zOff + size).uv(scaledTextureOffset, scaledTextureOffset).endVertex();
        bufferbuilder.vertex(model, xOff - size, yOff - size, zOff + size).uv(scaledTextureOffset, 1 - scaledTextureOffset).endVertex();

    }

}
