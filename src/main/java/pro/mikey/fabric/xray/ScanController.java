package pro.mikey.fabric.xray;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import pro.mikey.fabric.xray.records.BlockPosWithColor;
import pro.mikey.fabric.xray.storage.Stores;

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
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return false;
        }

        return playerLastChunk == null || !playerLastChunk.equals(player.getChunkPos());
    }

    /**
     * Runs the scan task by checking if the thread is ready but first attempting to provide the cache
     * if the cache is still valid.
     *
     * @param forceRerun if the task is required to re-run for instance, a block is broken in the
     *                   world.
     */
    public static synchronized void runTask(boolean forceRerun) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null && client.world == null) {
            return;
        }

        if (!Stores.SETTINGS.get().isActive() || (!forceRerun && !playerLocationChanged())) {
            return;
        }

        // Update the players last chunk to eval against above.
        playerLastChunk = client.player.getChunkPos();
        Util.getMainWorkerExecutor().execute(new ScanTask());
    }

    public static void blockBroken(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        if (!Stores.SETTINGS.get().isActive()) return;

        if (renderQueue.stream().anyMatch(e -> e.getPos().equals(blockPos))) {
            runTask(true);
        }
    }

    public static void blockPlaced(ItemPlacementContext context) {
        if (!Stores.SETTINGS.get().isActive()) return;

        BlockState defaultState = Block.getBlockFromItem(context.getStack().getItem()).getDefaultState();
        if (Stores.BLOCKS.getCache().get().stream().anyMatch(e -> e.getState() == defaultState)) {
            runTask(true);
        }
    }
}
