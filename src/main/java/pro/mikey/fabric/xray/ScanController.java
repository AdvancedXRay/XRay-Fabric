package pro.mikey.fabric.xray;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ScanController {

    /**
     * We Cache the entire list based on the players chunk, once they exit that chunk,
     * we'll rescan by triggering our thread.
     */
    private static final Cache<ChunkPos, Set<BlockPos>> cacheByChunk = CacheBuilder
            .newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();

    private static Set<BlockPos> renderQueue = Collections.synchronizedSet( new HashSet<>() );

    // Temp
    private static final Set<Block> scanningBlocks = new HashSet<>(Arrays.asList(Blocks.DIAMOND_ORE, Blocks.REDSTONE_ORE));

    // Handles the threading system
    private static Future<?> task;
    private static ExecutorService executor;

    /**
     * Runs the scan task by checking if the thread is ready but first attempting
     * to provide the cache if the cache is still valid.
     *
     * @param forceRerun if the task is required to re-run for instance, a block is broken in the world.
     */
    public static synchronized void runTask( boolean forceRerun ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null && client.world == null) {
            return;
        }

        if (!StateStore.getInstance().isActive() || (task != null && !task.isDone())) {
            return;
        }

        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }

        task = executor.submit(new ScanTask());
    }

    /**
     * Shut down the job. We disable xray here as a fail safe.
     */
    public static void shutdownTask() {
        if (StateStore.getInstance().isActive()) {
            StateStore.getInstance().setActive(false);
        }

        try { executor.shutdownNow(); }
        catch (Throwable ignore) {}
    }
}
