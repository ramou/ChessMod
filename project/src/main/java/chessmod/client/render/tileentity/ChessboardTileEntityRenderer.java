package chessmod.client.render.tileentity;

import chessmod.block.ChessboardBlock;
import chessmod.common.Point2f;
import chessmod.common.dom.model.chess.Side;
import chessmod.tileentity.GoldChessBoardTileEntity;
import net.minecraft.client.renderer.*;
import org.lwjgl.opengl.GL11;

import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.tileentity.ChessboardTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class ChessboardTileEntityRenderer extends TileEntityRenderer<ChessboardTileEntity> {


    public void draw2DRect(BufferBuilder bufferbuilder, Point2f p1, Point2f p2) {
        bufferbuilder.pos(p2.x, 1.001, p1.y).tex(1, 1).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(p1.x, 1.001, p1.y).tex(1, 0).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(p1.x, 1.001, p2.y).tex(0, 0).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(p2.x, 1.001, p2.y).tex(0, 1).normal(0, 1, 0).endVertex();
    }

    protected void showTurnColor(BufferBuilder bufferbuilder, Side s) {

        if (s.equals(Side.WHITE)) {
            this.bindTexture(new ResourceLocation("chessmod", "textures/gui/shadewhite.png"));
        } else {
            this.bindTexture(new ResourceLocation("chessmod", "textures/gui/shadeblack.png"));
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
        draw2DRect(bufferbuilder, p1, p2);
        //left
        p2 = new Point2f(x1Inner, z2Outter);
        draw2DRect(bufferbuilder, p1, p2);

        //right
        p1 = new Point2f(x2Inner, z1Outter);
        p2 = new Point2f(x2Outter, z2Outter);
        draw2DRect(bufferbuilder, p1, p2);

        //bottom
        p1 = new Point2f(x1Outter, z2Inner);
        draw2DRect(bufferbuilder, p1, p2);
    }

    /**
     * Render our TileEntity
     */
    @Override
    public void render(final ChessboardTileEntity tileEntityIn, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder bufferbuilder = tessellator.getBuffer();
        ResourceLocation black = new ResourceLocation("chessmod", "textures/block/black.png");
        ResourceLocation white = new ResourceLocation("chessmod", "textures/block/white.png");

        GlStateManager.pushMatrix();

        GlStateManager.disableCull();
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        BlockModelRenderer.enableCache();

        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        bufferbuilder.setTranslation(x, y, z);



        if (tileEntityIn instanceof GoldChessBoardTileEntity) {
            //Draw current-turn indicator:
            showTurnColor(bufferbuilder, tileEntityIn.getBoard().getCurrentPlayer());
            bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
            tessellator.draw();
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
        }

        rotateForBoardFacing(tileEntityIn, x,  y,  z);


        this.bindTexture(black);
        for (int by = 0; by < 8; by++) {
            for (int bx = 0; bx < 8; bx++) {
                Piece piece = tileEntityIn.getBoard().pieceAt(Point.create(bx, by));
                if (piece != null) switch (piece.getCharacter()) {
                    case 'r':
                        drawRook(bx, by, bufferbuilder);
                        break;
                    case 'n':
                        drawKnight(bx, by, false, bufferbuilder);
                        break;
                    case 'b':
                        drawBishop(bx, by, bufferbuilder);
                        break;
                    case 'q':
                        drawQueen(bx, by, bufferbuilder);
                        break;
                    case 'k':
                        drawKing(bx, by, bufferbuilder);
                        break;
                    case 'p':
                        drawPawn(bx, by, bufferbuilder);
                        break;
                    default:
                }
            }
        }

        tessellator.draw();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();

        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        rotateForBoardFacing(tileEntityIn, x,  y,  z);
        this.bindTexture(white);

        for (int by = 0; by < 8; by++) {
            for (int bx = 0; bx < 8; bx++) {

                Piece piece = tileEntityIn.getBoard().pieceAt(Point.create(bx, by));
                if (piece != null) switch (piece.getCharacter()) {
                    case 'R':
                        drawRook(bx, by, bufferbuilder);
                        break;
                    case 'N':
                        drawKnight(bx, by, true, bufferbuilder);
                        break;
                    case 'B':
                        drawBishop(bx, by, bufferbuilder);
                        break;
                    case 'Q':
                        drawQueen(bx, by, bufferbuilder);
                        break;
                    case 'K':
                        drawKing(bx, by, bufferbuilder);
                        break;
                    case 'P':
                        drawPawn(bx, by, bufferbuilder);
                        break;
                    default:
                }
            }
        }

        bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();

        BlockModelRenderer.disableCache();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    private void rotateForBoardFacing(ChessboardTileEntity tileEntity, double x, double y, double z) {

        GlStateManager.translated(x, y, z);
        switch (tileEntity.getBlockState().get(ChessboardBlock.FACING)) {
            case UP: //We should never have this cse, but it'll look like SOUTH if we do
            case DOWN:
                break;
            case NORTH:
                GlStateManager.rotatef(180,0, 1, 0);
                GlStateManager.translated(-1, 0, -1);
                break;
            case EAST:
                GlStateManager.rotatef(90,0, 1, 0);
                GlStateManager.translated(-1, 0, 0);
                break;
            case SOUTH:
            break;
            case WEST:
                GlStateManager.rotatef(270,0, 1, 0);
                GlStateManager.translated(0, 0, -1);
                break;
        }
    }

    private void drawBishop(int bx, int bz, BufferBuilder bufferbuilder) {
        drawPiece(0.02f, bx, bz, bufferbuilder, 0, 0, 0);
        drawPiece(0.02f, bx, bz, bufferbuilder, 0, 0.04, 0);
    }

    private void drawKnight(int bx, int bz, boolean flip, BufferBuilder bufferbuilder) {
        float x = flip ? 0.01f : -0.01f;
        float z = flip ? -0.01f : 0.01f;

        drawPiece(0.02f, bx, bz, bufferbuilder, +x, 0, +z);
        drawPiece(0.02f, bx, bz, bufferbuilder, -x, 0.04, -z);
    }

    private void drawRook(int bx, int bz, BufferBuilder bufferbuilder) {
        drawPiece(0.03f, bx, bz, bufferbuilder, 0, 0, 0);
    }

    private void drawKing(int bx, int bz, BufferBuilder bufferbuilder) {
        drawPiece(0.04f, bx, bz, bufferbuilder, 0, 0, 0);
    }

    private void drawQueen(int bx, int bz, BufferBuilder bufferbuilder) {
        drawPiece(0.03f, bx, bz, bufferbuilder, 0, 0, 0);
        drawPiece(0.02f, bx, bz, bufferbuilder, 0, 0.06, 0);
    }

    private void drawPawn(int bx, int bz, BufferBuilder bufferbuilder) {
        drawPiece(0.02f, bx, bz, bufferbuilder, 0, 0, 0);
    }

    private void drawPiece(float size, int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
        float xOff = (float) (x + 2.75f / 16f + bx * 1.5f / 16f);
        float yOff = (float) (y + 1 + size);
        float zOff = (float) (z + 2.75f / 16f + bz * 1.5f / 16f);

        final float PieceTileSize = 240;
        final float PieceTileBorderSize = 12;
        final float UnitPieceSize = 0.04f;
        float scaledTextureOffset = (2 - UnitPieceSize / size) / 2 * PieceTileBorderSize / PieceTileSize;

        //south side [pos z] [parent x]
        bufferbuilder.pos(xOff + size, yOff - size, zOff + size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(0, 0, 1).endVertex();
        bufferbuilder.pos(xOff + size, yOff + size, zOff + size).tex(1 - scaledTextureOffset, scaledTextureOffset).normal(0, 0, 1).endVertex();
        bufferbuilder.pos(xOff - size, yOff + size, zOff + size).tex(scaledTextureOffset, scaledTextureOffset).normal(0, 0, 1).endVertex();
        bufferbuilder.pos(xOff - size, yOff - size, zOff + size).tex(scaledTextureOffset, 1 - scaledTextureOffset).normal(0, 0, 1).endVertex();

        //north side [neg z] [parent x]
        bufferbuilder.pos(xOff - size, yOff - size, zOff - size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(0, 0, -1).endVertex();
        bufferbuilder.pos(xOff - size, yOff + size, zOff - size).tex(1 - scaledTextureOffset, scaledTextureOffset).normal(0, 0, -1).endVertex();
        bufferbuilder.pos(xOff + size, yOff + size, zOff - size).tex(scaledTextureOffset, scaledTextureOffset).normal(0, 0, -1).endVertex();
        bufferbuilder.pos(xOff + size, yOff - size, zOff - size).tex(scaledTextureOffset, 1 - scaledTextureOffset).normal(0, 0, -1).endVertex();

        //east side [pos x] [parent z]
        bufferbuilder.pos(xOff + size, yOff - size, zOff - size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(1, 0, 0).endVertex();
        bufferbuilder.pos(xOff + size, yOff + size, zOff - size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(1, 0, 0).endVertex();
        bufferbuilder.pos(xOff + size, yOff + size, zOff + size).tex(scaledTextureOffset, scaledTextureOffset).normal(1, 0, 0).endVertex();
        bufferbuilder.pos(xOff + size, yOff - size, zOff + size).tex(scaledTextureOffset, 1 - scaledTextureOffset).normal(1, 0, 0).endVertex();

        //west side [neg x] [parent z]
        bufferbuilder.pos(xOff - size, yOff - size, zOff + size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(-1, 0, 0).endVertex();
        bufferbuilder.pos(xOff - size, yOff + size, zOff + size).tex(1 - scaledTextureOffset, scaledTextureOffset).normal(-1, 0, 0).endVertex();
        bufferbuilder.pos(xOff - size, yOff + size, zOff - size).tex(scaledTextureOffset, scaledTextureOffset).normal(-1, 0, 0).endVertex();
        bufferbuilder.pos(xOff - size, yOff - size, zOff - size).tex(scaledTextureOffset, 1 - scaledTextureOffset).normal(-1, 0, 0).endVertex();

        //top [pos y] [parent x & y]
        bufferbuilder.pos(xOff + size, yOff + size, zOff - size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(xOff - size, yOff + size, zOff - size).tex(1 - scaledTextureOffset, scaledTextureOffset).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(xOff - size, yOff + size, zOff + size).tex(scaledTextureOffset, scaledTextureOffset).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(xOff + size, yOff + size, zOff + size).tex(scaledTextureOffset, 1 - scaledTextureOffset).normal(0, 1, 0).endVertex();

        //bottom [neg y] [parent x & y]
        bufferbuilder.pos(xOff - size, yOff - size, zOff - size).tex(1 - scaledTextureOffset, 1 - scaledTextureOffset).normal(0, -1, 0).endVertex();
        bufferbuilder.pos(xOff + size, yOff - size, zOff - size).tex(1 - scaledTextureOffset, scaledTextureOffset).normal(0, -1, 0).endVertex();
        bufferbuilder.pos(xOff + size, yOff - size, zOff + size).tex(scaledTextureOffset, scaledTextureOffset).normal(0, -1, 0).endVertex();
        bufferbuilder.pos(xOff - size, yOff - size, zOff + size).tex(scaledTextureOffset, 1 - scaledTextureOffset).normal(0, -1, 0).endVertex();

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
