package pro.mikey.fabric.xray;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScanController {
    public static List<BlockPos> renderQueue = Collections.synchronizedList( new ArrayList<>() );

    // Temp
    public static final Set<Block> scanningBlocks = new HashSet<>(Arrays.asList(Blocks.DIAMOND_ORE, Blocks.REDSTONE_ORE, Blocks.GRASS, Blocks.GRASS_BLOCK, Blocks.SAND));

    // Handles the threading system
    private static Future<?> task;
    private static ExecutorService executor;

    private static ChunkPos playerLastChunk;

    /**
     * No point even running if the player is still in the same chunk.
     */
    private static boolean playerLocationChanged() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return false;
        }

        return playerLastChunk == null || playerLastChunk.x != player.chunkX || playerLastChunk.z != player.chunkZ;
    }

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

        if (!StateStore.getInstance().isActive() || (task != null && !task.isDone()) || (!forceRerun && !playerLocationChanged())) {
            return;
        }

        executor = Executors.newSingleThreadExecutor();

        // Update the players last chunk to eval against above.
        playerLastChunk = new ChunkPos(client.player.chunkX, client.player.chunkZ);
        task = executor.submit(new ScanTask(client.player.getBlockPos()));
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
