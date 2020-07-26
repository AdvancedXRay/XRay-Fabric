package pro.mikey.fabric.xray;

import net.java.games.input.Controller;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ScanTask implements Runnable {
    private final ScanArea area;
    private final ChunkPos chunkPos;

    public ScanTask(ScanArea area, ChunkPos chunkPos) {
        this.chunkPos = chunkPos;
        this.area = area;
    }

    /**
     * This is an "exact" copy from the forge version of the mod but with the optimisations
     * that the rewrite (Fabric) version has brought like chunk location based cache, etc.
     */
    @Override
    public void run() {
//        HashMap<UUID, BlockData> blocks = Controller.getBlockStore().getStore();
        ScanController.renderQueue.clear();

        // The cache can fail so we protect against that.
        try {
            ScanController.renderQueue.addAll(ScanController.cacheByChunk.get(this.chunkPos, this::collectBlocks));
        } catch (ExecutionException ignored) {}
    }

    // This is only run if the cache is invalidate
    private Set<BlockPos> collectBlocks() {
        Set<Block> blocks = ScanController.scanningBlocks;

        // If we're not looking for blocks, don't run.
        if ( blocks.isEmpty() ) {
            if( !ScanController.renderQueue.isEmpty() )
                ScanController.renderQueue.clear();
            return new HashSet<>();
        }

        final World world = MinecraftClient.getInstance().world;
        final PlayerEntity player = MinecraftClient.getInstance().player;

        // Just stop if we can't get the player or world.
        if( world == null || player == null )
            return new HashSet<>();

        final Set<BlockPos> renderQueue = new HashSet<>();
        int lowBoundX, highBoundX, lowBoundY, highBoundY, lowBoundZ, highBoundZ;

        // Used for cleaning up the searching process
        BlockState currentState;
        FluidState currentFluid;

        // Loop on chunks (x, z)
        for ( int chunkX = area.minChunkX; chunkX <= area.maxChunkX; chunkX++ )
        {
            // Pre-compute the extend bounds on X
            int x = chunkX << 4; // lowest x coord of the chunk in block/world coordinates
            lowBoundX = (x < area.minX) ? area.minX - x : 0; // lower bound for x within the extend
            highBoundX = (x + 15 > area.maxX) ? area.maxX - x : 15;// and higher bound. Basically, we clamp it to fit the radius.

            for ( int chunkZ = area.minChunkZ; chunkZ <= area.maxChunkZ; chunkZ++ )
            {
                // Time to getStore the chunk (16x256x16) and split it into 16 vertical extends (16x16x16)
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    continue; // We won't find anything interesting in unloaded chunks
                }

                Chunk chunk = world.getChunk( chunkX, chunkZ );
                ChunkSection[] extendsList = chunk.getSectionArray();

                // Pre-compute the extend bounds on Z
                int z = chunkZ << 4;
                lowBoundZ = (z < area.minZ) ? area.minZ - z : 0;
                highBoundZ = (z + 15 > area.maxZ) ? area.maxZ - z : 15;

                // Loop on the extends around the player's layer (6 down, 2 up)
                for ( int curExtend = area.minChunkY; curExtend <= area.maxChunkY; curExtend++ )
                {
                    ChunkSection ebs = extendsList[curExtend];
                    if (ebs == null) // happens quite often!
                        continue;

                    // Pre-compute the extend bounds on Y
                    int y = curExtend << 4;
                    lowBoundY = (y < area.minY) ? area.minY - y : 0;
                    highBoundY = (y + 15 > area.maxY) ? area.maxY - y : 15;

                    // Now that we have an extend, let's check all its blocks
                    for ( int i = lowBoundX; i <= highBoundX; i++ ) {
                        for ( int j = lowBoundY; j <= highBoundY; j++ ) {
                            for ( int k = lowBoundZ; k <= highBoundZ; k++ ) {
                                currentState = ebs.getBlockState(i, j, k);
                                currentFluid = currentState.getFluidState();

                                renderQueue.add(new BlockPos(i, j, k));
//                                if( (currentFluid.getFluid() == Fluids.LAVA || currentFluid.getFluid() == Fluids.FLOWING_LAVA) && Controller.isLavaActive() ) {
//                                    renderQueue.add(new RenderBlockProps(x + i, y + j, z + k, 0xff0000));
//                                    continue;
//                                }
//
//                                // Reject blacklisted blocks
//                                if( Controller.blackList.contains(currentState.getBlock()) )
//                                    continue;
//
//                                block = currentState.getBlock().getRegistryName();
//                                if( block == null )
//                                    continue;
//
//                                dataWithUUID = Controller.getBlockStore().getStoreByReference(block.toString());
//                                if( dataWithUUID == null )
//                                    continue;
//
//                                if( dataWithUUID.getKey() == null || !dataWithUUID.getKey().isDrawing() ) // fail safe
//                                    continue;
//
//                                // Push the block to the render queue
//                                renderQueue.add(new RenderBlockProps(x + i, y + j, z + k, dataWithUUID.getKey().getColor()));
                            }
                        }
                    }
                }
            }
        }

        return renderQueue;
    }

    public static class ScanArea {
        public int minX;
        public int minY;
        public int minZ;
        public int maxX;
        public int maxY;
        public int maxZ;

        // Holds the chunk bounds for the area
        public int minChunkX;
        public int minChunkY;
        public int minChunkZ;
        public int maxChunkX;
        public int maxChunkY;
        public int maxChunkZ;


        /**
         * Constructs a world region from a player location and a radius.
         * Vertical extend is 92 blocks down and 32 blocks up
         * @param pos a world position
         * @param radius a block radius
         */
        public ScanArea(Vec3i pos, int radius)
        {
            minX = pos.getX() - radius;
            maxX = pos.getX() + radius;
            minY = Math.max(0, pos.getY() - 92);
            maxY = Math.min(255, pos.getY() + 32);
            minZ = pos.getZ() - radius;
            maxZ = pos.getZ() + radius;
            minChunkX = minX >> 4;
            maxChunkX = maxX >> 4;
            minChunkY = minY >> 4;
            maxChunkY = maxY >> 4;
            minChunkZ = minZ >> 4;
            maxChunkZ = maxZ >> 4;
        }
    }
}
