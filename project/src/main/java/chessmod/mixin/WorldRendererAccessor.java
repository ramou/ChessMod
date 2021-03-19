package chessmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
	@Accessor
	ChunkBuilder getChunkBuilder();
}
