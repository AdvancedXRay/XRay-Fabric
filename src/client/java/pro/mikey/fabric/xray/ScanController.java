package pro.mikey.fabric.xray;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pro.mikey.fabric.xray.records.BlockPosWithColor;
import pro.mikey.fabric.xray.storage.BlockStore;
import pro.mikey.fabric.xray.storage.SettingsStore;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ScanController {
    public static Set<BlockPosWithColor> renderQueue = Collections.synchronizedSet(new HashSet<>());
    private static ChunkPos playerLastChunk;

    /**
     * No point even running if the player is still in the same chunk.
     */
    private static boolean playerLocationChanged() {
        if (Minecraft.getInstance().player == null)
            return false;

        ChunkPos plyChunkPos = Minecraft.getInstance().player.chunkPosition();
        int range = StateSettings.getHalfRange();

        return playerLastChunk == null ||
                plyChunkPos.x > playerLastChunk.x + range || plyChunkPos.x < playerLastChunk.x - range ||
                plyChunkPos.z > playerLastChunk.z + range || plyChunkPos.z < playerLastChunk.z - range;
    }

    /**
     * Runs the scan task by checking if the thread is ready but first attempting to provide the cache
     * if the cache is still valid.
     *
     * @param forceRerun if the task is required to re-run for instance, a block is broken in the
     *                   world.
     */
    public static synchronized void runTask(boolean forceRerun) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null && client.level == null) {
            return;
        }

        if (!SettingsStore.getInstance().get().isActive() || (!forceRerun && !playerLocationChanged())) {
            return;
        }

        // Update the players last chunk to eval against above.
        playerLastChunk = client.player.chunkPosition();
        Util.backgroundExecutor().execute(new ScanTask());
    }

    public static void blockBroken(Level world, Player playerEntity, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        if (!SettingsStore.getInstance().get().isActive()) return;

        if (renderQueue.stream().anyMatch(e -> e.pos().equals(blockPos))) {
            runTask(true);
        }
    }

    public static void blockPlaced(BlockPlaceContext context) {
        if (!SettingsStore.getInstance().get().isActive()) return;

        BlockState defaultState = Block.byItem(context.getItemInHand().getItem()).defaultBlockState();
        if (BlockStore.getInstance().getCache().get().stream().anyMatch(e -> e.getState() == defaultState)) {
            runTask(true);
        }
    }
}
