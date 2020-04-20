package com.htmlweb.chess.client.render.tileentity;

import static com.htmlweb.chess.client.render.WoodChessboard.BLOCK_RENDER_LAYERS;

import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.htmlweb.chess.tileentity.WoodChessboardTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

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
		//BlockPos blockpos = tileEntityIn.getPos();
		//BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        ResourceLocation black = new ResourceLocation("com_htmlweb_chess", "textures/block/obsidian.png");
        ResourceLocation white = new ResourceLocation("com_htmlweb_chess", "textures/block/glowstone.png");
		

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
         drawRook(0, 0, bufferbuilder, x, y, z);
         drawKnight(0, 1, false, bufferbuilder, x, y, z);
         drawBishop(0, 2, bufferbuilder, x, y, z);
         drawKing(0, 3, bufferbuilder, x, y, z);
         drawQueen(0, 4, bufferbuilder, x, y, z);
         drawBishop(0, 5, bufferbuilder, x, y, z);
         drawKnight(0, 6, false, bufferbuilder, x, y, z);
         drawRook(0, 7, bufferbuilder, x, y, z);
         
         drawPawn(1, 0, bufferbuilder, x, y, z);
         drawPawn(1, 1, bufferbuilder, x, y, z);
         drawPawn(1, 2, bufferbuilder, x, y, z);
         drawPawn(1, 3, bufferbuilder, x, y, z);
         drawPawn(1, 4, bufferbuilder, x, y, z);
         drawPawn(1, 5, bufferbuilder, x, y, z);
         drawPawn(1, 6, bufferbuilder, x, y, z);
         drawPawn(1, 7, bufferbuilder, x, y, z);
    
         bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
         tessellator.draw();
         
         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
         this.bindTexture(white);
         
         drawPawn(6, 0, bufferbuilder, x, y, z);
         drawPawn(6, 1, bufferbuilder, x, y, z);
         drawPawn(6, 2, bufferbuilder, x, y, z);
         drawPawn(6, 3, bufferbuilder, x, y, z);
         drawPawn(6, 4, bufferbuilder, x, y, z);
         drawPawn(6, 5, bufferbuilder, x, y, z);
         drawPawn(6, 6, bufferbuilder, x, y, z);
         drawPawn(6, 7, bufferbuilder, x, y, z);
         
         drawRook(7, 0, bufferbuilder, x, y, z);
         drawKnight(7, 1, true, bufferbuilder, x, y, z);
         drawBishop(7, 2, bufferbuilder, x, y, z);
         drawKing(7, 3, bufferbuilder, x, y, z);
         drawQueen(7, 4, bufferbuilder, x, y, z);
         drawBishop(7, 5, bufferbuilder, x, y, z);
         drawKnight(7, 6, true, bufferbuilder, x, y, z);
         drawRook(7, 7, bufferbuilder, x, y, z);
         
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

	/**
	 * Loops through every non-empty {@link BufferBuilder} in buffers and renders the buffer without resetting it
	 *
	 * @param buffers       The {@link RegionRenderCacheBuilder} to get {@link BufferBuilder}s from
	 * @param compiledChunk The {@link CompiledChunk} to use to check if a layer has any rendered blocks
	 */
	private void renderChunkBuffers(final RegionRenderCacheBuilder buffers, final CompiledChunk compiledChunk) {
		final int length = BLOCK_RENDER_LAYERS.length;
		// Render each buffer that has been used
		for (int layerOrdinal = 0; layerOrdinal < length; ++layerOrdinal) {
			if (!compiledChunk.isLayerEmpty(BLOCK_RENDER_LAYERS[layerOrdinal])) {
				drawBufferWithoutResetting(buffers.getBuilder(layerOrdinal));
			}
		}
	}

	/**
	 * Copy of net.minecraft.client.renderer.WorldVertexBufferUploader#draw(net.minecraft.client.renderer.BufferBuilder)
	 * The only difference is that it does NOT reset the buffer after drawing it
	 *
	 * @param bufferBuilderIn the buffer builder to draw
	 */
	private void drawBufferWithoutResetting(final BufferBuilder bufferBuilderIn) {
		if (bufferBuilderIn.getVertexCount() > 0) {
			VertexFormat vertexformat = bufferBuilderIn.getVertexFormat();
			int i = vertexformat.getSize();
			ByteBuffer bytebuffer = bufferBuilderIn.getByteBuffer();
			List<VertexFormatElement> list = vertexformat.getElements();

			for (int j = 0; j < list.size(); ++j) {
				VertexFormatElement vertexformatelement = list.get(j);
				vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer); // moved to VertexFormatElement.preDraw
			}

			GlStateManager.drawArrays(bufferBuilderIn.getDrawMode(), 0, bufferBuilderIn.getVertexCount());
			int i1 = 0;

			for (int j1 = list.size(); i1 < j1; ++i1) {
				VertexFormatElement vertexformatelement1 = list.get(i1);
				vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer); // moved to VertexFormatElement.postDraw
			}
		}

		// Commented out - don't reset the buffer
//		bufferBuilderIn.reset();
	}

}
