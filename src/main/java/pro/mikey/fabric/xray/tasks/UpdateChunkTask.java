package pro.mikey.fabric.xray.tasks;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import pro.mikey.fabric.xray.cache.BlockSearchEntry;
import pro.mikey.fabric.xray.records.BasicColor;
import pro.mikey.fabric.xray.records.BlockPosWithColor;
import pro.mikey.fabric.xray.render.RenderOutlines;
import pro.mikey.fabric.xray.storage.BlockStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This task is responsible for taking a chunk and updating the blocks to the RenderOutlines to be rendered.
 * In its current implementation in the RenderOutlines class this does virtually the same as addChunk
 * This task is added to the thread pool and executed by a thread via the ScanController class.
 */
public class UpdateChunkTask implements Runnable{
    private final ChunkPos chunkPos;

        public UpdateChunkTask(ChunkPos chunkPos){
            this.chunkPos = chunkPos;
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(1);
            List<BlockPosWithColor> toAdd = addChunk(chunkPos);
            assert Minecraft.getInstance().player != null;
            if (Minecraft.getInstance().player.clientLevel.getChunkSource().getChunk(chunkPos.x, chunkPos.z, false) != null) {
                RenderOutlines.addChunk(chunkPos.toLong(), toAdd);
            } else {
                RenderOutlines.removeChunk(chunkPos.toLong());
            }
        }

    public static List<BlockPosWithColor> addChunk(ChunkPos chunkPos) {
        int chunkStartX = chunkPos.x * 16;
        int chunkStartZ = chunkPos.z * 16;
        final Level world = Minecraft.getInstance().level;
        Set<BlockSearchEntry> blocks = BlockStore.getInstance().getCache().get();
        assert world != null;
        int height = world.getHeight();
        List<BlockPosWithColor> toAdd = new ArrayList<>();
        for (int m = world.getMinBuildHeight(); m < height + (1 << 4); m++) {
            for (int l = chunkStartZ; l < chunkStartZ + 16; l++) {
                for (int k = chunkStartX; k < chunkStartX + 16; k++) {
                    BlockPos pos = new BlockPos(k, m, l);
                    BasicColor validBlock = isValidBlock(pos, world, blocks);
                    if (validBlock != null) {
                        toAdd.add(new BlockPosWithColor(pos, validBlock));
                    }
                }
            }
        }
        return toAdd;
    }

    @Nullable
    public static BasicColor isValidBlock(BlockPos pos, Level world, Set<BlockSearchEntry> blocks) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return null;
        }

        BlockState defaultState = state.getBlock().defaultBlockState();

        return blocks.stream()
                .filter(localState -> localState.isDefault() && defaultState == localState.getState() || !localState.isDefault() && state == localState.getState())
                .findFirst()
                .map(BlockSearchEntry::getColor)
                .orElse(null);
    }
}
