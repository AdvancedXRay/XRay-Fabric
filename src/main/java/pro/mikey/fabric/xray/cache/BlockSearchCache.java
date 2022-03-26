package pro.mikey.fabric.xray.cache;

import pro.mikey.fabric.xray.records.BlockEntry;
import pro.mikey.fabric.xray.records.BlockGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Physical representation of what we're searching for. Contains the actual state, Scraps unneeded
 * data and cleans up some logic along the way.
 *
 * <p>I control when this list is populated and has changes through the first load and the gui.
 */
public class BlockSearchCache {
  private Set<BlockSearchEntry> cache = new HashSet<>();

  public void processGroupedList(List<BlockGroup> blockEntries) {
    // Flatten the grouped list down to a single cacheable list
    this.cache =
        blockEntries.stream()
            .flatMap(
                e ->
                    e.entries().stream()
                        .filter(BlockEntry::isActive)
                        .map(a -> new BlockSearchEntry(a.getState(), a.getHex(), a.isDefault())))
            .collect(Collectors.toSet());
  }

  public Set<BlockSearchEntry> get() {
    return this.cache;
  }
}
