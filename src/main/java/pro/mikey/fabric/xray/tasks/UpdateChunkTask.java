package pro.mikey.fabric.xray.tasks;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import pro.mikey.fabric.xray.records.BlockPosWithColor;
import pro.mikey.fabric.xray.render.RenderOutlines;

import java.util.List;

/**
 * This task is responsible for taking a chunk and updating the blocks to the RenderOutlines to be rendered.
 * In its current implementation in the RenderOutlines class this does virtually the same as AddChunk
 * This task is added to the threadpool and executed by a thread via the Scancontroller class.
 */
public class UpdateChunkTask implements Runnable{
        private ChunkPos chunkPos;

        public UpdateChunkTask(ChunkPos chunkPos){
            this.chunkPos = chunkPos;
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(1);
            List<BlockPosWithColor> toAdd = AddChunkTask.AddChunk(chunkPos);
            if(Minecraft.getInstance().player.clientLevel.getChunkSource().getChunk(chunkPos.x,chunkPos.z,false)!=null) {
                RenderOutlines.addChunk(chunkPos.toLong(),toAdd);
            }
            else{
                RenderOutlines.removeChunk(chunkPos.toLong());
            }
        }

}
