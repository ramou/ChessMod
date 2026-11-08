package chessmod.client.render.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Matrix4f;

import chessmod.block.ChessboardBlock;
import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.piece.Piece;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ChessboardBlockEntityRenderer<T extends ChessboardBlockEntity> implements BlockEntityRenderer<T> {
	public static final ResourceLocation black = ResourceLocation.fromNamespaceAndPath("chessmod", "textures/block/black.png");
	public static final ResourceLocation white = ResourceLocation.fromNamespaceAndPath("chessmod", "textures/block/white.png");

	public ChessboardBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }


	

	
	
	/**
	 * Render our BlockEntity
	 */
	@Override
	public void render(T pBlockEntity, float pPartialTick, PoseStack pPoseStack,
			MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

		//Draw black pieces
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


		//Draw white pieces
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
				pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
	        	pPoseStack.translate(-1.0D, 0.0D, -1.0D);
	        }
	        case EAST ->  { 
	        	pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
	        	pPoseStack.translate(-1.0D, 0.0D, 0.0D);
	        }
	        case SOUTH -> {  
	        }
	        case WEST ->  { 
	        	pPoseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
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
				//bufferbuilder.vertex(model, p2.x, 1.001f, p1.y).color(r, g, b, a).uv(5, 0).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(matrix3f, 0, 1, 0).endVertex();
        //south side [pos z] [parent x]
        bufferbuilder.addVertex(model, xOff+size, yOff-size, zOff+size).setColor(1, 1, 1, 0.5f).setUv(1-scaledTextureOffset,1-scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,0,1);
        bufferbuilder.addVertex(model, xOff+size, yOff+size, zOff+size).setColor(1, 1, 1, 0.5f).setUv(1-scaledTextureOffset,scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,0,1);
        bufferbuilder.addVertex(model, xOff-size, yOff+size, zOff+size).setColor(1, 1, 1, 0.5f).setUv(scaledTextureOffset,scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,0,1);
        bufferbuilder.addVertex(model, xOff-size, yOff-size, zOff+size).setColor(1, 1, 1, 0.5f).setUv(scaledTextureOffset,1-scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,0,1);

        //north side [neg z] [parent x]
        bufferbuilder.addVertex(model, xOff-size, yOff-size, zOff-size).setColor(1, 1, 1, 0.5f).setUv(1-scaledTextureOffset,1-scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,0,-1);
        bufferbuilder.addVertex(model, xOff-size, yOff+size, zOff-size).setColor(1, 1, 1, 0.5f).setUv(1-scaledTextureOffset,scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,0,-1);
        bufferbuilder.addVertex(model, xOff+size, yOff+size, zOff-size).setColor(1, 1, 1, 0.5f).setUv(scaledTextureOffset,scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,0,-1);
        bufferbuilder.addVertex(model, xOff+size, yOff-size, zOff-size).setColor(1, 1, 1, 0.5f).setUv(scaledTextureOffset,1-scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,0,-1);

        //east side [pos x] [parent z]
        bufferbuilder.addVertex(model, xOff+size, yOff-size, zOff-size).setColor(1, 1, 1, 0.5f).setUv(1-scaledTextureOffset,1-scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 1,0,0);
        bufferbuilder.addVertex(model, xOff+size, yOff+size, zOff-size).setColor(1, 1, 1, 0.5f).setUv(1-scaledTextureOffset,scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 1,0,0);
        bufferbuilder.addVertex(model, xOff+size, yOff+size, zOff+size).setColor(1, 1, 1, 0.5f).setUv(scaledTextureOffset,scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 1,0,0);
        bufferbuilder.addVertex(model, xOff+size, yOff-size, zOff+size).setColor(1, 1, 1, 0.5f).setUv(scaledTextureOffset,1-scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 1,0,0);

        //west side [neg x] [parent z]
        bufferbuilder.addVertex(model, xOff-size, yOff-size, zOff+size).setColor(1, 1, 1, 0.5f).setUv(1-scaledTextureOffset,1-scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), -1,0,0);
        bufferbuilder.addVertex(model, xOff-size, yOff+size, zOff+size).setColor(1, 1, 1, 0.5f).setUv(1-scaledTextureOffset,scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), -1,0,0);
        bufferbuilder.addVertex(model, xOff-size, yOff+size, zOff-size).setColor(1, 1, 1, 0.5f).setUv(scaledTextureOffset,scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), -1,0,0);
        bufferbuilder.addVertex(model, xOff-size, yOff-size, zOff-size).setColor(1, 1, 1, 0.5f).setUv(scaledTextureOffset,1-scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), -1,0,0);

        //top [pos y] [parent x & y]
        bufferbuilder.addVertex(model, xOff+size, yOff+size, zOff-size).setColor(1, 1, 1, 0.5f).setUv(1-scaledTextureOffset,1-scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,1,0);
        bufferbuilder.addVertex(model, xOff-size, yOff+size, zOff-size).setColor(1, 1, 1, 0.5f).setUv(1-scaledTextureOffset,scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,1,0);
        bufferbuilder.addVertex(model, xOff-size, yOff+size, zOff+size).setColor(1, 1, 1, 0.5f).setUv(scaledTextureOffset,scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,1,0);
        bufferbuilder.addVertex(model, xOff+size, yOff+size, zOff+size).setColor(1, 1, 1, 0.5f).setUv(scaledTextureOffset,1-scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,1,0);

        //bottom [neg y] [parent x & y]
        bufferbuilder.addVertex(model, xOff-size, yOff-size, zOff-size).setColor(1, 1, 1, 0.5f).setUv(1-scaledTextureOffset,1-scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,-1,0);
        bufferbuilder.addVertex(model, xOff+size, yOff-size, zOff-size).setColor(1, 1, 1, 0.5f).setUv(1-scaledTextureOffset,scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,-1,0);
        bufferbuilder.addVertex(model, xOff+size, yOff-size, zOff+size).setColor(1, 1, 1, 0.5f).setUv(scaledTextureOffset,scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,-1,0);
        bufferbuilder.addVertex(model, xOff-size, yOff-size, zOff+size).setColor(1, 1, 1, 0.5f).setUv(scaledTextureOffset,1-scaledTextureOffset).setOverlay(pPackedOverlay).setLight(pPackedLight).setNormal(pPoseStack.last(), 0,-1,0);
           
	}

}