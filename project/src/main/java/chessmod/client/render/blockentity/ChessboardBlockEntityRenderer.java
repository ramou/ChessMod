package chessmod.client.render.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import chessmod.block.ChessboardBlock;
import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.blockentity.GoldChessboardBlockEntity;
import chessmod.client.gui.entity.ChessboardGUI;
import chessmod.common.Point2f;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.setup.Registration;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;

public class ChessboardBlockEntityRenderer implements BlockEntityRenderer<ChessboardBlockEntity> {
	public static final ResourceLocation black = new ResourceLocation("chessmod", "textures/block/black.png");
	public static final ResourceLocation white = new ResourceLocation("chessmod", "textures/block/white.png");

    public ChessboardBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    public static void register() {
    	BlockEntityRenderers.register(Registration.WOOD_CHESSBOARD_BE.get(), ChessboardBlockEntityRenderer::new);
    	BlockEntityRenderers.register(Registration.GOLD_CHESSBOARD_BE.get(), ChessboardBlockEntityRenderer::new);
		BlockEntityRenderers.register(Registration.AI_CHESSBOARD_BE.get(), ChessboardBlockEntityRenderer::new);
		BlockEntityRenderers.register(Registration.CHESSES_CHESSBOARD_BE.get(), ChessboardBlockEntityRenderer::new);
		BlockEntityRenderers.register(Registration.PUZZLE_CHESSBOARD_BE.get(), ChessboardBlockEntityRenderer::new);

    }

	public void draw2DRect(VertexConsumer  bufferbuilder, PoseStack pPoseStack, Point2f p1, Point2f p2, float r, float g, float b, float a, int pPackedLight, int pPackedOverlay) {
		Matrix4f model = pPoseStack.last().pose();
		Matrix3f matrix3f = pPoseStack.last().normal();

		bufferbuilder.vertex(model, p2.x, 1.001f, p1.y).color(r, g, b, a).uv(0, 0).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0, 1, 0).endVertex();
		bufferbuilder.vertex(model, p1.x, 1.001f, p1.y).color(r, g, b, a).uv(0, 0).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0, 1, 0).endVertex();
		bufferbuilder.vertex(model, p1.x, 1.001f, p2.y).color(r, g, b, a).uv(0, 0).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0, 1, 0).endVertex();
		bufferbuilder.vertex(model, p2.x, 1.001f, p2.y).color(r, g, b, a).uv(0, 0).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0, 1, 0).endVertex();
	}
	
	protected void showTurnColor(MultiBufferSource pBufferSource, PoseStack pPoseStack, Side s, int pPackedLight, int pPackedOverlay) {
		//It does not seem to care what the texture is for this...
		VertexConsumer bufferbuilder = pBufferSource.getBuffer(RenderType.entitySolid(ChessboardGUI.WHITE));;

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
		draw2DRect(bufferbuilder,pPoseStack, p1, p2, c, c, c, 1f, pPackedLight, pPackedOverlay);

		//bottom
		p1 = new Point2f(x1Outter, z2Inner);
		draw2DRect(bufferbuilder, pPoseStack, p1, p2, c, c, c, 1f, pPackedLight, pPackedOverlay);

		pPoseStack.popPose();
	}
	
	
	/**
	 * Render our BlockEntity
	 */
	@Override
	public void render(ChessboardBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
			MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

		if(pBlockEntity instanceof GoldChessboardBlockEntity) {
	        //Draw current-turn indicator:
	        showTurnColor(pBufferSource, pPoseStack, pBlockEntity.getBoard().getCurrentPlayer(), pPackedLight, pPackedOverlay);
		}

        pPoseStack.pushPose();
        rotateForBoardFacing(pBlockEntity, pPoseStack);
        VertexConsumer bufferbuilder = pBufferSource.getBuffer(RenderType.entityCutout(black));
        RenderSystem.setShaderTexture(0, black);
         for(int by = 0; by < 8; by++) { 
        	 for(int bx = 0; bx < 8; bx++) {
        		 Piece piece = pBlockEntity.getBoard().pieceAt(Point.create(bx, by));
        		 if(piece != null) switch(piece.getCharacter()) {
	        	 	case 'r':
	        	 		drawRook(bx, by, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay);
	        	 		break;
	        	 	case 'n':
	        	 		drawKnight(bx, by, false, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay);
	        	 		break;
	        	 	case 'b':
	        	 		drawBishop(bx, by, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay);
	        	 		break;
	        	 	case 'q':
	        	 		drawQueen(bx, by, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay);
	        	 		break;
	        	 	case 'k':
	        	 		drawKing(bx, by, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay);
	        	 		break;
	        	 	case 'p':
	        	 		drawPawn(bx, by, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay);
	        	 		break;
	        	 	default:
	        	 }
        	 }
         }
         pPoseStack.popPose();

         
         pPoseStack.pushPose();
         rotateForBoardFacing(pBlockEntity, pPoseStack);
         bufferbuilder = pBufferSource.getBuffer(RenderType.entityCutout(white));
         RenderSystem.setShaderTexture(0, white);
         
         
         for(int by = 0; by < 8; by++) { 
        	 for(int bx = 0; bx < 8; bx++) {
        		 Piece piece = pBlockEntity.getBoard().pieceAt(Point.create(bx, by));
        		 if(piece != null) switch(pBlockEntity.getBoard().pieceAt(Point.create(bx, by)).getCharacter()) {
	        	 	case 'R':
	        	 		drawRook(bx, by, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay);
	        	 		break;
	        	 	case 'N':
	        	 		drawKnight(bx, by, true, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay);
	        	 		break;
	        	 	case 'B':
	        	 		drawBishop(bx, by, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay);
	        	 		break;
	        	 	case 'Q':
	        	 		drawQueen(bx, by, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay);
	        	 		break;
	        	 	case 'K':
	        	 		drawKing(bx, by, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay);
	        	 		break;
	        	 	case 'P':
	        	 		drawPawn(bx, by, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay);
	        	 		break;
	        	 	default:
	        	 }
        	 }
         }

         pPoseStack.popPose();
         
	}

	private void rotateForBoardFacing(ChessboardBlockEntity pBlockEntity, PoseStack pPoseStack) {
		switch (pBlockEntity.getBlockState().getValue(ChessboardBlock.FACING)) {
			case UP  -> {//We should never have this cse, but it'll look like SOUTH if we do  
	        }
			case DOWN  -> {//We should never have this cse, but it'll look like SOUTH if we do  
	        }
	        case NORTH -> { 
	        	pPoseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
	        	pPoseStack.translate(-1.0D, 0.0D, -1.0D);
	        }
	        case EAST ->  { 
	        	pPoseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
	        	pPoseStack.translate(-1.0D, 0.0D, 0.0D);
	        }
	        case SOUTH -> {  
	        }
	        case WEST ->  { 
	        	pPoseStack.mulPose(Vector3f.YP.rotationDegrees(270.0F));
	        	pPoseStack.translate(0.0D, 0.0D, -1.0D);
	        }
        }
	}
	
	
	private void drawBishop(int bx, int bz, PoseStack pPoseStack, VertexConsumer  bufferbuilder, int pPackedLight, int pPackedOverlay) {
		drawPiece(0.02f, bx, bz, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay, 0, 0, 0);        
		drawPiece(0.02f, bx, bz, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay, 0, 0.04, 0);
	}

	private void drawKnight(int bx, int bz, boolean flip, PoseStack pPoseStack, VertexConsumer  bufferbuilder, int pPackedLight, int pPackedOverlay) {
		float x = flip?0.01f:-0.01f;
		float z = flip?-0.01f:0.01f;
		
		drawPiece(0.02f, bx, bz, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay, x, 0, z);        
		drawPiece(0.02f, bx, bz, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay, -x, 0.04, -z);	
	}
	
	private void drawRook(int bx, int bz, PoseStack pPoseStack, VertexConsumer  bufferbuilder, int pPackedLight, int pPackedOverlay) {
		drawPiece(0.03f, bx, bz, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay, 0, 0, 0);        
	}

	private void drawKing(int bx, int bz, PoseStack pPoseStack, VertexConsumer  bufferbuilder, int pPackedLight, int pPackedOverlay) {
		drawPiece(0.04f, bx, bz, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay, 0, 0, 0);        
	}
	
	private void drawQueen(int bx, int bz, PoseStack pPoseStack, VertexConsumer  bufferbuilder, int pPackedLight, int pPackedOverlay) {
		drawPiece(0.03f, bx, bz, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay, 0, 0, 0);
		drawPiece(0.02f, bx, bz, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay, 0, 0.06, 0);
	}
	
	private void drawPawn(int bx, int bz,PoseStack pPoseStack,  VertexConsumer  bufferbuilder, int pPackedLight, int pPackedOverlay) {
		drawPiece(0.02f, bx, bz, pPoseStack, bufferbuilder, pPackedLight, pPackedOverlay, 0, 0, 0);
	}

	private void drawPiece(float size, int bx, int bz, PoseStack pPoseStack, VertexConsumer bufferbuilder, int pPackedLight, int pPackedOverlay, final double x, final double y, final double z) {
		float xOff = (float)(x + 2.75f/16f + bx*1.5f/16f);
		float yOff = (float)(y +1+size);
		float zOff = (float)(z + 2.75f/16f + bz*1.5f/16f);
		
		
		
		final float PieceTileSize = 240;
		final float PieceTileBorderSize = 12;
		final float UnitPieceSize = 0.04f;
		float scaledTextureOffset  = (2 - UnitPieceSize/size)/2*PieceTileBorderSize/PieceTileSize;

		Matrix4f model = pPoseStack.last().pose();
		Matrix3f matrix3f = pPoseStack.last().normal();
		//bufferbuilder.vertex(model, p2.x, 1.001f, p1.y).color(r, g, b, a).uv(5, 0).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0, 1, 0).endVertex();
        //south side [pos z] [parent x]
        bufferbuilder.vertex(model, xOff+size, yOff-size, zOff+size).color(1, 1, 1, 0.5f).uv(1-scaledTextureOffset,1-scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,0,1).endVertex();
        bufferbuilder.vertex(model, xOff+size, yOff+size, zOff+size).color(1, 1, 1, 0.5f).uv(1-scaledTextureOffset,scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,0,1).endVertex();
        bufferbuilder.vertex(model, xOff-size, yOff+size, zOff+size).color(1, 1, 1, 0.5f).uv(scaledTextureOffset,scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,0,1).endVertex();
        bufferbuilder.vertex(model, xOff-size, yOff-size, zOff+size).color(1, 1, 1, 0.5f).uv(scaledTextureOffset,1-scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,0,1).endVertex();

        //north side [neg z] [parent x]
        bufferbuilder.vertex(model, xOff-size, yOff-size, zOff-size).color(1, 1, 1, 0.5f).uv(1-scaledTextureOffset,1-scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,0,-1).endVertex();
        bufferbuilder.vertex(model, xOff-size, yOff+size, zOff-size).color(1, 1, 1, 0.5f).uv(1-scaledTextureOffset,scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,0,-1).endVertex();
        bufferbuilder.vertex(model, xOff+size, yOff+size, zOff-size).color(1, 1, 1, 0.5f).uv(scaledTextureOffset,scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,0,-1).endVertex();
        bufferbuilder.vertex(model, xOff+size, yOff-size, zOff-size).color(1, 1, 1, 0.5f).uv(scaledTextureOffset,1-scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,0,-1).endVertex();

        //east side [pos x] [parent z]
        bufferbuilder.vertex(model, xOff+size, yOff-size, zOff-size).color(1, 1, 1, 0.5f).uv(1-scaledTextureOffset,1-scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 1,0,0).endVertex();
        bufferbuilder.vertex(model, xOff+size, yOff+size, zOff-size).color(1, 1, 1, 0.5f).uv(1-scaledTextureOffset,scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 1,0,0).endVertex();
        bufferbuilder.vertex(model, xOff+size, yOff+size, zOff+size).color(1, 1, 1, 0.5f).uv(scaledTextureOffset,scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 1,0,0).endVertex();
        bufferbuilder.vertex(model, xOff+size, yOff-size, zOff+size).color(1, 1, 1, 0.5f).uv(scaledTextureOffset,1-scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 1,0,0).endVertex();

        //west side [neg x] [parent z]
        bufferbuilder.vertex(model, xOff-size, yOff-size, zOff+size).color(1, 1, 1, 0.5f).uv(1-scaledTextureOffset,1-scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, -1,0,0).endVertex();
        bufferbuilder.vertex(model, xOff-size, yOff+size, zOff+size).color(1, 1, 1, 0.5f).uv(1-scaledTextureOffset,scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, -1,0,0).endVertex();
        bufferbuilder.vertex(model, xOff-size, yOff+size, zOff-size).color(1, 1, 1, 0.5f).uv(scaledTextureOffset,scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, -1,0,0).endVertex();
        bufferbuilder.vertex(model, xOff-size, yOff-size, zOff-size).color(1, 1, 1, 0.5f).uv(scaledTextureOffset,1-scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, -1,0,0).endVertex();

        //top [pos y] [parent x & y]
        bufferbuilder.vertex(model, xOff+size, yOff+size, zOff-size).color(1, 1, 1, 0.5f).uv(1-scaledTextureOffset,1-scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,1,0).endVertex();
        bufferbuilder.vertex(model, xOff-size, yOff+size, zOff-size).color(1, 1, 1, 0.5f).uv(1-scaledTextureOffset,scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,1,0).endVertex();
        bufferbuilder.vertex(model, xOff-size, yOff+size, zOff+size).color(1, 1, 1, 0.5f).uv(scaledTextureOffset,scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,1,0).endVertex();
        bufferbuilder.vertex(model, xOff+size, yOff+size, zOff+size).color(1, 1, 1, 0.5f).uv(scaledTextureOffset,1-scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,1,0).endVertex();

        //bottom [neg y] [parent x & y]
        bufferbuilder.vertex(model, xOff-size, yOff-size, zOff-size).color(1, 1, 1, 0.5f).uv(1-scaledTextureOffset,1-scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,-1,0).endVertex();
        bufferbuilder.vertex(model, xOff+size, yOff-size, zOff-size).color(1, 1, 1, 0.5f).uv(1-scaledTextureOffset,scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,-1,0).endVertex();
        bufferbuilder.vertex(model, xOff+size, yOff-size, zOff+size).color(1, 1, 1, 0.5f).uv(scaledTextureOffset,scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,-1,0).endVertex();
        bufferbuilder.vertex(model, xOff-size, yOff-size, zOff+size).color(1, 1, 1, 0.5f).uv(scaledTextureOffset,1-scaledTextureOffset).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0,-1,0).endVertex();
           
	}


	
}