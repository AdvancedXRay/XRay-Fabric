package pro.mikey.fabric.xray.tasks;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.LavaFluid;
import org.jetbrains.annotations.Nullable;
import pro.mikey.fabric.xray.cache.BlockSearchEntry;
import pro.mikey.fabric.xray.records.BasicColor;
import pro.mikey.fabric.xray.records.BlockPosWithColor;
import pro.mikey.fabric.xray.render.RenderOutlines;
import pro.mikey.fabric.xray.storage.BlockStore;
import pro.mikey.fabric.xray.storage.SettingsStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This task is responsible for taking a chunk and adding the blocks to the RenderOutlines to be rendered.
 * This task is added to the threadpool and executed by a thread via the Scancontroller class.
 */
public class AddChunkTask implements Runnable {

    private ChunkPos chunkPos;

    public AddChunkTask(ChunkPos chunkPos){
        this.chunkPos = chunkPos;
    }
    @Override
    public void run() {
        Thread.currentThread().setPriority(1);
        List<BlockPosWithColor> toAdd = AddChunk(chunkPos);
        if(Minecraft.getInstance().player.clientLevel.getChunkSource().getChunk(chunkPos.x,chunkPos.z,false)!=null) {
            RenderOutlines.addChunk(chunkPos.toLong(),toAdd);
        }
        else{
            RenderOutlines.removeChunk(chunkPos.toLong());
        }
    }

    public static List<BlockPosWithColor> AddChunk(ChunkPos chunkPos){
        int chunkStartX = chunkPos.x*16;
        int chunkStartZ = chunkPos.z*16;
        final Level world = Minecraft.getInstance().level;
        Set<BlockSearchEntry> blocks = BlockStore.getInstance().getCache().get();
        int height = world.getHeight();
        List<BlockPosWithColor> toAdd = new ArrayList<>();
        for (int k = chunkStartX; k < chunkStartX + 16; k++) {
            for (int l = chunkStartZ; l < chunkStartZ + 16; l++) {
                for (int m = world.getMinBuildHeight(); m < height + (1 << 4); m++) {
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
