package pro.mikey.fabric.xray.scan;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import pro.mikey.fabric.xray.storage.Stores;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ScanController {
  public static final Set<BlockPos> renderQueue = Collections.synchronizedSet(new HashSet<>());
  public static final Set<ChunkPos> activeChunks = Collections.synchronizedSet(new HashSet<>());
  public static final Set<ChunkPos> scannedChunks = new HashSet<>();

  // Temp
  static final Set<Block> scanningBlocks =
      new HashSet<>(
          Arrays.asList(
              Blocks.DIAMOND_ORE,
              Blocks.REDSTONE_ORE,
              Blocks.IRON_ORE,
              Blocks.GOLD_ORE,
              Blocks.COAL_ORE,
              Blocks.LAPIS_ORE,
              Blocks.EMERALD_ORE));

  private static ChunkPos playerLastChunk;

  /** No point even running if the player is still in the same chunk. */
  private static boolean playerLocationChanged() {
    ClientPlayerEntity player = MinecraftClient.getInstance().player;
    if (player == null) {
      return false;
    }

    // TODO: extend to a 2x2 grid | 4x4 grid
    return playerLastChunk == null
        || playerLastChunk.x != player.chunkX
        || playerLastChunk.z != player.chunkZ;
  }

  /**
   * Runs the scan task by checking if the thread is ready but first attempting to provide the cache
   * if the cache is still valid.
   *
   * @param forceRerun if the task is required to re-run for instance, a block is broken in the
   *     world.
   */
  public static void runTask(boolean forceRerun) {
    MinecraftClient client = MinecraftClient.getInstance();
    if (client.player == null && client.world == null) {
      return;
    }

    final ScanQueue scanner = ScanQueue.getInstance();
    scanner.process();

    // Don't run and don't add to the queue unless the player has xray active and the
    // player has moved out of their last location
    if (!Stores.SETTINGS.get().isActive() || ((!forceRerun && !playerLocationChanged()))) {
      return;
    }

    // Update the players last chunk to eval against above.
    playerLastChunk = new ChunkPos(client.player.chunkX, client.player.chunkZ);
    findChunks();
  }

  private static void findChunks() {
    ClientPlayerEntity player = MinecraftClient.getInstance().player;
    if (player == null) {
      return;
    }

    int cX = player.chunkX;
    int cZ = player.chunkZ;

    int range = 3 / 2;
    //    int skippedChunks = 0;

    Set<ChunkPos> chunks = new HashSet<>();
    for (int i = cX - range; i <= cX + range; i++) {
      for (int j = cZ - range; j <= cZ + range; j++) {
        chunks.add(new ChunkPos(i, j));
      }
    }

    //    Set<ChunkPos> toAdd = new HashSet<>()
  }
}
