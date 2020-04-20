package com.htmlweb.chess.client.render.tileentity;

import static com.htmlweb.chess.client.render.WoodChessboard.BLOCK_RENDER_LAYERS;

import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.htmlweb.chess.ChessMod;
import com.htmlweb.chess.client.render.WoodChessboard;
import com.htmlweb.chess.config.ChessModConfig;
import com.htmlweb.chess.tileentity.WoodChessboardTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

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
		super.render(tileEntityIn, x, y, z, partialTicks, destroyStage);
		
		final WoodChessboard miniModel = tileEntityIn.woodChessboard;

		if (miniModel == null)
			return;

		if (!miniModel.isBuilt())
			miniModel.rebuild();

		// Setup correct GL state
		this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		// Translucency
		if (ChessModConfig.modelTranslucency) {
			GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
		} else {
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
		GlStateManager.enableBlend();

		if (Minecraft.isAmbientOcclusionEnabled()) {
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		} else {
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}

		GlStateManager.pushMatrix();

		// Translate to render pos. The 0.5 is to translate into the centre of the block, rather than to the corner of it
		GlStateManager.translated(x + 0.5, y + 0.5, z + 0.5);

		final double scale = ChessModConfig.modelScale;
		GlStateManager.scaled(scale, scale, scale);

		// Translate to start of render (our TileEntity is at its centre)
		GlStateManager.translated(-8, -8, -8);

		// Render the buffers
		renderChunkBuffers(miniModel.regionRenderCacheBuilder, miniModel.generator.getCompiledChunk());

		GlStateManager.popMatrix();

		// Clean up GL state
		RenderHelper.enableStandardItemLighting();

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
