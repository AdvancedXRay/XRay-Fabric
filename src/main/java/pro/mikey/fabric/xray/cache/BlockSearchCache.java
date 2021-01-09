package pro.mikey.fabric.xray.cache;

import pro.mikey.fabric.xray.records.BasicColor;
import pro.mikey.fabric.xray.records.BlockGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Physical representation of what we're searching for. Contains the actual state,
 * Scraps unneeded data and cleans up some logic along the way.
 *
 * I control when this list is populated and has changes through the first load
 * and the gui.
 */
public class BlockSearchCache {
    public List<BlockSearchEntry> cache = new ArrayList<>();

    public void processGroupedList(List<BlockGroup> blockEntries) {
        // Flatten the grouped list down to a single cacheable list
        this.cache = blockEntries.stream()
                .flatMap(e -> e.getEntries().stream()
                        .map(a -> new BlockSearchEntry(
                                BlockSearchEntry.blockStateFromStringNBT(a.getState()),
                                BasicColor.of(a.getHex()),
                                a.isDefault())
                        ))
                .distinct()
                .collect(Collectors.toList());
    }

    public List<BlockSearchEntry> get() {
        return cache;
    }
}
