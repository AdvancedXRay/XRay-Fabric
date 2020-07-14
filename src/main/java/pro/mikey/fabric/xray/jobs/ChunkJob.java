package pro.mikey.fabric.xray.jobs;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;

public class ChunkJob {
    HashMap<ChunkPos, String> loadedChunks = new HashMap<>();

    public void load(ServerWorld serverWorld, WorldChunk worldChunk) {
        loadedChunks.put(worldChunk.getPos(), serverWorld.getDebugString());
        System.out.println("L Loaded chunks: " + loadedChunks.size());
    }

    public void unload(ServerWorld serverWorld, WorldChunk worldChunk) {
        loadedChunks.remove(worldChunk.getPos());
        System.out.println("U Loaded chunks: " + loadedChunks.size());
    }
}
