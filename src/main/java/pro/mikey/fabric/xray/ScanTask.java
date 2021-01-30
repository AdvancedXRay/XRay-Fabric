package pro.mikey.fabric.xray;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;

import java.util.*;

public class ScanTask implements Runnable {
  public ScanTask() {
  }

  @Override
  public void run() {
    ScanController.renderQueue.clear();
    ScanController.renderQueue.addAll(this.collectBlocks());
  }

  /**
   * This is an "exact" copy from the forge version of the mod but with the optimisations
   * that the rewrite (Fabric) version has brought like chunk location based cache, etc.
   * <p>
   * This is only run if the cache is invalidated.
   *
   * @implNote Using the {@link BlockPos#iterate(BlockPos, BlockPos)} may be a better system for the scanning.
   */
  private List<BlockPos> collectBlocks() {
    Set<Block> blocks = ScanController.scanningBlocks;

    // If we're not looking for blocks, don't run.
    if (blocks.isEmpty()) {
      if (!ScanController.renderQueue.isEmpty()) {
        ScanController.renderQueue.clear();
      }
      return new ArrayList<>();
    }

    MinecraftClient instance = MinecraftClient.getInstance();

    final World world = instance.world;
    final PlayerEntity player = instance.player;

    // Just stop if we can't get the player or world.
    if (world == null || player == null) {
      return new ArrayList<>();
    }

    final List<BlockPos> renderQueue = new ArrayList<>();

    int cX = player.chunkX;
    int cZ = player.chunkZ;

    int range = 2 / 2;
    System.out.println("Running");
    for (int i = cX - range; i <= cX + range; i++) {
      int chunkStartX = i << 4;
      for (int j = cZ - range; j <= cZ + range; j++) {
        int chunkStartZ = j << 4;

        int height = Arrays.stream(world.getChunk(i, j).getSectionArray())
            .filter(Objects::nonNull)
            .mapToInt(ChunkSection::getYOffset)
            .max().orElse(0);

        for (int k = chunkStartX; k < chunkStartX + 16; k++) {
          for (int l = chunkStartZ; l < chunkStartZ + 16; l++) {
            for (int m = 0; m < height + (1 << 4); m++) {
              BlockPos pos = new BlockPos(k, m, l);
              if (isValidBlock(pos, world, blocks)) {
                renderQueue.add(pos);
              }
            }
          }
        }
      }
    }

    return renderQueue;
  }

  private boolean isValidBlock(BlockPos pos, World world, Set<Block> blocks) {
    BlockState state = world.getBlockState(pos);
    return !state.isAir() && blocks.contains(state.getBlock());
  }
}
