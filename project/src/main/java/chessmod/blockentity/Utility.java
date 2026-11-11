package chessmod.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

/** Server-side dimension lookup for linked quantum boards. */
public final class Utility {
    private Utility() { }

    public static ServerLevel getServerLevel(String dimension) {
        if (dimension == null) return null;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimension));
        return server.getLevel(key);
    }

    public static BlockEntity getBlockEntityInDimension(String dimension, BlockPos pos) {
        ServerLevel level = getServerLevel(dimension);
        if (level == null || pos == null) return null;
        // Use the synchronous server-level chunk API available on Minecraft 1.19.4.
        // Deliberate chunk loading follows the 1.20.1 implementation's behaviour.
        level.getChunkAt(pos);
        return level.getBlockEntity(pos);
    }
}
