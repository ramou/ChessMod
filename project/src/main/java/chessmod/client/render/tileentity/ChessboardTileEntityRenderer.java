//package chessmod.client.render.tileentity;
//
//import org.lwjgl.opengl.GL11;
//
//import chessmod.common.dom.model.chess.Point;
//import chessmod.common.dom.model.chess.piece.Piece;
//import chessmod.tileentity.ChessboardTileEntity;
//import com.mojang.blaze3d.platform.GlStateManager;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.BlockModelRenderer;
//import net.minecraft.client.renderer.BufferBuilder;
//import net.minecraft.client.renderer.Tessellator;
//import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import net.minecraft.util.ResourceLocation;
//
//public class ChessboardTileEntityRenderer extends TileEntityRenderer<ChessboardTileEntity> {
//
//	/**
//	 * Render our TileEntity
//	 */
//	@Override
//	public void render(final ChessboardTileEntity tileEntityIn, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder bufferbuilder = tessellator.getBuffer();
//        ResourceLocation black = new ResourceLocation("chessmod", "textures/block/black.png");
//        ResourceLocation white = new ResourceLocation("chessmod", "textures/block/white.png");
//
//        GlStateManager.pushMatrix();
//
//        //RenderHelper.disableStandardItemLighting();
//        GlStateManager.disableCull();
//		if (Minecraft.isAmbientOcclusionEnabled()) {
//			GlStateManager.shadeModel(GL11.GL_SMOOTH);
//		} else {
//			GlStateManager.shadeModel(GL11.GL_FLAT);
//		}
//
//         BlockModelRenderer.enableCache();
//
//         this.bindTexture(black);
//
//         bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
//
//
//         for(int by = 0; by < 8; by++) {
//        	 for(int bx = 0; bx < 8; bx++) {
//        		 Piece piece = tileEntityIn.getBoard().pieceAt(Point.create(bx, by));
//        		 if(piece != null) switch(piece.getCharacter()) {
//	        	 	case 'r':
//	        	 		drawRook(bx, by, bufferbuilder, x, y, z);
//	        	 		break;
//	        	 	case 'n':
//	        	 		drawKnight(bx, by, false, bufferbuilder, x, y, z);
//	        	 		break;
//	        	 	case 'b':
//	        	 		drawBishop(bx, by, bufferbuilder, x, y, z);
//	        	 		break;
//	        	 	case 'q':
//	        	 		drawQueen(bx, by, bufferbuilder, x, y, z);
//	        	 		break;
//	        	 	case 'k':
//	        	 		drawKing(bx, by, bufferbuilder, x, y, z);
//	        	 		break;
//	        	 	case 'p':
//	        	 		drawPawn(bx, by, bufferbuilder, x, y, z);
//	        	 		break;
//	        	 	default:
//	        	 }
//        	 }
//         }
//
//         bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
//         tessellator.draw();
//
//         bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
//         this.bindTexture(white);
//
//         for(int by = 0; by < 8; by++) {
//        	 for(int bx = 0; bx < 8; bx++) {
//        		 Piece piece = tileEntityIn.getBoard().pieceAt(Point.create(bx, by));
//        		 if(piece != null) switch(tileEntityIn.getBoard().pieceAt(Point.create(bx, by)).getCharacter()) {
//	        	 	case 'R':
//	        	 		drawRook(bx, by, bufferbuilder, x, y, z);
//	        	 		break;
//	        	 	case 'N':
//	        	 		drawKnight(bx, by, true, bufferbuilder, x, y, z);
//	        	 		break;
//	        	 	case 'B':
//	        	 		drawBishop(bx, by, bufferbuilder, x, y, z);
//	        	 		break;
//	        	 	case 'Q':
//	        	 		drawQueen(bx, by, bufferbuilder, x, y, z);
//	        	 		break;
//	        	 	case 'K':
//	        	 		drawKing(bx, by, bufferbuilder, x, y, z);
//	        	 		break;
//	        	 	case 'P':
//	        	 		drawPawn(bx, by, bufferbuilder, x, y, z);
//	        	 		break;
//	        	 	default:
//	        	 }
//        	 }
//         }
//
//         bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
//         tessellator.draw();
//
//
//         BlockModelRenderer.disableCache();
//
//         GlStateManager.enableCull();
//
//         //RenderHelper.enableStandardItemLighting();
//         GlStateManager.popMatrix();
//	}
//
//
//	private void drawBishop(int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
//		drawPiece(0.02f, bx, bz, bufferbuilder, x, y, z);
//		drawPiece(0.02f, bx, bz, bufferbuilder, x, y+0.04, z);
//	}
//
//	private void drawKnight(int bx, int bz, boolean flip, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
//		drawPiece(0.02f, bx, bz, bufferbuilder, x, y, z);
//		drawPiece(0.02f, bx, bz, bufferbuilder, x+0.02*((flip)?-1:1), y+0.04, z);
//	}
//
//	private void drawRook(int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
//		drawPiece(0.03f, bx, bz, bufferbuilder, x, y, z);
//	}
//
//	private void drawKing(int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
//		drawPiece(0.04f, bx, bz, bufferbuilder, x, y, z);
//	}
//
//	private void drawQueen(int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
//		drawPiece(0.03f, bx, bz, bufferbuilder, x, y, z);
//		drawPiece(0.02f, bx, bz, bufferbuilder, x, y+0.06, z);
//	}
//
//	private void drawPawn(int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
//		drawPiece(0.02f, bx, bz, bufferbuilder, x, y, z);
//	}
//
//	private void drawPiece(float size, int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
//         bufferbuilder.setTranslation(x + 2.75D/16D + bx*1.5D/16D, y +1+size, z + 2.75D/16D + bz*1.5D/16D);
//
//         double textureScale = 0.5*size/0.03;
//
//         //south side [pos z] [parent x]
//         bufferbuilder.pos(+size, -size, +size).tex(textureScale,textureScale).normal(0, 0, 1).endVertex();
//         bufferbuilder.pos(+size, +size, +size).tex(textureScale,0).normal(0, 0, 1).endVertex();
//         bufferbuilder.pos(-size, +size, +size).tex(0,0).normal(0, 0, 1).endVertex();
//         bufferbuilder.pos(-size, -size, +size).tex(0,textureScale).normal(0, 0, 1).endVertex();
//
//         //north side [neg z] [parent x]
//         bufferbuilder.pos(-size, -size, -size).tex(textureScale,textureScale).normal(0, 0, -1).endVertex();
//         bufferbuilder.pos(-size, +size, -size).tex(textureScale,0).normal(0, 0, -1).endVertex();
//         bufferbuilder.pos(+size, +size, -size).tex(0,0).normal(0, 0, -1).endVertex();
//         bufferbuilder.pos(+size, -size, -size).tex(0,textureScale).normal(0, 0, -1).endVertex();
//
//         //east side [pos x] [parent z]
//         bufferbuilder.pos(+size, -size, -size).tex(textureScale,textureScale).normal(1, 0, 0).endVertex();
//         bufferbuilder.pos(+size, +size, -size).tex(textureScale,textureScale).normal(1, 0, 0).endVertex();
//         bufferbuilder.pos(+size, +size, +size).tex(0,0).normal(1, 0, 0).endVertex();
//         bufferbuilder.pos(+size, -size, +size).tex(0,textureScale).normal(1, 0, 0).endVertex();
//
//         //west side [neg x] [parent z]
//         bufferbuilder.pos(-size, -size, +size).tex(textureScale,textureScale).normal(-1, 0, 0).endVertex();
//         bufferbuilder.pos(-size, +size, +size).tex(textureScale,0).normal(-1, 0, 0).endVertex();
//         bufferbuilder.pos(-size, +size, -size).tex(0,0).normal(-1, 0, 0).endVertex();
//         bufferbuilder.pos(-size, -size, -size).tex(0,textureScale).normal(-1, 0, 0).endVertex();
//
//         //top [pos y] [parent x & y]
//         bufferbuilder.pos(+size, +size, -size).tex(textureScale,textureScale).normal(0, 1, 0).endVertex();
//         bufferbuilder.pos(-size, +size, -size).tex(textureScale,0).normal(0, 1, 0).endVertex();
//         bufferbuilder.pos(-size, +size, +size).tex(0,0).normal(0, 1, 0).endVertex();
//         bufferbuilder.pos(+size, +size, +size).tex(0,textureScale).normal(0, 1, 0).endVertex();
//
//         //bottom [neg y] [parent x & y]
//         bufferbuilder.pos(-size, -size, -size).tex(textureScale,textureScale).normal(0, -1, 0).endVertex();
//         bufferbuilder.pos(+size, -size, -size).tex(textureScale,0).normal(0, -1, 0).endVertex();
//         bufferbuilder.pos(+size, -size, +size).tex(0,0).normal(0, -1, 0).endVertex();
//         bufferbuilder.pos(-size, -size, +size).tex(0,textureScale).normal(0, -1, 0).endVertex();
//	}
//}
