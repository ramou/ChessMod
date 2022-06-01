package chessmod.client.render.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.blockentity.GoldChessboardBlockEntity;
import chessmod.common.Point2f;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.piece.Piece;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

public class ChessboardBlockEntityRenderer implements BlockEntityRenderer<ChessboardBlockEntity>  {
	
	@SuppressWarnings("deprecation")
	public static final Material BLACK_PIECE_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("chessmod", "textures/block/black.png"));
	
	@SuppressWarnings("deprecation")
	public static final Material WHITE_PIECE_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("chessmod", "textures/block/white.png"));



	public void draw2DRect(VertexConsumer bufferSource, PoseStack poseStackIn, Point2f p1, Point2f p2, float r, float g, float b, float a) {
		Matrix4f model = poseStackIn.last().pose();
		bufferSource.vertex(model, p2.x, 1.001f, p1.y).color(r, g, b, a).endVertex();
		bufferSource.vertex(model, p1.x, 1.001f, p1.y).color(r, g, b, a).endVertex();
		bufferSource.vertex(model, p1.x, 1.001f, p2.y).color(r, g, b, a).endVertex();
		bufferSource.vertex(model, p2.x, 1.001f, p2.y).color(r, g, b, a).endVertex();
	}
	
	protected void showTurnColor(MultiBufferSource pBufferSource, PoseStack poseStackIn, Side s) {
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();

		float c = 0;
		
		VertexConsumer bufferSource  = pBufferSource.getBuffer(RenderType.LINE_STRIP);
		
		if(s.equals(Side.WHITE)) c = 1;

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
		draw2DRect(bufferSource, poseStackIn, p1, p2, c, c, c, 1f);

		//left
		p2 = new Point2f(x1Inner, z2Outter);
		draw2DRect(bufferSource, poseStackIn, p1, p2, c, c, c, 1f);

		//right
		p1 = new Point2f(x2Inner, z1Outter);
		p2 = new Point2f(x2Outter, z2Outter);
		draw2DRect(bufferSource,poseStackIn, p1, p2, c, c, c, 1f);

		//bottom
		p1 = new Point2f(x1Outter, z2Inner);
		draw2DRect(bufferSource, poseStackIn, p1, p2, c, c, c, 1f);

		RenderSystem.enableTexture();		
		RenderSystem.disableBlend();
	}


	/**
	 * Render our BlockEntity
	 */
	@Override
	public void render(ChessboardBlockEntity pBlockEntity, float partialTicks, PoseStack poseStackIn, 
			MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        
		
		
		//TODO: re-add showing player turn!

		
		
		
        poseStackIn.pushPose();
        
		if(pBlockEntity instanceof GoldChessboardBlockEntity) {
	        //Draw current-turn indicator:
	        showTurnColor(pBufferSource, poseStackIn, pBlockEntity.getBoard().getCurrentPlayer());
		}
		poseStackIn.popPose();
		

		RenderSystem.disableCull();

        RenderSystem.enableTexture();
		
        poseStackIn.pushPose();

        VertexConsumer bufferSource  = BLACK_PIECE_TEXTURE.buffer(pBufferSource, RenderType::entitySolid);
        for(int by = 0; by < 8; by++) { 
        	 for(int bx = 0; bx < 8; bx++) {
        		 Piece piece = pBlockEntity.getBoard().pieceAt(Point.create(bx, by));
        		 if(piece != null) switch(piece.getCharacter()) {
	        	 	case 'r':
	        	 		drawRook(bx, by, bufferSource);
	        	 		break;
	        	 	case 'n':
	        	 		drawKnight(bx, by, false, bufferSource);
	        	 		break;
	        	 	case 'b':
	        	 		drawBishop(bx, by, bufferSource);
	        	 		break;
	        	 	case 'q':
	        	 		drawQueen(bx, by, bufferSource);
	        	 		break;
	        	 	case 'k':
	        	 		drawKing(bx, by, bufferSource);
	        	 		break;
	        	 	case 'p':
	        	 		drawPawn(bx, by, bufferSource);
	        	 		break;
	        	 	default:
	        	 }
        	 }
         }
         poseStackIn.popPose();

         poseStackIn.pushPose();
         bufferSource  = WHITE_PIECE_TEXTURE.buffer(pBufferSource, RenderType::entitySolid);
         
         for(int by = 0; by < 8; by++) { 
        	 for(int bx = 0; bx < 8; bx++) {
        		 Piece piece = pBlockEntity.getBoard().pieceAt(Point.create(bx, by));
        		 if(piece != null) switch(pBlockEntity.getBoard().pieceAt(Point.create(bx, by)).getCharacter()) {
	        	 	case 'R':
	        	 		drawRook(bx, by, bufferSource);
	        	 		break;
	        	 	case 'N':
	        	 		drawKnight(bx, by, true, bufferSource);
	        	 		break;
	        	 	case 'B':
	        	 		drawBishop(bx, by, bufferSource);
	        	 		break;
	        	 	case 'Q':
	        	 		drawQueen(bx, by, bufferSource);
	        	 		break;
	        	 	case 'K':
	        	 		drawKing(bx, by, bufferSource);
	        	 		break;
	        	 	case 'P':
	        	 		drawPawn(bx, by, bufferSource);
	        	 		break;
	        	 	default:
	        	 }
        	 }
         }

         poseStackIn.popPose();
         RenderSystem.enableCull();

	}
	
	
	private void drawBishop(int bx, int bz, VertexConsumer bufferSource) {
		drawPiece(0.02f, bx, bz, bufferSource, 0, 0, 0);        
		drawPiece(0.02f, bx, bz, bufferSource, 0, 0.04, 0);
	}

	private void drawKnight(int bx, int bz, boolean flip, VertexConsumer bufferSource) {
		float x = flip?0.01f:-0.01f;
		float z = flip?-0.01f:0.01f;
		
		drawPiece(0.02f, bx, bz, bufferSource, x, 0, z);        
		drawPiece(0.02f, bx, bz, bufferSource, -x, 0.04, -z);	
	}
	
	private void drawRook(int bx, int bz, VertexConsumer bufferSource) {
		drawPiece(0.03f, bx, bz, bufferSource, 0, 0, 0);        
	}

	private void drawKing(int bx, int bz, VertexConsumer bufferSource) {
		drawPiece(0.04f, bx, bz, bufferSource, 0, 0, 0);        
	}
	
	private void drawQueen(int bx, int bz, VertexConsumer bufferSource) {
		drawPiece(0.03f, bx, bz, bufferSource, 0, 0, 0);
		drawPiece(0.02f, bx, bz, bufferSource, 0, 0.06, 0);
	}
	
	private void drawPawn(int bx, int bz, VertexConsumer bufferSource) {
		drawPiece(0.02f, bx, bz, bufferSource, 0, 0, 0);
	}

	private void drawPiece(float size, int bx, int bz, VertexConsumer bufferSource, final double x, final double y, final double z) {
		float xOff = (float)(x + 2.75f/16f + bx*1.5f/16f);
		float yOff = (float)(y +1+size);
		float zOff = (float)(z + 2.75f/16f + bz*1.5f/16f);

		//south side [pos z] [parent x]
        bufferSource.vertex(xOff+size, yOff-size, zOff+size);
        bufferSource.vertex(xOff+size, yOff+size, zOff+size);
        bufferSource.vertex(xOff-size, yOff+size, zOff+size);
        bufferSource.vertex(xOff-size, yOff-size, zOff+size);

        //north side [neg z] [parent x]
        bufferSource.vertex(xOff-size, yOff-size, zOff-size);
        bufferSource.vertex(xOff-size, yOff+size, zOff-size);
        bufferSource.vertex(xOff+size, yOff+size, zOff-size);
        bufferSource.vertex(xOff+size, yOff-size, zOff-size);

        //east side [pos x] [parent z]
        bufferSource.vertex(xOff+size, yOff-size, zOff-size);
        bufferSource.vertex(xOff+size, yOff+size, zOff-size);
        bufferSource.vertex(xOff+size, yOff+size, zOff+size);
        bufferSource.vertex(xOff+size, yOff-size, zOff+size);

        //west side [neg x] [parent z]
        bufferSource.vertex(xOff-size, yOff-size, zOff+size);
        bufferSource.vertex(xOff-size, yOff+size, zOff+size);
        bufferSource.vertex(xOff-size, yOff+size, zOff-size);
        bufferSource.vertex(xOff-size, yOff-size, zOff-size);

        //top [pos y] [parent x & y]
        bufferSource.vertex(xOff+size, yOff+size, zOff-size);
        bufferSource.vertex(xOff-size, yOff+size, zOff-size);
        bufferSource.vertex(xOff-size, yOff+size, zOff+size);
        bufferSource.vertex(xOff+size, yOff+size, zOff+size);

        //bottom [neg y] [parent x & y]
        bufferSource.vertex(xOff-size, yOff-size, zOff-size);
        bufferSource.vertex(xOff+size, yOff-size, zOff-size);
        bufferSource.vertex(xOff+size, yOff-size, zOff+size);
        bufferSource.vertex(xOff-size, yOff-size, zOff+size);
         
	}	
	
}
