package com.htmlweb.chess.client.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.htmlweb.chess.tileentity.WoodChessboardTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

/**
 * Renders a model of the surrounding blocks.
 * This should really probably not be in an ChessMod for beginners,
 * but I added comments to it so its all good
 *
 * @author Cadiboo
 */
public class WoodChessboardTileEntityRenderer extends TileEntityRenderer<WoodChessboardTileEntity> {

	/**
	 * Render our TileEntity
	 */
	@Override
	public void render(final WoodChessboardTileEntity tileEntityIn, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        ResourceLocation black = new ResourceLocation("com_htmlweb_chess", "textures/block/obsidian.png");
        ResourceLocation white = new ResourceLocation("com_htmlweb_chess", "textures/block/glowstone.png");
        
        char[] squares = tileEntityIn.getBoardState().toCharArray();		
        
        //The board state is corrupt
        if(squares.length != 64) {
        	return;
        }
        
        GlStateManager.pushMatrix();

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableCull();
		if (Minecraft.isAmbientOcclusionEnabled()) {
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		} else {
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}

         BlockModelRenderer.enableCache();
         
         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

         this.bindTexture(black);

         //"rnbqkbnrpppppppp................................PPPPPPPPRNBQKBNR"
         for(int by = 0; by < 8; by++) { 
        	 for(int bx = 0; bx < 8; bx++) {
        		 
	        	 switch(squares[by*8+bx]) {
	        	 	case 'r':
	        	 		drawRook(bx, by, bufferbuilder, x, y, z);
	        	 		break;
	        	 	case 'n':
	        	 		drawKnight(bx, by, false, bufferbuilder, x, y, z);
	        	 		break;
	        	 	case 'b':
	        	 		drawBishop(bx, by, bufferbuilder, x, y, z);
	        	 		break;
	        	 	case 'q':
	        	 		drawQueen(bx, by, bufferbuilder, x, y, z);
	        	 		break;
	        	 	case 'k':
	        	 		drawKing(bx, by, bufferbuilder, x, y, z);
	        	 		break;
	        	 	case 'p':
	        	 		drawPawn(bx, by, bufferbuilder, x, y, z);
	        	 		break;
	        	 	default:
	        	 }
        	 }
         }
    
         bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
         tessellator.draw();
         
         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
         this.bindTexture(white);
         
         for(int by = 0; by < 8; by++) { 
        	 for(int bx = 0; bx < 8; bx++) {
        		 
	        	 switch(squares[by*8+bx]) {
	        	 	case 'R':
	        	 		drawRook(bx, by, bufferbuilder, x, y, z);
	        	 		break;
	        	 	case 'N':
	        	 		drawKnight(bx, by, true, bufferbuilder, x, y, z);
	        	 		break;
	        	 	case 'B':
	        	 		drawBishop(bx, by, bufferbuilder, x, y, z);
	        	 		break;
	        	 	case 'Q':
	        	 		drawQueen(bx, by, bufferbuilder, x, y, z);
	        	 		break;
	        	 	case 'K':
	        	 		drawKing(bx, by, bufferbuilder, x, y, z);
	        	 		break;
	        	 	case 'P':
	        	 		drawPawn(bx, by, bufferbuilder, x, y, z);
	        	 		break;
	        	 	default:
	        	 }
        	 }
         }
         
         bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
         tessellator.draw();
         
         
         BlockModelRenderer.disableCache();
         
         GlStateManager.enableCull();
         
         RenderHelper.enableStandardItemLighting();
         GlStateManager.popMatrix();
	}
	
	
	private void drawBishop(int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
		drawPiece(0.02f, bx, bz, bufferbuilder, x, y, z);        
		drawPiece(0.02f, bx, bz, bufferbuilder, x, y+0.04, z);
	}

	private void drawKnight(int bx, int bz, boolean flip, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
		drawPiece(0.02f, bx, bz, bufferbuilder, x, y, z);        
		drawPiece(0.02f, bx, bz, bufferbuilder, x+0.02*((flip)?-1:1), y+0.04, z);
	}
	
	private void drawRook(int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
		drawPiece(0.03f, bx, bz, bufferbuilder, x, y, z);        
	}

	private void drawKing(int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
		drawPiece(0.04f, bx, bz, bufferbuilder, x, y, z);        
	}
	
	private void drawQueen(int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
		drawPiece(0.03f, bx, bz, bufferbuilder, x, y, z);
		drawPiece(0.02f, bx, bz, bufferbuilder, x, y+0.06, z);
	}
	
	private void drawPawn(int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
		drawPiece(0.02f, bx, bz, bufferbuilder, x, y, z);
	}

	private void drawPiece(float size, int bx, int bz, BufferBuilder bufferbuilder, final double x, final double y, final double z) {
         bufferbuilder.setTranslation(x + 2.75D/16D + bx*1.5D/16D, y +1+size, z + 2.75D/16D + bz*1.5D/16D);
         //south side [pos z] [parent x]
         double textureScale = 4*size/0.03;
		 bufferbuilder.pos(-size, +size, +size).tex(0,textureScale).endVertex();
         bufferbuilder.pos(-size, -size, +size).tex(0,0).endVertex();
         bufferbuilder.pos(+size, -size, +size).tex(textureScale,0).endVertex();
         bufferbuilder.pos(+size, +size, +size).tex(textureScale,textureScale).endVertex();

         //north side [neg z] [parent x]
         bufferbuilder.pos(-size, +size, -size).tex(0,textureScale).endVertex();
         bufferbuilder.pos(-size, -size, -size).tex(0,0).endVertex();
         bufferbuilder.pos(+size, -size, -size).tex(textureScale,0).endVertex();
         bufferbuilder.pos(+size, +size, -size).tex(textureScale,textureScale).endVertex();

         //east side [pos x] [parent z]
         bufferbuilder.pos(+size, +size, -size).tex(textureScale,0).endVertex();
         bufferbuilder.pos(+size, -size, -size).tex(0,0).endVertex();
         bufferbuilder.pos(+size, -size, +size).tex(0,textureScale).endVertex();
         bufferbuilder.pos(+size, +size, +size).tex(textureScale,textureScale).endVertex();

         //west side [neg x] [parent z]
         bufferbuilder.pos(-size, -size, +size).tex(0,textureScale).endVertex();
         bufferbuilder.pos(-size, -size, -size).tex(0,0).endVertex();
         bufferbuilder.pos(-size, +size, -size).tex(textureScale,0).endVertex();
         bufferbuilder.pos(-size, +size, +size).tex(textureScale,textureScale).endVertex();

         //top [pos y] [parent x & y]
         bufferbuilder.pos(+size, +size, -size).tex(textureScale,0).endVertex();
         bufferbuilder.pos(+size, +size, +size).tex(textureScale,textureScale).endVertex();
         bufferbuilder.pos(-size, +size, +size).tex(0,textureScale).endVertex();
         bufferbuilder.pos(-size, +size, -size).tex(0,0).endVertex();

         //bottom [neg y] [parent x & y]
         bufferbuilder.pos(+size, -size, -size).tex(textureScale,0).endVertex();
         bufferbuilder.pos(+size, -size, +size).tex(textureScale,textureScale).endVertex();
         bufferbuilder.pos(-size, -size, +size).tex(0,textureScale).endVertex();
         bufferbuilder.pos(-size, -size, -size).tex(0,0).endVertex();
	}

	/**
	 * This renderer is a global renderer.
	 * This means that it will always render, even if the player is not able to see it's block.
	 * This is useful for rendering larger models or dynamically sized models.
	 * The Beacon's beam is also a global renderer
	 */
	@Override
	public boolean isGlobalRenderer(final WoodChessboardTileEntity te) {
		return true;
	}

}
