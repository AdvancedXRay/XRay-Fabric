package pro.mikey.fabric.xray.tasks;


import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import pro.mikey.fabric.xray.ScanController;

import java.util.ArrayList;
import java.util.List;

/**
 * This task is responsible for completely rebuilding all loaded chunks arround the player
 * This task is added to the threadpool and executed by a thread via the Scancontroller class.
 */
public class ReBuildCache implements Runnable{
    private static class ChunkObject {
        ChunkObject(ChunkPos pos, Direction direction) {
            this.direction = direction;
            this.pos = pos;
        }

        public ChunkPos pos;
        public Direction direction;
    }

    @Override
    public void run() {
        List<ChunkObject> toScan = new ArrayList<>();
        assert Minecraft.getInstance().player != null;
        Vec3 position = Minecraft.getInstance().player.position();
        toScan.add(new ChunkObject(new ChunkPos((int) position.x / 16, (int) position.z / 16), Direction.UP));
        toScan.add(new ChunkObject(new ChunkPos((int) position.x / 16, (int) position.z / 16 + 1), Direction.EAST));
        toScan.add(new ChunkObject(new ChunkPos((int) position.x / 16, (int) position.z / 16 - 1), Direction.WEST));
        toScan.add(new ChunkObject(new ChunkPos((int) position.x / 16 + 1, (int) position.z / 16), Direction.NORTH));
        toScan.add(new ChunkObject(new ChunkPos((int) position.x / 16 - 1, (int) position.z / 16), Direction.SOUTH));
        while (!toScan.isEmpty()) {
            ChunkObject chunkObject = toScan.remove(0);
            if (Minecraft.getInstance().player.clientLevel.getChunkSource().getChunk(chunkObject.pos.x, chunkObject.pos.z, false) != null) {
                ScanController.updateChunk(chunkObject.pos);
                switch (chunkObject.direction) {
                    case EAST -> {
                        if (chunkObject.pos.x == (int) position.x / 16) {
                            toScan.add(new ChunkObject(new ChunkPos(chunkObject.pos.x, chunkObject.pos.z + 1), chunkObject.direction));
                        }
                        toScan.add(new ChunkObject(new ChunkPos(chunkObject.pos.x + 1, chunkObject.pos.z), chunkObject.direction));
                    }
                    case WEST -> {
                        if(chunkObject.pos.x==(int)position.x/16){
                            toScan.add(new ChunkObject(new ChunkPos(chunkObject.pos.x, chunkObject.pos.z - 1), chunkObject.direction));
                        }
                        toScan.add(new ChunkObject(new ChunkPos(chunkObject.pos.x - 1, chunkObject.pos.z), chunkObject.direction));
                    }
                    case NORTH -> {
                        if(chunkObject.pos.z==(int)position.z/16){
                            toScan.add(new ChunkObject(new ChunkPos(chunkObject.pos.x + 1, chunkObject.pos.z), chunkObject.direction));
                        }
                        toScan.add(new ChunkObject(new ChunkPos(chunkObject.pos.x, chunkObject.pos.z - 1), chunkObject.direction));
                    }
                    case SOUTH -> {
                        toScan.add(new ChunkObject(new ChunkPos(chunkObject.pos.x, chunkObject.pos.z + 1), chunkObject.direction));
                        if(chunkObject.pos.z==(int)position.z/16){
                            toScan.add(new ChunkObject(new ChunkPos(chunkObject.pos.x - 1, chunkObject.pos.z), chunkObject.direction));
                        }
                    }
                }
            }
        }
    }
}
