package pro.mikey.fabric.xray.scan;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScanTask implements Runnable {
  ScanTask() {}

  @Override
  public void run() {
    //    Map<ChunkPos, Set<BlockPos>> existing = new HashMap<>(ScanController.scannedChunks);
    //
    //    ScanController.scannedChunks.clear();
    //    //    ScanController.renderQueue.clear();
    //
    //    Map<ChunkPos, Set<BlockPos>> scanned = this.collectBlocks(existing);
    //    ScanController.scannedChunks.putAll(scanned);
    //
    //    Set<BlockPos> collect =
    //        scanned.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    //
    //    synchronized (ScanController.renderQueue) {
    //      if (ScanController.renderQueue.size() != collect.size()) {
    //        XRay.xrayRenderer.firstRender = true;
    //      }
    //
    //      ScanController.renderQueue.clear();
    //      ScanController.renderQueue.addAll(collect);
    //      //      ScanController.renderQueue.retainAll(collect);
    //      System.out.println(ScanController.renderQueue.size());
    //    }
    //
    //    System.out.println("Chunks scanned and stored: " + ScanController.scannedChunks.size());
  }

  /**
   * This is an "exact" copy from the forge version of the mod but with the optimisations that the
   * rewrite (Fabric) version has brought like chunk location based cache, etc.
   *
   * <p>This is only run if the cache is invalidated.
   *
   * @implNote Using the {@link BlockPos#iterate(BlockPos, BlockPos)} may be a better system for the
   *     scanning.
   */
  private Map<ChunkPos, Set<BlockPos>> collectBlocks(
      Map<ChunkPos, Set<BlockPos>> existingChunkData) {
    //    Set<Block> blocks = ScanController.scanningBlocks;
    //
    //    // If we're not looking for blocks, don't run.
    //    if (blocks.isEmpty()) {
    //      if (!ScanController.renderQueue.isEmpty()) {
    //        ScanController.renderQueue.clear();
    //      }
    //      return new HashMap<>();
    //    }
    //
    //    MinecraftClient instance = MinecraftClient.getInstance();
    //
    //    final World world = instance.world;
    //    final PlayerEntity player = instance.player;
    //    // Just stop if we can't get the player or world.
    //    if (world == null || player == null) {
    return new HashMap<>();
    //    }
    //
    //    int cX = player.chunkX;
    //    int cZ = player.chunkZ;
    //
    //    int range = 16 / 2;
    //    int skippedChunks = 0;
    //    final List<ChunkPos> chunksFound = new ArrayList<>();
    //    for (int i = cX - range; i <= cX + range; i++) {
    //      int chunkStartX = i << 4;
    //      for (int j = cZ - range; j <= cZ + range; j++) {
    //        int chunkStartZ = j << 4;
    //        final ChunkPos chunkPos = new ChunkPos(i, j);
    //
    //        // Don't scan this chunk we already know about it.
    //        if (existingChunkData.containsKey(chunkPos)) {
    //          chunksFound.add(chunkPos);
    //          skippedChunks++;
    //          continue;
    //        }
    //
    //        int height =
    //            Arrays.stream(world.getChunk(i, j).getSectionArray())
    //                .filter(Objects::nonNull)
    //                .mapToInt(ChunkSection::getYOffset)
    //                .max()
    //                .orElse(0);
    //
    //        final Set<BlockPos> posList = new HashSet<>();
    //        for (int k = chunkStartX; k < chunkStartX + 16; k++) {
    //          for (int l = chunkStartZ; l < chunkStartZ + 16; l++) {
    //            for (int m = 0; m < height + (1 << 4); m++) {
    //              BlockPos pos = new BlockPos(k, m, l);
    //              if (this.isValidBlock(pos, world, blocks)) {
    //                posList.add(pos);
    //              }
    //            }
    //          }
    //        }
    //
    //        chunksFound.add(chunkPos);
    //        existingChunkData.put(chunkPos, posList);
    //      }
    //    }
    //
    //    System.out.println("Chunks Skipped: " + skippedChunks);
    //
    //    // Remove all now missing chunks from the stream.
    //    // As we only add to the existing list before ending the scan, it's important
    //    // that we remove the old chunks we no longer care about.
    //    existingChunkData.keySet().retainAll(chunksFound);
    //
    //    return existingChunkData;
  }

  private boolean isValidBlock(BlockPos pos, World world, Set<Block> blocks) {
    BlockState state = world.getBlockState(pos);
    return !state.isAir() && blocks.contains(state.getBlock());
  }
}
