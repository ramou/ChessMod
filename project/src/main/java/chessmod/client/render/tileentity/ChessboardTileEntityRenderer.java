package chessmod.client.render.tileentity;

import chessmod.block.ChessboardBlock;
import chessmod.common.Point2f;
import chessmod.common.dom.model.chess.Side;
import chessmod.tileentity.GoldChessBoardTileEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import chessmod.ChessMod;
import chessmod.client.gui.entity.ChessboardGUI.Color4f;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.tileentity.ChessboardTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class ChessboardTileEntityRenderer extends TileEntityRenderer<ChessboardTileEntity> {
    public static final ResourceLocation black = new ResourceLocation("chessmod", "textures/block/black.png");
    public static final ResourceLocation white = new ResourceLocation("chessmod", "textures/block/white.png");
    public static final ResourceLocation shadeblack = new ResourceLocation("chessmod", "textures/gui/shadeblack.png");
    public static final ResourceLocation shadewhite = new ResourceLocation("chessmod", "textures/gui/shadewhite.png");
    public static final RenderType BLACK_PIECE;
    public static final RenderType WHITE_PIECE;

    public static final RenderType SHADE_BLACK;
    public static final RenderType SHADE_WHITE;

    static {
        RenderType.State glState = RenderType.State.getBuilder().texture(new RenderState.TextureState(black, false, true)).diffuseLighting(new RenderState.DiffuseLightingState(true)).build(true);
        BLACK_PIECE = RenderType.makeType(ChessMod.MODID + "piece", DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 64, glState);
        glState = RenderType.State.getBuilder().texture(new RenderState.TextureState(white, false, true)).diffuseLighting(new RenderState.DiffuseLightingState(true)).build(true);
        WHITE_PIECE = RenderType.makeType(ChessMod.MODID + "piece", DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 64, glState);
        glState = RenderType.State.getBuilder().texture(new RenderState.TextureState(shadeblack, false, true)).diffuseLighting(new RenderState.DiffuseLightingState(true)).build(true);
        SHADE_BLACK = RenderType.makeType(ChessMod.MODID + "piece", DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 64, glState);
        glState = RenderType.State.getBuilder().texture(new RenderState.TextureState(shadewhite, false, true)).diffuseLighting(new RenderState.DiffuseLightingState(true)).build(true);
        SHADE_WHITE = RenderType.makeType(ChessMod.MODID + "piece", DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 64, glState);
    }

    public ChessboardTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    public void draw2DRect(MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, Point2f p1, Point2f p2) {
        Matrix4f model = matrixStackIn.getLast().getMatrix();
        bufferbuilder.pos(model, p2.x, 1.001F, p1.y).tex(1, 1).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(model, p1.x, 1.001F, p1.y).tex(1, 0).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(model, p1.x, 1.001F, p2.y).tex(0, 0).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(model, p2.x, 1.001F, p2.y).tex(0, 1).normal(0, 1, 0).endVertex();
    }

    protected void showTurnColor(MatrixStack matrixStackIn, IRenderTypeBuffer b, Side s) {
        IVertexBuilder bufferbuilder = null;
        if (s.equals(Side.WHITE)) {
            bufferbuilder = b.getBuffer(SHADE_WHITE);
        } else {
            bufferbuilder = b.getBuffer(SHADE_BLACK);
        }

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
        draw2DRect(matrixStackIn, bufferbuilder, p1, p2);
        //left
        p2 = new Point2f(x1Inner, z2Outter);
        draw2DRect(matrixStackIn, bufferbuilder, p1, p2);

        //right
        p1 = new Point2f(x2Inner, z1Outter);
        p2 = new Point2f(x2Outter, z2Outter);
        draw2DRect(matrixStackIn, bufferbuilder, p1, p2);

        //bottom
        p1 = new Point2f(x1Outter, z2Inner);
        draw2DRect(matrixStackIn, bufferbuilder, p1, p2);
    }

    /**
     * Render our TileEntity
     */
    @Override
    public void render(ChessboardTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer b, int combinedLightIn, int combinedOverlayIn) {
        final double x = 0;
        final double y = 0;
        final double z = 0;

        GlStateManager.disableCull();
        GlStateManager.enableTexture();
        Color4f.WHITE.apply();

        if (tileEntityIn instanceof GoldChessBoardTileEntity) {
            //Draw current-turn indicator:
            matrixStackIn.push();
            showTurnColor(matrixStackIn, b, tileEntityIn.getBoard().getCurrentPlayer());
            matrixStackIn.pop();
        }

        matrixStackIn.push();
        IVertexBuilder bufferbuilder = b.getBuffer(BLACK_PIECE);
        rotateForBoardFacing(tileEntityIn, matrixStackIn);

        for (int by = 0; by < 8; by++) {
            for (int bx = 0; bx < 8; bx++) {
                Piece piece = tileEntityIn.getBoard().pieceAt(Point.create(bx, by));
                if (piece != null) switch (piece.getCharacter()) {
                    case 'r':
                        drawRook(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
                        break;
                    case 'n':
                        drawKnight(bx, by, false, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
                        break;
                    case 'b':
                        drawBishop(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
                        break;
                    case 'q':
                        drawQueen(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
                        break;
                    case 'k':
                        drawKing(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
                        break;
                    case 'p':
                        drawPawn(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
                        break;
                    default:
                }
            }
        }
        matrixStackIn.pop();

        matrixStackIn.push();
        Color4f.WHITE.apply();
        bufferbuilder = b.getBuffer(WHITE_PIECE);
        rotateForBoardFacing(tileEntityIn, matrixStackIn);

        for (int by = 0; by < 8; by++) {
            for (int bx = 0; bx < 8; bx++) {
                Piece piece = tileEntityIn.getBoard().pieceAt(Point.create(bx, by));
                if (piece != null) switch (tileEntityIn.getBoard().pieceAt(Point.create(bx, by)).getCharacter()) {
                    case 'R':
                        drawRook(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
                        break;
                    case 'N':
                        drawKnight(bx, by, true, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
                        break;
                    case 'B':
                        drawBishop(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
                        break;
                    case 'Q':
                        drawQueen(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
                        break;
                    case 'K':
                        drawKing(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
                        break;
                    case 'P':
                        drawPawn(bx, by, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
                        break;
                    default:
                }
            }
        }

        matrixStackIn.pop();
        GlStateManager.enableCull();
    }

    private void rotateForBoardFacing(ChessboardTileEntity tileEntityIn, MatrixStack matrixStackIn) {
        BlockPos pos = tileEntityIn.getPos();
        switch (tileEntityIn.getBlockState().get(ChessboardBlock.FACING)) {
            case UP:

            case DOWN:

            case NORTH:
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180F));
                matrixStackIn.translate(-1.0D, 0.0D, -1.0D);
                break;
            case EAST:
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90F));
                matrixStackIn.translate(-1.0D, 0.0D, 0.0D);
                break;
            case SOUTH:
                break;
            case WEST:
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(270F));
                matrixStackIn.translate(0.0D, 0.0D, -1.0D);
                break;
        }
    }

    private void drawBishop(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn, final double x, final double y, final double z) {
        drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
        drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y + 0.04, z);
    }

    private void drawKnight(int bx, int bz, boolean flip, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn, final double x, final double y, final double z) {
        drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
        drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x + 0.02 * ((flip) ? -1 : 1), y + 0.04, z);
    }

    private void drawRook(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn, final double x, final double y, final double z) {
        drawPiece(0.03f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
    }

    private void drawKing(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn, final double x, final double y, final double z) {
        drawPiece(0.04f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
    }

    private void drawQueen(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn, final double x, final double y, final double z) {
        drawPiece(0.03f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
        drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y + 0.06, z);
    }

    private void drawPawn(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn, final double x, final double y, final double z) {
        drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
    }

    private void drawPiece(float size, int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int overlay, final double x, final double y, final double z) {
        float xOff = (float) (x + 2.75f / 16f + bx * 1.5f / 16f);
        float yOff = (float) (y + 1 + size);
        float zOff = (float) (z + 2.75f / 16f + bz * 1.5f / 16f);

        final float PieceTileSize = 240;
        final float PieceTileBorderSize = 12;
        final float UnitPieceSize = 0.04f;
        float scaledTextureOffset = (2 - UnitPieceSize / size) / 2 * PieceTileBorderSize / PieceTileSize;

        Matrix4f model = matrixStackIn.getLast().getMatrix();

        //bufferbuilder.setTranslation(x + 2.75D/16D + bx*1.5D/16D, y +1+size, z + 2.75D/16D + bz*1.5D/16D);
        float textureScale = 0.5f * size / 0.03f;
        //float textureScale=1;

        //south side [pos z] [parent x]

        bufferbuilder.pos(model, xOff + size, yOff - size, zOff + size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(0, 0, 1).endVertex();
        bufferbuilder.pos(model, xOff + size, yOff + size, zOff + size).tex(1 - scaledTextureOffset, scaledTextureOffset).normal(0, 0, 1).endVertex();
        bufferbuilder.pos(model, xOff - size, yOff + size, zOff + size).tex(scaledTextureOffset, scaledTextureOffset).normal(0, 0, 1).endVertex();
        bufferbuilder.pos(model, xOff - size, yOff - size, zOff + size).tex(scaledTextureOffset, 1 - scaledTextureOffset).normal(0, 0, 1).endVertex();

        //north side [neg z] [parent x]
        bufferbuilder.pos(model, xOff - size, yOff - size, zOff - size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(0, 0, -1).endVertex();
        bufferbuilder.pos(model, xOff - size, yOff + size, zOff - size).tex(1 - scaledTextureOffset, scaledTextureOffset).normal(0, 0, -1).endVertex();
        bufferbuilder.pos(model, xOff + size, yOff + size, zOff - size).tex(scaledTextureOffset, scaledTextureOffset).normal(0, 0, -1).endVertex();
        bufferbuilder.pos(model, xOff + size, yOff - size, zOff - size).tex(scaledTextureOffset, 1 - scaledTextureOffset).normal(0, 0, -1).endVertex();

        //east side [pos x] [parent z]
        bufferbuilder.pos(model, xOff + size, yOff - size, zOff - size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(1, 0, 0).endVertex();
        bufferbuilder.pos(model, xOff + size, yOff + size, zOff - size).tex(1 - scaledTextureOffset, scaledTextureOffset).normal(1, 0, 0).endVertex();
        bufferbuilder.pos(model, xOff + size, yOff + size, zOff + size).tex(scaledTextureOffset, scaledTextureOffset).normal(1, 0, 0).endVertex();
        bufferbuilder.pos(model, xOff + size, yOff - size, zOff + size).tex(scaledTextureOffset, 1 - scaledTextureOffset).normal(1, 0, 0).endVertex();

        //west side [neg x] [parent z]
        bufferbuilder.pos(model, xOff - size, yOff - size, zOff + size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(-1, 0, 0).endVertex();
        bufferbuilder.pos(model, xOff - size, yOff + size, zOff + size).tex(1 - scaledTextureOffset, scaledTextureOffset).normal(-1, 0, 0).endVertex();
        bufferbuilder.pos(model, xOff - size, yOff + size, zOff - size).tex(scaledTextureOffset, scaledTextureOffset).normal(-1, 0, 0).endVertex();
        bufferbuilder.pos(model, xOff - size, yOff - size, zOff - size).tex(scaledTextureOffset, 1 - scaledTextureOffset).normal(-1, 0, 0).endVertex();

        //top [pos y] [parent x & y]
        bufferbuilder.pos(model, xOff + size, yOff + size, zOff - size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(model, xOff - size, yOff + size, zOff - size).tex(1 - scaledTextureOffset, scaledTextureOffset).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(model, xOff - size, yOff + size, zOff + size).tex(scaledTextureOffset, scaledTextureOffset).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(model, xOff + size, yOff + size, zOff + size).tex(scaledTextureOffset, 1 - scaledTextureOffset).normal(0, 1, 0).endVertex();

        //bottom [neg y] [parent x & y]
        bufferbuilder.pos(model, xOff - size, yOff - size, zOff - size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(0, -1, 0).endVertex();
        bufferbuilder.pos(model, xOff + size, yOff - size, zOff - size).tex(1 - scaledTextureOffset, scaledTextureOffset).normal(0, -1, 0).endVertex();
        bufferbuilder.pos(model, xOff + size, yOff - size, zOff + size).tex(scaledTextureOffset, scaledTextureOffset).normal(0, -1, 0).endVertex();
        bufferbuilder.pos(model, xOff - size, yOff - size, zOff + size).tex(scaledTextureOffset, 1 - scaledTextureOffset).normal(0, -1, 0).endVertex();

    }

    /**
     * This renderer is a global renderer.
     * This means that it will always render, even if the player is not able to see it's block.
     * This is useful for rendering larger models or dynamically sized models.
     * The Beacon's beam is also a global renderer
     */
    @Override
    public boolean isGlobalRenderer(final ChessboardTileEntity te) {
        return true;
    }

}
