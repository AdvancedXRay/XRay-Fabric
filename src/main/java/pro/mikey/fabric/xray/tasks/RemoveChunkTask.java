package pro.mikey.fabric.xray.tasks;

import net.minecraft.world.level.ChunkPos;
import pro.mikey.fabric.xray.render.RenderOutlines;

/**
 * This task is responsible for completely removing a chunk
 * This task is added to the threadpool and executed by a thread via the Scancontroller class.
 */
public class RemoveChunkTask implements Runnable {
    private final ChunkPos chunkPos;

    public RemoveChunkTask(ChunkPos chunkPos){
        this.chunkPos = chunkPos;
    }

    @Override
    public void run() {
        Thread.currentThread().setPriority(1);
        RenderOutlines.removeChunk(chunkPos.toLong());
    }
}
