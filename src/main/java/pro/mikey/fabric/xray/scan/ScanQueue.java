package pro.mikey.fabric.xray.scan;

import net.minecraft.util.math.ChunkPos;
import pro.mikey.fabric.xray.storage.Stores;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

class ScanQueue {
  private static ScanQueue instance;

  private final Queue<ChunkPos> add = new LinkedList<>();
  private final Queue<ChunkPos> remove = new LinkedList<>();
  private final Set<ChunkPos> chunks = new HashSet<>();

  static ScanQueue getInstance() {
    if (instance == null) {
      instance = new ScanQueue();
    }

    return instance;
  }

  /** Process out queues data. */
  synchronized void process() {
    // Don't run if there is nothing to process
    if (this.queuesEmpty() && !Stores.SETTINGS.get().isActive()) {
      return;
    }

    // Always handle removal first.
    if (!this.remove.isEmpty()) {
      ChunkPos removeChunk = this.remove.poll();
      ScanController.activeChunks.remove(removeChunk);
      System.out.printf("Processing [%s] for removal%n", removeChunk);
    }

    if (!this.add.isEmpty()) {
      ChunkPos addChunk = this.add.poll();
      ScanController.activeChunks.add(addChunk);
      System.out.printf("Processing [%s] for additions%n", addChunk);
    }
    //    Util.getMainWorkerExecutor().execute(new ScanTask());
  }

  public void add(ChunkPos chunk) {}

  private void remove(ChunkPos chunk) {}

  private boolean queuesEmpty() {
    return this.add.isEmpty() && this.remove.isEmpty();
  }
}
