package pro.mikey.fabric.xray.records;

import java.util.List;

public class BlockGroup {
    List<BlockEntry> entries;
    int order;
    boolean active;

    public BlockGroup(List<BlockEntry> entries, int order, boolean active) {
        this.entries = entries;
        this.order = order;
        this.active = active;
    }
}
