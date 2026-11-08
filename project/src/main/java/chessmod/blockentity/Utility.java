package chessmod.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;


public class Utility {
    public static ServerLevel getServerLevel(String dimensionLocationString) {
        ResourceLocation dimensionLocation = ResourceLocation.parse(dimensionLocationString);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionLocation);
        return server.getLevel(dimensionKey);
    }

    public static BlockEntity getBlockEntityInDimension(String dimensionLocationString, BlockPos blockPos) {
        ServerLevel targetLevel = getServerLevel(dimensionLocationString);
        if (targetLevel != null && blockPos != null) {

            // Caller must run on the server thread. This can force a chunk load,
            // matching the old behaviour; review the loading policy under #207.
            targetLevel.getChunkAt(blockPos);
            return targetLevel.getBlockEntity(blockPos);
        }
        return null;
    }
}
