package chessmod.client.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import chessmod.ChessMod;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.tileentity.ChessboardTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

public class ChessboardTileEntityRenderer extends TileEntityRenderer<ChessboardTileEntity> {
	public static final ResourceLocation black = new ResourceLocation("chessmod", "textures/block/black.png");
	public static final ResourceLocation white = new ResourceLocation("chessmod", "textures/block/white.png");
	public static final RenderType BLACK_PIECE;
	public static final RenderType WHITE_PIECE;
	static {
		RenderType.State glState = RenderType.State.getBuilder().texture(new RenderState.TextureState(black, false, true)).diffuseLighting(new RenderState.DiffuseLightingState(true)).build(true);
		BLACK_PIECE = RenderType.makeType(ChessMod.MODID + "piece", DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 64, glState);
		glState = RenderType.State.getBuilder().texture(new RenderState.TextureState(white, false, true)).diffuseLighting(new RenderState.DiffuseLightingState(true)).build(true);
		WHITE_PIECE = RenderType.makeType(ChessMod.MODID + "piece", DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 64, glState);
	}

	public ChessboardTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
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
        //Minecraft.getInstance().textureManager.bindTexture(black);
        matrixStackIn.push();
        IVertexBuilder bufferbuilder = b.getBuffer(BLACK_PIECE);

        
        //Minecraft.getInstance().getTextureManager().bindTexture(black);

         for(int by = 0; by < 8; by++) { 
        	 for(int bx = 0; bx < 8; bx++) {
        		 Piece piece = tileEntityIn.getBoard().pieceAt(Point.create(bx, by));
        		 if(piece != null) switch(piece.getCharacter()) {
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
         bufferbuilder = b.getBuffer(WHITE_PIECE);
         //Minecraft.getInstance().getTextureManager().bindTexture(white);
         
         for(int by = 0; by < 8; by++) { 
        	 for(int bx = 0; bx < 8; bx++) {
        		 Piece piece = tileEntityIn.getBoard().pieceAt(Point.create(bx, by));
        		 if(piece != null) switch(tileEntityIn.getBoard().pieceAt(Point.create(bx, by)).getCharacter()) {
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
	
	
	private void drawBishop(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn, final double x, final double y, final double z) {
		drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);        
		drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y+0.04, z);
	}

	private void drawKnight(int bx, int bz, boolean flip, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn, final double x, final double y, final double z) {
		drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);        
		drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x+0.02*((flip)?-1:1), y+0.04, z);
	}
	
	private void drawRook(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn, final double x, final double y, final double z) {
		drawPiece(0.03f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);        
	}

	private void drawKing(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn, final double x, final double y, final double z) {
		drawPiece(0.04f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);        
	}
	
	private void drawQueen(int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn, final double x, final double y, final double z) {
		drawPiece(0.03f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
		drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y+0.06, z);
	}
	
	private void drawPawn(int bx, int bz,MatrixStack matrixStackIn,  IVertexBuilder bufferbuilder, int combinedLightIn, int combinedOverlayIn, final double x, final double y, final double z) {
		drawPiece(0.02f, bx, bz, matrixStackIn, bufferbuilder, combinedLightIn, combinedOverlayIn, x, y, z);
	}

	private void drawPiece(float size, int bx, int bz, MatrixStack matrixStackIn, IVertexBuilder bufferbuilder, int combinedLightIn, int overlay, final double x, final double y, final double z) {
		float xOff = (float)(x + 2.75f/16f + bx*1.5f/16f);
		float yOff = (float)(y +1+size);
		float zOff = (float)(z + 2.75f/16f + bz*1.5f/16f);

		
		Matrix4f model = matrixStackIn.getLast().getMatrix();

		//bufferbuilder.setTranslation(x + 2.75D/16D + bx*1.5D/16D, y +1+size, z + 2.75D/16D + bz*1.5D/16D);
        float textureScale = 0.5f*size/0.03f;
		//float textureScale=1;
         
        //south side [pos z] [parent x]

        bufferbuilder.pos(model, xOff+size, yOff-size, zOff+size).tex(textureScale,textureScale).normal(0, 0, 1).endVertex();
        bufferbuilder.pos(model, xOff+size, yOff+size, zOff+size).tex(textureScale,0).normal(0, 0, 1).endVertex();
        bufferbuilder.pos(model, xOff-size, yOff+size, zOff+size).tex(0,0).normal(0, 0, 1).endVertex();
        bufferbuilder.pos(model, xOff-size, yOff-size, zOff+size).tex(0,textureScale).normal(0, 0, 1).endVertex();

        //north side [neg z] [parent x]
        bufferbuilder.pos(model, xOff-size, yOff-size, zOff-size).tex(textureScale,textureScale).normal(0, 0, -1).endVertex();
        bufferbuilder.pos(model, xOff-size, yOff+size, zOff-size).tex(textureScale,0).normal(0, 0, -1).endVertex();
        bufferbuilder.pos(model, xOff+size, yOff+size, zOff-size).tex(0,0).normal(0, 0, -1).endVertex();
        bufferbuilder.pos(model, xOff+size, yOff-size, zOff-size).tex(0,textureScale).normal(0, 0, -1).endVertex();

        //east side [pos x] [parent z]
        bufferbuilder.pos(model, xOff+size, yOff-size, zOff-size).tex(textureScale,textureScale).normal(1, 0, 0).endVertex();
        bufferbuilder.pos(model, xOff+size, yOff+size, zOff-size).tex(textureScale,0).normal(1, 0, 0).endVertex();
        bufferbuilder.pos(model, xOff+size, yOff+size, zOff+size).tex(0,0).normal(1, 0, 0).endVertex();
        bufferbuilder.pos(model, xOff+size, yOff-size, zOff+size).tex(0,textureScale).normal(1, 0, 0).endVertex();

        //west side [neg x] [parent z]
        bufferbuilder.pos(model, xOff-size, yOff-size, zOff+size).tex(textureScale,textureScale).normal(-1, 0, 0).endVertex();
        bufferbuilder.pos(model, xOff-size, yOff+size, zOff+size).tex(textureScale,0).normal(-1, 0, 0).endVertex();
        bufferbuilder.pos(model, xOff-size, yOff+size, zOff-size).tex(0,0).normal(-1, 0, 0).endVertex();
        bufferbuilder.pos(model, xOff-size, yOff-size, zOff-size).tex(0,textureScale).normal(-1, 0, 0).endVertex();

        //top [pos y] [parent x & y]
        bufferbuilder.pos(model, xOff+size, yOff+size, zOff-size).tex(textureScale,textureScale).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(model, xOff-size, yOff+size, zOff-size).tex(textureScale,0).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(model, xOff-size, yOff+size, zOff+size).tex(0,0).normal(0, 1, 0).endVertex();
        bufferbuilder.pos(model, xOff+size, yOff+size, zOff+size).tex(0,textureScale).normal(0, 1, 0).endVertex();

        //bottom [neg y] [parent x & y]
        bufferbuilder.pos(model, xOff-size, yOff-size, zOff-size).tex(textureScale,textureScale).normal(0, -1, 0).endVertex();
        bufferbuilder.pos(model, xOff+size, yOff-size, zOff-size).tex(textureScale,0).normal(0, -1, 0).endVertex();
        bufferbuilder.pos(model, xOff+size, yOff-size, zOff+size).tex(0,0).normal(0, -1, 0).endVertex();
        bufferbuilder.pos(model, xOff-size, yOff-size, zOff+size).tex(0,textureScale).normal(0, -1, 0).endVertex();
         
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
