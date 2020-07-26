package pro.mikey.fabric.xray.jobs;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;

public class ChunkJob {
    HashMap<ChunkPos, String> loadedChunks = new HashMap<>();

    public void load(ClientWorld serverWorld, WorldChunk worldChunk) {
        loadedChunks.put(worldChunk.getPos(), serverWorld.getDebugString());
        System.out.println("L Loaded chunks: " + loadedChunks.size());
    }

    public void unload(ClientWorld serverWorld, WorldChunk worldChunk) {
        loadedChunks.remove(worldChunk.getPos());
        System.out.println("U Loaded chunks: " + loadedChunks.size());
    }
}
