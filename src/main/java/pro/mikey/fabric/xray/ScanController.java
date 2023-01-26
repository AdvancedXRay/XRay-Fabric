package pro.mikey.fabric.xray;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import pro.mikey.fabric.xray.render.RenderOutlines;
import pro.mikey.fabric.xray.storage.SettingsStore;
import pro.mikey.fabric.xray.tasks.*;

import java.util.concurrent.*;


public class ScanController {
    static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            StateSettings.getRadius(), StateSettings.getRadius(),
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>()
    );

    /**
     * This function sets the ThreadPool up properly.
     * Technically the threads set their own Priority to one anyways, so a normal Threapool would be sufficient
     */
    public static void setup(){
        executor.setThreadFactory(r -> {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });
    }

    private static void submitTask(Runnable worker){
        if(!executor.isShutdown()){
            executor.execute(worker);
        }
    }

    /**
     * Just a security measure to force-close the ThreadPool to make sure it doesn't linger in the background
     */
    public static void closeGame() {
        executor.shutdownNow();
    }


    /**
     * This function rebuilds the ChunkCache completly and reloads all Chunks
     * without flashing the already rendered Chunks instantly away
     */
    public static void reBuildCache(boolean force) {
        class RebuildThread extends Thread {
            @Override
            public synchronized void run() {
                RenderOutlines.clearChunks(force);
                executor.shutdownNow();
                while (!executor.isTerminated()) {
                    try {
                        currentThread().wait(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                executor = new ThreadPoolExecutor(
                        StateSettings.getRadius(), StateSettings.getRadius(),
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>()
                );
                setup();
                Runnable worker = new ReBuildCache();
                executor.execute(worker);
            }
        }
        Thread r = new RebuildThread();
        r.start();
    }

    /**
     * This function clears the Cache on a seperate Thread
     */
    public static void clearCache(boolean force) {
        class ClearThread extends Thread {
            @Override
            public synchronized void run() {
                RenderOutlines.clearChunks(force);
            }
        }
        Thread r = new ClearThread();
        r.start();
    }

    public static void reBuildCache() {
        reBuildCache(false);
    }

    /**
     * This function updates a Chunk based on Pos
     */
    public static void updateChunk(ChunkPos pos) {
        if (SettingsStore.getInstance().get().isActive()) {
            Runnable worker = new UpdateChunkTask(pos);
            submitTask(worker);
        }
    }

    /**
     * This function updates a Chunk based on Pos
     */
    public static void updateChunk(BlockPos pos){
        int chunkx = SectionPos.blockToSectionCoord(pos.getX());
        int chunkz = SectionPos.blockToSectionCoord(pos.getZ());
        ChunkPos chunkPos = new ChunkPos(chunkx,chunkz);
        updateChunk(chunkPos);
    }

    /**
     * This function removes a chunk from the Rendering
     */
    public static void removeChunk(ChunkPos pos){
        Runnable worker =  new RemoveChunkTask(pos);
        submitTask(worker);
    }
}
